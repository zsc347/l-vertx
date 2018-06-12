package com.scaiz.vertx.eventbus.impl.clustered;

import com.scaiz.vertx.buffer.Buffer;
import com.scaiz.vertx.container.VertxInternal;
import com.scaiz.vertx.eventbus.EventBusOptions;
import com.scaiz.vertx.eventbus.impl.CodecManager;
import com.scaiz.vertx.net.NetClient;
import com.scaiz.vertx.net.NetClientOptions;
import com.scaiz.vertx.net.NetSocket;
import com.scaiz.vertx.net.impl.NetClientImpl;
import com.scaiz.vertx.net.impl.ServerID;
import java.util.ArrayDeque;
import java.util.Queue;

public class ConnectionHolder {

  private final VertxInternal vertx;
  private final ClusteredEventBus eventBus;
  private final ServerID serverID;
  private final NetClient client;

  private NetSocket socket;
  private boolean connected;
  private long timeoutID = -1;
  private long pingTimeoutID = -1;
  private Queue<ClusteredMessage> pending;

  private static final String PING_ADDRESS = "__vertx_ping";

  ConnectionHolder(ClusteredEventBus clusteredEventBus,
      ServerID serverID, EventBusOptions options) {
    this.eventBus = clusteredEventBus;
    this.serverID = serverID;
    this.vertx = eventBus.vertx();
    NetClientOptions clientOptions = new NetClientOptions(options.toJson());
    this.client = new NetClientImpl(vertx, clientOptions, false);
  }

  synchronized void connect() {
    if (connected) {
      throw new IllegalStateException("Already connected");
    }
    client.connect(serverID.getPort(), serverID.getHost(), res -> {
      if (res.succeeded()) {
        connected = true;
        this.socket = res.result();
        socket.exceptionHandler(t -> close());
        socket.closeHandler(v -> close());
        socket.handler(data -> {
          vertx.cancelTimer(timeoutID);
          schedulePing();
        });
        schedulePing();
        if (pending != null) {
          for (ClusteredMessage message : pending) {
            Buffer data = message.encodeToWire();
            socket.write(data);
          }
        }
        pending = null;
      } else {
        System.err.println("Connecting to server " + serverID + " failed : "
            + res.cause().getMessage());
        res.cause().printStackTrace();
        close();
      }
    });
  }

  private void schedulePing() {
    EventBusOptions options = eventBus.options();
    pingTimeoutID = vertx.setTimer(options.getClusterPingInterval(), id1 -> {
      timeoutID = vertx.setTimer(options.getClusterPingInterval(), id2 -> {
        System.err.println("No pong from server " +
            serverID + " - will consider it dead");
        close();
      });
      ClusteredMessage pingMessage = new ClusteredMessage<>(
          serverID, PING_ADDRESS, null,
          null, null, CodecManager.PING_MESSAGE_CODEC,
          true, eventBus);
      socket.write(pingMessage.encodeToWire());
    });
  }

  public void close() {
    if (timeoutID != -1) {
      vertx.cancelTimer(timeoutID);
    }
    if (pingTimeoutID != -1) {
      vertx.cancelTimer(pingTimeoutID);
    }
    try {
      client.close();
    } catch (Exception ignore) {
    }
    eventBus.getConnections().remove(serverID, this);
  }

  synchronized void writeMessage(ClusteredMessage message) {
    if (connected) {
      socket.write(message.encodeToWire());
    } else {
      if (pending == null) {
        pending = new ArrayDeque<>();
      }
      pending.add(message);
    }
  }
}
