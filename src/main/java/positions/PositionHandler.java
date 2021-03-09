package positions;
import data.*;
import singletonHelpers.BinanceInfo;
import singletonHelpers.TelegramMessenger;
import strategies.ExitStrategy;
import utils.Utils;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;
import com.binance.client.api.model.trade.Position;
import singletonHelpers.RequestClient;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class PositionHandler implements Serializable {
    private String clientOrderId;
    private Long orderID;
    private BigDecimal qty = BigDecimal.valueOf(0.0);
    private final String symbol;
    private volatile boolean isActive = false;
    private String status = Config.NEW;
    private final ArrayList<ExitStrategy> exitStrategies;
    private Long baseTime = 0L;
    private volatile boolean rebuying = true;
    private volatile boolean isSelling = false;
    private volatile boolean terminated = false;

    public PositionHandler(Order order, ArrayList<ExitStrategy> _exitStrategies){
        clientOrderId = order.getClientOrderId();
        orderID = order.getOrderId();
        symbol = order.getSymbol().toLowerCase();
        exitStrategies = _exitStrategies;
    }

    public synchronized boolean isSoldOut(){
        return isActive && isSelling && (!status.equals(Config.NEW)) && (!rebuying) && ((qty.compareTo(BigDecimal.valueOf(0.0)) == 0));}

    public synchronized void run(RealTimeData realTimeData) {//TODO: adjust to long and short and trailing as exit method
        isSelling = false;
        for (ExitStrategy exitStrategy : exitStrategies) {
            SellingInstructions sellingInstructions = exitStrategy.run(realTimeData);
            if ((!isSelling) && sellingInstructions != null) {
                isSelling = true;
                closePosition(sellingInstructions, realTimeData);
            }
        }
    }

    public synchronized void update(RealTimeData realTimeData, CandlestickInterval interval) {
        rebuying = false;
        Position position = AccountBalance.getAccountBalance().getPosition(symbol);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        Order order = syncRequestClient.getOrder(symbol, orderID, clientOrderId);
        status = order.getStatus();
        isActive(realTimeData, order,interval);
        qty = position.getPositionAmt();
    }

    private void isActive(RealTimeData realTimeData, Order order,CandlestickInterval interval) {
        if ( !rebuying && status.equals(Config.NEW)){
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

    private synchronized void rebuyOrder(RealTimeData realTimeData, Order order) {
        rebuying = true;
        try{
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.cancelAllOpenOrder(symbol);
            OrderSide side = stringToOrderSide(order.getSide());
            Order buyOrder = syncRequestClient.postOrder(symbol, side, null, OrderType.LIMIT, TimeInForce.GTC,
                    order.getOrigQty().toString(),realTimeData.getCurrentPrice().toString(),null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
            TelegramMessenger.sendToTelegram("bought again:  " + buyOrder +", " + new Date(System.currentTimeMillis()));
            clientOrderId = buyOrder.getClientOrderId();
            orderID = buyOrder.getOrderId();
        }catch (Exception ignored){}
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
        if ( ! terminated){
            terminated = true;
            TelegramMessenger.sendToTelegram("Position closed!" + "         time: " + new Date(System.currentTimeMillis()));
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);
        }
    }

    private void closePosition(SellingInstructions sellingInstructions, RealTimeData realTimeData){
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(percentageOfQuantity(sellingInstructions.getSellingQtyPercentage()), symbol));
        switch (sellingInstructions.getType()) {

            case STAY_IN_POSITION:
                break;

            case SELL_LIMIT:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, realTimeData.getCurrentPrice().toString(), Config.REDUCE_ONLY, null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + realTimeData.getCurrentPrice().toString() +" ," + new Date(System.currentTimeMillis()));
                } catch (Exception ignored) {}
                break;

            case SELL_MARKET:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.MARKET, null,
                            sellingQty, null, Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + realTimeData.getCurrentPrice().toString() +" ," + new Date(System.currentTimeMillis()));
                } catch (Exception ignored) {}
                break;


            case CLOSE_SHORT_LIMIT:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, realTimeData.getCurrentPrice().toString(), Config.REDUCE_ONLY, null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + realTimeData.getCurrentPrice().toString() +" ," + new Date(System.currentTimeMillis()));
                } catch (Exception ignored) {}
                break;

            case CLOSE_SHORT_MARKET:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.MARKET, null,
                            sellingQty, null, Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + realTimeData.getCurrentPrice().toString() +" ," + new Date(System.currentTimeMillis()));
                } catch (Exception ignored) {}
                break;


            default:

        }
    }

    public enum ClosePositionTypes{
        STAY_IN_POSITION,
        SELL_MARKET,
        SELL_LIMIT,
        CLOSE_SHORT_MARKET,
        CLOSE_SHORT_LIMIT
    }
}
