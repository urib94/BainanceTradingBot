package Utils;

import Data.Config;
import Data.RealTimeData;
import SingletonHelpers.BinanceInfo;
import Strategies.EntryStrategy;
import Strategies.RSIStrategies.RSIEntryStrategy;
import com.binance.client.api.model.enums.CandlestickInterval;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.*;
import java.util.ArrayList;

public class Utils {
	public static Long candleStickIntervalToMilliseconds(CandlestickInterval interval) {
		String intervalCode = interval.toString();
		int value = Integer.parseInt(intervalCode.substring(0,intervalCode.length()-1));
		char typeOfTime = intervalCode.charAt(intervalCode.length()-1);
		switch (typeOfTime) {
			case 'm':
				return (long) value * TimeConstants.MINUTES_TO_MILLISECONDS_CONVERTER;
			case 'h':
				return (long) value * TimeConstants.HOURS_TO_MILLISECONDS_CONVERTER;
			case 'd':
				return (long) value * TimeConstants.DAYS_TO_MILLISECONDS_CONVERTER;
			case 'w':
				return (long) value * TimeConstants.WEEKS_TO_MILLISECONDS_CONVERTER;
			case 'M':
				return (long) value * TimeConstants.MONTHS_TO_MILLISECONDS_CONVERTER;
			default:
				return -1L;
		}
	}

	public static ZonedDateTime getZonedDateTime(Long timestamp) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
				ZoneId.systemDefault());
	}

	public static ClosePriceIndicator diffByElementBetweenIndicators(Indicator<Num> indicator1, Indicator<Num> indicator2, int length) {
		BaseBarSeries baseBarSeries = new BaseBarSeries();
		long timestamp = 1L;
		for (int i = 0; i < length; i++) {
			Double currentDiff = (indicator1.getValue(i).minus(indicator2.getValue(i))).doubleValue();
			baseBarSeries.addBar(Duration.ofMillis(7L),getZonedDateTime(timestamp++),1,2,3,currentDiff,4,5);
		}
		return new ClosePriceIndicator(baseBarSeries);
	}
	public static String getBuyingQtyAsString(RealTimeData realTimeData, String symbol, int leverage, BigDecimal requestedBuyingAmount) {
		BigDecimal buyingQty = requestedBuyingAmount.multiply(BigDecimal.valueOf(leverage)).divide(realTimeData.getCurrentPrice(), MathContext.DECIMAL32);
		return fixQuantity(BinanceInfo.formatQty(buyingQty, symbol));
	}

	public static String getTakeProfitPriceAsString(RealTimeData realTimeData, String symbol, double takeProfitPercentage) {
		BigDecimal takeProfitPrice = realTimeData.getCurrentPrice().add((realTimeData.getCurrentPrice().multiply(BigDecimal.valueOf(takeProfitPercentage))));
		return BinanceInfo.formatPrice(takeProfitPrice, symbol);
	}

	public static String getStopLossPriceAsString(RealTimeData realTimeData, String symbol, double stopLossPercentage) {
		BigDecimal stopLossPrice = realTimeData.getCurrentPrice().subtract(realTimeData.getCurrentPrice().multiply(BigDecimal.valueOf(stopLossPercentage)));
		return BinanceInfo.formatPrice(stopLossPrice, symbol);
	}

	public static String fixQuantity(String amt) {
		if (Double.parseDouble(amt) == 0) {
			amt = amt.substring(0, amt.length()-1).concat("1");
		}
		return amt;
	}
}
