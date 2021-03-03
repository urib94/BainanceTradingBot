package Positions;
import CodeExecution.RealTImeOperations;
import Data.*;
import SingletonHelpers.BinanceInfo;
import Strategies.ExitStrategy;
import Strategies.RSIStrategies.RSIExitStrategy1;
import Strategies.RSIStrategies.RSIExitStrategy2;
import Strategies.RSIStrategies.RSIExitStrategy3;
import Strategies.RSIStrategies.RSIExitStrategy4;
import Utils.Utils;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;
import com.binance.client.api.model.trade.Position;
import SingletonHelpers.RequestClient;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

public class PositionHandler implements Serializable {
    private final String clientOrderId;
    private final Long orderID;
    private BigDecimal qty = BigDecimal.valueOf(0.0);
    private final String symbol;
    private boolean isActive = false;
    private String status = Config.NEW;
    private final ArrayList<ExitStrategy> exitStrategies;
    private Long baseTime = 0L;

    public PositionHandler(Order order, ArrayList<ExitStrategy> _exitStrategies){//TODO: trying something
        clientOrderId = order.getClientOrderId();
        orderID = order.getOrderId();
        symbol = order.getSymbol().toLowerCase();
        exitStrategies = _exitStrategies;
    }

    public PositionHandler(BigDecimal qty){//TODO: trying something
        clientOrderId = null;
        orderID = null;
        symbol = Config.SYMBOL;
        status = Config.FILLED;
        isActive = true;
        this.qty = qty;
        exitStrategies = new ArrayList<>();
        exitStrategies.add(new RSIExitStrategy1());
        exitStrategies.add(new RSIExitStrategy2());
        exitStrategies.add(new RSIExitStrategy3());
        exitStrategies.add(new RSIExitStrategy4());
    }

    public boolean isSoldOut(){ return isActive && !status.equals(Config.NEW) && (qty.compareTo(BigDecimal.valueOf(0.0)) <= 0);}

    public synchronized void run(RealTimeData realTimeData) {//TODO: adjust to long and short and trailing as exit method
        System.out.println("selling");
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        for (ExitStrategy exitStrategy : exitStrategies) {
            BigDecimal sellingQtyPercentage = exitStrategy.run(realTimeData);
            if (sellingQtyPercentage != null) {
                closePosition();
                String sellingQty = BinanceInfo.formatQty(percentageOfQuantity(sellingQtyPercentage), symbol);
                if (sellingQty.equals("0.000")){
                    sellingQty = "0.001";
                }
                try { //TODO: in case don't succeed selling.
                    Order sellingOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.LIMIT, TimeInForce.GTC,
                            sellingQty, realTimeData.getCurrentPrice().toString(), Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                    System.out.println("Sold order: " + sellingOrder);
                } catch (Exception e) { System.out.println(e);}
            }
        }
    }

    public synchronized void update(CandlestickInterval interval) {
        Position position = AccountBalance.getAccountBalance().getPosition(symbol);
        if (orderID != null) {
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            Order order = syncRequestClient.getOrder(symbol, orderID, clientOrderId);
            status = order.getStatus();
            isActive(order,interval);
        }
        qty = position.getPositionAmt();
    }

    private void isActive(Order order,CandlestickInterval interval) {
        if (status.equals(Config.PARTIALLY_FILLED)){
            Long updateTime = order.getUpdateTime();
            if (baseTime.equals(0L)) {
                baseTime = updateTime;
            } else {
               long difference = updateTime - baseTime;
               Long intervalInMilliSeconds = Utils.candleStickIntervalToMilliseconds(interval);
               if  (difference >= (intervalInMilliSeconds/2.0)) {
                   SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                   syncRequestClient.cancelOrder(symbol,orderID,clientOrderId);
                   isActive = true;
               }
            }
        }
        else { // FULL. since in NEW we don't go in the function.
            isActive = true;
        }
    }

    private BigDecimal percentageOfQuantity(BigDecimal percentage) {
        return qty.multiply(percentage);
    }

    public synchronized void terminate(){
        System.out.println("Terminating");
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);
    }

    private void closePosition(ClosePositionTypes type, BigDecimal sellingQtyPercentage, double trailingPercentage){
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        switch (type) {
            case SELL:






                break;

            case SELL_WITH_TRAILING:






                break;

            case CLOSE_SHORT:








                break;

            case CLOSE_SHORT_WITH_TRAILING:









                break;

            default:
                System.out.println("Wrong closePositionType");
        }

    }

    public enum ClosePositionTypes{
        SELL,
        SELL_WITH_TRAILING,
        CLOSE_SHORT,
        CLOSE_SHORT_WITH_TRAILING;
    }
}
