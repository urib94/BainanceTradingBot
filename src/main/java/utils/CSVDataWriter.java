package utils;

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
import java.util.ArrayList;
import java.util.List;

public class CSVDataWriter {
    public static void writeDataAtOnce() {
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        List<Candlestick> candlestickBars = syncRequestClient.getCandlestick("btcusdt", CandlestickInterval.ONE_MINUTE, null, null, 1500);
        // first create file object for file placed at location
        // specified by filepath
        String fileName = candlestickBars.get(1499).getOpenTime().toString();
        File file = new File("./Candles/" + fileName);

        try {
            // create FileWriter object with file as parameter
            FileWriter outputFile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputFile);

            // create a List which contains String array
            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"openTime", "open", "high", "low",
                    "close", "volume", "closeTime", "quoteAssetVolume", "numTrades", "takerBuyBaseAssetVolume", "takerBuyQuoteAssetVolume"});
            for (Candlestick candlestick : candlestickBars){
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
}
