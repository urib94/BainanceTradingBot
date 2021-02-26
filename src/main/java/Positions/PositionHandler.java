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
    private BigDecimal unrealizedProfit = null;



    public PositionHandler(Order order, String stopLossClientOrderId, Long stopLossOrderID, String takeProfitClientOrderId, Long takeProfitOrderID, Integer leverage, ArrayList<ExitStrategy> exitStrategies){
        clientOrderId = order.getClientOrderId();
        this.stopLossClientOrderId = stopLossClientOrderId;
        this.stopLossOrderID = stopLossOrderID;
        this.takeProfitClientOrderId = takeProfitClientOrderId;
        this.takeProfitOrderID = takeProfitOrderID;
        orderID = order.getOrderId();
        qty = BigDecimal.valueOf(0.0);
        symbol = order.getSymbol().toLowerCase();
        isActive = false;
        this.exitStrategies = exitStrategies;
        status = Config.NEW;
        this.baseTime = 0L;

    }

    public PositionHandler(BigDecimal qty, String clientOrderId, ArrayList<ExitStrategy> exitStrategies){
        symbol = Config.SYMBOL;
        this.qty = BigDecimal.valueOf(0.0);
        this.clientOrderId = clientOrderId;
        this.stopLossClientOrderId = null;
        this.stopLossOrderID = null;
        this.takeProfitClientOrderId = null;
        this.takeProfitOrderID = null;
        orderID = null;
        isActive = false;
        this.exitStrategies = exitStrategies;
        status = Config.NEW;
        this.baseTime = 0L;
    }

    public boolean isSoldOut(){ return isActive && (qty.compareTo(BigDecimal.valueOf(0.0)) <= 0);}

    public void run(RealTimeData realTimeData){
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        for (ExitStrategy exitStrategy: exitStrategies){
            //BigDecimal sellingQtyPercentage  = exitStrategy.run(realTimeData);
            BigDecimal sellingQtyPercentage = BigDecimal.valueOf(100.0);//TODO: change test
            if (sellingQtyPercentage != null && status.equals(Config.FILLED) && qty != null && qty.compareTo(BigDecimal.valueOf(0.0)) <= 0){
                String sellingQty = BinanceInfo.formatQty(percentageOfQuantity(sellingQtyPercentage), symbol);

                try{
                    if (unrealizedProfit != null && unrealizedProfit.compareTo(BigDecimal.valueOf(0.0)) < 0){
                        System.out.println("here1");
                        Order stopLossOrder1 = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.STOP_MARKET, TimeInForce.GTC,
                                null,null,null,null, realTimeData.getCurrentPrice().subtract(BigDecimal.valueOf(100.0)).toString(),"true", null, NewOrderRespType.RESULT);
                        Order stopLossOrder2 = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.STOP_MARKET, TimeInForce.GTC,
                                null,null,null,null, realTimeData.getCurrentPrice().subtract(BigDecimal.valueOf(10.0)).toString(),"true", null, NewOrderRespType.RESULT);
                        Order stopLossOrder3 = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.STOP_MARKET, TimeInForce.GTC,
                                null,null,null,null, realTimeData.getCurrentPrice().subtract(BigDecimal.valueOf(1.0)).toString(),"true", null, NewOrderRespType.RESULT);
                        Order stopLossOrder4 = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.STOP_MARKET, TimeInForce.GTC,
                                null,null,null,null, realTimeData.getCurrentPrice().toString(),"true", null, NewOrderRespType.RESULT);
                    }
                    else if (unrealizedProfit != null && unrealizedProfit.compareTo(BigDecimal.valueOf(0.0)) > 0){
                        System.out.println("here2");
                        Order takeProfitOrder1 = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
                                null,null,null,null, realTimeData.getCurrentPrice().add(BigDecimal.valueOf(100.0)).toString() ,"true", null, NewOrderRespType.RESULT);
                        Order takeProfitOrder2 = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
                                null,null,null,null, realTimeData.getCurrentPrice().add(BigDecimal.valueOf(10.0)).toString() ,"true", null, NewOrderRespType.RESULT);
                        Order takeProfitOrder3 = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
                                null,null,null,null, realTimeData.getCurrentPrice().add(BigDecimal.valueOf(1.0)).toString() ,"true", null, NewOrderRespType.RESULT);
                        Order takeProfitOrder4 = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
                                null,null,null,null, realTimeData.getCurrentPrice().toString() ,"true", null, NewOrderRespType.RESULT);
                    }
                }catch (Exception e){
                    System.out.println(e);
                }
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
