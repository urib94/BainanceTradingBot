package codeExecution;

import java.util.concurrent.*;

import data.*;
import positions.PositionHandler;
import strategies.EntryStrategy;
import com.binance.client.api.SubscriptionClient;
import com.binance.client.api.model.enums.*;
import singletonHelpers.ExecService;
import singletonHelpers.SubClient;

public class InvestmentManager implements Runnable{
    private final CandlestickInterval interval;
    private final String symbol;
    private final Object lock = new Object();
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
        ExecutorService iterationExecutorService = ExecService.getExecService().getExecutorService();
//        PositionHandler oldPosition = AccountBalance.getAccountBalance().manageOldPositions(symbol);
//        if (oldPosition != null){
//            positionHandlers.add(oldPosition);
//        }
        subscriptionClient.subscribeCandlestickEvent(symbol, interval, ((event) -> {
            iterationExecutorService.execute(()->{
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
