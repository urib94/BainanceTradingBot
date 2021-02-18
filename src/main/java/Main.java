import com.binance.client.RequestOptions;
import com.binance.client.SubscriptionClient;
import com.binance.client.model.enums.CandlestickInterval;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args){
        RealTimeData rtd = new RealTimeData("btcusdt", CandlestickInterval.ONE_MINUTE, 40);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        SubscriptionClient client = SubscriptionClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
        client.subscribeCandlestickEvent("btcusdt", CandlestickInterval.ONE_MINUTE, ((event) -> {
            rtd.updateData(event);
            executorService.execute(()-> {
                System.out.println("omri the king");
            });

        }), null);
        //client.unsubscribeAll();
    }
}


