package Data;

import Strategies.RSIStrategies.RSIConstants;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;
import com.binance.client.api.model.event.CandlestickEvent;
import com.binance.client.api.model.market.Candlestick;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;


public class RealTimeData{

    private Long lastCandleOpenTime = 0L;
    //* For us, in realTimeData, the last candle is always open. The previous ones are closed.
    private BaseBarSeries realTimeData;
    private BigDecimal currentPrice;
    private RSIIndicator rsiOpenIndicator;
    private RSIIndicator rsiCloseIndicator;

    public RealTimeData(String symbol, CandlestickInterval interval, int amount){
        realTimeData = new BaseBarSeries();
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        List<Candlestick> candlestickBars = syncRequestClient.getCandlestick(symbol, interval, null, null, amount);
        lastCandleOpenTime = candlestickBars.get(candlestickBars.size() - 1).getOpenTime();
        currentPrice = candlestickBars.get(candlestickBars.size() -1).getClose();
        for (Candlestick candlestickBar : candlestickBars) {
            ZonedDateTime closeTime = getZonedDateTime(candlestickBar.getCloseTime());
            Duration candleDuration = Duration.ofMillis(candlestickBar.getCloseTime()
                    - candlestickBar.getOpenTime());
            double open = candlestickBar.getOpen().doubleValue();
            double high = candlestickBar.getHigh().doubleValue();
            double low = candlestickBar.getLow().doubleValue();
            double close = candlestickBar.getClose().doubleValue();
            double volume = candlestickBar.getVolume().doubleValue();
            realTimeData.addBar(candleDuration, closeTime, open, high, low, close, volume);
        }
        rsiOpenIndicator = calculateRSI(RSIType.OPEN);
        rsiCloseIndicator = calculateRSI(RSIType.CLOSE);

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
        currentPrice = event.getClose();
        boolean isNewCandle = !(event.getStartTime().doubleValue() == lastCandleOpenTime);
        ZonedDateTime closeTime = getZonedDateTime(event.getCloseTime());
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
        rsiOpenIndicator = calculateRSI(RSIType.OPEN);
        rsiCloseIndicator = calculateRSI(RSIType.CLOSE);
    }

    public RSIIndicator getRsiCloseIndicator() {
        return rsiCloseIndicator;
    }

    public RSIIndicator getRsiOpenIndicator() {
        return rsiOpenIndicator;
    }

    public BaseBarSeries getRealTimeData(){
        return realTimeData;
    }

    /**
     * Checks if the previous RSIIndicator of the same
     * @param crossType up or down. Checks if the @param rsiType RSIIndicator is up (above) or down (below) the threshold.
     * @param rsiType close or open
     * @param threshold - the threshold to check crossing.
     * @return
     */
    public boolean crossed(CrossType crossType, RSIType rsiType, int threshold) {
        double rsiValueNow,rsiValuePrev;
        int lastBarIndex;
        if (rsiType == RSIType.OPEN) {
            lastBarIndex = realTimeData.getEndIndex();
            rsiValueNow = rsiOpenIndicator.getValue(lastBarIndex).doubleValue();
            rsiValuePrev = rsiOpenIndicator.getValue(lastBarIndex-1).doubleValue();
        }
        else { // type == close
            lastBarIndex = getAllClosedCandles().getEndIndex();
            rsiValueNow = rsiCloseIndicator.getValue(lastBarIndex).doubleValue();
            rsiValuePrev = rsiCloseIndicator.getValue(lastBarIndex-1).doubleValue();
        }
        if (crossType == CrossType.UP) return rsiValueNow > threshold && rsiValuePrev <= threshold;
        return rsiValuePrev >= threshold && rsiValueNow < threshold;
    }

    public BaseBarSeries getAllClosedCandles() {
        return realTimeData.getSubSeries(0, realTimeData.getEndIndex());
    }

    private RSIIndicator calculateRSI(RSIType type) {
        ClosePriceIndicator closePriceIndicator;
        if (type == RSIType.OPEN) {
            closePriceIndicator = new ClosePriceIndicator(realTimeData);
        } else {
            closePriceIndicator = new ClosePriceIndicator(getAllClosedCandles());
        }
        return new RSIIndicator(closePriceIndicator, RSIConstants.RSI_CANDLE_NUM);

    }

    private ZonedDateTime getZonedDateTime(Long timestamp) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault());
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }


    public boolean above(RSIType type, int threshold) {
        double rsiValue;
        if (type == RSIType.CLOSE){
            rsiValue = calculateCurrentClosedRSIValue();
        }
        else{
            rsiValue = calculateCurrentOpenRSIValue();
        }
        return rsiValue > threshold;
    }

    public boolean currentRSIValueEquals(RSIType type, double valueToCheck) {
        double rsiValue;
        if (type == RSIType.CLOSE){
            rsiValue = calculateCurrentClosedRSIValue();
        }
        else{
            rsiValue = calculateCurrentOpenRSIValue();
        }
        return valueToCheck == rsiValue;
    }

    public double calculateCurrentClosedRSIValue() {
        int lastBarIndex = getAllClosedCandles().getEndIndex();
        return rsiCloseIndicator.getValue(lastBarIndex).doubleValue();
    }

    public double calculateCurrentOpenRSIValue() {
        int lastBarIndex = realTimeData.getEndIndex();
        return rsiOpenIndicator.getValue(lastBarIndex).doubleValue();
    }

    public enum RSIType {
        OPEN,CLOSE
    }

    public enum CrossType {
        UP,DOWN
    }
}
