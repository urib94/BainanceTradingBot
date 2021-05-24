package strategies;

public interface DCAStrategy  {

    void run(double qty, double averagePrice);

    enum DCAType {
        LONG_DCA_LIMIT,
        LONG_DCA_MARKET,
        SHORT_DCA_LIMIT,
        SHORT_DCA_MARKET
    }
}
