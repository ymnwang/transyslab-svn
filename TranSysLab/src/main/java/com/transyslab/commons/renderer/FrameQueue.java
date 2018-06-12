package com.transyslab.commons.renderer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import com.transyslab.roadnetwork.VehicleData;

//˫������У�һ���ɶ��������ߣ���һ����д�������ߣ���
//��Ⱦ�߳̽��ж������������߳̽���д����
//һ����ԣ������д���ٶ�>��Ⱦ�Ķ�ȡ�ٶ�
//Ϊ�����ȱ�֤��Ⱦ�����������ɶ�����Ϊ�����������д���н��н�����
public class FrameQueue {
	
	//����֡����
    private final static int capacity_ = 30;
    //����������֤д�����Ͷ��н����������̰߳�ȫ
    private ReentrantLock  writeLock_;  
    //�������������ڻ��ѷ���д�����
    private Condition notFull_;  
  
    //�ɶ�����д����  
    private AnimationFrame[] writeArray_, readArray_;
    //���в�����  
    private volatile int writeCount_, readCount_;
    //����ͷβ����  
    private int writeArrayHP_, writeArrayTP_, readArrayHP_, readArrayTP_;  
    
    //֡��
    private int frameCount;
    
    //����ģʽ
    private static FrameQueue theFrameQueue;
    public static FrameQueue getInstance(){
    	if(theFrameQueue == null)
    		 theFrameQueue = new FrameQueue(capacity_) ;
    	return theFrameQueue;
    }
    //main�̵߳��ã���ʼ��������Ԫ��
    public void initFrameQueue(){

    	/*
    	for(int i=0;i<capacity_;i++){
    		writeArray_[i] = new AnimationFrame();
    		readArray_[i] = new AnimationFrame();
    	}*/
    }
    public int getFrameCount(){
    	return frameCount;
    }
    public FrameQueue(int capacity){

        if(capacity<=0)  
        {  
            throw new IllegalArgumentException("Queue initial capacity can't less than 0!");  
        }
        frameCount = 0;
        readArray_ = new AnimationFrame[capacity];  
        writeArray_ = new AnimationFrame[capacity];  
        
        writeLock_ = new ReentrantLock();
        notFull_ = writeLock_.newCondition(); 
    }  
    //������󣬷��̰߳�ȫ���ɼ�������offer���ñ�֤�̰߳�ȫ
    private void insert(AnimationFrame e)  
    {  
    	
        writeArray_[writeArrayTP_] = e;  
        ++writeArrayTP_;
        //0-capacity
        ++writeCount_;  
    }  
    //ȡ������ �����̰߳�ȫ���ɼ�������poll���ñ�֤�̰߳�ȫ 
    private AnimationFrame extract(boolean isPause)  
    {  
    	AnimationFrame e = readArray_[readArrayHP_];
    	if(!isPause){
            ++readArrayHP_;
            //capacity-0
            --readCount_;  
    	}
        return e;  
    }  
  
      

//��������:  
//�ɶ�����Ϊ��  && ��д�������� 
    private boolean queueSwitch() 
    {
    	//��Ⱦ�߳�ռ��д������������߳��޸�����
        writeLock_.lock();  
        try  
        {
        	//��д����δ��
            if (writeCount_ < writeArray_.length)  
            {   
            	return false;
            }  
            else  
            {  
            	//�������ý���
            	AnimationFrame[] tmpArray = readArray_;  
                readArray_ = writeArray_;  
                writeArray_ = tmpArray;
                
                //���в��������������û򽻻�
                readCount_ = writeCount_;  
                readArrayHP_ = 0;  
                readArrayTP_ = writeArrayTP_;  
  
                writeCount_ = 0;  
                writeArrayHP_ = readArrayHP_;  
                writeArrayTP_ = 0;  
                //�����ѱ��������������ķ����߳�
                //ע�⣺һ����д���������������̻߳ᱻ������������await()����
                notFull_.signal();   
                return true;  
            }  
        }  
        finally  
        {  
        	//��Ⱦ�߳��ͷ�д��
            writeLock_.unlock();  
        }  
    }  

    public void offer(AnimationFrame e) throws InterruptedException 
    {  
        if(e != null) {
            writeLock_.lock();
            try {
                //���������������������߳�
                if(writeCount_ < writeArray_.length) {
                    insert(e);
                }
                else
                    //�����߳������ڴ˴��ȴ���Ⱦ�߳�signal
                    notFull_.await();
            }
            finally {
                writeLock_.unlock();
            }
        }
    }


    public AnimationFrame poll(boolean isPause){
    	//���жϿɶ������Ƿ�Ϊ��
        if(readCount_<=0)  
        {
        	//��������ʧ���򷵻ؿ�֡
        	if(queueSwitch()) 
        		return extract(isPause);
        	else 
        		return null;  
        }
        //�ɶ����в�Ϊ�����ȡ����
        if(readCount_>0) 
        	return extract(isPause);
        return null;
    }
    public void clear(){
        frameCount = 0;
        writeArray_ = new AnimationFrame[capacity_];
        readArray_ = new AnimationFrame[capacity_];
    }
}
