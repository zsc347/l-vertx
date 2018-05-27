package com.scaiz.vertx.eventbus.clustered;

import com.scaiz.vertx.net.impl.ServerID;
import java.util.Objects;

public class ClusteredNodeInfo {

  private String nodeId;
  private ServerID serverID;

  public ClusteredNodeInfo() {

  }

  public ClusteredNodeInfo(String nodeId, ServerID serverID) {
    this.nodeId = nodeId;
    this.serverID = serverID;
  }

  public String getNodeId() {
    return nodeId;
  }

  public ServerID getServerID() {
    return serverID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ClusteredNodeInfo that = (ClusteredNodeInfo) o;
    return Objects.equals(this.nodeId, that.nodeId)
        && Objects.equals(this.serverID, that.serverID);
  }

  @Override
  public int hashCode() {
    int result = nodeId != null ? nodeId.hashCode() : 0;
    result = result * 31 + (serverID != null ? serverID.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return nodeId + ":" + serverID.toString();
  }
}
