package net.wang.socket.channel.server;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolBuilder {

    public static ExecutorService build(String name, int coreSize, int maxSize) {

        return new ThreadPoolExecutor(coreSize, maxSize, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>(100), new DefaultThreadFactory(name),new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ExecutorService buildFixPool(String name, int size) {
        return Executors.newFixedThreadPool(size,new DefaultThreadFactory(name));
    }

    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = name + "-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
