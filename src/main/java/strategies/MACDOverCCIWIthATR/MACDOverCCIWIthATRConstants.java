package strategies.MACDOverCCIWIthATR;

import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

public class MACDOverCCIWIthATRConstants {
    public static final int FAST_BAR_COUNT = 12;//TODO:change back to 14
    public static final int SLOW_BAR_COUNT = 24;
    public static final double DEFAULT_TAKE_PROFIT_PERCENTAGE = 0.0;
    public static final double DEFAULT_STOP_LOSS_PERCENTAGE = 0.0;
    public static final int DEFAULT_LEVERAGE = 100;
    public static double DEFAULT_BUYING_AMOUNT = 0.74;
    public static final int SIGNAL_LENGTH = 9;
//    public static final int RSI_CANDLE_NUM = 9;
    public static final double MACDOverCCIWIthATR_SELLING_PERCENTAGE = 100;
    public static final double SKIPPINGֹ_TRAILING_PERCENTAGE_BUY = 0.05;
    public static final double NEGATIVE_SKIPINGֹ_TRAILING_PERCENTAGE_BUY=-0.05;
//    public static final double CONSTANT_TRAILING_PERCENTAGE = 0.1;
    public static final double LOWER_PERCENT = 0.0;
    public static final double UPPER_PERCENT = 0.0;
    public static final double STRONG_TRADE_MULTIPLIER = 1;
    public static final double REGULAR_TRADE_MULTIPLIER = 1.0;
    public static final double WEAK_TRADE_MULTIPLIER = 1;
    public static final int CCI_CANDLES = 20;
    public static final int ATR_CANDLE_COUNT = 14;
    public static final int MAX_DCA = 2;
    public static final double AMOUNT_FACTOR = 2;
    public static final double ATR1 = 1.5;
    public static final double ATR2 = 3.1;
    public static final double ATR3 = 6.2;
    public static final boolean USE_TP = true;
    public static final int SMA_CANDLES = 20;
    public static final int STANDARD_DEVIATION_CANDLES = 20;
    public static final Num DEVIATION_MULTIPLIER = PrecisionNum.valueOf(2);
    public static final double STEP_FACTOR = 1.5 ;
    public static final double STEP = 0;
    public static final String SYMBOL ="btcusdt" ;
    public static final double LOWER_BI = 0.0;
    public static final double UPPER_BI = 1.0;
    public static final double CLOSE_ATR1 = 0.5;
    public static final double CLOSE_ATR2 = 1.5;
    public static final double FIRST_DCA_SIZE = DEFAULT_BUYING_AMOUNT*AMOUNT_FACTOR;
    public static final double FEE = 0.018;
    public static final double AVAILABLE_BALANCE_PRECENTAGE = 10;
}
