package com.transyslab.commons.renderer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import com.transyslab.roadnetwork.VehicleData;

//˫������У�һ���ɶ��������ߣ���һ����д�������ߣ���
//��Ⱦ�߳̽��ж������������߳̽���д����
//һ����ԣ������д���ٶ�>��Ⱦ�Ķ�ȡ�ٶ�
//Ϊ�����ȱ�֤��Ⱦ�����������ɶ�����Ϊ�����������д���н��н�����
public class JOGLFrameQueue {
	
	//����֡����
    private final static int capacity_ = 60;
    //����������֤д�����Ͷ��н����������̰߳�ȫ
    private ReentrantLock  writeLock_;  
    //�������������ڻ��ѷ���д�����
    private Condition notFull_;  
  
    //�ɶ�����д����  
    private JOGLAnimationFrame[] writeArray_, readArray_;
    //���в�����  
    private volatile int writeCount_, readCount_;
    //����ͷβ����  
    private int writeArrayHP_, writeArrayTP_, readArrayHP_, readArrayTP_;  
    
    //����ģʽ
    private static JOGLFrameQueue theFrameQueue;
    public static JOGLFrameQueue getInstance(){
    	if(theFrameQueue == null)
    		 theFrameQueue = new JOGLFrameQueue(capacity_) ;
    	return theFrameQueue;
    }
    //main�̵߳��ã���ʼ��������Ԫ��
    public void initFrameQueue(){
    	
    	for(int i=0;i<capacity_;i++){
    		writeArray_[i] = new JOGLAnimationFrame();
    		readArray_[i] = new JOGLAnimationFrame();
    	}
    }
    public JOGLFrameQueue(int capacity){

        if(capacity<=0)  
        {  
            throw new IllegalArgumentException("Queue initial capacity can't less than 0!");  
        }  
          
        readArray_ = new JOGLAnimationFrame[capacity];  
        writeArray_ = new JOGLAnimationFrame[capacity];  
        
        writeLock_ = new ReentrantLock();            
        notFull_ = writeLock_.newCondition();  


    }  
    //������󣬷��̰߳�ȫ���ɼ�������offer���ñ�֤�̰߳�ȫ
    private void insert(JOGLAnimationFrame e)  
    {  
        writeArray_[writeArrayTP_] = e;  
        ++writeArrayTP_;
        //0-capacity
        ++writeCount_;  
    }  
    //ȡ������ �����̰߳�ȫ���ɼ�������poll���ñ�֤�̰߳�ȫ 
    private JOGLAnimationFrame extract()  
    {  
    	JOGLAnimationFrame e = readArray_[readArrayHP_];   
        ++readArrayHP_;
        //capacity-0
        --readCount_;  
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
            	JOGLAnimationFrame[] tmpArray = readArray_;  
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

    public void offer(JOGLAnimationFrame e) throws InterruptedException 
    {  
        if(e == null)  
        {  
            throw new NullPointerException();  
        }  
    
        writeLock_.lock();  
        try  
        {  
            if(writeCount_ < writeArray_.length)  
            {  
                insert(e);    
            }
            else
            	notFull_.await();           	
        }  
        finally  
        {  
            writeLock_.unlock();  
        }  
    }
    //��ÿһ֡frame����VehicleData��vhcnumΪ������������ȷ��д��ÿһ֡�ĳ���λ����Ϣ
    public void offer(VehicleData vd, int vhcnum) throws InterruptedException 
    {
    	//�����̻߳�ȡд��
        writeLock_.lock();  
        try  
        {  
        	//���������������������߳�
            if(writeCount_ == writeArray_.length)
            	//�����߳������ڴ˴��ȴ���Ⱦ�߳�signal
            	notFull_.await();
            //��������
        	writeArray_[writeArrayTP_].addVehicleData(vd);
        	//ȷ��д��ÿһ֡���г�����λ����Ϣ
        	if(writeArray_[writeArrayTP_].getVhcDataQueue().size() == vhcnum){
        		//����������һ֡
                ++writeArrayTP_;
                ++writeCount_; 
        	}            	           	
        }  
        finally  
        {  
        	//�����߳��ͷ�д��
        	writeLock_.unlock();  
        }  
    }
    public JOGLAnimationFrame poll(){
    	//���жϿɶ������Ƿ�Ϊ��
        if(readCount_<=0)  
        {
        	//��������ʧ���򷵻ؿ�֡
        	if(queueSwitch()) 
        		return extract();
        	else return null;  
        }
        //�ɶ����в�Ϊ�����ȡ����
        //ע�⣺���ﲻ����else����Ϊ��������֮��readCount_>0
        if(readCount_>0) 
        	return extract();
        return null;
    }  
}
