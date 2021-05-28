package strategies.MACrosses;

import TradingTools.Trailers.SkippingExitTrailer;
import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;

import java.util.Date;

public class MACrossesExitStrategy1 extends BaseMACrossesExitStrategy {
    private boolean isSlowTrailing = false;
    private boolean isFastTrailing = false;
    private final SkippingExitTrailer slowTrailer;
    private final SkippingExitTrailer fastTrailer;
    
    public MACrossesExitStrategy1(PositionSide positionSide, SkippingExitTrailer slowTrailer, SkippingExitTrailer fastTrailer) {
        super(positionSide);
        this.slowTrailer = slowTrailer;
        this.fastTrailer = fastTrailer;

    }

    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        updateManagement(realTimeData);
        if (!fastManagement) {
            switch (positionSide) {
                case SHORT:
                    if (isSlowTrailing) {
                        slowTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                            isSlowTrailing = false;
                            fastManagement = true;
                            return null;
                        }
                        if (slowTrailer.needToSell(realTimeData.getCurrentPrice())) {
                            TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                            return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                        }
                    } else {
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP,MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                            isSlowTrailing = true;
                            slowTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                            TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                        }
                    }
                    return null;
                case LONG:
                    if (isSlowTrailing) {
                        slowTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                            isSlowTrailing = false;
                            fastManagement = true;
                            return null;
                        }
                        if (slowTrailer.needToSell(realTimeData.getCurrentPrice())) {
                            TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                            return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                        }
                    } else {
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                            isSlowTrailing = true;
                            slowTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                            TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                        }
                    }
                    return null;
            }
        }else {
            switch (positionSide) {
                case SHORT:
                    if (isFastTrailing) {
                        fastTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
                            isFastTrailing = false;
                            fastManagement = true;
                            return null;
                        }
                        if (fastTrailer.needToSell(realTimeData.getCurrentPrice())) {
                            TelegramMessenger.sendToTelegram("Closing position with fast RSI crossed MA " + new Date(System.currentTimeMillis()));
                            return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                        }
                    } else {
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP,MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
                            isFastTrailing = true;
                            fastTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                            TelegramMessenger.sendToTelegram("Started  fast trailing " + new Date(System.currentTimeMillis()));
                        }
                    }
                    return null;
                case LONG:
                    if (isFastTrailing) {
                        fastTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
                            isFastTrailing = false;
                            fastManagement = true;
                            return null;
                        }
                        if (fastTrailer.needToSell(realTimeData.getCurrentPrice())) {
                            TelegramMessenger.sendToTelegram("Closing position fast with RSI crossed MA " + new Date(System.currentTimeMillis()));
                            return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                        }
                    } else {
                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
                            isFastTrailing = true;
                            fastTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                            TelegramMessenger.sendToTelegram("Started fast trailing " + new Date(System.currentTimeMillis()));
                        }
                    }
                    return null;
            }
        }
        return null;
    }
}
