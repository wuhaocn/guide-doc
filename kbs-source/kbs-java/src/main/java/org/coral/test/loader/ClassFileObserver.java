package org.coral.test.loader;

import java.io.File;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

public class ClassFileObserver extends Observable {

	private ObserveTask observeTask;

	public ClassFileObserver(String path) {
		observeTask = new ObserveTask(path, this);
	}

	/**
	 * 用于更新观察者
	 * 
	 * @param objects
	 */
	public void sendChanged(Object[] objects) {

		super.setChanged();// 必须调用，否则通知无效
		super.notifyObservers(objects);
	}

	public void reset(String path) {
		if (observeTask != null && !observeTask.isStop) {
			observeTask.isStop = false;
			observeTask.interrupt();
			observeTask = null;
		}
		observeTask = new ObserveTask(path, this);
	}

	/**
	 * 开始观察文件
	 */
	public void startObserve() {
		if (isStop()) {
			System.out.println("--启动类文件更新监控程序--");
			observeTask.isStop = false;
			observeTask.start();
		}
	}

	public boolean isStop() {

		return observeTask != null && !observeTask.isStop;
	}

	/**
	 * 停止观察文件
	 */
	public void stopObserve() {
		System.out.println("--停止类文件更新监控程序--");
		observeTask.isStop = true;
	}

	public static class ObserveTask extends Thread {

		private String path;
		private long lastLoadTime;

		private boolean isStop = false;
		private ClassFileObserver observable;

		public ObserveTask(String path, ClassFileObserver obs) {
			this.path = path;
			this.observable = obs;
			this.lastLoadTime = -1;
		}

		public void run() {
			while (!isStop && this.isAlive()) {
				synchronized (this) {
					long loadTime = getLastLoadTime();
					if (loadTime != this.lastLoadTime) {
						observable.sendChanged(new Object[] { loadTime,
								this.lastLoadTime });

						this.lastLoadTime = loadTime;
					}
					try {
						TimeUnit.SECONDS.sleep(3); // 每隔3秒检查一次文件
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		/**
		 * 将文件最后修改时间作为最后加载时间
		 * 
		 * @return
		 */
		public long getLastLoadTime() {
			if (path == null) {
				return -1;
			}
			File f = new File(path);
			if (!f.exists() || f.isDirectory()) { // 不需要监控目录
				return -1;
			}
			return f.lastModified();
		}
	}

}