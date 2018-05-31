package com.scaiz.vertx.parsetools;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.parsetools.impl.RecordParserImpl;
import com.scaiz.vertx.streams.ReadStream;

public interface RecordParser extends Handler<Buffer>, ReadStream<Buffer> {

  static RecordParser newDelimited(String delim, Handler<Buffer> output) {
    return RecordParserImpl.newDelimited(delim, null, output);
  }

  static RecordParser newDelimited(String delim, ReadStream<Buffer> stream) {
    return RecordParserImpl.newDelimited(delim, stream, null);
  }

  static RecordParser newDelimited(String delim) {
    return RecordParserImpl.newDelimited(delim, null, null);
  }

  static RecordParser newDelimited(Buffer delim) {
    return RecordParserImpl.newDelimited(delim,null,  null);
  }

  static RecordParser newDelimited(Buffer delim, Handler<Buffer> output) {
    return RecordParserImpl.newDelimited(delim, null, output);
  }

  static RecordParser newDelimited(Buffer delim, ReadStream<Buffer> stream) {
    return RecordParserImpl.newDelimited(delim, stream, null);
  }

  static RecordParser newFixed(int size) {
    return RecordParserImpl.newFixed(size, null, null);
  }

  static RecordParser newFixed(int size, Handler<Buffer> output) {
    return RecordParserImpl.newFixed(size, null, output);
  }

  static RecordParser newFixed(int size, ReadStream<Buffer> stream) {
    return RecordParserImpl.newFixed(size, stream, null);
  }

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
