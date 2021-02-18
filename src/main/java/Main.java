import com.binance.client.RequestOptions;
import com.binance.client.SubscriptionClient;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args){
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        AccountBalance accountBalance = AccountBalance.getAccountBalance();
        RealTimeData rtd = new RealTimeData("btcusdt", CandlestickInterval.ONE_MINUTE, 40);
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, options);
        SubscriptionClient subscriptionClient = SubscriptionClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
        String listenKey = syncRequestClient.startUserDataStream();
        syncRequestClient.keepUserDataStream(listenKey);
        subscriptionClient.subscribeUserDataEvent(listenKey, (event)-> accountBalance.updateBalance(event, "usdt"),System.out::println);
        subscriptionClient.subscribeCandlestickEvent("btcusdt", CandlestickInterval.ONE_MINUTE, ((event) -> {
            rtd.updateData(event);
            executorService.execute(()-> {
                System.out.println("omri the king");
            });
        }), System.out::println);
        //subscriptionClient.unsubscribeAll();
    }
}


