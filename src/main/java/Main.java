import Data.AccountBalance;
import Data.PrivateConfig;
import Data.RealTimeData;
import com.binance.client.RequestOptions;
import com.binance.client.SubscriptionClient;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


public class Main {
    public static void main(String[] args){
        LocalDateTime programStartTime = LocalDateTime.now();
        AtomicReference<LocalDateTime> baseTime = new AtomicReference<>(LocalDateTime.now()); // to check if 30 minutes have passed since the last keep alive request.
        ExecutorService executorService = Executors.newFixedThreadPool(PrivateConfig.THREAD_NUM);
        AccountBalance accountBalance = AccountBalance.getAccountBalance();
        RealTimeData rtd = new RealTimeData("btcusdt", CandlestickInterval.ONE_MINUTE, PrivateConfig.THREAD_NUM);
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, options);
        SubscriptionClient subscriptionClient = SubscriptionClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
        String listenKey = syncRequestClient.startUserDataStream();
        syncRequestClient.keepUserDataStream(listenKey);
        subscriptionClient.subscribeUserDataEvent(listenKey, ((event)-> {
            LocalDateTime programTimeNow = LocalDateTime.now();
            if (utils.minutePassed(baseTime.get(),programTimeNow,PrivateConfig.MINUTES_TO_KEEP_ALIVE)) {
                syncRequestClient.keepUserDataStream(listenKey);
                System.out.println("Sent keep User Alive request");
                baseTime.set(programTimeNow);
            }
            System.out.println(event);
            accountBalance.updateBalance(event, "usdt");
        }),System.out::println);
        subscriptionClient.subscribeCandlestickEvent("btcusdt", CandlestickInterval.ONE_MINUTE, ((event) -> {
            rtd.updateData(event);
           executorService.execute(()-> {
                System.out.println("omri the king");
            });
        }), System.out::println);
       //subscriptionClient.unsubscribeAll();
    }
}


