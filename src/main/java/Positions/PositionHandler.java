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
    private BigDecimal qty;
    private final String symbol;
    private boolean isActive;
    private String status;
    private ArrayList<ExitStrategy> exitStrategies;
    private Long baseTime;

    public PositionHandler(Order order,String stopLossClientOrderId, Long stopLossOrderID, String takeProfitClientOrderId, Long takeProfitOrderID, Integer leverage, ArrayList<ExitStrategy> exitStrategies){
        clientOrderId = order.getClientOrderId();
        this.stopLossClientOrderId = stopLossClientOrderId;
        this.stopLossOrderID = stopLossOrderID;
        this.takeProfitClientOrderId = takeProfitClientOrderId;
        this.takeProfitOrderID = takeProfitOrderID;
        orderID = order.getOrderId();
        qty = BigDecimal.ZERO;
        symbol = order.getSymbol();
        isActive = false;
        this.exitStrategies = exitStrategies;
        status = Config.NEW;
        this.baseTime = 0L;
    }

    public boolean isSoldOut(){ return isActive && (qty.compareTo(BigDecimal.ZERO) <= 0);}

    public void run(RealTimeData realTimeData){
        for (ExitStrategy exitStrategy: exitStrategies){
            BigDecimal sellingQtyPercentage  = exitStrategy.run(realTimeData);
            if (sellingQtyPercentage != null){
                System.out.println("selling order: " + clientOrderId);
                String sellingQty = BinanceInfo.formatQty(percentageOfQuantity(sellingQtyPercentage), symbol);
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                System.out.println(syncRequestClient.postOrder(symbol,OrderSide.SELL, PositionSide.LONG, OrderType.LIMIT, TimeInForce.GTC,
                        sellingQty,realTimeData.getCurrentPrice().toString(),"true",null, null, null, NewOrderRespType.RESULT));
            }
        }
    }

    public void update(CandlestickInterval interval) {
        Position position = AccountBalance.getAccountBalance().getPosition(symbol);
        System.out.println("position in update: " + position);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        Order order = syncRequestClient.getOrder(symbol, orderID , clientOrderId);
        status = order.getStatus();
        qty = position.getPositionAmt();
        if (!status.equals(Config.NEW)) isActive(order,interval);
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
        System.out.println("isActive: " + isActive);
    }

    private BigDecimal percentageOfQuantity(BigDecimal percentage) {
        return qty.multiply(percentage);
    }

    public void terminate() {
        System.out.println("terminating: stopLoss: " + stopLossClientOrderId + "takeProfit: " + takeProfitClientOrderId);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        syncRequestClient.cancelOrder(symbol, stopLossOrderID, stopLossClientOrderId);
        syncRequestClient.cancelOrder(symbol, takeProfitOrderID, takeProfitClientOrderId);
    }
}
