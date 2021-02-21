package Positions;



import java.math.BigDecimal;

public class PositionAction {
	private BigDecimal price;
	private BigDecimal qtyPercentage;

	public PositionAction(BigDecimal price, BigDecimal qtyPercentage){
		this.price = price;
		this.qtyPercentage = qtyPercentage;
	}

	public BigDecimal getQty() { return qtyPercentage; }
	public BigDecimal getPrice() { return price; }

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