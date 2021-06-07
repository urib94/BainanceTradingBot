package main.java.strategies.nutral;

import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.OrderStatus;
import com.binance.client.model.trade.Order;
import data.AccountBalance;
import data.DataHolder;
import singletonHelpers.RequestClient;

public class NutralExit {
    AccountBalance accountBalance;
    Order exitShort, exitLong;
    Order[] exit = {exitLong, exitShort};
    public boolean exitIsFilld = false;
    SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();

    public NutralExit(Order[] exitOrders) {
        exit[0] = exitOrders[0];
        exit[1] = exitOrders[1];
    }

    public synchronized boolean run(DataHolder realTimeData, String symbol) {
        return exitIsFilld();
    }

    public boolean exitIsFilld(){
        updateOrders(syncRequestClient);
        for(Order order: exit){
            boolean filled= order.getStatus().equals(OrderStatus.FILLED);
            if(filled){
                System.out.println("finished current exit butch");
            }
            return filled;
        }
        return false;
    }

    private void updateOrders(SyncRequestClient syncRequestClient){
        for (Order order :exit){
            order = syncRequestClient.getOrder(order.getSymbol(), order.getOrderId(),order.getClientOrderId() );
        }
    }
}
