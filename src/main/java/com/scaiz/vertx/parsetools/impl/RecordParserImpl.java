package com.scaiz.vertx.parsetools.impl;

import com.scaiz.vertx.async.Handler;
import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.parsetools.RecordParser;
import com.scaiz.vertx.streams.ReadStream;
import java.util.Objects;

public class RecordParserImpl implements RecordParser {

  private Buffer buff;

  private Handler<Buffer> output;
  private final ReadStream<Buffer> stream;

  private Handler<Throwable> exceptionHandler;
  private Handler<Void> endHandler;

  private int start; // position of beginning of current record
  private int delimPos; // position of current match in delimiter array
  private int pos; // current position in buffer

  private boolean delimited;
  private int recordSize;
  private byte[] delim;
  private boolean reset;

  private RecordParserImpl(ReadStream<Buffer> stream, Handler<Buffer> output) {
    this.stream = stream;
    this.output = output;
  }

  public static RecordParser newFixed(int size, ReadStream<Buffer> stream,
      Handler<Buffer> output) {
    RecordParserImpl parser = new RecordParserImpl(stream, output);
    parser.fixedSizeMode(size);
    return parser;
  }

  public static RecordParser newDelimited(Buffer delim,
      ReadStream<Buffer> stream, Handler<Buffer> output) {
    RecordParserImpl parser = new RecordParserImpl(stream, output);
    parser.delimitedMode(delim);
    return parser;
  }

  public static RecordParser newDelimited(String delim,
      ReadStream<Buffer> stream, Handler<Buffer> output) {
    RecordParserImpl parser = new RecordParserImpl(stream, output);
    parser.delimitedMode(delim);
    return parser;
  }

  @Override
  public void setOutput(Handler<Buffer> output) {
    Objects.requireNonNull(output, "output");
    this.output = output;
  }

  @Override
  public void delimitedMode(String delim) {
    delimitedMode(latin1StringToBytes(delim));
  }

  private Buffer latin1StringToBytes(String str) {
    byte[] bytes = new byte[str.length()];
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      bytes[i] = (byte) (c & 0xff);
    }
    return Buffer.buffer(bytes);

  }

  @Override
  public void delimitedMode(Buffer delim) {
    Objects.requireNonNull(delim, "delim");
    delimited = true;
    this.delim = delim.getBytes();
    delimPos = 0;
    reset = true;
  }


  public void fixedSizeMode(int size) {
    if (size <= 0) {
      throw new IllegalStateException("size must > 0");
    }
    delimited = false;
    recordSize = size;
    reset = true;
  }

  private void handleParsing() {
    int len = buff.length();
    do {
      reset = false;
      if (delimited) {
        parseDelimited();
      } else {
        parseFixed();
      }
    } while (reset);

    if (start == len) {
      buff = null;
      pos = 0;
    } else {
      buff = buff.getBuffer(start, len);
      pos = buff.length();
    }

    start = 0;
  }

  private void parseFixed() {
    int len = buff.length();
    while (len - start >= recordSize && !reset) {
      int end = start + recordSize;
      Buffer ret = buff.getBuffer(start, end);
      start = end;
      pos = start - 1;
      output.handle(ret);
    }
  }

  private void parseDelimited() {
    int len = buff.length();
    for (; pos < len && !reset; pos++) {
      if (buff.getByte(pos) == delim[delimPos]) {
        delimPos++;
        if (delimPos == delim.length) {
          Buffer ret = buff.getBuffer(start, pos - delim.length + 1);
          start = pos + 1;
          delimPos = 0;
          output.handle(ret);
        }
      } else {
        if (delimPos > 0) {
          pos -= delimPos;
          delimPos = 0;
        }
      }
    }
  }


  @Override
  public void handle(Buffer buffer) {
    if (buff == null) {
      buff = buffer;
    } else {
      buff.appendBuffer(buffer);
    }
    handleParsing();
  }

  @Override
  public RecordParser exceptionHandler(Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }

  private void end() {
    Handler<Void> handler = endHandler;
    if (handler != null) {
      handler.handle(null);
    }
  }

  @Override
  public RecordParser handler(Handler<Buffer> handler) {
    output = handler;
    if (stream != null) {
      if (handler != null) {
        stream.endHandler(v -> end());
        stream.exceptionHandler(err -> {
          if (exceptionHandler != null) {
            exceptionHandler.handle(err);
          }
        });
        stream.handler(this);
      } else {
        stream.handler(null);
        stream.endHandler(null);
        stream.exceptionHandler(null);
      }
    }
    return this;
  }

  @Override
  public RecordParser pause() {
    if (stream != null) {
      stream.pause();
    }
    return this;
  }

  @Override
  public RecordParser resume() {
    if (stream != null) {
      stream.resume();
    }
    return this;
  }

  @Override
  public RecordParser endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }
}
