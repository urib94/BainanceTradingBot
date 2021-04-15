//package strategies.MACDOverCCIWIthATR;
//
//import com.binance.client.SyncRequestClient;
//import com.binance.client.model.enums.CandlestickInterval;
//import com.binance.client.model.market.Candlestick;
//import org.ta4j.core.*;
//import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
//import org.ta4j.core.indicators.SMAIndicator;
//import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
//import org.ta4j.core.num.Num;
//import org.ta4j.core.num.PrecisionNum;
//import org.ta4j.core.trading.rules.OverIndicatorRule;
//import org.ta4j.core.trading.rules.UnderIndicatorRule;
//import singletonHelpers.RequestClient;
//
//import java.time.Duration;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MACDOverCCIWIthATRBackTesting {
//    public static void main(String[] args) throws InterruptedException {
//        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//        List<Candlestick> candlesticks = new ArrayList<>();
//        Long start = 1609509725L;
//        Long end = Long.valueOf(String.valueOf(System.currentTimeMillis()));
//        while (start < end - 1500) {
//            candlesticks.addAll(syncRequestClient.getCandlestick("bnbusdt", CandlestickInterval.ONE_MINUTE, start,
//                    end, 1500));
//            start += 1500;
//        }
//
//        BarSeries series = createBarSeries(candlesticks);
//
//        Strategy strategy3DaySma = create3DaySmaStrategy(series);
//
//        BarSeriesManager seriesManager = new BarSeriesManager(series);
//        TradingRecord tradingRecord3DaySma = seriesManager.run(strategy3DaySma, Order.OrderType.BUY,
//                PrecisionNum.valueOf(50));
//        System.out.println(tradingRecord3DaySma);
//
//        Strategy strategy2DaySma = create2DaySmaStrategy(series);
//        TradingRecord tradingRecord2DaySma = seriesManager.run(strategy2DaySma, Order.OrderType.BUY,
//                PrecisionNum.valueOf(50));
//        System.out.println(tradingRecord2DaySma);
//
//        AnalysisCriterion criterion = new TotalProfitCriterion();
//        Num calculate3DaySma = criterion.calculate(series, tradingRecord3DaySma);
//        Num calculate2DaySma = criterion.calculate(series, tradingRecord2DaySma);
//
//        System.out.println(calculate3DaySma);
//        System.out.println(calculate2DaySma);
//    }
//
//    private static BarSeries createBarSeries(List<Candlestick> candlesticks ) {
//        BarSeries series = new BaseBarSeries();
//
//        int count = 0;
//        for (Candlestick candlestick : candlesticks) {
//            series.addBar(createBar(candlestick));
//        }
//            return series;
//
//    }
//    public static String[] candlestickToStringArray(Candlestick candlestick) {
//        String[] strings = new String[11];
//        strings[0] = candlestick.getOpenTime().toString();
//        strings[1] = candlestick.getOpen().toString();
//        strings[2] = candlestick.getHigh().toString();
//        strings[3] = candlestick.getLow().toString();
//        strings[4] = candlestick.getClose().toString();
//        strings[5] = candlestick.getVolume().toString();
//        strings[6] = candlestick.getCloseTime().toString();
//        strings[7] = candlestick.getQuoteAssetVolume().toString();
//        strings[8] = candlestick.getNumTrades().toString();
//        strings[9] = candlestick.getTakerBuyBaseAssetVolume().toString();
//        strings[10] = candlestick.getTakerBuyQuoteAssetVolume().toString();
//
//        return strings;
//
//    }
//
//    private static BaseBar createBar(Candlestick candlestick) {
//        ZonedDateTime endTime=ZonedDateTime.now() ;
//        return BaseBar.builder(PrecisionNum::valueOf, Number.class).timePeriod(Duration.ofDays(1)).endTime(endTime)
//                .openPrice(candlestick.getOpen()).highPrice(candlestick.getHigh())
//                .lowPrice(candlestick.getLow()).closePrice(candlestick.getClose()).volume(candlestick.getVolume())
//                .build();
//    }
//
//    private static ZonedDateTime CreateDay(int day) {
//        return ZonedDateTime.of(2018, 01, day, 12, 0, 0, 0, ZoneId.systemDefault());
//    }
//
//    private static Strategy create3DaySmaStrategy(BarSeries series) {
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        SMAIndicator sma = new SMAIndicator(closePrice, 3);
//        return new BaseStrategy(new UnderIndicatorRule(sma, closePrice), new OverIndicatorRule(sma, closePrice));
//    }
//
//    private static Strategy create2DaySmaStrategy(BarSeries series) {
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        SMAIndicator sma = new SMAIndicator(closePrice, 2);
//        return new BaseStrategy(new UnderIndicatorRule(sma, closePrice), new OverIndicatorRule(sma, closePrice));
//    }
//
//}
