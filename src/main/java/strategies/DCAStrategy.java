package strategies;

import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.Order;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import positions.SellingInstructions;

public interface DCAStrategy  {

    PositionSide getPositionSide();

    double gettPPrice();

    int getdCACount();

    double  getMaxDCACount();

    boolean getNeedToDCA();

    void TakeProfit (SellingInstructions sellingInstructions, double qty,double entryPrice,DataHolder realTimeData);

    double getInitialAmount();

    DCAInstructions getDCAInstructions ();

    Instructions run(DataHolder realTimeData);

    void DCAOrder(DCAInstructions dcaInstructions,DataHolder realTimeData, double qty);

    void updateDCAPrices(double currentPrice);

    Order getTpOrder();

    enum DCAType {
        LONG_DCA_LIMIT,
        LONG_DCA_MARKET,
        SHORT_DCA_LIMIT,
        SHORT_DCA_MARKET
    }
}
