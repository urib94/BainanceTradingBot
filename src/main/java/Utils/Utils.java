package Utils;

import com.binance.client.api.model.enums.CandlestickInterval;

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
}
