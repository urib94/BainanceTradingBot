package CodeExecution;

import Strategies.EntryStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIEntryStrategy;
import Strategies.RSIStrategies.RSIConstants;
import Strategies.RSIStrategies.RSIEntryStrategy;
import Utils.TimeConstants;
import Utils.Utils;
import com.binance.client.api.model.enums.CandlestickInterval;

import java.math.BigDecimal;

public class InputMessage {
    public String operation = "";
    private String symbol;
    private CandlestickInterval interval;
    private EntryStrategy entryStrategy;
    private String apiKey;
    private String secretKey;
    private Long startTime;
    private Long endTime;
    private Long clientId;

    public Long getClientId() {
        return clientId;
    }

    private Integer tradesLimit;






    public String getSymbol() {
        return symbol;
    }

    public void initialize(String input) {//TODO: complete
        String [] messageParts = input.split(", ");
        operation = messageParts[0];
        switch (operation) {
            case RealTImeOperations.CANCEL_ALL_ORDERS:
                symbol = messageParts[1];
                break;

            case RealTImeOperations.CLOSE_ALL_POSITIONS:
                break;

            case RealTImeOperations.ACTIVATE_STRATEGY:
            symbol = messageParts[1];
            for (CandlestickInterval candlestickInterval: CandlestickInterval.values()){
                if (candlestickInterval.toString().equals(messageParts[2])) interval = candlestickInterval;
            }
            entryStrategy = stringToEntryStrategy(messageParts[3]);
            if (entryStrategy != null){
                entryStrategy.setTakeProfitPercentage(Double.parseDouble(messageParts[4]));
                entryStrategy.setStopLossPercentage(Double.parseDouble(messageParts[5]));
                entryStrategy.setLeverage(Integer.parseInt(messageParts[6]));
                entryStrategy.setRequestedBuyingAmount(BigDecimal.valueOf(Double.parseDouble(messageParts[7])));
            }
                break;

            case RealTImeOperations.ACTIVATE_STRATEGY_D:
            symbol = messageParts[1];
            for (CandlestickInterval candlestickInterval: CandlestickInterval.values()){
                if (candlestickInterval.toString().equals(messageParts[2])) interval = candlestickInterval;
            }
            entryStrategy = stringToEntryStrategy(messageParts[3]);
                break;

            case RealTImeOperations.DEACTIVATE_STRATEGY:
                symbol = messageParts[1];
                for (CandlestickInterval candlestickInterval: CandlestickInterval.values()){
                    if (candlestickInterval.toString().equals(messageParts[2])) interval = candlestickInterval;
                }
                break;

            case RealTImeOperations.GET_LAST_TRADES:
                symbol = messageParts[1];
                break;

            case RealTImeOperations.GET_OPEN_POSITIONS:
                break;

            case RealTImeOperations.GET_OPEN_ORDERS:
                symbol = messageParts[1];
                break;

            case RealTImeOperations.GET_CURRENT_BALANCE:
                symbol = messageParts[1];
                break;

            case RealTImeOperations.LOGIN:
                apiKey = messageParts[1];
                secretKey = messageParts[2];
                break;

            case "help":
                System.out.println("Optional commands:\n" +
                        "cancel all orders, [symbol]\n" +
                        "close all positions\n" +
                        "activate strategy, [symbol], [interval], [entrystrategy], [takeprofit], [stoploss], [leverage], [request buying ammount]\n" +
                        "activate strategy default, [symbol], [interval], [entrystrategy]\n" +
                        "deactivate strategy, [symbol], [interval]\n" +
                        "get last trades, [symbol]\n" +
                        "get open positions\n" +
                        "get open orders, [symbol]\n" +
                        "get current balance, [symbol]\n" +
                        "login, [apikey], [secretkey]\n"
                );
                break;

            default:
                System.out.println("Wrong message");
        }
    }

    private EntryStrategy stringToEntryStrategy(String strategyName) {
        switch (strategyName) {
            case "rsi":
                return new RSIEntryStrategy();

            case "macd over rsi":
                return new MACDOverRSIEntryStrategy();

            default:
                return null;
        }
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public Integer getTradesLimit() {
        return tradesLimit;
    }

    public String getOperation() {
        return operation;
    }

    public CandlestickInterval getInterval() {
        return interval;
    }

    public EntryStrategy getEntryStrategy() {
        return entryStrategy;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
