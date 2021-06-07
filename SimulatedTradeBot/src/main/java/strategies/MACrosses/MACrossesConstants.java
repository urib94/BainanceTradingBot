package main.java.strategies.MACrosses;

import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

public class MACrossesConstants {
    public static final int RSI_CANDLE_NUM = 50;
    public static final int SLOW_SMA_OVER_RSI_BAR_COUNT = 15;
    public static final int FAST_SMA_OVER_RSI_BAR_COUNT = 9;
    public static final int MFI_BAR_COUNT = 14;
    public static final int SMA_OVER_MFI_BAR_COUNT = 9;
    public static final int FAST_SMA_BAR_COUNT = 20;
    public static final int SLOW_SMA_BAR_COUNT = 85;
    public static final double AVAILABLE_BALANCE_PERCENTAGE = 1;
    public static final double EXIT_SELLING_PERCENTAGE = 100;
    public static final double SLOW_SKIPPING_TRAILING_PERCENTAGE = 0.35;
    public static final double FAST_SKIPPING_TRAILING_PERCENTAGE = 0.15;
    public static final double TRAILING_PERCENTAGE = 0.04;
    public static final double DEFAULT_BUYING_AMOUNT = 0;
    public static final int LEVERAGE = 100;
    public static final double EXIT_THRESHOLD = 0.5;
    public static final double ENTRY_THRESHOLD = 1;
    public static final double  OVER_SOLD_THRESHOLD = 60;


    public static final int SMA_OVER_VOLUME_BAR_COUNT = 10;
    public static final double STOP_LOSS_PERCENTAGE = 0.35;
    public static final Num DEVIATION_MULTIPLIER = PrecisionNum.valueOf(2);
    public static final Num CLOSE_DEVIATION_MULTIPLIER = PrecisionNum.valueOf(1);
    public static final int STANDARD_DEVIATION_CANDLES = 20;
    public static final double UPPER_BANNED_ZONE_THRESHOLD = 50;
    public static final double LOWER_BANNED_ZONE_THRESHOLD = 45;
}
    