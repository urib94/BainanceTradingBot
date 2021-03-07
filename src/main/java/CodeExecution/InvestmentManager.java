package CodeExecution;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import Data.*;
import Positions.PositionHandler;
import Strategies.EntryStrategy;
import com.binance.client.api.SubscriptionClient;
import com.binance.client.api.model.enums.*;
import SingletonHelpers.ExecService;
import SingletonHelpers.SubClient;

public class InvestmentManager implements Runnable{
    private final CandlestickInterval interval;
    private final String symbol;
    ConcurrentLinkedDeque<EntryStrategy> entryStrategies;
    ConcurrentLinkedDeque<PositionHandler> positionHandlers;
    ConcurrentLinkedDeque<Future<?>> futures;



    public InvestmentManager(CandlestickInterval interval, String symbol, EntryStrategy entryStrategy) {
        this.interval = interval;
        this.symbol = symbol;
        entryStrategies = new ConcurrentLinkedDeque<>();
        positionHandlers = new ConcurrentLinkedDeque<>();
        futures = new ConcurrentLinkedDeque<>();
        entryStrategies.add(entryStrategy);
    }

    public void run(){
        RealTimeData realTimeData = new RealTimeData(symbol, interval);
        SubscriptionClient subscriptionClient = SubClient.getSubClient().getSubscriptionClient();
        ExecutorService executorService = ExecService.getExecService().getExecutorService();
        PositionHandler oldPosition = AccountBalance.getAccountBalance().manageOldPositions(symbol);
        if (oldPosition != null){
            positionHandlers.add(oldPosition);
        }
        subscriptionClient.subscribeCandlestickEvent(symbol, interval, ((event) -> {
            executorService.execute(()->{
                realTimeData.updateData(event);
                AccountBalance.getAccountBalance().updateBalance();
                for (PositionHandler positionHandler :positionHandlers){
                    positionHandler.update(realTimeData, interval);
                    if (positionHandler.isSoldOut()){
                        positionHandler.terminate();
                        positionHandlers.remove(positionHandler);
                    }
                    else{
                        positionHandler.run(realTimeData);
                    }
                }
                for (EntryStrategy entryStrategy: entryStrategies){
                    PositionHandler positionHandler = entryStrategy.run(realTimeData, symbol);
                    if (positionHandler != null){
                        positionHandlers.add(positionHandler);
                    }
                }
            });
        }), System.out::println);
    }

    private void waitUntilFinished(ConcurrentLinkedDeque<Future<?>> futures){
        for (Future<?> future: futures){
            try{
                future.get();
            }catch (Exception ignored){}
            futures.remove(future);
        }
    }

    public void addEntryStrategy(EntryStrategy entryStrategy){
        entryStrategies.add(entryStrategy);
    }

    public void removeEntryStrategy(EntryStrategy entryStrategy) {
        if (entryStrategies.contains(entryStrategy)){
            entryStrategies.remove(entryStrategy);
        }
    }
}
