package Utils;

import Data.Config;
import Data.RequestClient;
import com.binance.client.api.SyncRequestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class RealTimeCommandOperator implements Runnable {
    private final HashMap<String,RealTimeOperation> commandsAndOps;

    public RealTimeCommandOperator() {
        commandsAndOps = new HashMap<>();
        commandsAndOps.put("cancel all orders",()->{
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            syncRequestClient.cancelAllOpenOrder(Config.SYMBOL);});
        /*TODO:
        -get open positions
        -get open orders
        -buy immediately
        -sell immediately
        -get current balance
        -switch from long to short in delta rsi strategy
        -Login
         */
    }

    @Override
    public void run() {
        while(true){
            String input = "";
            try {
                input = readFromKeyboard();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(input);
            if (commandsAndOps.containsKey(input)){
                commandsAndOps.get(input).run();
            }
        }
    }

    private String readFromKeyboard() throws IOException {
        InputStreamReader r=new InputStreamReader(System.in);
        BufferedReader br=new BufferedReader(r);
        return br.readLine();
    }
}
