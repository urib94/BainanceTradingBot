package data;

import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.BollingerBandWidthIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.bollinger.PercentBIndicator;
import org.ta4j.core.indicators.helpers.*;
import strategies.MACDOverCCIWIthATR.MACDOverCCIWIthATRConstants;
import strategies.MACDOverSMAStrategy.MACDOverSMAConstants;

public class DataHolder {
    private double currentPrice;
    private MACDIndicator macdOverRsiIndicator;
    private MACDIndicator macdOverCCIIndicator;
    private BollingerBandWidthIndicator closeBollingerBandWidthIndicator;
    private BollingerBandsUpperIndicator closeBollingerBandsUpperIndicator;
    private BollingerBandsLowerIndicator closeBollingerBandsLowerIndicator;
    private BollingerBandsUpperIndicator bollingerBandsUpperIndicator;
    private BollingerBandsLowerIndicator bollingerBandsLowerIndicator;
    private BollingerBandWidthIndicator bollingerBandWidthIndicator;
    private ATRIndicator atrIndicator;

    private PercentBIndicator percentBIndicator;
    private ClosePriceIndicator closePriceIndicator;
    private HighPriceIndicator highPriceIndicator;
    private LowPriceIndicator lowPriceIndicator;
    private OpenPriceIndicator openPriceIndicator;
    private int endIndex;
    private MACDIndicator macdOverMa9;
    private MACDIndicator macdOverMa14;
    private MACDIndicator macdOverMa50;

    private CCICIndicator ccicIndicator;
    private MFIIndicator mfiIndicator;
    private RSIIndicator rsiIndicator;

    private SMAIndicator slowSmaIndicator;
    private SMAIndicator fastSmaIndicator;
    private SMAIndicator slowSMAOverRsiIndicator;
    private SMAIndicator fastSMAOverRsiIndicator;

    private VolumeIndicator volumeIndicator;
    private SMAIndicator smaOverVolumeIndicator;


    public DataHolder(HighPriceIndicator highPriceIndicator, LowPriceIndicator lowPriceIndicator, ClosePriceIndicator closePriceIndicator, RSIIndicator rsiIndicator, MACDIndicator macdOverRsiIndicator, BollingerBandsUpperIndicator bollingerBandsUpperIndicator,
                      BollingerBandsLowerIndicator bollingerBandsLowerIndicator, SMAIndicator slowSmaIndicator, BollingerBandWidthIndicator bollingerbandWidthIndicator, PercentBIndicator percentBIndicator, int endIndex
            , MACDIndicator macdOverCCIIndicator, ATRIndicator atrIndicator, CCICIndicator ccicIndicator, MACDIndicator macdOverMa9, MACDIndicator macdOverMa14, MACDIndicator macdOverMa50, MFIIndicator mfiIndicator,
                      SMAIndicator fastSmaIndicator, SMAIndicator fastSMAOverRsiIndicator, SMAIndicator slowSMAOverRsiIndicator, OpenPriceIndicator openPriceIndicator, VolumeIndicator volumeIndicator, SMAIndicator smaOverVolumeIndicator,
                      BollingerBandWidthIndicator closeBollingerBandWidthIndicator, BollingerBandsUpperIndicator closeBollingerBandsUpperIndicator, BollingerBandsLowerIndicator closeBollingerBandsLowerIndicator) {
        this.rsiIndicator = rsiIndicator;
        this.mfiIndicator = mfiIndicator;
        this.fastSmaIndicator = fastSmaIndicator;
        this.fastSMAOverRsiIndicator = fastSMAOverRsiIndicator;
        this.slowSMAOverRsiIndicator = slowSMAOverRsiIndicator;
        this.slowSmaIndicator = slowSmaIndicator;
        this.macdOverRsiIndicator = macdOverRsiIndicator;
        this.endIndex = endIndex;
        this.closeBollingerBandWidthIndicator = closeBollingerBandWidthIndicator;
        this.closeBollingerBandsUpperIndicator = closeBollingerBandsUpperIndicator;
        this.closeBollingerBandsLowerIndicator = closeBollingerBandsLowerIndicator;
        this.bollingerBandsUpperIndicator = bollingerBandsUpperIndicator;
        this.bollingerBandsLowerIndicator = bollingerBandsLowerIndicator;
        this.bollingerBandWidthIndicator=bollingerbandWidthIndicator;
        this.percentBIndicator=percentBIndicator;
        this.closePriceIndicator = closePriceIndicator;
        this.highPriceIndicator = highPriceIndicator;
        this.lowPriceIndicator = lowPriceIndicator;
        this.macdOverCCIIndicator=macdOverCCIIndicator;
        this.atrIndicator =atrIndicator;
        this.ccicIndicator = ccicIndicator;
        this.macdOverMa9 = macdOverMa9;
        this.macdOverMa14 = macdOverMa14;
        this.macdOverMa50 = macdOverMa50;
        this.openPriceIndicator = openPriceIndicator;
        currentPrice = getClosePriceAtIndex(endIndex);
        this.volumeIndicator = volumeIndicator;
        this.smaOverVolumeIndicator = smaOverVolumeIndicator;

    }

    public double getVolumeAtIndex(int index) {
        return volumeIndicator.getValue(index).doubleValue();
    }

    public  double getSmaOverVolumeValueAtIndex(int index) {
        return smaOverVolumeIndicator.getValue(index).doubleValue();
    }
    public  double getSlowSmaValueAtIndex(int index) {
        return slowSmaIndicator.getValue(index).doubleValue();
    }

    public double getFastSmaValue(int index){
        return fastSmaIndicator.getValue(index).doubleValue();
    }
    public double getFastSmaOverRSIValue(int index){
        return fastSMAOverRsiIndicator.getValue(index).doubleValue();
    }
    public double getSlowSmaOverRSIValue(int index){
        return slowSMAOverRsiIndicator.getValue(index).doubleValue();
    }

    public double getCCICValue(int index){
        return ccicIndicator.getValue(index).doubleValue();
    }

    public double getMFIValue(int index){
        return mfiIndicator.getValue(index).doubleValue();
    }


    public double getRSIValueAtIndex(int index) {
        return rsiIndicator.getValue(index).doubleValue();
    }


    public double getClosePriceAtIndex(int index){return closePriceIndicator.getValue(index).doubleValue();}

    public double getHighPriceAtIndex(int index){return highPriceIndicator.getValue(index).doubleValue();}

    public double getLowPriceAtIndex(int index){return lowPriceIndicator.getValue(index).doubleValue();}

    public double getUpperBollingerAtIndex(int index){return bollingerBandsUpperIndicator.getValue(index).doubleValue();}

    public double getLowerBollingerAtIndex(int index){return bollingerBandsLowerIndicator.getValue(index).doubleValue();}

    public double getBandWidthAtIndex(int index){return bollingerBandWidthIndicator.getValue(index).doubleValue();}

    public double getCloseUpperBollingerAtIndex(int index){return closeBollingerBandsUpperIndicator.getValue(index).doubleValue();}

    public double getCloseLowerBollingerAtIndex(int index){return closeBollingerBandsLowerIndicator.getValue(index).doubleValue();}

    public double getCloseBandWidthAtIndex(int index){return closeBollingerBandWidthIndicator.getValue(index).doubleValue();}

    public double getPercentBIAtIndex(int index){return percentBIndicator.getValue(index).doubleValue();}

    public double getMACDOverSMAAverage(int index, int candleCount){
        if (candleCount == MACDOverSMAConstants.FAST_CANDLE_COUNT){
            return (macdOverMa9.getValue(index).doubleValue() + getMACDOverSMASignalLineValueAtIndex(index, candleCount))/2;
        }

        else if(candleCount == MACDOverSMAConstants.MEDIUM_CANDLE_COUNT){
            return (macdOverMa14.getValue(index).doubleValue() - getMACDOverSMASignalLineValueAtIndex(index, candleCount))/2;
        }
        else{
            return (macdOverMa50.getValue(index).doubleValue() - getMACDOverSMASignalLineValueAtIndex(index, candleCount))/2;
        }
    }

    public double getMACDOverSMASignalLineValueAtIndex(int index, int candleCount){
        EMAIndicator signal;
        if (candleCount == MACDOverSMAConstants.FAST_CANDLE_COUNT){
            signal = new EMAIndicator(macdOverMa9, MACDOverSMAConstants.SIGNAL_LENGTH);
        }

        else if(candleCount == MACDOverSMAConstants.MEDIUM_CANDLE_COUNT){
            signal = new EMAIndicator(macdOverMa14, MACDOverSMAConstants.SIGNAL_LENGTH);
        }
        else{
            signal = new EMAIndicator(macdOverMa50, MACDOverSMAConstants.SIGNAL_LENGTH);
        }
        return signal.getValue(index).doubleValue();
    }

    public double getMACDOverSMAHistAtIndex(int index, int candleCount){
        if (candleCount == MACDOverSMAConstants.FAST_CANDLE_COUNT){
            return macdOverMa9.getValue(index).doubleValue() - getMACDOverSMASignalLineValueAtIndex(index, candleCount);
        }

        else if(candleCount == MACDOverSMAConstants.MEDIUM_CANDLE_COUNT){
            return macdOverMa14.getValue(index).doubleValue() - getMACDOverSMASignalLineValueAtIndex(index, candleCount);
        }
        else{
            return macdOverMa50.getValue(index).doubleValue() - getMACDOverSMASignalLineValueAtIndex(index, candleCount);
        }
    }

    public double getMacdOverSMAMacdLineValueAtIndex(int index, int candleCount) {
        if (candleCount == MACDOverSMAConstants.FAST_CANDLE_COUNT){
            return macdOverMa9.getValue(index).doubleValue();
        }

        else if(candleCount == MACDOverSMAConstants.MEDIUM_CANDLE_COUNT){
            return macdOverMa14.getValue(index).doubleValue();
        }
        else{
            return macdOverMa50.getValue(index).doubleValue();
        }
    }

    public double getMACDOverCCISignalLineValueAtIndex(int index){
        EMAIndicator signal = new EMAIndicator(macdOverCCIIndicator, MACDOverCCIWIthATRConstants.SIGNAL_LENGTH);
        return signal.getValue(index).doubleValue();
    }

    public double getMACDOverCCIHistAtIndex(int index){
        return macdOverCCIIndicator.getValue(index).doubleValue()-getMACDOverCCISignalLineValueAtIndex(index);
    }

    public double getMacdOverCCIValueAtIndex(int index) {
        return getMacdOverCCIMacdLineValueAtIndex(index) - getMacdOverCCISignalLineValueAtIndex(index);
    }


    public double getPercentBIndicatorAtIndex(int index) {
        return percentBIndicator.getValue(index).doubleValue();
    }


    private double getMacdOverCCIMacdLineValueAtIndex(int index) {
        return macdOverCCIIndicator.getValue(index).doubleValue();
    }

    private double getMacdOverCCISignalLineValueAtIndex(int index) {
        EMAIndicator signal = new EMAIndicator(macdOverCCIIndicator, MACDOverCCIWIthATRConstants.SIGNAL_LENGTH);
        return signal.getValue(index).doubleValue();
    }
    public double getATRValueAtIndex(int index){
        return getAtrIndicator().getValue(index).doubleValue();
    }
    public double getRsiOpenValue() {
        return rsiIndicator.getValue(endIndex).doubleValue();
    }

    public double getRsiCloseValue() {
        return rsiIndicator.getValue(endIndex-1).doubleValue();
    }

    public BollingerBandsLowerIndicator getBollingerBandsLowerIndicator() {
        return bollingerBandsLowerIndicator;
    }
    public org.ta4j.core.indicators.ATRIndicator getAtrIndicator() {
        return atrIndicator;
    }

    public boolean crossed(IndicatorType indicatorType, CrossType crossType, CandleType candleType, double threshold) {
        switch (indicatorType) {
            case RSI:
                return rsiCrossed(crossType,candleType,threshold);
            case PERECENT_BI:
                return percentBICross(crossType,candleType,threshold);
            case MACD_OVER_CCI:
                return macdOverCCICrossed(crossType,candleType,threshold);

            case BOllINGER_BANDS_UPPER_INDICATOR:
                break;
            case BOllINGER_BANDS_LOWER_INDICATOR:
                break;
            case BOllINGER_BANDS_WIDTH_INDICATOR:
                break;
            case SAR:
                break;
            case CLOSE_PRICE:
                return closePriceCrossed(crossType,candleType,threshold);

        }

        return true; // will not come to this!

    }

//    private boolean upperBBCross(CrossType crossType, CandleType candleType, double threshold) {
//        double currBBValue = getUpperBollingerAtIndex(getLastCloseIndex());
//        double prevBBValue = getUpperBollingerAtIndex(getLastCloseIndex()-1);
//        double prevClosePrice = getClosePriceAtIndex(getLastCloseIndex()-1);
//        double currPrice = threshold;
//        switch (crossType){
//
//            case UP:
//                return (prevClosePrice <= prevBBValue) && currPrice > currBBValue;
//
//            case DOWN:
//                return (prevClosePrice >= prevBBValue) && currPrice < currBBValue;
//        }
//        return false;
//    }

    public boolean crossedAtIndex(IndicatorType indicatorType, CrossType crossType, CandleType candleType, double threshold,int index) {
        switch (indicatorType) {
            case RSI:
                return rsiCrossed(crossType,candleType,threshold);
            case PERECENT_BI:
                return percentBICross(crossType,candleType,threshold);
            case MACD_OVER_CCI:
                return macdOverCCICrossedAtIndex(crossType,candleType,threshold, index);

            case BOllINGER_BANDS_UPPER_INDICATOR:
                break;
            case BOllINGER_BANDS_LOWER_INDICATOR:
                break;
            case BOllINGER_BANDS_WIDTH_INDICATOR:
                break;
            case SAR:
                break;
            case CLOSE_PRICE:
                return closePriceCrossed(crossType,candleType,threshold);
        }

        return true; // will not come to this!

    }

    public boolean closePriceCrossed(CrossType crossType, CandleType candleType, double threshold) {
        double curr,prev;
        if (candleType == CandleType.OPEN) {
            curr = getClosePriceAtIndex(endIndex);
            prev = getClosePriceAtIndex(endIndex -1);
        }
        else {
            curr = getClosePriceAtIndex(endIndex -1);
            prev = getClosePriceAtIndex(endIndex -2);
        }
        if (crossType == CrossType.UP) return curr > threshold && prev <= threshold;
        return prev >= threshold && curr < threshold;
    }

    private boolean rsiCrossed(CrossType crossType, CandleType candleType, double threshold) {
        double rsiValueNow,rsiValuePrev;
        if (candleType == CandleType.OPEN) {
            rsiValueNow = getRsiOpenValue();
            rsiValuePrev = getRsiCloseValue();
        }
        else {
            rsiValueNow = getRsiCloseValue();
            rsiValuePrev = getRSIValueAtIndex(endIndex-2);
        }
        if (crossType == CrossType.UP) return rsiValueNow > threshold && rsiValuePrev <= threshold;
        return rsiValuePrev >= threshold && rsiValueNow < threshold;
    }

    private boolean macdOverCCICrossed(CrossType crossType, CandleType candleType, double threshold) {
        double currentMacdOverRsiValue,prevMacdOverRsiValue;
        if (candleType == CandleType.OPEN) {
            currentMacdOverRsiValue = getMacdOverCCIValueAtIndex(endIndex);
            prevMacdOverRsiValue  = getMacdOverCCIValueAtIndex(endIndex-1);
        } else {
            currentMacdOverRsiValue =getMacdOverCCIValueAtIndex(endIndex-1);
            prevMacdOverRsiValue = getMacdOverCCIValueAtIndex(endIndex-2);
        }
        if (crossType == CrossType.UP) return currentMacdOverRsiValue > threshold && prevMacdOverRsiValue <= threshold;
        return prevMacdOverRsiValue >= threshold && currentMacdOverRsiValue < threshold;
    }
    private boolean macdOverCCICrossedAtIndex(CrossType crossType, CandleType candleType, double threshold ,int index) {
        double currentMacdOverRsiValue,prevMacdOverRsiValue;
        if (candleType == CandleType.OPEN) {
            currentMacdOverRsiValue = getMacdOverCCIValueAtIndex(index);
            prevMacdOverRsiValue  = getMacdOverCCIValueAtIndex(index-1);
        } else {
            currentMacdOverRsiValue =getMacdOverCCIValueAtIndex(index-1);
            prevMacdOverRsiValue = getMacdOverCCIValueAtIndex(index-2);
        }
        if (crossType == CrossType.UP) return currentMacdOverRsiValue > threshold && prevMacdOverRsiValue <= threshold;
        return prevMacdOverRsiValue >= threshold && currentMacdOverRsiValue < threshold;
    }


    public boolean percentBICross(CrossType crossType, CandleType candleType, double threshold){
        double currentPercentBIValue, prevPercentBIValue;
        if (candleType == CandleType.OPEN) {
            currentPercentBIValue = getPercentBIndicatorAtIndex(endIndex);
            prevPercentBIValue = getPercentBIndicatorAtIndex(endIndex-1);
        } else {
            currentPercentBIValue =getPercentBIndicatorAtIndex(endIndex-1);
            prevPercentBIValue = getPercentBIndicatorAtIndex(endIndex-2);
        }
        if (crossType == CrossType.UP) return currentPercentBIValue > threshold && prevPercentBIValue <= threshold;
        return prevPercentBIValue >= threshold && currentPercentBIValue < threshold;
    }


    public boolean above(IndicatorType indicatorType, CandleType type, double threshold) {
        if (indicatorType == IndicatorType.RSI) {
            if (type == CandleType.OPEN) {
                return getRsiOpenValue() > threshold;
            } else {
                return getRsiCloseValue() > threshold;
            }
        }  else if(indicatorType == IndicatorType.MACD_OVER_CCI ){
            return getMACDOverCCIHistAtIndex(getLastCloseIndex()) > threshold;
        }
        else {
            if (type == CandleType.OPEN) {
                return getSlowSmaValueAtIndex(getLastIndex())>threshold;
            } else {
                return getSlowSmaValueAtIndex(getLastCloseIndex()) > threshold;
            }
        }
    }

    public synchronized double getCurrentPrice() { return currentPrice;}

    public int getLastIndex(){return endIndex;}

    public int getLastCloseIndex(){return endIndex-1;}

    public boolean candleType(CandleType type) {
        double curr = getClosePriceAtIndex(endIndex -1);
        double prev = getClosePriceAtIndex(endIndex -2);

        if (type == CandleType.BEARISH){
            return curr < prev;
        }
        else{
            return curr > prev;
        }
    }

    public BollingerBandWidthIndicator getBollingerBandWidthIndicator() {
        return bollingerBandWidthIndicator;
    }

    public void setBollingerBandWidthIndicator(BollingerBandWidthIndicator bollingerBandWidthIndicator) {
        this.bollingerBandWidthIndicator = bollingerBandWidthIndicator;
    }

    public boolean bandWidthIsShrinking(int index){
        return (getBandWidthAtIndex(index)<getBandWidthAtIndex(index-1));
    }

    public PercentBIndicator getPercentIndicator() {
        return percentBIndicator;
    }

    public void setPercentIndicator(PercentBIndicator percentIndicator) {
        this.percentBIndicator = percentIndicator;
    }

    public double getOpenPrice(int index) {
        return openPriceIndicator.getValue(index).doubleValue();
    }

    public enum CandleType {
        OPEN,CLOSE,BEARISH,BULLISH;
    }

    public enum CrossType {
        UP,DOWN
    }
    public enum IndicatorType {
        RSI,
        SMA,
        MFI,
        MACD_OVER_RSI,
        BOllINGER_BANDS_UPPER_INDICATOR,
        BOllINGER_BANDS_LOWER_INDICATOR,
        BOllINGER_BANDS_WIDTH_INDICATOR,
        PERECENT_BI,
        SAR,
        CLOSE_PRICE,
        MACD_OVER_CCI,
        ATR,
        MACD_OVER_CCI_HIST,
    }
}
