import data.*;
import singletonHelpers.BinanceInfo;
import codeExecution.RealTimeCommandOperator;
import singletonHelpers.TelegramMessenger;

import java.time.ZonedDateTime;

public class Main {
    public static void main(String[] args) {
        AccountBalance accountBalance = AccountBalance.getAccountBalance(); //!Don't touch
        BinanceInfo binanceInfo = BinanceInfo.getBinanceInfo(); //!Don't touch
        RealTimeCommandOperator realTimeCommandOperator = new RealTimeCommandOperator();
        TelegramMessenger.sendToTelegram("Start running: " + ZonedDateTime.now());
        try {
            realTimeCommandOperator.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


