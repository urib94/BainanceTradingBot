package strategies.macdOverRSIStrategies;

import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;


public class MACDOverRSIConstants {
    public static final int FAST_BAR_COUNT = 12;//TODO:change back to 14
    public static final int SLOW_BAR_COUNT = 24;
    public static final double DEFAULT_TAKE_PROFIT_PERCENTAGE = 0.0;
    public static final double DEFAULT_STOP_LOSS_PERCENTAGE = 0.0;
    public static final int DEFAULT_LEVERAGE = 100;
    public static final double DEFAULT_BUYING_AMOUNT = 0.6;
    public static final int SIGNAL_LENGTH = 9;
    public static final int RSI_CANDLE_NUM = 9;
    public static final double MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE = 100;
    public static final double POSITIVE_TRAILING_PERCENTAGE = 0.25;
    public static final double CONSTANT_TRAILING_PERCENTAGE = 0.40;
    public static final double LONG_EXIT2_OPEN_THRESHOLD = -0.2;
    public static final double SHORT_EXIT2_OPEN_THRESHOLD = 0.2;
    public static final int STANDARD_DEVIATION_CANDLES = 20;
    public static final Num DEVIATION_MULTIPLIER = PrecisionNum.valueOf(2);
    public static final double PROFIT_TRAILING_PERCENTAGE = 0.10;
    public static final Double EXTREME_LOW_TRAILING_PERCENTAGE = 0.05;
    public static final int SMA_CANDLES = 75;
    public static final int CCIC_CANDLES = 14;
}
