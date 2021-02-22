package utilities;

import Data.Config;
import Data.RealTimeData;
import com.binance.client.api.model.market.Candlestick;
import com.sun.org.apache.xpath.internal.operations.Minus;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.GainIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;

public class RSIUtiles {
	public static boolean aboveThreshold(BigDecimal value, int threshold) {
		return value.compareTo(new BigDecimal(threshold)) > 0;
	}
	public static boolean belowThreshold(BigDecimal value, int threshold) {
		return value.compareTo(new BigDecimal(threshold)) < 0;
	}
	public static BigDecimal rsiStepTwoCalculatorForClosed(RealTimeData realTimeData) {
		BaseBarSeries baseBarSeries = realTimeData.getLastAmountOfCandles(Config.RSI_CANDLE_NUM);
		return calculateRsi(baseBarSeries);
	}
	public static BigDecimal rsiStepTwoCalculatorForOpen(RealTimeData realTimeData) {
		BaseBarSeries baseBarSeries = realTimeData.getLastAmountOfClosedCandles(Config.RSI_CANDLE_NUM);
		return calculateRsi(baseBarSeries);
	}
	private static BigDecimal calculateRsi(BaseBarSeries baseBarSeries) {
		ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(baseBarSeries);
		BigDecimal averageGainNotIncludingLastOne = calculateAverageGain(baseBarSeries, baseBarSeries.getEndIndex()-1);
		BigDecimal averageLossNotIncludingLastOne = calculateAverageLoss(baseBarSeries, baseBarSeries.getEndIndex()-1);
		BigDecimal currentGain = calculateCurrentGain(baseBarSeries);
		BigDecimal currentLoss = calculateCurrentLoss(baseBarSeries);
		BigDecimal mone = BigDecimal.valueOf(100);
		BigDecimal moneOfMehane = (averageGainNotIncludingLastOne.multiply(BigDecimal.valueOf(13))).add(currentGain);
		BigDecimal mehaneOfMehane = (averageLossNotIncludingLastOne.multiply(BigDecimal.valueOf(13))).add(currentLoss);
		mehaneOfMehane = mehaneOfMehane.multiply(BigDecimal.valueOf(-1));
		BigDecimal mehane = (moneOfMehane.divide(mehaneOfMehane,Config.ROUNDING_MODE)).add(BigDecimal.ONE);
		BigDecimal fraction = mone.divide(mehane,Config.ROUNDING_MODE);
		return BigDecimal.valueOf(100).subtract(fraction);
	}

	private static BigDecimal calculateCurrentGain(BaseBarSeries baseBarSeries) {
		int lastIndex = baseBarSeries.getBeginIndex();
		Num candleLastClosedPrice = baseBarSeries.getBar(lastIndex).getClosePrice();
		Num candlePrevLastClosedPrice = baseBarSeries.getBar(lastIndex-1).getClosePrice();
		if (candleLastClosedPrice.isGreaterThan(candlePrevLastClosedPrice)) {
			Num gain = candleLastClosedPrice.minus(candlePrevLastClosedPrice);
			return BigDecimal.valueOf(gain.doubleValue());
		}
		return BigDecimal.ZERO;
	}

	private static BigDecimal calculateCurrentLoss(BaseBarSeries baseBarSeries) {
		int lastIndex = baseBarSeries.getBeginIndex();
		Num candleLastClosedPrice = baseBarSeries.getBar(lastIndex).getClosePrice();
		Num candlePrevLastClosedPrice = baseBarSeries.getBar(lastIndex-1).getClosePrice();
		if (candleLastClosedPrice.isLessThan(candlePrevLastClosedPrice)) {
			Num gain = candlePrevLastClosedPrice.minus(candleLastClosedPrice);
			return BigDecimal.valueOf(gain.doubleValue());
		}
		return BigDecimal.ZERO;
	}

	private static BigDecimal calculateAverageGain(BaseBarSeries baseBarSeries, int lastIndex) {
		DoubleNum sum = DoubleNum.valueOf(0);
		for (int i = 1; i <= lastIndex; i++) {
			Bar candlePrev = baseBarSeries.getBar(i-1);
			Bar candleNow = baseBarSeries.getBar(i);
			Num candlePrevClosePrice = candlePrev.getClosePrice();
			Num candleNowClosedPrice = candleNow.getClosePrice();
			if (candleNowClosedPrice.isGreaterThan(candlePrevClosePrice)) {
				sum = (DoubleNum) sum.plus(candleNowClosedPrice.minus(candlePrevClosePrice));
			}
		}
		Num average = sum.dividedBy(DoubleNum.valueOf(lastIndex+1));
		return BigDecimal.valueOf(average.doubleValue());
	}
	private static BigDecimal calculateAverageLoss(BaseBarSeries baseBarSeries, int lastIndex) {
		DoubleNum sum = DoubleNum.valueOf(0);
		for (int i = 1; i <= lastIndex; i++) {
			Bar candlePrev = baseBarSeries.getBar(i-1);
			Bar candleNow = baseBarSeries.getBar(i);
			Num candlePrevClosePrice = candlePrev.getClosePrice();
			Num candleNowClosedPrice = candleNow.getClosePrice();
			if (candleNowClosedPrice.isLessThan(candlePrevClosePrice)) {
				sum = (DoubleNum) sum.plus(candlePrevClosePrice.minus(candleNowClosedPrice));
			}
		}
		Num average = sum.dividedBy(DoubleNum.valueOf(lastIndex+1));
		return BigDecimal.valueOf(average.doubleValue());
	}


}
