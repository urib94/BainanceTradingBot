package data;

import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.event.CandlestickEvent;
import com.binance.client.model.market.Candlestick;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import singletonHelpers.RequestClient;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

//* For us, in realTimeData, the last candle is always open. The previous ones are closed.
public class RealTimeData{

    private Long lastCandleOpenTime;
    private BaseBarSeries realTimeData;
    private RSIIndicator rsiIndicator;
    private MACDIndicator macdOverRsiIndicator;
    private SMAIndicator smaIndicator;
    private int counter = 0;
    private BollingerBandsUpperIndicator bollingerBandsUpperIndicator;
    private BollingerBandsLowerIndicator bollingerBandsLowerIndicator;
    private ClosePriceIndicator closePriceIndicator;
    private HighPriceIndicator highPriceIndicator;
    private LowPriceIndicator lowPriceIndicator;


    public RealTimeData(String symbol, CandlestickInterval interval){
        realTimeData = new BaseBarSeries();
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        List<Candlestick> candlestickBars = syncRequestClient.getCandlestick(symbol, interval, null, null, Config.CANDLE_NUM);
        lastCandleOpenTime = candlestickBars.get(candlestickBars.size() - 1).getOpenTime();
        fillRealTimeData(candlestickBars);
        calculateIndicators();
    }


    /**
     * Receives the current candlestick - usually an open one.
     * The function updateData updates realTimeData in the following way: if the candle received is closed => push to the end
     * of realTimeData and erase the first. If the candle is open - delete the last one from real time data and push the new one.
     * Calculates the RSIIndicators in either case - to get the most accurate data.
     * to realTimeData
     * @param event - the new Candlestick received from the subscribeCandleStickEvent.
     */
    public synchronized DataHolder updateData(CandlestickEvent event){
        boolean isNewCandle = updateLastCandle(event);
        counter += 1;
        if (! isNewCandle && counter != 20) return null;
        counter = 0;
        calculateIndicators();
        return new DataHolder(highPriceIndicator, lowPriceIndicator, closePriceIndicator, rsiIndicator, macdOverRsiIndicator, bollingerBandsUpperIndicator, bollingerBandsLowerIndicator, smaIndicator, realTimeData.getEndIndex());
    }

    private boolean updateLastCandle(CandlestickEvent event) {
        boolean isNewCandle = !(event.getStartTime().doubleValue() == lastCandleOpenTime);
        ZonedDateTime closeTime = utils.Utils.getZonedDateTime(event.getCloseTime());
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
        return isNewCandle;
    }

    private void fillRealTimeData(List<Candlestick> candlestickBars){
        for (Candlestick candlestickBar : candlestickBars) {
            ZonedDateTime closeTime = utils.Utils.getZonedDateTime(candlestickBar.getCloseTime());
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
        //rsiIndicator = calculateRSI(RSIConstants.RSI_CANDLE_NUM);
        BaseBarSeries currData = new BaseBarSeries(realTimeData.getBarData());
        highPriceIndicator = new HighPriceIndicator(currData);
        lowPriceIndicator = new LowPriceIndicator(currData);
        macdOverRsiIndicator = calculateMacdOverRsi(currData);
        smaIndicator = new SMAIndicator(new ClosePriceIndicator(currData), MACDOverRSIConstants.SMA_CANDLES);
        calculateBollingerBandsIndicators(currData);
    }

    public synchronized void calculateBollingerBandsIndicators(BaseBarSeries currData){
        closePriceIndicator = new ClosePriceIndicator(currData);
        SMAIndicator smaIndicator = new SMAIndicator(closePriceIndicator, 20);
        BollingerBandsMiddleIndicator bollingerBandsMiddleIndicator = new BollingerBandsMiddleIndicator(smaIndicator);
        bollingerBandsUpperIndicator = new BollingerBandsUpperIndicator(bollingerBandsMiddleIndicator, new StandardDeviationIndicator(closePriceIndicator, MACDOverRSIConstants.STANDARD_DEVIATION_CANDLES), MACDOverRSIConstants.DEVIATION_MULTIPLIER);
        bollingerBandsLowerIndicator = new BollingerBandsLowerIndicator(bollingerBandsMiddleIndicator, new StandardDeviationIndicator(closePriceIndicator, MACDOverRSIConstants.STANDARD_DEVIATION_CANDLES), MACDOverRSIConstants.DEVIATION_MULTIPLIER);
    }

    private MACDIndicator calculateMacdOverRsi(BaseBarSeries currData) {
        //RSIIndicator rsiIndicator14 = calculateRSI(MACDOverRSIConstants.RSI_CANDLE_NUM, currData);
        CCICIndicator ccicIndicator = new CCICIndicator(currData, MACDOverRSIConstants.CCIC_CANDLES);
        System.out.println("cc: " + ccicIndicator.getValue(currData.getEndIndex()-1));
        return new MACDIndicator(ccicIndicator/*rsiIndicator14*/, MACDOverRSIConstants.FAST_BAR_COUNT, MACDOverRSIConstants.SLOW_BAR_COUNT);
    }

    private RSIIndicator calculateRSI(int candleNum, BaseBarSeries currData) {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(currData);
        return new RSIIndicator(closePriceIndicator, candleNum);
    }
}
