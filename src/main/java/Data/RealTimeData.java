package Data;

import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import Strategies.RSIStrategies.RSIConstants;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;
import com.binance.client.api.model.event.CandlestickEvent;
import com.binance.client.api.model.market.Candlestick;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import SingletonHelpers.RequestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

//* For us, in realTimeData, the last candle is always open. The previous ones are closed.
public class RealTimeData{

    private Long lastCandleOpenTime = 0L;
    private BaseBarSeries realTimeData;
    private BigDecimal currentPrice;
    private RSIIndicator rsiIndicator;
    private MACDIndicator macdOverRsiIndicator;
    private SMAIndicator smaIndicator;





    public RealTimeData(String symbol, CandlestickInterval interval){
        realTimeData = new BaseBarSeries();
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        List<Candlestick> candlestickBars = syncRequestClient.getCandlestick(symbol, interval, null, null, Config.CANDLE_NUM);
        lastCandleOpenTime = candlestickBars.get(candlestickBars.size() - 1).getOpenTime();
        currentPrice = candlestickBars.get(candlestickBars.size() -1).getClose();
        fillRealTimeData(candlestickBars);
        calculateIndicators();
    }

    /**
     * Receives the current candlestick - usually an open one.
     * The function updateData updates realTimeData in the following way: if the candle received is closed => push to the end
     * of realTimeData and erase the first. If the candle is open - delete the last one from realtimedata and push the new one.
     * Calculates the RSIIndicators in either case - to get the most accurate data.
     * to realTimeData
     * @param event - the new Candlestick received from the subscribeCandleStickEvent.
     */
    public void updateData(CandlestickEvent event){
        updateLastCandle(event);
        calculateIndicators();
//        System.out.println("macd: " + getMacdOverRsiMacdLineValueAtIndex(getLastIndex()) + " time: " + System.currentTimeMillis());
//        System.out.println("signal: " + getMacdOverRsiSignalLineValueAtIndex(getLastIndex()) + " time: " + System.currentTimeMillis());
//        System.out.println("hist: " + getMacdOverRsiValueAtIndex(getLastIndex()) + " time: " + System.currentTimeMillis());
    }

    private void updateLastCandle(CandlestickEvent event) {
        currentPrice = event.getClose();
        boolean isNewCandle = !(event.getStartTime().doubleValue() == lastCandleOpenTime);
        ZonedDateTime closeTime = Utils.Utils.getZonedDateTime(event.getCloseTime());
        Duration candleDuration = Duration.ofMillis(event.getCloseTime() - event.getStartTime());
        double open = event.getOpen().doubleValue();
        double high = event.getHigh().doubleValue();
        double low = event.getLow().doubleValue();
        double close = event.getClose().doubleValue();
        double volume = event.getVolume().doubleValue();
        lastCandleOpenTime = event.getStartTime();
        if (isNewCandle){
            realTimeData = realTimeData.getSubSeries(1, realTimeData.getEndIndex() + 1);
        }
        else{
            realTimeData = realTimeData.getSubSeries(0, realTimeData.getEndIndex());
        }
        realTimeData.addBar(candleDuration, closeTime, open, high, low, close, volume);
    }

    private void fillRealTimeData(List<Candlestick> candlestickBars){
        for (Candlestick candlestickBar : candlestickBars) {
            ZonedDateTime closeTime = Utils.Utils.getZonedDateTime(candlestickBar.getCloseTime());
            Duration candleDuration = Duration.ofMillis(candlestickBar.getCloseTime()
                    - candlestickBar.getOpenTime());
            double open = candlestickBar.getOpen().doubleValue();
            double high = candlestickBar.getHigh().doubleValue();
            double low = candlestickBar.getLow().doubleValue();
            double close = candlestickBar.getClose().doubleValue();
            double volume = candlestickBar.getVolume().doubleValue();
            realTimeData.addBar(candleDuration, closeTime, open, high, low, close, volume);
        }
    }

    private void calculateIndicators() {
        rsiIndicator = calculateRSI(RSIConstants.RSI_CANDLE_NUM);
        macdOverRsiIndicator = calculateMacdOverRsi();
        smaIndicator = new SMAIndicator(new ClosePriceIndicator(realTimeData), MACDOverRSIConstants.SMA_CANDLE_NUM);
    }

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

    public double getRsiOpenValue() {
        return rsiIndicator.getValue(realTimeData.getEndIndex()).doubleValue();
    }

    public double getRsiCloseValue() {
        return rsiIndicator.getValue(realTimeData.getEndIndex()-1).doubleValue();
    }

    public double getRSIValueAtIndex(int index) {
        return rsiIndicator.getValue(index).doubleValue();
    }

    public double getSMAValueAtIndex(int index) {
        return smaIndicator.getValue(index).doubleValue();
    }

    private MACDIndicator calculateMacdOverRsi() {
        RSIIndicator rsiIndicator14 = calculateRSI(MACDOverRSIConstants.RSI_CANDLE_NUM);
        return new MACDIndicator(rsiIndicator14, MACDOverRSIConstants.FAST_BAR_COUNT, MACDOverRSIConstants.SLOW_BAR_COUNT);
    }

    private RSIIndicator calculateRSI(int candleNum) {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(realTimeData);
        return new RSIIndicator(closePriceIndicator, candleNum);
    }

    public boolean crossed(IndicatorType indicatorType, CrossType crossType, CandleType candleType, int threshold) {
        switch (indicatorType) {
            case RSI:
                return rsiCrossed(crossType,candleType,threshold);
            case MACD_OVER_RSI:
                return macdOverRsiCrossed(crossType,candleType,threshold);
        }
        return true; // will not come to this!

    }
    private boolean rsiCrossed(CrossType crossType,CandleType candleType, int threshold) {
        double rsiValueNow,rsiValuePrev;
        if (candleType == CandleType.OPEN) {
            rsiValueNow = getRsiOpenValue();
            rsiValuePrev = getRsiCloseValue();
        }
        else {
            rsiValueNow = getRsiCloseValue();
            rsiValuePrev = getRSIValueAtIndex(realTimeData.getEndIndex()-2);
        }
        if (crossType == CrossType.UP) return rsiValueNow > threshold && rsiValuePrev <= threshold;
        return rsiValuePrev >= threshold && rsiValueNow < threshold;
    }
    private boolean macdOverRsiCrossed(CrossType crossType,CandleType candleType, int threshold) {
        double currentMacdOverRsiValue,prevMacdOverRsiValue;
        if (candleType == CandleType.OPEN) {
            currentMacdOverRsiValue = getMacdOverRsiValueAtIndex(getLastIndex());
            prevMacdOverRsiValue  = getMacdOverRsiValueAtIndex(getLastIndex()-1);
        } else {
            currentMacdOverRsiValue = getMacdOverRsiValueAtIndex(getLastCloseIndex());
            prevMacdOverRsiValue = getMacdOverRsiValueAtIndex(getLastCloseIndex()-1);
        }
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
                return getSMAValueAtIndex(getLastIndex())>threshold;
            } else {
                return getSMAValueAtIndex(getLastCloseIndex()) > threshold;
            }
        }
    }

    public RSIIndicator getRsiIndicator() {return rsiIndicator;}

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public int getLastIndex(){return realTimeData.getEndIndex();}
    public int getLastCloseIndex(){return realTimeData.getEndIndex()-1;}

    public BaseBarSeries getRealTimeData() {
        return realTimeData;
    }

    public enum CandleType {
        OPEN,CLOSE
    }

    public enum CrossType {
        UP,DOWN
    }
    public enum IndicatorType {
        RSI,MACD_OVER_RSI, SMA
    }
}
