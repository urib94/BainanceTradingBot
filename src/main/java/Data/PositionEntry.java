package Data;
import Strategies.ExitStrategy;
import Strategies.PositionAction;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.trade.Order;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PositionEntry {
    private String clientPositionId;
    private String positionId;
    private String  asset;
    private BigDecimal origQty;
    private BigDecimal executedQty;
    private String side;





    private BigDecimal balance; //depends on order status
    private BigDecimal expectedBalance;

    private ArrayList<ExitStrategy> exitStrategies;

    public PositionEntry(String _positionId, BigDecimal _balance, String _asset, ArrayList<ExitStrategy> _exitStrategies){
        positionId = _positionId;
        balance = _balance;
        asset = _asset;
        exitStrategies = _exitStrategies;
        expectedBalance = balance;
    }

    public synchronized BigDecimal getBalance(){
        return balance;
    }

    public synchronized void run(RealTimeData realTimeData){
        for (ExitStrategy exitStrategy: exitStrategies){
            PositionAction positionAction = exitStrategy.run(AccountBalance.getAccountBalance(),realTimeData);
            if (positionAction != null){

            }
        }
    }

    public synchronized void update() {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);
        Order order = syncRequestClient.getOrder("BTCUSDT", 37886301L, null);
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


    /*
    Order fields:
        private String clientOrderId;
        private BigDecimal cumQuote;
        private BigDecimal executedQty;
        private Long orderId;
        private BigDecimal origQty;
        private BigDecimal price;
        private Boolean reduceOnly;
        private String side;
        private String positionSide;
        private String status;
        private BigDecimal stopPrice;
        private String symbol;
        private String timeInForce;
        private String type;
        private Long updateTime;
        private String workingType;*/


    /*
    position fields:

        private Boolean isolated;

        private BigDecimal leverage;

        private BigDecimal initialMargin;

        private BigDecimal maintMargin;

        private BigDecimal openOrderInitialMargin;

        private BigDecimal positionInitialMargin;

        private String symbol;

        private BigDecimal unrealizedProfit;

        private String entryPrice;

        private String maxNotional;

        private String positionSide;

    */
}
