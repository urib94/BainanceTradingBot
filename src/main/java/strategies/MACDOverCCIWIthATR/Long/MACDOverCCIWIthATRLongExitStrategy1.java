package strategies.MACDOverCCIWIthATR.Long;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.Instructions;
import positions.PositionHandler;
import positions.SellingInstructions;
import strategies.MACDOverCCIWIthATR.BaseMACDOverCCIWIthATRExitStrategy;
import strategies.MACDOverCCIWIthATR.MACDOverCCIWIthATRConstants;

public class MACDOverCCIWIthATRLongExitStrategy1 extends BaseMACDOverCCIWIthATRExitStrategy {
    public double[] exitPrice;
    public double ATRValue;


    public MACDOverCCIWIthATRLongExitStrategy1(double initiallPrice, double maxDCACount, double initialAmount, double amountFactor,
                                               PositionSide positionSide, double[] exitPrices,String symbol, boolean useTP,DataHolder dataHolder, double ATRValue) {
        super(initiallPrice, maxDCACount, initialAmount, amountFactor, positionSide, exitPrices,symbol,useTP,dataHolder);

        this.exitPrices = exitPrice;
        this.ATRValue = ATRValue;

    }

//    public MACDOverCCIWIthATRLongExitStrategy1(double initiallPrice, double maxDCACount, double initialAmount, double amountFactor, PositionSide positionSide, double[] exitPrices, PositionHandler positionHandler) {
//        super(initiallPrice, maxDCACount, initialAmount, amountFactor, positionSide, exitPrices);
//        this.positionHandler = positionHandler;
//        this.exitPrices = exitPrices;
//
//    }

    @Override
    public void updateExitStrategy() {


    }

    @Override
    public Instructions run(DataHolder realTimeData) {
        for (double priceToExit : exitPrices) {
            if (realTimeData.getCurrentPrice() <= priceToExit) {
                return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, MACDOverCCIWIthATRConstants.MACDOverCCIWIthATR_SELLING_PERCENTAGE);
            }
        }
        return null;
    }
}
