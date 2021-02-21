package com.binance.client.api.model.enums;

import com.binance.client.api.impl.utils.EnumLookup;

public enum TransferMasterType {


  MASTER_TRANSFER_IN("master-transfer-in"),
  MASTER_TRANSFER_OUT("master-transfer-out"),
  MASTER_POINT_TRANSFER_IN("master-point-transfer-in"),
  MASTER_POINT_TRANSFER_OUT("master-point-transfer-out");
  private final String code;

  TransferMasterType(String side) {
    this.code = side;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<TransferMasterType> lookup = new EnumLookup<>(TransferMasterType.class);

  public static TransferMasterType lookup(String name) {
    return lookup.lookup(name);
  }
}
