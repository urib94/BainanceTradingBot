package strategies.rsiDivergence;

import TradingTools.Trailers.SkippingExitTrailer;
import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.ExitStrategy;
import strategies.MACrosses.MACrossesConstants;

import java.util.Date;

public class rsiDivergenceExitStrategy1 implements ExitStrategy{
    private boolean isTrailing = false;
    private final SkippingExitTrailer trailer;
    private final PositionSide positionSide;

    public rsiDivergenceExitStrategy1(PositionSide positionSide, SkippingExitTrailer trailer) {
        this.positionSide = positionSide;
        this.trailer = trailer;

    }

    @Override
    public SellingInstructions run(DataHolder realTimeData) {

        switch (positionSide) {
            case SHORT:
                if (isTrailing) {
                    trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                    if (devCrossedDown(realTimeData)) {
                        isTrailing = false;
                        return null;
                    }
                    if (trailer.needToSell(realTimeData.getCurrentPrice())) {
                        TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    }
                } else {
                    if (devCrossedUp(realTimeData)) {
                        isTrailing = true;
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                    }
                }
                return null;
            case LONG:
                if (isTrailing) {
                    trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                    if (devCrossedUp    (realTimeData)) {
                        isTrailing = false;
                        return null;
                    }
                    if (trailer.needToSell(realTimeData.getCurrentPrice())) {
                        TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    }
                } else {
                    if (devCrossedDown(realTimeData)) {
                        isTrailing = true;
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                    }
                }
                return null;
        }
        return null;
    }


    private boolean devCrossedDown(DataHolder realTimeData) {
        int index = realTimeData.getLastCloseIndex();
        double currDev = realTimeData.getRsiDivergenceAtIndex(index);
        double prevDev = realTimeData.getRsiDivergenceAtIndex(index - 1);
        return prevDev > 0 && currDev <= 0;
    }

    private boolean devCrossedUp(DataHolder realTimeData) {
        int index = realTimeData.getLastCloseIndex();
        double currDev = realTimeData.getRsiDivergenceAtIndex(index);
        double prevDev = realTimeData.getRsiDivergenceAtIndex(index - 1);
        return prevDev < 0 && currDev >= 0;
    }

    private boolean rsiSpikedUp(DataHolder realTimeData){
        int index = realTimeData.getLastCloseIndex();
        double currRsiValue = realTimeData.getRSIValueAtIndex(index);
        double prevRsiValue = realTimeData.getRSIValueAtIndex(index);
        return currRsiValue > prevRsiValue + 10 && currRsiValue < prevRsiValue + 15;
    }
    private boolean rsiSpikedDown(DataHolder realTimeData){
        int index = realTimeData.getLastCloseIndex();
        double currRsiValue = realTimeData.getRSIValueAtIndex(index);
        double prevRsiValue = realTimeData.getRSIValueAtIndex(index);
        return currRsiValue < prevRsiValue - 10 && currRsiValue > prevRsiValue - 15;
    }

}

