package strategies.MACrosses;

import TradingTools.Trailers.ExitTrailer;
import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;

import java.util.Date;

public class MACrossesExitStrategy2 extends BaseMACrossesExitStrategy {

    private boolean isTrailing = false;
    private final ExitTrailer trailer;

    public MACrossesExitStrategy2(PositionSide positionSide, ExitTrailer trailer) {
        super(positionSide);
        this.trailer = trailer;
    }

    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        boolean rsiAboveSmaOverRsi = realTimeData.getRSIValueAtIndex(realTimeData.getLastCloseIndex()) > realTimeData.getSmaOverRSIValue(realTimeData.getLastCloseIndex());
        switch (positionSide){
            case SHORT:
                if (isTrailing) {
                    trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                    if (rsiAboveSmaOverRsi || !rsiMoveTowardsSma(realTimeData) || crossedSma(realTimeData, DataHolder.IndicatorType.MFI, DataHolder.CrossType.DOWN, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                        isTrailing = false;
                        return null;
                    }
                    if (trailer.needToSell(realTimeData.getCurrentPrice())){
                        TelegramMessenger.sendToTelegram("Closing position with MFI crossed MA " + new Date(System.currentTimeMillis()));
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    }
                }
                else {
                    if (!rsiAboveSmaOverRsi && rsiMoveTowardsSma(realTimeData) && crossedSma(realTimeData, DataHolder.IndicatorType.MFI, DataHolder.CrossType.UP, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                        isTrailing = true;
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                    }
                }
                return null;

            case LONG:
                if (isTrailing) {
                    trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                    if (!rsiAboveSmaOverRsi || !rsiMoveTowardsSma(realTimeData) || crossedSma(realTimeData, DataHolder.IndicatorType.MFI, DataHolder.CrossType.UP, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                        isTrailing = false;
                        return null;
                    }
                    if (trailer.needToSell(realTimeData.getCurrentPrice())){
                        TelegramMessenger.sendToTelegram("Closing position with MFI crossed MA " + new Date(System.currentTimeMillis()));
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    }
                }
                else {

                    if (rsiAboveSmaOverRsi && rsiMoveTowardsSma(realTimeData) && crossedSma(realTimeData, DataHolder.IndicatorType.MFI, DataHolder.CrossType.DOWN, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {
                        isTrailing = true;
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                    }
                }
                return null;
        }
        return null;
    }

    private boolean rsiMoveTowardsSma(DataHolder realTimeData){
        int index = realTimeData.getLastCloseIndex();
        double prevDistance = Math.abs(realTimeData.getSmaOverRSIValue(index - 1) - realTimeData.getRSIValueAtIndex(index - 1));
        double currDistance = Math.abs(realTimeData.getSmaOverRSIValue(index) - realTimeData.getRSIValueAtIndex(index));
        return currDistance < prevDistance;
    }


}
