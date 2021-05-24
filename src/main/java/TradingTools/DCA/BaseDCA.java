package TradingTools.DCA;

import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import data.DataHolder;
import singletonHelpers.BinanceInfo;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.DCAStrategy;
import utils.Utils;

import java.util.Date;

public class BaseDCA implements DCAStrategy {
    private final double maxDCACount;
    private final double amountFactor;
    private final PositionSide positionSide;
    private final String symbol;
    private Order orderDCA, orderTP;
    private double step;
    private final double stepFactor;
    private double dCASize = 0;
    private int dCACount = 0;
    private double nextDCAPrice;
    private final DCAType dcaType;
    private final double distanceToTP;
    private double amount;
    private boolean initialize = true;
    private int dcaOrderCheckCounter = 0;
    private double averagePrice;

    public BaseDCA(double openPrice, double maxDCACount, double amountFactor, PositionSide positionSide,
                   double tPPrice, String symbol, double step, double stepFactor, DCAType dcaType) {
        this.maxDCACount = maxDCACount;
        this.amountFactor = amountFactor;
        this.positionSide = positionSide;
        this.symbol = symbol;
        this.step = step ;
        this.dcaType = dcaType;
        distanceToTP = (Math.abs(openPrice - tPPrice)) / (openPrice / 100);
        this.stepFactor = stepFactor;
    }


    public void run(double qty, double averagePrice) {
        this.averagePrice = averagePrice;
        if (dCACount < maxDCACount) {
            if (initialize) {
                postDCAOrder(qty, averagePrice);
                postTakeProfit(qty, averagePrice);
                postStopLoss(qty, averagePrice);
                amount = qty;
                initialize = false;
            } else {
                boolean needToDCA = qty != amount;
                boolean dcaOrderIsFilled = isDCAOrderFilled();
                if (needToDCA && dcaOrderIsFilled) {
                    postDCAOrder(qty, averagePrice);
                    postTakeProfit(qty, averagePrice);
                    amount = qty;
                }
            }
        }
    }

    private void postStopLoss(double qty, double averagePrice) {
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(qty, symbol));
        String stopLossPrice = BinanceInfo.formatPrice(calculateStopLossPrice(averagePrice), symbol);
        switch (dcaType) {
            case LONG_DCA_LIMIT:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.STOP, TimeInForce.GTC,
                            sellingQty, stopLossPrice, "true", null, stopLossPrice, null,
                            stopLossPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case LONG_DCA_MARKET:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                            sellingQty, null, "true", null, null, null,
                            stopLossPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case SHORT_DCA_LIMIT:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.STOP, TimeInForce.GTC,
                            sellingQty, stopLossPrice, "true", null, stopLossPrice, null,
                            stopLossPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case SHORT_DCA_MARKET:
                try {
                    syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                            sellingQty, null, "true", null, null, null,
                            stopLossPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    private double calculateStopLossPrice(double currentPrice) {
        double stopLossPrice = 0, stopLossStep = step;
        for (int i = 0; i <= maxDCACount; i++){
            if (positionSide == PositionSide.LONG){
                if (i == 0){
                    stopLossPrice = currentPrice - (currentPrice / 100) * stopLossStep;
                }
                else{
                    stopLossPrice = stopLossPrice - (stopLossStep * (stopLossPrice/100));
                }
            }else {
                if (i == 0){
                    stopLossPrice = currentPrice + (currentPrice / 100) * stopLossStep;
                }
                else{
                    stopLossPrice = stopLossPrice + (stopLossStep * (stopLossPrice/100));
                }
            }
            stopLossStep *= stepFactor;
        }
        return stopLossPrice;
    }

    private boolean isDCAOrderFilled() {
        if (dcaOrderCheckCounter <= 12){
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            orderDCA = syncRequestClient.getOrder(symbol, orderDCA.getOrderId(), orderDCA.getClientOrderId());
            dcaOrderCheckCounter++ ;
            return orderDCA.getStatus().equals("FILLED");
        }
        return true;
    }

    public void postDCAOrder(double qty, double averagePrice){
        dcaOrderCheckCounter = 0;
        updateDCASize(qty);
        updateNextDCAPrice(averagePrice);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(dCASize, symbol));
        String dcaPrice = BinanceInfo.formatPrice(nextDCAPrice, symbol);
        TelegramMessenger.sendToTelegram("DCA number  " + dCACount + " ," + new Date(System.currentTimeMillis()));
        switch (dcaType) {
            case LONG_DCA_LIMIT:
                try {
                    orderDCA = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, dcaPrice, null, null, null, null,
                            dcaPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("DCA price  " + dcaPrice + " ," + new Date(System.currentTimeMillis()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case LONG_DCA_MARKET:
                try {
                    orderDCA = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                            sellingQty, null, null, null, null, null,
                            dcaPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("DCA price:  " + dcaPrice + " ," + new Date(System.currentTimeMillis()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case SHORT_DCA_LIMIT:
                try {
                    orderDCA = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, dcaPrice, null, null, null, null, dcaPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("DCA price:  " + dcaPrice + " ," + new Date(System.currentTimeMillis()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case SHORT_DCA_MARKET:
                try {
                    orderDCA = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                            sellingQty, null, null, null, null, null,
                            dcaPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("DCA price:  " + dcaPrice + " ," + new Date(System.currentTimeMillis()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
        TelegramMessenger.sendToTelegram("DCA order:  " + orderDCA + " ," + new Date(System.currentTimeMillis()));
        dCACount++;
    }

    public void postTakeProfit(double qty, double averagePrice){
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            if (orderTP != null) {
                TelegramMessenger.sendToTelegram("Posting TP order ");
                syncRequestClient.cancelOrder(symbol, orderTP.getOrderId(), orderTP.getClientOrderId());
            }
            String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(qty, symbol));
            String tPPrice;
            switch (dcaType) {
                case LONG_DCA_LIMIT:
                    tPPrice = BinanceInfo.formatPrice(averagePrice + ((averagePrice / 100) * distanceToTP), symbol);
                    try {
                        orderTP = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                                sellingQty, tPPrice, "true", null, tPPrice, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case LONG_DCA_MARKET:
                    try {
                        tPPrice = BinanceInfo.formatPrice(averagePrice + ((averagePrice / 100) * distanceToTP), symbol);
                        orderTP = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
                                sellingQty, null, "true", null, null, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case SHORT_DCA_LIMIT:
                    tPPrice = BinanceInfo.formatPrice(averagePrice - ((averagePrice / 100) * distanceToTP), symbol);
                    try {
                        orderTP = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                                sellingQty, tPPrice, "true", null, tPPrice, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case SHORT_DCA_MARKET:
                    tPPrice = BinanceInfo.formatPrice(averagePrice - ((averagePrice / 100) * distanceToTP), symbol);
                    try {
                        orderTP = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
                                sellingQty, null, "true", null, null, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        TelegramMessenger.sendToTelegram("TP order:  " + orderTP + " ," + new Date(System.currentTimeMillis()));
    }

    private void updateNextDCAPrice(double currentPrice){
        if (positionSide == PositionSide.LONG){
            if (dCACount == 0){
                nextDCAPrice = currentPrice - (currentPrice / 100) * step;
            }
            else{
                nextDCAPrice = nextDCAPrice - (step * (nextDCAPrice/100));
            }
        }else {
            if (dCACount == 0){
                nextDCAPrice = currentPrice + (currentPrice / 100) * step;
            }
            else{
                nextDCAPrice = nextDCAPrice + (step * (nextDCAPrice/100));
            }
        }
        step *= stepFactor;
    }

    public void updateDCASize(double prevQTY) {
        dCASize = prevQTY * amountFactor;
    }
}

