package data;

import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.*;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import strategies.MACDOverCCIWIthATR.MACDOverCCIWIthATRConstants;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.event.CandlestickEvent;
import com.binance.client.model.market.Candlestick;
import org.ta4j.core.BaseBarSeries;
import singletonHelpers.RequestClient;
import strategies.MACrosses.MACrossesConstants;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

//* For us, in realTimeData, the last candle is always open. The previous ones are closed.
public class RealTimeData{

    private Long lastCandleOpenTime;
    private BaseBarSeries realTimeData;
    private MACDIndicator macdOverRsiIndicator;
    private MACDIndicator macdOverCCIIndicator;
    private int counter = 0;
    private BollingerBandWidthIndicator closeBollingerBandWidthIndicator;
    private BollingerBandsUpperIndicator closeBollingerBandsUpperIndicator;
    private BollingerBandsLowerIndicator closeBollingerBandsLowerIndicator;
    private BollingerBandWidthIndicator bollingerBandWidthIndicator;
    private BollingerBandsUpperIndicator bollingerBandsUpperIndicator;
    private BollingerBandsLowerIndicator bollingerBandsLowerIndicator;
    private PercentBIndicator percentBIndicator;
    private ClosePriceIndicator closePriceIndicator;
    private HighPriceIndicator highPriceIndicator;
    private LowPriceIndicator lowPriceIndicator;
    private OpenPriceIndicator openPriceIndicator;
    private ATRIndicator atrIndicator;

    private CCICIndicator ccicIndicator;
    private MFIIndicator mfiIndicator;
    private RSIIndicator rsiIndicator;

    private SMAIndicator slowSmaIndicator;
    private SMAIndicator fastSmaIndicator;
    private SMAIndicator fastSMAOverRsiIndicator;
    private SMAIndicator slowSMAOverRsiIndicator;

    private VolumeIndicator volumeIndicator;
    private SMAIndicator smaOverVolumeIndicator;



    private MACDIndicator macdOverMa9;
    private MACDIndicator macdOverMa14;
    private MACDIndicator macdOverMa50;





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
                slowSmaIndicator,bollingerBandWidthIndicator, percentBIndicator, realTimeData.getEndIndex(), macdOverCCIIndicator, atrIndicator, ccicIndicator, macdOverMa9, macdOverMa14,
                macdOverMa50, mfiIndicator, fastSmaIndicator, fastSMAOverRsiIndicator, slowSMAOverRsiIndicator, openPriceIndicator, volumeIndicator, smaOverVolumeIndicator,
                closeBollingerBandWidthIndicator , closeBollingerBandsUpperIndicator , closeBollingerBandsLowerIndicator);
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
        BaseBarSeries currData = new BaseBarSeries(realTimeData.getBarData());
        rsiIndicator = calculateRSI(MACrossesConstants.RSI_CANDLE_NUM, currData);
        fastSmaIndicator = new SMAIndicator(new ClosePriceIndicator(currData), MACrossesConstants.FAST_SMA_BAR_COUNT);
        fastSMAOverRsiIndicator = new SMAIndicator(rsiIndicator , MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT);
        mfiIndicator = new MFIIndicator(currData, MACrossesConstants.MFI_BAR_COUNT);
        slowSMAOverRsiIndicator = new SMAIndicator(rsiIndicator, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT);
        highPriceIndicator = new HighPriceIndicator(currData);
        lowPriceIndicator = new LowPriceIndicator(currData);
        openPriceIndicator = new OpenPriceIndicator(currData);
        macdOverCCIIndicator = calculateMacdOverCCI(currData);
        slowSmaIndicator = new SMAIndicator(new ClosePriceIndicator(currData), MACrossesConstants.SLOW_SMA_BAR_COUNT);
        closePriceIndicator = new ClosePriceIndicator(currData);
        volumeIndicator = new VolumeIndicator(currData);
        smaOverVolumeIndicator = new SMAIndicator(volumeIndicator, MACrossesConstants.SMA_OVER_VOLUME_BAR_COUNT);
        calculateBollingerBandsIndicators(currData);
//        atrIndicator = calculateATR(currData,MACDOverCCIWIthATRConstants.ATR_CANDLE_COUNT);
//        macdOverMa9 = calculateMacdOverMa(currData, MACDOverSMAConstants.FAST_CANDLE_COUNT);
//        macdOverMa14 = calculateMacdOverMa(currData, MACDOverSMAConstants.MEDIUM_CANDLE_COUNT);
//        macdOverMa50 = calculateMacdOverMa(currData, MACDOverSMAConstants.SLOW_CANDLE_COUNT);
    }

    private MACDIndicator calculateMacdOverMa(BaseBarSeries currData, int i) {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(currData);
        SMAIndicator smaIndicator = new SMAIndicator(closePriceIndicator, i);
        return new MACDIndicator(smaIndicator, MACDOverCCIWIthATRConstants.FAST_BAR_COUNT, MACDOverCCIWIthATRConstants.SLOW_BAR_COUNT);
    }


    public synchronized void calculateBollingerBandsIndicators(BaseBarSeries currData){
        closePriceIndicator = new ClosePriceIndicator(currData);
        SMAIndicator smaIndicator = new SMAIndicator(closePriceIndicator, 20);
        BollingerBandsMiddleIndicator bollingerBandsMiddleIndicator = new BollingerBandsMiddleIndicator(smaIndicator);
        bollingerBandsUpperIndicator = new BollingerBandsUpperIndicator(bollingerBandsMiddleIndicator, new StandardDeviationIndicator(closePriceIndicator, MACrossesConstants.STANDARD_DEVIATION_CANDLES), MACrossesConstants.DEVIATION_MULTIPLIER);
        bollingerBandsLowerIndicator = new BollingerBandsLowerIndicator(bollingerBandsMiddleIndicator, new StandardDeviationIndicator(closePriceIndicator, MACrossesConstants.STANDARD_DEVIATION_CANDLES), MACrossesConstants.DEVIATION_MULTIPLIER);
        bollingerBandWidthIndicator = new BollingerBandWidthIndicator(bollingerBandsUpperIndicator,bollingerBandsMiddleIndicator,bollingerBandsLowerIndicator);
        percentBIndicator = new PercentBIndicator(closePriceIndicator,20,2);
        closeBollingerBandsUpperIndicator = new BollingerBandsUpperIndicator(bollingerBandsMiddleIndicator, new StandardDeviationIndicator(closePriceIndicator, MACrossesConstants.STANDARD_DEVIATION_CANDLES), MACrossesConstants.CLOSE_DEVIATION_MULTIPLIER) ;
        closeBollingerBandsLowerIndicator = new BollingerBandsLowerIndicator(bollingerBandsMiddleIndicator, new StandardDeviationIndicator(closePriceIndicator, MACrossesConstants.STANDARD_DEVIATION_CANDLES), MACrossesConstants.CLOSE_DEVIATION_MULTIPLIER) ;
        closeBollingerBandWidthIndicator = new BollingerBandWidthIndicator (closeBollingerBandsUpperIndicator,bollingerBandsMiddleIndicator,closeBollingerBandsLowerIndicator);
        //System.out.println("%B="+percentBIndicator.getValue(currData.getEndIndex()));
    }

    private MACDIndicator calculateMacdOverCCI(BaseBarSeries currData) {
        //RSIIndicator rsiIndicator14 = calculateRSI(MACDOverRSIConstants.RSI_CANDLE_NUM, currData);
        ccicIndicator = new CCICIndicator(currData, MACDOverCCIWIthATRConstants.CCI_CANDLES);
        //System.out.println("cci: "+"" + cciIndicator.getValue(currData.getEndIndex())+currData.getLastBar().getEndTime().toLocalDateTime().toString());
        return new MACDIndicator(ccicIndicator, MACDOverCCIWIthATRConstants.FAST_BAR_COUNT, MACDOverCCIWIthATRConstants.SLOW_BAR_COUNT);

    }

        private RSIIndicator calculateRSI(int candleNum, BaseBarSeries currData) {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(currData);
        return new RSIIndicator(closePriceIndicator, candleNum);
    }
    private ATRIndicator calculateATR(BaseBarSeries currData, int candleNum){
        return new ATRIndicator (currData,candleNum);
    }
}
