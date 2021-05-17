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
    private ArrayList<DCAStrategy> DCAStrategies;
    private String clientOrderId;
    private Long orderID;
    private int openOrderCount = 0;
    private boolean halt = false;
    private double qty = 0.0;
    private double entryPrice = 0.0;
    private static double amount = 0;
    private final String symbol;
    private volatile boolean isActive = false;
    private  ArrayList<ExitStrategy> exitStrategies;
    private Long baseTime = 0L;
    private volatile boolean rebuying = true;
    private volatile boolean isSelling = false;
    private volatile boolean terminated = false;
    private boolean newPosition = true;
    SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
    private long timeRefrencer = System.currentTimeMillis();

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
        DCAStrategies = _DCAStrategies;
        amount = qty;
    }

    public synchronized boolean isSoldOut(){
        boolean res = isActive && (!rebuying) && ((qty == 0.0));
        if(res)System.out.println("is sold out ");
        return res;}

    public synchronized void run(DataHolder realTimeData)
    {//TODO: adjust to long and short and trailing as exit method
       double currentPrice = realTimeData.getCurrentPrice();
       if(!halt) {
           isSelling = false;
           if (isActive) {
               if (DCAStrategies != null) {
                   for (DCAStrategy DCAStrategy : DCAStrategies) {
                       if (newPosition) {
                           DCAStrategy.run(realTimeData);
                           System.out.println("TP & DCA for new position");
                           switch (DCAStrategy.getPositionSide()) {
                               case SHORT:
                                   System.out.println("short first TP, DCA");
                                   DCAStrategy.TakeProfit(new SellingInstructions(ClosePositionTypes.SELL_LIMIT, Config.ONE_HUNDRED),
                                           qty, entryPrice, realTimeData);
                                   DCAStrategy.DCAOrder(DCAStrategy.getDCAInstructions(), realTimeData);
                                   break;
                               case LONG:
                                   System.out.println("long first TP, DCA");
                                   DCAStrategy.TakeProfit(new SellingInstructions(ClosePositionTypes.SELL_LIMIT, Config.ONE_HUNDRED),
                                           qty, entryPrice, realTimeData);
                                   DCAStrategy.DCAOrder(DCAStrategy.getDCAInstructions(), realTimeData);
                                   break;
                           }
                           newPosition = false;
                       } else {
                           System.out.println("need for DCA" + DCAStrategy.getNeedToDCA() + "      DCAStrategy.getDCAInstructions() != null " + DCAStrategy.getDCAInstructions() != null);
                           if(currentPrice > DCAStrategy.gettPPrice() && DCAStrategy.getTpOrder() != null){
                               if(DCAStrategy.getPositionSide() == PositionSide.LONG) {
                                   closePosition(new SellingInstructions(ClosePositionTypes.SELL_MARKET,Config.ONE_HUNDRED), realTimeData, currentPrice);
                               }
                           }else if(currentPrice < DCAStrategy.gettPPrice() && DCAStrategy.getTpOrder() != null){
                               closePosition(new SellingInstructions(ClosePositionTypes.CLOSE_SHORT_MARKET,Config.ONE_HUNDRED), realTimeData, currentPrice);
                           }
                           if (DCAStrategy.getNeedToDCA() && DCAStrategy.getDCAInstructions() != null && DCAStrategy.getdCACount() <= DCAStrategy.getMaxDCACount()) {
                               System.out.println("tp &SL for existing position");
                               TelegramMessenger.sendToTelegram("update after DCA");
                               DCAStrategy.DCAOrder(DCAStrategy.getDCAInstructions(), realTimeData);
                               DCAStrategy.TakeProfit(new SellingInstructions(ClosePositionTypes.SELL_LIMIT, Config.ONE_HUNDRED),
                                       qty, entryPrice, realTimeData);
                           }
                       }
                   }
               }

               for (ExitStrategy exitStrategy : exitStrategies) {
                   SellingInstructions sellingInstructions = (SellingInstructions) exitStrategy.run(realTimeData);
                   if ((!isSelling) && sellingInstructions != null) {
                       isSelling = true;
                       closePosition(sellingInstructions, realTimeData, currentPrice);
                   }
               }
           }
       }
    }

    public synchronized void update (DataHolder realTimeData, CandlestickInterval interval){
        timeRefrencer=System.currentTimeMillis();
            rebuying = false;
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            Order order = syncRequestClient.getOrder(symbol, orderID, clientOrderId);
            isActive(realTimeData, order, interval);
            entryPrice=Double.parseDouble(AccountBalance.getAccountBalance().getPosition(symbol).getEntryPrice());
            qty = AccountBalance.getAccountBalance().getPosition(symbol).getPositionAmt().doubleValue();
            if(qty!=0 && !isActive){
                isActive=true;
            }
            else if(isSoldOut()){
                halt=true;
            }
            if (amount!=qty&& amount!=0.0){
                System.out.println("qty= "+qty+"    amount="+amount);
                for (DCAStrategy DCAStrategy : DCAStrategies){
                    DCAStrategy.setNeedToDCA(true);
                }
            }
            else if (amount!=0){
                for (DCAStrategy DCAStrategy : DCAStrategies){
                DCAStrategy.setNeedToDCA(false);
            }
        }
        amount=qty;
    }

        private void isActive (DataHolder realTimeData, Order order, CandlestickInterval interval){
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

    public static double getQty() {
        return amount;
    }

    public  double getEntryPrice(){ return  entryPrice;}
    }

