import Data.AccountBalance;
import Data.Config;
import Data.RealTimeData;
import Data.RequestClient;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.RSIStrategies.RSIEntryStrategy;
import com.binance.client.api.RequestOptions;
import com.binance.client.api.SubscriptionClient;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main {
    public static void main(String[] args){
        LocalDateTime programStartTime = LocalDateTime.now();
        ExecutorService executorService = Executors.newFixedThreadPool(Config.THREAD_NUM);
        //AccountBalance accountBalance = AccountBalance.getAccountBalance();
        RealTimeData realTimeData = new RealTimeData("btcusdt", CandlestickInterval.ONE_MINUTE, Config.CANDLE_NUM);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        SubscriptionClient subscriptionClient = SubscriptionClient.create(Config.API_KEY, Config.SECRET_KEY);
        String listenKey = syncRequestClient.startUserDataStream();
        ArrayList<EntryStrategy> entryStrategies = new ArrayList<>();
        ArrayList<PositionHandler> positionEntries = new ArrayList<>();
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
        timer.schedule(timerTask, Config.THIRTY_MINUTES_IN_MILLISECONDS, Config.THIRTY_MINUTES_IN_MILLISECONDS);
        subscriptionClient.subscribeUserDataEvent(listenKey, ((event)-> {
            //accountBalance.updateBalance(event);
        }),System.out::println);

        subscriptionClient.subscribeCandlestickEvent("btcusdt", Config.INTERVAL, ((event) -> {
            realTimeData.updateData(event);
            lock.readLock().lock();
            for (PositionHandler positionHandler :positionEntries){
                executorService.execute(()->{
                    positionHandler.update(Config.INTERVAL);
                    if (positionHandler.isSoldOut()) positionEntries.remove(positionHandler);
                    else{
                        positionHandler.run(realTimeData);
                    }
                });
            }
            lock.readLock().unlock();
            for (EntryStrategy entryStrategy: entryStrategies){
                executorService.execute(()->{
                    PositionHandler positionHandler = entryStrategy.run(realTimeData, "btcusdt");
                    if (positionHandler != null){
                        lock.writeLock().lock();
                        positionEntries.add(positionHandler);
                        lock.writeLock().unlock();
                    }
                });
            }
        }), System.out::print);
        //subscriptionClient.unsubscribeAll();
        //TODO: code termination;
    }
}


