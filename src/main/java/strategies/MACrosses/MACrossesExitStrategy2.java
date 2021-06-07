//package strategies.MACrosses;
//
//import TradingTools.Trailers.ExitTrailer;
//import TradingTools.Trailers.SkippingExitTrailer;
//import com.binance.client.model.enums.PositionSide;
//import data.DataHolder;
//import positions.PositionHandler;
//import positions.SimulatedSellingInstructions;
//import singletonHelpers.TelegramMessenger;
//
//import java.util.Date;
//
//public class MACrossesExitStrategy2 extends BaseMACrossesExitStrategy {
//    private boolean arabSolotionToAlertManagmatShifted = false;
//    private boolean isSlowTrailing = false;
//    private boolean isFastTrailing = false;
//    private final SkippingExitTrailer slowTrailer;
//    private final SkippingExitTrailer fastTrailer;
//
//    public MACrossesExitStrategy2(PositionSide positionSide, SkippingExitTrailer slowTrailer, SkippingExitTrailer fastTrailer) {
//        super(positionSide);
//        this.slowTrailer = slowTrailer;
//        this.fastTrailer = fastTrailer;
//
//    }
//
//    @Override
//    public SimulatedSellingInstructions run(DataHolder realTimeData) {
//        //updateManagement(realTimeData);
//        if (!fastManagement/* && slowCondition*/) {
//            if (!arabSolotionToAlertManagmatShifted){
//                TelegramMessenger.sendToTelegram("Transitioning to slow mangment");
//                arabSolotionToAlertManagmatShifted = true;
//            }
//            switch (positionSide) {
//                case SHORT:
//                    if (isSlowTrailing) {
//                        slowTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
//                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT)) {
//                            isSlowTrailing = false;
//                            fastManagement = true;
//                            TelegramMessenger.sendToTelegram("Transitioning back to fast mangment");
//                            slowCondition = true;
//                            return null;
//                        }
//                        if (slowTrailer.needToSell(realTimeData.getCurrentPrice())) {
//                            TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
//                            return new SimulatedSellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
//                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
//                        }
//                    } else {
//                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP,MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT)) {
//                            isSlowTrailing = true;
//                            slowCondition = false;
//                            slowTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
//                            TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
//                        }
//                    }
//                    return null;
//                case LONG:
//                    if (isSlowTrailing) {
//                        slowTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
//                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT)) {
//                            isSlowTrailing = false;
//                            slowCondition = true;
//                            fastManagement = true;
//                            TelegramMessenger.sendToTelegram("Transitioning back to fast mangment");
//                            return null;
//                        }
//                        if (slowTrailer.needToSell(realTimeData.getCurrentPrice())) {
//                            TelegramMessenger.sendToTelegram("Closing position with RSI crossed MA " + new Date(System.currentTimeMillis()));
//                            return new SimulatedSellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
//                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
//                        }
//                    } else {
//                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT)) {
//                            isSlowTrailing = true;
//                            slowCondition = false;
//                            slowTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
//                            TelegramMessenger.sendToTelegram("Started trailing " + new Date(System.currentTimeMillis()));
//                        }
//                    }
//                    return null;
//            }
//        }else {
//            switch (positionSide) {
//                case SHORT:
//                    if (isFastTrailing) {
//                        fastTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
//                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
//                            isFastTrailing = false;
//                            fastManagement = true;
//                            TelegramMessenger.sendToTelegram("Stoped  fast trailing " + new Date(System.currentTimeMillis()));
//                            return null;
//                        }
//                        if (fastTrailer.needToSell(realTimeData.getCurrentPrice())) {
//                            TelegramMessenger.sendToTelegram("Closing position with fast RSI crossed MA " + new Date(System.currentTimeMillis()));
//                            return new SimulatedSellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
//                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
//                        }
//                    } else {
//                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP,MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
//                            isFastTrailing = true;
//                            fastTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
//                            TelegramMessenger.sendToTelegram("Started  fast trailing " + new Date(System.currentTimeMillis()));
//                        }
//                    }
//                    return null;
//                case LONG:
//                    if (isFastTrailing) {
//                        fastTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
//                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
//                            isFastTrailing = false;
//                            fastManagement = true;
//                            TelegramMessenger.sendToTelegram("Stoped  fast trailing " + new Date(System.currentTimeMillis()));
//
//                            return null;
//                        }
//                        if (fastTrailer.needToSell(realTimeData.getCurrentPrice())) {
//                            TelegramMessenger.sendToTelegram("Closing position fast with RSI crossed MA " + new Date(System.currentTimeMillis()));
//                            return new SimulatedSellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
//                                    MACrossesConstants.EXIT_SELLING_PERCENTAGE);
//                        }
//                    } else {
//                        if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT)) {
//                            isFastTrailing = true;
//                            fastTrailer.updateTrailer(realTimeData.getOpenPrice(realTimeData.getLastIndex()));
//                            TelegramMessenger.sendToTelegram("Started fast trailing " + new Date(System.currentTimeMillis()));
//                        }
//                    }
//                    return null;
//            }
//        }
//        return null;
//    }
//}
