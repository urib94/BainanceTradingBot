//package strategies.StochRsiCrosse;
//
//import TradingTools.Trailers.SkippingExitTrailer;
//import com.binance.client.SyncRequestClient;
//import com.binance.client.model.enums.*;
//import com.binance.client.model.trade.Order;
//import data.AccountBalance;
//import data.Config;
//import data.DataHolder;
//import positions.PositionHandler;
//import singletonHelpers.RequestClient;
//import singletonHelpers.TelegramMessenger;
//import strategies.EntryStrategy;
//import strategies.ExitStrategy;
//import strategies.MACrosses.MACrossesConstants;
//import strategies.MACrosses.MACrossesExitStrategy1;
//import utils.Utils;
//
//import java.util.ArrayList;
//import java.util.Date;
//
//public class StochRsiCrosseEntryStrategy implements EntryStrategy {
//    int smaCrosseIndex;
//    AccountBalance accountBalance;
//    private double requestedBuyingAmount = StochRsiCrosseConstance.DEFAULT_BUYING_AMOUNT;
//    PositionHandler positionHandler;
//    private int leverage = StochRsiCrosseConstance.LEVERAGE;
//
//    public StochRsiCrosseEntryStrategy (){
//        accountBalance = AccountBalance.getAccountBalance();
//        System.out.println("Stoch Rsi D & K Crosses");
//    }
//
//    @Override
//    public PositionHandler run(DataHolder realTimeData, String symbol) {
//        double currentPrice = realTimeData.getCurrentPrice();
//        if(positionHandler != null){
//            if(isLongZone(realTimeData)){
//                if(stochCrosse(realTimeData, DataHolder.CrossType.UP) && outOfBannedZone(realTimeData,PositionSide.LONG) && notToLate(realTimeData, PositionSide.LONG)){
//                    return  buyAndCreatePositionHandler(currentPrice, symbol, PositionSide.LONG);
//                }
//            } else
//                  if(stochCrosse(realTimeData, DataHolder.CrossType.DOWN) && outOfBannedZone(realTimeData, PositionSide.SHORT) && notToLate(realTimeData, PositionSide.SHORT)){
//                    return buyAndCreatePositionHandler(currentPrice, symbol, PositionSide.SHORT);
//            } else
//                if (nutralZone(realTimeData)){
//                    if(stochCrosse(realTimeData, DataHolder.CrossType.UP) && outOfBannedZone(realTimeData,PositionSide.LONG)) {
//                      return buyAndCreatePositionHandler(currentPrice, symbol, PositionSide.LONG);
//                    }
//                }
//                else
//                    if(stochCrosse(realTimeData, DataHolder.CrossType.DOWN) && outOfBannedZone(realTimeData, PositionSide.SHORT) && notToLate(realTimeData, PositionSide.SHORT)){
//                        return buyAndCreatePositionHandler(currentPrice, symbol, PositionSide.SHORT);
//                    }
//        }
//        return null;
//    }
//
//    private boolean outOfBannedZone(DataHolder realTimeData, PositionSide positionSide){
//        int index = realTimeData.getLastCloseIndex();
//        double kValue = realTimeData.getRsiAsKAtIndex(index);
//        double dValue = realTimeData.getDIndicatorAtIndex(index);
//        boolean inOverZone  = (kValue < StochRsiCrosseConstance.LOWER_LOWER_EXIT_BUNNE_ZONE && dValue < StochRsiCrosseConstance.LOWER_LOWER_EXIT_BUNNE_ZONE) ||
//                (kValue > StochRsiCrosseConstance.UPPER_UPPER_EXIT_BUNNE_ZONE && dValue > StochRsiCrosseConstance.UPPER_UPPER_EXIT_BUNNE_ZONE);;
//        switch(positionSide){
//
//            case SHORT:
//                boolean aboveMinValue = kValue > StochRsiCrosseConstance.SHORT_ENTRY_MIN_VAL && dValue > StochRsiCrosseConstance.SHORT_ENTRY_MIN_VAL;
//                boolean underUpperBunnedValue = kValue < StochRsiCrosseConstance.UPPER_UPPER_EXIT_BUNNE_ZONE && dValue < StochRsiCrosseConstance.UPPER_UPPER_EXIT_BUNNE_ZONE;
//                return (underUpperBunnedValue && aboveMinValue) || inOverZone;
//
//            case LONG:
//                boolean underMaxValue = kValue > StochRsiCrosseConstance.LONG_ENTRY_MAX_VAL && dValue > StochRsiCrosseConstance.LONG_ENTRY_MAX_VAL;
//                boolean aboveLoerBunnedValue = kValue < StochRsiCrosseConstance.LOWER_LOWER_EXIT_BUNNE_ZONE && dValue < StochRsiCrosseConstance.LOWER_LOWER_EXIT_BUNNE_ZONE;
//                return (aboveLoerBunnedValue && underMaxValue) || inOverZone;
//        }
//        return false;
//    }
//
//    public static boolean stochCrosse(DataHolder realTimeData, DataHolder.CrossType crossType){
//        int index = realTimeData.getLastCloseIndex();
//        double kValue = realTimeData.getRsiAsKAtIndex(index);
//        double dValue = realTimeData.getDIndicatorAtIndex(index);
//        double prevKValue = realTimeData.getRsiAsKAtIndex(index - 1);
//        double prevDValue = realTimeData.getDIndicatorAtIndex(index - 1);
//        switch (crossType){
//            case UP:
//                return prevKValue <= prevDValue && kValue > dValue;
//            case DOWN:
//                return prevKValue >= prevDValue && kValue < dValue;
//        }
//        return false;
//    }
//
//    private boolean notToLate(DataHolder realTimeData, PositionSide positionSide){
//        double currentPrice = realTimeData.getCurrentPrice();
//        double fastSmaCurrValue = realTimeData.getStoch_FastSmaValue(realTimeData.getLastCloseIndex());
//        switch(positionSide){
//            case LONG:
//                return currentPrice - (1.5 * fastSmaCurrValue / 100) < fastSmaCurrValue;
//            case SHORT:
//                return currentPrice + (1.5 * fastSmaCurrValue / 100) > fastSmaCurrValue;
//        }
//        return false;
//    }
//
//    private boolean isLongZone(DataHolder realTimeData){
//        int index = realTimeData.getLastCloseIndex();
//        double fastSma = realTimeData.getStoch_FastSmaValue(index);
//        double slowSma = realTimeData.getStoch_SlowSmaValue(index);
//        return fastSma > slowSma ;
//    }
//
//    private boolean nutralZone(DataHolder realTimeData){
//        int i = 0;
//        int index = realTimeData.getLastCloseIndex();
//        double fastSma = realTimeData.getStoch_FastSmaValue(index);
//        double slowSma = realTimeData.getStoch_SlowSmaValue(index);
//        double prevFastSma = realTimeData.getStoch_FastSmaValue(index - 1);
//        double prevSlowSma = realTimeData.getStoch_SlowSmaValue(index - 1);
//        while((prevFastSma < prevSlowSma && fastSma > slowSma) || (prevFastSma > prevSlowSma && fastSma < slowSma)) i++;
//        return i > 35;
//    }
//
//    private PositionHandler buyAndCreatePositionHandler(double currentPrice, String symbol, PositionSide positionSide) {
//        updateBuyingAmount(symbol);
//        TelegramMessenger.sendToTelegram("Entering new position " + new Date(System.currentTimeMillis()));
//        ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
//        SkippingExitTrailer trailer = new SkippingExitTrailer(MACrossesConstants.TRAILING_PERCENTAGE, positionSide);
//        exitStrategies.add(new fasrfMACrossesExitStrategy1(positionSide, trailer));
//        if (positionSide == PositionSide.LONG) {
//            try {
//                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//                syncRequestClient.changeInitialLeverage(symbol, leverage);
//                String buyingQty = Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
//                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
//                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE, "TRUE", NewOrderRespType.RESULT);
//                TelegramMessenger.sendToTelegram("bought long:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
//                        " , Average Price: " + buyOrder.getAvgPrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
//                positionHandler = new PositionHandler(buyOrder, exitStrategies, StochRsiCrosseConstance.STOP_LOSS_PERCENTAGE, positionSide);
//                return positionHandler;
//            } catch (Exception e) {
//                TelegramMessenger.sendToTelegram("Exception was thrown" + new Date(System.currentTimeMillis()));
//                e.printStackTrace();
//            }
//        }
//        else {
//            try {
//                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//                syncRequestClient.changeInitialLeverage(symbol, leverage);
//                String buyingQty = Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
//                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.MARKET, null,
//                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE,"TRUE" , NewOrderRespType.RESULT);
//                TelegramMessenger.sendToTelegram("bought short:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
//                        " , Average Price: " + buyOrder.getAvgPrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
//                positionHandler = new PositionHandler(buyOrder, exitStrategies, StochRsiCrosseConstance.STOP_LOSS_PERCENTAGE, positionSide);
//                return positionHandler;
//            } catch (Exception e) {
//                TelegramMessenger.sendToTelegram("Exception was thrown" + new Date(System.currentTimeMillis()));
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//
//    private void updateBuyingAmount(String symbol) {
//        String baseSymbol = Config.BASE_COIN;
//        double balance = accountBalance.getCoinBalance(baseSymbol).doubleValue();
//        requestedBuyingAmount = (balance * MACrossesConstants.AVAILABLE_BALANCE_PERCENTAGE) / 100;
//
//    }
//
//    @Override
//    public void setLeverage(int leverage) {
//
//    }
//
//    @Override
//    public void setRequestedBuyingAmount(double requestedBuyingAmount) {
//
//    }
//
//    @Override
//    public void positionClosed() {
//
//    }
//}
