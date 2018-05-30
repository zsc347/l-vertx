package com.scaiz.vertx.parsetools;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.streams.ReadStream;

public interface RecordParser extends Handler<Buffer>, ReadStream<Buffer> {

  void setOutput(Handler<Buffer> output);

  /**
   * only for latin-1
   */
  void delimitedMode(String delim);


  void delimitedMode(Buffer delim);

  void fixedSizeMode(int size);

  void handle(Buffer buffer);

  @Override
  RecordParser exceptionHandler(Handler<Throwable> handler);

  @Override
  RecordParser handler(Handler<Buffer> handler);

  @Override
  RecordParser pause();

  @Override
  RecordParser resume();

  @Override
  RecordParser endHandler(Handler<Void> endHandler);

}
