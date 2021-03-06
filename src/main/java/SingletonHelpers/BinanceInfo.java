package SingletonHelpers;

import Utils.Utils;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.market.ExchangeInfoEntry;
import com.binance.client.api.model.market.ExchangeInformation;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BinanceInfo {
    private static ExchangeInformation exchangeInformation;
    private static Map<String, ExchangeInfoEntry> symbolInformation;

    private static class BinanceInfoHolder{
        private static BinanceInfo binanceInfo = new BinanceInfo();
    }

    private BinanceInfo(){
        symbolInformation = new HashMap<>();
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        exchangeInformation = syncRequestClient.getExchangeInformation();
        for (ExchangeInfoEntry exchangeInfoEntry: exchangeInformation.getSymbols()){
            symbolInformation.put(exchangeInfoEntry.getSymbol().toLowerCase(), exchangeInfoEntry);
        }
    }

    public static BinanceInfo getBinanceInfo() {
        return BinanceInfo.BinanceInfoHolder.binanceInfo;
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

    public static String formatQty(BigDecimal buyingQty, String symbol){
        String formatter = "%." + symbolInformation.get(symbol).getQuantityPrecision().toString() + "f";
        return String.format(formatter, Math.abs(buyingQty.doubleValue()));
    }

    public static String formatPrice(BigDecimal price, String symbol){
        return String.format("%." + symbolInformation.get(symbol).getPricePrecision().toString() + "f", price.doubleValue());
    }

}
