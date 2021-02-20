import Data.AccountBalance;
import Strategies.PositionEntry;
import Data.PrivateConfig;
import Data.RealTimeData;
import Strategies.EntryStrategy;
import Strategies.RSIStrategies.RSIEntryStrategy;
import com.binance.client.RequestOptions;
import com.binance.client.SubscriptionClient;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


public class Main {
    public static void main(String[] args){
        LocalDateTime programStartTime = LocalDateTime.now();
        AtomicReference<LocalDateTime> baseTime = new AtomicReference<>(LocalDateTime.now()); // to check if 30 minutes have passed since the last keep alive request.
        ExecutorService executorService = Executors.newFixedThreadPool(PrivateConfig.THREAD_NUM);
        AccountBalance accountBalance = AccountBalance.getAccountBalance();
        RealTimeData realTimeData = new RealTimeData("btcusdt", CandlestickInterval.ONE_MINUTE, PrivateConfig.THREAD_NUM);
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, options);
        SubscriptionClient subscriptionClient = SubscriptionClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
        String listenKey = syncRequestClient.startUserDataStream();
        syncRequestClient.keepUserDataStream(listenKey);
        ArrayList<EntryStrategy> entryStrategies = new ArrayList<>();
        ArrayList<PositionEntry> positionEntries = new ArrayList<>();
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
            for (EntryStrategy entryStrategy: entryStrategies){
                executorService.execute(()->{
                    PositionEntry positionEntry = entryStrategy.run(realTimeData);
                    if (positionEntry != null) positionEntries.add(positionEntry);
                });
            }
            for (PositionEntry positionEntry:positionEntries){
                if (positionEntry.getBalance().compareTo(new BigDecimal(0)) <= 0) positionEntries.remove(positionEntry);
                else{
                    executorService.execute(()->{
                        positionEntry.update();
                        positionEntry.run(realTimeData);
                    });
                }
            }
        }), System.out::print);
        //subscriptionClient.unsubscribeAll();
    }
}


