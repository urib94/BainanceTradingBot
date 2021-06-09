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

public class rsiDivergenceExitStrategy2 implements ExitStrategy {

    private PositionSide positionSide;
    private boolean isTrailing = false;
    private final SkippingExitTrailer trailer;
    private double openIndex;
    private boolean needToSell = false;

    public rsiDivergenceExitStrategy2 (PositionSide positionSide,SkippingExitTrailer trailer, double opemIndex){
        this.trailer = trailer;
        this.positionSide=positionSide;
        this.openIndex =opemIndex;
    }

    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        int index = realTimeData.getLastIndex();
        boolean isActive = openIndex >= index + 3;
        boolean crossedUp = openDevCrossedUp(realTimeData, index);
        boolean crossedDown = openDevCrossedDown(realTimeData, index);
        if (isActive && !needToSell){
            switch (positionSide){

                case SHORT:
                    if (isTrailing) {
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        if (crossedDown) {
                            isTrailing = false;
                            return null;
                        }
                        if (trailer.needToSell(realTimeData.getCurrentPrice())) {
                            TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                            return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                        }

                    } else {
                        if (crossedUp) {
                            isTrailing = true;
                            trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                            TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                        }
                    }
                    return null;
                case LONG:
                    if (isTrailing) {
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        if (crossedUp) {
                            isTrailing = false;
                            return null;
                        }
                        if (trailer.needToSell(realTimeData.getCurrentPrice())) {
                            TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                            return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                        }
                    } else {
                        if (crossedDown) {
                            isTrailing = true;
                            trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                            TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                        }
                    }
                    return null;

            }
        }
        return null;
    }

    private boolean openDevCrossedDown(DataHolder realTimeData, int index) {
        double currDev = realTimeData.getRsiDivergenceAtIndex(index);
        double prevDev = realTimeData.getRsiDivergenceAtIndex(index - 1);
        return prevDev > 0 && currDev <= 0;
    }

    private boolean openDevCrossedUp(DataHolder realTimeData, int index) {
        double currDev = realTimeData.getRsiDivergenceAtIndex(index);
        double prevDev = realTimeData.getRsiDivergenceAtIndex(index - 1);
        return prevDev < 0 && currDev >= 0;
    }
}
