//Name: Daniel Khuu
//ID: 109372156

//I pledge my honor that all parts of this project were done by me individually
//and without collaboration with anybody else.

package osp.Threads;
import java.util.PriorityQueue;
import java.util.Vector;
import java.util.Enumeration;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

//MMU.getPTBR() gets you the page table of the current task
//getTask() gets the task from the page table
//getCurrentThread() gets the current thread from the task

//Context Switch:
// Pre-Empty
// 1. Change state of thread to ThreadWaiting/ThreadReady.
// 2. Get current thread using method above
// 3. Set PTBR to null.
// 4. Change the current league of the previous task to null.

/**
   This class is responsible for actions related to threads, including
   creating, killing, dispatching, resuming, and suspending threads.

   @OSPProject Threads
*/
public class ThreadCB extends IflThreadCB
{

    private static PriorityQueue<ThreadCB> thread_queue;
    private double total_time;
    /**
       The thread constructor. Must call 

       	   super();

       as its first statement.

       @OSPProject Threads
    */
    public ThreadCB()
    {
        super();
        //each thread will have a total_time counter at 0
        total_time = 0;

    }

    /**
       This method will be called once at the beginning of the
       simulation. The student can set up static variables here.
       
       @OSPProject Threads
    */
    public static void init()
    {

        thread_queue = new PriorityQueue<ThreadCB>();

    }

    /** 
        Sets up a new thread and adds it to the given task. 
        The method must set the ready status 
        and attempt to add thread to task. If the latter fails 
        because there are already too many threads in this task, 
        so does this method, otherwise, the thread is appended 
        to the ready queue and dispatch() is called.

	The priority of the thread can be set using the getPriority/setPriority
	methods. However, OSP itself doesn't care what the actual value of
	the priority is. These methods are just provided in case priority
	scheduling is required.

	@return thread or null

        @OSPProject Threads
    */
    static public ThreadCB do_create(TaskCB task)
    {

        if(task.getThreadCount() >= MaxThreadsPerTask || task == null){
            dispatch(); //we do this, so the next call will work!
            return null;
        }
        
        ThreadCB newThread = new ThreadCB();
        newThread.setStatus(ThreadReady);
        
        if(task.getStatus() == TaskTerm){
            return null;
        }

        newThread.setPriority(task.getPriority());
        newThread.setTask(task);

        if(task.addThread(newThread) == FAILURE){
            dispatch();
            return null;
        }

        thread_queue.add(newThread);
        dispatch();
        return newThread;

    }

    /** 
	Kills the specified thread. 

	The status must be set to ThreadKill, the thread must be
	removed from the task's list of threads and its pending IORBs
	must be purged from all device queues.
        
	If some thread was on the ready queue, it must removed, if the 
	thread was running, the processor becomes idle, and dispatch() 
	must be called to resume a waiting thread.
	
	@OSPProject Threads
    */
    public void do_kill()
    {
        //REMAINDER: looping through devics and make sure all pending Ios are killed

        if(getStatus() == ThreadReady){
            thread_queue.remove(this);
        }
        else if(getStatus() == ThreadRunning){
            MMU.getPTBR().getTask().setCurrentThread(null);
        }
        //nothing special to do for ThreadWaiting

        //cancelling the IO
        for(int i = 0; i < Device.getTableSize(); i++){
            Device.get(i).cancelPendingIO(this);
        }

        setStatus(ThreadKill);

        ResourceCB.giveupResources(this);

        dispatch(); //dispatch a new thread

        if(getTask().getThreadCount() == 0){
            getTask().kill();
        }


    }

    /** Suspends the thread that is currenly on the processor on the 
        specified event. 

        Note that the thread being suspended doesn't need to be
        running. It can also be waiting for completion of a pagefault
        and be suspended on the IORB that is bringing the page in.
	
	Thread's status must be changed to ThreadWaiting or higher,
        the processor set to idle, the thread must be in the right
        waiting queue, and dispatch() must be called to give CPU
        control to some other thread.

	@param event - event on which to suspend this thread.

        @OSPProject Threads
    */
    public void do_suspend(Event event)
    {
        if(getStatus() == ThreadRunning){
            setStatus(ThreadWaiting);
            //set the current thread to null
            getTask().setCurrentThread(null);
        }
        else if(getStatus() == ThreadWaiting){
            setStatus(getStatus() + 1); //check this
        }

        event.addThread(this);
        thread_queue.remove(this);
        dispatch();

    }

    /** Resumes the thread.
        
	Only a thread with the status ThreadWaiting or higher
	can be resumed.  The status must be set to ThreadReady or
	decremented, respectively.
	A ready thread should be placed on the ready queue.
	
	@OSPProject Threads
    */
    public void do_resume()
    {
        if(getStatus() == ThreadWaiting){
            setStatus(ThreadReady);
            thread_queue.add(this);
        }
        else{
            setStatus(getStatus() - 1);
        }

        dispatch();

    }

    /** 
        Selects a thread from the run queue and dispatches it. 

        If there is just one theread ready to run, reschedule the thread 
        currently on the processor.

        In addition to setting the correct thread status it must
        update the PTBR.
	
	@return SUCCESS or FAILURE

        @OSPProject Threads
    */
    public static int do_dispatch()
    {
        //THE THREAD IS SCHEDULED USING CPU TIME
        //I think startTime = HClock.get() returns the hardware time in long. 
        //After 100 units of time, it will be startTime + 100. For setting timer, use HTimer class.

        ThreadCB start_thread = null;



        try{
            start_thread = MMU.getPTBR().getTask().getCurrentThread();
        }catch(Exception e){
            System.out.print("This is a null starting thread");
        }

        //USE THE PRIORITIES TO START AND STOP THREADS!
        //CURRENTLY ROBIN

        //If the current thread is not null, set it to ThreadReady
        if(start_thread != null){
            MMU.getPTBR().getTask().setCurrentThread(null);
            MMU.setPTBR(null);
            start_thread.setStatus(ThreadReady);
            thread_queue.add(start_thread);
        }

        ThreadCB activate_thread = null;
        double lesser = Double.MAX_VALUE;
        //WE WANT TO RUN THE THREAD WITH THE LEAST CPU TIME USED!
        for(ThreadCB element: thread_queue){
            if(element.getTime() < lesser){
                lesser = element.getTime();
                activate_thread = element;
            }
        }

        if(thread_queue.size() > 0){
            thread_queue.remove(activate_thread); //CHANGE THIS TO THE ONE WITH THE MOST PRIORITY
            MMU.setPTBR(activate_thread.getTask().getPageTable());
            activate_thread.getTask().setCurrentThread(activate_thread);
            activate_thread.setStatus(ThreadRunning);
            HTimer.set(100);
            activate_thread.setTime(activate_thread.getTimeOnCPU());

            return SUCCESS;
        }

        MMU.setPTBR(null);
        return FAILURE;

    }

    /**
       Called by OSP after printing an error message. The student can
       insert code here to print various tables and data structures in
       their state just after the error happened.  The body can be
       left empty, if this feature is not used.

       @OSPProject Threads
    */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
        can insert code here to print various tables and data
        structures in their state just after the warning happened.
        The body can be left empty, if this feature is not used.
       
        @OSPProject Threads
     */
    public static void atWarning()
    {
        // your code goes here

    }

    public double getTime(){
        return total_time;
    }

    public void setTime(double total_time){
        this.total_time = total_time;
    }

}

/*
      Feel free to add local classes to improve the readability of your code
*/
