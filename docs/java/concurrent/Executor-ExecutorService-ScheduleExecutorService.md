# Executor, ExecutorService, ScheduledExecutorService

## Executor
Executor接口类如下，只有一个方法 `void execute(Runnale command)`, 典型的命令模式，
提交一个Runnable任务，并执行。

Executor接口用于实现任务创建和任务执行之间解耦合。
````
public interface Executor {
    void execute(Runnable command);
}

````

## ExecutorService
Executor
ExecutorService接口总共可以分为以下几类
+ shutdown and shutdownNow
+ submit 可以提交Runnable, Callable
+ invokeALl and invokeAny

1. `shutdown` 和 `shutdownNow` 
`shutdown` 会使ExecutorService不再接受新的任务，
但仍然会等待之前已经提交的任务在终止之前继续执行。
`shudownNow` 会不再执行等待的任务，并且会尝试终止当前正在执行的任务。

如下方法中会通过两个阶段来关闭ExecutorService
第一个阶段中通过`shutdown`来停止接受新的任务
第二个阶段执行`shutdownNow`来取消等待执行的任务，并尝试终止正在执行的任务。

````
void shutdownAndAwaitTermination(ExecutorService pool) {
  pool.shutdown(); // Disable new tasks from being submitted
   try {
     // Wait a while for existing tasks to terminate
     if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
       pool.shutdownNow(); // Cancel currently executing tasks
       // Wait a while for tasks to respond to being cancelled
       if (!pool.awaitTermination(60, TimeUnit.SECONDS))
           System.err.println("Pool did not terminate");
     }
   } catch (InterruptedException ie) {
     // (Re-)Cancel if current thread also interrupted
     pool.shutdownNow();
     // Preserve interrupt status
     Thread.currentThread().interrupt();
}
````

`awaitTermination`的作用是等待ExecutorService终止，如果在等待时间中
终止则返回true， 否则返回false，这个函数会阻塞当前进程，并且可能会因为当前
线程的中断而提前返回抛出`InterruptedException`。

2. `invokeAll` 和 `invokeAny`
等待所有任务执行完成或者等待至少一个任务完成。`invokeAny`中一个任务完成会取消
其他的等待的任务。
等待过程中被中断会抛出`InterruptedException`
任务被拒绝执行这两个函数会抛出`RejectedExecutionException`, 区别是`invokeAll`只
要有一个任务被拒绝执行就会抛出，而`invokeAny`会在所有任务被拒绝执行时抛出。



当函数返回时，返回的Future列表中的所有Future的`isDone`均为true，即提交的所有
task均已经终止。当然终止的原因既可能是任务已经顺利执行，也有可能是任务执行中抛出
异常。如果这些提交的任务中有任务被拒绝执行，这个函数会抛出`RejectedExecutionException`。
如果在等待过程中被中断，没有执行完成的任务会被cancel并且抛出`InterruptedException`
。

3. ExecutorService 中 继承的`execute`方法和自带的`submit`方法的区别
参考： 
https://stackoverflow.com/questions/3929342/choose-between-executorservices-submit-and-executorservices-execute

简要翻译：
`execute`方法中，如果task抛出异常会导致线程的`UncaughtExceptionHandler`被执行，
而`submit`方法中，如果task抛出异常则可以通过返回结果`Future`来获得。

## ScheduledExecutorService
增加了以下接口
+ schedule 可以提交
+ scheduleAtFixedRate
+ scheduleWithFixedDelay

`scheduleAtFixedRate` 和 `scheduleWithFixedDelay` 的区别是前者以一定的频率来执行
一项任务，而后者固定每一次任务执行完成后经过延迟多久开始下一次的执行。
`scheduleAtFixedRate`可能遇到一个
问题是任务执行时间过长，超过了参数中定义的`rate`时间，此时任务执行会有延迟。

一个示例
````
class BeeperControl {
   private final ScheduledExecutorService scheduler =
     Executors.newScheduledThreadPool(1);

   public void beepForAnHour() {
     final Runnable beeper = new Runnable() {
       public void run() { System.out.println("beep"); }
     };
     final ScheduledFuture<?> beeperHandle =
       scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS);
     scheduler.schedule(new Runnable() {
       public void run() { beeperHandle.cancel(true); }
     }, 60 * 60, SECONDS);
   }
}}
````
