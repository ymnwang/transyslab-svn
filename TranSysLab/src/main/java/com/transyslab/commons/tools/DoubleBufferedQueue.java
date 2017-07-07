package com.transyslab.commons.tools;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DoubleBufferedQueue<E> {
	
	  
    /** The queued items  */  
    private final E[] itemsA_;  
    private final E[] itemsB_;  
      
    private ReentrantLock readLock_, writeLock_;  
//    private Condition notEmpty_;  
    private Condition notFull_;  
//    private Condition awake_;  
      
    private E[] writeArray_, readArray_;  
    private volatile int writeCount_, readCount_;  
    private int writeArrayHP_, writeArrayTP_, readArrayHP_, readArrayTP_;  
      
      
    public DoubleBufferedQueue(int capacity){

        if(capacity<=0)  
        {  
            throw new IllegalArgumentException("Queue initial capacity can't less than 0!");  
        }  
          
        itemsA_ = (E[])new Object[capacity];  
        itemsB_ = (E[])new Object[capacity];  
  
        readLock_ = new ReentrantLock();  
        writeLock_ = new ReentrantLock();  
          
//        notEmpty_ = readLock_.newCondition();  
//        notFull_ = writeLock_.newCondition();  
//        awake_ = writeLock_.newCondition();  
          
        readArray_ = itemsA_;  
        writeArray_ = itemsB_;  
    }  
   public E[] getWriteArray(){
	   return writeArray_;
   }
    private void insert(E e)  
    {  
        writeArray_[writeArrayTP_] = e;  
        ++writeArrayTP_;  
        ++writeCount_;  
    }  
      
    private E extract()  
    {  
        E e = readArray_[readArrayHP_];  
        readArray_[readArrayHP_] = null;  
        ++readArrayHP_;  
        --readCount_;  
        return e;  
    }  
  
      
    /** 
     *switch condition:  
     *read queue is empty && write queue is not empty 
     *  
     *Notice:This function can only be invoked after readLock_ is  
         * grabbed,or may cause dead lock 
     * @param
     * @param : whether need to wait forever until some other
     * thread awake_ it 
     * @return 
     * @throws InterruptedException 
     */  
    private boolean queueSwitch() 
    {  
        writeLock_.lock();  
        try  
        {
        	//小于容量则不交换
            if (writeCount_ < writeArray_.length)  
            {   
            	return false;
            }  
            else  
            {  
                E[] tmpArray = readArray_;  
                readArray_ = writeArray_;  
                writeArray_ = tmpArray;  
  
                readCount_ = writeCount_;  
                readArrayHP_ = 0;  
                readArrayTP_ = writeArrayTP_;  
  
                writeCount_ = 0;  
                writeArrayHP_ = readArrayHP_;  
                writeArrayTP_ = 0;  
                  
                notFull_.signal();  
//	                logger.debug("Queue switch successfully!");  
                return true;  
            }  
        }  
        finally  
        {  
            writeLock_.unlock();  
        }  
    }  
  
    public void offer(E e) throws InterruptedException 
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
    public E poll(){    
        if(readCount_>0)  
        {  
            return extract();  
        } 
        else 
        	queueSwitch();
        return null;  
    }  

}
