package com.automate.df.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DashboardThreadPoolExecutorSubProcess2 {

	private ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 2, 600, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
	private static DashboardThreadPoolExecutorSubProcess2 dashboardTPE = null;

	private DashboardThreadPoolExecutorSubProcess2() {
	}

	public static DashboardThreadPoolExecutorSubProcess2 getInstance() {
		if (dashboardTPE == null) {
			dashboardTPE = new DashboardThreadPoolExecutorSubProcess2();
		}
		return dashboardTPE;
	}

	public void submit(Runnable r) {
		pool.submit(r);
	}
}
