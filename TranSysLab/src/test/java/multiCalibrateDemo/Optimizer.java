package multiCalibrateDemo;

import com.transyslab.commons.tools.TaskCenter;

public class Optimizer extends Thread{
	private TaskCenter taskCenter;
	public Optimizer(String threadname, TaskCenter tc) {
		setName(threadname);
		taskCenter = tc;
	}
	
	@Override
	public void run() {
		long bt = System.currentTimeMillis();
		
		int patchScale = 100;
		
		for (int k = 0; k < 3; k++) {
			System.out.println("Generation " + k + " started");
			taskCenter.setTaskAmount(patchScale);//ÿ�ηַ�����ʱ�����ȳ�ʼ�������С
			for (int i = 0; i < patchScale; i++) {
				try {
					taskCenter.undoneTasks.put(new double[] {i, 0.2});//�첽����ÿ�����ӵ�fitness
					System.out.println("TID " + i + " Submitted");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			double [] results = new double [patchScale];
			for (int i = 0; i < patchScale; i++) {
				results[i] = taskCenter.getResult(i);
			}
			System.out.println("Generation " + k + " finished");
		}
		
		taskCenter.Dismiss();
		System.out.println("time used " + (System.currentTimeMillis()-bt));
	}

	public static void main(String[] args) {
		TaskCenter taskCenter = new TaskCenter(100);//�����б�ļ���
		int engCount = 100;//��õ���һ���еĸ�������
		Optimizer agorithm = new Optimizer("agorithm", taskCenter);		
		agorithm.start();
		for (int i = 0; i < engCount; i++) {
			(new EngThread("Eng" + i, taskCenter)).start();
		}
	}

}
