import TradingTools.Trailers.SkippingExitTrailer;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.Order;
import strategies.MACrosses.MACrossesConstants;

public class Draft {


    private Order orderDCA, orderTP;
    private double step;
    private double dCASize = 0;
    private int dCACount = 0;
    private double nextDCAPrice;
    private double amount;
    private boolean initialize = true;
    private int dcaOrderCheckCounter = 0;
    private double averagePrice;

    private Long timeToExit;
    private final Long timeProtection = 10000L;



    public static void main(String[] args) {

        SkippingExitTrailer skippingExitTrailer = new SkippingExitTrailer(MACrossesConstants.FAST_SKIPPING_TRAILING_PERCENTAGE, PositionSide.LONG);


    }
}