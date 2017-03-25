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
	int pagesSize;
    /** 
	The page table constructor. Must call
	
	    super(ownerTask)

	as its first statement.

	@OSPProject Memory
    */
    public PageTable(TaskCB ownerTask)
    {
        super(ownerTask);

        pagesSize = (int) Math.pow(2, MMU.getPageAddressBits());
        pages = new PageTableEntry[pagesSize];

        for(int i = 0; i < pagesSize; i++){
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
        for(int i = 0; i < pagesSize; i++){
        	FrameTableEntry tempFr = pages[i].getFrame();
        	if(tempFr != null && tempFr.getPage().getTask() == getTask()){
        		tempFr.setPage(null);
        		tempFr.setDirty(false);
        		tempFr.setReferenced(false);
        	}
        }

        for(int i = 0; i < MMU.getFrameTableSize(); i++){
        	TaskCB task = getTask();
        	if(MMU.getFrame(i).getReserved() == task){
        		MMU.setFrame(i).setUnreserved(task);
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
