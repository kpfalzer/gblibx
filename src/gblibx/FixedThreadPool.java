
package gblibx;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FixedThreadPool<T> {
    public static final int DEFAULT_NTHREADS = 64;

    public FixedThreadPool(int nthreads) {
        __threadPool = Executors.newFixedThreadPool(nthreads);
    }

    public FixedThreadPool() {
        this(DEFAULT_NTHREADS);
    }

    public Future<T> submit(Runnable task, T rval) {
        return __threadPool.submit(task, rval);
    }

    public List<Runnable> shutdown() {
        return __threadPool.shutdownNow();
    }

    public boolean isTerminated() {
        return __threadPool.isTerminated();
    }

    private final ExecutorService __threadPool;
}
