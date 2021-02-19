package Data;

import Data.PrivateConfig;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.event.CandlestickEvent;
import com.binance.client.model.market.Candlestick;
import org.ta4j.core.BaseBarSeries;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class RealTimeData{

    private Long lastCandleOpenTime = 0L;
    private BaseBarSeries realTimeData;
    private final ReentrantReadWriteLock lock;

    public RealTimeData(String symbol, CandlestickInterval interval, int amount){
        realTimeData = new BaseBarSeries();
        lock = new ReentrantReadWriteLock();
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, options);
        List<Candlestick> candlestickBars = syncRequestClient.getCandlestick(symbol, interval, null, null, amount);
        lastCandleOpenTime = candlestickBars.get(candlestickBars.size() - 1).getOpenTime();
        for (Candlestick candlestickBar : candlestickBars) {
            ZonedDateTime closeTime = getZonedDateTime(candlestickBar.getCloseTime());
            Duration candleDuration = Duration.ofMillis(candlestickBar.getCloseTime()
                    - candlestickBar.getOpenTime());
            double open = candlestickBar.getOpenTime().doubleValue();
            double high = candlestickBar.getHigh().doubleValue();
            double low = candlestickBar.getLow().doubleValue();
            double close = candlestickBar.getCloseTime().doubleValue();
            double volume = candlestickBar.getVolume().doubleValue();
            realTimeData.addBar(candleDuration, closeTime, open, high, low, close, volume);
        }
    }

    /**
     *
     * @param updateCandlestick - the current candlestick the class maintains
     * @param event - the candlestick event from the subscription of candlesticks in Main Class.
     */
    private static void fillCandleStickFromEvent(Candlestick updateCandlestick, CandlestickEvent event) {
        updateCandlestick.setOpenTime(event.getStartTime());
        updateCandlestick.setOpen(event.getOpen());
        updateCandlestick.setLow(event.getLow());
        updateCandlestick.setHigh(event.getHigh());
        updateCandlestick.setClose(event.getClose());
        updateCandlestick.setCloseTime(event.getCloseTime());
        updateCandlestick.setVolume(event.getVolume());
        updateCandlestick.setNumTrades(Math.toIntExact(event.getNumTrades()));
        updateCandlestick.setQuoteAssetVolume(event.getQuoteAssetVolume());
        updateCandlestick.setTakerBuyQuoteAssetVolume(event.getTakerBuyQuoteAssetVolume());
        updateCandlestick.setTakerBuyBaseAssetVolume(event.getTakerBuyQuoteAssetVolume());

    }

    public void updateData(CandlestickEvent event){
        boolean isNewCandle = !(event.getStartTime().doubleValue() == lastCandleOpenTime);
        Candlestick updateCandlestick = new Candlestick();
        fillCandleStickFromEvent(updateCandlestick,event);
        ZonedDateTime closeTime = getZonedDateTime(updateCandlestick.getCloseTime());
        Duration candleDuration = Duration.ofMillis(updateCandlestick.getCloseTime() - updateCandlestick.getOpenTime());
        double open = updateCandlestick.getOpenTime().doubleValue();
        double high = updateCandlestick.getHigh().doubleValue();
        double low = updateCandlestick.getLow().doubleValue();
        double close = updateCandlestick.getCloseTime().doubleValue();
        double volume = updateCandlestick.getVolume().doubleValue();
        lastCandleOpenTime = updateCandlestick.getOpenTime();
        lock.writeLock().lock();
        if (isNewCandle){
            realTimeData = realTimeData.getSubSeries(1, realTimeData.getBarCount());
        }
        else{
            realTimeData = realTimeData.getSubSeries(0, realTimeData.getBarCount() - 1);
        }
        realTimeData.addBar(candleDuration, closeTime, open, high, low, close, volume);
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

    private ZonedDateTime getZonedDateTime(Long timestamp) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault());
    }
}
