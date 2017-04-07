/*Name: Daniel Khuu
ID: 109372156

I pledge my honor that all parts of this project were done by me individually
and without collaboration with anybody else.*/

package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;

/**
    The MMU class contains the student code that performs the work of
    handling a memory reference.  It is responsible for calling the
    interrupt handler if a page fault is required.

    @OSPProject Memory
*/
public class MMU extends IflMMU
{
    /** 
        This method is called once before the simulation starts. 
	Can be used to initialize the frame table and other static variables.

        @OSPProject Memory
    */
    public static void init()
    {
        for(int i = 0; i < MMU.getFrameTableSize(); i++){
        	MMU.setFrame(i, new FrameTableEntry(i));
        }

    }

    /**
       This method handlies memory references. The method must 
       calculate, which memory page contains the memoryAddress,
       determine, whether the page is valid, start page fault 
       by making an interrupt if the page is invalid, finally, 
       if the page is still valid, i.e., not swapped out by another 
       thread while this thread was suspended, set its frame
       as referenced and then set it as dirty if necessary.
       (After pagefault, the thread will be placed on the ready queue, 
       and it is possible that some other thread will take away the frame.)
       
       @param memoryAddress A virtual memory address
       @param referenceType The type of memory reference to perform 
       @param thread that does the memory access
       (e.g., MemoryRead or MemoryWrite).
       @return The referenced page.

       @OSPProject Memory
    */
    static public PageTableEntry do_refer(int memoryAddress,
					  int referenceType, ThreadCB thread)
    {
        int pagesNum = memoryAddress / (int) Math.pow(2, (MMU.getVirtualAddressBits() - MMU.getPageAddressBits()));
        PageTableEntry page = thread.getTask().getPageTable().pages[pagesNum];

        if(page.isValid() == true){
            if(referenceType == MemoryWrite) //we only set the dirty when the page is being written to
                page.getFrame().setDirty(true);
            page.getFrame().setReferenced(true);

            page.setTime(HClock.get()); //you still need to update the stopwatch
            return page;
        }

        //the page is invalid
        if(page.getValidatingThread() != null){ //OPTION 1
            thread.suspend(page);

            if(thread.getStatus() != ThreadKill){
                page.getFrame().setReferenced(true);
                if(referenceType == MemoryWrite)
                    page.getFrame().setDirty(true);
            }
            return page; //when you suspend, you don't update the spotwatch, let it flow

        }else{ //OPTION 2 || MUST DO A PAGEFAULT
            InterruptVector.setPage(page);
            InterruptVector.setReferenceType(referenceType);
            InterruptVector.setThread(thread);
            CPU.interrupt(PageFault);
        }

        if(thread.getStatus() != ThreadKill){
            if(referenceType == MemoryWrite)
                page.getFrame().setDirty(true);
            page.getFrame().setReferenced(true);
        }

        page.setTime(HClock.get()); //the page was changed, so we need to take another timestamp
        return page;

    }

    /** Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures
	in their state just after the error happened.  The body can be
	left empty, if this feature is not used.
     
	@OSPProject Memory
     */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.
     
      @OSPProject Memory
     */
    public static void atWarning()
    {
        // your code goes here

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
