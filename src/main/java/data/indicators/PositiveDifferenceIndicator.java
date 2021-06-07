package data.indicators;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.DifferenceIndicator;
import org.ta4j.core.num.Num;

public class PositiveDifferenceIndicator extends CachedIndicator<Num> {
    private final Indicator<Num> indicator;

    public PositiveDifferenceIndicator(Indicator<Num> indicator) {
        super(indicator);
        this.indicator = indicator;
    }
    protected Num calculate(int index) {
        Num curr, prev, output;
        curr = this.indicator.getValue(index);
        prev = this.indicator.getValue(index - 1);
        output = curr.minus(prev);
        if (output.isGreaterThan(this.numOf(0))) return output;
        else return this.numOf(0);
    }

    public String toString(){
        return this.getClass().getSimpleName();
    }
}

