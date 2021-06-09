package data.indicators;

import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.MMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

public class RSIDivergenceIndicator extends CachedIndicator<Num> {
    private final SpecialRSIIndicator fastRsiIndicator;
    private final SpecialRSIIndicator slowRsiIndicator;
    private MMAIndicator upSlow  ;
    private MMAIndicator downSlow;
    private MMAIndicator upFast  ;
    private MMAIndicator downFast;

    public RSIDivergenceIndicator(BaseBarSeries series, int slowBarCount, int fastBarCount) {
        super(series);
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        PositiveDifferenceIndicator positiveCloseDifferenceIndicator = new PositiveDifferenceIndicator(closePriceIndicator);
        NegativeDifferenceIndicator negativeDifferenceIndicator = new NegativeDifferenceIndicator(closePriceIndicator);
        upFast   = new MMAIndicator(positiveCloseDifferenceIndicator , fastBarCount);
        downFast = new MMAIndicator(negativeDifferenceIndicator , fastBarCount);
        fastRsiIndicator = new SpecialRSIIndicator(series, upFast, downFast);
        upSlow   = new MMAIndicator(positiveCloseDifferenceIndicator , slowBarCount);
        downSlow = new MMAIndicator(negativeDifferenceIndicator , slowBarCount);
        slowRsiIndicator = new SpecialRSIIndicator(series, upSlow, downSlow);
    }

    protected Num calculate(int index) {
//        System.out.println("*******fast RSI = " + this.fastRsiIndicator.getValue(index));
//        System.out.println("up = " + upFast.getValue(index));
//        System.out.println("down = " + this.downFast.getValue(index));
//        System.out.println("\n********slow RSI = " + this.slowRsiIndicator.getValue(index));
//        System.out.println("up = " + this.upSlow.getValue(index));
//        System.out.println("down = " + this.downSlow.getValue(index));
        Num rsiFast = this.fastRsiIndicator.getValue(index);
        Num rsiSlow = this.slowRsiIndicator.getValue(index);
        return rsiFast.minus(rsiSlow);
    }
}
