import Data.AccountBalance;
import Data.PrivateConfig;
import Data.RealTimeData;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.RSIStrategies.RSIEntryStrategy;
import com.binance.client.RequestOptions;
import com.binance.client.SubscriptionClient;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main {
    public static void main(String[] args){
        LocalDateTime programStartTime = LocalDateTime.now();
        ExecutorService executorService = Executors.newFixedThreadPool(PrivateConfig.THREAD_NUM);
        AccountBalance accountBalance = AccountBalance.getAccountBalance();
        RealTimeData realTimeData = new RealTimeData("btcusdt", CandlestickInterval.ONE_MINUTE, PrivateConfig.CANDLE_NUM);
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, options);
        SubscriptionClient subscriptionClient = SubscriptionClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
        String listenKey = syncRequestClient.startUserDataStream();
        syncRequestClient.keepUserDataStream(listenKey);
        ArrayList<EntryStrategy> entryStrategies = new ArrayList<>();
        ReadWriteLock lock = new ReentrantReadWriteLock();
        ArrayList<PositionHandler> positionEntries = new ArrayList<>();
        entryStrategies.add(new RSIEntryStrategy());
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                syncRequestClient.keepUserDataStream(listenKey);
                System.out.println("Sent keep user Alive request");
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask,PrivateConfig.THIRTY_MINUTES_IN_MILLISECONDS,PrivateConfig.THIRTY_MINUTES_IN_MILLISECONDS);
        subscriptionClient.subscribeUserDataEvent(listenKey, ((event)-> {
            System.out.println(event);
            accountBalance.updateBalance(event, "usdt");
        }),System.out::println);

        subscriptionClient.subscribeCandlestickEvent("btcusdt", CandlestickInterval.ONE_MINUTE, ((event) -> {
            realTimeData.updateData(event);
            lock.readLock().lock();
            for (PositionHandler positionHandler :positionEntries){
                positionHandler.update();
                if (positionHandler.isSoldOut()) positionEntries.remove(positionHandler);
                else{
                    executorService.execute(()->{
                        positionHandler.update();
                        positionHandler.run(realTimeData);
                    });
                }
            }
            lock.readLock().unlock();
            for (EntryStrategy entryStrategy: entryStrategies){
                executorService.execute(()->{
                    PositionHandler positionHandler = entryStrategy.run(realTimeData);
                    if (positionHandler != null){
                        lock.writeLock().lock();
                        positionEntries.add(positionHandler);
                        lock.writeLock().unlock();
                    }
                });
            }
        }), System.out::print);
        //subscriptionClient.unsubscribeAll();
    }
}


