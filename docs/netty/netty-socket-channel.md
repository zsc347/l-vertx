# netty 支持的各种socketChannel

+ OioSocketChannel
传统阻塞式编程

+ NioSocketChannel
select/poll 或 epoll，jdk7之后Linux下会自动选择epoll

+ EpollSocketChannel
epoll，仅限linux

+ EpollDomainSocketChannel
IPC模式，仅限客户端，服务器在相同主机的情况