package strategies.rsiStrategies;
import data.DataHolder;
import positions.Instructions;
import positions.PositionHandler;
import positions.SellingInstructions;
import strategies.ExitStrategy;
import strategies.PositionInStrategy;

public class RSIExitStrategy1 implements ExitStrategy {
	private PositionInStrategy positionInStrategy = PositionInStrategy.POSITION_ONE;

	public Instructions run(DataHolder realTimeData) {
		if (positionInStrategy == PositionInStrategy.POSITION_ONE) {
			if (realTimeData.above(DataHolder.IndicatorType.RSI,DataHolder.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_OVER_THRESHOLD1)
					&& !(realTimeData.above(DataHolder.IndicatorType.RSI,DataHolder.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_2_OVER_THRESHOLD1))) {
				System.out.println(this.getClass().getSimpleName() + "Switching to Position 2");
				positionInStrategy = PositionInStrategy.POSITION_TWO;
			}
			return null;
		} else if (positionInStrategy == PositionInStrategy.POSITION_TWO) {
			if ( ! realTimeData.above(DataHolder.IndicatorType.RSI,DataHolder.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_UNDER_THRESHOLD1)) {
				positionInStrategy = PositionInStrategy.POSITION_THREE;
				System.out.println(this.getClass().getSimpleName() + " Switching to Position 3. Returning 50% ");
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, RSIConstants.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE1);
			}
		} else if(positionInStrategy == PositionInStrategy.POSITION_THREE) {
			if (! realTimeData.above(DataHolder.IndicatorType.RSI,DataHolder.CandleType.CLOSE, RSIConstants.RSI_EXIT_OPTION_1_UNDER_THRESHOLD2)) {
				System.out.println(this.getClass().getSimpleName() + " Switching to Position 1. Returning 100% ");
				positionInStrategy = PositionInStrategy.POSITION_ONE;
				return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, RSIConstants.RSI_EXIT_OPTION_1_SELLING_PERCENTAGE2);
			}
		}
		return null;
	}
	@Override
	public void updateExitStrategy() {

	}
}
