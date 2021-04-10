package data;

import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.*;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import strategies.MACDOverCCIWIthATR.MACDOverCCIWIthATRConstants;
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
    private MACDIndicator macdOverCCIIndicator;
    private SMAIndicator smaIndicator;
    private int counter = 0;
    private BollingerBandWidthIndicator bollingerBandWidthIndicator;
    private BollingerBandsUpperIndicator bollingerBandsUpperIndicator;
    private BollingerBandsLowerIndicator bollingerBandsLowerIndicator;
    private PercentBIndicator percentBIndicator;
    private ClosePriceIndicator closePriceIndicator;
    private HighPriceIndicator highPriceIndicator;
    private LowPriceIndicator lowPriceIndicator;
    private ATRIndicator atrIndicator;



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
        return new DataHolder(highPriceIndicator, lowPriceIndicator, closePriceIndicator, rsiIndicator, macdOverRsiIndicator, bollingerBandsUpperIndicator, bollingerBandsLowerIndicator,
                smaIndicator,bollingerBandWidthIndicator, percentBIndicator, realTimeData.getEndIndex(),macdOverCCIIndicator,atrIndicator);
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
        macdOverCCIIndicator=calculateMacdOverCCI(currData);
        macdOverRsiIndicator = calculateMacdOverRsi(currData);
        smaIndicator = new SMAIndicator(new ClosePriceIndicator(currData), MACDOverRSIConstants.SMA_CANDLES);
        calculateBollingerBandsIndicators(currData);
        atrIndicator=calculateATR(currData,MACDOverCCIWIthATRConstants.ATR_CANDLE_COUNT);
    }


    public synchronized void calculateBollingerBandsIndicators(BaseBarSeries currData){
        closePriceIndicator = new ClosePriceIndicator(currData);
        SMAIndicator smaIndicator = new SMAIndicator(closePriceIndicator, 20);
        BollingerBandsMiddleIndicator bollingerBandsMiddleIndicator = new BollingerBandsMiddleIndicator(smaIndicator);
        bollingerBandsUpperIndicator = new BollingerBandsUpperIndicator(bollingerBandsMiddleIndicator, new StandardDeviationIndicator(closePriceIndicator, MACDOverRSIConstants.STANDARD_DEVIATION_CANDLES), MACDOverRSIConstants.DEVIATION_MULTIPLIER);
        bollingerBandsLowerIndicator = new BollingerBandsLowerIndicator(bollingerBandsMiddleIndicator, new StandardDeviationIndicator(closePriceIndicator, MACDOverRSIConstants.STANDARD_DEVIATION_CANDLES), MACDOverRSIConstants.DEVIATION_MULTIPLIER);
        bollingerBandWidthIndicator= new BollingerBandWidthIndicator(bollingerBandsUpperIndicator,bollingerBandsMiddleIndicator,bollingerBandsLowerIndicator);
        percentBIndicator=new PercentBIndicator(closePriceIndicator,20,2);
        //System.out.println("%B="+percentBIndicator.getValue(currData.getEndIndex()));
    }
    private MACDIndicator calculateMacdOverRsi(BaseBarSeries currData) {
        RSIIndicator rsiIndicator14 = calculateRSI(MACDOverRSIConstants.RSI_CANDLE_NUM, currData);
        //CCICIndicator ccicIndicator = new CCICIndicator(currData, MACDOverRSIConstants.CCIC_CANDLES);
        //System.out.println("rsi: " + rsiIndicator14.getValue(currData.getEndIndex()-1));
        return new MACDIndicator(rsiIndicator14, MACDOverRSIConstants.FAST_BAR_COUNT, MACDOverRSIConstants.SLOW_BAR_COUNT);
    }

    private MACDIndicator calculateMacdOverCCI(BaseBarSeries currData) {
        //RSIIndicator rsiIndicator14 = calculateRSI(MACDOverRSIConstants.RSI_CANDLE_NUM, currData);
        CCICIndicator cciIndicator = new CCICIndicator(currData, MACDOverCCIWIthATRConstants.CCI_CANDLES);
        //System.out.println("cci: "+"" + cciIndicator.getValue(currData.getEndIndex())+currData.getLastBar().getEndTime().toLocalDateTime().toString());
        return new MACDIndicator(cciIndicator, MACDOverRSIConstants.FAST_BAR_COUNT, MACDOverRSIConstants.SLOW_BAR_COUNT);

    }

        private RSIIndicator calculateRSI(int candleNum, BaseBarSeries currData) {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(currData);
        return new RSIIndicator(closePriceIndicator, candleNum);
    }
    private ATRIndicator calculateATR(BaseBarSeries currData, int candleNum){
        return new ATRIndicator (currData,candleNum);
    }
}
