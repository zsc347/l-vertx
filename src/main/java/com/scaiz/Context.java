package com.scaiz;

import com.scaiz.async.Handler;

public interface Context {
    void runOnContext(Handler<Void> action);
}
