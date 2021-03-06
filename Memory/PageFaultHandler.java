/*Name: Daniel Khuu
ID: 109372156

I pledge my honor that all parts of this project were done by me individually
and without collaboration with anybody else.*/

package osp.Memory;
import java.util.*;
import osp.Hardware.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.FileSys.FileSys;
import osp.FileSys.OpenFile;
import osp.IFLModules.*;
import osp.Interrupts.*;
import osp.Utilities.*;
import osp.IFLModules.*;

/**
    The page fault handler is responsible for handling a page
    fault.  If a swap in or swap out operation is required, the page fault
    handler must request the operation.

    @OSPProject Memory
*/
public class PageFaultHandler extends IflPageFaultHandler
{
    /**
        This method handles a page fault. 

        It must check and return if the page is valid, 

        It must check if the page is already being brought in by some other
	thread, i.e., if the page's has already pagefaulted
	(for instance, using getValidatingThread()).
        If that is the case, the thread must be suspended on that page.
        
        If none of the above is true, a new frame must be chosen 
        and reserved until the swap in of the requested 
        page into this frame is complete. 

	Note that you have to make sure that the validating thread of
	a page is set correctly. To this end, you must set the page's
	validating thread using setValidatingThread() when a pagefault
	happens and you must set it back to null when the pagefault is over.

        If a swap-out is necessary (because the chosen frame is
        dirty), the victim page must be dissasociated 
        from the frame and marked invalid. After the swap-in, the 
        frame must be marked clean. The swap-ins and swap-outs 
        must are preformed using regular calls read() and write().

        The student implementation should define additional methods, e.g, 
        a method to search for an available frame.

	Note: multiple threads might be waiting for completion of the
	page fault. The thread that initiated the pagefault would be
	waiting on the IORBs that are tasked to bring the page in (and
	to free the frame during the swapout). However, while
	pagefault is in progress, other threads might request the same
	page. Those threads won't cause another pagefault, of course,
	but they would enqueue themselves on the page (a page is also
	an Event!), waiting for the completion of the original
	pagefault. It is thus important to call notifyThreads() on the
	page at the end -- regardless of whether the pagefault
	succeeded in bringing the page in or not.

        @param thread the thread that requested a page fault
        @param referenceType whether it is memory read or write
        @param page the memory page 

	@return SUCCESS is everything is fine; FAILURE if the thread
	dies while waiting for swap in or swap out or if the page is
	already in memory and no page fault was necessary (well, this
	shouldn't happen, but...). In addition, if there is no frame
	that can be allocated to satisfy the page fault, then it
	should return NotEnoughMemory

        @OSPProject Memory
    */
        private static int swap_inc = 0; //used to count the number of pages swapped in and out
        private static int swap_out = 0;
    public static int do_handlePageFault(ThreadCB thread, 
					 int referenceType,
					 PageTableEntry page)
    {
        if(page.isValid() == true) //anything valid is a failure
            return FAILURE;

        FrameTableEntry newF = gettingFrames(); //must be at the beginning
        if(newF == null)
            return NotEnoughMemory;

        Event event = new SystemEvent("PageFault");
        thread.suspend(event);

        page.setValidatingThread(thread);
        newF.setReserved(thread.getTask());

        PageTableEntry newP = newF.getPage();
        if(newP != null && newF.isDirty() == true){
            swapOut(thread, newF);

            if(thread.getStatus() == ThreadKill){
                page.notifyThreads();
                event.notifyThreads();
                ThreadCB.dispatch();
                return FAILURE;
            }
            newF.setDirty(false);
        }

        if(newP != null){
            newF.setReferenced(false);
            newF.setPage(null);
            newP.setValid(false);
            newP.setFrame(null);
        }

        page.setFrame(newF);
        swapIn(thread, page);
        if(thread.getStatus() == ThreadKill){
            if((newF.getPage() != null) && (newF.getPage().getTask() == thread.getTask()))
                    newF.setPage(null);

            event.notifyThreads();
            page.notifyThreads();
            page.setValidatingThread(null);
            page.setFrame(null);
            ThreadCB.dispatch();
            return FAILURE;
        }

        TaskCB task = thread.getTask();
        newF.setPage(page);
        page.setValid(true);
        if(newF.getReserved() == task)
            newF.setUnreserved(task);
        
        page.notifyThreads();
        page.setValidatingThread(null);
        event.notifyThreads();
        ThreadCB.dispatch();
        return SUCCESS;

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

    public static FrameTableEntry gettingFrames(){
        FrameTableEntry newF = null;
        FrameTableEntry maxF = null; //this is the frame least recently used
        PageTableEntry newP = null; //for LRU
        long max = -99; //used to calculate the LRU

        for(int i = 0; i < MMU.getFrameTableSize(); i++){
            newF = MMU.getFrame(i);
            if(newF.isReserved() == false){
                if(newF.getLockCount() == 0)
                    return newF;
            }
        }

        //all these frames aren't reserved, so we pick pick by LRU algorithm    
        for(int i = 0; i < MMU.getFrameTableSize(); i++){
            newF = MMU.getFrame(i);
            newP = newF.getPage();
            if((HClock.get() - newP.getTime()) > max){ //as we go through the for loop, the frame with the largest difference between stopwatches is the LRU
                maxF = newF;
                max = HClock.get() - newP.getTime();
            }
        }
        return maxF;
        
    }

    public static void swapIn(ThreadCB thread, PageTableEntry page){
        TaskCB newTask = page.getTask();
        newTask.getSwapFile().read(page.getID(), page, thread);
        swap_inc++;
        //System.out.println("S.IN: " + swap_inc);
    }

    public static void swapOut(ThreadCB thread, FrameTableEntry frame){
        PageTableEntry newP = frame.getPage();
        TaskCB newTask = newP.getTask();
        newTask.getSwapFile().write(newP.getID(), newP, thread);
        swap_out++;
        //System.out.println("S.OUT: " + swap_out);
    }

}

/*
      Feel free to add local classes to improve the readability of your code
*/
