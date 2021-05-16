package org.example.closable;

import javax.inject.Singleton;

import java.io.Closeable;

@Singleton
public class ShouldCloseAuto implements Closeable {

  private boolean closed;

  @Override
  public void close() {
    closed = true;
  }

  boolean isClosed() {
    return closed;
  }
}
