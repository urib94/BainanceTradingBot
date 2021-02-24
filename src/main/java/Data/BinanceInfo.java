package Data;

import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.market.ExchangeInfoEntry;
import com.binance.client.api.model.market.ExchangeInformation;

import java.util.HashMap;
import java.util.Map;

public class BinanceInfo {
    private static ExchangeInformation exchangeInformation;
    private static Map<String, ExchangeInfoEntry> symbolInformation;

    public BinanceInfo(){
        symbolInformation = new HashMap<>();
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        exchangeInformation = syncRequestClient.getExchangeInformation();
        for (ExchangeInfoEntry exchangeInfoEntry: exchangeInformation.getSymbols()){
            symbolInformation.put(exchangeInfoEntry.getSymbol(), exchangeInfoEntry);
        }
    }

    public static ExchangeInformation getExchangeInformation(){
        return exchangeInformation;
    }

    /**
     *
     * @param symbol need to be upper case.
     * @return the ExchangeInfoEntry of symbol.
     */
    public static ExchangeInfoEntry getSymbolInformation(String symbol){
        return symbolInformation.get(symbol);
    }

}
