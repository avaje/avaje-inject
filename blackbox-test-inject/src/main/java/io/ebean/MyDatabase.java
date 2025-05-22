package io.ebean;

public class MyDatabase implements Database {

  public boolean shutdownCalled = false;

  @Override
  public void shutdown() {
    shutdownCalled = true;
  }

  @Override
  public boolean isShutdown() {
    return shutdownCalled;
  }
}
