package main.java.data;

import data.NegativeMoneyFlowIndicator;
import data.PositiveMoneyFlowIndicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.TypicalPriceIndicator;
import org.ta4j.core.num.Num;

public class MFIIndicator extends CachedIndicator<Num>{
    private final TypicalPriceIndicator typicalPriceIndicator;
    private final PositiveMoneyFlowIndicator positiveMoneyFlowIndicator;
    private final NegativeMoneyFlowIndicator negativeMoneyFlowIndicator;
    private final int barCount;

    public MFIIndicator(BarSeries series, int barCount) {
        super(series);
        this.typicalPriceIndicator = new TypicalPriceIndicator(series);
        this.positiveMoneyFlowIndicator = new PositiveMoneyFlowIndicator(series, this.typicalPriceIndicator, barCount);
        this.negativeMoneyFlowIndicator = new NegativeMoneyFlowIndicator(series, this.typicalPriceIndicator, barCount);
        this.barCount = barCount;
    }

    protected Num calculate(int index) {
        Num positiveMoneyFlow = (Num)this.positiveMoneyFlowIndicator.getValue(index);
        Num negativeMoneyFlow = (Num)this.negativeMoneyFlowIndicator.getValue(index);
        Num moneyFlowRatio;
        if (negativeMoneyFlow.isZero()){
            return this.numOf(0);
        }
        else{
            moneyFlowRatio = (positiveMoneyFlow.dividedBy(negativeMoneyFlow));
        }

        return  this.numOf(100).minus(this.numOf(100).dividedBy(this.numOf(1).plus(moneyFlowRatio)));
    }

    public String toString() {
        return this.getClass().getSimpleName() + " barCount: " + this.barCount;
    }
}
