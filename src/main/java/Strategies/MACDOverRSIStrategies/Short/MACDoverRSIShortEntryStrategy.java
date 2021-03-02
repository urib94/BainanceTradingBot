package Strategies.MACDOverRSIStrategies.Short;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import SingletonHelpers.RequestClient;
import Strategies.EntryStrategy;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.Long.MACDOverRSILongExitStrategy1;
import Strategies.MACDOverRSIStrategies.Long.MACDOverRSILongExitStrategy2;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.NewOrderRespType;
import com.binance.client.api.model.enums.OrderSide;
import com.binance.client.api.model.enums.OrderType;
import com.binance.client.api.model.enums.WorkingType;
import com.binance.client.api.model.trade.Loan;
import com.binance.client.api.model.trade.Order;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MACDoverRSIShortEntryStrategy implements EntryStrategy {

	double takeProfitPercentage = MACDOverRSIConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
	private double stopLossPercentage = MACDOverRSIConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
	private int leverage = MACDOverRSIConstants.DEFAULT_LEVERAGE;
	private  BigDecimal requestedBuyingAmount = MACDOverRSIConstants.DEFAULT_BUYING_AMOUNT;
	private boolean borrowFromMargin = true;

	@Override
	public PositionHandler run(RealTimeData realTimeData, String symbol) {
		if (! (BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(Config.CANDLE_NUM)).compareTo(realTimeData.getCurrentPrice()) <= Config.ZERO)){
			SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
			if (borrowFromMargin){
				Loan loan = syncRequestClient.borrow(MACDOverRSIConstants.LEVERAGED_COIN, MACDOverRSIConstants.BORROWING_AMOUNT, MACDOverRSIConstants.BASE_COIN, MACDOverRSIConstants.BASE_COIN_AMOUNT);
				//TODO: MOVE BITCOIN FROM MARGIN TO FUTURE WALLET
				borrowFromMargin = false;
			}

			if (realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.DOWN, RealTimeData.CandleType.CLOSE,0)) {
				//TODO: short stuff
				syncRequestClient.changeInitialLeverage(symbol,leverage);
				String buyingQty = getBuyingQtyAsString(realTimeData, symbol);
				Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.MARKET, null,
						buyingQty,null,null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);//TODO: check if buying with market price is ok.

				ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
				exitStrategies.add(new MACDOverRSIShortExitStrategy1());
				exitStrategies.add(new MACDOverRSIShortExitStrategy2());
				return null;
			}
			if (realTimeData.getMacdOverRsiSignalLineValueAtIndex(realTimeData.getLastCloseIndex()) < 0) {
				if (realTimeData.urisRulesForEntry()) {
					//TODO: short stuff
					ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
					exitStrategies.add(new MACDOverRSIShortExitStrategy1());
					exitStrategies.add(new MACDOverRSIShortExitStrategy2());
					return null;
				}
			}
		}
		return null;
	}






	@Override
	public void setTakeProfitPercentage(double takeProfitPercentage) {
		this.takeProfitPercentage =takeProfitPercentage;
	}

	@Override
	public void setStopLossPercentage(double stopLossPercentage) {
		this.stopLossPercentage = stopLossPercentage;
	}

	@Override
	public void setLeverage(int leverage) {
		this.leverage = leverage;
	}

	@Override
	public void setRequestedBuyingAmount(BigDecimal requestedBuyingAmount) {
		this.requestedBuyingAmount = requestedBuyingAmount;
	}
}
