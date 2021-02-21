package Data;



import java.math.BigDecimal;

public class PositionAction {
	private BigDecimal price;
	private BigDecimal qty;
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