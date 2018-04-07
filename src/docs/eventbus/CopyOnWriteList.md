# Copy on write list

From link https://www.cnblogs.com/dolphin0520/p/3938914.html

Copy on Write, 处理并发问题的一种思路。简称COW。
基本思想是读写分离。

同类型有： CopyOnWriteList， CopyOnWriteSet

其思路是， 当往一个容器中添加元素时，不直接往容器中添加，
而是先将当前容器Copy，复制出一个新的容器后再修改。
添加完元素后，再将原容器引用指向新的容器。


优点是：
- 可以并发读取，而不需要加锁

缺点：
- 内存占用， 因为写时全复制会导致大量内存占用
- 数据一致性， 不能保证写入的数据能被及时读取

适用场景：
多读少写。

如vertx中EventBus实现中的interceptor

疑问：
Linux内核中有copy on write linked list， 可以解决缺点中的内存占用问题，
为什么java中没有

stack over flow link 

https://stackoverflow.com/questions/22572566/why-copyonwritelinkedlist-does-not-exist
https://stackoverflow.com/questions/14639260/copyonwritelinkedlist-implementation-for-java

两个回答对我而言不具备说服力，待解。