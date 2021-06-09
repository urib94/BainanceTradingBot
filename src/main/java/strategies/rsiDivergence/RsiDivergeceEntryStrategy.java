package strategies.rsiDivergence;

import TradingTools.Trailers.SkippingExitTrailer;
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
import utils.Utils;

import java.util.ArrayList;
import java.util.Date;

public class RsiDivergeceEntryStrategy implements EntryStrategy {
    private PositionHandler positionHandler;
    private AccountBalance accountBalance;
    private double requestedBuyingAmount = RsiDivergeceConstance.BUYING_AMOUNT;
    private int leverage = RsiDivergeceConstance.LEVRAGE;
    private boolean firstTrade = true;
    private boolean freeToTrade = true;

    public RsiDivergeceEntryStrategy(){
        accountBalance = AccountBalance.getAccountBalance();
        System.out.println("Rsi Divergence");
    }

    @Override
    public PositionHandler run(DataHolder realTimeData, String symbol) {
        if (firstTrade){
            if(devCrossedUp(realTimeData)){
                firstTrade = false;
                freeToTrade = false;
                return buyAndCreatePositionHandler(realTimeData,PositionSide.LONG, symbol);
            } else if(devCrossedDown(realTimeData)){
                firstTrade = false;
                freeToTrade = false;
                return buyAndCreatePositionHandler(realTimeData,PositionSide.SHORT, symbol);
            }
        }
        else if (freeToTrade){
            double dev = realTimeData.getRsiDivergenceAtIndex(realTimeData.getLastCloseIndex());
            double currentDev = realTimeData.getRsiDivergenceAtIndex(realTimeData.getLastIndex());
            freeToTrade = false;
            if(dev > 0.0 && currentDev > 0.0){
                return buyAndCreatePositionHandler(realTimeData,PositionSide.LONG, symbol);
            }else if(dev < 0.0 && currentDev < 0.0){
                return buyAndCreatePositionHandler(realTimeData,PositionSide.SHORT, symbol);
                }
        }

        return null;
    }

    private boolean devCrossedDown(DataHolder realTimeData) {
        int index = realTimeData.getLastCloseIndex();
        double currDev = realTimeData.getRsiDivergenceAtIndex(index);
        double prevDev = realTimeData.getRsiDivergenceAtIndex(index - 1);
        return prevDev > 0 && currDev <= 0;
    }

    private boolean devCrossedUp(DataHolder realTimeData) {
        int index = realTimeData.getLastCloseIndex();
        double currDev = realTimeData.getRsiDivergenceAtIndex(index);
        double prevDev = realTimeData.getRsiDivergenceAtIndex(index - 1);
        return prevDev < 0 && currDev >= 0;
    }


    private PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, PositionSide positionSide, String symbol){
        setLeverage(symbol);
        TelegramMessenger.sendToTelegram("Entering new position " + new Date(System.currentTimeMillis()));
        ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
        SkippingExitTrailer skippingExitTrailer = new SkippingExitTrailer(RsiDivergeceConstance.TRAILING_PRECENTAGE, positionSide);
        exitStrategies.add(new rsiDivergenceExitStrategy1(positionSide, skippingExitTrailer));
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
                positionHandler = new PositionHandler(buyOrder, exitStrategies, RsiDivergeceConstance.STOP_LOSS_PERCENTAGE, positionSide);
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
                positionHandler = new PositionHandler(buyOrder, exitStrategies, RsiDivergeceConstance.STOP_LOSS_PERCENTAGE, positionSide);
                return positionHandler;
            } catch (Exception e) {
                TelegramMessenger.sendToTelegram("Exception was thrown" + new Date(System.currentTimeMillis()));
                e.printStackTrace();
            }
        }
        return null;
    }


    private void updateBuyingAmount(String symbol) {
        String baseSymbol = Config.BASE_COIN;
        double balance = accountBalance.getCoinBalance(baseSymbol).doubleValue();
        requestedBuyingAmount = (balance * RsiDivergeceConstance.AVAILABLE_BALANCE_PERCENTAGE) / 100;

    }

    private void setLeverage(String symbol){
        switch (symbol){

            case "bnbusdt":
                leverage = 75;
                break;
            case "ethusdt":
                leverage = 100;
                break;
            case "btcusdt":
                leverage = 100;
                break;
            case "dogeusdt" :
                leverage = 50;
                break;
        }
    }

    @Override
    public void setLeverage(int leverage) {

    }

    @Override
    public void setRequestedBuyingAmount(double requestedBuyingAmount) {

    }

    @Override
    public void positionClosed() {
        freeToTrade = true;
    }
}
