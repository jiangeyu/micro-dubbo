package com.github.client;

import com.github.protocol.Request;
import com.github.protocol.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 上午11:05 2018/6/27
 * @desc
 */
@Slf4j
public class RemoteFuture implements Future<Object> {

    private Sync sync;
    private Request request;
    private Response response;
    private long startTime;
    private long responseTimeThreshold = 5000;

    private List<AsyncRemoteCallback> waitingCallbacks = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();


    public RemoteFuture(Request request) {
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }


    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        if (this.response != null) {
            return this.response.getResult();
        } else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }


    public void done(Response response) {
        this.response = response;
        sync.release(1);
        invokeCallbacks();
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            log.warn("Service response time is too slow. Request id = " + response.getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    public void invokeCallbacks() {
        lock.lock();
        try {
            waitingCallbacks.stream().forEach(callback -> runCallback(callback));
        } finally {
            lock.unlock();
        }
    }

    public RemoteFuture addCallBack(AsyncRemoteCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.waitingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(final AsyncRemoteCallback callback) {
        final Response response = this.response;
        RemoteClient.submit(() -> {
            if(response.getError() == null) {
                callback.success(response.getRequestId());
            } else {
                callback.fail(new RuntimeException("response error", new Throwable(response.getError())));
            }
        });
    }


    /**
     * 互斥锁简单实现
     */
    static class Sync extends AbstractQueuedSynchronizer {

        private final int done = 1;
        private final int await = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == await) {
                if (compareAndSetState(await, done)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        public boolean isDone() {
            getState();
            return getState() == done;
        }
    }
}
