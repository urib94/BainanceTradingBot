package main.java.data;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.num.Num;

public class StochDIndicator extends CachedIndicator<Num> {

    private Indicator<Num> indicator;

    public StochDIndicator(StochRsiAsK k, int barCount) {
        this(new SMAIndicator(k, barCount));
    }

    public StochDIndicator(Indicator<Num> indicator) {
        super(indicator);
        this.indicator = indicator;
    }

    @Override
    protected Num calculate(int index) {
        return indicator.getValue(index);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + indicator;
    }
}

