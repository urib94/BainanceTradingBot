package strategies.MAStrategies.BasicMaStrategyBNB;

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
import strategies.MACrosses.MACrossesConstants;
import utils.Utils;

import java.util.ArrayList;
import java.util.Date;

public class BasicMaBNBEntryStrategy implements EntryStrategy {
    AccountBalance accountBalance;
    private double requestedBuyingAmount = MACrossesConstants.DEFAULT_BUYING_AMOUNT;
    PositionHandler positionHandler;
    private int leverage = BasicMaBNBConsts.LEVERAGE;

    public BasicMaBNBEntryStrategy(){
        accountBalance = AccountBalance.getAccountBalance();
        System.out.println("Basic Ma");
    }

    @Override
    public PositionHandler run(DataHolder realTimeData, String symbol) {
        if (positionHandler == null){
            int closeIndex = realTimeData.getLastCloseIndex();
            double currentPrice = realTimeData.getClosePriceAtIndex(closeIndex);
            boolean currentPriceCrossedMaUp = currentPrice > realTimeData.getSlowSmaValueAtIndex(closeIndex) && realTimeData.getClosePriceAtIndex(closeIndex - 1) < realTimeData.getSlowSmaValueAtIndex(closeIndex - 1);
            if (currentPriceCrossedMaUp){
                return buyAndCreatePositionHandler(realTimeData, PositionSide.LONG, symbol);
            }
            boolean currentPriceCrossedMaDown = currentPrice < realTimeData.getSlowSmaValueAtIndex(closeIndex) && realTimeData.getClosePriceAtIndex(closeIndex - 1) > realTimeData.getSlowSmaValueAtIndex(closeIndex - 1);
            if (currentPriceCrossedMaDown){
                return buyAndCreatePositionHandler(realTimeData, PositionSide.SHORT, symbol);
            }

        }
        return null;
    }

    private PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, PositionSide positionSide, String symbol){
        updateBuyingAmount(symbol);
        TelegramMessenger.sendToTelegram("Entering new position " + new Date(System.currentTimeMillis()));
        ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
        exitStrategies.add(new BasicMaBNBExitStrategy1(positionSide));
        exitStrategies.add(new BasicMaBNBExitStrategy2(positionSide, realTimeData.getOpenPrice(realTimeData.getLastCloseIndex())));
        double currentPrice = realTimeData.getCurrentPrice();
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        syncRequestClient.changeInitialLeverage(symbol, leverage);
        String buyingQty = Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
        if (positionSide == PositionSide.LONG) {
            try {
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE, "TRUE", NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought long:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Average Price: " + buyOrder.getAvgPrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
                positionHandler = new PositionHandler(buyOrder, exitStrategies, BasicMaBNBConsts.STOP_LOSS_PERCENTAGE, positionSide);
                return positionHandler;
            } catch (Exception e) {
                TelegramMessenger.sendToTelegram("Exception was thrown" + new Date(System.currentTimeMillis()));
                e.printStackTrace();
            }
        }
        else {
            try {
                Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.MARKET, null,
                        buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE,"TRUE" , NewOrderRespType.RESULT);
                TelegramMessenger.sendToTelegram("bought short:  " + "Side: " + buyOrder.getSide() + " , Qty: " + buyOrder.getCumQty() +
                        " , Average Price: " + buyOrder.getAvgPrice() + " ,                   Time: " + new Date(System.currentTimeMillis()));
                positionHandler = new PositionHandler(buyOrder, exitStrategies, BasicMaBNBConsts.STOP_LOSS_PERCENTAGE, positionSide);
                return positionHandler;
            } catch (Exception e) {
                TelegramMessenger.sendToTelegram("Exception was thrown" + new Date(System.currentTimeMillis()));
                e.printStackTrace();
            }
        }
        return null;
    }


    @Override
    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    @Override
    public void setRequestedBuyingAmount(double requestedBuyingAmount) {
        this.requestedBuyingAmount = requestedBuyingAmount;
    }

    @Override
    public void positionClosed() {
        positionHandler = null;
    }
    private void updateBuyingAmount(String symbol) {
        String baseSymbol = Config.BASE_COIN;
        double balance = accountBalance.getCoinBalance(baseSymbol).doubleValue();
        requestedBuyingAmount = (balance * BasicMaBNBConsts.AVAILABLE_BALANCE_PERCENTAGE) / 100;

    }
}
