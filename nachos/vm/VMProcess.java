package nachos.vm;

import java.util.Arrays;
import java.util.LinkedList;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {
  /**
   * Allocate a new process.
   */
  public VMProcess() {
    super();
    used_pages = new LinkedList<Integer>();
  }

  /**
   * Save the state of this process in preparation for a context switch.
   * Called by <tt>UThread.saveState()</tt>.
   */
  public void saveState() {
    super.saveState();
  }

  /**
   * Restore the state of this process after a context switch. Called by
   * <tt>UThread.restoreState()</tt>.
   */
  public void restoreState() {
    super.restoreState();
  }

  /**
   * Initializes page tables for this process so that the executable can be
   * demand-paged.
   * 
   * @return <tt>true</tt> if successful.
   */
  protected boolean loadSections() {
    pageTable = new TranslationEntry[numPages];
    for (int i = 0; i < numPages; i++) {
      pageTable[i] = new TranslationEntry(i, -1, false, false, false, false);
    }
    return true;
  }

  /**
   * Release any resources allocated by <tt>loadSections()</tt>.
   */
  protected void unloadSections() {
    UserKernel.lock.acquire();
    
    while (!used_pages.isEmpty()) {
      UserKernel.freePages.add(used_pages.removeFirst());
    }

    UserKernel.lock.release();
  }

  public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
    VMKernel.lock.acquire();

    byte[] memory = Machine.processor().getMemory();

    // Validate parameters
    if (vaddr < 0 ) {
        VMKernel.lock.release();
        return 0;
    }

    int read_result = 0;

    while (length > 0) {
        int vpn = Processor.pageFromAddress(vaddr);
        int phyOff = Processor.offsetFromAddress(vaddr);
        if(vpn >= pageTable.length || vpn < 0){
          break;
        }
        // Handle the page fault
        if (!pageTable[vpn].valid) {
            handlePageFault(vaddr);
        }     
        pinPage(vpn);
        int phyAdd = pageTable[vpn].ppn * Processor.pageSize + phyOff;
        int amount = Math.min(length, Processor.pageSize - phyOff);
        if (!(phyAdd >= 0 && phyAdd + amount <= memory.length)) {
          //  VMKernel.IPT[pageTable[vpn].ppn].pin = false;
          //   VMKernel.pinCnt --;
          //   VMKernel.CV.wake();
            unpinPage(vpn);
            break;
        }
        System.arraycopy(memory, phyAdd, data, offset, amount);
        // Update variables for next iteration
        vaddr += amount;
        offset += amount;
        length -= amount;
        read_result += amount;
        // VMKernel.IPT[pageTable[vpn].ppn].pin = false;
        // VMKernel.pinCnt --;
        // VMKernel.CV.wake();
        unpinPage(vpn);
        pageTable[vpn].used = true;
    }
    VMKernel.lock.release();
    return read_result;
}

  public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
    VMKernel.lock.acquire();
    byte[] memory = Machine.processor().getMemory();
    if (!(vaddr < memory.length && vaddr >= 0 )) {
      VMKernel.lock.release();
      return 0;
    }

    int writeCnt = 0;
    for (int i = 0; i < length; i++) {
      int vpn = Processor.pageFromAddress(vaddr + i);
      TranslationEntry entry = pageTable[vpn];
      if (vpn < 0 || vpn >= pageTable.length || !entry.valid)
        break;
      int ppn = entry.ppn;
      if (!entry.readOnly) {
        int phyAdd = pageSize * ppn + Processor.offsetFromAddress(vaddr + i);
        memory[phyAdd] = data[offset + i];
        entry.dirty = true;
        writeCnt++;
      }
    }

    VMKernel.lock.release();
    return writeCnt;
  }

  protected void handlePageFault(int badVaddr) {
    UserKernel.lock.acquire();

    boolean badCoff = true;
    int firstVPN = -1;
    // Iterate through each section of the COFF file
    for (int s = 0; s < coff.getNumSections(); s++) {
      // Iterate through each page of the section
      firstVPN = coff.getSection(s).getFirstVPN();
      iteratePages(badCoff, firstVPN, s, badVaddr);
    }

    badCoff = false;
    iteratePages(badCoff, firstVPN, coff.getNumSections() - 1, badVaddr);

    UserKernel.lock.release();
  }

  private void pinPage(int vpn) {
  VMKernel.IPT[pageTable[vpn].ppn].pin = true;
  VMKernel.pinCnt++;
  // pageTable[vpn].used = true;
}

private void unpinPage(int vpn) {
  VMKernel.IPT[pageTable[vpn].ppn].pin = false;
  VMKernel.pinCnt--;
  VMKernel.CV.wake();
}


  // helper method to check pages within a section/stack;
  // if bad, swapOut&swapIn
  private void iteratePages(boolean badCoff, int firstVPN, int currSectionIndex, int badVaddr) {
    int vpn;

    int start = 0;
    int end = coff.getSection(currSectionIndex).getLength();
    if (!badCoff) {
      start = firstVPN + coff.getSection(currSectionIndex).getLength() + 1;
      end = numPages;
    }

    // try to find bad pages by iterating
    for (int i = start; i < end; i++) {
      if (badCoff) {
        vpn = firstVPN + i;
      } else {
        vpn = i;
      }
      //found bad page
      if (vpn == Processor.pageFromAddress(badVaddr)) { 
        // swap out
        int availablePPN = swapOut(); 
        // swap in
        SwapIn(badCoff, coff.getSection(currSectionIndex), i, vpn, availablePPN);// swap in
      }
    }
  }

  // swap out(if no free pages)
  private int swapOut() {
    int availablePPN;

    // if there are free space
    if (!UserKernel.freePages.isEmpty()) {
      availablePPN = UserKernel.freePages.removeLast();
    }
    // if not
    else {
      // Clock algorithm to find evict
      int toEvict = clockAlgorithm();

      // If the victim is dirty, write it to the swap file
      handleDirtyPage(toEvict);

      // Remove the evicted page from the process's used pages and update IPT
      VMKernel.IPT[toEvict].entry.valid = false;

      int evictedPPN = VMKernel.IPT[toEvict].entry.ppn;
      VMKernel.IPT[toEvict].process.used_pages.remove(evictedPPN);

      // New available ppn is the page being swapped out
      availablePPN = VMKernel.IPT[toEvict].entry.ppn;
    }

    return availablePPN;
  }

  private void SwapIn(boolean badCoff, CoffSection section, int sectionPageIndex, int vpn, int ppn) {
    // swap from swapFile
    if (pageTable[vpn].dirty) {
      loadFromSwapFile(vpn, ppn);
    }
    // if this page never loaded
    else {
      if (badCoff) {
        loadFromCoff(section, sectionPageIndex, vpn, ppn);
      } else {
        loadFromStack(vpn, ppn);
      }
    }
    used_pages.add(ppn);
    VMKernel.IPT[ppn].process = this;
    VMKernel.IPT[ppn].entry = pageTable[vpn];
  }
  
  private void loadFromSwapFile(int vpn, int ppn) {
    VMKernel.swapFile.read(pageTable[vpn].vpn * Processor.pageSize, Machine.processor().getMemory(),
        Processor.makeAddress(ppn, 0), Processor.pageSize);
    VMKernel.availableSwapPages.add(pageTable[vpn].vpn);
    updatePageTableEntry(vpn, ppn, false, true);
  }

  private void loadFromCoff(CoffSection section, int sectionPageIndex, int vpn, int ppn) {
    section.loadPage(sectionPageIndex, ppn);
    updatePageTableEntry(vpn, ppn, section.isReadOnly(), false);
  }

  private void loadFromStack(int vpn, int ppn) {
    // Get the memory array from the machine's processor
    byte[] memory = Machine.processor().getMemory();

    // Calculate the start address of the physical page
    int startAddr = Processor.makeAddress(ppn, 0);

    // Fill the entire page with zeros
    Arrays.fill(memory, startAddr, startAddr + Processor.pageSize, (byte) 0);

    // Update the page table entry for this virtual page number (vpn)
    updatePageTableEntry(vpn, ppn, false, false); // The page is writable (not read-only)
  }

  // clock algorithm
  private int clockAlgorithm() {
    while (true) {
      // Check the current victim page in the circular list (clock)
      TranslationEntry currentEntry = VMKernel.IPT[VMKernel.victimIndex].entry;

      // If the page is not in use (used == false), it's a candidate for eviction
      if (!currentEntry.used) {
        // If the page is not pinned and not used recently, select it as the victim
        if (!VMKernel.IPT[VMKernel.victimIndex].pin) {
          int victimPPN = currentEntry.ppn; // Physical Page Number of the victim
          VMKernel.victimIndex = (VMKernel.victimIndex + 1) % Machine.processor().getNumPhysPages();
          return victimPPN;
        }
      } else {
        // If the page was recently used, set used to false and move to the next page
        currentEntry.used = false;
      }

      // Move to the next page in the circular list
      VMKernel.victimIndex = (VMKernel.victimIndex + 1) % Machine.processor().getNumPhysPages();
    }
  }

  // If the victim is dirty, write it to the swap file
  private void handleDirtyPage(int toEvict) {
    TranslationEntry entry = VMKernel.IPT[toEvict].entry;

    if (entry.dirty) {
      // Check if there are available swap pages
      // If no swap pages are available, use the next swap page index and increment
      // the swapped counter
      int swapPageIndex = VMKernel.availableSwapPages.isEmpty() ? (VMKernel.swapCnt++)
          : (VMKernel.availableSwapPages.removeLast());

      int physicalAddr = Processor.makeAddress(entry.ppn, 0);
      byte[] memory = Machine.processor().getMemory();

      // Write the page to the swap file
      VMKernel.swapFile.write(swapPageIndex * Processor.pageSize, memory, physicalAddr, Processor.pageSize);
      entry.vpn = swapPageIndex; // Update the vpn to swapPageIndex
    }
  }

  private void updatePageTableEntry(int vpn, int ppn, boolean isReadOnly, boolean dirty) {
    // Create a new translation entry for the page table
    pageTable[vpn] = new TranslationEntry(vpn, ppn, true, isReadOnly, true, dirty);
  }

  /**
   * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
   * . The <i>cause</i> argument identifies which exception occurred; see the
   * <tt>Processor.exceptionZZZ</tt> constants.
   * 
   * @param cause the user exception that occurred.
   */
  public void handleException(int cause) {
    Processor processor = Machine.processor();

    switch (cause) {
      case Processor.exceptionPageFault:
        int badVaddr = processor.readRegister(Processor.regBadVAddr);
        handlePageFault(badVaddr);
        break;
      default:
        super.handleException(cause);
        break;
    }
  }

  // public void printIPT() {
  //   for (int i = 0; i < Machine.processor().getNumPhysPages(); i++) {
  //     Lib.debug(dbgProcess, "ppn: " + i + " pid: " + VMKernel.IPT[i].process + " vpn: "
  //         + VMKernel.IPT[i].entry.vpn + " valid: " + VMKernel.IPT[i].entry.valid + " dirty: "
  //         + VMKernel.IPT[i].entry.dirty + " used: " + VMKernel.IPT[i].entry.used);
  //   }
  // }

  public LinkedList<Integer> used_pages;

  private static final int pageSize = Processor.pageSize;

  private static final char dbgProcess = 'a';

  private static final char dbgVM = 'v';

}