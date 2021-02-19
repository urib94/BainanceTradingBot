package Strategies;

import Data.AccountBalance;
import Data.RealTimeData;

public class RSIExitStrategy implements ExitStrategy {
	private RSI_EXIT_STRATEGY_TYPE type;

	public RSIExitStrategy(RSI_EXIT_STRATEGY_TYPE type) {
		this.type = type;
	}
	@Override
	public void run(AccountBalance accountBalance, RealTimeData realTimeData) {

	}
}
