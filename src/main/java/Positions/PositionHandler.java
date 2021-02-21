package Positions;
import Data.AccountBalance;
import Data.Config;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import com.binance.client.api.RequestOptions;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;
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

    public PositionHandler(Order order, BigDecimal leverage, ArrayList<ExitStrategy> exitStrategies){
        clientOrderId = order.getClientOrderId();
        orderID = order.getOrderId();
        qty = BigDecimal.ZERO; //order.getOrigQty(); TODO: originalQty or executedQty
        symbol = order.getSymbol();
        side = order.getSide();
        purchasePrice = order.getPrice();
        isActive = false;
        pNL = BigDecimal.ZERO;
        pNLPercentage = BigDecimal.ZERO;
        liquidationPrice = BigDecimal.ZERO;
        distanceToLiquidation = BigDecimal.ZERO;
        this.leverage = leverage;
        this.exitStrategies = exitStrategies;
        status = Config.NEW;
        this.baseTime = 0L;
    }

    public synchronized boolean isSoldOut(){ return isActive && (qty.compareTo(BigDecimal.ZERO) <= 0);}

    public synchronized void run(RealTimeData realTimeData){
        for (ExitStrategy exitStrategy: exitStrategies){
            PositionAction positionAction = exitStrategy.run(realTimeData);
            if (positionAction != null){
                //TODO: check exit strategies and sell if needed.
            }
        }
    }

    public synchronized void update(CandlestickInterval interval) {
        Position position = AccountBalance.getAccountBalance().getPosition(symbol);
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(Config.API_KEY, Config.SECRET_KEY, options);
        Order order = syncRequestClient.getOrder(symbol, orderID , clientOrderId);
        status = order.getStatus();
        pNL = position.getUnrealizedProfit();
        qty = position.getPositionAmt();
        if (!status.equals(Config.NEW)) isActive(order,interval);
        //TODO: complete this function and change isActive
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
                   //TODO: cancel order!
                   isActive = true;
               }
            }
        }
        else {
            isActive = true;
        }
    }
    private Long candleStickIntervalToMilliseconds(CandlestickInterval interval) {
        String intervalCode = interval.toString();
        int value = Integer.parseInt(intervalCode.substring(0,intervalCode.length()-1));
        char typeOfTime = intervalCode.charAt(intervalCode.length()-1);
        switch (typeOfTime) {
            case 'm':
                return (long) value * Config.MINUTES_TO_MILLISECONDS_CONVERTER;
            case 'h':
                return (long) value * Config.HOURS_TO_MILLISECONDS_CONVERTER;
            case 'd':
                return (long) value * Config.DAYS_TO_MILLISECONDS_CONVERTER;
            case 'w':
                return (long) value * Config.WEEKS_TO_MILLISECONDS_CONVERTER;
            case 'M':
                return (long) value * Config.MONTHS_TO_MILLISECONDS_CONVERTER;
            default:
                return -1L;
        }
    }
}
