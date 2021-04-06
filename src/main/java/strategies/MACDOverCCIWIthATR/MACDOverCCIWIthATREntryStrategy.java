package strategies.MACDOverCCIWIthATR;

import TradingTools.DCA.BaseDCA;
import TradingTools.Trailers.SkippingEntryTrailer;
import TradingTools.Trailers.TrailingExit;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import data.AccountBalance;
import data.Config;
import data.DataHolder;
import positions.PositionHandler;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.ExitStrategy;
import strategies.MACDOverCCIWIthATR.Long.MACDOverCCIWIthATRLongExitStrategy1;
import strategies.macdOverRSIStrategies.Long.*;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import strategies.macdOverRSIStrategies.Short.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class MACDOverCCIWIthATREntryStrategy {
    private double takeProfitPercentage = MACDOverCCIWIthATRConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
    private double stopLossPercentage = MACDOverCCIWIthATRConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
    private int leverage = MACDOverCCIWIthATRConstants.DEFAULT_LEVERAGE;
    private double requestedBuyingAmount = MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT;
    private final AccountBalance accountBalance = AccountBalance.getAccountBalance();
    private volatile boolean bought = false;
    private double positivePeek = 0;
    private double negativePeek = 0;
    private SkippingEntryTrailer skippingEntryTrailer;
    private BaseDCA baseDCA;


    public synchronized PositionHandler run(DataHolder realTimeData, String symbol) {
        double currentPrice=realTimeData.getCurrentPrice();
        boolean notInPosition = accountBalance.getPosition(symbol).getPositionAmt().compareTo(BigDecimal.valueOf(Config.DOUBLE_ZERO)) == Config.ZERO;
        if(notInPosition){
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            skippingEntryTrailer=new SkippingEntryTrailer(realTimeData.getClosePriceAtIndex(-1),MACDOverCCIWIthATRConstants.NEGATIVE_SKIPINGֹ_TRAILING_PERCENTAGE_BUY,PositionSide.LONG);
            boolean noOpenOrders = syncRequestClient.getOpenOrders(symbol).size() == Config.ZERO;
            if (noOpenOrders){
                if (candleIndicateLong(realTimeData)){
                    if (realTimeData.getPercentBIAtIndex(realTimeData.getLastCloseIndex())<Config.DOUBLE_ZERO) {
                        skippingEntryTrailer.updateTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()));
                        if (skippingEntryTrailer.needToEnter(currentPrice)) {
                            double[] DCAPrices = new double[]{currentPrice - (realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.ATR1),
                                    currentPrice - (realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.ATR2), currentPrice - (realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.ATR3)};
                            baseDCA = new BaseDCA(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()), MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT, MACDOverCCIWIthATRConstants.MAX_DCA,
                                    MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, PositionSide.LONG, DCAPrices,symbol,true,realTimeData);
                            buyAndCreatePositionHandler(realTimeData, realTimeData.getCurrentPrice(), symbol, PositionSide.LONG, baseDCA);
                        }
                    }
//                    else if (realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_CCI, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE,Config.DOUBLE_ZERO)) {
//
//
//                    }
                }else if (candleIndicateShort(realTimeData)){
                    if(realTimeData.getPercentBIAtIndex(realTimeData.getLastCloseIndex())>Config.DOUBLE_ONE){
                        skippingEntryTrailer.updateTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()));
                        if (skippingEntryTrailer.needToEnter(currentPrice)) {
                            double[] DCAPrices = new double[]{currentPrice + (realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.ATR1),
                                    currentPrice + (realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.ATR2, currentPrice + (realTimeData.getLastCloseIndex() * MACDOverCCIWIthATRConstants.ATR3)};
                            baseDCA = new BaseDCA(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()), MACDOverCCIWIthATRConstants.MAX_DCA,
                                    MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT, MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, PositionSide.SHORT, DCAPrices,symbol,true,realTimeData);
                            buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, baseDCA);
                        }

                    }
                }
//                else if (realTimeData.crossed(DataHolder.IndicatorType.MACD_OVER_CCI, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE,Config.DOUBLE_ONE)){
//
//                }
            }
                }
        return null;
    }
    public boolean candleIndicateLong(DataHolder realTimeData){
        double currentMacdOverRsiValue = realTimeData.getMacdOverCCIValueAtIndex(realTimeData.getLastCloseIndex());
        double prevMacdOverRsiValue = realTimeData.getMacdOverCCIValueAtIndex(realTimeData.getLastCloseIndex()-1);
        return currentMacdOverRsiValue > prevMacdOverRsiValue;
    }

    public boolean candleIndicateShort(DataHolder realTimeData){
        double currentMacdOverRsiValue = realTimeData.getMacdOverCCIValueAtIndex(realTimeData.getLastCloseIndex());
        double prevMacdOverRsiValue = realTimeData.getMacdOverCCIValueAtIndex(realTimeData.getLastCloseIndex()-1);
        return currentMacdOverRsiValue < prevMacdOverRsiValue;
    }
    private PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, double currentPrice, String symbol, PositionSide positionSide, BaseDCA baseDCA) {//TODO: maybe change market later.
        bought = true;
        if (positionSide == PositionSide.LONG) {
            TelegramMessenger.sendToTelegram("trailing enter long: " + new Date(System.currentTimeMillis()));
            if(skippingEntryTrailer.needToEnter(currentPrice)) {
                TelegramMessenger.sendToTelegram("entring long: " + new Date(System.currentTimeMillis()));
                try {
                    SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                    syncRequestClient.changeInitialLeverage(symbol, leverage);
                    String buyingQty = utils.Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
                    Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.LIMIT, TimeInForce.GTC,
                            buyingQty, String.valueOf(currentPrice), null, null, null, null, null, null, WorkingType.MARK_PRICE, null, NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("entring long: buyOrder: " + buyOrder + new Date(System.currentTimeMillis()));
                    double[] exitPrices={currentPrice - (realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.ATR1)};
                    ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
                    exitStrategies.add(new MACDOverCCIWIthATRLongExitStrategy1(currentPrice,MACDOverCCIWIthATRConstants.MAX_DCA,MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT,
                            MACDOverCCIWIthATRConstants.AMOUNT_FACTOR,PositionSide.LONG,exitPrices,exitPrices[0],realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex())));
                    return new PositionHandler(buyOrder, exitStrategies);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else if (positionSide == PositionSide.SHORT) {
            if (skippingEntryTrailer.needToEnter(currentPrice)) {
                TelegramMessenger.sendToTelegram("trailing enter short: " + new Date(System.currentTimeMillis()));
                try {
                    TelegramMessenger.sendToTelegram("entering short: " + new Date(System.currentTimeMillis()));
                    SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                    syncRequestClient.changeInitialLeverage(symbol, leverage);
                    String buyingQty = utils.Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
                    Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.LIMIT, TimeInForce.GTC,
                            buyingQty, String.valueOf(currentPrice), null, null, null, null, null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                    TelegramMessenger.sendToTelegram("entring short: sellOrder: " + buyOrder + new Date(System.currentTimeMillis()));
                    ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
                    exitStrategies.add(new MACDOverRSIShortExitStrategy1(new TrailingExit(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.SHORT)));
                    return new PositionHandler(buyOrder, exitStrategies);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return null;
    }
}
