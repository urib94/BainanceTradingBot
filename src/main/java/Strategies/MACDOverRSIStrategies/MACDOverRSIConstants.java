package Strategies.MACDOverRSIStrategies;

import java.math.BigDecimal;

public class MACDOverRSIConstants {
    public static final int FAST_BAR_COUNT = 12;
    public static final int SLOW_BAR_COUNT = 24;
    public static final double DEFAULT_TAKE_PROFIT_PERCENTAGE = 0.0;//TODO: change to real values
    public static final double DEFAULT_STOP_LOSS_PERCENTAGE = 0.0;//TODO: change to real values
    public static final int DEFAULT_LEVERAGE = 6;//TODO: change to real values
    public static final BigDecimal DEFAULT_BUYING_AMOUNT = BigDecimal.valueOf(10);//TODO: change to real values
    public static final int SIGNAL_LENGTH = 9;
    public static final int RSI_CANDLE_NUM = 14;
    public static final int SMA_CANDLE_NUM = 150;
    public static final BigDecimal MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE = new BigDecimal(100);
    public static final double POSITIVE_TRAILING_PERCENTAGE = 0.25;
    public static final String LEVERAGED_COIN = "btc";
    public static final String BORROWING_AMOUNT = "15";
    public static final String BASE_COIN = "usdt";
    public static final String BASE_COIN_AMOUNT = "30";
}
