package Data;

import com.binance.client.api.RequestOptions;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;
import com.binance.client.api.model.event.CandlestickEvent;
import com.binance.client.api.model.market.Candlestick;
import org.ta4j.core.BaseBarSeries;
import utilities.RSIUtiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class RealTimeData{

    private Long lastCandleOpenTime = 0L;

    //* For us, in realTimeData, the last candle is always open. The previous ones are closed.
    private BaseBarSeries realTimeData;
    private final ReentrantReadWriteLock lock;
    private BigDecimal currentPrice;
    private BigDecimal rsiValue;

    public RealTimeData(String symbol, CandlestickInterval interval, int amount){
        realTimeData = new BaseBarSeries();
        lock = new ReentrantReadWriteLock();
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
        rsiValue = RSIUtiles.rsiStepTwoCalculatorForOpen(this);
    }

    public void updateData(CandlestickEvent event){
        currentPrice = event.getClose();
        System.out.println(currentPrice);
        boolean isNewCandle = !(event.getStartTime().doubleValue() == lastCandleOpenTime);
        ZonedDateTime closeTime = getZonedDateTime(event.getCloseTime());
        Duration candleDuration = Duration.ofMillis(event.getCloseTime() - event.getStartTime());
        double open = event.getOpen().doubleValue();//updateCandlestick.getOpen().doubleValue();
        double high = event.getHigh().doubleValue();
        double low = event.getLow().doubleValue();
        double close = event.getClose().doubleValue();
        double volume = event.getVolume().doubleValue();
        lastCandleOpenTime = event.getStartTime();
        lock.writeLock().lock();
        if (isNewCandle){
            realTimeData = realTimeData.getSubSeries(1, realTimeData.getEndIndex() + 1);
        }
        else{
            realTimeData = realTimeData.getSubSeries(0, realTimeData.getEndIndex());
        }
        realTimeData.addBar(candleDuration, closeTime, open, high, low, close, volume);
        rsiValue = RSIUtiles.rsiStepTwoCalculatorForOpen(this);
        System.out.println(rsiValue);
        lock.writeLock().unlock();
    }

    public BaseBarSeries getRealTimeData(){
        try{
            lock.readLock().lock();
            return realTimeData;
        }finally {
            lock.readLock().unlock();
        }
    }
    public BaseBarSeries getLastAmountOfClosedCandles(int amount) {
        try {
            lock.readLock().lock();
            return realTimeData.getSubSeries(realTimeData.getBarCount() - (amount + 1), realTimeData.getEndIndex()-1);
        } finally {
            lock.readLock().unlock();
        }
    }
    public BaseBarSeries getLastAmountOfCandles(int amount) {
        try {
            lock.readLock().lock();
            return realTimeData.getSubSeries(realTimeData.getBarCount() - amount, realTimeData.getBarCount());
        } finally {
            lock.readLock().unlock();
        }
    }
    private ZonedDateTime getZonedDateTime(Long timestamp) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault());
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getOpenCandleRsiValue() {
        try{
            lock.readLock().lock();
            return rsiValue;
        }
        finally {
            lock.readLock().unlock();
        }
    }
}
