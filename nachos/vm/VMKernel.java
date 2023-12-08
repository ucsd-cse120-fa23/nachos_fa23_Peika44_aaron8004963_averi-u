package nachos.vm;

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

 public class IPT{
  public int vpn;
  public VMProcess currentProcess;
  public boolean pin;

  public IPT(){
   this.vpn = -1;
   this.currentProcess = null;
   this.pin = false;
  }

  public IPT(VMProcess process, int vpn, boolean pinned){
   this.vpn = vpn;
   this.currentProcess = process;
   this.pin = pinned;
  }
 }
 


 public static IPT[] IPT = new IPT[Machine.processor().getNumPhysPages()];

 private Lock Lock = new Lock();

 private Condition condition = new Condition(Lock);

 private int pinCount = 0;

 private int victim;

 // dummy variables to make javac smarter
 private static VMProcess dummy1 = null;

 private static final char dbgVM = 'v';
}