import com.binance.client.model.enums.CandlestickInterval;
import org.ta4j.core.BaseBarSeries;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        RealTimeData rlt = new RealTimeData("btcusdt", CandlestickInterval.ONE_MINUTE, 40);
        rlt.updateData();

        while(true){
            //Perform operations;
            BaseBarSeries data = rlt.getRealTimeData();
        }

    }
}

//Omri we need to figure out how to cause the "while" to iterate only after there is a change in the data, i didn't figured it out yet.
//In the current code the data is being updated continuously and it is concurrent.
