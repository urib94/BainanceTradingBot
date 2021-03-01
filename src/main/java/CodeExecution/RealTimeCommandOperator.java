package CodeExecution;

import Data.AccountBalance;
import Data.Config;
import Data.RequestClient;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;
import com.binance.client.api.model.trade.Position;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class RealTimeCommandOperator implements Runnable {
    private final HashMap<String, RealTimeOperation> commandsAndOps;
    private final HashMap<Pair<String,CandlestickInterval>, InvestmentManager> investmentManagerHashMap;
    private final ReadWriteLock investmentManagerHashMapLock = new ReentrantReadWriteLock();


    public RealTimeCommandOperator() {
        investmentManagerHashMap = new HashMap<>();
        commandsAndOps = new HashMap<>();

        commandsAndOps.put(RealTImeOperations.CANCEL_ALL_ORDERS,(message)->{
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.cancelAllOpenOrder(message.getSymbol());
        });

        commandsAndOps.put(RealTImeOperations.CLOSE_ALL_POSITIONS,(message)->{
            List<Position> openPositions = AccountBalance.getAccountBalance().getOpenPositions();
            for (Position openPosition: openPositions){
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                if (openPosition.getPositionSide().equals("LONG")){
                    Order sellingOrder = syncRequestClient.postOrder(message.getSymbol(), OrderSide.SELL, null, OrderType.MARKET, TimeInForce.GTC,
                            openPosition.getPositionAmt().toString(), null, Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                }
                else{
                    Order sellingOrder = syncRequestClient.postOrder(message.getSymbol(), OrderSide.BUY, null, OrderType.MARKET, TimeInForce.GTC,
                            openPosition.getPositionAmt().toString(), null, Config.REDUCE_ONLY, null, null, null, null, NewOrderRespType.RESULT);
                }
            }
        });

        commandsAndOps.put(RealTImeOperations.ACTIVATE_STRATEGY,(message)->{
            Pair<String, CandlestickInterval> pair = new MutablePair<String, CandlestickInterval>(message.getSymbol(), message.getInterval());
            investmentManagerHashMapLock.readLock().lock();
            if (investmentManagerHashMap.containsKey(pair)){
                investmentManagerHashMap.get(pair).addEntryStrategy(message.getEntryStrategy());
                investmentManagerHashMapLock.readLock().unlock();
            }
            else{
                investmentManagerHashMapLock.readLock().unlock();
                investmentManagerHashMapLock.writeLock().lock();
                InvestmentManager investmentManager = new InvestmentManager(message.getInterval(), message.getSymbol(), message.getEntryStrategy());
                investmentManagerHashMap.put(pair, investmentManager);
                investmentManagerHashMapLock.writeLock().unlock();
                investmentManager.run();
            }
        });

        commandsAndOps.put(RealTImeOperations.ACTIVATE_STRATEGY_D,(message)->{
            Pair<String, CandlestickInterval> pair = new MutablePair<String, CandlestickInterval>(message.getSymbol(), message.getInterval());
            investmentManagerHashMapLock.readLock().lock();
            if (investmentManagerHashMap.containsKey(pair)){
                investmentManagerHashMap.get(pair).addEntryStrategy(message.getEntryStrategy());
                investmentManagerHashMapLock.readLock().unlock();
            }
            else{
                investmentManagerHashMapLock.readLock().unlock();
                investmentManagerHashMapLock.writeLock().lock();
                InvestmentManager investmentManager = new InvestmentManager(message.getInterval(), message.getSymbol(), message.getEntryStrategy());
                investmentManagerHashMap.put(pair, investmentManager);
                investmentManagerHashMapLock.writeLock().unlock();
                investmentManager.run();
            }
        });

        commandsAndOps.put(RealTImeOperations.DEACTIVATE_STRATEGY,(message)->{
            Pair<String, CandlestickInterval> pair = new MutablePair<String, CandlestickInterval>(message.getSymbol(), message.getInterval());
            investmentManagerHashMapLock.readLock().lock();
            if (investmentManagerHashMap.containsKey(pair)){
                investmentManagerHashMap.get(pair).removeEntryStrategy(message.getEntryStrategy());
                investmentManagerHashMapLock.readLock().unlock();
            }
        });

        commandsAndOps.put(RealTImeOperations.GET_LAST_TRADES,(message)->{//TODO: complete
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.getAccountTrades(message.getSymbol(), message.getStartTime(), message.getEndTime(), message.getTradesLimit());


        });

        commandsAndOps.put(RealTImeOperations.GET_OPEN_POSITIONS,(message)->{
            List<Position> openPositions = AccountBalance.getAccountBalance().getOpenPositions();
            int index = 1;
            for (Position openPosition: openPositions){
                System.out.println("Open position "+ index + ": " + openPosition);
                index++;
            }
        });

        commandsAndOps.put(RealTImeOperations.GET_OPEN_ORDERS,(message)->{
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            List<Order> openOrders = syncRequestClient.getOpenOrders(message.getSymbol());
            int index = 1;
            for (Order openOrder: openOrders){
                System.out.println("Open position "+ index + ": " + openOrder);
                index++;
            }
        });

        commandsAndOps.put(RealTImeOperations.GET_CURRENT_BALANCE,(message)->{
            System.out.println("Your current balance is: " + AccountBalance.getAccountBalance().getCoinBalance(message.getSymbol()));
        });

        commandsAndOps.put(RealTImeOperations.LOGIN,(message)->{
            Config.setApiKey(message.getApiKey());
            Config.setSecretKey(message.getSecretKey());
        });
    }

    @Override
    public void run() {
        while(true){
            InputMessage message = new InputMessage();
            try {
                String input = readFromKeyboard();
                message.initialize(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (commandsAndOps.containsKey(message.getOperation())){
                commandsAndOps.get(message.getOperation()).run(message);
            }
        }
    }

    private String readFromKeyboard() throws IOException {
        InputStreamReader r = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(r);
        return br.readLine();
    }
}
