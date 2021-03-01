package CodeExecution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Data.*;
import Positions.PositionHandler;
import Strategies.EntryStrategy;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;

public class InvestmentManager implements Runnable{
    private final CandlestickInterval interval;
    private final String symbol;
    ArrayList<EntryStrategy> entryStrategies;
    ReadWriteLock entryStrategiesLock = new ReentrantReadWriteLock();


    public InvestmentManager(CandlestickInterval interval, String symbol, EntryStrategy entryStrategy) {
        this.interval = interval;
        this.symbol = symbol;
        entryStrategies = new ArrayList<>();
        entryStrategies.add(entryStrategy);
    }

    public void run(){
        RealTimeData realTimeData = new RealTimeData(symbol, interval);
        com.binance.client.api.SubscriptionClient subscriptionClient = SubClient.getSubClient().getSubscriptionClient();
        ExecutorService executorService = ExecService.getExecService().getExecutorService();
        ArrayList<PositionHandler> positionHandlers = new ArrayList<>();
        ArrayList<Future<?>> futures = new ArrayList<>();
        ReadWriteLock positionHandlersLock = new ReentrantReadWriteLock();
        PositionHandler oldPosition = AccountBalance.getAccountBalance().manageOldPositions(symbol);
        if (oldPosition != null){
            positionHandlers.add(oldPosition);
        }
        subscriptionClient.subscribeCandlestickEvent(symbol, interval, ((event) -> {
            waitUntilFinished(futures);
            realTimeData.updateData(event);
            AccountBalance.getAccountBalance().updateBalance();
            positionHandlersLock.readLock().lock();
            for (PositionHandler positionHandler :positionHandlers){
                positionHandler.update(interval);
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
            entryStrategiesLock.readLock().lock();
            for (EntryStrategy entryStrategy: entryStrategies){
                Future<?> future = executorService.submit(()-> {
                    PositionHandler positionHandler = entryStrategy.run(realTimeData, symbol);
                    if (positionHandler != null){
                        positionHandlersLock.writeLock().lock();
                        positionHandlers.add(positionHandler);
                        positionHandlersLock.writeLock().unlock();
                    }
                });
                futures.add(future);
            }
            entryStrategiesLock.readLock().unlock();
        }), null);
    }

    private void waitUntilFinished(ArrayList<Future<?>> futures){
        for (Future<?> future: futures){
            try{
                future.get(2, TimeUnit.SECONDS);//TODO: check if this change is slowing the machine
            }catch (Exception ignored){}
            futures.remove(future);
        }
    }

    public void addEntryStrategy(EntryStrategy entryStrategy){
        entryStrategiesLock.writeLock().lock();
        entryStrategies.add(entryStrategy);
        entryStrategiesLock.writeLock().unlock();
    }

    public void removeEntryStrategy(EntryStrategy entryStrategy) {
        if (entryStrategies.contains(entryStrategy)){
            entryStrategiesLock.writeLock().lock();
            entryStrategies.remove(entryStrategy);
            entryStrategiesLock.writeLock().unlock();
        }
    }
}
