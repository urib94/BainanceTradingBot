package main.java.data;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.num.Num;

public class NegativeMoneyFlowIndicator extends CachedIndicator<Num> {
    private final Indicator<Num> indicator;
    private final VolumeIndicator volumeIndicator;
    private final int barCount;

    public NegativeMoneyFlowIndicator(BarSeries series, Indicator<Num> indicator, int barCount) {
        super(indicator);
        this.indicator = indicator;
        this.volumeIndicator = new VolumeIndicator(series);
        this.barCount = barCount;
    }

    protected Num calculate(int index) {
        Num curr, prev;
        Num sum = this.numOf(0);
        int realBarCount;
        for (realBarCount = Math.max(0, index - this.barCount + 1); realBarCount <= index; ++realBarCount) {
            curr = (Num) this.indicator.getValue(realBarCount);
            prev = (Num) this.indicator.getValue(realBarCount - 1);
            if (prev.isGreaterThan(curr)) {
                sum = sum.plus((Num)this.indicator.getValue(realBarCount).multipliedBy((Num) this.volumeIndicator.getValue(realBarCount)));
            }
        }
        return sum;
    }


    public String toString(){
        return this.getClass().getSimpleName() + " barCount: " + this.barCount;
    }
}

