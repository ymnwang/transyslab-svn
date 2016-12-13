/**
 *
 */
package com.transyslab.commons.tools;

/**
 * @author 10540
 *
 */
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

public class RenameThreadFactory implements ThreadFactory {
	private int counter_;
	private int threadNumber_;
	private ConcurrentHashMap hm_;
	public RenameThreadFactory(int tn, ConcurrentHashMap hm) {
		counter_ = 0;
		threadNumber_ = tn;
		hm_ = hm;
	}
	@Override
	public Thread newThread(Runnable r) {
		int index = counter_ % threadNumber_;
		String name = "Thread-" + counter_;
		hm_.put(name, index);
		Thread thread = new Thread(r, name);
		counter_++;
		return thread;
	}
}
