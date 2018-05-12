# Netty Config 

### WaterMark
+ writerBufferHighWaterMark

当写入buffer中未处理的字节数超过writerBufferHighWaterMark时，
Channel.isWritable()会返回false。

+ writerBufferLowWaterMark
当写入buffer由于未处理字节超过writerBufferHighWaterMark导致不可写，
之后经过处理后buffer未处理字节到达writerBufferLowWaterMark时，
Channel.isWritable()会重新返回true.

