package Strategies.MACDOverRSIStrategies.Long;

import Data.Config;
import Data.RealTimeData;
import SingletonHelpers.BinanceInfo;
import SingletonHelpers.RequestClient;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.NewOrderRespType;
import com.binance.client.api.model.enums.OrderSide;
import com.binance.client.api.model.enums.OrderType;
import com.binance.client.api.model.enums.TimeInForce;
import com.binance.client.api.model.trade.Order;

import java.math.BigDecimal;

public class MACDOverRSILongExitStrategy2 implements ExitStrategy {

	@Override
	public BigDecimal run(RealTimeData realTimeData) {
		if (realTimeData.urisRulesOfEntry()) {
			return MACDOverRSIConstants.MACD_OVER_RSI_EXIT_SELLING_PERCENTAGE; //TODO: exit 100% with trailing 0.25.
		}
		return null;
	}
}
