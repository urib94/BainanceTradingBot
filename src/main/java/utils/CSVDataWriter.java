package utils;

import com.alibaba.fastjson.JSONStreamAware;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;
import com.opencsv.CSVWriter;
import data.Config;
import singletonHelpers.RequestClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Timestamp;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class CSVDataWriter {



    public static void writeDataAtOnce() {
            String[] symbols = {"btcusdt", "ethusdt", "trxusdt", "bnbusdt", "xrpusdt", "filusdt",
                    "eosusdt", "hotusdt", "ltcusdt", "adausdt"};
            String[] intervals = {"MONTHLY", "WEEKLY", "THREE_DAILY", "DAILY", "TWELVE_HOURLY", "EIGHT_HOURLY",
                    "SIX_HOURLY", "FOUR_HOURLY", "TWO_HOURLY", "HOURLY", "HALF_HOURLY", "FIFTEEN_MINUTES", "FIVE_MINUTES", "THREE_MINUTES", "ONE_MINUTE"};
            int[] time={1,1,1,1,3,4,5,7,13,25,50,100,300,500,1500};
//        System.out.println("Enter symbol for retriving last 1500 candles");
//        String symbol = new String(scanner.next());
//        System.out.println("Enter the wonted candle interval");
            //String interval = new String(scanner.next());
            SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        for (String symbol : symbols) {
            System.out.println(symbol);
            for (int j = 0; j < intervals.length; j++) {
                CandlestickInterval candlestickInterval = IntevalMaker.makeCandlestickInterval(intervals[j]);
                List<Candlestick> candlestickBars = syncRequestClient.getCandlestick(symbol,
                        candlestickInterval, null, null, time[j]);
                // first create file object for file placed at location
                // specified by filepath
//                    String pattern = "yyyy-MM-dd";
//                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//
//                    LocalDate localDate = LocalDate.now();
                String fileName = candlestickBars.get(time[j] - 1).getOpenTime().toString();
                File file = new File("./Candles/" + symbol + "/" + intervals[j] + "/" + fileName);
                try {
                    // create FileWriter object with file as parameter
                    FileWriter outputFile = new FileWriter(file);

                    // create CSVWriter object filewriter object as parameter
                    CSVWriter writer = new CSVWriter(outputFile);


                    // create a List which contains String array
                    List<String[]> data = new ArrayList<String[]>();
                    data.add(new String[]{"openTime", "open", "high", "low",
                            "close", "volume", "closeTime", "quoteAssetVolume", "numTrades", "takerBuyBaseAssetVolume", "takerBuyQuoteAssetVolume"});
                    for (Candlestick candlestick : candlestickBars) {
                        data.add(candlestickToStringArray(candlestick));
                    }
                    writer.writeAll(data);

                    // closing writer connection
                    writer.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }



    }

    private static String[] candlestickToStringArray(Candlestick candlestick) {
        String[] strings = new String[11];
        strings[0] = candlestick.getOpenTime().toString();
        strings[1] = candlestick.getOpen().toString();
        strings[2] = candlestick.getHigh().toString();
        strings[3] = candlestick.getLow().toString();
        strings[4] = candlestick.getClose().toString();
        strings[5] = candlestick.getVolume().toString();
        strings[6] = candlestick.getCloseTime().toString();
        strings[7] = candlestick.getQuoteAssetVolume().toString();
        strings[8] = candlestick.getNumTrades().toString();
        strings[9] = candlestick.getTakerBuyBaseAssetVolume().toString();
        strings[10] = candlestick.getTakerBuyQuoteAssetVolume().toString();
        return strings;
    }

    public static void main(String[] args) {
        CSVDataWriter.writeDataAtOnce();
    }
}




