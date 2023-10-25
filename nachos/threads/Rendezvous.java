package nachos.threads;

// import java.util.ArrayList;
// import java.util.Vector;

import nachos.machine.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

/**
 * A <i>Rendezvous</i> allows threads to synchronously exchange values.
 */
public class Rendezvous {

    private static Lock lock;
    private static Condition notEmpty;
    private static Condition notFull;
    private int instanceValue;
    private Rendezvoustag tags;

    /*
     * version with integer and pair of integer
     * store data, value, and value
     */
    // private class Pair<K, V> {
    //     private K key;
    //     private V value;
    
    //     public Pair(K key, V value) {
    //         this.key = key;
    //         this.value = value;
    //     }
    
    //     public K getKey() {
    //         return key;
    //     }
    
    //     public V getValue() {
    //         return value;
    //     }

    //     public void setValue(V value){
    //         this.value = value;
    //     }

    // }

    // private class Rendezvoustag{
    //     // private Map<Integer, Pair<Integer, Condition>> tags;
    //     // //private int value;

    //     Rendezvoustag(){
    //         //tags = new HashMap<Integer, Pair<Integer, Condition>>();
    //         //this.value = value;
    //         tags = new HashMap<Integer, Pair<Integer,Integer>>();
    //     }
        
    //     // public Map<Integer, Pair<Integer, Condition>> getTags(){
    //     //     return this.tags;
    //     // }

    //     private Map<Integer, Pair<Integer,Integer>> tags;
        
    //     // public void addTag(int tag, int value, Condition cv ){
    //     //     this.tags.put(tag, new Pair<Integer, Condition>(value, cv));
    //     // }

    //     public Map<Integer, Pair<Integer,Integer>> getTags(){
    //         return this.tags;
    //     }

    //     public void addTag(int tag, int value, int lastValue){
    //         this.tags.put(tag, new Pair<Integer, Integer>(value, lastValue));
    //     }

    //     public void deleteTag(int tag){
    //         this.tags.remove(tag);
    //     }
    // }

    /*
     * new version tried to block other threads
     * until the cooresponding thread finished
     */

     private class Rendezvoustag{
        private Map<Integer,Integer> tags;

        Rendezvoustag(){
            tags = new ConcurrentHashMap<Integer,Integer>();
        }

        // public Map<Integer,Integer> getTags(){
        //     return this.tags;
        // }

        public int getValue(int tag){
            return this.tags.get(tag);
        }

        public void addTag(int tag, int value){
            this.tags.put(tag, value);
        }

        public void deleteTag(int tag){
            this.tags.remove(tag);
        }

        public boolean containsTag(int tag){
            return this.tags.containsKey(tag);
        }

     }
    /**
     * Allocate a new Rendezvous.
     */
    public Rendezvous () {
        lock = new Lock();
        notEmpty = new Condition(lock);
        notFull = new Condition(lock);
        tags = new Rendezvoustag();
        int i = 0;
    }

    /**
     * Synchronously exchange a value with another thread.  The first
     * thread A (with value X) to exhange will block waiting for
     * another thread B (with value Y).  When thread B arrives, it
     * will unblock A and the threads will exchange values: value Y
     * will be returned to thread A, and value X will be returned to
     * thread B.
     *
     * Different integer tags are used as different, parallel
     * synchronization points (i.e., threads synchronizing at
     * different tags do not interact with each other).  The same tag
     * can also be used repeatedly for multiple exchanges.
     *
     * @param tag the synchronization tag.
     * @param value the integer to exchange.
     */
    public int exchange (int tag, int value) {
        lock.acquire();
        try{
            // if(tags.getTags().containsKey(tag)){
                
            //     int v = tags.getTags().get(tag).getKey();
            //     tags.getTags().get(tag).setValue(value);
            //     System.out.println( KThread.currentThread().getName());
            //     cv.wake();
            //     System.out.println('0');
            //     return v;
            // }
            // else{
            //     tags.addTag(tag, value, value);
            //     System.out.println('2');
            //     cv.sleep();
            //     System.out.println('1');
            //     int instance = tags.getTags().get(tag).getValue();
            //     tags.deleteTag(tag);
            //     return instance;  
            // }
                // if(tags.containsTag(tag)){
                //     int v = tags.getValue(tag);
                //     tags.addTag(tag, value);
                //     notEmpty.wake();
                //     return v;
                // }
                // else{
                //     tags.addTag(tag, value);
                //     notFull.sleep();
                //     int instance = tags.getValue(tag);
                //     tags.deleteTag(tag);
                //     return instance;
                // }
            
            
            if(tags.containsTag(tag)){
                int v = get_tag(tag);
                put_tag(tag, value);
                return v;
            }
            else{
                put_tag(tag, value);
                notFull.sleep();
                return get_tag(tag);
            }
            
        }
        finally{
            lock.release();
        }
    }

    private void put_tag(int tag, int value){
        while(tags.containsTag(tag)){
            notFull.sleep();
        }
        tags.addTag(tag, value);
        notEmpty.wake();
    }

    private int get_tag(int tag){
        while(!tags.containsTag(tag)){
            notEmpty.sleep();
        }
        int instance = tags.getValue(tag);
        tags.deleteTag(tag);
        notFull.wake();
        return instance;
    }
    
    // Place Rendezvous test code inside of the Rendezvous class.

    public static void rendezTest1() {
        final Rendezvous r = new Rendezvous();

        KThread t1 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = -1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 1, "Was expecting " + 1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv + " with tag " + tag);
            }
        });
        t1.setName("t1");
        KThread t2 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == -1, "Was expecting " + -1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv + " with tag " + tag);
            }
        });
        t2.setName("t2");

        t1.fork(); t2.fork();
        // assumes join is implemented correctly
        // ls
        t1.join(); t2.join();
    }

    public static void rendezTest2() {
        final Rendezvous r = new Rendezvous();

        KThread t1 = new KThread( new Runnable () {
            public void run() {
                int tag = 1;
                int send = -1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 1, "Was expecting " + 1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv + " with tag " + tag);
            }
        });
        t1.setName("t1");
        KThread t2 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 0;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 5, "Was expecting " + 5 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv + " with tag " + tag);
            }
        });
        t2.setName("t2");

        KThread t3 = new KThread( new Runnable () {
            public void run() {
                int tag = 1;
                int send = 1;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == -1, "Was expecting " + -1 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv + " with tag " + tag);
            }
        });
        t3.setName("t3");

        KThread t4 = new KThread( new Runnable () {
            public void run() {
                int tag = 0;
                int send = 5;

                System.out.println ("Thread " + KThread.currentThread().getName() + " exchanging " + send);
                int recv = r.exchange (tag, send);
                Lib.assertTrue (recv == 0, "Was expecting " + 0 + " but received " + recv);
                System.out.println ("Thread " + KThread.currentThread().getName() + " received " + recv + " with tag " + tag);
            }
        });
        t4.setName("t4");

        t1.fork(); t2.fork(); t3.fork(); t4.fork();
        // assumes join is implemented correctly
        // ls
        t1.join(); t2.join(); t3.join(); t4.join();
    }

        // Invoke Rendezvous.selfTest() from ThreadedKernel.selfTest()

    public static void selfTest() {
        // place calls to your Rendezvous tests that you implement here
        //rendezTest1();
        rendezTest2();
    }
}
