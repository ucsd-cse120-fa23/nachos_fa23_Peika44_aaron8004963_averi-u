// package nachos.vm;

// import java.util.LinkedList;

// import nachos.machine.*;
// import nachos.threads.*;
// import nachos.userprog.*;
// import nachos.vm.*;

// /**
//  * A kernel that can support multiple demand-paging user processes.
//  */
// public class VMKernel extends UserKernel {
// 	/**
// 	 * Allocate a new VM kernel.
// 	 */
// 	public VMKernel() {
// 		super();
// 	}

// 	/**
// 	 * Initialize this kernel.
// 	 */
// 	public void initialize(String[] args) {
// 		super.initialize(args);
// 	}

// 	/**
// 	 * Test this kernel.
// 	 */
// 	public void selfTest() {
// 		super.selfTest();
// 	}

// 	/**
// 	 * Start running user programs.
// 	 */
// 	public void run() {
// 		super.run();
// 	}

// 	/**
// 	 * Terminate this kernel. Never returns.
// 	 */
// 	public void terminate() {
// 		super.terminate();
// 	}

// 	// dummy variables to make javac smarter
// 	private static VMProcess dummy1 = null;

// 	private static final char dbgVM = 'v';
// 	//starter codes end

// 	public static int victimIndex;

// 	public static System IPT[];

// 	public static LinkedList<Integer> availableSwapPages;

// 	public static OpenFile swap;

// 	public static int sp;

// 	public static Lock mutex;

// 	public static Condition CV;

// 	public static int numPin;

// 	protected class System{
// 		public VMProcess process;
// 		public TranslationEntry entry;
// 		public boolean pin;

// 		public System(VMProcess process, TranslationEntry entry, boolean pin){
// 		  this.process = process;
// 		  this.entry = entry;
// 		  this.pin = pin;
// 		}           
// 	  }

// }

package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;
import java.util.*;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {
	/**
	 * Allocate a new VM kernel.
	 */
	public VMKernel() {
		super();
	}

	/**
	 * Initialize this kernel.
	 */
	public void initialize(String[] args) {
		super.initialize(args);
		victimIndex = 0;
		swapCnt = 0;
		pinCnt = 0;

		//IPT maps vpn to ppn 
		int numPhysPages = Machine.processor().getNumPhysPages();
		IPT = new PageInfo[numPhysPages];
		Arrays.fill(IPT, new PageInfo(null, null, false));
		
		swapFile = ThreadedKernel.fileSystem.open("swapFile", true);
		availableSwapPages = new LinkedList<Integer>();
		lock = new Lock();
		CV = new Condition(lock);
	}

	/**
	 * Test this kernel.
	 */
	public void selfTest() {
		super.selfTest();
	}

	/**
	 * Start running user programs.
	 */
	public void run() {
		super.run();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		swapFile.close();
		ThreadedKernel.fileSystem.remove("swapFile");
		super.terminate();
	}

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	private static final char dbgVM = 'v';

	public static int victimIndex;

	public static PageInfo IPT[];

	public static LinkedList<Integer> availableSwapPages;

	public static OpenFile swapFile;

	public static int swapCnt;

	public static Lock lock;

	public static Condition CV;

	public static int pinCnt;

	protected class PageInfo {
		public VMProcess process;
		public TranslationEntry entry;
		public boolean pin;

		public PageInfo(VMProcess process, TranslationEntry entry, boolean pin) {
			this.process = process;
			this.entry = entry;
			this.pin = pin;
		}
	}
}