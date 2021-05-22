//package strategies.MACDOverCCIWIthATR;
//
//import TradingTools.Trailers.SkippingEntryTrailer;
//import TradingTools.Trailers.SkippingExitTrailer;
//import com.binance.client.SyncRequestClient;
//import com.binance.client.model.enums.*;
//import com.binance.client.model.trade.Order;
//import data.AccountBalance;
//import data.Config;
//import data.DataHolder;
//import positions.DCAInstructions;
//import positions.PositionHandler;
//import singletonHelpers.RequestClient;
//import singletonHelpers.TelegramMessenger;
//import strategies.DCAStrategy;
//import strategies.ExitStrategy;
//import strategies.MACDOverCCIWIthATR.Long.MACDOverCCIWIthATRLongExitStrategy1;
//import strategies.MACDOverCCIWIthATR.Long.MACDOverCCIWIthATRLongExitStrategy2;
//import strategies.MACDOverCCIWIthATR.Short.MACDOverCCIWIthATRShortExitStrategy1;
//import strategies.MACDOverCCIWIthATR.Short.MACDOverCCIWIthATRShortExitStrategy2;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Date;
//
//public class MACDOverCCIWithATRSecondEntryStrategy {
//
//    private double takeProfitPercentage = MACDOverCCIWIthATRConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
//    private double stopLossPercentage = MACDOverCCIWIthATRConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
//    private int leverage = MACDOverCCIWIthATRConstants.DEFAULT_LEVERAGE;
//    private double requestedBuyingAmount = MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT;
//    private final AccountBalance accountBalance;
//    private volatile boolean bought = false;
//    private double positivePeek = 0;
//    private double negativePeek = 0;
//    private SkippingEntryTrailer skippingEntryTrailer;
//    private double DCAPrices;
//    private double TPPrice;
//    private boolean entering = false;
//
//    public MACDOverCCIWithATRSecondEntryStrategy(){
//        accountBalance = AccountBalance.getAccountBalance();
//        System.out.println("macd over cci");
//    }
//
//    public synchronized PositionHandler run(DataHolder realTimeData, String symbol) {
//        ArrayList <DCAStrategy> DCAStrategies = new ArrayList<>();
//        ArrayList <ExitStrategy> exitStrategies = new ArrayList<>();
//        double currentPrice=realTimeData.getCurrentPrice();
//        boolean notInPosition = accountBalance.getPosition(symbol).getPositionAmt().compareTo(BigDecimal.valueOf(Config.DOUBLE_ZERO)) == Config.ZERO;
//        if(notInPosition){
//            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//            double lastClosePrice = realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex());
//            skippingEntryTrailer = new SkippingEntryTrailer(lastClosePrice, MACDOverCCIWIthATRConstants.NEGATIVE_SKIPINGֹ_TRAILING_PERCENTAGE_BUY,PositionSide.LONG);
//            boolean noOpenOrders = syncRequestClient.getOpenOrders(symbol).size() == Config.ZERO;
//            if (noOpenOrders){
//                double atrVAl = realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex());
//                boolean cCIAboveZero = realTimeData.above(DataHolder.IndicatorType.MACD_OVER_CCI, DataHolder.CandleType.CLOSE,Config.DOUBLE_ZERO);
//                if (cCIAboveZero){
//                    boolean candleIndicateLong = candleIndicateLong(realTimeData, realTimeData.getLastCloseIndex());
//                    if (candleIndicateLong && !BBIsExpanding(realTimeData) && longCurrHightIsBigger(realTimeData) && !entering ){
//                        entering = true;
//                        skippingEntryTrailer.updateTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()));
//                        if(lowerBICroosUp(realTimeData)) {
//                            return buyAndCreatePositionHandler(realTimeData, realTimeData.getCurrentPrice(), symbol, PositionSide.LONG, DCAStrategies,
//                                    exitStrategies, MACDOverCCIWIthATRConstants.STRONG_TRAD_MULTIPLIER );
//                        }else if (skippingEntryTrailer.needToEnter(currentPrice)) {
//                           MACDOverCCIWIthATRLongExitStrategy2 macdOverCCIWIthATRLongExitStrategy2=new MACDOverCCIWIthATRLongExitStrategy2();
//                           DCAStrategies.add(enterLongPostion(realTimeData, currentPrice, atrVAl, symbol));
//                           exitStrategies.add(macdOverCCIWIthATRLongExitStrategy2);
//                            TelegramMessenger.sendToTelegram("buyAndCreatePositionHandler----long ");
//                           return buyAndCreatePositionHandler(realTimeData, realTimeData.getCurrentPrice(), symbol, PositionSide.LONG, DCAStrategies,
//                                    exitStrategies, MACDOverCCIWIthATRConstants.REGULAR_TRAD_MULTIPLIER );
//                        }
//                    } else if (candleIndicateShort(realTimeData,realTimeData.getLastCloseIndex())){
//                        skippingEntryTrailer.dismiss();
//                    }
//                }else if (upperBICroosDown(realTimeData)&& !entering){
//                    if (candleIndicateShort(realTimeData,realTimeData.getLastCloseIndex()) && !BBIsExpanding(realTimeData )){
//                        entering=true;
//                        skippingEntryTrailer.updateTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()));
//                        if (skippingEntryTrailer.needToEnter(currentPrice)) {
//                            TPPrice=currentPrice + (realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.CLOSE_ATR1);
//                            MACDOverCCIWIthATRConstants.STEP=realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.CLOSE_ATR1;
//                            MACDOverCCIWIthATRLongExitStrategy1 macdOverCCIWIthATRLongExitStrategy1 = new MACDOverCCIWIthATRLongExitStrategy1(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()), MACDOverCCIWIthATRConstants.MAX_DCA, MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT*MACDOverCCIWIthATRConstants.DEFAULT_LEVERAGE,
//                                    MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, PositionSide.SHORT,TPPrice, DCAPrices,symbol,true,MACDOverCCIWIthATRConstants.STEP,
//                                    MACDOverCCIWIthATRConstants.STEP_FACTOR,realTimeData,
//                                    new SkippingExitTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()),MACDOverCCIWIthATRConstants.POSITIVE_SKIPINGֹ_TRAILING_PERCENTAGE_BUY,PositionSide.LONG)
//                                    ,new DCAInstructions(DCAStrategy.DCAType.LONG_DCA_LIMIT,MACDOverCCIWIthATRConstants.FIRST_DCA_SIZE));
//                            MACDOverCCIWIthATRLongExitStrategy2 macdOverCCIWIthATRLongExitStrategy2=new MACDOverCCIWIthATRLongExitStrategy2();
//                            DCAStrategies.add(macdOverCCIWIthATRLongExitStrategy1);
//                            exitStrategies.add(macdOverCCIWIthATRLongExitStrategy2);
//                            TelegramMessenger.sendToTelegram("buyAndCreatePositionHandler----short ");
//
//                            return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, DCAStrategies,exitStrategies, );
//                        }
//
//                    } else if (candleIndicateLong(realTimeData,realTimeData.getLastCloseIndex())){
//                        skippingEntryTrailer.dismiss();
//                    }
//
//                }else if((candleIndicateLong(realTimeData,realTimeData.getLastCloseIndex()) && candleIndicateShort(realTimeData,realTimeData.getLastCloseIndex()-1))
//                        && upperBICroosUp(realTimeData) && !entering ){
//                    entering=true;
//                    SkippingExitTrailer nullTrailler=null;
//                    DCAPrices = currentPrice - (realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.CLOSE_ATR1);
//                    MACDOverCCIWIthATRConstants.STEP=realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.CLOSE_ATR1;
//                    TPPrice=currentPrice + (realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.CLOSE_ATR1);
//                    MACDOverCCIWIthATRLongExitStrategy1 macdOverCCIWIthATRLongExitStrategy1 = new MACDOverCCIWIthATRLongExitStrategy1(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()), MACDOverCCIWIthATRConstants.MAX_DCA, MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT*MACDOverCCIWIthATRConstants.DEFAULT_LEVERAGE,
//                            MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, PositionSide.LONG,TPPrice, DCAPrices,symbol,true,MACDOverCCIWIthATRConstants.STEP,
//                            MACDOverCCIWIthATRConstants.STEP_FACTOR, realTimeData,
//                            new SkippingExitTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()),MACDOverCCIWIthATRConstants.POSITIVE_SKIPINGֹ_TRAILING_PERCENTAGE_BUY,PositionSide.LONG)
//                            ,new DCAInstructions(DCAStrategy.DCAType.LONG_DCA_LIMIT,MACDOverCCIWIthATRConstants.FIRST_DCA_SIZE));
//                    MACDOverCCIWIthATRLongExitStrategy2 macdOverCCIWIthATRLongExitStrategy2=new MACDOverCCIWIthATRLongExitStrategy2();
//                    DCAStrategies.add(macdOverCCIWIthATRLongExitStrategy1);
//                    exitStrategies.add(macdOverCCIWIthATRLongExitStrategy2);
//
//                    return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.LONG, DCAStrategies, exitStrategies, );
//                }
//                else if ((candleIndicateShort(realTimeData,realTimeData.getLastCloseIndex()) && candleIndicateLong(realTimeData,realTimeData.getLastCloseIndex()-1))
//                        && lowerBICroosDown(realTimeData) && !entering){
//                    SkippingExitTrailer nullTrailler=null;
//                    MACDOverCCIWIthATRShortExitStrategy2 macdOverCCIWIthATRLongExitStrategy2=new MACDOverCCIWIthATRSHExitStrategy2();
//                    DCAStrategies.add(enterShortPostion(realTimeData,currentPrice,atrVAl,symbol));
//                    exitStrategies.add(macdOverCCIWIthATRLongExitStrategy2);
//
//
//                    return buyAndCreatePositionHandler(realTimeData, currentPrice, symbol, PositionSide.SHORT, DCAStrategies, exitStrategies, );
//                }
//            }
//        }
//        entering=false;
//        return null;
//
//    }
//    private boolean MACDOverZeroCroosUp(DataHolder dataHolder,int index){
//        return dataHolder.crossedAtIndex(DataHolder.IndicatorType.MACD_OVER_CCI, DataHolder.CrossType.UP,DataHolder.CandleType.CLOSE, Config.DOUBLE_ZERO ,index);
//    }
//
//    private MACDOverCCIWIthATRLongExitStrategy1 enterLongPostion(DataHolder realTimeData,double currentPrice, double atrVAl, String symbol){
//        DCAPrices = currentPrice - (atrVAl * MACDOverCCIWIthATRConstants.ATR1);
//        MACDOverCCIWIthATRConstants.STEP=(currentPrice- (currentPrice -realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.CLOSE_ATR1))/(currentPrice/100);
//        System.out.println("step="+MACDOverCCIWIthATRConstants.STEP);
//        TPPrice=currentPrice + (atrVAl * MACDOverCCIWIthATRConstants.CLOSE_ATR1);
//        MACDOverCCIWIthATRLongExitStrategy1 macdOverCCIWIthATRLongExitStrategy1 = new MACDOverCCIWIthATRLongExitStrategy1(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()), MACDOverCCIWIthATRConstants.MAX_DCA, MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT*MACDOverCCIWIthATRConstants.DEFAULT_LEVERAGE,
//                MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, PositionSide.LONG,TPPrice, DCAPrices,symbol,true,MACDOverCCIWIthATRConstants.STEP,MACDOverCCIWIthATRConstants.STEP_FACTOR,realTimeData,
//                new SkippingExitTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()),MACDOverCCIWIthATRConstants.POSITIVE_SKIPINGֹ_TRAILING_PERCENTAGE_BUY,PositionSide.LONG)
//                ,new DCAInstructions(DCAStrategy.DCAType.LONG_DCA_LIMIT,MACDOverCCIWIthATRConstants.FIRST_DCA_SIZE));
//        return macdOverCCIWIthATRLongExitStrategy1;
//    }
//
//    private MACDOverCCIWIthATRShortExitStrategy1 enterShortPostion(DataHolder realTimeData, double currentPrice, double atrVAl, String symbol){
//        DCAPrices = currentPrice - (atrVAl * MACDOverCCIWIthATRConstants.ATR1);
//        MACDOverCCIWIthATRConstants.STEP=(currentPrice- (currentPrice -realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.CLOSE_ATR1))/(currentPrice/100);
//        TPPrice=currentPrice + (atrVAl * MACDOverCCIWIthATRConstants.CLOSE_ATR1);
//        MACDOverCCIWIthATRShortExitStrategy1 macdOverCCIWIthATRShortExitStrategy1 = new MACDOverCCIWIthATRShortExitStrategy1(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex())
//                ,MACDOverCCIWIthATRConstants.MAX_DCA, MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT*MACDOverCCIWIthATRConstants.DEFAULT_LEVERAGE,
//                MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, PositionSide.SHORT,TPPrice , DCAPrices, symbol, MACDOverCCIWIthATRConstants.STEP,MACDOverCCIWIthATRConstants.STEP_FACTOR ,realTimeData,
//                new SkippingExitTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()),MACDOverCCIWIthATRConstants.POSITIVE_SKIPINGֹ_TRAILING_PERCENTAGE_BUY,PositionSide.LONG)
//                ,new DCAInstructions(DCAStrategy.DCAType.LONG_DCA_LIMIT,MACDOverCCIWIthATRConstants.FIRST_DCA_SIZE));
//        return macdOverCCIWIthATRShortExitStrategy1;
//    }
//
//    private boolean longCurrHightIsBigger(DataHolder realTimeData){
//        int i = 0, lastIndex = realTimeData.getLastCloseIndex();
//        double prevHight = 0, macdVal;
//        while (!MACDOverZeroCroosDown(realTimeData,lastIndex-i++)){
//            if(Math.abs(prevHight)<Math.abs((macdVal = realTimeData.getMACDOverCCIHistAtIndex(lastIndex-i)))){
//                prevHight = macdVal;
//            }
//        }
//        return prevHight<Math.abs(realTimeData.getMACDOverCCIHistAtIndex(lastIndex));
//    }
//
//    private boolean ShortCurrHightIsBigger(DataHolder realTimeData){
//        int i = 0, lastIndex = realTimeData.getLastCloseIndex();
//        double prevHight = 0, macdVal;
//        while (!MACDOverZeroCroosUp(realTimeData,lastIndex-i++)){
//            if(Math.abs(prevHight)<Math.abs((macdVal = realTimeData.getMACDOverCCIHistAtIndex(lastIndex-i)))){
//                prevHight = macdVal;
//            }
//        }
//        return prevHight<Math.abs(realTimeData.getMACDOverCCIHistAtIndex(lastIndex));
//    }
//    private boolean MACDOverZeroCroosDown(DataHolder dataHolder, int index){
//        return dataHolder.crossedAtIndex(DataHolder.IndicatorType.MACD_OVER_CCI, DataHolder.CrossType.DOWN,DataHolder.CandleType.CLOSE, Config.DOUBLE_ZERO, index );
//    }
//
//    private boolean lowerBICroosUp(DataHolder dataHolder){
//        return dataHolder.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE,MACDOverCCIWIthATRConstants.LOWER_BI);
//    }
//
//    private boolean upperBICroosDown(DataHolder dataHolder){
//
//        return dataHolder.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE,MACDOverCCIWIthATRConstants.UPPER_BI);
//    }
//
//    private boolean upperBICroosUp(DataHolder dataHolder){
//        return dataHolder.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE,MACDOverCCIWIthATRConstants.UPPER_BI);
//    }
//
//    private boolean lowerBICroosDown(DataHolder dataHolder){
//        return dataHolder.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE,MACDOverCCIWIthATRConstants.LOWER_BI);
//    }
//
//    private boolean candleIndicateLong(DataHolder realTimeData,int index){
//        double currentMacdOverCCIValue = realTimeData.getMACDOverCCIHistAtIndex(index);
//        double prevMacdOverCCIValue = realTimeData.getMACDOverCCIHistAtIndex(index-1);
//        //double prevPrevMacdOverCCIValue = realTimeData.getMacdOverCCIValueAtIndex(realTimeData.getLastCloseIndex()-2);
//        return currentMacdOverCCIValue > prevMacdOverCCIValue; /*&& prevMacdOverCCIValue <prevPrevMacdOverCCIValue;*/
//    }
//
//    private boolean candleIndicateShort(DataHolder realTimeData,int index){
//        double currentMacdOverCCIValue = realTimeData.getMACDOverCCIHistAtIndex(index);
//        double prevMacdOverCCIValue = realTimeData.getMACDOverCCIHistAtIndex(index-1);
//        //double prevPrevMacdOverCCIValue = realTimeData.getMacdOverCCIValueAtIndex(realTimeData.getLastCloseIndex()-2);
//        boolean result=currentMacdOverCCIValue < prevMacdOverCCIValue;
//        return currentMacdOverCCIValue < prevMacdOverCCIValue; /*  && prevMacdOverCCIValue <prevPrevMacdOverCCIValue;*/
//    }
//
//    private boolean BBIsExpanding(DataHolder realTimeData){
//        double prevWidh=realTimeData.getBandWidthAtIndex(realTimeData.getLastCloseIndex()-1)-realTimeData.getBandWidthAtIndex(realTimeData.getLastCloseIndex()-2);
//        double currWidh=realTimeData.getBandWidthAtIndex(realTimeData.getLastCloseIndex())-realTimeData.getBandWidthAtIndex(realTimeData.getLastCloseIndex()-1);
//        return currWidh>prevWidh;
//    }
//
//    private PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, double currentPrice, String symbol, PositionSide positionSide, ArrayList <DCAStrategy> DCAStrategies,ArrayList<ExitStrategy> exitStrategies, double multiplier ) {//TODO: maybe change market later.
//        bought = true;
//        if (positionSide == PositionSide.LONG) {
//            TelegramMessenger.sendToTelegram("trailing enter long: " + new Date(System.currentTimeMillis()));
//            if(skippingEntryTrailer.needToEnter(currentPrice)) {
//                TelegramMessenger.sendToTelegram("entring long: " + new Date(System.currentTimeMillis()));
//                try {
//                    SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//                    syncRequestClient.changeInitialLeverage(symbol, leverage);
//                    String buyingQty = utils.Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount * multiplier);
//                    Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.BUY, null, OrderType.MARKET, null,
//                            buyingQty, null, null, null, null, null, null, null, WorkingType.MARK_PRICE, "TRUE", NewOrderRespType.RESULT);
//                    TelegramMessenger.sendToTelegram("entring long: buyOrder: " + buyOrder + new Date(System.currentTimeMillis()));
//                    double[] exitPrices={currentPrice - (realTimeData.getATRValueAtIndex(realTimeData.getLastCloseIndex()) * MACDOverCCIWIthATRConstants.ATR1)};
//                    MACDOverCCIWIthATRLongExitStrategy1 macdOverCCIWIthATRLongExitStrategy1 = new MACDOverCCIWIthATRLongExitStrategy1(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()), MACDOverCCIWIthATRConstants.MAX_DCA,
//                            MACDOverCCIWIthATRConstants.DEFAULT_BUYING_AMOUNT*MACDOverCCIWIthATRConstants.DEFAULT_LEVERAGE,
//                            MACDOverCCIWIthATRConstants.AMOUNT_FACTOR, PositionSide.LONG,TPPrice, DCAPrices,symbol,true,MACDOverCCIWIthATRConstants.STEP,
//                            MACDOverCCIWIthATRConstants.STEP_FACTOR, realTimeData,
//                            new SkippingExitTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()),MACDOverCCIWIthATRConstants.POSITIVE_SKIPINGֹ_TRAILING_PERCENTAGE_BUY,PositionSide.LONG)
//                            ,new DCAInstructions(DCAStrategy.DCAType.LONG_DCA_LIMIT,MACDOverCCIWIthATRConstants.FIRST_DCA_SIZE));
//
//
//                    return new PositionHandler(buyOrder, exitStrategies,DCAStrategies);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        else if (positionSide == PositionSide.SHORT) {
//            if (skippingEntryTrailer.needToEnter(currentPrice)) {
//                TelegramMessenger.sendToTelegram("trailing enter short: " + new Date(System.currentTimeMillis()));
//                try {
//                    TelegramMessenger.sendToTelegram("entering short: " + new Date(System.currentTimeMillis()));
//                    SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//                    syncRequestClient.changeInitialLeverage(symbol, leverage);
//                    String buyingQty = utils.Utils.getBuyingQtyAsString(currentPrice, symbol, leverage, requestedBuyingAmount);
//                    Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.MARKET, null,
//                            buyingQty, null, null, "maca_cci_entry", null, null, null, null, WorkingType.MARK_PRICE,"TRUE" , NewOrderRespType.RESULT);
//                    TelegramMessenger.sendToTelegram("entring short: sellOrder: " + buyOrder + new Date(System.currentTimeMillis()));
//                    return new PositionHandler(buyOrder, exitStrategies,DCAStrategies);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//        return null;
//    }
//    @Override
//    public void setTakeProfitPercentage(double takeProfitPercentage) {
//
//    }
//
//    @Override
//    public void setStopLossPercentage(double stopLossPercentage) {
//
//    }
//
//    @Override
//    public void setLeverage(int leverage) {
//
//    }
//
//    @Override
//    public void setRequestedBuyingAmount(double requestedBuyingAmount) {
//
//    }
//
//}