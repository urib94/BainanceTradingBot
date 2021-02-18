import com.binance.client.SubscriptionClient;
import com.binance.client.model.enums.CandlestickInterval;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        RealTimeData rlt = new RealTimeData("btcusdt", CandlestickInterval.ONE_MINUTE, 40);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        SubscriptionClient client = SubscriptionClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
        client.subscribeCandlestickEvent("btcusdt", CandlestickInterval.ONE_MINUTE, ((event) -> {
            rlt.updateData(event);

            executorService.execute(()->{



            });
        }), null);
        client.unsubscribeAll();
    }
}

//Omri we need to figure out how to cause the "while" to iterate only after there is a change in the data, i didn't figured it out yet.
//In the current code the data is being updated continuously and it is concurrent.
