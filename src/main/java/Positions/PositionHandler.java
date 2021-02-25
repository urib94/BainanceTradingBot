package Positions;
import Data.*;
import Strategies.ExitStrategy;
import Utils.TimeConstants;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;
import com.binance.client.api.model.trade.Position;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;

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
        qty = null;
        symbol = order.getSymbol().toLowerCase();
        isActive = false;
        this.exitStrategies = exitStrategies;
        status = Config.NEW;
        this.baseTime = 0L;

    }

    public boolean isSoldOut(){ return false;}//isActive && (qty.compareTo(BigDecimal.ZERO) <= 0);}//TODO: fix

    public void run(RealTimeData realTimeData){
        for (ExitStrategy exitStrategy: exitStrategies){
            //BigDecimal sellingQtyPercentage  = exitStrategy.run(realTimeData);
            BigDecimal sellingQtyPercentage = BigDecimal.valueOf(100.0);//TODO: change test
            if (sellingQtyPercentage != null && status.equals(Config.FILLED) && qty != null){
                System.out.println("selling order: " + clientOrderId);
                String sellingQty = BinanceInfo.formatQty(percentageOfQuantity(sellingQtyPercentage), symbol);
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                System.out.println("here");
                Order sellingOrder = syncRequestClient.postOrder(symbol,OrderSide.SELL, PositionSide.SHORT, OrderType.LIMIT, TimeInForce.GTC,
                        sellingQty,realTimeData.getCurrentPrice().toString(),null,null, null, null, NewOrderRespType.RESULT);
                System.out.println("sold order");
                System.out.println(sellingOrder);
            }
        }
    }

    public void update(CandlestickInterval interval) {
        AccountBalance.getAccountBalance().aggresiveUpdateBalance();
        Position position = AccountBalance.getAccountBalance().getPosition(symbol);
        System.out.println(position);
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
               Long intervalInMilliSeconds = candleStickIntervalToMilliseconds(interval);
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

    private Long candleStickIntervalToMilliseconds(CandlestickInterval interval) {
        String intervalCode = interval.toString();
        int value = Integer.parseInt(intervalCode.substring(0,intervalCode.length()-1));
        char typeOfTime = intervalCode.charAt(intervalCode.length()-1);
        switch (typeOfTime) {
            case 'm':
                return (long) value * TimeConstants.MINUTES_TO_MILLISECONDS_CONVERTER;
            case 'h':
                return (long) value * TimeConstants.HOURS_TO_MILLISECONDS_CONVERTER;
            case 'd':
                return (long) value * TimeConstants.DAYS_TO_MILLISECONDS_CONVERTER;
            case 'w':
                return (long) value * TimeConstants.WEEKS_TO_MILLISECONDS_CONVERTER;
            case 'M':
                return (long) value * TimeConstants.MONTHS_TO_MILLISECONDS_CONVERTER;
            default:
                return -1L;
        }
    }

    public void terminate() {
        System.out.println("terminating: stopLoss: " + stopLossClientOrderId + "takeProfit: " + takeProfitClientOrderId);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        syncRequestClient.cancelOrder(symbol, stopLossOrderID, stopLossClientOrderId);
        syncRequestClient.cancelOrder(symbol, takeProfitOrderID, takeProfitClientOrderId);
    }
}
