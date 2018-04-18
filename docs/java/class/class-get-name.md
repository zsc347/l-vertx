# Java中获取class名称

Java中提供了三种获取类名的方式
+ getName
+ getCanonicalName
+ getSimpleName

getName返回虚拟机内的class表示(FQCN)
getCanonicalName返回更容易理解的表示
大部分情况下是相同的，但对于array或内部类而言来说,getCanonicalName返回的名字更容易理解。


getSimpleName 不包含包名