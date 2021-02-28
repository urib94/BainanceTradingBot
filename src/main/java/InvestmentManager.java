import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Data.*;
import Positions.PositionHandler;
import Strategies.EntryStrategy;
import Strategies.RSIStrategies.RSIEntryStrategy;
import Utils.RealTimeCommandOperator;
import com.binance.client.api.SubscriptionClient;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.CandlestickInterval;

public class InvestmentManager implements Runnable{
    private double takeProfitPercentage;
    private final double stopLossPercentage;
    private final int rsiCandleNum;
    private final CandlestickInterval interval;
    private final int leverage;
    private final String symbol;
    private final BigDecimal requestedBuyingAmount;


    public InvestmentManager(double takeProfitPercentage, double stopLossPercentage, int rsiCandleNum, CandlestickInterval interval, int leverage, String symbol, BigDecimal requestedBuyingAmount) {
        this.takeProfitPercentage = takeProfitPercentage;
        this.stopLossPercentage = stopLossPercentage;
        this.rsiCandleNum = rsiCandleNum;
        this.leverage = leverage;
        this.interval = interval;
        this.symbol = symbol;
        this.requestedBuyingAmount = requestedBuyingAmount;
    }

    public void run(ArrayList<EntryStrategy> entryStrategies, ArrayList<PositionHandler> positionHandlers){
        RealTimeData realTimeData = new RealTimeData(symbol, interval, Config.CANDLE_NUM);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        com.binance.client.api.SubscriptionClient subscriptionClient = SubscriptionClient.getSubscriptionClient().getSubscriptionClient();
        ArrayList<Future<?>> futures = new ArrayList<>();
        ReadWriteLock positionHandlersLock = new ReentrantReadWriteLock();
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

    private void waitUntilFinished(ArrayList<Future<?>> futures){
        for (Future<?> future: futures){
            try{
                future.get();
            }catch (Exception ignored){}
            futures.remove(future);
        }
    }
}
