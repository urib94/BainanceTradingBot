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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main {
    public static void main(String[] args){
        LocalDateTime programStartTime = LocalDateTime.now();
        AtomicReference<LocalDateTime> baseTime = new AtomicReference<>(LocalDateTime.now()); // to check if 30 minutes have passed since the last keep alive request.
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
        subscriptionClient.subscribeUserDataEvent(listenKey, ((event)-> {
            LocalDateTime programTimeNow = LocalDateTime.now();
            LocalDateTime baseLocalTime = baseTime.get();
            if (utils.minutePassed(baseLocalTime,programTimeNow,PrivateConfig.MINUTES_TO_KEEP_ALIVE)) {
                syncRequestClient.keepUserDataStream(listenKey); // keep user stream alive (it dies after 60 minutes).
                System.out.println("Sent keep User Alive request");
                baseTime.set(programTimeNow);
            }
            System.out.println(event);
            accountBalance.updateBalance(event, "usdt");
        }),System.out::println);

        subscriptionClient.subscribeCandlestickEvent("btcusdt", CandlestickInterval.ONE_MINUTE, ((event) -> {
            realTimeData.updateData(event);
            lock.readLock().lock();
            for (PositionHandler positionHandler :positionEntries){
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


