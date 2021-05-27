package strategies.MACDOverSMAStrategy;

import TradingTools.DCA.BaseDCA;
import com.binance.client.model.enums.PositionSide;
import data.AccountBalance;
import data.DataHolder;
import positions.PositionHandler;
import strategies.DCAStrategy;
import strategies.EntryStrategy;
import strategies.MACDOverCCIWIthATR.MACDOverCCIWIthATRConstants;

public class MACDOverSMAEntryStrategy implements EntryStrategy {

    private int leverage = MACDOverSMAConstants.DEFAULT_LEVERAGE;
    private double requestedBuyingAmount = MACDOverSMAConstants.DEFAULT_BUYING_AMOUNT;
    private final AccountBalance accountBalance;
    private PositionHandler positionHandler;
    private boolean isSlowTriggeredLong = false;
    private boolean isMediumTriggeredLong = false;
    private boolean isFastTriggeredLong = false;
    private boolean isSlowTriggeredShort = false;
    private boolean isMediumTriggeredShort = false;
    private boolean isFastTriggeredShort = false;
    private Zone fastZone = Zone.NONE;
    private Zone mediumZone = Zone.NONE;
    private Zone slowZone = Zone.NONE;
    public MACDOverSMAEntryStrategy(){
        accountBalance = AccountBalance.getAccountBalance();
        System.out.println("macd over sma");
    }

    @Override
    public PositionHandler run(DataHolder realTimeData, String symbol) {
        int closeIndex = realTimeData.getLastCloseIndex();
        boolean slowIndicateLong = candleIndicateLong(realTimeData, MACDOverSMAConstants.SLOW_CANDLE_COUNT, closeIndex);
        boolean mediumIndicateLong = candleIndicateLong(realTimeData, MACDOverSMAConstants.MEDIUM_CANDLE_COUNT, closeIndex);
        boolean fastIndicateLong = candleIndicateLong(realTimeData, MACDOverSMAConstants.FAST_CANDLE_COUNT, closeIndex);
        boolean slowIndicateShort = candleIndicateShort(realTimeData, MACDOverSMAConstants.SLOW_CANDLE_COUNT, closeIndex);
        boolean mediumIndicateShort = candleIndicateShort(realTimeData, MACDOverSMAConstants.MEDIUM_CANDLE_COUNT, closeIndex);
        boolean fastIndicateShort = candleIndicateShort(realTimeData, MACDOverSMAConstants.FAST_CANDLE_COUNT, closeIndex);
        updateTriggers(realTimeData, slowIndicateLong, mediumIndicateLong, fastIndicateLong, slowIndicateShort, mediumIndicateShort, fastIndicateShort, closeIndex);
        if (positionHandler == null && (fastZone != Zone.NONE && mediumZone != Zone.NONE && slowZone != Zone.NONE)){
            double currentPrice = realTimeData.getCurrentPrice();
            boolean slowAverageLineAboveZero = realTimeData.getMACDOverSMAAverage(closeIndex, MACDOverSMAConstants.SLOW_CANDLE_COUNT) > 0;
            boolean mediumAverageLineAboveZero = realTimeData.getMACDOverSMAAverage(closeIndex, MACDOverSMAConstants.MEDIUM_CANDLE_COUNT) > 0;
            boolean fastAverageLineAboveZero = realTimeData.getMACDOverSMAAverage(closeIndex, MACDOverSMAConstants.FAST_CANDLE_COUNT) > 0;

            if ((slowAverageLineAboveZero && mediumAverageLineAboveZero && fastAverageLineAboveZero) && (slowZone == Zone.LONG || mediumZone == Zone.LONG || fastZone == Zone.LONG)){
                return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG, true);
            }

            if ((mediumAverageLineAboveZero && fastAverageLineAboveZero) && (mediumZone == Zone.LONG || fastZone == Zone.LONG)){
                return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG, false);
            }

            if (slowAverageLineAboveZero && (!mediumAverageLineAboveZero || !fastAverageLineAboveZero) && (!isMediumTriggeredLong || !isFastTriggeredLong)) {
                return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG, false);
            }

            if (!slowAverageLineAboveZero && ((!mediumAverageLineAboveZero && !isMediumTriggeredLong) || ( !fastAverageLineAboveZero && !isFastTriggeredLong))){
                return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG, false);
            }

            if ((slowAverageLineAboveZero && fastAverageLineAboveZero && mediumAverageLineAboveZero) && (slowZone == Zone.SHORT || mediumZone == Zone.SHORT || fastZone == Zone.SHORT)){
                return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, true);
            }

            if (!fastAverageLineAboveZero && !mediumAverageLineAboveZero && (mediumZone == Zone.SHORT || fastZone == Zone.SHORT)){
                return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, false);
            }

            if (!slowAverageLineAboveZero && (fastAverageLineAboveZero || mediumAverageLineAboveZero) && (!isMediumTriggeredShort && isFastTriggeredShort)){
                return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, false);
            }

            if (slowAverageLineAboveZero && (fastAverageLineAboveZero || mediumAverageLineAboveZero) && (!isFastTriggeredShort && ! isMediumTriggeredShort)){
                return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, false);//TODO: use stoploss?
            }
        }
        return null;
    }

    private void updateTriggers(DataHolder realTimeData, boolean slowIndicateLong, boolean mediumIndicateLong, boolean fastIndicateLong,
                                boolean slowIndicateShort, boolean mediumIndicateShort, boolean fastIndicateShort, int index) { //TODO: change zones
        if (slowIndicateLong){
            isSlowTriggeredLong = true;
            slowZone = Zone.LONG; }
        else if (slowIndicateShort){
            isSlowTriggeredShort = true;
            slowZone = Zone.SHORT;
        }
        else{
            if (smaAverageCrossedZero(realTimeData, MACDOverSMAConstants.SLOW_CANDLE_COUNT, index)){
                isSlowTriggeredLong = false;
                isSlowTriggeredShort = false;
            }
        }
        if (mediumIndicateLong){
            isMediumTriggeredLong = true;
            mediumZone = Zone.LONG;
        }
        else if (mediumIndicateShort){
            isMediumTriggeredShort = true;
            mediumZone = Zone.SHORT;
        }
        else{
            if (smaAverageCrossedZero(realTimeData, MACDOverSMAConstants.MEDIUM_CANDLE_COUNT, index)){
                isMediumTriggeredLong = false;
                isMediumTriggeredShort = false;
            }
        }
        if (fastIndicateLong){
            isFastTriggeredLong = true;
            fastZone = Zone.LONG;
        }
        else if (fastIndicateShort){
            isFastTriggeredShort = true;
            fastZone = Zone.SHORT;
        }
        else{
            if (smaAverageCrossedZero(realTimeData, MACDOverSMAConstants.FAST_CANDLE_COUNT, index)){
                isFastTriggeredLong = false;
                isFastTriggeredShort = false;
            }
        }
    }

    private boolean smaAverageCrossedZero(DataHolder realTimeData, int candleCount, int index) {
        double prev = realTimeData.getMACDOverSMAAverage(index - 1 , candleCount);
        double curr = realTimeData.getMACDOverSMAAverage(index, candleCount);
        return (prev >= 0 && curr <= 0) || (prev <= 0 && curr >= 0);
    }

    private PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, double currentPrice, String symbol, PositionSide positionSide, boolean addDCA) {
        return null;
    }

    private boolean candleIndicateLong(DataHolder realTimeData, int candleCount, int index){
        double currentMacdOverSMAValue = realTimeData.getMACDOverSMAHistAtIndex(index, candleCount);
        double prevMacdOverSMAValue = realTimeData.getMACDOverSMAHistAtIndex(index - 1, candleCount);
        return currentMacdOverSMAValue > prevMacdOverSMAValue;
    }

    private boolean candleIndicateShort(DataHolder realTimeData, int candleCount, int index){
        double currentMacdOverSMAValue = realTimeData.getMACDOverSMAHistAtIndex(index, candleCount);
        double prevMacdOverSMAValue = realTimeData.getMACDOverSMAHistAtIndex(index - 1, candleCount);
        return currentMacdOverSMAValue < prevMacdOverSMAValue;
    }

    private DCAStrategy createBaseDCA(DataHolder realTimeData, double currentPrice, String symbol, PositionSide positionSide) {
        double atrValue = realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex());
        switch (positionSide){
            case SHORT:
                double shortTPPrice = currentPrice - (atrValue * MACDOverCCIWIthATRConstants.ATR1);
                double shortStep = (currentPrice - shortTPPrice) / (currentPrice / 100);
                return new BaseDCA(currentPrice, MACDOverCCIWIthATRConstants.MAX_DCA, MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, positionSide, shortTPPrice, symbol,
                        shortStep, MACDOverCCIWIthATRConstants.STEP_FACTOR, DCAStrategy.DCAType.SHORT_DCA_LIMIT);
            case LONG:
                double longTPPrice = currentPrice + (atrValue * MACDOverCCIWIthATRConstants.ATR1);
                double longStep = (longTPPrice - currentPrice) / (currentPrice / 100);
                return new BaseDCA(currentPrice, MACDOverCCIWIthATRConstants.MAX_DCA, MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, positionSide, longTPPrice, symbol,
                        longStep, MACDOverCCIWIthATRConstants.STEP_FACTOR, DCAStrategy.DCAType.LONG_DCA_LIMIT);
            default:
                break;
        }
        return null;
    }

    @Override
    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    @Override
    public void setRequestedBuyingAmount(double requestedBuyingAmount) {
        this.requestedBuyingAmount = requestedBuyingAmount;
    }

    @Override
    public void positionClosed() {
        positionHandler = null;
    }

    enum Zone{
        SHORT,
        LONG,
        NONE
    }
}
