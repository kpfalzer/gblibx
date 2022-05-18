package gblibx;

import java.util.LinkedList;

/**
 * Ordered (concurrent) queue.
 * Highest ordered item first/top.
 * Order determined by Comparable interface (of T).
 * (NOTE: The java.util.PriorityXXX didn;t seem to allow high priority ordering?)
 */
public class PriorityQueue<T extends Comparable> {
    /**
     * Insert jobs into ordered queue.
     *
     * @param jobs jobs to insert.
     * @return this object.
     */
    public synchronized PriorityQueue<T> insert(T... jobs) {
        for (T job : jobs) insert(job);
        return this;
    }

    /**
     * Insert job into ordered queue.
     * Higher priority are before lower priority.
     * Equal priority inserted after all of same priority
     * (so oldest stay near top).
     *
     * @param job jobs to insert.
     * @return this object.
     */
    public synchronized PriorityQueue<T> insert(T job) {
        int index = 0;
        for (T curr : __jobs) {
            if (0 < job.compareTo(curr)) {
                __jobs.add(index, job);
                return this;
            }
            index++;
        }
        __jobs.add(job);
        return this;
    }

    public synchronized int size() {
        return __jobs.size();
    }

    public synchronized boolean isEmpty() {
        return __jobs.isEmpty();
    }

    public synchronized T peek() {
        return __jobs.peek();
    }

    public synchronized T pop() {
        return __jobs.removeFirst();
    }

    private final LinkedList<T> __jobs = new LinkedList<>();
}