package TradingTools.DCA;

import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.BinanceInfo;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.DCAStrategy;
import utils.Utils;

import java.util.Date;

public class BaseDCA implements DCAStrategy {
    private int dCACount = 0;
    private double step;
    private double stepFactor;
    private double currentPrice;
    private double maxDCACount;
    private double initialAmount;
    private double amountFactor;
    public double tPPrice;
    public double dCAPrice;
    private final String symbol;
    public boolean useTP;
    public double dCASize = 0;
    public double nextDCAPrice;
    public Order orderDCA, orderTP;
    private DCAInstructions dcaInstructions;
    private double prevQTY = 0;
    private double distanceToTP;
    public boolean needToDCA = true;
    public PositionSide positionSide;


    public BaseDCA(double currentPrice, double maxDCACount, double initialAmount, double amountFactor, PositionSide positionSide,
                   double TPPrices, double DCAPrices, String symbol, boolean useTP, double step,double stepFactor, DCAInstructions dcaInstructions) {
        this.currentPrice = currentPrice;
        this.maxDCACount = maxDCACount;
        this.initialAmount = initialAmount;
        this.amountFactor = amountFactor;
        this.positionSide = positionSide;
        this.tPPrice = TPPrices;
        this.dCAPrice = DCAPrices;
        this.symbol = symbol;
        this.useTP = useTP;
        this.step = step ;
        this.dCASize = initialAmount;
        this.dcaInstructions = dcaInstructions;
        distanceToTP = (Math.abs(currentPrice-tPPrice)) / (currentPrice / 100);
        this.stepFactor = stepFactor;
    }


    public Instructions run(DataHolder realTimeData) {
        if(needToDCA || orderDCA == null){
            switch (positionSide){
                // TODO: 4/5/2021  include position side BOTH
                case SHORT:
                    return new DCAInstructions(DCAStrategy.DCAType.SHORT_DCA_LIMIT, dCASize);
                case LONG:
                    return new DCAInstructions(DCAStrategy.DCAType.LONG_DCA_LIMIT, dCASize);
            }
        }
        return null;
    }

    public double calculateTotalAmount(){
        double totalAmount = 0;
        if(dCACount == 0){
            totalAmount=getInitialAmount();
        }else {
            for (int i = 0; i < dCACount; i++){
                totalAmount+=totalAmount*amountFactor;
            }
        }
        return totalAmount;
    }

    public void setNeedToDCA(DataHolder dataHolder){
        double currentPrice = dataHolder.getCurrentPrice();
        if(maxDCACount < dCACount) return;
        switch (positionSide){
            case SHORT:
                if(currentPrice >= getCurrentPrice() + (step * 100)){
                    needToDCA = true;
                }
                break;
            case LONG:
                if(currentPrice <= (getCurrentPrice() - (step * 100))){
                    needToDCA = true;
                }
                break;
        }
        needToDCA = false;
    }

//

    private void updateStep() {
        step *= stepFactor;
    }

    public void DCAOrder(DCAInstructions dcaInstructions, DataHolder realTimeData){
        System.out.println("posting DCA order");
        if(needToDCA) {
            setDCASize();
            setNeedToDCA(false);
            if (dCACount <= maxDCACount) {
                prevQTY = PositionHandler.getQty();
                double currentPrice = realTimeData.getCurrentPrice();
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(dCASize / currentPrice, symbol));
                String dcaPrice = BinanceInfo.formatPrice(calculateNextDCAPrice(currentPrice), symbol);
                switch (dcaInstructions.getDCAType()) {


                    case LONG_DCA_LIMIT:
                        try {
                            orderDCA = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                                    sellingQty, String.valueOf(dcaPrice), null, null, null, null,
                                    String.valueOf(dcaPrice), null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                            TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(dcaPrice) + " ," + new Date(System.currentTimeMillis()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case LONG_DCA_MARKET:
                        try {
                            orderDCA = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                                    sellingQty, String.valueOf(dcaPrice), null, null, String.valueOf(dcaPrice), null,
                                    String.valueOf(dcaPrice), null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                            TelegramMessenger.sendToTelegram("DCA price:  " + String.valueOf(dcaPrice) + " ," + new Date(System.currentTimeMillis()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case SHORT_DCA_LIMIT:
                        try {
                            orderDCA = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, null,
                                    sellingQty, String.valueOf(dcaPrice), null, null, null, null, String.valueOf(dCAPrice), null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                            TelegramMessenger.sendToTelegram("DCA price:  " + String.valueOf(dCAPrice) + " ," + new Date(System.currentTimeMillis()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case SHORT_DCA_MARKET:
                        try {
                            orderDCA = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                                    sellingQty, String.valueOf(dcaPrice), null, null, String.valueOf(dcaPrice), null,
                                    String.valueOf(dcaPrice), null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                            TelegramMessenger.sendToTelegram("DCA price:  " + String.valueOf(dcaPrice) + " ," + new Date(System.currentTimeMillis()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
                dCACount++;
            }
            updateDCAPrices(currentPrice);
            return;
        }
        System.out.println("wrong call for DCA");
    }

    public void TakeProfit(SellingInstructions sellingInstructions, double qty,double entryPrice, DataHolder realTimeData){
        System.out.println("posting Take Profit Order");
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            if (orderTP != null) {
                TelegramMessenger.sendToTelegram("Posting TP order ");
                syncRequestClient.cancelOrder(symbol, orderTP.getOrderId(), orderTP.getClientOrderId());
                orderTP = null;
            }

            String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(qty, symbol));
            String tPPrice= Utils.fixQuantity(BinanceInfo.formatQty(entryPrice+(entryPrice/100*distanceToTP),symbol));
            switch (sellingInstructions.getType()) {

                case STAY_IN_POSITION:
                    break;

                case SELL_LIMIT:
                    System.out.println("sell limit");
                    try {
                        orderTP=syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                                sellingQty, tPPrice, "true", null, tPPrice, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(dCAPrice) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case SELL_MARKET:
                    System.out.println("sell maeket");
                    try {
                        orderTP=syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
                                sellingQty, tPPrice, "true", null, tPPrice, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(dCAPrice) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case CLOSE_SHORT_LIMIT:
                    try {
                        orderTP= syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                                sellingQty, tPPrice, "true", null, tPPrice, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(dCAPrice) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case CLOSE_SHORT_MARKET:
                    try {
                        orderTP= syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
                                sellingQty, tPPrice, "true", null, tPPrice, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(dCAPrice) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                default:
            }
        }

    public boolean getNeedToTP(){
        return orderTP == null;
    }

    private double calculateNextDCAPrice(double currentPrice){
        if(positionSide == PositionSide.LONG){
            if (dCACount == 0) {
                return currentPrice - (currentPrice / 100 * getStep());
            }
            else return nextDCAPrice - (getStep() * nextDCAPrice);
        }else {
            if (dCACount == 0) {
                return currentPrice + currentPrice / 100 * getStep();
            }else return nextDCAPrice + (getStep() * nextDCAPrice);
        }
    }

    public void setDCASize() {
        if (dCASize == 0) dCASize = getInitialAmount()*getAmountFactor();
        else {
                dCASize = dCASize * getAmountFactor();
            }
    }

    @Override
    public double distanceToTP() {
        return distanceToTP;
    }


    public double getNextDCASize(){
        System.out.println("next DCA size = " + dCASize * amountFactor);
        return dCASize *= amountFactor;
    }

    public double getNextDCAPrice(){
        //calculateNextDCAPrice();
        return nextDCAPrice;
    }

    public void setNeedToDCA(boolean needToDCA) {
        this.needToDCA = needToDCA;
    }
    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public double getStepFactor() {
        return stepFactor;
    }

    @Override
    public boolean isNewPosition() {
        return orderTP == null && orderDCA == null && dCACount < 1;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getMaxDCACount() {
        return maxDCACount;
    }

    public void setMaxDCACount(double maxDCACount) {
        this.maxDCACount = maxDCACount;
    }

    public double getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(double initialAmount) {
        this.initialAmount = initialAmount;
    }

    public double getAmountFactor() {
        return amountFactor;
    }

    public void setAmountFactor(double amountFactor) {
        this.amountFactor = amountFactor;
    }

    @Override
    public boolean getNeedToDCA() {
        return needToDCA;
    }

    public int getdCACount() {
        return dCACount;
    }


    @Override
    public double gettPPrice() {
        return tPPrice;
    }


    public void updateDCAPrices(double currentPrice) {
        switch (positionSide) {
            case SHORT:
                if (dCACount == 0) {
                    dCAPrice = currentPrice + (currentPrice / 100 * step);
                    break;
                } else dCAPrice += (dCAPrice / 100) * (step);
                break;
            case LONG:
                if (dCACount == 0) {
                    dCAPrice = currentPrice - (currentPrice / 100 * step);
                    break;
                } else dCAPrice -= (dCAPrice / 100) * (step);
                break;
        }
    }

    @Override
    public Order getTpOrder() {
        return orderTP;
    }


    public DCAInstructions getDCAInstructions() {
        return dcaInstructions;
    }


//    public void setDCAInstructions(DCAInstructions dcaInstructions) {

    // }


    @Override
    public PositionSide getPositionSide() {
        return positionSide;
    }
//    public void setNeedToDCA(DataHolder dataHolder , double[] closePrices){
//        double currentPrice= dataHolder.getCurrentPrice();
//        switch (positionSide){
//            case SHORT:
//                for (double price : closePrices) {
//                    if (currentPrice <= price){
//                        price*=10;
//                        needToDCA=true;
//                        return;
//                    }
//                }
//                break;
//            case LONG:
//                for (double price : closePrices) {
//                    if (currentPrice >= price){
//                        price*=10;
//                        needToDCA=true;
//                        return;
//                    }
//                }
//            }
//    }
//

}

