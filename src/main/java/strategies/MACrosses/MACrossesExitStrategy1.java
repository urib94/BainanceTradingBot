package strategies.MACrosses;

import TradingTools.Trailers.SkippingExitTrailer;
import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;

import java.util.Date;

public class MACrossesExitStrategy1 extends BaseMACrossesExitStrategy {
    private boolean isTrailing = false;
    private final SkippingExitTrailer trailer;
    
    public MACrossesExitStrategy1(PositionSide positionSide, SkippingExitTrailer trailer) {
        super(positionSide);
        this.trailer = trailer;

    }

    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        switch (positionSide){
            case SHORT:
            if (isTrailing) {
                trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN)) {
                    isTrailing = false;
                    return null;
                }
                if (trailer.needToSell(realTimeData.getCurrentPrice())){
                    TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                    return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
                            MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                }
            } else {
                if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP)) {
                    isTrailing = true;
                    trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                    TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                }
            }
                return null;
            case LONG:
                if (isTrailing) {
                    trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                    if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP)) {
                        isTrailing = false;
                        return null;
                    }
                    if (trailer.needToSell(realTimeData.getCurrentPrice())){
                        TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    }
                } else {
                    if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN)) {
                        isTrailing = true;
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                    }
                }
                return null;
        }
        return null;
    }
}
