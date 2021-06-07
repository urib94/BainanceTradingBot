package main.java.positions;
import data.*;
import positions.SellingInstructions;
import singletonHelpers.BinanceInfo;
import singletonHelpers.TelegramMessenger;
import strategies.DCAStrategy;
import strategies.ExitStrategy;
import strategies.nutral.NutralExit;
import utils.Utils;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import singletonHelpers.RequestClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class PositionHandler implements Serializable {
    private ArrayList<DCAStrategy> dcaStrategies;
    private double qty = 0.0;
    private double averagePrice = 0.0;
    private final String symbol;
    private volatile boolean isActive = false;
    private final ArrayList<ExitStrategy> exitStrategies;
    private volatile boolean isSelling = false;
    private volatile boolean terminated = false;
    private double stopLossPercentage;
    private boolean requireStopLoss = false;
    private PositionSide positionSide;

    public PositionHandler(Order order, ArrayList<ExitStrategy> _exitStrategies, double stopLossPercentage, PositionSide positionSide) {
        symbol = order.getSymbol().toLowerCase();
        exitStrategies = _exitStrategies;
        this.stopLossPercentage = stopLossPercentage;
        if (stopLossPercentage != 0) requireStopLoss = true;
        this.positionSide = positionSide;
    }


    public PositionHandler(Order order, ArrayList<ExitStrategy> _exitStrategies, ArrayList<DCAStrategy> dcaStrategies) {
        symbol = order.getSymbol().toLowerCase();
        exitStrategies = _exitStrategies;
        this.dcaStrategies = dcaStrategies;
    }

    public PositionHandler(Order order, NutralExit nutralExit){
        exitStrategies = null;
        symbol = order.getSymbol().toLowerCase();


    }
    public synchronized boolean isSoldOut() {
        return isActive && (qty == 0.0);
    }

    public synchronized void run(DataHolder realTimeData) {//TODO: adjust to long and short and trailing as exit method
        double currentPrice = realTimeData.getCurrentPrice();
        isSelling = false;
        if (isActive) {
            if (requireStopLoss){
                requireStopLoss = false;
                postStopLoss(qty, averagePrice, positionSide, symbol);
            }
            if(dcaStrategies != null) {
                for (DCAStrategy dcaStrategy : dcaStrategies) {
                    dcaStrategy.run(qty, averagePrice);
                }
            }
            if(exitStrategies !=null) {
                for (ExitStrategy exitStrategy : exitStrategies) {
                    SellingInstructions sellingInstructions = exitStrategy.run(realTimeData);
                    if ((!isSelling) && sellingInstructions != null) {
                        isSelling = true;
                        closePosition(sellingInstructions, realTimeData, currentPrice);
                    }
                }
            }
        }

    }

    public synchronized void update() {
        averagePrice = Double.parseDouble(AccountBalance.getAccountBalance().getPosition(symbol).getEntryPrice());
        qty = AccountBalance.getAccountBalance().getPosition(symbol).getPositionAmt().doubleValue();
        if (qty != 0 && !isActive) {
            isActive = true;
        }
    }

    private void isActive(DataHolder realTimeData, Order order, CandlestickInterval interval) {
//            if (status.equals(Config.NEW)) {
//                rebuyOrder(order);
//            } else if (status.equals(Config.PARTIALLY_FILLED)) {
//                Long updateTime = order.getUpdateTime();
//                if (baseTime.equals(0L)) {
//                    baseTime = updateTime;
//                } else {
//                    long difference = updateTime - baseTime;
//                    Long intervalInMilliSeconds = Utils.candleStickIntervalToMilliseconds(interval);
//                    if (difference >= (intervalInMilliSeconds / 2.0)) {
//                        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//                        syncRequestClient.cancelOrder(symbol, orderID, clientOrderId);
//                        isActive = true;
//                    }
//                }
//            } else { // FULL. since in NEW we don't go in the function.
//                isActive = true;
//            }
    }

//    private synchronized void rebuyOrder(Order order) {
//        rebuying = true;
//        try {
//            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//            syncRequestClient.cancelOrder(order.getSymbol(), order.getOrderId(), order.getClientOrderId());//// TODO: 4/5/2021  cheke if this canceletion methood is working.
//            //            syncRequestClient.cancelAllOpenOrder(symbol);
//            OrderSide side = stringToOrderSide(order.getSide());
//            Order buyOrder = syncRequestClient.postOrder(symbol, side, null, OrderType.MARKET, null,
//                    order.getOrigQty().toString(), null, null, null, null, null, null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
//            TelegramMessenger.sendToTelegram("bought again:  " + buyOrder + ", " + new Date(System.currentTimeMillis()));
//        } catch (Exception ignored) {
//        }
//    }

    private OrderSide stringToOrderSide(String side) {
        for (OrderSide orderSide : OrderSide.values()) {
            if (orderSide.toString().equals(side)) return orderSide;
        }
        return null;
    }

    public static double percentageOfQuantity(double percentage, double qty) {
        return qty * (percentage / 100);
    }

    public void terminate() {
        if (!terminated) {
            terminated = true;
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);
            TelegramMessenger.sendToTelegram("Position closed!, balance:  " + AccountBalance.getAccountBalance().getCoinBalance("usdt") + ", " + new Date(System.currentTimeMillis()));
        }
    }

    public void closePosition(SellingInstructions sellingInstructions, DataHolder realTimeData, double price) {
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
                    TelegramMessenger.sendToTelegram("Selling price:  " + realTimeData.getCurrentPrice() + " ," + new Date(System.currentTimeMillis()));
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

    private void postStopLoss(double qty, double averagePrice, PositionSide positionSide, String symbol) {
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(qty, symbol));
        String stopLossPrice = BinanceInfo.formatPrice(calculateStopLossPrice(averagePrice, positionSide), symbol);
        switch (positionSide) {
            case LONG:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                            sellingQty, null, "true", null, stopLossPrice, null,
                            null, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case SHORT:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                            sellingQty, null, "true", null, stopLossPrice, null,
                            null, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    private double calculateStopLossPrice(double averagePrice, PositionSide positionSide) {
        switch (positionSide){

            case SHORT:
                return averagePrice + averagePrice * (stopLossPercentage / 100);
            case LONG:
                return averagePrice - averagePrice * (stopLossPercentage / 100);
        }
        return averagePrice;//never
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




