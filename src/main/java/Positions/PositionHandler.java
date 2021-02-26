package Positions;
import Data.*;
import Strategies.ExitStrategy;
import Utils.Utils;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;
import com.binance.client.api.model.trade.Position;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PositionHandler {
    private final String clientOrderId;
    private final Long orderID;
    private final String stopLossClientOrderId;
    private final Long stopLossOrderID;
    private final String takeProfitClientOrderId;
    private final Long takeProfitOrderID;
    private BigDecimal qty = BigDecimal.valueOf(0.0);
    private final String symbol;
    private boolean isActive = false;
    private String status = Config.NEW;
    private ArrayList<ExitStrategy> exitStrategies;
    private Long baseTime = 0L;
    private BigDecimal unrealizedProfit = null;



    public PositionHandler(Order order, String stopLossClientOrderId, Long stopLossOrderID, String takeProfitClientOrderId, Long takeProfitOrderID, Integer leverage, ArrayList<ExitStrategy> exitStrategies){
        clientOrderId = order.getClientOrderId();
        this.stopLossClientOrderId = stopLossClientOrderId;
        this.stopLossOrderID = stopLossOrderID;
        this.takeProfitClientOrderId = takeProfitClientOrderId;
        this.takeProfitOrderID = takeProfitOrderID;
        orderID = order.getOrderId();
        symbol = order.getSymbol().toLowerCase();
        this.exitStrategies = exitStrategies;
    }

    public boolean isSoldOut(){ return isActive && (qty.compareTo(BigDecimal.valueOf(0.0)) <= 0);}

    public void run(RealTimeData realTimeData) {
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        for (ExitStrategy exitStrategy : exitStrategies) {
            BigDecimal sellingQtyPercentage = exitStrategy.run(realTimeData);
            if (sellingQtyPercentage != null) {
                String sellingQty = BinanceInfo.formatQty(percentageOfQuantity(sellingQtyPercentage), symbol);
                try {
                    Order sellingOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, realTimeData.getCurrentPrice().toString(), "true", null, null, null, null, NewOrderRespType.RESULT);
                } catch (Exception e) { System.out.println(e);}
            }
        }
    }

    public void update(CandlestickInterval interval) {
        Position position = AccountBalance.getAccountBalance().getPosition(symbol);
        System.out.println(position);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        Order order = syncRequestClient.getOrder(symbol, orderID , clientOrderId);
        status = order.getStatus();
        qty = position.getPositionAmt();
        unrealizedProfit = position.getUnrealizedProfit();
        if (!status.equals(Config.NEW)) isActive(order,interval);
        else {
            //TODO: buy order again
        }
    }

    private void isActive(Order order,CandlestickInterval interval) {
        if (status.equals(Config.PARTIALLY_FILLED)){
            Long updateTime = order.getUpdateTime();
            if (baseTime.equals(0L)) {
                baseTime = updateTime;
            } else {
               long difference = updateTime - baseTime;
               Long intervalInMilliSeconds = Utils.candleStickIntervalToMilliseconds(interval);
               if  (difference >= (intervalInMilliSeconds/2.0)) {
                   SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                   syncRequestClient.cancelOrder(symbol,orderID,clientOrderId);
                   isActive = true;
               }
            }
        }
        else {
            isActive = true;
        }
    }

    private BigDecimal percentageOfQuantity(BigDecimal percentage) {
        return qty.multiply(percentage);
    }

    public void terminate(){
        System.out.println("terminating");
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        syncRequestClient.changeInitialLeverage(Config.SYMBOL,Config.LEVERAGE);
        syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);
    }
}
