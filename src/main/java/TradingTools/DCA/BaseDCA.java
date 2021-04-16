package TradingTools.DCA;

import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import data.Config;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import positions.SellingInstructions;
import singletonHelpers.BinanceInfo;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.DCAStrategy;
import utils.Utils;

import java.util.Date;

import static positions.PositionHandler.percentageOfQuantity;

public class BaseDCA implements DCAStrategy {
    private DataHolder dataHolder;
    int DCACount=0;
    double step;
    double stepFactor;
    double InitialPrice;
    double maxDCACount;
    double InitialAmount;
    double amountFactor;
    public double TPPrice;
    public double[] DCAPrices;
    public boolean didDCA=false;
    private String TPOrderClaintId=null;
    private final String symbol;
    public boolean useTP;
    public double DCASize=0;
    public double nextDCAPrice;
    public boolean activeFirstTP=false,activeFirstDCA=false;
    public Order orderDCA, orderTP;
    DCAInstructions dcaInstructions;
    //public ArrayList<Order> activeDCAOrders = new ArrayList<>();


    public boolean needToDCA=true;
    PositionSide positionSide;


    public BaseDCA(double initialPrice, double maxDCACount, double initialAmount, double amountFactor,
                   PositionSide positionSide, double TPPrices,double[] DCAPrices, String symbol, boolean useTP, DataHolder dataHolder, DCAInstructions dcaInstructions) {
        this.InitialPrice = initialPrice;
        this.maxDCACount = maxDCACount;
        this.InitialAmount = initialAmount;
        this.amountFactor = amountFactor;
        this.positionSide=positionSide;
        this.TPPrice = this.TPPrice;
        this.DCAPrices=DCAPrices;
        this.symbol=symbol;
        this.useTP=useTP;
        this.dataHolder=dataHolder;
        this.DCASize=getDCASize();
        this.dcaInstructions=dcaInstructions;
    }


    public Instructions run(DataHolder realTimeData) {
        if(getNeedToDCA()){
            switch (positionSide){
                //// TODO: 4/5/2021  includ position side BOTH
                case SHORT:
                    return new DCAInstructions(DCAStrategy.DCAType.SHORT_DCA_LIMIT, getDCASize());
                case LONG:
                    return new DCAInstructions(DCAStrategy.DCAType.LONG_DCA_LIMIT, getDCASize());
            }
        }
        return null;
    }


    public double calculateTotalAmount(){
        double totalAmount=0;
        if(DCACount==0){
            return totalAmount=getInitialAmount();
        }else {
            for (int i =0; i<DCACount;i++){
                totalAmount+=totalAmount*amountFactor;
            }
            return totalAmount;
        }
    }

    public void setNeedToDCA(DataHolder dataHolder){
        double currentPrice= dataHolder.getCurrentPrice();
        if(maxDCACount<DCACount) return;
        switch (positionSide){
            case SHORT:
                if(currentPrice>= getInitialPrice()+step*100){
                    DCACount++;
                    needToDCA=true;
                }
                break;
            case LONG:
                if(currentPrice<=(getInitialPrice()-step*100)){
                    DCACount++;
                    needToDCA=true;
                }
                break;
        }
        needToDCA=false;
    }

    public void setNeedToDCA(DataHolder dataHolder , double[] closePrices){
        double currentPrice= dataHolder.getCurrentPrice();
        switch (positionSide){
            case SHORT:
                for (double price : closePrices) {
                    if (currentPrice <= price){
                        price*=10;
                        needToDCA=true;
                        return;
                    }
                }
                break;
            case LONG:
                for (double price : closePrices) {
                    if (currentPrice >= price){
                        price*=10;
                        needToDCA=true;
                        return;
                    }
                }
            }
    }

    @Override
    public double getTPPrice() {
        return TPPrice;
    }



    @Override
    public void updatTP(double qty, DataHolder realTimeData) {

        if (useTP){    if (getDidDCA()) {

//
//            switch (positionSide){
//                case SHORT:
//                    (new SellingInstructions (PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT,Config.ONE_HANDRED)//todo change to take profit type
//                            ,qty, realTimeData);
//                    break;
//                    case LONG:
//                        TakeProfit(new SellingInstructions (PositionHandler.ClosePositionTypes.SELL_LIMIT,Config.ONE_HANDRED)
//                                ,qty, realTimeData);
//                        break;
//            }
        }

        setDidDCA(false);
            setNeedToDCA(false);
        }

    }

    public void updateDCAPrices(){
        if(positionSide == PositionSide.LONG) DCAPrices[0] -= (DCAPrices[0]/100)*(step*stepFactor*DCACount);
        else   DCAPrices[0] += (DCAPrices[0]/100)*(step*stepFactor*DCACount);
    }

    public void DCAOrder (DCAInstructions dcaInstructions){
        if(DCAPrices.length==1)updateDCAPrices();
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            String sellingQty= Utils.fixQuantity(BinanceInfo.formatQty(percentageOfQuantity(dcaInstructions.getSellingQtyPercentage(), dcaInstructions.getDCAAmount()), symbol));
            String dcaPrice =Utils.fixQuantity(BinanceInfo.formatQty(DCAPrices[0],symbol));
            switch (dcaInstructions.getDCAType()) {


                case LONG_DCA_LIMIT:
                    try {
                        orderDCA = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                                sellingQty, String.valueOf(dcaPrice), null, null, null, null,
                                String.valueOf(dcaPrice), null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(DCAPrices[0]) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case LONG_DCA_MARKET:
                    try {
                        orderDCA = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
                                sellingQty, String.valueOf(dcaPrice), null, null, String.valueOf(dcaPrice), null,
                                String.valueOf(dcaPrice), null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price:  " + String.valueOf(dcaPrice)+ " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case SHORT_DCA_LIMIT:
                    try {
                        orderDCA = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, null,
                                sellingQty, String.valueOf(dcaPrice), null, null, null, null, String.valueOf(DCAPrices[0]), null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price:  " + String.valueOf(DCAPrices[0]) + " ," + new Date(System.currentTimeMillis()));
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


        for(int i=0;i<DCAPrices.length&& DCAPrices[i]!=0;i++){
            if(i==DCAPrices.length-1){
                DCAPrices[i]=0;
                break;
            }
            DCAPrices[i]=DCAPrices[i+1];
        }
        DCACount++;
    }

    public boolean isActiveFirstTP() {
        return activeFirstTP;
    }

    public void setActiveFirstTP(boolean activeFirstTP) {
        this.activeFirstTP = activeFirstTP;
    }

    public void TakeProfit(SellingInstructions sellingInstructions, double qty, DataHolder realTimeData){
        System.out.println("im baseDCA TP, initial amount="+getInitialAmount());


            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            if (orderTP != null) {
                TelegramMessenger.sendToTelegram("Posting TP order ");
                syncRequestClient.cancelOrder(symbol, orderTP.getUpdateTime(), orderTP.getClientOrderId());
                TPOrderClaintId = null;
            }

            String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(percentageOfQuantity(sellingInstructions.getSellingQtyPercentage(),qty), symbol));
            String tPPrice= Utils.fixQuantity(BinanceInfo.formatQty(TPPrice,symbol));
            switch (sellingInstructions.getType()) {

                case STAY_IN_POSITION:
                    break;

                case SELL_LIMIT:
                    System.out.println("sell limit");
                    try {
                        orderTP=syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                                sellingQty, tPPrice, "true", null, tPPrice, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(DCAPrices[0]) + " ," + new Date(System.currentTimeMillis()));
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
                        TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(DCAPrices[0]) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case CLOSE_SHORT_LIMIT:
                    try {
                        orderTP= syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                                sellingQty, tPPrice, "true", null, tPPrice, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(DCAPrices[0]) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case CLOSE_SHORT_MARKET:
                    try {
                        orderTP= syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
                                sellingQty, tPPrice, "true", null, tPPrice, null,
                                tPPrice, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(DCAPrices[0]) + " ," + new Date(System.currentTimeMillis()));
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

    private  void calculateNextDCAPrice(){
        if(positionSide==PositionSide.LONG){
            if (DCACount==0) {
                nextDCAPrice = getInitialPrice()-(getStep()*getInitialPrice());
            }else nextDCAPrice=nextDCAPrice-(getStep()*nextDCAPrice);
        }else {
            if (DCACount==0) {
                nextDCAPrice = getInitialPrice()+(getStep()*getInitialPrice());
            }else nextDCAPrice=nextDCAPrice+(getStep()*nextDCAPrice);
        }
    }

    public void setDCASize() {
        if (DCASize == 0) DCASize = getInitialAmount() + (getInitialAmount() / 100 * step);
        else {
            for (int i = 0; i < DCACount; i++) {
                DCASize += DCASize * getAmountFactor();
            }
        }
    }

    public double getDCASize(){
        if(DCASize==0)setDCASize();
        return DCASize;
    }

    public double getNextDCAPrice(){
        calculateNextDCAPrice();
        return nextDCAPrice;
    }

    public boolean isNeedToDCA() {
        return needToDCA;
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

    public void setStepFactor(double stepFactor) {
        this.stepFactor = stepFactor;
    }

    public double getInitialPrice() {
        return InitialPrice;
    }

    public void setInitialPrice(double initialPrice) {
        InitialPrice = initialPrice;
    }

    public double getMaxDCACount() {
        return maxDCACount;
    }

    public void setMaxDCACount(double maxDCACount) {
        this.maxDCACount = maxDCACount;
    }

    public double getInitialAmount() {
        return InitialAmount;
    }

    public void setInitialAmount(double initialAmount) {
        InitialAmount = initialAmount;
    }

    public double getAmountFactor() {
        return amountFactor;
    }

    public void setAmountFactor(double amountFactor) {
        this.amountFactor = amountFactor;
    }


    @Override
    public void setDidDCA(boolean valToSet) {
        didDCA=valToSet;
    }

    @Override
    public boolean getDidDCA() {
        return orderDCA.getStatus().equals(OrderStatus.FILLED.toString());
    }


    @Override
    public boolean getNeedToDCA() {
        if(orderDCA==null|| orderDCA.getStatus().equals(OrderStatus.FILLED.toString()))return needToDCA=true;
        else return needToDCA=false;

    }

    public void setTimeToDCA(DataHolder dataHolder) {

    }


    public DCAInstructions getDCAInstructions() {
        return dcaInstructions;
    }


//    public void setDCAInstructions(DCAInstructions dcaInstructions) {

    // }

    public PositionSide getPositionSide() {
        return positionSide;
    }

    @Override
    public void increaseDCACount() {DCACount++;}

    // public ArrayList getActiveDCAOrders(int)


}

