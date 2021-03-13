package data;

import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;

import java.math.BigDecimal;

public class DataHolder {
    private double currentPrice;
    private RSIIndicator rsiIndicator;
    private MACDIndicator macdOverRsiIndicator;
    private ParabolicSarIndicator sarIndicator;
    private BollingerBandsUpperIndicator bollingerBandsUpperIndicator;
    private BollingerBandsLowerIndicator bollingerBandsLowerIndicator;
    private ClosePriceIndicator closePriceIndicator;
    private HighPriceIndicator highPriceIndicator;
    private LowPriceIndicator lowPriceIndicator;
    private double macdOverRsiCloseValue;
    private int endIndex;

    public DataHolder(HighPriceIndicator highPriceIndicator, LowPriceIndicator lowPriceIndicator, ClosePriceIndicator closePriceIndicator, RSIIndicator rsiIndicator, MACDIndicator macdOverRsiIndicator, BollingerBandsUpperIndicator bollingerBandsUpperIndicator,
                      BollingerBandsLowerIndicator bollingerBandsLowerIndicator, ParabolicSarIndicator sarIndicator, int endIndex) {
        this.rsiIndicator = rsiIndicator;
        this.macdOverRsiIndicator = macdOverRsiIndicator;
        this.sarIndicator = sarIndicator;
        this.endIndex = endIndex;
        this.macdOverRsiCloseValue = getMacdOverRsiValueAtIndex(endIndex-1);
        this.bollingerBandsUpperIndicator = bollingerBandsUpperIndicator;
        this.bollingerBandsLowerIndicator = bollingerBandsLowerIndicator;
        this.closePriceIndicator = closePriceIndicator;
        currentPrice = getClosePriceAtIndex(endIndex);
        this.highPriceIndicator = highPriceIndicator;
        this.lowPriceIndicator = lowPriceIndicator;
    }

    public double getClosePriceAtIndex(int index){return closePriceIndicator.getValue(index).doubleValue();}

    public double getHighPriceAtIndex(int index){return highPriceIndicator.getValue(index).doubleValue();}

    public double getLowPriceAtIndex(int index){return lowPriceIndicator.getValue(index).doubleValue();}


    public double getUpperBollingerAtIndex(int index){return bollingerBandsUpperIndicator.getValue(index).doubleValue();}

    public double getLowerBollingerAtIndex(int index){return bollingerBandsLowerIndicator.getValue(index).doubleValue();}

    public double getMacdOverRsiSignalLineValueAtIndex(int index) {
        EMAIndicator signal = new EMAIndicator(macdOverRsiIndicator, MACDOverRSIConstants.SIGNAL_LENGTH);
        return signal.getValue(index).doubleValue();
    }

    public double getMacdOverRsiMacdLineValueAtIndex(int index) {
        return macdOverRsiIndicator.getValue(index).doubleValue();
    }

    public double getMacdOverRsiValueAtIndex(int index) {
        return getMacdOverRsiMacdLineValueAtIndex(index) - getMacdOverRsiSignalLineValueAtIndex(index);
    }

    public double getMacdOverRsiCloseValue() {
        return macdOverRsiCloseValue;
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

    public  double getSarValueAtIndex(int index) {
        return sarIndicator.getValue(index).doubleValue();
    }


    public boolean crossed(IndicatorType indicatorType, CrossType crossType, CandleType candleType, double threshold) {
        switch (indicatorType) {
            case RSI:
                return rsiCrossed(crossType,candleType,threshold);
            case MACD_OVER_RSI:
                return macdOverRsiCrossed(crossType,candleType,threshold);
        }
        return true; // will not come to this!

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
        System.out.println("current macd: "  + currentMacdOverRsiValue);
        System.out.println("prev macd: "  + prevMacdOverRsiValue);
        if (crossType == CrossType.UP) return currentMacdOverRsiValue > threshold && prevMacdOverRsiValue <= threshold;
        return prevMacdOverRsiValue >= threshold && currentMacdOverRsiValue < threshold;
    }

    public boolean above(IndicatorType indicatorType, CandleType type, int threshold) {
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
        } else {
            if (type == CandleType.OPEN) {
                return getSarValueAtIndex(getLastIndex())>threshold;
            } else {
                return getSarValueAtIndex(getLastCloseIndex()) > threshold;
            }
        }
    }

    public synchronized double getCurrentPrice() { return currentPrice;}

    public int getLastIndex(){return endIndex;}

    public int getLastCloseIndex(){return endIndex-1;}

    public boolean candleType(CandleType type) {
        double curr = getClosePriceAtIndex(endIndex);
        double prev = getClosePriceAtIndex(endIndex);

        if (type == CandleType.BEARISH){
            return curr < prev;
        }
        else{
            return curr > prev;
        }
    }

    public enum CandleType {
        OPEN,CLOSE,BEARISH,BULLISH;
    }

    public enum CrossType {
        UP,DOWN
    }
    public enum IndicatorType {
        RSI,MACD_OVER_RSI, UpperBollinger, SAR, CLOSE_PRICE
    }
}
