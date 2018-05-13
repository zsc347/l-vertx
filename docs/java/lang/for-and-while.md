# for and while

## for(;;) 与 while(true)的区别

在Java语言中，for(;;)和while(true)生成的字节码是一样的，所以java语言中没有区别。

````$java
  public void whileLoop() {
    while (true) {
      System.out.println("yes");
    }
  }

  public void forLoop() {
    for (; ; ) {
      System.out.println("yes");
    }
  }
````
编译之后的字节码为
````$java
 public void whileLoop();
    Code:
       0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #3                  // String yes
       5: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: goto          0

  public void forLoop();
    Code:
       0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #3                  // String yes
       5: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: goto          0
````
但是在jdk源码和开源框架中都倾向于使用for(;;)
参考https://blog.csdn.net/baidu_29029173/article/details/51175799，猜测这种倾向
可能的原因是C语言中for(;;)在之前是优于while(true)，习惯流传导致这种倾向。
为保证与jdk源码的一致性，个人推荐使用for(;;)