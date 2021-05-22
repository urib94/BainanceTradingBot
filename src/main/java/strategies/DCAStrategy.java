package strategies;

import data.DataHolder;

public interface DCAStrategy  {

    void run(DataHolder realTimeData, double qty, double averagePrice);

    enum DCAType {
        LONG_DCA_LIMIT,
        LONG_DCA_MARKET,
        SHORT_DCA_LIMIT,
        SHORT_DCA_MARKET
    }
}
