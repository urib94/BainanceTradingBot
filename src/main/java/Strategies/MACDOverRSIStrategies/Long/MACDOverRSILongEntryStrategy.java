package Strategies.MACDOverRSIStrategies.Long;

import Data.AccountBalance;
import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import SingletonHelpers.RequestClient;
import SingletonHelpers.TelegramMessenger;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseEntryStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class MACDOverRSILongEntryStrategy extends MACDOverRSIBaseEntryStrategy {

    double takeProfitPercentage = MACDOverRSIConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
    private double stopLossPercentage = MACDOverRSIConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
    private int leverage = MACDOverRSIConstants.DEFAULT_LEVERAGE;
    private  BigDecimal requestedBuyingAmount = MACDOverRSIConstants.DEFAULT_BUYING_AMOUNT;

    @Override
    public synchronized PositionHandler run(RealTimeData realTimeData, String symbol) {
        boolean notInPosition = AccountBalance.getAccountBalance().getPosition(symbol).getPositionAmt().compareTo(BigDecimal.valueOf(Config.DOUBLE_ZERO)) == 0;
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        boolean noOpenOrders = syncRequestClient.getOpenOrders(symbol).size() == Config.ZERO;
        boolean currentPriceAboveSMA = BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(realTimeData.getLastIndex())).compareTo(realTimeData.getCurrentPrice()) < Config.ZERO;
        if (currentPriceAboveSMA && notInPosition && noOpenOrders) {
            boolean rule1 = realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.UP,RealTimeData.CandleType.CLOSE,Config.ZERO);
            if (rule1) return buyAndCreatePositionHandler(realTimeData,symbol);
            else {
                boolean macdValueBelowZero = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) < 0;
                if (macdValueBelowZero && decliningPyramid(realTimeData, DecliningType.NEGATIVE)) return buyAndCreatePositionHandler(realTimeData,symbol);
            }
        }
        return null;
    }

    private PositionHandler buyAndCreatePositionHandler(RealTimeData realTimeData, String symbol) {//TODO: maybe change market later.
        TelegramMessenger.sendToTelegram("buying long: " + new Date(System.currentTimeMillis()));
        try{
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.changeInitialLeverage(symbol,leverage);
            String buyingQty = Utils.Utils.getBuyingQtyAsString(realTimeData, symbol,leverage,requestedBuyingAmount);
            Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.LIMIT, TimeInForce.GTC,
                    buyingQty,realTimeData.getCurrentPrice().toString(),null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
            TelegramMessenger.sendToTelegram("buying long: buyOrder: "+ buyingQty + " " + new Date(System.currentTimeMillis()));
            ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
            exitStrategies.add(new MACDOverRSILongExitStrategy1());
            exitStrategies.add(new MACDOverRSILongExitStrategy2());
            exitStrategies.add(new MACDOverRSILongExitStrategy3());
            exitStrategies.add(new MACDOverRSILongExitStrategy4());
            return new PositionHandler(buyOrder ,exitStrategies);
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void setTakeProfitPercentage(double takeProfitPercentage) {
        this.takeProfitPercentage =takeProfitPercentage;
    }

    @Override
    public void setStopLossPercentage(double stopLossPercentage) {
        this.stopLossPercentage = stopLossPercentage;
    }

    @Override
    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    @Override
    public void setRequestedBuyingAmount(BigDecimal requestedBuyingAmount) {
        this.requestedBuyingAmount = requestedBuyingAmount;
    }
}
