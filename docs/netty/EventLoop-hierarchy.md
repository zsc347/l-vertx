# EventLoop Hierarchy

### EventExecutorGroup

`EventExecutorGroup` 在ScheduleExecutorService的基础上增加了以下函数
+ shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
+ Future<?> terminationFuture()
+ iterator() 和 next()

并且将其他的函数中返回的Future替换成了Netty自己的抽象的增加通知能力和非阻塞能力的Future

1. shutdownGracefully

`ExecutorService` 提供了两个关闭函数，一个是`shutdown`，用于停止接受新的任务，
一个是`shutdownNow`,用于停止接受新的任务并且拒绝等待的任务，同时尝试终止正在执行的任务。
`EventExecutorGroup`中弃用了`shutdown`函数，并提供了`shutdownGracefully`用于执行器的关闭。
`shutdownGracefully`保证在真正调用`shutdown`之前，有一段等待时间，只有这段时间中
没有新的任务到来才会真正的进入`shutdown`状态，否则会重新开始计算。

````
Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
```` 

其中`quietPeriod`标志需要等待的时间, `timeout`强制调用`shutdown`的时间。

2. terminationFuture
这个函数会返回一个future，用于通知所有由这个group管理的EventExecutor都已经被终止。

3.  iterator 和 next
`iterator()`使用迭代器模式来管理`EventExecutor`
`next`用于返回这个group中的一个`EventExecutor`


### EventExecutor
+ next() 和 parent()
+ inEventLoop
+ newPromise 和 newProgressivePromise
+ newSucceededFuture 和 newFailedFuture

1. next 和 parent
EventExecutor本身也是一个EventExecutorGroup，只是next()将返回自己。
parent()将返回其所属的EventExecutorGroup

2. inEventLoop
判断一个线程是否在Netty的event loop 中执行

3. promise 和 future

### EventLoopGroup
+ EventLoop next()
+ ChannelFuture register(Channel channel)
+ ChannelFuture register(ChannelPromise promise)

特殊的EventExecutorGroup, 可以注册Channel

### EventLoop

````
interface EventLoop extends OrderedEventExecutor, EventLoopGroup
````

其中`OrderedEventExecutor`只是一个标志接口，用于标志所有提交的任务会被顺序执行


