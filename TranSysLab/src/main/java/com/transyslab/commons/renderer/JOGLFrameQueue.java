package com.transyslab.commons.renderer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import com.transyslab.roadnetwork.VehicleData;

//双缓冲队列，一个可读（消费者），一个可写（生产者）；
//渲染线程进行读操作，仿真线程进行写操作
//一般而言，仿真的写入速度>渲染的读取速度
//为了优先保证渲染读操作，当可读队列为空则马上与可写队列进行交换。
public class JOGLFrameQueue {
	
	//队列帧容量
    private final static int capacity_ = 60;
    //互斥锁，保证写操作和队列交换操作的线程安全
    private ReentrantLock  writeLock_;  
    //条件变量，用于唤醒仿真写入操作
    private Condition notFull_;  
  
    //可读、可写队列  
    private JOGLAnimationFrame[] writeArray_, readArray_;
    //队列操作数  
    private volatile int writeCount_, readCount_;
    //队列头尾索引  
    private int writeArrayHP_, writeArrayTP_, readArrayHP_, readArrayTP_;  
    
    //单例模式
    private static JOGLFrameQueue theFrameQueue;
    public static JOGLFrameQueue getInstance(){
    	if(theFrameQueue == null)
    		 theFrameQueue = new JOGLFrameQueue(capacity_) ;
    	return theFrameQueue;
    }
    //main线程调用，初始化数组内元素
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
    //插入对象，非线程安全，由加锁函数offer调用保证线程安全
    private void insert(JOGLAnimationFrame e)  
    {  
        writeArray_[writeArrayTP_] = e;  
        ++writeArrayTP_;
        //0-capacity
        ++writeCount_;  
    }  
    //取出对象 ，非线程安全，由加锁函数poll调用保证线程安全 
    private JOGLAnimationFrame extract()  
    {  
    	JOGLAnimationFrame e = readArray_[readArrayHP_];   
        ++readArrayHP_;
        //capacity-0
        --readCount_;  
        return e;  
    }  
  
      

//交换条件:  
//可读队列为空  && 可写队列已满 
    private boolean queueSwitch() 
    {
    	//渲染线程占用写锁，避免仿真线程修改数据
        writeLock_.lock();  
        try  
        {
        	//可写队列未满
            if (writeCount_ < writeArray_.length)  
            {   
            	return false;
            }  
            else  
            {  
            	//队列引用交换
            	JOGLAnimationFrame[] tmpArray = readArray_;  
                readArray_ = writeArray_;  
                writeArray_ = tmpArray;
                
                //队列操作数、索引重置或交换
                readCount_ = writeCount_;  
                readArrayHP_ = 0;  
                readArrayTP_ = writeArrayTP_;  
  
                writeCount_ = 0;  
                writeArrayHP_ = readArrayHP_;  
                writeArrayTP_ = 0;  
                //唤醒已被条件变量阻塞的仿真线程
                //注意：一旦可写队列已满，仿真线程会被条件变量调用await()阻塞
                notFull_.signal();   
                return true;  
            }  
        }  
        finally  
        {  
        	//渲染线程释放写锁
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
    //向每一帧frame插入VehicleData，vhcnum为在网车辆数，确保写完每一帧的车辆位置信息
    public void offer(VehicleData vd, int vhcnum) throws InterruptedException 
    {
    	//仿真线程获取写锁
        writeLock_.lock();  
        try  
        {  
        	//当队列已满，阻塞仿真线程
            if(writeCount_ == writeArray_.length)
            	//仿真线程阻塞在此处等待渲染线程signal
            	notFull_.await();
            //插入数据
        	writeArray_[writeArrayTP_].addVehicleData(vd);
        	//确保写完每一帧所有车辆的位置信息
        	if(writeArray_[writeArrayTP_].getVhcDataQueue().size() == vhcnum){
        		//索引跳到下一帧
                ++writeArrayTP_;
                ++writeCount_; 
        	}            	           	
        }  
        finally  
        {  
        	//仿真线程释放写锁
        	writeLock_.unlock();  
        }  
    }
    public JOGLAnimationFrame poll(){
    	//先判断可读队列是否为空
        if(readCount_<=0)  
        {
        	//交换队列失败则返回空帧
        	if(queueSwitch()) 
        		return extract();
        	else return null;  
        }
        //可读队列不为空则读取数据
        //注意：这里不能用else，因为交换队列之后readCount_>0
        if(readCount_>0) 
        	return extract();
        return null;
    }  
}
