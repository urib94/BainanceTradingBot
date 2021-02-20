package Data;

import com.binance.client.impl.RestApiInvoker;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;

import java.math.BigDecimal;

public class PositionAction {
	private BigDecimal price;
	private Enum orderType;
	private boolean reduceOnly;
}




/*
System.out.println(syncRequestClient.postOrder("BTCUSDT", OrderSide.SELL, PositionSide.SHORT, OrderType.LIMIT, TimeInForce.GTC,
		"1", "9000", null, null, null, null, NewOrderRespType.RESULT));
		postOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType orderType, TimeInForce timeInForce,
		 String quantity, String price, String reduceOnly, String newClientOrderId, String stopPrice, WorkingType workingType, NewOrderRespType newOrderRespType) {


String symbol
OrderSide side
PositionSide positionSide
OrderType orderType
TimeInForce timeInForce
String quantity
String price
String reduceOnly
String stopPrice



 */