package com.scaiz.shareddata;

import java.nio.Buffer;

public interface ClusterSerializable {

  void writeToBuffer(Buffer buffer);

  int readFromBuffer(int pos, Buffer buffer);
}
