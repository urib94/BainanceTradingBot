package Positions;
import Data.*;
import Strategies.ExitStrategy;
import Utils.TimeConstants;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;
import com.binance.client.api.model.trade.Position;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PositionHandler {
    private final String clientOrderId;
    private final Long orderID;
    private BigDecimal qty;
    private final String symbol;
    private String side;
    private BigDecimal purchasePrice;
    private boolean isActive;
    private String status;
    private BigDecimal pNLPercentage;
    private BigDecimal pNL;
    private BigDecimal liquidationPrice;
    private BigDecimal distanceToLiquidation;
    private BigDecimal leverage;
    private ArrayList<ExitStrategy> exitStrategies;
    private Long baseTime;

    public PositionHandler(Order order, Integer leverage, ArrayList<ExitStrategy> exitStrategies){
        clientOrderId = order.getClientOrderId();
        orderID = order.getOrderId();
        qty = BigDecimal.ZERO;
        symbol = order.getSymbol();
        side = order.getSide();
        purchasePrice = order.getPrice();
        isActive = false;
        pNL = BigDecimal.ZERO;
        pNLPercentage = BigDecimal.ZERO;
        liquidationPrice = BigDecimal.ZERO;
        distanceToLiquidation = BigDecimal.ZERO;
        this.leverage = new BigDecimal(leverage);
        this.exitStrategies = exitStrategies;
        status = Config.NEW;
        this.baseTime = 0L;
    }

    public boolean isSoldOut(){ return isActive && (qty.compareTo(BigDecimal.ZERO) <= 0);}

    public void run(RealTimeData realTimeData){
        for (ExitStrategy exitStrategy: exitStrategies){
            BigDecimal sellingQtyPercentage  = exitStrategy.run(realTimeData);
            if (sellingQtyPercentage != null){
                String sellingQty = BinanceInfo.formatQty(percentageOfQuantity(sellingQtyPercentage), symbol);
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.postOrder(symbol,OrderSide.SELL, PositionSide.LONG, OrderType.LIMIT, TimeInForce.GTC,
                      sellingQty,realTimeData.getCurrentPrice().toString(),"true",null, null, null, NewOrderRespType.RESULT);
            }
        }
    }

    public void update(CandlestickInterval interval) {
        Position position = AccountBalance.getAccountBalance().getPosition(symbol);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        Order order = syncRequestClient.getOrder(symbol, orderID , clientOrderId);
        status = order.getStatus();
        pNL = position.getUnrealizedProfit();
        qty = position.getPositionAmt();
        if (!status.equals(Config.NEW)) isActive(order,interval);
    }

    private void isActive(Order order,CandlestickInterval interval) {
        if (status.equals(Config.PARTIALLY_FILLED)){
            Long updateTime = order.getUpdateTime();
            if (baseTime.equals(0L)) {
                baseTime = updateTime;
            } else {
               long difference = updateTime - baseTime;
               Long intervalInMilliSeconds = candleStickIntervalToMilliseconds(interval);
               if  (difference >= (intervalInMilliSeconds/2.0)) {
                   SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                   syncRequestClient.cancelOrder(symbol,orderID,clientOrderId);
                   isActive = true;
               }
            }
        }
        else {
            isActive = true;
        }
    }

    private BigDecimal percentageOfQuantity(BigDecimal percentage) {
        return qty.multiply(percentage);
    }

    private Long candleStickIntervalToMilliseconds(CandlestickInterval interval) {
        String intervalCode = interval.toString();
        int value = Integer.parseInt(intervalCode.substring(0,intervalCode.length()-1));
        char typeOfTime = intervalCode.charAt(intervalCode.length()-1);
        switch (typeOfTime) {
            case 'm':
                return (long) value * TimeConstants.MINUTES_TO_MILLISECONDS_CONVERTER;
            case 'h':
                return (long) value * TimeConstants.HOURS_TO_MILLISECONDS_CONVERTER;
            case 'd':
                return (long) value * TimeConstants.DAYS_TO_MILLISECONDS_CONVERTER;
            case 'w':
                return (long) value * TimeConstants.WEEKS_TO_MILLISECONDS_CONVERTER;
            case 'M':
                return (long) value * TimeConstants.MONTHS_TO_MILLISECONDS_CONVERTER;
            default:
                return -1L;
        }
    }
}
