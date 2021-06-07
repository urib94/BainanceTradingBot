package data.indicators;

import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.MMAIndicator;
import org.ta4j.core.indicators.helpers.GainIndicator;
import org.ta4j.core.indicators.helpers.LossIndicator;
import org.ta4j.core.num.Num;

public class SpecialRSIIndicator extends CachedIndicator<Num> {
    private final RMAIndicator averageGainIndicator;
    private final RMAIndicator averageLossIndicator;

    public SpecialRSIIndicator(BaseBarSeries series, RMAIndicator up, RMAIndicator down) {
        super(series);
        this.averageGainIndicator = up;
        this.averageLossIndicator = down;
    }

    protected Num calculate(int index) {
        Num averageGain = (Num)this.averageGainIndicator.getValue(index);
        Num averageLoss = (Num)this.averageLossIndicator.getValue(index);
        if (averageLoss.isZero()) {
            return averageGain.isZero() ? this.numOf(0) : this.numOf(100);
        } else {
            Num relativeStrength = averageGain.dividedBy(averageLoss);
            return this.numOf(100).minus(this.numOf(100).dividedBy(this.numOf(1).plus(relativeStrength)));
        }
    }
}
