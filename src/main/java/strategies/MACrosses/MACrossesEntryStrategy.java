package strategies.MACrosses;

import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import data.AccountBalance;
import data.Config;
import data.DataHolder;
import positions.PositionHandler;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.EntryStrategy;
import strategies.ExitStrategy;
import strategies.MACDOverCCIWIthATR.MACDOverCCIWIthATRConstants;
import strategies.MACrosses.Long.MACroosesLongExitStrategy2;
import strategies.MACrosses.Long.MACrossesLongExitStrategy1;
import strategies.MACrosses.Short.MACroosesShortExitStrategy2;
import strategies.MACrosses.Short.MACrossesShortExitStrategy1;
import utils.Utils;

import java.util.ArrayList;
import java.util.Date;

public class MACrossesEntryStrategy implements EntryStrategy {
    AccountBalance accountBalance;
    PositionHandler positionHandler;
    int leverage = MACrossesConstants.LEVERAGE;
    DataHolder dataHolder = null;


    public MACrossesEntryStrategy(){
        accountBalance = AccountBalance.getAccountBalance();
        System.out.println("MFI & RSI SMA Crosses");
    }


    @Override
    public PositionHandler run(DataHolder realTimeData, String symbol) {
        if (dataHolder == null){
            dataHolder = realTimeData;
        }
        double currentPrice = dataHolder.getCurrentPrice();
        if(priceISAboveSMA()){ // long only
            if(RSICrosseSMAUp())
        }


        return null;
    }

    private boolean RSICrosseSMAUp() {
        double prevRSIValue = dataHolder.getRSIValueAtIndex(dataHolder.getLastCloseIndex() - 1);
        double
        return dataHolder.crossed()
    }

    private boolean priceISAboveSMA() {
        double sMaValue = dataHoler.getSmaValueAtIndex(dataHolder.getLastCloseIndex());
        return dataHolder.above(DataHolder.IndicatorType.SMA, DataHolder.CandleType.CLOSE, sMaValue);
    }


    private PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, double currentPrice, String symbol, PositionSide positionSide) {
        updateBuyingAmount(symbol);
        double requestedBuyingAmount = MACrossesConstants.DEFAULT_BUYING_AMOUNT;
        TelegramMessenger.sendToTelegram("Entering new position " + new Date(System.currentTimeMillis()));
        ArrayList <ExitStrategy> exitStrategies = new ArrayList<>();
//        updateBuyingAmount(symbol);
        if (positionSide == PositionSide.LONG) {
            try {
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol, leverage);
                String buyingQty = Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE, "TRUE", NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought long:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Activate Price: " + buyOrder.getActivatePrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
                exitStrategies.add(new MACrossesLongExitStrategy1());
                exitStrategies.add(new MACroosesLongExitStrategy2());
                positionHandler = new PositionHandler(buyOrder, exitStrategies);
                return positionHandler;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                syncRequestClient.changeInitialLeverage(symbol, leverage);
                String buyingQty = Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.MARKET, null,
                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE,"TRUE" , NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought long:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Activate Price: " + buyOrder.getActivatePrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
                exitStrategies.add(new MACrossesShortExitStrategy1());
                exitStrategies.add(new MACroosesShortExitStrategy2());
                positionHandler = new PositionHandler(buyOrder, exitStrategies);
                return positionHandler;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void updateBuyingAmount(String symbol) {
        String baseSymbol = Config.BASE_COIN;
        double balance = accountBalance.getCoinBalance(baseSymbol).doubleValue();
        double maxAmount = 1;
        for (int i = 0 ; i < MACDOverCCIWIthATRConstants.MAX_DCA ; i++){
            maxAmount += 2 * maxAmount;
        }
        MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT = (balance / maxAmount) * (MACDOverCCIWIthATRConstants.AVAILABLE_BALANCE_PRECENTAGE / 100);

    }

    @Override
    public void setTakeProfitPercentage(double takeProfitPercentage) {

    }

    @Override
    public void setStopLossPercentage(double stopLossPercentage) {

    }

    @Override
    public void setLeverage(int leverage) {

    }

    @Override
    public void setRequestedBuyingAmount(double requestedBuyingAmount) {

    }

    @Override
    public void positionClosed() {

    }
}
