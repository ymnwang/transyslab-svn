package com.transyslab.commons.tools.mutitask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
		this(100);
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
//			System.out.println("DEBUG: Task added at " + LocalDateTime.now());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected Task fetchUnspecificTask() {
		try {
			return undoneTasksQueue.poll(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected synchronized Task fetchSpecificTask() {
		Task theTask = undoneTasksQueue.stream()
				.filter(t -> t.workerName.equals(Thread.currentThread().getName()))
				.findFirst().orElse(null);
		if (theTask != null) {
			undoneTasksQueue.remove(theTask);
		}
		return theTask;
	}
}
