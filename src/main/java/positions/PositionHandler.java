package positions;
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
    private  ArrayList<DCAStrategy> DCAStrategeis;
    private String clientOrderId;
    private Long orderID;
    private double qty = 0.0;
    private final String symbol;
    private volatile boolean isActive = false;
    private String status = Config.NEW;
    private  ArrayList<ExitStrategy> exitStrategies;
    private Long baseTime = 0L;
    private volatile boolean rebuying = true;
    private volatile boolean isSelling = false;
    private volatile boolean terminated = false;
    SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();

    public PositionHandler(Order order, ArrayList<ExitStrategy> _exitStrategies){
        clientOrderId = order.getClientOrderId();
        orderID = order.getOrderId();
        symbol = order.getSymbol().toLowerCase();
        exitStrategies = _exitStrategies;
    }


    public PositionHandler(Order order, ArrayList<ExitStrategy> _exitStrategies, ArrayList<DCAStrategy> _DCAStrategies){
        clientOrderId = order.getClientOrderId();
        orderID = order.getOrderId();
        symbol = order.getSymbol().toLowerCase();
        exitStrategies = _exitStrategies;
        DCAStrategeis = _DCAStrategies;
    }

    public synchronized boolean isSoldOut(){
        System.out.println(symbol);
        boolean noOpenOrders = syncRequestClient.getOpenOrders(symbol).size() == Config.ZERO;
        return isActive && noOpenOrders && (!rebuying) && ((qty == 0.0));}

    public synchronized void run(DataHolder realTimeData)
    {//TODO: adjust to long and short and trailing as exit method
        isSelling = false;
        if (isActive) {


        }
        if(DCAStrategeis!=null) {
            for (DCAStrategy DCAStrategy : DCAStrategeis) {
                DCAStrategy.run(realTimeData);
                if (DCAStrategy.getNeedToTP()) {
                    Class instance=DCAStrategy.getClass();

                    switch (DCAStrategy.getPositionSide()) {
                        case SHORT:
                            System.out.println("short first TP, DCA");
                            closePosition(new SellingInstructions(ClosePositionTypes.SELL_LIMIT, Config.ONE_HANDRED)
                                    , realTimeData, realTimeData.getCurrentPrice()+10);
                            DCAStrategy.DCAOrder(DCAStrategy.getDCAInstructions());
                            break;
                        case LONG:
                            System.out.println("long first TP, DCA");
                            closePosition(new SellingInstructions(ClosePositionTypes.SELL_LIMIT, Config.ONE_HANDRED)
                                    , realTimeData, realTimeData.getCurrentPrice()+10);
                            DCAStrategy.DCAOrder(DCAStrategy.getDCAInstructions());
                            break;
                    }

                } else {
                    if (DCAStrategy.getNeedToDCA() && DCAStrategy.getDCAInstructions() != null) {
                        TelegramMessenger.sendToTelegram("update after DCA");
                        DCAStrategy.DCAOrder(DCAStrategy.getDCAInstructions());
                        DCAStrategy.updatTP(qty+DCAStrategy.getDCASize(), realTimeData);
                        DCAStrategy.increaseDCACount();
                        break;
                    }
                }
            }
        }

            for (ExitStrategy exitStrategy : exitStrategies) {
                SellingInstructions sellingInstructions = (SellingInstructions) exitStrategy.run(realTimeData);
                if ((!isSelling) && sellingInstructions != null) {
                    isSelling = true;
                    closePosition(sellingInstructions, realTimeData, -1);
                }
            }

    }
        public synchronized void update (DataHolder realTimeData, CandlestickInterval interval){
            rebuying = false;
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            Order order = syncRequestClient.getOrder(symbol, orderID, clientOrderId);
            status = order.getStatus();
            isActive(realTimeData, order, interval);
            qty = AccountBalance.getAccountBalance().getPosition(symbol).getPositionAmt().doubleValue();

        }

        private void isActive (DataHolder realTimeData, Order order, CandlestickInterval interval){
            if (status.equals(Config.NEW)) {
                rebuyOrder(order);
            } else if (status.equals(Config.PARTIALLY_FILLED)) {
                Long updateTime = order.getUpdateTime();
                if (baseTime.equals(0L)) {
                    baseTime = updateTime;
                } else {
                    long difference = updateTime - baseTime;
                    Long intervalInMilliSeconds = Utils.candleStickIntervalToMilliseconds(interval);
                    if (difference >= (intervalInMilliSeconds / 2.0)) {
                        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                        syncRequestClient.cancelOrder(symbol, orderID, clientOrderId);
                        isActive = true;
                    }
                }
            } else { // FULL. since in NEW we don't go in the function.
                isActive = true;
            }
        }

        private synchronized void rebuyOrder (Order order){
            rebuying = true;
            try {
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.cancelOrder(order.getSymbol(), order.getOrderId(), order.getClientOrderId());//// TODO: 4/5/2021  cheke if this canceletion methood is working.
                //            syncRequestClient.cancelAllOpenOrder(symbol);
                OrderSide side = stringToOrderSide(order.getSide());
                Order buyOrder = syncRequestClient.postOrder(symbol, side, null, OrderType.MARKET, null,
                        order.getOrigQty().toString(), null, null, null, null, null, null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought again:  " + buyOrder + ", " + new Date(System.currentTimeMillis()));
                clientOrderId = buyOrder.getClientOrderId();
                orderID = buyOrder.getOrderId();
            } catch (Exception ignored) {
            }
        }

        private OrderSide stringToOrderSide (String side){
            for (OrderSide orderSide : OrderSide.values()) {
                if (orderSide.toString().equals(side)) return orderSide;
            }
            return null;
        }

        public static double percentageOfQuantity ( double percentage,double qty){
            return qty * percentage;
        }

        public void terminate () {
            if (!terminated) {
                terminated = true;
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);
                TelegramMessenger.sendToTelegram("Position closed!, balance:  " + AccountBalance.getAccountBalance().getCoinBalance("usdt") + ", " + new Date(System.currentTimeMillis()));
            }
        }

        public void closePosition (SellingInstructions sellingInstructions, DataHolder realTimeData, double price) {
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(percentageOfQuantity(sellingInstructions.getSellingQtyPercentage(), qty), symbol));
            switch (sellingInstructions.getType()) {

                case STAY_IN_POSITION:
                    break;

                case SELL_LIMIT:
                    try {
                        syncRequestClient.postOrder(this.symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                                sellingQty, String.valueOf(realTimeData.getCurrentPrice()), Config.REDUCE_ONLY, null, null, null, null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception ignored) {
                    }
                    break;

                case SELL_MARKET:
                    try {
                        syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.MARKET, null,
                                sellingQty, null, Config.REDUCE_ONLY, null, null, null, null, null, null, null, NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception ignored) {
                    }
                    break;


                case CLOSE_SHORT_LIMIT:
                    try {
                        syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                                sellingQty, String.valueOf(realTimeData.getCurrentPrice()), Config.REDUCE_ONLY, null, null, null, null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception ignored) {
                    }
                    break;

                case CLOSE_SHORT_MARKET:
                    try {
                        syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.MARKET, null,
                                sellingQty, null, Config.REDUCE_ONLY, null, null, null, null, null, null, null, NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(realTimeData.getCurrentPrice()) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception ignored) {
                    }
                    break;


                default:

                case TP:
                    try {
                        syncRequestClient.postOrder(this.symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                                sellingQty, String.valueOf(realTimeData.getCurrentPrice()), Config.REDUCE_ONLY, null, null, null, null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(price) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception ignored) {
                    }
                    break;
            }

        }

    public enum ClosePositionTypes {
        STAY_IN_POSITION,
        SELL_MARKET,
        SELL_LIMIT,
        CLOSE_SHORT_MARKET,
        CLOSE_SHORT_LIMIT,
        TP,
        SL
    }



    }

