package strategies.macdOverRSIStrategies;

import java.math.BigDecimal;

public class MACDOverRSIConstants {
    public static final int FAST_BAR_COUNT = 14;
    public static final int SLOW_BAR_COUNT = 24;
    public static final double DEFAULT_TAKE_PROFIT_PERCENTAGE = 0.0;//TODO: change to real values
    public static final double DEFAULT_STOP_LOSS_PERCENTAGE = 0.0;//TODO: change to real values
    public static final int DEFAULT_LEVERAGE = 6;//TODO: change to real values
    public static final BigDecimal DEFAULT_BUYING_AMOUNT = BigDecimal.valueOf(10);//TODO: change to real values
    public static final int SIGNAL_LENGTH = 9;
    public static final int RSI_CANDLE_NUM = 9;
    public static final int SMA_CANDLE_NUM = 150;
    public static final BigDecimal MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE = new BigDecimal(100);
    public static final double POSITIVE_TRAILING_PERCENTAGE = 0.2;
    public static final double CONSTANT_TRAILING_PERCENTAGE = 0.3;
    public static final double LONG_EXIT2_OPEN_THRESHOLD = -0.3;
    public static final double SHORT_EXIT2_OPEN_THRESHOLD = 0.3;
}
