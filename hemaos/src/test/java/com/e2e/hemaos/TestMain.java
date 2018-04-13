package com.e2e.hemaos;

import com.e2e.Main;

public class TestMain {

	public static void main(String[] args) {
		System.setProperty("e2e.schedule.crontab", "0 0/1 * * * ?");// 计划任务任务同步时间 默认45 9,19,29,39,49,59 * * *
																// *
		System.setProperty("spring.profiles.active", "test");// 数据库配置文件 默认使用6.5
		System.setProperty("e2e.schedule.rootPath", "/quite");
		//System.setProperty("epd.default.login", "N");
		Main.main(args);
	}
}