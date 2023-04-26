package com.automate.df.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DashboardThreadPoolExecutorSubProcessForBranchRank1 {

	private ThreadPoolExecutor pool = new ThreadPoolExecutor(2, 2, 600, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
	private static DashboardThreadPoolExecutorSubProcessForBranchRank1 dashboardTPE = null;

	private DashboardThreadPoolExecutorSubProcessForBranchRank1() {
	}

	public static DashboardThreadPoolExecutorSubProcessForBranchRank1 getInstance() {
		if (dashboardTPE == null) {
			dashboardTPE = new DashboardThreadPoolExecutorSubProcessForBranchRank1();
		}
		return dashboardTPE;
	}

	public void submit(Runnable r) {
		pool.submit(r);
	}
}
