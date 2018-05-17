# LockSupport

https://blog.csdn.net/hengyunabc/article/details/28126139

内容抄录如下

LockSupport实际上调用了Unsafe里的函数，归结到Unsafe里，只有两个函数:
````
public native void unpark(Thread thread)
// isAbsolte指明时间是绝对的，还是相对的
pulbic native void park(boolean isAbsolute, long time)
````
对这两个函数的解释

`unpark`函数为线程提供“许可”，线程调用`park`
函数则等待“许可”。这个有点像信号量，但是“许可”
不能叠加，许可只能被使用一次。

许可的另一个特性是， `unpark`可以先于`park`
调用。比如线程B调用 `unpark`函数，给线程A发了
一个许可，那么线程A调用 `park`时，发现已经有
许可了，那么它就会马上再继续运行。

实际上, `park`函数即使没有许可，有时候也会
无理由的返回。

`park` 和 `unpark`的灵活之处
`unpark`函数可以先于 `park`调用，从而带来了灵活性。
这样就可以不用考虑 `park` 与 `unpark`的时序问题，

考虑如下case
Java5中同步采用wait/notify/notifyAll来实现同步，
这三个方法的限制是，如果B要采用notify来通知A，前提
是A已经在wait调用上等待了，否则A可能永远都在等待。
另外需要考虑的是应该调用notify还是notifyAll，
如果错误的有两个线程在同一个对象上等待。
为了安全起见，就只能调用notifyAll了。

`unpark` 与 `park` 解耦了线程之间的同步，
线程之间不再需要一个Object或者其他变量来存储状态，
不再需要关心对方的状态。

park会在以下几种情况下被唤醒
+ 有其他线程调用 `unpark`
+ 有其他线程调用 `Thread.interrupt`
+ 可能出现无原因返回

----------------------------------------------------------------

由于可能会出现无条件返回，所以通常在使用 `park`时，需要使用以下形式

````
while (!canProceed()) { 
    ... 
    LockSupport.park(this); 
}
````


示例代碼
````
public class FIFOMutex {

  private final Queue<Thread> waiters = new ConcurrentLinkedQueue<>();
  private final AtomicBoolean locked = new AtomicBoolean(false);

  public void lock() {
    boolean wasInterrupted = false;
    Thread current = Thread.currentThread();
    waiters.add(current);

    while (waiters.peek() != current
        || !locked.compareAndSet(false, true)) {
      LockSupport.park(this);
      if (!Thread.interrupted()) {
        wasInterrupted = true;
      }

      waiters.remove();
      if (wasInterrupted) {
        current.interrupt();
      }
    }
  }

  public void unlock() {
    locked.set(false);
    LockSupport.unpark(waiters.peek());
  }
}
````

代碼解读
第一个线程执行到
````
while (waiters.peek() != current
        || !locked.compareAndSet(false, true))

````
时假设这个时候没有第二个线程，则
waiters.peek() != current -> false
!locked.compareAndSet(false, true) -> false
直接pass

假定这个时候第二个线程开始执行
waiters.peek() != current -> true 第二个线程需要执行LockSupport.park(this)

假定由第一个线程 `unpark`, 第二个线程被唤醒，开始往下执行 
waiters.remove(); 删除掉第一个线程
waiters.peek() != current -> false
!locked.compareAndSet(false, true) -> false

最终状态
waiters里会有一个element ？







 