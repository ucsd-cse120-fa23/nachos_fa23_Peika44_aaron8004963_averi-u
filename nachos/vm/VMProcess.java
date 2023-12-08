package nachos.vm;

import java.util.concurrent.locks.Lock;

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
  // return super.loadSections();
  pageTable = new TranslationEntry[numPages];
  for (int i = 0; i < numPages; i++){
   int phy = UserKernel.freePages.removeFirst();

   pageTable[i] = new TranslationEntry(i, phy, false, false, false, false);
   
   for (int s = 0; s < coff.getNumSections(); s++) {
		CoffSection section = coff.getSection(s);
		for (int j = 0; j < section.getLength(); j++) {
			int vpn = section.getFirstVPN() + j;
			pageTable[vpn].readOnly = section.isReadOnly();
			section.loadPage(j, pageTable[vpn].ppn);
		}	
	}
  }

  return true;
 }

 /**
  * Release any resources allocated by <tt>loadSections()</tt>.
  */
 protected void unloadSections() {
  super.unloadSections();
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
    handlePageFault(cause);
    break;
   default:
    super.handleException(cause);
    break;
  }
 }
 
 /*
  * PageFault handler for page demanding
  */
 private void handlePageFault(int bad_vaddr){
  // get faulting vpn
  int vpn = Processor.pageFromAddress(bad_vaddr);
  preparePage(vpn);
 }

 /*
  * prepare a page
  */
 private void preparePage(int vpn){
  TranslationEntry entry = pageTable[vpn];
  
  int ppn = entry.ppn;
  
  for (int s = 0; s < coff.getNumSections(); s++) {
	CoffSection section = coff.getSection(s);

	Lib.debug(dbgProcess, "\tinitializing " + section.getName()
					+ " section (" + section.getLength() + " pages)");
                        
  }

 	int coffLength = numPages - stackPages - 1;

	// if (vpn < coffLength) {
	// 	for (int s = 0; s < coff.getNumSections(); s++) {
	// 		CoffSection section = coff.getSection(s);

	// 		if (vpn < section.getFirstVPN() + section.getLength()) {
	// 			pageTable[vpn].readOnly = section.isReadOnly();
	// 			if (pageTable[vpn].readOnly) Lib.debug(dbgVM, "\tReadOnly");
	// 			section.loadPage(vpn - section.getFirstVPN(), ppn);
	// 			break;
	// 		}
	// 	}
	// }



//   // find a free page
//   try{
//    int freePage = super.freePages.removeFirst();
//   }
//   catch(NoSuchElementException e){
//    // need to be implimented:
//    // swap out a page 
//    Lib.debug(dbgVM, "No free pages, swap out a page");
//   }
  
 

//   CoffSection section = coff.getSection(vpn);

//   // load page from coff
//   if(section){
//    section.loadPage(vpn, ppn);
//   }
//   else{
//    // fill page with zero
//    byte[] memory = Machine.Processor().getMemory();

//    int startPos = Processor.makeAddress(entry.ppn, 0);
//    int pageSize = Machine.Processor().pageSize();

//    Arrays.fill(memory, startPos, startPos + pageSize, (byte) 0);
//   }

//   entry.valid = true;
 }
 private static final int pageSize = Processor.pageSize;

 private static final char dbgProcess = 'a';

 private static final char dbgVM = 'v';

}