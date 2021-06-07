package data.indicators;

import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;

public class SpecialRSIIndicator extends CachedIndicator<Num> {
    private final MMAIndicator averageGainIndicator;
    private final MMAIndicator averageLossIndicator;

    public SpecialRSIIndicator(BaseBarSeries series, MMAIndicator up, MMAIndicator down) {
        super(series);
        this.averageGainIndicator = up;
        this.averageLossIndicator = down;
    }

    protected Num calculate(int index) {
        Num averageGain = this.averageGainIndicator.getValue(index);
        Num averageLoss = this.averageLossIndicator.getValue(index);
        if (averageLoss.isZero()) {
            return averageGain.isZero() ? this.numOf(0) : this.numOf(100);
        } else {
            Num relativeStrength = averageGain.dividedBy(averageLoss);
            return this.numOf(100).minus(this.numOf(100).dividedBy(this.numOf(1).plus(relativeStrength)));
        }
    }
}
