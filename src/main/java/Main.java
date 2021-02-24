import Data.AccountBalance;
import Data.Config;
import Data.RealTimeData;
import Data.RequestClient;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.RSIStrategies.RSIEntryStrategy;
import Utils.TimeConstants;
import com.binance.client.api.SubscriptionClient;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;
import com.binance.client.api.model.market.ExchangeInfoEntry;
import com.binance.client.api.model.market.ExchangeInformation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main {
    public static void main(String[] args){
        LocalDateTime programStartTime = LocalDateTime.now();
        AccountBalance accountBalance = AccountBalance.getAccountBalance();
        RealTimeData realTimeData = new RealTimeData("btcusdt", CandlestickInterval.ONE_MINUTE, Config.CANDLE_NUM);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        SubscriptionClient subscriptionClient = SubscriptionClient.create(Config.API_KEY, Config.SECRET_KEY);
        String listenKey = syncRequestClient.startUserDataStream();
        ArrayList<EntryStrategy> entryStrategies = new ArrayList<>();
        ArrayList<PositionHandler> positionHandlers = new ArrayList<>();
        ReadWriteLock lock = new ReentrantReadWriteLock();
        entryStrategies.add(new RSIEntryStrategy());
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                syncRequestClient.keepUserDataStream(listenKey);
                System.out.println("Sent keep user Alive request");
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, TimeConstants.THIRTY_MINUTES_IN_MILLISECONDS, TimeConstants.THIRTY_MINUTES_IN_MILLISECONDS);
        subscriptionClient.subscribeUserDataEvent(listenKey, (accountBalance::updateBalance),null);

        subscriptionClient.subscribeCandlestickEvent("btcusdt", Config.INTERVAL, ((event) -> {
            realTimeData.updateData(event);
            lock.readLock().lock();
            for (PositionHandler positionHandler :positionHandlers){
                    positionHandler.update(Config.INTERVAL);
                    if (positionHandler.isSoldOut()) positionHandlers.remove(positionHandler);
                    else{
                        positionHandler.run(realTimeData);
                    }
            }
            lock.readLock().unlock();
            for (EntryStrategy entryStrategy: entryStrategies){
                    PositionHandler positionHandler = entryStrategy.run(realTimeData, "btcusdt");
                    if (positionHandler != null){
                        lock.writeLock().lock();
                        positionHandlers.add(positionHandler);
                        lock.writeLock().unlock();
                    }
            }
        }), System.out::print);
        //subscriptionClient.unsubscribeAll();
        //TODO: code termination;
    }
}


