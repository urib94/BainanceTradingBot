package Positions;
import CodeExecution.RealTImeOperations;
import Data.*;
import SingletonHelpers.BinanceInfo;
import SingletonHelpers.TelegramMessenger;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import Strategies.RSIStrategies.RSIExitStrategy1;
import Strategies.RSIStrategies.RSIExitStrategy2;
import Strategies.RSIStrategies.RSIExitStrategy3;
import Strategies.RSIStrategies.RSIExitStrategy4;
import Utils.Utils;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;
import com.binance.client.api.model.trade.Position;
import SingletonHelpers.RequestClient;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class PositionHandler implements Serializable {
    private String clientOrderId;
    private Long orderID;
    private BigDecimal qty = BigDecimal.valueOf(0.0);
    private final String symbol;
    private boolean isActive = false;
    private String status = Config.NEW;
    private final ArrayList<ExitStrategy> exitStrategies;
    private Long baseTime = 0L;
    private boolean isTrailing = false;
    private Order trailingOrder = null;


    public PositionHandler(Order order, ArrayList<ExitStrategy> _exitStrategies){//TODO: trying something
        clientOrderId = order.getClientOrderId();
        orderID = order.getOrderId();
        symbol = order.getSymbol().toLowerCase();
        exitStrategies = _exitStrategies;
    }

    public PositionHandler(BigDecimal qty){//TODO: trying something
        clientOrderId = null;
        orderID = null;
        symbol = Config.SYMBOL;
        status = Config.FILLED;
        isActive = true;
        this.qty = qty;
        exitStrategies = new ArrayList<>();
        exitStrategies.add(new RSIExitStrategy1());
        exitStrategies.add(new RSIExitStrategy2());
        exitStrategies.add(new RSIExitStrategy3());
        exitStrategies.add(new RSIExitStrategy4());
    }

    public synchronized boolean isSoldOut(){ return isActive && !status.equals(Config.NEW)&& (qty.compareTo(BigDecimal.valueOf(0.0)) == 0);}

    public synchronized void run(RealTimeData realTimeData) {//TODO: adjust to long and short and trailing as exit method
        for (ExitStrategy exitStrategy : exitStrategies) {
            SellingInstructions sellingInstructions = exitStrategy.run(realTimeData, isTrailing);
            if (sellingInstructions != null) {
                if (sellingInstructions.isStopTrailing()) stopTrailing();
                closePosition(sellingInstructions, realTimeData);
            }
        }
    }

    public synchronized void update(RealTimeData realTimeData, CandlestickInterval interval) {
        Position position = AccountBalance.getAccountBalance().getPosition(symbol);
        if (orderID != null) {
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            Order order = syncRequestClient.getOrder(symbol, orderID, clientOrderId);
            status = order.getStatus();
            isActive(realTimeData, order,interval);
        }
        qty = position.getPositionAmt();
    }

    private void isActive(RealTimeData realTimeData, Order order,CandlestickInterval interval) {
        if (status.equals(Config.NEW)){
            rebuyOrder(realTimeData, order);
        }
        else if (status.equals(Config.PARTIALLY_FILLED)){
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
        else { // FULL. since in NEW we don't go in the function.
            isActive = true;
        }
    }

    private void rebuyOrder(RealTimeData realTimeData, Order order) {
        try{
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            OrderSide side = stringToOrderSide(order.getSide());
            Order buyOrder = syncRequestClient.postOrder(symbol, side, null, OrderType.LIMIT, TimeInForce.GTC,
                    order.getOrigQty().toString(),realTimeData.getCurrentPrice().toString(),null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
            clientOrderId = buyOrder.getClientOrderId();
            orderID = buyOrder.getOrderId();
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    private OrderSide stringToOrderSide(String side) {
        for (OrderSide orderSide: OrderSide.values()){
            if (orderSide.toString().equals(side)) return orderSide;
        }
        return null;
    }

    private BigDecimal percentageOfQuantity(BigDecimal percentage) {
        return qty.multiply(percentage);
    }

    public synchronized void terminate(){
        TelegramMessenger.sendToTelegram("Position closed!" + "         time: " + ZonedDateTime.now());
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);
    }

    private void closePosition(SellingInstructions sellingInstructions, RealTimeData realTimeData){
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        String sellingQty = BinanceInfo.formatQty(percentageOfQuantity(sellingInstructions.getSellingQtyPercentage()), symbol);
        TelegramMessenger.sendToTelegram("Closing position - " + "expected pnl: "+ AccountBalance.getAccountBalance().getPosition(symbol).getUnrealizedProfit() + "         time: " + ZonedDateTime.now());
        switch (sellingInstructions.getType()) {

            case STAY_IN_POSITION:
                break;

            case SELL_LIMIT:
                try {
                    Order sellingOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, realTimeData.getCurrentPrice().toString(), Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                    Order orderCheck = syncRequestClient.getOrder(sellingOrder.getSymbol(), sellingOrder.getOrderId(), sellingOrder.getClientOrderId());
                } catch (Exception exception) { exception.printStackTrace();}
                break;

            case SELL_MARKET:
                try {
                    Order sellingOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.MARKET, null,
                            sellingQty, null, Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                } catch (Exception exception) { exception.printStackTrace();}
                break;

            case SELL_WITH_TRAILING:
                isTrailing = true;
                closeTrailingOrder();
                try {
                    String trailingPrice = BinanceInfo.formatPrice(calculateSellingTrailingPrice(realTimeData.getCurrentPrice(), sellingInstructions.getTrailingPercentage()), symbol);
                    trailingOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, trailingPrice, Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                } catch (Exception exception) { exception.printStackTrace();}
                break;

            case CLOSE_SHORT_LIMIT:
                try {
                    Order buyingOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, realTimeData.getCurrentPrice().toString(), Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                } catch (Exception exception) { exception.printStackTrace();}
                break;

            case CLOSE_SHORT_MARKET:
                try {
                    Order buyingOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
                            sellingQty, null, Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                } catch (Exception exception) { exception.printStackTrace();}
                break;

            case CLOSE_SHORT_WITH_TRAILING:
                isTrailing = true;
                closeTrailingOrder();
                try {
                    String trailingPrice = BinanceInfo.formatPrice(calculateBuyingTrailingPrice(realTimeData.getCurrentPrice(), sellingInstructions.getTrailingPercentage()), symbol);
                    trailingOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, trailingPrice, Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                } catch (Exception exception) { exception.printStackTrace();}
                break;

            default:

        }
    }

    private void stopTrailing() {
        closeTrailingOrder();
        isTrailing = false;
    }

    private void closeTrailingOrder() {
        if (trailingOrder != null){
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.cancelOrder(trailingOrder.getSymbol(), trailingOrder.getOrderId(), trailingOrder.getClientOrderId());
        }
    }

    private BigDecimal calculateBuyingTrailingPrice(BigDecimal currentPrice, double trailingPercentage) {
        return currentPrice.add((currentPrice.multiply(BigDecimal.valueOf(trailingPercentage)).multiply(BigDecimal.valueOf(1.0/100))));
    }

    private BigDecimal calculateSellingTrailingPrice(BigDecimal currentPrice, double trailingPercentage) {
        return currentPrice.subtract((currentPrice.multiply(BigDecimal.valueOf(trailingPercentage)).multiply(BigDecimal.valueOf(1.0/100))));
    }

    //todo: ADD MARKET AND LIMIT YA PUBLIC
    public enum ClosePositionTypes{
        STAY_IN_POSITION,
        SELL_MARKET,
        SELL_LIMIT,
        SELL_WITH_TRAILING,
        CLOSE_SHORT_MARKET,
        CLOSE_SHORT_LIMIT,
        CLOSE_SHORT_WITH_TRAILING;
    }
}
