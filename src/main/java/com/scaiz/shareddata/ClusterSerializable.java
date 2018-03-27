package com.scaiz.shareddata;


import com.scaiz.buffer.Buffer;

public interface ClusterSerializable {

  void writeToBuffer(Buffer buffer);

  int readFromBuffer(int pos, Buffer buffer);
}
