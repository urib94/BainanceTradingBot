package Strategies.MACDOverRSIStrategies.Short;

import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import SingletonHelpers.RequestClient;
import Strategies.EntryStrategy;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseEntryStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.NewOrderRespType;
import com.binance.client.api.model.enums.OrderSide;
import com.binance.client.api.model.enums.OrderType;
import com.binance.client.api.model.enums.WorkingType;
import com.binance.client.api.model.trade.Order;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MACDoverRSIShortEntryStrategy extends MACDOverRSIBaseEntryStrategy {

	double takeProfitPercentage = MACDOverRSIConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
	private double stopLossPercentage = MACDOverRSIConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
	private int leverage = MACDOverRSIConstants.DEFAULT_LEVERAGE;
	private  BigDecimal requestedBuyingAmount = MACDOverRSIConstants.DEFAULT_BUYING_AMOUNT;

	@Override
	public PositionHandler run(RealTimeData realTimeData, String symbol) {
		boolean rule1 = realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.DOWN, RealTimeData.CandleType.OPEN,0);
		boolean rule2 = realTimeData.getMacdOverRsiSignalLineValueAtIndex(realTimeData.getLastCloseIndex()) < 0;
		boolean currentPriceBelowSMA = BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(Config.CANDLE_NUM)).compareTo(realTimeData.getCurrentPrice()) > Config.ZERO;
		if (currentPriceBelowSMA){
			if (rule1) return buyAndCreatePositionHandler(realTimeData,symbol);
			if (rule2 && urisRulesOfEntry(realTimeData)) return buyAndCreatePositionHandler(realTimeData,symbol);
		}
		return null;
	}

	private PositionHandler buyAndCreatePositionHandler(RealTimeData realTimeData, String symbol) {
		SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
		syncRequestClient.changeInitialLeverage(symbol,leverage);
		String buyingQty = Utils.Utils.getBuyingQtyAsString(realTimeData, symbol,leverage,requestedBuyingAmount);
		Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.MARKET, null,
				buyingQty,null,null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);//TODO: check if buying with market price is ok.

		ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
		exitStrategies.add(new MACDOverRSIShortExitStrategy1());
		exitStrategies.add(new MACDOverRSIShortExitStrategy2());
		exitStrategies.add(new MACDOverRSIShortExitStrategy3());
		exitStrategies.add(new MACDOverRSIShortExitStrategy4());
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
