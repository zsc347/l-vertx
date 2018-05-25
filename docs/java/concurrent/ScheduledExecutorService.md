# ScheduledExecutorService

Executor接口类如下，只有一个方法 `void execute(Runnale command)`, 典型的命令模式，
提交一个Runnable任务，并执行。

Executor接口用于实现任务创建和任务执行之间解耦合。
````
public interface Executor {
    void execute(Runnable command);
}

````

ExecutorService接口总共可以分为以下几类
+ shutdown and shutdownNow
+ submit 可以提交Runnable, Callable
+ invokeALl and invokeAny

````
public interface ExecutorService extends Executor {


    void shutdown();
    List<Runnable> shutdownNow();


    boolean isShutdown();
    boolean isTerminated();


    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;


    <T> Future<T> submit(Callable<T> task);
    <T> Future<T> submit(Runnable task, T result);
    Future<?> submit(Runnable task);


    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException;
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                  long timeout, TimeUnit unit)
        throws InterruptedException;


    <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException;
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}

````