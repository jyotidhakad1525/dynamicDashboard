package com.automate.df.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DashboardThreadPoolExecutorSubProcessForBranchRank2 {

	private ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 2, 600, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
	private static DashboardThreadPoolExecutorSubProcessForBranchRank2 dashboardTPE = null;

	private DashboardThreadPoolExecutorSubProcessForBranchRank2() {
	}

	public static DashboardThreadPoolExecutorSubProcessForBranchRank2 getInstance() {
		if (dashboardTPE == null) {
			dashboardTPE = new DashboardThreadPoolExecutorSubProcessForBranchRank2();
		}
		return dashboardTPE;
	}

	public void submit(Runnable r) {
		pool.submit(r);
	}
}
