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
        updateManagement(realTimeData);
        if (!fastManagement) {
            switch (positionSide) {
                case SHORT:
                    if (isTrailing) {
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                            isTrailing = false;
                            fastManagement = true;
                            return null;
                        }
                        if (trailer.needToSell(realTimeData.getCurrentPrice())) {
                            TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                            return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                        }
                    } else {
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP,MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                            isTrailing = true;
                            trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                            TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                        }
                    }
                    return null;
                case LONG:
                    if (isTrailing) {
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                            isTrailing = false;
                            fastManagement = true;
                            return null;
                        }
                        if (trailer.needToSell(realTimeData.getCurrentPrice())) {
                            TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                            return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                        }
                    } else {
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                            isTrailing = true;
                            trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                            TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                        }
                    }


                    return null;

            }
        }else
        {
            switch (positionSide){

                case SHORT:
                    if(crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)){
                        TelegramMessenger.sendToTelegram("Closing position with fast RSI crossed MA " + new Date(System.currentTimeMillis()));
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    }
                    break;
                case LONG:
                    if(crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
                        TelegramMessenger.sendToTelegram("Closing position with fast RSI crossed MA " + new Date(System.currentTimeMillis()));
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    }
                    break;
            }
        }
        return null;
    }
}
