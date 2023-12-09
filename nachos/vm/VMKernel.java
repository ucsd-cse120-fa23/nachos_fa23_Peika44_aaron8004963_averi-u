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
		// swapFile.close();
		ThreadedKernel.fileSystem.remove("swapFile");
		super.terminate();
	}

	
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

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	private static final char dbgVM = 'v';

	public static PageInfo IPT[];
	public static int victimIndex;
	public static int pinCnt;
	
	public static OpenFile swapFile;
	public static LinkedList<Integer> availableSwapPages;
	public static int swapCnt;

	public static Lock lock;
	public static Condition CV;

	

	
}