package com.usercenter.handler;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.alibaba.fastjson.JSONObject;
import com.utils.JsonUtil;
import com.utils.Log;
import com.utils.StringUtils;
import com.utils.TimeUtil;

import io.netty.util.internal.StringUtil;

import com.usercenter.action.SaveUserInfoAction;
import com.usercenter.base.executor.ExecutorMgr;
import com.usercenter.entity.UserInfo;
import com.usercenter.entity.UserInfoDao;
import com.usercenter.mgr.UserCenterMgr;
import com.usercenter.redis.RedisKey;
import com.usercenter.redis.RedisPool;

public class UserIdHandler extends AbstractHandler {
	/**成功的状态码**/
	public static int SUCCESS = 200;
	/**一天的秒数**/
	public static final int DAY_SEC = 604800;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (!"/center/user_id".equals(target)) {
			Log.info("target!=/center/user_id");
			return;
		}
		String platformId = null;
		Log.info("用户中心收得到登陆请求，查找用户id，信息:" + request);
		JSONObject jsObject = new JSONObject();
		int resultUserId = 0;
		boolean result = false;
		UserInfo userInfo = null;
		try {
			response.setHeader("Content-Type","text/html;charset=utf-8");
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			if(request.getMethod().equals("GET")) {
				if (request.getParameterMap() == null || request.getParameterMap().isEmpty()) {
					Log.error("参数为null!");
					return;
				}
				platformId = request.getParameter("platform_id");
			}else {
				byte[] bytes = new byte[512];
				request.getInputStream().read(bytes);
				String str = new String(bytes);
				JSONObject js = (JSONObject) JsonUtil.parse(str.trim());
				result = js.containsKey("platform_id");
				if(!result) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "platform_id param not find!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("platform_id param not find!");
					return;
				}
				
				result = js.containsKey("game_id");
				if(!result) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "game_id param not find!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("game_id param not find!");
					return;
				}
				
				platformId = js.getString("platform_id");
				int gameId = js.getInteger("game_id");
				String registerPlatform = js.getString("register_platform");
				String nickName = js.getString("nick_name");
				String phoneNo = js.getString("phone_no");
				//校验platformId
				if(StringUtils.isEmpty(platformId)) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "platform_id is empty!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("platform_id is empty!");
					return;
				}
				
				if(StringUtils.isEmpty(registerPlatform)) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "register_platform is empty!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("register_platform is empty!");
					return;
				}
				
				if(StringUtils.isEmpty(nickName)) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "nick_name is empty!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("nick_name is empty!");
					return;
				}
				
				if(StringUtils.isEmpty(phoneNo)) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "phone_no is empty!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("phone_no is empty!");
					return;
				}
				
				if(gameId <=0) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "game_id is invalid!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("game_id is invalid!");
					return;
				}
				//判断是否存在这个游戏
				result = UserCenterMgr.hasGame(gameId);
				if(!result) {
					response.setStatus(500, "no data");
					jsObject.put("err_mess", "this game is not exist for game_id:"+gameId);
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("this game is not exist for game_id:"+gameId);
					return;
				}
				
				//校验玩家数据是否存在
				String value = RedisPool.hgetWithIndex(0,RedisKey.USERS, platformId);
				if(StringUtils.isEmpty(value)) {
					//代表新号,创建新号
					AtomicInteger userId = UserCenterMgr.getMaxUserId(gameId,true);
					resultUserId = userId.incrementAndGet();
					userInfo = new UserInfo();
					Date date = TimeUtil.getSysteCurTime();
					userInfo.setCreate_time(date);
					userInfo.setUser_id(resultUserId);
					userInfo.setGame_id(gameId);
					userInfo.setNick_name(nickName);
					userInfo.setPhone_no(phoneNo);
					userInfo.setPlatform_id(platformId);
					userInfo.setRegister_ip(request.getRemoteAddr());
					userInfo.setRegister_platform(registerPlatform);
					userInfo.setLast_login_time(date);
					userInfo.setOn_line(false);
					ExecutorMgr.getOrderExecutor().enqueue(new SaveUserInfoAction(null, userInfo));
					value = JsonUtil.stringify(userInfo);
					//int deleteTime = DAY_SEC*7;;
					RedisPool.hsetWithIndex(0,RedisKey.USERS,platformId, value,0);
					String ruId = Integer.toString(resultUserId);
					RedisPool.hsetWithIndex(1,RedisKey.UID_2_PID, ruId, platformId,0);
				}
				
				userInfo = JsonUtil.parse(value, UserInfo.class);
				//判断是否在线
				if(userInfo.isOn_line()) {
					response.setStatus(500, "this account already login!");
					jsObject.put("err_mess", "this account already login!");
					jsObject.put("status", "fail!");
					response.getOutputStream().write(jsObject.toJSONString().getBytes());
					Log.error("repeat login for pid:"+platformId);
					return;
				}
			}
			Log.info("platformId:"+platformId);
			jsObject.put("status", "OK");
			jsObject.put("user_id", userInfo.getUser_id());
			response.setStatus(SUCCESS, "OK");
			response.setHeader("Content-Type","text/html;charset=utf-8");
			response.setContentType("application/json");
			response.setCharacterEncoding("utf-8");
			response.getOutputStream().write(jsObject.toJSONString().getBytes());
		} catch (Exception e) {
			Log.error(e.getMessage());
		}finally {
			request.getInputStream().close();
			response.getOutputStream().close();
		}
	}
}
