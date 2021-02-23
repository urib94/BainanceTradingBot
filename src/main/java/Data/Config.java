package Data;

import com.binance.client.api.model.enums.CandlestickInterval;

import java.math.BigDecimal;

public class Config {

	public static final String API_KEY = "h4AdqbWrKlF5wOxWmbNTKLq7IpDReZOvVXeOujGwUfU0HjXdIejUmqKvoVLbcXJ6";
	public static final String SECRET_KEY = "7qqNLwB7ZXiZu2nBJhrdv7ARVpNDhImNA1Rj1oXR6eGjZFkGsRC9ayEO7HLYVm6w";
	public static final int THREAD_NUM = 3;
	public static final int CANDLE_NUM = 500;
	public static final CandlestickInterval INTERVAL =  CandlestickInterval.ONE_MINUTE;
	public static final int MINUTES_TO_MILLISECONDS_CONVERTER = 60000;
	public static final int HOURS_TO_MILLISECONDS_CONVERTER = 60 * MINUTES_TO_MILLISECONDS_CONVERTER;
	public static final int DAYS_TO_MILLISECONDS_CONVERTER = 24 * HOURS_TO_MILLISECONDS_CONVERTER;
	public static final int WEEKS_TO_MILLISECONDS_CONVERTER = 7 * DAYS_TO_MILLISECONDS_CONVERTER;
	public static final int MONTHS_TO_MILLISECONDS_CONVERTER = 30 * DAYS_TO_MILLISECONDS_CONVERTER;
	public static final int THIRTY_MINUTES_IN_MILLISECONDS = 30 * MINUTES_TO_MILLISECONDS_CONVERTER;
	public static final int RSI_ENTRY_THRESHOLD_1 = 27;
	public static final int RSI_ENTRY_THRESHOLD_2 = 30;
	public static final int RSI_ENTRY_THRESHOLD_3 = 35;
	public static final int RSI_CANDLE_NUM = 9;
	public static final Integer LEVERAGE = 6;
	public static final int RSI_EXIT_OPTION_1_OVER_THRESHOLD1 = 65;
	public static final int RSI_EXIT_OPTION_1_UNDER_THRESHOLD1 = 65;
	public static final int RSI_EXIT_OPTION_1_UNDER_THRESHOLD2 = 50;
	public static final int RSI_EXIT_OPTION_2_OVER_THRESHOLD1 = 73;
	public static final int RSI_EXIT_OPTION_2_UNDER_THRESHOLD1 = 70;
	public static final int RSI_EXIT_OPTION_2_UNDER_THRESHOLD2 = 60;
	public static final int RSI_EXIT_OPTION_4_UNDER_THRESHOLD = 30;
	public static final BigDecimal RSI_EXIT_OPTION_1_SELLING_PERCENTAGE = new BigDecimal(50);
	public static final BigDecimal RSI_EXIT_OPTION_2_SELLING_PERCENTAGE1 = new BigDecimal(40);
	public static final BigDecimal RSI_EXIT_OPTION_2_SELLING_PERCENTAGE2 = new BigDecimal(60);
	public static final BigDecimal RSI_EXIT_OPTION_3_SELLING_PERCENTAGE = new BigDecimal(100);
	public static final BigDecimal RSI_EXIT_OPTION_4_SELLING_PERCENTAGE = new BigDecimal(100);
	public static final BigDecimal BUYING_AMOUNT_REQUESTED = new BigDecimal(1/10);
	public static final double ALPHA = 1.0/RSI_CANDLE_NUM;
	public static final String NEW = "NEW";
	public static final String PARTIALLY_FILLED = "PARTIALLY_FILLED";
	public static final String FILLED = "FILLED";
	public static final String CANCELED = "CANCELED";
	public static final String EXPIRED = "EXPIRED";


	public static final int ZERO = 0;
    public static final String SYMBOL = "btcusdt";
}