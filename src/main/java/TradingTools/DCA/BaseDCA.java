package TradingTools.DCA;

import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import data.Config;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.DCAStrategy;

import java.util.Date;

public class BaseDCA implements DCAStrategy {
    private DataHolder dataHolder;
    int DCACount=0;
    double step;
    double stepFactor;
    double InitialPrice;
    double maxDCACount;
    double InitialAmount;
    double amountFactor;
    public double[] exitPrices;
    public boolean didDCA=false;
    private String TPOrderClaintId=null;
    private final String symbol;
    public boolean useTP;
    public double DCASize;
    public double nextDCAPrice;
    public boolean activeFirstTP=false,activeFirstDCA=false;
    //public ArrayList<Order> activeDCAOrders = new ArrayList<>();


    public boolean needToDCA=false;
    PositionSide positionSide;


    public BaseDCA(double initialPrice, double maxDCACount, double initialAmount, double amountFactor,
                   PositionSide positionSide, double[] exitPrices, String symbol, boolean useTP, DataHolder dataHolder) {
        this.InitialPrice = initialPrice;
        this.maxDCACount = maxDCACount;
        this.InitialAmount = initialAmount;
        this.amountFactor = amountFactor;
        this.positionSide=positionSide;
        this.exitPrices=exitPrices;
        this.symbol=symbol;
        this.useTP=useTP;
        this.dataHolder=dataHolder;
        this.DCASize=calculateDCASize();
//        if (useTP){
//            for (double exitPrice : exitPrices) {
//                switch (positionSide) {
//                    case SHORT:
//                        TakeProfit(new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT, Config.ONE_HANDRED)//todo change to take profit type
//                                , dataHolder, exitPrice, initialAmount);
//                        break;
//                    case LONG:
//                        TakeProfit(new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, Config.ONE_HANDRED)
//                                , dataHolder, exitPrice,initialAmount);
//                        break;
//                }
//
//            }
//        }
    }


    public Instructions run(DataHolder realTimeData) {
        setNeedToDCA(realTimeData);
        if(getNeedToDCA()){
            switch (positionSide){
                //// TODO: 4/5/2021  includ position side BOTH
                case SHORT:
                    return new DCAInstructions(PositionHandler.DCAType.SHORT_DCA_LIMIT,calculateDCASize());
                case LONG:
                    return new DCAInstructions(PositionHandler.DCAType.LONG_DCA_LIMIT,calculateDCASize());
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
    public double[] getexitPrices() {
        return exitPrices;
    }

    public double calculateDCASize(){
        if (DCACount==0 ) return  getInitialAmount()*amountFactor;
        return getInitialAmount()*DCACount*amountFactor;
    }

    @Override
    public void updateExitPrice(double qty,DataHolder realTimeData) {
        double DCAOrderSize =getInitialAmount();
        for (int i=0;i<DCACount;i++){
            DCAOrderSize +=DCAOrderSize*getAmountFactor();
        }
        if(exitPrices!=null) {
            if (getDidDCA()) {
                for (int i=0; i< exitPrices.length;i++) {

                    if (getPositionSide() == PositionSide.LONG) {
                        exitPrices[i] -= getStep() * (calculateDCASize() / DCAOrderSize);
                    } else {
                        exitPrices[i] += getStep() * (calculateDCASize() / DCAOrderSize);
                    }
                    if (useTP){
                        switch (positionSide){
                            case SHORT:
                                TakeProfit(new SellingInstructions (PositionHandler.ClosePositionTypes.CLOSE_SHORT_LIMIT,Config.ONE_HANDRED)//todo change to take profit type
                                        , exitPrices[i],DCAOrderSize);
                                break;
                            case LONG:
                                TakeProfit(new SellingInstructions (PositionHandler.ClosePositionTypes.SELL_LIMIT,Config.ONE_HANDRED)
                                        , exitPrices[i],DCAOrderSize);
                                break;
                        }
                    }
                    setDidDCA(false);
                    setNeedToDCA(false);

                }
            }
        }
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
        return didDCA;
    }


    @Override
    public boolean getNeedToDCA() {
        return needToDCA;
    }

    public void setTimeToDCA(DataHolder dataHolder) {

    }


    public DCAInstructions getDCAInstructions() {
        return null;
    }


//    public void setDCAInstructions(DCAInstructions dcaInstructions) {

   // }

    public PositionSide getPositionSide() {
        return positionSide;
    }

    @Override
    public void increaseDCACount() {DCACount++;}

    public void DCAOrder (DCAInstructions dcaInstructions, double exitPrice){
        if(!activeFirstDCA) {
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            String sellingQty = String.valueOf(calculateDCASize());
            Order order = new Order();
            long orderIn = System.currentTimeMillis();
            switch (dcaInstructions.getDCAType()) {

                case LONG_DCA_LIMIT:
                    try {
                        order = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
                                sellingQty, String.valueOf(exitPrice), null, symbol + "_DCA_price_" + String.valueOf(exitPrice), null, null, null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price  " + String.valueOf(exitPrice) + " ," + new Date(System.currentTimeMillis()));
                        orderIn = System.currentTimeMillis();
                    } catch (Exception ignored) {
                    }
                    break;
                case LONG_DCA_MARKET:
                    try {
                        order = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.MARKET, null,
                                sellingQty, null, null, null, null, null, null, null, null, null, NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price:  " + String.valueOf(exitPrice) + " ," + new Date(System.currentTimeMillis()));
                        orderIn = System.currentTimeMillis();
                    } catch (Exception ignored) {
                    }
                    break;
                case SHORT_DCA_LIMIT:
                    try {
                        order = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, null,
                                sellingQty, null, null, null, null, null, null, null, null, null, NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price:  " + String.valueOf(exitPrice) + " ," + new Date(System.currentTimeMillis()));
                        orderIn = System.currentTimeMillis();
                    } catch (Exception ignored) {
                    }
                    break;
                case SHORT_DCA_MARKET:
                    try {
                        order = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.MARKET, null,
                                sellingQty, null, null, null, null, null, null, null, null, null, NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("DCA price:  " + String.valueOf(exitPrice) + " ," + new Date(System.currentTimeMillis()));
                        orderIn = System.currentTimeMillis();
                    } catch (Exception ignored) {
                    }
                    break;
            }
            if (dcaInstructions.getDCAType() == PositionHandler.DCAType.LONG_DCA_LIMIT || dcaInstructions.getDCAType() == PositionHandler.DCAType.SHORT_DCA_LIMIT) {
                long cur = System.currentTimeMillis();
                while (cur - orderIn < 4000) cur = System.currentTimeMillis();
            }
        }
    }

    public boolean isActiveFirstTP() {
        return activeFirstTP;
    }

    public void setActiveFirstTP(boolean activeFirstTP) {
        this.activeFirstTP = activeFirstTP;
    }

    public void TakeProfit (SellingInstructions sellingInstructions, double exitPrice, double qty){
        if(!activeFirstTP) {
            TelegramMessenger.sendToTelegram("Posting TP order ");
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            if (TPOrderClaintId != null) {
                syncRequestClient.cancelOrder(symbol, System.currentTimeMillis(), TPOrderClaintId);
                TPOrderClaintId = null;
            }

            String sellingQty = String.valueOf(calculateTotalAmount());
            switch (sellingInstructions.getType()) {

                case STAY_IN_POSITION:
                    break;

                case SELL_LIMIT:
                    try {
                        TPOrderClaintId = String.valueOf(System.currentTimeMillis());
                        syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                                sellingQty, String.valueOf(exitPrice), Config.REDUCE_ONLY, String.valueOf(TPOrderClaintId), String.valueOf(exitPrice - (exitPrice / 1000)), null, null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(exitPrice) + " ," + new Date(System.currentTimeMillis()));

                    } catch (Exception ignored) {
                    }
                    break;

                case SELL_MARKET:
                    try {
                        syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.MARKET, null,
                                sellingQty, null, Config.REDUCE_ONLY, null, String.valueOf(exitPrice), null, null, null, null, null, NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(exitPrice) + " ," + new Date(System.currentTimeMillis()));
                        TPOrderClaintId = String.valueOf(System.currentTimeMillis());
                    } catch (Exception ignored) {
                    }
                    break;


                case CLOSE_SHORT_LIMIT:
                    try {
                        TPOrderClaintId = String.valueOf(System.currentTimeMillis());
                        syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.TAKE_PROFIT, TimeInForce.GTC,
                                sellingQty, String.valueOf(exitPrice), Config.REDUCE_ONLY, TPOrderClaintId, String.valueOf(exitPrice + (exitPrice / 1000)), null, null, null, null, WorkingType.MARK_PRICE.toString(), NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(exitPrice) + " ," + new Date(System.currentTimeMillis()));
                    } catch (Exception ignored) {
                    }
                    break;

                case CLOSE_SHORT_MARKET:
                    try {
                        syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.MARKET, null,
                                sellingQty, null, Config.REDUCE_ONLY, null, String.valueOf(exitPrice), null, null, null, null, null, NewOrderRespType.RESULT);
                        TelegramMessenger.sendToTelegram("Selling price:  " + String.valueOf(exitPrice) + " ," + new Date(System.currentTimeMillis()));
                        TPOrderClaintId = String.valueOf(System.currentTimeMillis());
                    } catch (Exception ignored) {
                    }
                    break;


                default:

            }

        }
    }
   // public ArrayList getActiveDCAOrders(int)
}

