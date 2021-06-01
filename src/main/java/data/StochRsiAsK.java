package data;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.*;
import org.ta4j.core.num.Num;

public class StochRsiAsK extends CachedIndicator<Num> {
    private int stochLength;
    private StochasticRSIIndicator stochasticRSIIndicator;

    public StochRsiAsK(RSIIndicator rsiIndicator, int rSIbarCount, int stochLength) {
        super( rsiIndicator);
        stochasticRSIIndicator = new StochasticRSIIndicator(rsiIndicator,stochLength);
    }

    protected Num calculate(int index) {
        SMAIndicator smaIndicator = new SMAIndicator((Indicator<Num>) stochasticRSIIndicator, stochLength);
        System.out.println(smaIndicator.getValue(index).doubleValue());
        return (Num)smaIndicator.getValue(index);
    }

}
