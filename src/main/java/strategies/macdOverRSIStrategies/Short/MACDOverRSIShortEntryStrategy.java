package strategies.macdOverRSIStrategies.Short;

import data.AccountBalance;
import data.Config;
import data.RealTimeData;
import positions.PositionHandler;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.ExitStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIBaseEntryStrategy;
import strategies.macdOverRSIStrategies.MACDOverRSIConstants;
import utils.Trailer;
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
	private final AccountBalance accountBalance;
	private volatile boolean bought = false;


	public MACDOverRSIShortEntryStrategy(){
		accountBalance = AccountBalance.getAccountBalance();
		System.out.println("short");
	}

	@Override
	public synchronized PositionHandler run(RealTimeData realTimeData, String symbol) {
		boolean notInPosition = accountBalance.getPosition(symbol).getPositionAmt().compareTo(BigDecimal.valueOf(Config.DOUBLE_ZERO)) == Config.ZERO;
		SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
		boolean noOpenOrders = syncRequestClient.getOpenOrders(symbol).size() == Config.ZERO;
		boolean currentPriceBelowSMA = BigDecimal.valueOf(realTimeData.getSMAValueAtIndex(realTimeData.getLastIndex())).compareTo(realTimeData.getCurrentPrice()) >= Config.ZERO;
		if (currentPriceBelowSMA && notInPosition && noOpenOrders) {
			boolean rule1 = realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.DOWN, RealTimeData.CandleType.CLOSE, Config.ZERO);
			if (rule1){
				if (bought)return null;
				return buyAndCreatePositionHandler(realTimeData, symbol);
			}
			else{
				if (realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()) > Config.ZERO && decliningPyramid(realTimeData, DecliningType.POSITIVE)){
					if (bought) return null;
					return buyAndCreatePositionHandler(realTimeData, symbol);
				}
			}
			bought = false;
		}
		return null;
	}


	//todo: check short!
	private PositionHandler buyAndCreatePositionHandler(RealTimeData realTimeData, String symbol) {
		bought = true;
		try{
			TelegramMessenger.sendToTelegram("buying short: " + new Date(System.currentTimeMillis()));
			SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
			syncRequestClient.changeInitialLeverage(symbol,leverage);
			BigDecimal currentPrice = realTimeData.getCurrentPrice();
			String buyingQty = utils.Utils.getBuyingQtyAsString(realTimeData, symbol,leverage,requestedBuyingAmount);
			Order buyOrder = syncRequestClient.postOrder(symbol, OrderSide.SELL, null, OrderType.LIMIT, TimeInForce.GTC,
					buyingQty,currentPrice.toString(),null,null, null, null, WorkingType.MARK_PRICE, NewOrderRespType.RESULT);
			TelegramMessenger.sendToTelegram("buying long: buyOrder: "+ buyOrder.toString() + new Date(System.currentTimeMillis()));
			ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
			exitStrategies.add(new MACDOverRSIShortExitStrategy1());
			exitStrategies.add(new MACDOverRSIShortExitStrategy2());
			exitStrategies.add(new MACDOverRSIShortExitStrategy3(new Trailer(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.SHORT)));
			exitStrategies.add(new MACDOverRSIShortExitStrategy4(new Trailer(currentPrice, MACDOverRSIConstants.POSITIVE_TRAILING_PERCENTAGE, PositionSide.SHORT)));
			exitStrategies.add(new MACDOverRSIShortExitStrategy5(new Trailer(currentPrice, MACDOverRSIConstants.CONSTANT_TRAILING_PERCENTAGE, PositionSide.SHORT)));
			TelegramMessenger.sendToTelegram("buying short: " + "buyOrder: "+ buyingQty + " " + new Date(System.currentTimeMillis()));
			return new PositionHandler(buyOrder ,exitStrategies);
		}catch (Exception ignored){}
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
