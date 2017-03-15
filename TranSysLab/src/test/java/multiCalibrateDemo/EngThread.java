package multiCalibrateDemo;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.transyslab.commons.tools.TaskCenter;

public class EngThread extends Thread{
	private TaskCenter taskCenter;
	private Random rand;
	public EngThread(String ThreadName, TaskCenter b) {
		setName(ThreadName);
		taskCenter = b;
		rand = new Random();
	}
	public double calFitness(double [] para) {
		try {
			sleep(rand.nextInt(1000) + 1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rand.nextDouble();
	}
	@Override
	public void run() {
		while (!taskCenter.isDismissed()) {
			double[] task = null;
			try {
				task = taskCenter.undoneTasks.poll(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (task != null) {
				System.out.println(Thread.currentThread().getName() + " received TID " + (int) task[0]);
				double [] p = new double [task.length - 1];
				for (int i = 0; i < p.length; i++) {
					p[i] = task[i+1];
				}
				double fitVal = calFitness(p);
				taskCenter.setResult((int) task[0], fitVal);				
			}
		}
	}

}
