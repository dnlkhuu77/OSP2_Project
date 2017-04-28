/*Name: Daniel Khuu
ID: 109372156

I pledge my honor that all parts of this project were done by me individually
and without collaboration with anybody else.*/

package osp.Devices;

/**
    This class stores all pertinent information about a device in
    the device table.  This class should be sub-classed by all
    device classes, such as the Disk class.

    @OSPProject Devices
*/

import osp.IFLModules.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Memory.*;
import osp.FileSys.*;
import osp.Tasks.*;
import java.util.*;

public class Device extends IflDevice
{
    int head = 0;
    /**
        This constructor initializes a device with the provided parameters.
    As a first statement it must have the following:

        super(id,numberOfBlocks);

    @param numberOfBlocks -- number of blocks on device

        @OSPProject Devices
    */
    public Device(int id, int numberOfBlocks)
    {
        super(id, numberOfBlocks);
        iorbQueue = new GenericList();

    }

    /**
       This method is called once at the beginning of the
       simulation. Can be used to initialize static variables.

       @OSPProject Devices
    */
    public static void init()
    {
        // your code goes here

    }

    /**
       Enqueues the IORB to the IORB queue for this device
       according to some kind of scheduling algorithm.
       
       This method must lock the page (which may trigger a page fault),
       check the device's state and call startIO() if the 
       device is idle, otherwise append the IORB to the IORB queue.

       @return SUCCESS or FAILURE.
       FAILURE is returned if the IORB wasn't enqueued 
       (for instance, locking the page fails or thread is killed).
       SUCCESS is returned if the IORB is fine and either the page was 
       valid and device started on the IORB immediately or the IORB
       was successfully enqueued (possibly after causing pagefault pagefault)
       
       @OSPProject Devices
    */
    public int do_enqueueIORB(IORB iorb)
    {
        PageTableEntry page = iorb.getPage(); //sure?
        page.lock(iorb);

        iorb.getOpenFile().incrementIORBCount();

        int block_size = (int) Math.pow(2, (MMU.getVirtualAddressBits() - MMU.getPageAddressBits())); //to the power of 2?
        int bytespertrack = ((Disk) this).getSectorsPerTrack() * ((Disk) this).getBytesPerSector();
        int numbBlocksperTrack = bytespertrack / block_size;
        int numbBlocksperCylinder = numbBlocksperTrack * ((Disk) this).getPlatters();
        int cylinder = iorb.getBlockNumber() / numbBlocksperCylinder;

        iorb.setCylinder(cylinder);

        ThreadCB thread = iorb.getThread();
        if(thread.getStatus() == ThreadKill)
            return FAILURE;

        if(isBusy() == true){
            inserting(iorb, cylinder);
        }
        else
            startIO(iorb);

        return SUCCESS;

    }

    /**
       Selects an IORB (according to some scheduling strategy)
       and dequeues it from the IORB queue.

       @OSPProject Devices
    */
    public IORB do_dequeueIORB()
    {
        //pick which iorb to delete according to the CSCAN algorithm
        GenericList a = new GenericList();
        a = (GenericList) iorbQueue;
        Enumeration list = a.forwardIterator();
        int flag = 0; //if the elements in front of the queue is empty, we must do the loop again with head = 0

        IORB item = null;

        if(a.isEmpty())
            return null;

        while(list.hasMoreElements()){ //for C-SCAN, we want to get next element after the head
            item = (IORB) list.nextElement();
            if(head <= item.getCylinder()){ //we are at the head (or element after head) of the queue, which will be dequeued
                flag = 1;
            }
        }

        if(flag == 0){ //if there is nothing after the head, C-SCAN requires that the head will go back to cylinder 0
            head = 0;
        }

        if(flag == 1)
            a.remove(item);
        else
            item = (IORB) a.removeHead();

        head = item.getCylinder(); //set the new head
        return item;

    }

    /**
        Remove all IORBs that belong to the given ThreadCB from 
    this device's IORB queue

        The method is called when the thread dies and the I/O 
        operations it requested are no longer necessary. The memory 
        page used by the IORB must be unlocked and the IORB count for 
    the IORB's file must be decremented.

    @param thread thread whose I/O is being canceled

        @OSPProject Devices
    */
    public void do_cancelPendingIO(ThreadCB thread)
    {
        GenericList a = new GenericList();
        a = (GenericList) iorbQueue;

        Enumeration list = a.forwardIterator();

        while(list.hasMoreElements()){
            IORB item = (IORB) list.nextElement();
            if(item.getThread() == thread){ //must check if the the IORB is initialed by thread
                item.getPage().unlock();
                item.getOpenFile().decrementIORBCount();

                if(item.getOpenFile().closePending == true && item.getOpenFile().getIORBCount() == 0)
                    item.getOpenFile().close();

                a.remove(item);
            }
        }

    }

    /** Called by OSP after printing an error message. The student can
    insert code here to print various tables and data structures
    in their state just after the error happened.  The body can be
    left empty, if this feature is not used.
    
    @OSPProject Devices
     */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
    can insert code here to print various tables and data
    structures in their state just after the warning happened.
    The body can be left empty, if this feature is not used.
    
    @OSPProject Devices
     */
    public static void atWarning()
    {
        // your code goes here

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */
    public void inserting(IORB iorb, int cylinder_number){
        GenericList a = new GenericList();
        a = (GenericList) iorbQueue;
        Enumeration list = a.forwardIterator();
        IORB item = null;

        if(a.isEmpty())
            a.append(iorb);
        else{
            while(list.hasMoreElements()){ //we want to order the queue in order of cylinder number to go C-SCAN
                item = (IORB) list.nextElement();
                if(item.getCylinder() >= cylinder_number){ //if our cy# is 6, this will break at a higher cylinder (like #8)
                    break;
                }
            }

            a.prependAtCurrent(iorb); //we will insert the element before the higher cylinder number; if at the end of queue, insert anyway
        }

    }

}

/*
      Feel free to add local classes to improve the readability of your code
*/