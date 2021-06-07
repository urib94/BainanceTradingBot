package main.java.data;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.TypicalPriceIndicator;
import org.ta4j.core.indicators.statistics.MeanDeviationIndicator;
import org.ta4j.core.num.Num;

public class CCICIndicator extends CachedIndicator<Num> {
    private final Num factor = this.numOf(0.015D);
    private final ClosePriceIndicator closePriceIndicator;
    private final SMAIndicator smaInd;
    private final MeanDeviationIndicator meanDeviationInd;
    private final int barCount;

    public CCICIndicator(BarSeries series, int barCount) {
        super(series);
        this.closePriceIndicator = new ClosePriceIndicator(series);
        this.smaInd = new SMAIndicator(this.closePriceIndicator, barCount);
        this.meanDeviationInd = new MeanDeviationIndicator(this.closePriceIndicator, barCount);
        this.barCount = barCount;
    }

    protected Num calculate(int index) {
        Num closePrice = (Num)this.closePriceIndicator.getValue(index);
        Num priceAvg = (Num)this.smaInd.getValue(index);
        Num meanDeviation = (Num)this.meanDeviationInd.getValue(index);
        return meanDeviation.isZero() ? this.numOf(0) : closePrice.minus(priceAvg).dividedBy(meanDeviation.multipliedBy(this.factor));
    }

    public String toString() {
        return this.getClass().getSimpleName() + " barCount: " + this.barCount;
    }

}
