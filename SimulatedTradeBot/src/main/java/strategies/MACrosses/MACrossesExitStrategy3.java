package main.java.strategies.MACrosses;//package strategies.MACrosses;
//
//import com.binance.client.model.enums.PositionSide;
//import data.DataHolder;
//import positions.PositionHandler;
//import positions.SimulatedSellingInstructions;
//
//public class MACrossesExitStrategy3 extends BaseMACrossesExitStrategy{
//
//    public MACrossesExitStrategy3(PositionSide positionSide) {
//        super(positionSide);
//    }
//
//    @Override
//    public SimulatedSellingInstructions run(DataHolder realTimeData) {
//        switch (positionSide){
//
//            case SHORT:
//                if(priceIsAboveSMA(realTimeData)){// long only
//                    if(crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT) ||
//                            (crossedSma(realTimeData, DataHolder.IndicatorType.MFI, DataHolder.CrossType.UP, MACrossesConstants.SMA_OVER_MFI_BAR_COUNT) && mfiAndRSIAlign(realTimeData))){
//                        return new SimulatedSellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
//                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
//                    }
//                }
//                break;
//            case LONG:
//                if (!priceIsAboveSMA(realTimeData)){
//                    if(crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.SLOW_SMA_OVER_RSI_BAR_COUNT) ||
//                            (crossedSma(realTimeData, DataHolder.IndicatorType.MFI, DataHolder.CrossType.DOWN, MACrossesConstants.SMA_OVER_MFI_BAR_COUNT) && mfiAndRSIAlign(realTimeData))){
//                        return new SimulatedSellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
//                                MACrossesConstants.EXIT_SELLING_PERCENTAGE);
//                    }
//                }
//                break;
//        }
//        return null;
//    }
//
//    private boolean priceIsAboveSMA(DataHolder realTimeData) {
//        int index = realTimeData.getLastCloseIndex();
//        double smaValue = realTimeData.getSlowSmaValueAtIndex(index);
//        double smaPrevValue = realTimeData.getSlowSmaValueAtIndex(index - 1);
//        double closeValue = realTimeData.getClosePriceAtIndex(index);
//        double closePrevValue = realTimeData.getClosePriceAtIndex(index - 1);
//        return closeValue > smaValue || closePrevValue > smaPrevValue;
//    }
//
//    private boolean mfiAndRSIAlign(DataHolder realTimeData){
//        int index = realTimeData.getLastCloseIndex();
//        double currRsi = realTimeData.getRSIValueAtIndex(index);
//        double prevRsi = realTimeData.getRSIValueAtIndex(index - 1);
//        double currMfi = realTimeData.getRSIValueAtIndex(index);
//        double prevMfi = realTimeData.getRSIValueAtIndex(index - 1);
//        if (prevRsi < currRsi){
//            return prevMfi < currMfi;
//        }
//        else{
//            return prevMfi > currMfi;
//        }
//    }
//}
