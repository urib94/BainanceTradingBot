package data.indicators;

import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.MMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class RSIDivergenceIndicator extends CachedIndicator<Num> {
    private final SpecialRSIIndicator fastRsiIndicator;
    private final SpecialRSIIndicator slowRsiIndicator;

    public RSIDivergenceIndicator(BaseBarSeries series, int slowBarCount, int fastBarCount) {
        super(series);
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        PositiveDifferenceIndicator positiveCloseDifferenceIndicator = new PositiveDifferenceIndicator(closePriceIndicator);
        NegativeDifferenceIndicator negativeDifferenceIndicator = new NegativeDifferenceIndicator(closePriceIndicator);
        MMAIndicator upFast = new MMAIndicator(positiveCloseDifferenceIndicator , fastBarCount);
        MMAIndicator downFast = new MMAIndicator(negativeDifferenceIndicator , fastBarCount);
        fastRsiIndicator = new SpecialRSIIndicator(series, upFast, downFast);
        MMAIndicator upSlow = new MMAIndicator(positiveCloseDifferenceIndicator , slowBarCount);
        MMAIndicator downSlow = new MMAIndicator(negativeDifferenceIndicator , slowBarCount);
        slowRsiIndicator = new SpecialRSIIndicator(series, upSlow, downSlow);
    }

    protected Num calculate(int index) {
        Num rsiFast = this.fastRsiIndicator.getValue(index);
        Num rsiSlow = this.slowRsiIndicator.getValue(index);
        return rsiFast.minus(rsiSlow);
    }
}
