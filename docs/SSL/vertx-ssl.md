1.配置SSL的服务端证书
SSL server通常会为 ssl client提供一个证书，从而能够识别client的身份
ssl server配置证书有几种方式


注：
文件后缀名
- jks是java的keytools证书工具支持的证书私钥格式
- pfx是微软支持的私钥格式
- cer是证书的公钥
- der是二进制的编码的证书，这些证书也可以用cer或crt作为扩展名
- pem是用Base64编码的各种X.509 v3证书，文件由一行"--Begin ..."开始
