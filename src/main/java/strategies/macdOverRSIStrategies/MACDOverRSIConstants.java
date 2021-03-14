package strategies.macdOverRSIStrategies;

import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;


public class MACDOverRSIConstants {
    public static final int FAST_BAR_COUNT = 14;
    public static final int SLOW_BAR_COUNT = 24;
    public static final double DEFAULT_TAKE_PROFIT_PERCENTAGE = 0.0;
    public static final double DEFAULT_STOP_LOSS_PERCENTAGE = 0.0;
    public static final int DEFAULT_LEVERAGE = 60;
    public static final double DEFAULT_BUYING_AMOUNT = 5;
    public static final int SIGNAL_LENGTH = 9;
    public static final int RSI_CANDLE_NUM = 9;
    public static final double MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE = 100;
    public static final double POSITIVE_TRAILING_PERCENTAGE = 0.2;
    public static final double CONSTANT_TRAILING_PERCENTAGE = 0.35;
    public static final double LONG_EXIT2_OPEN_THRESHOLD = -0.2;
    public static final double SHORT_EXIT2_OPEN_THRESHOLD = 0.2;
    public static final int STANDARD_DEVIATION_CANDLES = 20;
    public static final Num DEVIATION_MULTIPLIER = PrecisionNum.valueOf(2);
    public static final Num SAR_START = PrecisionNum.valueOf(0.02);
    public static final Num MAX_VALUE = PrecisionNum.valueOf(0.2);
    public static final Num INCREMENT = PrecisionNum.valueOf(0.02);
    public static final double PROFIT_TRAILING_PERCENTAGE = 0.25;
}
