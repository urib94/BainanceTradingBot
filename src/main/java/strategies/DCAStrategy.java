package strategies;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import positions.SellingInstructions;

public interface DCAStrategy  {
    public boolean needToDCA =false;
    DCAInstructions DCA_INSTRUCTIONS = null;

    public void setDidDCA(boolean valToSet);

    public boolean getDidDCA ();

    public void updateExitPrice(double qty,DataHolder realTimeData);

    public void setNeedToDCA(DataHolder dataHolder);

    public void setNeedToDCA(DataHolder dataHolder , double[] closePrices);

    public double[] getexitPrices();

    public void setNeedToDCA(boolean valToSet);

    public PositionSide getPositionSide();

    public default boolean getNeedToDCA(){
        return needToDCA;
    }

    public void TakeProfit (SellingInstructions sellingInstructions,  double exitPrice, double qty);

    public double getInitialAmount();
    public DCAInstructions getDCAInstructions ();
    //public void setDCAInstructions(DCAInstructions dcaInstructions);


    Instructions run(DataHolder realTimeData);

    void increaseDCACount();

    void DCAOrder(DCAInstructions dcaInstructions, double DCAPrice);
    public double getNextDCAPrice();
}
