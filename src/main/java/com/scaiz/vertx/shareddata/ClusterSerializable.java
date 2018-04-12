package com.scaiz.vertx.shareddata;


import com.scaiz.vertx.buffer.Buffer;

public interface ClusterSerializable {

  void writeToBuffer(Buffer buffer);

  int readFromBuffer(int pos, Buffer buffer);
}
