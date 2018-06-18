package com.scaiz.vertx.deploy;

import java.util.Set;

public interface Deployment {

  boolean addChild(Deployment deployment);

  void removeChild(Deployment deployment);

  

  Set<Verticle> getVerticles();

  boolean isChild();
}
