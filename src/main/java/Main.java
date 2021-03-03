import Data.*;
import SingletonHelpers.BinanceInfo;
import CodeExecution.RealTimeCommandOperator;
import Utils.Utils;

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


