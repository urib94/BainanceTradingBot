package utilities;

import Data.Config;
import Data.RealTimeData;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RSIUtiles {

	public static boolean aboveThreshold(BigDecimal value, int threshold) {
		return value.compareTo(new BigDecimal(threshold)) > 0;
	}
	public static boolean belowThreshold(BigDecimal value, int threshold) {
		return value.compareTo(new BigDecimal(threshold)) < 0;
	}
}
