/**
 * All rights reserved. This material is confidential and proprietary to HITALK team.
 */
package com.recharge.base.config;

import java.util.HashSet;

/**
 * <pre>
 * Config的Key
 * </pre>
 * 
 * @author reison
 * @time 2019年7月27日
 */
public class ConfigKey {

	// ---------------------各个进程启动配置----------------------//
	public final static String GAME_SERVER = "game_server";// 游戏服ip
	public final static String GAME_PORT = "game_port";// 游戏服ip
	public final static String ROOM_SERVER = "room_server";// 房间服ip
	public final static String ROOM_PORT = "room_port";// 房间服端口
	public final static String GATE_PORT = "gate_port";// gate端口

	// ---------------------mysql配置--------------------------------------------//
	public final static String MYSQL_CONF = "mysql";
	public final static String HOST = "host";
	public final static String PORT = "port";
	public final static String PASS = "pass";
	public final static String USER = "user";
	public final static String DB_DATA = "db_data";
	public final static String DB_LOG = "db_log";

	// ---------------------其他配置（暂时用不上）--------------------------------------------//
	public final static String REDIS_CONF = "redis";
	public final static String WEB_SERVER = "web_server";
	public final static String SERVER_ID = "server_id";// 本游戏服的唯一Id
	public final static String CROSS_IP = "crossip";// 本机器所属跨服的机器IP
	public final static String CRO_SERVER_IDX = "crosvridx";// 连接crossip机器的第几个跨服进程
	public final static String GRP_CONF = "grpconf";// 活动分组配置的可以
	public static int tver;// 登录服当前版本号

	public final static HashSet<String> groupSet = new HashSet<>();

}
