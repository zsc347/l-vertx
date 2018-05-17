1. StackTrace Message


2. Daemon thread
当JVM线程只剩下守护线程的时候, JVM就会自动退出。
但如果还有其他任意一个用户线程存在，JVM就不会退出。

3. 线程状态

+ NEW
线程刚刚创建

+ RUNNABLE
可运行状态，表示线程正在JVM中运行但可能正在等待某些资源，如可用的处理器

+ BLOCKED
阻塞状态，线程正在等待一个监视器锁。等待进入同步区

+ WAITING
等待状态。线程会由于调用以下几个方法进入等待状态
Object#wait() 没有设置timeout
Thread.join() 没有设置timeout
LockSupport#park()
处于此状态的线程需要等待另一个线程调用 Object#notify()方法来唤醒，
如果是由于调用Thread#join进入此状态，则需要等待一个特定的线程终止
 

+ TIMED_WAITING,
标志一个线程正处于等待状态，但是有指定的过期时间
线程可由调用以下方法时传入一个等待时间来进入此状态
Thread.sleep(xxx) Thread.join(xxx)
Object.wait(xxx)
LockSupport.parkNanos(xxx) LockSupport.parkUntil

+ TERMINATED
线程终止状态，线程已经完成了执行
