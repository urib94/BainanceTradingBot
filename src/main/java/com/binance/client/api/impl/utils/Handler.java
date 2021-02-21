package com.binance.client.api.impl.utils;

@FunctionalInterface
public interface Handler<T> {

  void handle(T t);
}
