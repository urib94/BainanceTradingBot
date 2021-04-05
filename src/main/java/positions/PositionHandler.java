package positions;
import TradingTools.DCA.BaseDCA;
import data.*;
import singletonHelpers.BinanceInfo;
import singletonHelpers.TelegramMessenger;
import strategies.DCAStrategy;
import strategies.ExitStrategy;
import utils.Utils;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import singletonHelpers.RequestClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class PositionHandler implements Serializable {
    private String clientOrderId;
    private Long orderID;
    private double qty = 0.0;
    private final String symbol;
    private volatile boolean isActive = false;
    private String status = Config.NEW;
    private final ArrayList<ExitStrategy> exitStrategies;
    private ArrayList <DCAStrategy> DCAStrategies;
    private Long baseTime = 0L;
    private volatile boolean rebuying = true;
    private volatile boolean isSelling = false;
    private volatile boolean terminated = false;
    private BaseDCA baseDCA;
    private boolean isDCAing=false;

    public PositionHandler(Order order, ArrayList<ExitStrategy> _exitStrategies){
        clientOrderId = order.getClientOrderId();
        orderID = order.getOrderId();
        symbol = order.getSymbol().toLowerCase();
        exitStrategies = _exitStrategies;
    }

    public PositionHandler(Order order, ArrayList<ExitStrategy> _exitStrategies,ArrayList <DCAStrategy> dcaStrateys){
        clientOrderId = order.getClientOrderId();
        orderID = order.getOrderId();
        symbol = order.getSymbol().toLowerCase();
        exitStrategies = _exitStrategies;
        this.DCAStrategies = dcaStrateys;
    }

    public synchronized boolean isSoldOut(){
        return isActive && isSelling && (!status.equals(Config.NEW)) && (!rebuying) && ((qty == 0.0));}

    public synchronized void run(DataHolder realTimeData) {//TODO: adjust to long and short and trailing as exit method
        isSelling = false;
        for (DCAStrategy dcaStrategy: DCAStrategies){
            DCAInstructions dcaInstructions=dcaStrategy.run(realTimeData);
            if ((!isDCAing) && dcaInstructions!=null) {
                DCAOrder(dcaInstructions, realTimeData);
                break;
            }
        }
        for (ExitStrategy exitStrategy : exitStrategies) {
            SellingInstructions sellingInstructions = exitStrategy.run(realTimeData);
            if ((!isSelling) && sellingInstructions != null) {
                isSelling = true;
                closePosition(sellingInstructions, realTimeData);
                break;
            }
        }
    }

    public synchronized void update(DataHolder realTimeData, CandlestickInterval interval) {
        rebuying = false;
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        Order order = syncRequestClient.getOrder(symbol, orderID, clientOrderId);
        status = order.getStatus();
        isActive(realTimeData, order,interval);
        qty = AccountBalance.getAccountBalance().getPosition(symbol).getPositionAmt().doubleValue();
    }

    private void isActive(DataHolder realTimeData, Order order,CandlestickInterval interval) {
        if (status.equals(Config.NEW)){
            rebuyOrder(order);
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

    private synchronized void rebuyOrder(Order order) {
        rebuying = true;
        try{
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.cancelOrder(order.getSymbol(),order.getOrderId(),order.getClientOrderId());//// TODO: 4/5/2021  cheke if this canceletion methood is working.
            //            syncRequestClient.cancelAllOpenOrder(symbol);
            OrderSide side = stringToOrderSide(order.getSide());
            Order buyOrder = syncRequestClient.postOrder(symbol, side, null, OrderType.MARKET, null,
                    order.getOrigQty().toString(),null,null,null, null,null,null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
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

    private double percentageOfQuantity(double percentage) {
        return qty * percentage;
    }

    public void terminate(){
        if ( ! terminated){
            terminated = true;
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);
            TelegramMessenger.sendToTelegram("Position closed!, balance:  " + AccountBalance.getAccountBalance().getCoinBalance("usdt") +", " + new Date(System.currentTimeMillis()));
        }
    }

    private void closePosition(SellingInstructions sellingInstructions, DataHolder realTimeData){
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(percentageOfQuantity(sellingInstructions.getSellingQtyPercentage()), symbol));
        switch (sellingInstructions.getType()) {

            case STAY_IN_POSITION:
                break;

            case SELL_LIMIT:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, String.valueOf(realTimeData.getCurrentPrice()), Config.REDUCE_ONLY, null, null,null,null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) +" ," + new Date(System.currentTimeMillis()));
                } catch (Exception ignored) {}
                break;

            case SELL_MARKET:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.MARKET, null,
                            sellingQty, null, Config.REDUCE_ONLY, null, null, null,null,null, null, null, NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) +" ," + new Date(System.currentTimeMillis()));
                } catch (Exception ignored) {}
                break;


            case CLOSE_SHORT_LIMIT:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, String.valueOf(realTimeData.getCurrentPrice()), Config.REDUCE_ONLY, null, null,null,null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) +" ," + new Date(System.currentTimeMillis()));
                } catch (Exception ignored) {}
                break;

            case CLOSE_SHORT_MARKET:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.MARKET, null,
                            sellingQty, null, Config.REDUCE_ONLY, null, null, null,null,null, null, null, NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) +" ," + new Date(System.currentTimeMillis()));
                } catch (Exception ignored) {}
                break;


            default:

        }
    }

    private void DCAOrder (DCAInstructions dcaInstructions , DataHolder realTimeData){
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        String sellingQty = String.valueOf(baseDCA.calculateDCASize());
        Order order=new Order();
        long orderIn= System.currentTimeMillis();
        switch(dcaInstructions.getType()){

            case LONG_DCA_LIMIT:
                try {
                    order = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, String.valueOf(realTimeData.getCurrentPrice()), null, null, null,null,null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) +" ," + new Date(System.currentTimeMillis()));
                    orderIn= System.currentTimeMillis();
                } catch (Exception ignored) {}
                break;
            case LONG_DCA_MARKET:
                try {
                    order = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.MARKET, null,
                            sellingQty, null,null, null, null, null,null,null, null, null, NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) +" ," + new Date(System.currentTimeMillis()));
                    orderIn= System.currentTimeMillis();
                } catch (Exception ignored) {}
                break;
            case SHORT_DCA_LIMIT:
                try {
                    order = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, null,
                            sellingQty, null, null, null, null, null,null,null, null, null, NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) +" ," + new Date(System.currentTimeMillis()));
                     orderIn= System.currentTimeMillis();
                } catch (Exception ignored) {}
                break;
            case SHORT_DCA_MARKET:
                try {
                    order = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.MARKET, null,
                            sellingQty, null, null, null, null, null,null,null, null, null, NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) +" ," + new Date(System.currentTimeMillis()));
                    orderIn= System.currentTimeMillis();
                } catch (Exception ignored) {}
                break;
        }
        if (dcaInstructions.getType()== PositionHandler.DCAType.LONG_DCA_LIMIT||dcaInstructions.getType()== PositionHandler.DCAType.SHORT_DCA_LIMIT) {
            long cur = System.currentTimeMillis();
            while (cur - orderIn < 4000) cur = System.currentTimeMillis();
            rebuyOrder(order);
        }
    }



    public enum ClosePositionTypes{
        STAY_IN_POSITION,
        SELL_MARKET,
        SELL_LIMIT,
        CLOSE_SHORT_MARKET,
        CLOSE_SHORT_LIMIT
    }

    public enum DCAType{
        LONG_DCA_LIMIT,
        LONG_DCA_MARKET,
        SHORT_DCA_LIMIT,
        SHORT_DCA_MARKET
    }
}
