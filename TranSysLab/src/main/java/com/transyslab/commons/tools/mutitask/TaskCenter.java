package com.transyslab.commons.tools.mutitask;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskCenter {
	private BlockingQueue<Task> undoneTasksQueue;
	private boolean killThreadSignal;

	public TaskCenter(int taskQueueSize) {
		undoneTasksQueue = new ArrayBlockingQueue<>(taskQueueSize);
		killThreadSignal = false;
	}

	public TaskCenter() {
		undoneTasksQueue = new ArrayBlockingQueue<>(100);
		killThreadSignal = false;
	}

	protected synchronized boolean dismissAllowed() {
		return killThreadSignal;
	}

	protected synchronized void dismiss() {
		killThreadSignal = true;
		notify();
	}

	protected void addTask(Task task) {
		try {
			undoneTasksQueue.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected Task fetchTask() {
		try {
			return undoneTasksQueue.poll(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
