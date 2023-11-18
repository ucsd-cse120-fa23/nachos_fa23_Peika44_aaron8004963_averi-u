package nachos.threads;

import java.util.PriorityQueue;

import nachos.machine.*;

import java.util.Comparator;

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

        long currentTime = Machine.timer().getTime();
        if (!waitQueue.isEmpty()) {
			if(waitQueue.peek().wakeTime <= currentTime){
           		 waitQueue.poll().thread.ready();
			}else{
				// System.out.println("current time " + currentTime + ", waitqueue.peek time: "+ waitQueue.peek().wakeTime);
			}
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
        boolean current = Machine.interrupt().disable();
		if(x > 0){
        	long wakeTime = Machine.timer().getTime() + x;
			waitQueue.add(new AlarmEntry(KThread.currentThread(), wakeTime));
        	KThread.currentThread().sleep();
		}
        Machine.interrupt().restore(current);
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
		boolean initStatus = Machine.interrupt().disable();
		AlarmEntry entryToCancel = null;
	
		for (AlarmEntry entry : waitQueue) {
			if (entry.thread == thread) {
				entryToCancel = entry;
				break;
			}
		}
	
		if (entryToCancel != null) {
			waitQueue.remove(entryToCancel);
			entryToCancel.thread.ready();
			Machine.interrupt().restore(initStatus);
			return true;
		}
	
		Machine.interrupt().restore(initStatus);
		return false;
	}

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

		public static void alarmTest2() {
			int durations[] = { 1000, 2000, 3000, 4000, 5000 };
			long t0, t1;
		
			for (int d : durations) {
				t0 = Machine.timer().getTime();
				ThreadedKernel.alarm.waitUntil(d);
				t1 = Machine.timer().getTime();
				System.out.println("alarmTest2: waited for " + (t1 - t0) + " ticks");
			}
		}

		public static void alarmTest3() {
			KThread thread1 = new KThread(new Runnable() {
				public void run() {
					long startTime = Machine.timer().getTime();
					ThreadedKernel.alarm.waitUntil(2000);
					long wakeUpTime = Machine.timer().getTime();
					System.out.println("alarmTest3: Thread_1 waited for " + (wakeUpTime - startTime) + "(Expected 2000)" + " ticks");
				}
			});
		
			KThread thread2 = new KThread(new Runnable() {
				public void run() {
					long startTime = Machine.timer().getTime();
					ThreadedKernel.alarm.waitUntil(3000);
					long wakeUpTime = Machine.timer().getTime();
					System.out.println("alarmTest3: Thread_2 waited for " + (wakeUpTime - startTime) + "(Expected 3000)" + " ticks");
				}
			});
		
			thread1.setName("alarmTest3_Thread_1");
			thread2.setName("alarmTest3_Thread_2");
		
			thread1.fork();
			thread2.fork();
		
			thread1.join();
			thread2.join();
		}

		public static void alarmTest4() {
			KThread thread1 = new KThread(new Runnable() {
				public void run() {
					long startTime = Machine.timer().getTime();
					ThreadedKernel.alarm.waitUntil(0);
					long wakeUpTime = Machine.timer().getTime();
					System.out.println("alarmTest4: Thread_1 waited for " + (wakeUpTime - startTime) + "(Expected 0)" + " ticks");
				}
			});
		
			KThread thread2 = new KThread(new Runnable() {
				public void run() {
					long startTime = Machine.timer().getTime();
					ThreadedKernel.alarm.waitUntil(0);
					long wakeUpTime = Machine.timer().getTime();
					System.out.println("alarmTest4: Thread_2 waited for " + (wakeUpTime - startTime) + "(Expected 0)" + " ticks");
				}
			});
		
			thread1.setName("alarmTest4_Thread_1");
			thread2.setName("alarmTest4_Thread_2");
		
			thread1.fork();
			thread2.fork();
		
			thread1.join();
			thread2.join();
		}

		public static void alarmTest5() {
			int durations[] = { 5000, 1000, 2000, 500, 5000 };
			long t0, t1;
		
			for (int d : durations) {
				t0 = Machine.timer().getTime();
				ThreadedKernel.alarm.waitUntil(d);
				t1 = Machine.timer().getTime();
				System.out.println("alarmTest2: waited for " + (t1 - t0) + " ticks"+ "(Expected" + d + ")" + " ticks");
			}
		}
		
	
		// Implement more test methods here ...
	
		// Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
	public static void selfTest() {
		System.out.println("\n" +
		"-----------------------------alarmTest1()---------------------------------------"
		);
		alarmTest1();
		System.out.println("\n" +
		"-----------------------------alarmTest2()---------------------------------------"
		);
		alarmTest2();
		System.out.println("\n" +
		"-----------------------------alarmTest3()---------------------------------------"
		);
		alarmTest3();
		System.out.println("\n" +
		"-----------------------------alarmTest4()---------------------------------------"
		);
		alarmTest4();
		System.out.println("\n" +
		"-----------------------------alarmTest5()---------------------------------------"
		);
		alarmTest5();
	}

    private PriorityQueue<AlarmEntry> waitQueue = new PriorityQueue<>();

	private static class AlarmEntry implements Comparable<AlarmEntry> {
		public KThread thread;
		public long wakeTime;
	
		public AlarmEntry(KThread t, long wt) {
			thread = t;
			wakeTime = wt;
		}
	
		@Override
		public int compareTo(AlarmEntry other) {
			// Compare AlarmEntries based on wakeTime
			return Long.compare(this.wakeTime, other.wakeTime);
		}
	}
	

}
