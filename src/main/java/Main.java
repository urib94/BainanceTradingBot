import Data.*;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.RSIStrategies.RSIEntryStrategy;
import Utils.TimeConstants;
import com.binance.client.api.SubscriptionClient;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main {
    public static void main(String[] args){
        LocalDateTime programStartTime = LocalDateTime.now();
        AccountBalance accountBalance = AccountBalance.getAccountBalance();
        BinanceInfo binanceInfo = BinanceInfo.getBinanceInfo();
        RealTimeData realTimeData = new RealTimeData(Config.SYMBOL, CandlestickInterval.ONE_MINUTE, Config.CANDLE_NUM);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);
        SubscriptionClient subscriptionClient = SubscriptionClient.create(Config.API_KEY, Config.SECRET_KEY);
        String listenKey = syncRequestClient.startUserDataStream();
        ArrayList<EntryStrategy> entryStrategies = new ArrayList<>();
        ArrayList<PositionHandler> positionHandlers = new ArrayList<>();
        ArrayList<Future<?>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(Config.THREAD_NUM);
        ReadWriteLock positionHandlersLock = new ReentrantReadWriteLock();
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
        subscriptionClient.subscribeUserDataEvent(listenKey, (accountBalance::updateBalance), null);
        subscriptionClient.subscribeCandlestickEvent(Config.SYMBOL, Config.INTERVAL, ((event) -> {
            waitUntilFinished(futures);
            realTimeData.updateData(event);
            AccountBalance.getAccountBalance().aggresiveUpdateBalance();
            positionHandlersLock.readLock().lock();
            for (PositionHandler positionHandler :positionHandlers){
                positionHandler.update(Config.INTERVAL);
                if (positionHandler.isSoldOut()){
                    positionHandler.terminate();
                    positionHandlers.remove(positionHandler);
                }
                else{
                    Future<?> future = executorService.submit(()->{
                        positionHandler.run(realTimeData);
                    });
                    futures.add(future);
                }
            }
            positionHandlersLock.readLock().unlock();
            for (EntryStrategy entryStrategy: entryStrategies){
                Future<?> future = executorService.submit(()-> {
                    PositionHandler positionHandler = entryStrategy.run(realTimeData, Config.SYMBOL);
                    if (positionHandler != null){
                        positionHandlersLock.writeLock().lock();
                        positionHandlers.add(positionHandler);
                        positionHandlersLock.writeLock().unlock();
                    }
                });
                futures.add(future);
            }
        }), null);
        //subscriptionClient.unsubscribeAll();
        //TODO: code termination;
    }

    private static void waitUntilFinished(ArrayList<Future<?>> futures){
        for (Future<?> future: futures){
            try{
                future.get();
            }catch (Exception ignored){}
            futures.remove(future);
        }
    }
}


