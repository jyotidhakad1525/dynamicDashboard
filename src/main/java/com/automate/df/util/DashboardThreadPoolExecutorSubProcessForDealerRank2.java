package com.automate.df.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DashboardThreadPoolExecutorSubProcessForDealerRank2 {

	private ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 2, 600, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
	private static DashboardThreadPoolExecutorSubProcessForDealerRank2 dashboardTPE = null;

	private DashboardThreadPoolExecutorSubProcessForDealerRank2() {
	}

	public static DashboardThreadPoolExecutorSubProcessForDealerRank2 getInstance() {
		if (dashboardTPE == null) {
			dashboardTPE = new DashboardThreadPoolExecutorSubProcessForDealerRank2();
		}
		return dashboardTPE;
	}

	public void submit(Runnable r) {
		pool.submit(r);
	}
}
