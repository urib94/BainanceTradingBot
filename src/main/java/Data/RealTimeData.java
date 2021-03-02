package Data;

import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import Strategies.OSMAStrategies.OSMAConstants;
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
//    private MACDIndicator macdOverRsiIndicator;
////    private double rsiOpenValue;
////    private double rsiCloseValue;
////    private double macdOverRsiMacdLineValue;
////    private double macdOverRsiSignalLineValue;
////    private double macdOverRsiValue;




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
        rsiIndicator = calculateRSI();
        macdOverRsiIndicator = calculateMacdOverRsi();
        System.out.println("RSI OPEN VALUE: " + getRsiOpenValue());
        System.out.println("MACDOVER RSI: " + getMacdOverRsiValueAtIndex(getLastIndex()));
    }

    private double calculateOSMAValue() {
        MACDIndicator macd = new MACDIndicator(new ClosePriceIndicator(realTimeData), OSMAConstants.SHORT_BAR_COUNT,OSMAConstants.LONG_BAR_COUNT);
        SMAIndicator signal = new SMAIndicator(macd,OSMAConstants.BAR_COUNT);
        ClosePriceIndicator osma = Utils.Utils.diffByElementBetweenIndicators(macd,signal,Config.CANDLE_NUM);
        SMAIndicator signal2 = new SMAIndicator(osma,OSMAConstants.BAR_COUNT);
        return (osma.getValue(Config.CANDLE_NUM-1).minus(signal2.getValue(Config.CANDLE_NUM-1))).doubleValue();
    }

    private double getMacdOverRsiSignalLineValueAtIndex(int index) {
        EMAIndicator signal = new EMAIndicator(macdOverRsiIndicator, MACDOverRSIConstants.SIGNAL_LENGTH);
        return signal.getValue(index).doubleValue();
    }

    private double getMacdOverRsiMacdLineValueAtIndex(int index) {
        return macdOverRsiIndicator.getValue(index).doubleValue();
    }

    private double getMacdOverRsiValueAtIndex(int index) {
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

    private MACDIndicator calculateMacdOverRsi() {
        return new MACDIndicator(rsiIndicator, MACDOverRSIConstants.FAST_BAR_COUNT, MACDOverRSIConstants.SLOW_BAR_COUNT);
    }

    private RSIIndicator calculateRSI() {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(realTimeData);
        return new RSIIndicator(closePriceIndicator, RSIConstants.RSI_CANDLE_NUM);
    }

    public boolean crossed(CrossType crossType, RSIType rsiType, int threshold) {
        double rsiValueNow,rsiValuePrev;
        if (rsiType == RSIType.OPEN) {
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

    public boolean above(RSIType type, int threshold) {
        if (type == RSIType.OPEN){
            return getRsiOpenValue() > threshold;
        }
        else{
            return getRsiCloseValue() > threshold;
        }
    }



    public RSIIndicator getRsiIndicator() {return rsiIndicator;}

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public int getLastIndex(){return realTimeData.getEndIndex();}
    public int getLastCloseIndex(){return realTimeData.getEndIndex()-1;}

    public enum RSIType {
        OPEN,CLOSE
    }

    public enum CrossType {
        UP,DOWN
    }
}
