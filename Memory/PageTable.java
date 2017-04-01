/*Name: Daniel Khuu
ID: 109372156

I pledge my honor that all parts of this project were done by me individually
and without collaboration with anybody else.*/

package osp.Memory;
/**
    The PageTable class represents the page table for a given task.
    A PageTable consists of an array of PageTableEntry objects.  This
    page table is of the non-inverted type.

    @OSPProject Memory
*/
import java.lang.Math;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Hardware.*;

public class PageTable extends IflPageTable
{
	int numPages;
    /** 
	The page table constructor. Must call
	
	    super(ownerTask)

	as its first statement.

	@OSPProject Memory
    */
    public PageTable(TaskCB ownerTask)
    {
        super(ownerTask);

        numPages = (int) Math.pow(2, MMU.getPageAddressBits());
        pages = new PageTableEntry[numPages];

        for(int i = 0; i < numPages; i++){
        	pages[i] = new PageTableEntry(this, i);
        }

    }

    /**
       Frees up main memory occupied by the task.
       Then unreserves the freed pages, if necessary.

       @OSPProject Memory
    */
    public void do_deallocateMemory()
    {
        TaskCB task = getTask();

        for(int i = 0; i < MMU.getFrameTableSize(); i++){
            FrameTableEntry tempFr = MMU.getFrame(i);
            PageTableEntry tempPg = tempFr.getPage();

            if(tempPg.getTask() == task && tempPg != null){
                tempFr.setPage(null);
                tempFr.setDirty(false);
                tempFr.setReferenced(false);

                if(tempFr.getReserved() == task)
                    tempFr.setUnreserved(task);
            }
        }

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
