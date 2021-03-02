package Strategies.MACDOverRSIStrategies;

import java.math.BigDecimal;

public class MACDOverRSIConstants {
    public static final int FAST_BAR_COUNT = 12;
    public static final int SLOW_BAR_COUNT = 26;
    public static final double DEFAULT_TAKE_PROFIT_PERCENTAGE = 0.0;//TODO: change to real values
    public static final double DEFAULT_STOP_LOSS_PERCENTAGE = 0.0;//TODO: change to real values
    public static final int DEFAULT_LEVERAGE = 0;//TODO: change to real values
    public static final BigDecimal DEFAULT_BUYING_AMOUNT = BigDecimal.valueOf(0.0);//TODO: change to real values
    public static final int SIGNAL_LENGTH = 9;
    public static final int RSI_CANDLE_NUM = 14;
    public static final int MACD_OVER_RSI_ENTRY_THRESHOLD = 0;
    public static final int MACD_OVER_RSI_EXIT_THRESHOLD = 0;
    public static final BigDecimal MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE = new BigDecimal(100);

}
