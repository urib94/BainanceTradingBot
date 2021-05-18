package data;

import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.BollingerBandWidthIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.bollinger.PercentBIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import strategies.MACDOverCCIWIthATR.MACDOverCCIWIthATRConstants;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;

public class DataHolder {
    private double currentPrice;
    private RSIIndicator rsiIndicator;
    private MACDIndicator macdOverRsiIndicator;
    private MACDIndicator macdOverCCIIndicator;
    private SMAIndicator smaIndicator;
    private BollingerBandsUpperIndicator bollingerBandsUpperIndicator;
    private BollingerBandsLowerIndicator bollingerBandsLowerIndicator;
    private BollingerBandWidthIndicator bollingerBandWidthIndicator;
    private ATRIndicator ATRIndicator;



    private PercentBIndicator percentBIndicator;
    private ClosePriceIndicator closePriceIndicator;
    private HighPriceIndicator highPriceIndicator;
    private LowPriceIndicator lowPriceIndicator;
    private double macdOverRsiCloseValue;
    private int endIndex;



    public DataHolder(HighPriceIndicator highPriceIndicator, LowPriceIndicator lowPriceIndicator, ClosePriceIndicator closePriceIndicator, RSIIndicator rsiIndicator, MACDIndicator macdOverRsiIndicator, BollingerBandsUpperIndicator bollingerBandsUpperIndicator,
                      BollingerBandsLowerIndicator bollingerBandsLowerIndicator, SMAIndicator smaIndicator, BollingerBandWidthIndicator bollingerbandWidthIndicator, PercentBIndicator percentBIndicator, int endIndex
                        , MACDIndicator macdOverCCIIndicator, ATRIndicator atrIndicator) {
        this.rsiIndicator = rsiIndicator;
        this.macdOverRsiIndicator = macdOverRsiIndicator;
        this.smaIndicator = smaIndicator;
        this.endIndex = endIndex;
        this.macdOverRsiCloseValue = getMacdOverRsiValueAtIndex(endIndex-1);
        this.bollingerBandsUpperIndicator = bollingerBandsUpperIndicator;
        this.bollingerBandsLowerIndicator = bollingerBandsLowerIndicator;
        this.bollingerBandWidthIndicator=bollingerbandWidthIndicator;
        this.percentBIndicator=percentBIndicator;
        this.closePriceIndicator = closePriceIndicator;
        currentPrice = getClosePriceAtIndex(endIndex);
        this.highPriceIndicator = highPriceIndicator;
        this.lowPriceIndicator = lowPriceIndicator;
        this.macdOverCCIIndicator=macdOverCCIIndicator;
        this.ATRIndicator=atrIndicator;
    }

    public double getClosePriceAtIndex(int index){return closePriceIndicator.getValue(index).doubleValue();}

    public double getHighPriceAtIndex(int index){return highPriceIndicator.getValue(index).doubleValue();}

    public double getLowPriceAtIndex(int index){return lowPriceIndicator.getValue(index).doubleValue();}


    public double getUpperBollingerAtIndex(int index){return bollingerBandsUpperIndicator.getValue(index).doubleValue();}

    public double getLowerBollingerAtIndex(int index){return bollingerBandsLowerIndicator.getValue(index).doubleValue();}

    public double getBandWidthAtIndex(int index){return bollingerBandWidthIndicator.getValue(index).doubleValue();}

    public double getPercentBIAtIndex(int index){return percentBIndicator.getValue(index).doubleValue();}

    public double getMacdOverRsiSignalLineValueAtIndex(int index) {
        EMAIndicator signal = new EMAIndicator(macdOverRsiIndicator, MACDOverRSIConstants.SIGNAL_LENGTH);
        return signal.getValue(index).doubleValue();
    }

    public double getMACDOverCCISignalLineValueAtIndex(int index){
        EMAIndicator signal = new EMAIndicator(macdOverCCIIndicator, MACDOverRSIConstants.SIGNAL_LENGTH);
        return signal.getValue(index).doubleValue();
    }

    public double getMACDOverCCIHistAtIndex(int index){
        return macdOverCCIIndicator.getValue(index).doubleValue()-getMACDOverCCISignalLineValueAtIndex(index);
    }

    public double getMacdOverRsiMacdLineValueAtIndex(int index) {
        return macdOverRsiIndicator.getValue(index).doubleValue();
    }

    public double getPercentBIndicatorAtIndex(int index) {
        return percentBIndicator.getValue(index).doubleValue();
    }

    public double getMacdOverRsiValueAtIndex(int index) {
        return getMacdOverRsiMacdLineValueAtIndex(index) - getMacdOverRsiSignalLineValueAtIndex(index);
    }

    public double getMacdOverCCIValueAtIndex(int index) {
        return getMacdOverCCIMacdLineValueAtIndex(index) - getMacdOverCCISignalLineValueAtIndex(index);
    }

    private double getMacdOverCCIMacdLineValueAtIndex(int index) {
        return macdOverCCIIndicator.getValue(index).doubleValue();
    }


    private double getMacdOverCCISignalLineValueAtIndex(int index) {
        EMAIndicator signal = new EMAIndicator(macdOverCCIIndicator, MACDOverCCIWIthATRConstants.SIGNAL_LENGTH);
        return signal.getValue(index).doubleValue();
    }
    public double getMacdOverRsiCloseValue() {
        return macdOverRsiCloseValue;
    }

    public double getATRValueAtIndex(int index){
        return getATRIndicator().getValue(index).doubleValue();
    }
    public double getRsiOpenValue() {
        return rsiIndicator.getValue(endIndex).doubleValue();
    }

    public double getRsiCloseValue() {
        return rsiIndicator.getValue(endIndex-1).doubleValue();
    }

    public double getRSIValueAtIndex(int index) {
        return rsiIndicator.getValue(index).doubleValue();
    }

    public  double getSmaValueAtIndex(int index) {
        return smaIndicator.getValue(index).doubleValue();
    }

    public BollingerBandsLowerIndicator getBollingerBandsLowerIndicator() {
        return bollingerBandsLowerIndicator;
    }
    public org.ta4j.core.indicators.ATRIndicator getATRIndicator() {
        return ATRIndicator;
    }

    public boolean crossed(IndicatorType indicatorType, CrossType crossType, CandleType candleType, double threshold) {
        switch (indicatorType) {
            case RSI:
                return rsiCrossed(crossType,candleType,threshold);
            case PERECENT_BI:
                return percentBICross(crossType,candleType,threshold);
            case MACD_OVER_RSI:
                return macdOverRsiCrossed(crossType,candleType,threshold);
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

    public boolean crossedAtIndex(IndicatorType indicatorType, CrossType crossType, CandleType candleType, double threshold,int index) {
        switch (indicatorType) {
            case RSI:
                return rsiCrossed(crossType,candleType,threshold);
            case PERECENT_BI:
                return percentBICross(crossType,candleType,threshold);
            case MACD_OVER_RSI:
                return macdOverRsiCrossed(crossType,candleType,threshold);
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

    private boolean closePriceCrossed(CrossType crossType, CandleType candleType, double threshold) {
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
    private boolean macdOverRsiCrossed(CrossType crossType, CandleType candleType, double threshold) {
        double currentMacdOverRsiValue,prevMacdOverRsiValue;
        if (candleType == CandleType.OPEN) {
            currentMacdOverRsiValue = getMacdOverRsiValueAtIndex(endIndex);
            prevMacdOverRsiValue  = macdOverRsiCloseValue;
        } else {
            currentMacdOverRsiValue = macdOverRsiCloseValue;
            prevMacdOverRsiValue = getMacdOverRsiValueAtIndex(endIndex - 2);
        }
        if (crossType == CrossType.UP) return currentMacdOverRsiValue > threshold && prevMacdOverRsiValue <= threshold;
        return prevMacdOverRsiValue >= threshold && currentMacdOverRsiValue < threshold;
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
        } else if(indicatorType == IndicatorType.MACD_OVER_RSI) {
            if (type == CandleType.OPEN) {
                return getMacdOverRsiValueAtIndex(getLastIndex()) > threshold;
            } else {
                return  getMacdOverRsiValueAtIndex(getLastCloseIndex()) > threshold;
            }
        }  else if(indicatorType == IndicatorType.MACD_OVER_CCI ){
            return getMACDOverCCIHistAtIndex(getLastCloseIndex()) > threshold;
        }
        else {
            if (type == CandleType.OPEN) {
                return getSmaValueAtIndex(getLastIndex())>threshold;
            } else {
                return getSmaValueAtIndex(getLastCloseIndex()) > threshold;
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

    public enum CandleType {
        OPEN,CLOSE,BEARISH,BULLISH;
    }

    public enum CrossType {
        UP,DOWN
    }
    public enum IndicatorType {
        RSI,
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
