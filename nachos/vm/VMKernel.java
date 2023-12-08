package nachos.vm;

import java.util.LinkedList;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

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
		super.terminate();
	}

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	

	private static final char dbgVM = 'v';
	//starter codes end


	public static int victim;

	public static System IPT[];

	public static LinkedList<Integer> freeSwapPages;

	public static OpenFile swap;

	public static int sp;

	public static Lock mutex;

	public static Condition CV;

	public static int numPin;

	protected class System{
		public VMProcess process;
		public TranslationEntry entry;
		public boolean pin;

		public System(VMProcess process, TranslationEntry entry, boolean pin){
		  this.process = process;
		  this.entry = entry;
		  this.pin = pin;
		}           
	  }
        
}
