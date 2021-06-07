package main.java;

import codeExecution.RealTimeCommandOperator;
import data.AccountBalance;
import singletonHelpers.BinanceInfo;
import singletonHelpers.TelegramMessenger;

import java.util.Date;

public class Main {
    public static void main(String[] args) {
        AccountBalance accountBalance = AccountBalance.getAccountBalance(); //!Don't touch
        BinanceInfo binanceInfo = BinanceInfo.getBinanceInfo(); //!Don't touch
        RealTimeCommandOperator realTimeCommandOperator = new RealTimeCommandOperator();
        TelegramMessenger.sendToTelegram("Start running with " + new Date(System.currentTimeMillis()));
//        CSVDataWriter.writeDataAtOnce();
        try {
            realTimeCommandOperator.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


