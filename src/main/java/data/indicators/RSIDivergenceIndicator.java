package data.indicators;

import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.DifferenceIndicator;
import org.ta4j.core.indicators.helpers.GainIndicator;
import org.ta4j.core.indicators.helpers.LossIndicator;
import org.ta4j.core.num.Num;

public class RSIDivergenceIndicator extends CachedIndicator<Num> {
    private final SpecialRSIIndicator fastRsiIndicator;
    private final SpecialRSIIndicator slowRsiIndicator;

    public RSIDivergenceIndicator(BaseBarSeries series, int slowBarCount, int fastBarCount) {
        super(series);
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        PositiveCloseDifferenceIndicator positiveCloseDifferenceIndicator = new PositiveCloseDifferenceIndicator(closePriceIndicator);
        NegativeDifferenceIndicator negativeDifferenceIndicator = new NegativeDifferenceIndicator(closePriceIndicator);
        RMAIndicator upFast = new RMAIndicator(positiveCloseDifferenceIndicator , fastBarCount);
        RMAIndicator downFast = new RMAIndicator(negativeDifferenceIndicator , fastBarCount);
        fastRsiIndicator = new SpecialRSIIndicator(series, upFast, downFast);
        RMAIndicator upSlow = new RMAIndicator(positiveCloseDifferenceIndicator , slowBarCount);
        RMAIndicator downSlow = new RMAIndicator(negativeDifferenceIndicator , slowBarCount);
        slowRsiIndicator = new SpecialRSIIndicator(series, upSlow, downSlow);
    }

    protected Num calculate(int index) {
        Num rsiFast = this.fastRsiIndicator.getValue(index);
        Num rsiSlow = this.slowRsiIndicator.getValue(index);
        return rsiFast.minus(rsiSlow);
    }
}
