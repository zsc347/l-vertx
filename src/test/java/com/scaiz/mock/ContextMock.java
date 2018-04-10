package com.scaiz.mock;

import com.scaiz.Context;
import com.scaiz.async.Closeable;
import com.scaiz.async.Handler;

public class ContextMock implements Context {

  @Override
  public void runOnContext(Handler<Void> action) {
    action.handle(null);

  }

  @Override
  public void addCloseHook(Closeable hook) {

  }

  @Override
  public void removeCloseHook(Closeable hook) {

  }

}
