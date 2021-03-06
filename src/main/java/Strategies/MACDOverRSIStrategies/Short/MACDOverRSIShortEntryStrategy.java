package Strategies.MACDOverRSIStrategies.Short;

import Data.AccountBalance;
import Data.Config;
import Data.RealTimeData;
import Positions.PositionHandler;
import SingletonHelpers.RequestClient;
import SingletonHelpers.TelegramMessenger;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIBaseEntryStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class MACDOverRSIShortEntryStrategy extends MACDOverRSIBaseEntryStrategy {

	double takeProfitPercentage = MACDOverRSIConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
	private double stopLossPercentage = MACDOverRSIConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
	private int leverage = MACDOverRSIConstants.DEFAULT_LEVERAGE;
	private  BigDecimal requestedBuyingAmount = MACDOverRSIConstants.DEFAULT_BUYING_AMOUNT;
	private AccountBalance accountBalance;


	public MACDOverRSIShortEntryStrategy(){
		accountBalance = AccountBalance.getAccountBalance();
		System.out.println("short");
	}

	@Override
	public synchronized PositionHandler run(RealTimeData realTimeData, String symbol) {//TODO: not in position too slow.
		boolean notInPosition = accountBalance.getPosition(symbol).getPositionAmt().compareTo(BigDecimal.valueOf(Config.DOUBLE_ZERO)) == Config.ZERO;
		SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
		boolean noOpenOrders = syncRequestClient.getOpenOrders(symbol).size() == Config.ZERO;
		boolean currentPriceBelowSMA = true;//BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(realTimeData.getLastIndex())).compareTo(realTimeData.getCurrentPrice()) >= Config.ZERO;
		if (currentPriceBelowSMA && notInPosition && noOpenOrders) {
			boolean rule1 = realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.DOWN, RealTimeData.CandleType.CLOSE, Config.ZERO);
			if (rule1) return buyAndCreatePositionHandler(realTimeData, symbol);
			else{
				if (realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) > Config.ZERO && decliningPyramid(realTimeData, DecliningType.POSITIVE)) return buyAndCreatePositionHandler(realTimeData, symbol);
			}
		}
		return null;
	}


	//todo: check short!
	private PositionHandler buyAndCreatePositionHandler(RealTimeData realTimeData, String symbol) {
		try{
			TelegramMessenger.sendToTelegram("buying short: " + new Date(System.currentTimeMillis()));
			SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
			syncRequestClient.changeInitialLeverage(symbol,leverage);
			String buyingQty = Utils.Utils.getBuyingQtyAsString(realTimeData, symbol,leverage,requestedBuyingAmount);
			Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.LIMIT, TimeInForce.GTC,
					buyingQty,realTimeData.getCurrentPrice().toString(),null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
			ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
			//exitStrategies.add(new MACDOverRSIShortExitStrategy1());
			exitStrategies.add(new MACDOverRSIShortExitStrategy2());
			exitStrategies.add(new MACDOverRSIShortExitStrategy3());
			//exitStrategies.add(new MACDOverRSIShortExitStrategy4());
			TelegramMessenger.sendToTelegram("buying short: " + "buyOrder: "+ buyingQty + " " + new Date(System.currentTimeMillis()));
			return new PositionHandler(buyOrder ,exitStrategies);
		}catch (Exception exception){
			exception.printStackTrace();}
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
