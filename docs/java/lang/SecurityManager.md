# SecurityManager

当运行未知java程序时，由于该程序可能有恶意代码，为了防止恶意代码造成破坏，
需要对运行的代码的权限进行控制。这个时候就需要启用Java安全管理器。

启用安全管理有两种方式，一种是使用启动参数，另一种是使用编码方式
启动参数方式
````
-Djava.security.manager
// 同时制定配置文件位置
-Djava.security.manager -Djava.security.policy="/etc/java.policy"
````
编码方式启动
````
System.setSecurityManager(new SecurityManager())
````

安全管理器配置文件
白名单模式，指定能做什么

参考
https://www.cnblogs.com/yiwangzhibujian/p/6207212.html
