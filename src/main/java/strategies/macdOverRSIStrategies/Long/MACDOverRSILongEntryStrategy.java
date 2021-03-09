package strategies.macdOverRSIStrategies.Long;

import data.AccountBalance;
import data.Config;
import data.RealTimeData;
import positions.PositionHandler;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.ExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseEntryStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import utils.Trailer;
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
    private final AccountBalance accountBalance;
    private volatile boolean bought = false;

    public MACDOverRSILongEntryStrategy(){
        accountBalance = AccountBalance.getAccountBalance();
        System.out.println("long");
    }


    @Override
    public synchronized PositionHandler run(RealTimeData realTimeData, String symbol) {
        boolean notInPosition = accountBalance.getPosition(symbol).getPositionAmt().compareTo(BigDecimal.valueOf(Config.DOUBLE_ZERO)) == Config.ZERO;
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        boolean noOpenOrders = syncRequestClient.getOpenOrders(symbol).size() == Config.ZERO;
        BigDecimal currentPrice = realTimeData.getCurrentPrice();
        boolean  currentPriceBelowUpperBollinger = BigDecimal.valueOf(realTimeData.getUpperBollingerAtIndex(MACDOverRSIConstants.LAST_INDEX)).compareTo(currentPrice) > Config.ZERO;
        boolean currentPriceAboveSMA = BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(MACDOverRSIConstants.LAST_INDEX)).compareTo(currentPrice) < Config.ZERO;
        if (currentPriceBelowUpperBollinger && currentPriceAboveSMA && notInPosition && noOpenOrders) {
            boolean rule1 = realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.UP,RealTimeData.CandleType.CLOSE,Config.ZERO);
            if (rule1){
                if (bought)return null;
                return buyAndCreatePositionHandler(currentPrice,symbol);
            }
            else {
                boolean macdValueBelowZero = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_INDEX) < Config.ZERO;
                if (macdValueBelowZero && decliningPyramid(realTimeData, DecliningType.NEGATIVE)){
                    if (bought)return null;
                    return buyAndCreatePositionHandler(currentPrice,symbol);
                }
            }
            bought = false;
        }
        return null;
    }

    private PositionHandler buyAndCreatePositionHandler(BigDecimal currentPrice, String symbol) {//TODO: maybe change market later.
        bought = true;
        TelegramMessenger.sendToTelegram("buying long: " + new Date(System.currentTimeMillis()));
        try{
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.changeInitialLeverage(symbol,leverage);
            String buyingQty = utils.Utils.getBuyingQtyAsString(currentPrice, symbol,leverage,requestedBuyingAmount);
            Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.LIMIT, TimeInForce.GTC,
                    buyingQty,currentPrice.toString(),null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
            TelegramMessenger.sendToTelegram("buying long: buyOrder: "+ buyOrder.toTelegram() + new Date(System.currentTimeMillis()));
            ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
            //exitStrategies.add(new MACDOverRSILongExitStrategy1());
            exitStrategies.add(new MACDOverRSILongExitStrategy2());
            exitStrategies.add(new MACDOverRSILongExitStrategy3(new Trailer(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.LONG)));
            exitStrategies.add(new MACDOverRSILongExitStrategy4(new Trailer(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.LONG)));
            exitStrategies.add(new MACDOverRSILongExitStrategy5(new Trailer(currentPrice, MACDOverRSIConstants.CONSTANT_TRAILING_PERCENTAGE, PositionSide.LONG)));
            exitStrategies.add(new MACDOverRSILongExitStrategy6());
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
