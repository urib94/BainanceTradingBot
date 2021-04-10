import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.enums.ContractType;
import com.binance.client.model.market.Candlestick;
import com.opencsv.CSVWriter;
import singletonHelpers.RequestClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Draft {
    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        Date startDate=new Date (115, Calendar.JANUARY,1);
        String fileName = "symbol" + " - " + "interval";
        File file = new File("C:/Users/urib9/OneDrive/Candles1/" + fileName);
        System.out.println(startDate.getTime()+"   "+System.currentTimeMillis());
        try{
            // create FileWriter object with file as parameter
            FileWriter outputFile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputFile);
            List<String[]> data=new ArrayList<>();
            data.add(new String[]{"s", "D", "f"});

            writer.writeAll(data);

            // closing writer connection
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}