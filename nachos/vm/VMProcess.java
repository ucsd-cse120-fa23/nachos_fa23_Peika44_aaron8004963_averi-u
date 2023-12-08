// package nachos.vm;

// import java.util.concurrent.locks.Lock;

// import nachos.machine.*;
// import nachos.threads.*;
// import nachos.userprog.*;
// import nachos.vm.*;

// /**
//  * A <tt>UserProcess</tt> that supports demand-paging.
//  */
// public class VMProcess extends UserProcess {
//  /**
//   * Allocate a new process.
//   */
//  public VMProcess() {
//   super();
//  }

//  /**
//   * Save the state of this process in preparation for a context switch.
//   * Called by <tt>UThread.saveState()</tt>.
//   */
//  public void saveState() {
//   super.saveState();
//  }

//  /**
//   * Restore the state of this process after a context switch. Called by
//   * <tt>UThread.restoreState()</tt>.
//   */
//  public void restoreState() {
//   super.restoreState();
//  }

//  /**
//   * Initializes page tables for this process so that the executable can be
//   * demand-paged.
//   * 
//   * @return <tt>true</tt> if successful.
//   */
//  protected boolean loadSections() {
//   // return super.loadSections();
//   pageTable = new TranslationEntry[numPages];
//   for (int i = 0; i < numPages; i++){
//    int phy = UserKernel.freePages.removeFirst();

//    pageTable[i] = new TranslationEntry(i, phy, false, false, false, false);

//   }

//   return true;
//  }

//  /**
//   * Release any resources allocated by <tt>loadSections()</tt>.
//   */
//  protected void unloadSections() {
//   super.unloadSections();
//  }

//  /**
//   * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
//   * . The <i>cause</i> argument identifies which exception occurred; see the
//   * <tt>Processor.exceptionZZZ</tt> constants.
//   * 
//   * @param cause the user exception that occurred.
//   */
//  public void handleException(int cause) {
//   Processor processor = Machine.processor();

//   switch (cause) {
//    case Processor.exceptionPageFault:
//     handlePageFault(cause);
//     break;
//    default:
//     super.handleException(cause);
//     break;
//   }
//  }

//  /*
//   * PageFault handler for page demanding
//   */
//  private void handlePageFault(int bad_vaddr){
//   // get faulting vpn
//   int vpn = Processor.pageFromAddress(bad_vaddr);
//   preparePage(vpn);
//  }

//  /*
//   * prepare a page
//   */
//  private void preparePage(int vpn){
//   TranslationEntry entry = pageTable[vpn];

//   int ppn = entry.ppn;

//   for (int s = 0; s < coff.getNumSections(); s++) {
// 	CoffSection section = coff.getSection(s);

// 	Lib.debug(dbgProcess, "\tinitializing " + section.getName()
// 					+ " section (" + section.getLength() + " pages)");

//   }

//  	int coffLength = numPages - stackPages - 1;

// 	// if (vpn < coffLength) {
// 	// 	for (int s = 0; s < coff.getNumSections(); s++) {
// 	// 		CoffSection section = coff.getSection(s);

// 	// 		if (vpn < section.getFirstVPN() + section.getLength()) {
// 	// 			pageTable[vpn].readOnly = section.isReadOnly();
// 	// 			if (pageTable[vpn].readOnly) Lib.debug(dbgVM, "\tReadOnly");
// 	// 			section.loadPage(vpn - section.getFirstVPN(), ppn);
// 	// 			break;
// 	// 		}
// 	// 	}
// 	// }

// //   // find a free page
// //   try{
// //    int freePage = super.freePages.removeFirst();
// //   }
// //   catch(NoSuchElementException e){
// //    // need to be implimented:
// //    // swap out a page 
// //    Lib.debug(dbgVM, "No free pages, swap out a page");
// //   }

// //   CoffSection section = coff.getSection(vpn);

// //   // load page from coff
// //   if(section){
// //    section.loadPage(vpn, ppn);
// //   }
// //   else{
// //    // fill page with zero
// //    byte[] memory = Machine.Processor().getMemory();

// //    int startPos = Processor.makeAddress(entry.ppn, 0);
// //    int pageSize = Machine.Processor().pageSize();

// //    Arrays.fill(memory, startPos, startPos + pageSize, (byte) 0);
// //   }

// //   entry.valid = true;
//  }
//  private static final int pageSize = Processor.pageSize;

//  private static final char dbgProcess = 'a';

//  private static final char dbgVM = 'v';

// }

package nachos.vm;

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
      pageTable[i] = new TranslationEntry(i, i, false, false, false, false);
    }
    // load sections
    return true;
    // return super.loadSections();
  }

  /**
   * Release any resources allocated by <tt>loadSections()</tt>.
   */
  protected void unloadSections() {
    // UserKernel.lock.acquire();
    while (!used_pages.isEmpty()) {
      UserKernel.freePages.add(used_pages.removeLast());
    }
    System.out.println("  used page.size: " + used_pages.size());

    System.out.println("  free page.size: " + UserKernel.freePages.size());
    // UserKernel.lock.release();
  }

  public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
    // used_pages = new LinkedList<Integer>();
    VMKernel.lock.acquire();
    Lib.assertTrue(offset >= 0 && length >= 0
        && offset + length <= data.length);

    byte[] memory = Machine.processor().getMemory();

    if (vaddr < 0) {
      VMKernel.lock.release();
      return 0;
    }
    int left = length;
    int amount = 0;
    int cur_offset = offset;
    int total_read = 0;
    int paddr = -1;
    int paddr_offset = Processor.offsetFromAddress(vaddr);
    int vpn = Processor.pageFromAddress(vaddr);

    if (vpn >= pageTable.length || vpn < 0) {
      VMKernel.lock.release();
      return total_read;
    }
    if (pageTable[vpn].valid) {
      VMKernel.IPT[pageTable[vpn].ppn].pin = true;
      VMKernel.pinCnt++;
      pageTable[vpn].used = true;
      paddr = pageTable[vpn].ppn * pageSize + paddr_offset; // if paddr but not good used bit set?????
    } else {
      handlePageFault(vaddr); // an error may occur??????
      if (pageTable[vpn].valid) {
        VMKernel.IPT[pageTable[vpn].ppn].pin = true;
        VMKernel.pinCnt++;
        pageTable[vpn].used = true;
        paddr = pageTable[vpn].ppn * pageSize + paddr_offset;
      } else {
        VMKernel.lock.release();
        return total_read;
      }
    }
    // for now, just assume that virtual addresses equal physical addresses
    if (paddr < 0 || paddr >= memory.length) {
      VMKernel.IPT[pageTable[vpn].ppn].pin = false;
      VMKernel.pinCnt--;
      VMKernel.CV.wake();
      VMKernel.lock.release();
      return 0;
    }

    amount = Math.min(left, (pageSize - paddr_offset));
    System.arraycopy(memory, paddr, data, offset, amount);
    VMKernel.IPT[pageTable[vpn].ppn].pin = false;
    VMKernel.pinCnt--;
    VMKernel.CV.wake();
    total_read += amount;
    cur_offset += amount;
    left -= amount;
    while (left > 0) {
      vpn++;
      if (vpn >= pageTable.length || vpn < 0) {
        VMKernel.lock.release();
        return total_read;
      }
      if (pageTable[vpn].valid) {
        VMKernel.IPT[pageTable[vpn].ppn].pin = true;
        VMKernel.pinCnt++;
        // System.out.println("b");
        pageTable[vpn].used = true;
        paddr = pageTable[vpn].ppn * pageSize;
      } else {
        vaddr = Processor.makeAddress(vpn, 0);
        handlePageFault(vaddr); // an error may occurrrrrr?????
        if (pageTable[vpn].valid) { // valid means correct?????
          VMKernel.IPT[pageTable[vpn].ppn].pin = true;
          VMKernel.pinCnt++;
          // System.out.println("a");
          pageTable[vpn].used = true;
          paddr = pageTable[vpn].ppn * pageSize;
        } else {
          VMKernel.lock.release();
          return total_read; // else return immedia????????
        }
      }

      if (paddr < 0 || paddr >= memory.length) {
        VMKernel.IPT[pageTable[vpn].ppn].pin = false;
        VMKernel.pinCnt--;
        VMKernel.CV.wake();
        VMKernel.lock.release();
        return total_read;
      }
      amount = Math.min(left, pageSize);
      System.arraycopy(memory, paddr, data, cur_offset, amount);
      VMKernel.IPT[pageTable[vpn].ppn].pin = false;
      VMKernel.pinCnt--;
      // System.out.println("jkafahkjfhadjkfhdashgasfbvsdfbasdfasd");
      VMKernel.CV.wake();
      total_read += amount;
      cur_offset += amount;
      left -= amount;
    }

    VMKernel.lock.release();
    return total_read;
  }

  public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
    VMKernel.lock.acquire();
    Lib.assertTrue(offset >= 0 && length >= 0
        && offset + length <= data.length);

    byte[] memory = Machine.processor().getMemory();

    if (vaddr < 0) {
      VMKernel.lock.release();
      return 0;
    }
    int left = length;
    int amount = 0;
    int cur_offset = offset;
    int total_write = 0;
    int paddr = -1;
    int paddr_offset = Processor.offsetFromAddress(vaddr);
    int vpn = Processor.pageFromAddress(vaddr);

    if (vpn >= pageTable.length || vpn < 0) {
      VMKernel.lock.release();
      return total_write;
    }

    if (pageTable[vpn].valid) {
      VMKernel.IPT[pageTable[vpn].ppn].pin = true;
      VMKernel.pinCnt++;
      // System.out.println("c");
      if (pageTable[vpn].readOnly == false) {
        paddr = pageTable[vpn].ppn * pageSize + paddr_offset;
        pageTable[vpn].used = true;
      }
    } else {
      handlePageFault(vaddr); // an error may occur??????
      if (pageTable[vpn].valid) {
        if (pageTable[vpn].readOnly == false) {
          VMKernel.IPT[pageTable[vpn].ppn].pin = true;
          VMKernel.pinCnt++;
          // System.out.println("d");
          paddr = pageTable[vpn].ppn * pageSize + paddr_offset;
          pageTable[vpn].used = true;
        } else {
          VMKernel.lock.release();
          return total_write;
        }
      } else {
        VMKernel.lock.release();
        return total_write;
      }
    }

    // for now, just assume that virtual addresses equal physical addresses
    if (paddr < 0 || paddr >= memory.length) {
      VMKernel.IPT[pageTable[vpn].ppn].pin = false;
      VMKernel.pinCnt--;
      VMKernel.CV.wake();
      VMKernel.lock.release();
      return 0;
    }

    amount = Math.min(left, (pageSize - paddr_offset));
    System.arraycopy(data, offset, memory, paddr, amount);
    if (amount > 0) {
      pageTable[vpn].dirty = true;
    }
    VMKernel.IPT[pageTable[vpn].ppn].pin = false;
    VMKernel.pinCnt--;
    VMKernel.CV.wake();
    total_write += amount;
    cur_offset += amount;
    left -= amount;
    while (left > 0) {
      vpn++;
      if (vpn >= pageTable.length || vpn < 0) {
        VMKernel.lock.release();
        return total_write;
      }
      if (pageTable[vpn].valid) {
        if (pageTable[vpn].readOnly == false) {
          VMKernel.IPT[pageTable[vpn].ppn].pin = true;
          VMKernel.pinCnt++;
          paddr = pageTable[vpn].ppn * pageSize;
          pageTable[vpn].used = true;
        } else {
          VMKernel.lock.release();
          return total_write;
        }
      } else {
        vaddr = Processor.makeAddress(vpn, 0);
        handlePageFault(vaddr); // an error may occur??????
        if (pageTable[vpn].valid) {
          if (pageTable[vpn].readOnly == false) {
            VMKernel.IPT[pageTable[vpn].ppn].pin = true;
            VMKernel.pinCnt++;
            paddr = pageTable[vpn].ppn * pageSize;
            pageTable[vpn].used = true;
          } else {
            VMKernel.lock.release();
            return total_write;
          }
        } else {
          VMKernel.lock.release();
          return total_write;
        }
      }

      if (paddr < 0 || paddr >= memory.length) {
        VMKernel.IPT[pageTable[vpn].ppn].pin = false;
        VMKernel.pinCnt--;
        VMKernel.CV.wake();
        VMKernel.lock.release();
        return total_write;
      }
      amount = Math.min(left, pageSize);
      System.arraycopy(data, cur_offset, memory, paddr, amount);
      if (amount > 0) {
        pageTable[vpn].dirty = true;
      }
      VMKernel.IPT[pageTable[vpn].ppn].pin = false;
      VMKernel.pinCnt--;
      VMKernel.CV.wake();
      total_write += amount;
      cur_offset += amount;
      left -= amount;
    }

    VMKernel.lock.release();
    return total_write;

  }

  protected void handlePageFault(int badVaddr) {
    // UserKernel.mutex.acquire();
    int badVpn = Processor.pageFromAddress(badVaddr);
    int coffVpn = 0;
    for (int s = 0; s < coff.getNumSections(); s++) {
      CoffSection section = coff.getSection(s);

      Lib.debug(dbgProcess, "\tinitializing " + section.getName()
          + " section (" + section.getLength() + " pages)");

      for (int i = 0; i < section.getLength(); i++) {
        int vpn = section.getFirstVPN() + i;
        coffVpn = vpn;
        if (vpn == badVpn) {
          int ppn = 0;
          // for now, just assume virtual addresses=physical addresses
          if (!UserKernel.freePages.isEmpty()) {
            ppn = UserKernel.freePages.removeLast();
          } else {
            while (true) {
              // add lock tomorrow
              if (VMKernel.IPT[VMKernel.victimIndex].pin == true) {
                // System.out.println(VMKernel.pinCnt);
                if (VMKernel.pinCnt == Machine.processor().getNumPhysPages()) {
                  VMKernel.CV.sleep();
                } // if or while?????
                VMKernel.victimIndex = (VMKernel.victimIndex + 1) % Machine.processor().getNumPhysPages();
                continue;
              }
              if (VMKernel.IPT[VMKernel.victimIndex].entry.used == false) {
                break;
              }
              VMKernel.IPT[VMKernel.victimIndex].entry.used = false;
              VMKernel.victimIndex = (VMKernel.victimIndex + 1) % Machine.processor().getNumPhysPages();
            }
            int toEvict = VMKernel.victimIndex;
            VMKernel.victimIndex = (VMKernel.victimIndex + 1) % Machine.processor().getNumPhysPages();
            if (VMKernel.IPT[toEvict].entry.dirty) {
              int spn = 0;
              if (!VMKernel.availableSwapPages.isEmpty()) {
                spn = VMKernel.availableSwapPages.removeLast();
              } else {
                spn = VMKernel.swapCnt;
                VMKernel.swapCnt++;
              }
              VMKernel.swapFile.write(spn * Processor.pageSize, Machine.processor().getMemory(),
                  Processor.makeAddress(VMKernel.IPT[toEvict].entry.ppn, 0), Processor.pageSize);

              VMKernel.IPT[toEvict].entry.vpn = spn;
              // swap out
              // spn
            }
            VMKernel.IPT[toEvict].process.used_pages.remove(new Integer(VMKernel.IPT[toEvict].entry.ppn)); // ???????????????
                                                                                                           // remove
                                                                                                           // pages
                                                                                                           // actuall
                                                                                                           // physicalllll
            VMKernel.IPT[toEvict].entry.valid = false;
            ppn = VMKernel.IPT[toEvict].entry.ppn;
          }
          used_pages.add(ppn);
          if (!pageTable[vpn].dirty) {
            section.loadPage(i, ppn); // this load to PMem?
            if (section.isReadOnly()) {
              pageTable[vpn] = new TranslationEntry(vpn, ppn, true, true, true, false);
            } else {
              pageTable[vpn] = new TranslationEntry(vpn, ppn, true, false, true, false);
            }
          } else {
            // swap in
            VMKernel.swapFile.read(pageTable[vpn].vpn * Processor.pageSize, Machine.processor().getMemory(),
                Processor.makeAddress(ppn, 0), Processor.pageSize);
            VMKernel.availableSwapPages.add(pageTable[vpn].vpn);
            pageTable[vpn] = new TranslationEntry(vpn, ppn, true, false, true, true);
          }
          VMKernel.IPT[ppn].process = this;
          VMKernel.IPT[ppn].entry = pageTable[vpn];
        }
      }
    }

    for (int i = coffVpn + 1; i < numPages; i++) {
      int vpn = i;

      if (vpn == badVpn) {
        int ppn = 0;
        if (!UserKernel.freePages.isEmpty()) {
          ppn = UserKernel.freePages.removeLast();
        } else {
          while (true) {
            // add lock tomorrow
            if (VMKernel.IPT[VMKernel.victimIndex].pin == true) {
              // System.out.println(VMKernel.pinCnt);
              if (VMKernel.pinCnt == Machine.processor().getNumPhysPages()) {
                VMKernel.CV.sleep();
              } // if or while????? VMKernel.lock.acquire();
              VMKernel.victimIndex = (VMKernel.victimIndex + 1) % Machine.processor().getNumPhysPages();
              continue;
            }
            if (VMKernel.IPT[VMKernel.victimIndex].entry.used == false) {
              break;
            }
            VMKernel.IPT[VMKernel.victimIndex].entry.used = false;
            VMKernel.victimIndex = (VMKernel.victimIndex + 1) % Machine.processor().getNumPhysPages();
          }

          int toEvict = VMKernel.victimIndex;
          VMKernel.victimIndex = (VMKernel.victimIndex + 1) % Machine.processor().getNumPhysPages();
          if (VMKernel.IPT[toEvict].entry.dirty) {
            int spn = 0;
            if (!VMKernel.availableSwapPages.isEmpty()) {
              spn = VMKernel.availableSwapPages.removeLast();
            } else {
              spn = VMKernel.swapCnt;
              VMKernel.swapCnt++;
            }
            VMKernel.swapFile.write(spn * Processor.pageSize, Machine.processor().getMemory(),
                Processor.makeAddress(VMKernel.IPT[toEvict].entry.ppn, 0), Processor.pageSize);

            VMKernel.IPT[toEvict].entry.vpn = spn;
            // swap out
          }
          VMKernel.IPT[toEvict].process.used_pages.remove(new Integer(VMKernel.IPT[toEvict].entry.ppn));
          VMKernel.IPT[toEvict].entry.valid = false;
          ppn = VMKernel.IPT[toEvict].entry.ppn;
        }
        used_pages.add(ppn);
        if (!pageTable[vpn].dirty) {
          // fill with 000000???????????????
          byte[] data = new byte[Processor.pageSize];
          for (int j = 0; j < data.length; j++) {
            data[j] = 0;
          }
          System.arraycopy(data, 0, Machine.processor().getMemory(), Processor.makeAddress(ppn, 0), Processor.pageSize);
          pageTable[vpn] = new TranslationEntry(vpn, ppn, true, false, true, false);
        } else {
          // swap in
          VMKernel.swapFile.read(pageTable[vpn].vpn * Processor.pageSize, Machine.processor().getMemory(),
              Processor.makeAddress(ppn, 0), Processor.pageSize);
          VMKernel.availableSwapPages.add(pageTable[vpn].vpn);
          pageTable[vpn] = new TranslationEntry(vpn, ppn, true, false, true, true);
        }
        // fill with 00000?????
        VMKernel.IPT[ppn].process = this;
        VMKernel.IPT[ppn].entry = pageTable[vpn];
      }
    }

    // System.out.println("process id: " + this.process_id);
    // for(int i = 0; i < used_pages.size(); i++){
    // System.out.print(used_pages.get(i));
    // }
    // UserKernel.mutex.release();

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
        handlePageFault(processor.readRegister(Processor.regBadVAddr)); // need to return anything??????????
        break;
      default:
        super.handleException(cause);
        break;
    }
  }

  private static final int pageSize = Processor.pageSize;

  private static final char dbgProcess = 'a';

  private static final char dbgVM = 'v';
  public LinkedList<Integer> used_pages;
}
