package strategies.rsiDivergence;

import TradingTools.Trailers.SkippingExitTrailer;
import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.TelegramMessenger;
import strategies.ExitStrategy;
import strategies.MACrosses.MACrossesConstants;
import strategies.MAStrategies.BasicMaStrategy.BasicMaConsts;

import java.util.Date;

public class rsiDivergenceExitStrategy1 implements ExitStrategy{
    private boolean isTrailing = false;
    private final SkippingExitTrailer trailer;

    public rsiDivergenceExitStrategy1(PositionSide positionSide, SkippingExitTrailer trailer) {
        super(positionSide);
        this.trailer = trailer;

    }

    @Override
    public SellingInstructions run(DataHolder realTimeData) {

        switch (positionSide) {
            case SHORT:
                if (isTrailing) {
                    trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                    if (shortFastTriger(realTimeData) || shortSlowTriger(realTimeData)) {
                        isTrailing = false;
                        return null;
                    }
                    if (trailer.needToSell(realTimeData.getCurrentPrice())) {
                        TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    }
                } else {
                    if (rsiSpikedUp(realTimeData)) return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
                            MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    if (longFastTriger(realTimeData) || longSlowTriger(realTimeData)) {
                        isTrailing = true;
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                    }
                }
                return null;
            case LONG:
                if (isTrailing) {
                    trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                    if (longFastTriger(realTimeData) || longSlowTriger(realTimeData) || rsiSlowAndFastSmaCrossedUp(realTimeData)) {
                        isTrailing = false;
                        return null;
                    }
                    if (trailer.needToSell(realTimeData.getCurrentPrice())) {
                        TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    }
                } else {
                    if (rsiSpikedDown(realTimeData)) return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                            MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                    if (shortFastTriger(realTimeData) || shortSlowTriger(realTimeData) || rsiSlowAndFastSmaCrossedDown(realTimeData)) {
                        isTrailing = true;
                        slowCondition = false;
                        trailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
                        TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
                    }
                }
                return null;
        }
        return null;
    }


    private boolean isLongArea(DataHolder realTimeData){
        int index = realTimeData.getLastCloseIndex();
        double fastSma = realTimeData.getFastSmaValue(index);
        double slowSma = realTimeData.getSlowSmaOverRSIValue(index);
        return fastSma > slowSma;
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

    private boolean shortFastTriger(DataHolder realTimeData) {
        if (rsiCrossedSma(realTimeData, DataHolder.CrossType.DOWN, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
            return !volumeSpike(realTimeData);
        }
        return false;
    }

    private boolean shortSlowTriger(DataHolder realTimeData){
        if (rsiCrossedSma(realTimeData, DataHolder.CrossType.DOWN, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT)) {
            return !volumeSpike(realTimeData);
        }
        return false;
    }

    private boolean longFastTriger(DataHolder realTimeData) {
        if (rsiCrossedSma(realTimeData, DataHolder.CrossType.UP, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT )) {
            return !volumeSpike(realTimeData);
        }
        return false;
    }



    private boolean longSlowTriger(DataHolder realTimeData) {
        if (rsiCrossedSma(realTimeData, DataHolder.CrossType.UP, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT)) {
            return !volumeSpike(realTimeData);
        }
        return false;
    }

    private boolean rsiSlowAndFastSmaCrossedUp(DataHolder realTimeData){
        int closeIndex = realTimeData.getLastCloseIndex();
        double fastSmaCurrValue = realTimeData.getFastSmaValue(closeIndex);
        double fastSmaPrevValue = realTimeData.getFastSmaValue(closeIndex - 1);
        double slowSmaCurrValue = realTimeData.getFastSmaOverRSIValue(closeIndex);
        double slowSmaPrevValue = realTimeData.getFastSmaOverRSIValue(closeIndex - 1);
        return slowSmaPrevValue >= fastSmaPrevValue && slowSmaCurrValue < fastSmaCurrValue;
    }

    private boolean rsiSlowAndFastSmaCrossedDown(DataHolder realTimeData){
        int closeIndex = realTimeData.getLastCloseIndex();
        double fastSmaCurrValue = realTimeData.getFastSmaValue(closeIndex);
        double fastSmaPrevValue = realTimeData.getFastSmaValue(closeIndex - 1);
        double slowSmaCurrValue = realTimeData.getFastSmaOverRSIValue(closeIndex);
        double slowSmaPrevValue = realTimeData.getFastSmaOverRSIValue(closeIndex - 1);
        return slowSmaPrevValue <= fastSmaPrevValue && slowSmaCurrValue > fastSmaCurrValue;
    }
    private boolean rsiCrossedSma(DataHolder realTimeData, DataHolder.CrossType crossType, int barCount) {
        int closeIndex = realTimeData.getLastCloseIndex();
        double curr, prev, smaCurrValue, smaPrevValue;
        curr = realTimeData.getRSIValueAtIndex(closeIndex);
        prev = realTimeData.getRSIValueAtIndex(closeIndex - 1);
        if(barCount == MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT) {
            smaCurrValue = realTimeData.getSlowSmaOverRSIValue(closeIndex);
            smaPrevValue = realTimeData.getSlowSmaOverRSIValue(closeIndex - 1);
        } else {
            smaCurrValue = realTimeData.getFastSmaOverRSIValue(closeIndex);
            smaPrevValue = realTimeData.getFastSmaOverRSIValue(closeIndex - 1);

        }
        switch (crossType) {

            case UP:
                return prev <= smaPrevValue && curr > smaCurrValue;

            case DOWN:
                return prev >= smaPrevValue && curr < smaCurrValue;
        }
        return false;
    }

    private boolean outOfCloseBoliingers(DataHolder realTimeData) {
        int index =realTimeData.getLastCloseIndex();
        double upperBoliinger = realTimeData.getCloseUpperBollingerAtIndex(index);
        double lowerBoliinger = realTimeData.getCloseLowerBollingerAtIndex(index);
        double currentPrice = realTimeData.getCurrentPrice();
//        System.out.println(" upper = " + upperBoliinger + "\n lower = " + lowerBoliinger + "\ncurrent price = " + currentPrice );
        boolean in = currentPrice > lowerBoliinger && currentPrice < upperBoliinger;
//        System.out.println( "in = "+ in);
        return !in;
    }

    private boolean volumeSpike(DataHolder realTimeData) {
        int index = realTimeData.getLastCloseIndex();
        double volume = realTimeData.getVolumeAtIndex(index);
        double smaOverVolume = realTimeData.getSmaOverVolumeValueAtIndex(index);
        return volume > 2.5 * smaOverVolume && volume < 3.5 * smaOverVolume;
    }


    private boolean crossedSma(DataHolder realTimeData, DataHolder.IndicatorType indicatorType, DataHolder.CrossType crossType, int candleCount){
        int closeIndex = realTimeData.getLastCloseIndex();
        double prev = 0, curr = 0;
        double smaCurrValue = 0, smaPrevValue = 0;
        switch(indicatorType){
            case RSI:
                curr = realTimeData.getRSIValueAtIndex(closeIndex);
                prev = realTimeData.getRSIValueAtIndex(closeIndex - 1);
                if(candleCount == MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT) {
                    smaCurrValue = realTimeData.getFastSmaValue(closeIndex);
                    smaPrevValue = realTimeData.getFastSmaValue(closeIndex - 1);
                } else {
                    if (candleCount == MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT) {
                        smaCurrValue = realTimeData.getFastSmaOverRSIValue(closeIndex);
                        smaPrevValue = realTimeData.getFastSmaOverRSIValue(closeIndex - 1);
                    }
                }
                break;

            default:
                break;
        }
        switch(crossType){
            case UP:
                if(smaCurrValue > 40 && smaCurrValue < 60 ){
                    return false;
                }
                return prev <= smaPrevValue && curr > smaCurrValue + MACrossesConstants.ENTRY_THRESHOLD;
            case DOWN:
                if(smaCurrValue > 40 && smaCurrValue < 60 ){
                    return false;
                }
                return prev >= smaPrevValue && curr < smaCurrValue - MACrossesConstants.ENTRY_THRESHOLD;
        }
        return false;
    }
}

