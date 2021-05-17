package utils;

import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.enums.ContractType;
import com.binance.client.model.market.Candlestick;
import com.opencsv.CSVWriter;
import singletonHelpers.RequestClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CSVDataWriter {

    public static void writeDataAtOnce() {
        Long startTime = 1420063200000L;
        Long endTime=System.currentTimeMillis();
        String[] symbols = {"btcusdt", "ethusdt", "trxusdt", "bnbusdt", "xrpusdt", "filusdt",
                "eosusdt", "hotusdt", "ltcusdt", "adausdt"};
//            String[] intervals = {"MONTHLY", "WEEKLY", "THREE_DAILY", "DAILY", "TWELVE_HOURLY", "EIGHT_HOURLY",
//                    "StringIX_HOURLY", "FOUR_HOURLY", "TWO_HOURLY", "HOURLY", "HALF_HOURLY", "FIFTEEN_MINUTES", "FIVE_MINUTES", "THREE_MINUTES", "ONE_MINUTE"};
        CandlestickInterval[] intervals = new CandlestickInterval[]{/*CandlestickInterval.THREE_DAILY, CandlestickInterval.DAILY, CandlestickInterval.TWELVE_HOURLY
                , CandlestickInterval.EIGHT_HOURLY, CandlestickInterval.SIX_HOURLY, CandlestickInterval.FOUR_HOURLY,*/ CandlestickInterval.TWO_HOURLY, CandlestickInterval.HOURLY, CandlestickInterval.HALF_HOURLY
                , CandlestickInterval.FIFTEEN_MINUTES, CandlestickInterval.FIVE_MINUTES, CandlestickInterval.ONE_MINUTE};
        long fullDay = 86400000L;
//            List<Long> candlesAmounts = new ArrayList<>();
//            candlesAmounts.add(new Long())
        int[] time = {1, 2, 2, 2, 4, 5, 6, 7, 14, 26, 60, 111, 310, 550, 1500};
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        for (String symbol : symbols) {
            System.out.println(symbol);
            for (CandlestickInterval interval : intervals) {
                startTime = 1420063200000L;
                System.out.println(interval);
                List<Candlestick> candlestickBars = new ArrayList<>();
                ArrayList<Candlestick> bigList = new ArrayList<Candlestick>();
                int part=1;
                String fileName = symbol + " - " + interval + "part "+part;
                File file = new File("C:/Users/urib9/OneDrive/Candles1/" + symbol + "/" + fileName);
                int count=0;
                while (startTime < endTime) {
                    count+=1500;
                    if(count %16==0) System.out.println(count);
                    else System.out.print("+Candle, ");
                    if (count+1 % 10000==0) {
                        writToFile(file, candlestickBars);
                        part++;
                        candlestickBars.clear();
                    }
                    switch (interval) {
                        case ONE_MINUTE:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime += 1000*1500;
                            break;
                        case THREE_MINUTES:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime +=3000*1500;
                            break;
                        case FIVE_MINUTES:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                            startTime, endTime, 1499));
                            startTime += 5000*1500;
                            break;
                        case FIFTEEN_MINUTES:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime +=( fullDay / 24 / 4)*1500;
                            break;
                        case HALF_HOURLY:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime += (fullDay / 24 / 2)*1500;
                            break;
                        case HOURLY:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime +=( fullDay / 24)*1500;
                            break;
                        case TWO_HOURLY:
                                candlestickBars.addAll(syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                        startTime, endTime, 1499));

                            startTime += (fullDay / 12)*1500;
                            break;
                        case FOUR_HOURLY:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime += (fullDay / 6)*1500;
                            break;
                        case SIX_HOURLY:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime += (fullDay / 4)*1500;
                            break;
                        case EIGHT_HOURLY:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime += (fullDay / 3)*1500;
                            break;
                        case TWELVE_HOURLY:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime += (fullDay / 2)*1500;
                            break;
                        case DAILY:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime += fullDay*1500;
                            break;
                        case THREE_DAILY:
                            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks(symbol, ContractType.PERPETUAL, interval,
                                    startTime, endTime, 1499));
                            startTime += fullDay*3*1500;
                            break;

                    }
                }
                writToFile(file, candlestickBars);
            }
        }

    }
    //1500 minutes = 90 000 seconds
    public static void getAndWriteData() {
        long startTime = 1618088400000L;
        long endTime = System.currentTimeMillis() - 90000000L;
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        List<Candlestick> candlestickBars = new ArrayList<>();
        File file = new File("./lotOfCandles");
        while (startTime < endTime){
            candlestickBars.addAll( syncRequestClient.getContinuousCandlesticks("btcusdt", ContractType.PERPETUAL, CandlestickInterval.ONE_MINUTE,
                    startTime, null, 1500));
            startTime += 90000000;
        }
        writToFile(file, candlestickBars);
    }


    public static void writToFile(File file, List<Candlestick> candlestickBars) {
        // create a List which contains String array
        ArrayList<String[]> data = new ArrayList<>();

        data.add(new String[]{"openTime", "open", "high", "low",
                "close", "volume", "closeTime", "quoteAssetVolume", "numTrades", "takerBuyBaseAssetVolume", "takerBuyQuoteAssetVolume"});
        for (Candlestick candlestick :candlestickBars) {
                data.add(candlestickToStringArray(candlestick));
        }
        try{
        // create FileWriter object with file as parameter
        FileWriter outputFile = new FileWriter(file);

        // create CSVWriter object filewriter object as parameter
        CSVWriter writer = new CSVWriter(outputFile);

            writer.writeAll(data);
            writer.flush();
            // closing writer connection
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



    }

    public static void main(String[] args) {
        CSVDataWriter.getAndWriteData();
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




