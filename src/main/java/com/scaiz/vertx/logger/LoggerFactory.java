package com.scaiz.vertx.logger;

public interface LoggerFactory {

  Logger getLogger(final Class<?> clazz);

}
