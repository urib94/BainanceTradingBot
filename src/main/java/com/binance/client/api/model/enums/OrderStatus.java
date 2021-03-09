package com.binance.client.api.model.enums;

import com.binance.client.api.impl.utils.EnumLookup;

/**
 * NEW, PARTIALLY_FILLED, FILLED. CANCELED, REJECTED EXPIRED.
 */
public enum OrderStatus {
  NEW("NEW"),
  PARTIALLY_FILLED("PARTIALLY_FILLED"),
  FILLED("FILLED"),
  CANCELED("CANCELED"),
  REJECTED("REJECTED"),
  EXPIRED("EXPIRED");


  private final String code;

  OrderStatus(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<OrderStatus> lookup = new EnumLookup<>(OrderStatus.class);

  public static OrderStatus lookup(String name) {
    return lookup.lookup(name);
  }
}
