package Utils;

import Strategies.EntryStrategy;
import Strategies.RSIStrategies.RSIEntryStrategy;
import com.binance.client.api.model.enums.CandlestickInterval;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

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

	public static EntryStrategy stringToEntryStrategy(String strategyName) {
		switch (strategyName) {
			case "rsiStrategy":
				return new RSIEntryStrategy();

			default:
				return null;
		}
	}
	private static ZonedDateTime getZonedDateTime(Long timestamp) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
				ZoneId.systemDefault());
	}

	public static ClosePriceIndicator diffByElementBetweenIndicators(Indicator<Num> indicator1, Indicator<Num> indicator2, int length) {
		BaseBarSeries baseBarSeries = new BaseBarSeries();
		Long timestamp = 1L;
		for (int i = 0; i < length; i++) {
			Double currentDiff = (indicator1.getValue(i).minus(indicator2.getValue(i))).doubleValue();
			baseBarSeries.addBar(Duration.ofMillis(7L),getZonedDateTime(timestamp++),1,2,3,currentDiff,4,5);
		}
		return new ClosePriceIndicator(baseBarSeries);
	}
}
