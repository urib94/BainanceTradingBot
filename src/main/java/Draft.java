import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import singletonHelpers.RequestClient;

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

    private static void postStopLoss(double qty) {
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        String symbol = "ethusdt";
        String sellingQty = String.valueOf(qty);
        String stopLossPrice = String.valueOf(3000);
        try {
            syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.TAKE_PROFIT_MARKET, null,
                    sellingQty, null, "false", null, stopLossPrice, null,
                    null, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        postStopLoss(0.1);
    }
}