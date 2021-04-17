package strategies;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import positions.SellingInstructions;

public interface DCAStrategy  {
    public boolean needToDCA =false;

    public double gettPPrice();

    int getdCACount();

    double  getMaxDCACount();

    public void setNeedToDCA(boolean valToSet);

    public PositionSide getPositionSide();

    public  boolean getNeedToDCA();

    public boolean getNeedToTP();

    public void TakeProfit (SellingInstructions sellingInstructions, double qty,double entryPrice,DataHolder realTimeData);

    public double getInitialAmount();
    public DCAInstructions getDCAInstructions ();
    //public void setDCAInstructions(DCAInstructions dcaInstructions);


    Instructions run(DataHolder realTimeData);

    void DCAOrder(DCAInstructions dcaInstructions,DataHolder realTimeData);
     double getNextDCASize();

    void updateDCAPrices(double currentPrice);

    double distanceToTP();

    public double getNextDCAPrice();

    boolean isNewPosition();

    public enum DCAType {
        LONG_DCA_LIMIT,
        LONG_DCA_MARKET,
        SHORT_DCA_LIMIT,
        SHORT_DCA_MARKET
    }
}
