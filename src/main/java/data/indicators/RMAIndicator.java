package data.indicators;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.AbstractEMAIndicator;
import org.ta4j.core.num.Num;

public class RMAIndicator extends AbstractEMAIndicator {

    public RMAIndicator(Indicator<Num> indicator, int barCount) {
        super(indicator, barCount, 1.0D / (double)(barCount + 1));
    }
}

