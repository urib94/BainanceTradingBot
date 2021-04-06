package TradingTools.DCA;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.DCAInstructions;
import positions.PositionHandler;

public class BaseDCA {
    int DCACount=0;
    double step;
    double stepFactor;
    double InitiallPrice;
    double maxDCACount;
    double InitialAmount;
    double amountFactor;
    double[] exitPrices;

    PositionSide positionSide;

    public BaseDCA(double step, double stepFactor, double initiallPrice, double maxDCACount,
                   double initialAmount, double amountFactor, PositionSide positionSide) {
        this.step = step;
        this.stepFactor = stepFactor;
        this.InitiallPrice = initiallPrice;
        this.maxDCACount = maxDCACount;
        this.InitialAmount = initialAmount;
        this.amountFactor = amountFactor;
        this.positionSide=positionSide;

    }
    public BaseDCA(double initiallPrice, double maxDCACount, double initialAmount, double amountFactor,
                   PositionSide positionSide, double[] exitPrices) {
        this.InitiallPrice = initiallPrice;
        this.maxDCACount = exitPrices.length;
        this.InitialAmount = initialAmount;
        this.amountFactor = amountFactor;
        this.positionSide=positionSide;
        this.exitPrices=exitPrices;
    }

    boolean needToAdd(DataHolder dataHolder){
        if(maxDCACount<DCACount) return false;
        switch (positionSide){
            case SHORT:
                if(dataHolder.getCurrentPrice()>=getInitiallPrice()+step*100){
                    DCACount++;
                    return true;
                }
                break;
            case LONG:
                if(dataHolder.getCurrentPrice()<=(getInitiallPrice()-step*100)){
                    DCACount++;
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean needToAdd(DataHolder dataHolder , double[] closePrices){
        switch (positionSide){
            case SHORT:
                for (double price : closePrices) {
                    if (dataHolder.getCurrentPrice() <= price) return true;
                }
                break;
            case LONG:
                for (double price : closePrices) {
                    if (dataHolder.getCurrentPrice() >= price) return true;
                }
            }
        return false;

    }
    public double calculateDCASize(){
        return getInitialAmount()*DCACount*amountFactor;
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

    public double getInitiallPrice() {
        return InitiallPrice;
    }

    public void setInitiallPrice(double initiallPrice) {
        InitiallPrice = initiallPrice;
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


    public void setTimeToDCA(DataHolder dataHolder) {

    }


    public DCAInstructions getDCAInstructions() {
        return null;
    }


    public void setDCAInstructions(DCAInstructions dcaInstructions) {

    }


    public DCAInstructions run(DataHolder realTimeData) {
        if(needToAdd(realTimeData)){
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
}
