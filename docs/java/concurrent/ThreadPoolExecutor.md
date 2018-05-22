# ThreadPoolExecutor

ThreadPoolExecutor,线程池执行器，当我们谈到XX池，都是为了资源复用。
建小资源的创建与释放的代价。

线程池需要考虑以下几个问题
1. 什么时候创建线程?
2. 线程什么时候释放?
3. 线程不够用怎么办?

先来明确第三个问题的定义，什么情况下是线程不够用的情况，线程池用于执行任务，
当一个任务来临的时候，我们有四种选择，
1. 创建一个新的线程来执行
2. 从线程池中选一个闲置的线程来执行
3. 将线程放入缓冲区，等待池中有可用的线程
4. 拒绝任务执行

这些选择可以通过ThreadPoolExecutor来配置
````
ThreadPoolExecutor(int corePoolSize,
                   int maximumPoolSize,
                   long keepAliveTime,
                   TimeUnit unit,
                   BlockingQueue<Runnable> workQueue) {

````
第五个参数对应了缓冲区的配置，ThreadPoolExecutor策略选择的优先顺序是
1. 当池中线程数 < corePoolSize 时，优先创建新线程
2. 当池中线程数在 [corePoolSize, maximumPoolSize]之间时，优先放入缓冲区等待执行，
如果缓冲区放满了，则会创建新的线程来执行
3. 当池中线程数到达 `maximumPoolSize`，此时若有新任务到来若缓冲区已经填满且线程都在
执行任务，那么任务将会被拒绝执行。

再来考虑线程什么时候释放的问题，ThreadPoolExecutor的构造函数中有两个参数对应于这个问题。
`keepAliveTime` 和 `TimeUnit unit`, 这两个参数共同决定了线程的保活时间。当线程闲置的
时候超过保活时间后，线程将会被释放。`keepAliveTime` 默认情况下只应用于
(corePoolSize, maximumPoolSize]之间的线程，即当池中线程数目`<=corePoolSize`时，
线程默认情况下不会释放。但是可以通过`allowCoreThreadTimeOut`方法来使得`keepAliveTime`
对核心线程生效。