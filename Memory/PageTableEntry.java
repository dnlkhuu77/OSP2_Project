/*Name: Daniel Khuu
ID: 109372156

I pledge my honor that all parts of this project were done by me individually
and without collaboration with anybody else.*/

package osp.Memory;

import osp.Hardware.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Devices.*;
import osp.Utilities.*;
import osp.IFLModules.*;
/**
   The PageTableEntry object contains information about a specific virtual
   page in memory, including the page frame in which it resides.
   
   @OSPProject Memory

*/

public class PageTableEntry extends IflPageTableEntry
{
    private long lru_time; //the timer needed to implement the LRU algorithm
    /**
       The constructor. Must call

       	   super(ownerPageTable,pageNumber);
	   
       as its first statement.

       @OSPProject Memory
    */
    public PageTableEntry(PageTable ownerPageTable, int pageNumber)
    {
        super(ownerPageTable, pageNumber);
        lru_time = HClock.get(); //set the spotwatch needed to do the LRU

    }

    /**
    	WRONG COMMENT!
       This method increases the lock count on the page by one. 

	The method must FIRST increment lockCount, THEN  
	check if the page is valid, and if it is not and no 
	page validation event is present for the page, start page fault 
	by calling PageFaultHandler.handlePageFault().

	@return SUCCESS or FAILURE
	FAILURE happens when the pagefault due to locking fails or the 
	that created the IORB thread gets killed.

	@OSPProject Memory
     */
    public int do_lock(IORB iorb)
    {
        ThreadCB thr = iorb.getThread();
        this.lru_time = HClock.get(); //reset the watch

        if(isValid() == false){ //the validity bit of the page is not valid, pagefault must be initialed
        	if(getValidatingThread() == null){ //page is not involved in pagefault
        		PageFaultHandler.handlePageFault(thr, MemoryLock, this);
        	}else if (getValidatingThread() != thr){ //we must wait until the page is valid
        		thr.suspend(this);
        	}
          else{
            getFrame().incrementLockCount();
            return SUCCESS;
          }
        }

        //if the new thread is ThreadKilled
        if(isValid() == false || thr.getStatus() == ThreadKill){
          return FAILURE;
        }

        getFrame().incrementLockCount();
        return SUCCESS;

    }

    /** This method decreases the lock count on the page by one. 

	This method must decrement lockCount, but not below zero.

	@OSPProject Memory
    */
    public void do_unlock()
    {
        getFrame().decrementLockCount();

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */
    public void setTime(long a){
      lru_time = a;
    }
    public long getTime(){
      return lru_time;
    }

}

/*
      Feel free to add local classes to improve the readability of your code
*/
