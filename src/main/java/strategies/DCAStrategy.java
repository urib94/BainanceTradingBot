package strategies;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import positions.SellingInstructions;

public interface DCAStrategy  {
    public boolean needToDCA =false;


    public void setDidDCA(boolean valToSet);

    public boolean getDidDCA ();

    public void updatTP(double qty, DataHolder realTimeData);

    public void setNeedToDCA(DataHolder dataHolder);

    public void setNeedToDCA(DataHolder dataHolder , double[] closePrices);

    public double getTPPrice();

    public void setNeedToDCA(boolean valToSet);

    public PositionSide getPositionSide();

    public default boolean getNeedToDCA(){
        return needToDCA;
    }

    public boolean getNeedToTP();

    public void TakeProfit (SellingInstructions sellingInstructions, double qty,DataHolder realTimeData);

    public double getInitialAmount();
    public DCAInstructions getDCAInstructions ();
    //public void setDCAInstructions(DCAInstructions dcaInstructions);


    Instructions run(DataHolder realTimeData);

    void increaseDCACount();

    void DCAOrder(DCAInstructions dcaInstructions);
     double getDCASize();

    void updateDCAPrices();

    public double getNextDCAPrice();


    public enum DCAType {
        LONG_DCA_LIMIT,
        LONG_DCA_MARKET,
        SHORT_DCA_LIMIT,
        SHORT_DCA_MARKET
    }
}
