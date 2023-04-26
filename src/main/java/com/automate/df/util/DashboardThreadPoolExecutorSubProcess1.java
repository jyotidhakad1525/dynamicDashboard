package com.automate.df.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DashboardThreadPoolExecutorSubProcess1 {

	private ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 2, 600, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
	private static DashboardThreadPoolExecutorSubProcess1 dashboardTPE = null;

	private DashboardThreadPoolExecutorSubProcess1() {
	}

	public static DashboardThreadPoolExecutorSubProcess1 getInstance() {
		if (dashboardTPE == null) {
			dashboardTPE = new DashboardThreadPoolExecutorSubProcess1();
		}
		return dashboardTPE;
	}

	public void submit(Runnable r) {
		pool.submit(r);
	}
}
