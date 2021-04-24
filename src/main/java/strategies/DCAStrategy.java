package strategies;

import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.Order;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import positions.SellingInstructions;

public interface DCAStrategy  {
    public boolean needToDCA =false;

    PositionSide getPositionSide();

    public double gettPPrice();

    int getdCACount();

    double  getMaxDCACount();

    public void setNeedToDCA(boolean valToSet);

    public  boolean getNeedToDCA();

    double calculateTotalAmount();

    public boolean getNeedToTP();

    public void TakeProfit (SellingInstructions sellingInstructions, double qty,double entryPrice,DataHolder realTimeData);

    public double getInitialAmount();
    public DCAInstructions getDCAInstructions ();
    //public void setDCAInstructions(DCAInstructions dcaInstructions);


    Instructions run(DataHolder realTimeData);

    void DCAOrder(DCAInstructions dcaInstructions,DataHolder realTimeData);
    double getNextDCASize();

    void updateDCAPrices(double currentPrice);

    Order getTpOrder();
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
