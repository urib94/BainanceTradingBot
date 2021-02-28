import Data.*;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.RSIStrategies.RSIEntryStrategy;
import CodeExecution.RealTimeCommandOperator;
import com.binance.client.api.SubscriptionClient;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;

import java.util.ArrayList;

import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main {
    public static void main(String[] args){ //TODO: allow to orders to run parallel
        AccountBalance accountBalance = AccountBalance.getAccountBalance(); //!Don't touch
        BinanceInfo binanceInfo = BinanceInfo.getBinanceInfo(); //!Don't touch
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        SubscriptionClient subscriptionClient = SubscriptionClient.create(Config.API_KEY, Config.SECRET_KEY);
        ArrayList<EntryStrategy> entryStrategies = new ArrayList<>();//TODO: change to map of symbol and strategies so each strategy will have its own subscribe candlestick event
        ArrayList<PositionHandler> positionHandlers = new ArrayList<>();
        ArrayList<Future<?>> futures = new ArrayList<>();
        Thread realTimeCommandOperatorThread = new Thread(new RealTimeCommandOperator());
        realTimeCommandOperatorThread.start();
        ReadWriteLock positionHandlersLock = new ReentrantReadWriteLock();
        entryStrategies.add(new RSIEntryStrategy());
    }
}


