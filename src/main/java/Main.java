import CodeExecution.InvestmentManager;
import Data.*;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.RSIStrategies.RSIEntryStrategy;
import CodeExecution.RealTimeCommandOperator;
import com.binance.client.api.SubscriptionClient;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;

import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main {
    public static void main(String[] args){ //TODO: allow to orders to run parallel
        AccountBalance accountBalance = AccountBalance.getAccountBalance(); //!Don't touch
        BinanceInfo binanceInfo = BinanceInfo.getBinanceInfo(); //!Don't touch
        Thread realTimeCommandOperatorThread = new Thread(new RealTimeCommandOperator());
        InvestmentManager investmentManager = new InvestmentManager(CandlestickInterval.ONE_MINUTE, "btcusdt", new RSIEntryStrategy(0.05,6,BigDecimal.valueOf(10.0)));
        investmentManager.run();
    }
}


