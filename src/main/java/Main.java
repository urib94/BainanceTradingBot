import Data.*;
import Strategies.EntryStrategy;
import Positions.PositionHandler;
import Strategies.RSIStrategies.RSIEntryStrategy;
import Utils.RealTimeCommandOperator;
import com.binance.client.api.SubscriptionClient;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;

import java.util.ArrayList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main {
    public static void main(String[] args){ //TODO: allow to orders to run parallel
        AccountBalance accountBalance = AccountBalance.getAccountBalance(); //!Don't touch
        BinanceInfo binanceInfo = BinanceInfo.getBinanceInfo(); //!Don't touch
        RealTimeData realTimeData = new RealTimeData(Config.SYMBOL, CandlestickInterval.ONE_MINUTE, Config.CANDLE_NUM);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);
        SubscriptionClient subscriptionClient = SubscriptionClient.create(Config.API_KEY, Config.SECRET_KEY);
        ArrayList<EntryStrategy> entryStrategies = new ArrayList<>();//TODO: change to map of symbol and strategies so each strategy will have its own subscribe candlestick event
        ArrayList<PositionHandler> positionHandlers = new ArrayList<>();
        ArrayList<Future<?>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(Config.THREAD_NUM);
        Thread realTimeCommandOperatorThread = new Thread(new RealTimeCommandOperator());
        realTimeCommandOperatorThread.start();
        ReadWriteLock positionHandlersLock = new ReentrantReadWriteLock();
        entryStrategies.add(new RSIEntryStrategy());
        PositionHandler oldPosition = accountBalance.manageOldPositions();
        if (oldPosition != null){
            positionHandlers.add(oldPosition);
        }
        subscriptionClient.subscribeCandlestickEvent(Config.SYMBOL, Config.INTERVAL, ((event) -> {
            waitUntilFinished(futures);
            realTimeData.updateData(event, executorService);
            AccountBalance.getAccountBalance().aggressiveUpdateBalance();
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


