package utilities;

import Data.Config;
import Data.RealTimeData;
import com.binance.client.api.model.market.Candlestick;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import com.sun.org.apache.xpath.internal.operations.Minus;
import javafx.util.Pair;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.GainIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RSIUtiles {
	/**
	 * @param value
	 * @param threshold
	 * @return if the value >= threshold
	 */
	public static boolean aboveThreshold(BigDecimal value, int threshold) {
		return value.compareTo(new BigDecimal(threshold)) > 0;
	}
	public static boolean belowThreshold(BigDecimal value, int threshold) {
		return value.compareTo(new BigDecimal(threshold)) < 0;
	}
	//! In construction methods! DO NOT USE.
	public static BigDecimal rsiStepTwoCalculatorForClosed(RealTimeData realTimeData) {
		BaseBarSeries baseBarSeries = realTimeData.getLastAmountOfClosedCandles(Config.RSI_CANDLE_NUM + 1);
		return calculateRsi(baseBarSeries);
	}
	public static BigDecimal rsiStepTwoCalculatorForOpen(RealTimeData realTimeData) {
		BaseBarSeries baseBarSeries = realTimeData.getLastAmountOfCandles(Config.RSI_CANDLE_NUM + 1);
		return calculateRsi(baseBarSeries);
	}
	private synchronized static BigDecimal calculateRsi(BaseBarSeries baseBarSeries){
		List<Num> closePrices = baseBarSeries.getBarData().stream().map(Bar::getClosePrice).collect(Collectors.toList());
		List<Double> gains = new ArrayList<>();
		List<Double> losses = new ArrayList<>();
		for (int i = 1; i < closePrices.size(); i++) {
			gains.add(up(closePrices.get(i),closePrices.get(i-1)));
			losses.add(down(closePrices.get(i),closePrices.get(i-1)));
		}
		System.out.println("gains:" + gains);
		System.out.println("losses:" + losses);
		int lastIndex = gains.size()-1;
		System.out.println(closePrices);
		double up = ema(gains,lastIndex);
		double down = ema(losses,lastIndex);
		double RS = up / down;
		return BigDecimal.valueOf(100 - (100 / (1 + RS)));
	}

	private static double down(Num nt2, Num nt1) {
		return Math.max(nt1.minus(nt2).doubleValue(),0);
	}
	private static double up(Num nt2, Num nt1) {
		return Math.max(nt2.minus(nt1).doubleValue(), 0);
	}
	private static double ema(List<Double> list, int index) {
		if (index == 0) {
			return list.get(0);
		}
		return Config.ALPHA*list.get(index) + (1-Config.ALPHA)*ema(list,index -1);
	}



			//		BigDecimal averageLossNotIncludingLastOne = calculateAverageLoss(baseBarSeries, baseBarSeries.getEndIndex()-1);
//		BigDecimal currentGain = calculateCurrentGain(baseBarSeries);
//		BigDecimal currentLoss = calculateCurrentLoss(baseBarSeries);
//		BigDecimal mone = new BigDecimal(100);
//		BigDecimal moneOfMehane = (averageGainNotIncludingLastOne.multiply(new BigDecimal(Config.RSI_CANDLE_NUM-1))).add(currentGain);
//		BigDecimal mehaneOfMehane = (averageLossNotIncludingLastOne.multiply(new BigDecimal(Config.RSI_CANDLE_NUM-1))).add(currentLoss);
//		BigDecimal mehane = (moneOfMehane.divide(mehaneOfMehane, MathContext.DECIMAL64)).add(BigDecimal.ONE);
//		BigDecimal fraction = mone.divide(mehane,MathContext.DECIMAL64);
//		return new BigDecimal(100).subtract(fraction);
//	}
//
//	private static BigDecimal calculateCurrentGain(BaseBarSeries baseBarSeries) {
//		int lastIndex = baseBarSeries.getEndIndex();
//		Num candleLastClosedPrice = baseBarSeries.getBar(lastIndex).getClosePrice();
//		Num candlePrevLastClosedPrice = baseBarSeries.getBar(lastIndex-1).getClosePrice();
//		if (candleLastClosedPrice.isGreaterThan(candlePrevLastClosedPrice)) {
//			Num gain = candleLastClosedPrice.minus(candlePrevLastClosedPrice);
//			return BigDecimal.valueOf(gain.doubleValue());
//		}
//		return BigDecimal.ZERO;
//	}
//
//	private static BigDecimal calculateCurrentLoss(BaseBarSeries baseBarSeries) {
//		int lastIndex = baseBarSeries.getEndIndex();
//		Num candleLastClosedPrice = baseBarSeries.getBar(lastIndex).getClosePrice();
//		Num candlePrevLastClosedPrice = baseBarSeries.getBar(lastIndex-1).getClosePrice();
//		if (candleLastClosedPrice.isLessThan(candlePrevLastClosedPrice)) {
//			Num gain = candlePrevLastClosedPrice.minus(candleLastClosedPrice);
//			return BigDecimal.valueOf(gain.doubleValue());
//		}
//		return BigDecimal.ZERO;
//	}
//
//	private static BigDecimal calculateAverageGain(BaseBarSeries baseBarSeries, int lastIndex) {
//		PrecisionNum sum = PrecisionNum.valueOf(0);
//		for (int i = baseBarSeries.getBeginIndex()+1; i <= lastIndex; i++) {
//			Bar candlePrev = baseBarSeries.getBar(i-1);
//			Bar candleNow = baseBarSeries.getBar(i);
//			Num candlePrevClosePrice = candlePrev.getClosePrice();
//			Num candleNowClosedPrice = candleNow.getClosePrice();
//			if (candleNowClosedPrice.isGreaterThan(candlePrevClosePrice)) {
//				sum = (PrecisionNum) sum.plus((candleNowClosedPrice.minus(candlePrevClosePrice)));
//			}
//		}
//		Num length = PrecisionNum.valueOf(lastIndex-baseBarSeries.getBeginIndex() + 1);
//		Num average = sum.dividedBy(length);
//		return BigDecimal.valueOf(average.doubleValue());
//	}
//	private static BigDecimal calculateAverageLoss(BaseBarSeries baseBarSeries, int lastIndex) {
//		PrecisionNum sum = PrecisionNum.valueOf(0);
//		for (int i = baseBarSeries.getBeginIndex()+1; i <= lastIndex; i++) {
//			Bar candlePrev = baseBarSeries.getBar(i-1);
//			Bar candleNow = baseBarSeries.getBar(i);
//			Num candlePrevClosePrice = candlePrev.getClosePrice();
//			Num candleNowClosedPrice = candleNow.getClosePrice();
//			if (candleNowClosedPrice.isLessThan(candlePrevClosePrice)) {
//				sum = (PrecisionNum) sum.plus(candlePrevClosePrice.minus(candleNowClosedPrice));
//			}
//		}
//		Num length = PrecisionNum.valueOf(lastIndex-baseBarSeries.getBeginIndex() + 1);
//		Num average = sum.dividedBy(length);
//		return BigDecimal.valueOf(average.doubleValue());
//	}


}
