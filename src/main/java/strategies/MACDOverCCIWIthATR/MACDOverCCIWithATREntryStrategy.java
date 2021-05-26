package strategies.MACDOverCCIWIthATR;

import TradingTools.DCA.BaseDCA;
import TradingTools.Trailers.SkippingEntryTrailer;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import data.AccountBalance;
import data.Config;
import data.DataHolder;
import positions.PositionHandler;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.DCAStrategy;
import strategies.EntryStrategy;
import strategies.ExitStrategy;
import strategies.MACDOverCCIWIthATR.Long.MACDOverCCIWIthATRLongExitStrategy1;
import strategies.MACDOverCCIWIthATR.Long.MACDOverCCIWIthATRLongExitStrategy2;

import java.util.ArrayList;
import java.util.Date;

public class MACDOverCCIWithATREntryStrategy implements EntryStrategy {

    private double takeProfitPercentage = MACDOverCCIWIthATRConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
    private double stopLossPercentage = MACDOverCCIWIthATRConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
    private int leverage = MACDOverCCIWIthATRConstants.DEFAULT_LEVERAGE;

    private final AccountBalance accountBalance;
    private double positivePeek = 0;
    private double negativePeek = 0;
    private SkippingEntryTrailer skippingEntryTrailer;
    private double DCAPrices;
    private double TPPrice;
    private boolean entering = false;
    private PositionHandler positionHandler;
    private IndicatorSign macdOverCCIPrevSign = IndicatorSign.NEUTRAL;

    public MACDOverCCIWithATREntryStrategy(){
        accountBalance = AccountBalance.getAccountBalance();
        System.out.println("macd over cci");
    }

    public synchronized PositionHandler run(DataHolder realTimeData, String symbol) {
        if (macdOverCCIPrevSign == IndicatorSign.NEUTRAL) initializePeeks(realTimeData);
        updatePeeks(realTimeData.getMACDOverCCIHistAtIndex(realTimeData.getLastCloseIndex()));
        if (positionHandler == null){
            double currentPrice = realTimeData.getCurrentPrice();
            boolean positivePeekLargerthanNegative = positivePeek > Math.abs(negativePeek);
            double cciValue = realTimeData.getCCICIndciator(realTimeData.getLastCloseIndex());
            switch (macdOverCCIPrevSign){
                case NEGATIVE:
                    if(positivePeekLargerthanNegative && cciValue <= 100 && candleIndicateLong(realTimeData, realTimeData.getLastCloseIndex())){
                        if (!bbiExpanding(realTimeData)){
                            return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG, MACDOverCCIWIthATRConstants.STRONG_TRADE_MULTIPLIER);
                        }
                        return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG, MACDOverCCIWIthATRConstants.WEAK_TRADE_MULTIPLIER);
                    }
//                    if (!positivePeekLargerthanNegative && candleIndicateShort(realTimeData, realTimeData.getLastCloseIndex())){
//                        if (bbiExpanding(realTimeData)){
//                            return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, MACDOverCCIWIthATRConstants.STRONG_TRADE_MULTIPLIER);
//                        }
//                        return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, MACDOverCCIWIthATRConstants.WEAK_TRADE_MULTIPLIER);
//                    }
                    break;

                case POSITIVE:
//                    if(positivePeekLargerthanNegative && candleIndicateLong(realTimeData, realTimeData.getLastCloseIndex())){
//                        if (bbiExpanding(realTimeData)){
//                            return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG, MACDOverCCIWIthATRConstants.STRONG_TRADE_MULTIPLIER);
//                        }
//                        return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG, MACDOverCCIWIthATRConstants.WEAK_TRADE_MULTIPLIER);
//                    }
                    if (!positivePeekLargerthanNegative && cciValue >= -100 && candleIndicateShort(realTimeData, realTimeData.getLastCloseIndex())){
                        if (!bbiExpanding(realTimeData)){
                            return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, MACDOverCCIWIthATRConstants.STRONG_TRADE_MULTIPLIER);
                        }
                        return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, MACDOverCCIWIthATRConstants.WEAK_TRADE_MULTIPLIER);
                    }
                    break;
                default:
                    break;
            }
        }
        return null;
    }

    private void initializePeeks(DataHolder realTimeData) {
        int i = 0, numCrossed = 0, index = realTimeData.getLastCloseIndex();
        double macdOverCCI;
        while (numCrossed < 2) {
            macdOverCCI = realTimeData.getMACDOverCCIHistAtIndex(index);
            if (negativePeek > macdOverCCI) negativePeek = macdOverCCI;
            if (positivePeek < macdOverCCI) positivePeek = macdOverCCI;
            if (macdOverCCICrossedZero(realTimeData, index)) numCrossed++;
            index--;
        }
    }

    private void updatePeeks(double macdOverCCI) {
        if (macdOverCCI <=0 ){
            if (negativePeek > macdOverCCI || macdOverCCIPrevSign == IndicatorSign.POSITIVE) negativePeek = macdOverCCI;
            macdOverCCIPrevSign = IndicatorSign.NEGATIVE;
        }
        else{
            if (positivePeek < macdOverCCI || macdOverCCIPrevSign == IndicatorSign.NEGATIVE) positivePeek = macdOverCCI;
            macdOverCCIPrevSign = IndicatorSign.POSITIVE;
        }
    }

    private PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, double currentPrice, String symbol, PositionSide positionSide, double multiplier ) {//TODO: maybe change market later.
        updateBuyingAmount(symbol);
        double requestedBuyingAmount = MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT;
        TelegramMessenger.sendToTelegram("Entering new position " + new Date(System.currentTimeMillis()));
        ArrayList <DCAStrategy> DCAStrategies = new ArrayList<>();
        ArrayList <ExitStrategy> exitStrategies = new ArrayList<>();
//        updateBuyingAmount(symbol);
        if (positionSide == PositionSide.LONG) {
            try {
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol, leverage);
                String buyingQty = utils.Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT * multiplier);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE, "TRUE", NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought long:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Activate Price: " + buyOrder.getActivatePrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
                exitStrategies.add(new MACDOverCCIWIthATRLongExitStrategy1());
                exitStrategies.add(new MACDOverCCIWIthATRLongExitStrategy2());
                DCAStrategies.add(createBaseDCA(realTimeData, currentPrice, symbol, positionSide));
                positionHandler = new PositionHandler(buyOrder, exitStrategies, DCAStrategies);
                return positionHandler;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol, leverage);
                String buyingQty = utils.Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.MARKET, null,
                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE,"TRUE" , NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought long:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Activate Price: " + buyOrder.getActivatePrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
                exitStrategies.add(new MACDOverCCIWIthATRLongExitStrategy1());
                exitStrategies.add(new MACDOverCCIWIthATRLongExitStrategy2());
                DCAStrategies.add(createBaseDCA(realTimeData, currentPrice, symbol, positionSide));
                positionHandler = new PositionHandler(buyOrder, exitStrategies, DCAStrategies);
                return positionHandler;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private void updateBuyingAmount(String symbol) {
        String baseSymbol = Config.BASE_COIN;
        double balance = accountBalance.getCoinBalance(baseSymbol).doubleValue();
        double maxAmount = 1;
        for (int i = 0 ; i < MACDOverCCIWIthATRConstants.MAX_DCA ; i++){
            maxAmount += 2 * maxAmount;
        }
        MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT = (balance / maxAmount) * (MACDOverCCIWIthATRConstants.AVAILABLE_BALANCE_PRECENTAGE / 100);

    }

    private DCAStrategy createBaseDCA(DataHolder realTimeData,double currentPrice, String symbol, PositionSide positionSide) {
        double atrValue = realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex());
        switch (positionSide){
            case SHORT:
                double shortTPPrice = currentPrice - (atrValue * MACDOverCCIWIthATRConstants.ATR1);
                double shortStep = (currentPrice - shortTPPrice) / (currentPrice / 100);
                return new BaseDCA(currentPrice, MACDOverCCIWIthATRConstants.MAX_DCA, MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, positionSide, shortTPPrice, symbol,
                        shortStep, MACDOverCCIWIthATRConstants.STEP_FACTOR, DCAStrategy.DCAType.SHORT_DCA_MARKET);
            case LONG:
                double longTPPrice = currentPrice + (atrValue * MACDOverCCIWIthATRConstants.ATR1);
                double longStep = (longTPPrice - currentPrice) / (currentPrice / 100);
                return new BaseDCA(currentPrice, MACDOverCCIWIthATRConstants.MAX_DCA, MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, positionSide, longTPPrice, symbol,
                        longStep, MACDOverCCIWIthATRConstants.STEP_FACTOR, DCAStrategy.DCAType.LONG_DCA_MARKET);
            default:
                break;
        }
        return null;
    }

    private boolean macdOverCCICrossedZero(DataHolder dataHolder, int currIndex){
        return dataHolder.crossedAtIndex(DataHolder.IndicatorType.MACD_OVER_CCI, DataHolder.CrossType.UP,DataHolder.CandleType.CLOSE, Config.DOUBLE_ZERO ,currIndex) ||
                dataHolder.crossedAtIndex(DataHolder.IndicatorType.MACD_OVER_CCI, DataHolder.CrossType.DOWN,DataHolder.CandleType.CLOSE, Config.DOUBLE_ZERO ,currIndex);
    }

    private boolean longCurrHightIsBigger(DataHolder realTimeData){
        int i = 0, lastIndex = realTimeData.getLastCloseIndex();
        double prevHight = 0, macdVal;
        while (!MACDOverZeroCroosDown(realTimeData,lastIndex-i++)){
            if(Math.abs(prevHight)<Math.abs((macdVal = realTimeData.getMACDOverCCIHistAtIndex(lastIndex-i)))){
                prevHight = macdVal;
            }
        }
        return prevHight<Math.abs(realTimeData.getMACDOverCCIHistAtIndex(lastIndex));
    }

    private boolean ShortCurrHightIsBigger(DataHolder realTimeData){
        int i = 0, lastIndex = realTimeData.getLastCloseIndex();
        double prevHight = 0, macdVal;
        while (!macdOverCCICrossedZero(realTimeData,lastIndex-i++)){
            if(Math.abs(prevHight)<Math.abs((macdVal = realTimeData.getMACDOverCCIHistAtIndex(lastIndex-i)))){
                prevHight = macdVal;
            }
        }
        return prevHight<Math.abs(realTimeData.getMACDOverCCIHistAtIndex(lastIndex));
    }
    private boolean MACDOverZeroCroosDown(DataHolder dataHolder, int index){
        return dataHolder.crossedAtIndex(DataHolder.IndicatorType.MACD_OVER_CCI, DataHolder.CrossType.DOWN,DataHolder.CandleType.CLOSE, Config.DOUBLE_ZERO, index );
    }

    private boolean lowerBICroosUp(DataHolder dataHolder){
        return dataHolder.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE,MACDOverCCIWIthATRConstants.LOWER_BI);
    }

    private boolean upperBICroosDown(DataHolder dataHolder){

        return dataHolder.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE,MACDOverCCIWIthATRConstants.UPPER_BI);
    }

    private boolean upperBICroosUp(DataHolder dataHolder){
        return dataHolder.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE,MACDOverCCIWIthATRConstants.UPPER_BI);
    }

    private boolean lowerBICroosDown(DataHolder dataHolder){
        return dataHolder.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE,MACDOverCCIWIthATRConstants.LOWER_BI);
    }

    private boolean candleIndicateLong(DataHolder realTimeData, int index){
        double currentMacdOverCCIValue = realTimeData.getMACDOverCCIHistAtIndex(index);
        double prevMacdOverCCIValue = realTimeData.getMACDOverCCIHistAtIndex(index - 1);
        double prevPrevMacdOverCCIValue = realTimeData.getMacdOverCCIValueAtIndex(realTimeData.getLastCloseIndex() - 2);
        return currentMacdOverCCIValue > prevMacdOverCCIValue && prevMacdOverCCIValue <= prevPrevMacdOverCCIValue;
    }

    private boolean candleIndicateShort(DataHolder realTimeData,int index){
        double currentMacdOverCCIValue = realTimeData.getMACDOverCCIHistAtIndex(index);
        double prevMacdOverCCIValue = realTimeData.getMACDOverCCIHistAtIndex(index - 1);
        double prevPrevMacdOverCCIValue = realTimeData.getMacdOverCCIValueAtIndex(realTimeData.getLastCloseIndex() - 2);
        return currentMacdOverCCIValue < prevMacdOverCCIValue && prevMacdOverCCIValue >= prevPrevMacdOverCCIValue;
    }

    private boolean bbiExpanding(DataHolder realTimeData){
        double prevWidth = realTimeData.getBandWidthAtIndex(realTimeData.getLastCloseIndex() - 1) - realTimeData.getBandWidthAtIndex(realTimeData.getLastCloseIndex() - 2);
        double currWidth = realTimeData.getBandWidthAtIndex(realTimeData.getLastCloseIndex()) - realTimeData.getBandWidthAtIndex(realTimeData.getLastCloseIndex() - 1);
        return currWidth > prevWidth;
    }

    @Override
    public void setTakeProfitPercentage(double takeProfitPercentage) {

    }

    @Override
    public void setStopLossPercentage(double stopLossPercentage) {

    }

    @Override
    public void setLeverage(int leverage) {

    }

    @Override
    public void setRequestedBuyingAmount(double requestedBuyingAmount) {

    }

    @Override
    public void positionClosed() {
        positionHandler = null;
    }

    enum IndicatorSign{
        NEGATIVE,
        POSITIVE,
        NEUTRAL
    }


}