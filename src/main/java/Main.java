import Data.*;
import SingletonHelpers.BinanceInfo;
import CodeExecution.RealTimeCommandOperator;
import SingletonHelpers.RequestClient;
import Utils.Utils;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        AccountBalance accountBalance = AccountBalance.getAccountBalance(); //!Don't touch
        BinanceInfo binanceInfo = BinanceInfo.getBinanceInfo(); //!Don't touch
        RealTimeCommandOperator realTimeCommandOperator = new RealTimeCommandOperator();
        try {
            realTimeCommandOperator.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


