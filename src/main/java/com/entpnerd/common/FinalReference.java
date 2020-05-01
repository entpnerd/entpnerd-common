package com.entpnerd.common;

import java.util.concurrent.atomic.AtomicReference;

public final class FinalReference<T> {
  private final AtomicReference<T> reference = new AtomicReference<T>();

  public FinalReference() {
  }

  public void set(T value) {
    this.reference.compareAndSet(null, value);
  }

  public T get() {
    return this.reference.get();
  }
}
