package nachos.threads;

import java.util.PriorityQueue;

import nachos.machine.*;

import java.util.Comparator;

class PriorThread {
    public KThread thread;
    public long priority;

    public PriorThread(KThread t, long p) {
        thread = t;
        priority = p;
    }

    public static class Comp implements Comparator<PriorThread> {
        public int compare(PriorThread a, PriorThread b) {
            if (a.priority > b.priority)
                return 1;
            else if (a.priority < b.priority)
                return -1;
            return 0;
        }
    }
}
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		
		boolean initStatus = Machine.interrupt().disable();

		// while()
		while (!waitQueue.isEmpty()) {
            PriorThread priorThread = waitQueue.peek();
            if (priorThread.priority <= Machine.timer().getTime()) {
                waitQueue.poll();
                priorThread.thread.ready();
            } else
                break;

        }


		Machine.interrupt().restore(initStatus);
		KThread.currentThread().yield();
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		// long wakeTime = Machine.timer().getTime() + x;
		// while (wakeTime > Machine.timer().getTime())
		// 	KThread.yield();

		// boolean current = Machine.interrupt().disable();

		// long wakeT = Machine.timer().getTime() + x;

		// KThread currentThread = KThread.currentThread();
		// PriorThread priorThread = new PriorThread(currentThread, wakeT);
        // waitQueue.add(priorThread);
        // currentThread.sleep();

		boolean intrStatus = Machine.interrupt().disable();
		long timeToWake = Machine.timer().getTime() + x;
	
		PriorThread pt = new PriorThread(KThread.currentThread(), timeToWake);
		waitQueue.offer(pt);
		KThread.sleep();
	  
		Machine.interrupt().restore(intrStatus);

	}

        /**
	 * Cancel any timer set by <i>thread</i>, effectively waking
	 * up the thread immediately (placing it in the scheduler
	 * ready set) and returning true.  If <i>thread</i> has no
	 * timer set, return false.
	 * 
	 * <p>
	 * @param thread the thread whose timer should be cancelled.
	 */
    public boolean cancel(KThread thread) {
			return false;
	}

	    // Add Alarm testing code to the Alarm class
    
	// Add Alarm testing code to the Alarm class
    
    public static void alarmTest1() {
		int durations[] = {1000, 10*1000, 100*1000};
		long t0, t1;
	
		for (int d : durations) {
			t0 = Machine.timer().getTime();
			ThreadedKernel.alarm.waitUntil (d);
			t1 = Machine.timer().getTime();
			System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
		}
		}
	
		// Implement more test methods here ...
	
	// Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
	public static void selfTest() {
		alarmTest1();

	// Invoke your other test methods here ...
	}
	

	private PriorityQueue<PriorThread> waitQueue = new PriorityQueue<>(new PriorThread.Comp());


}

// Add Alarm testing code to the Alarm class
    

