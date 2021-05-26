package strategies.MACDOverCCIWIthATR.Long;

import TradingTools.Trailers.SkippingExitTrailer;
import data.DataHolder;
import positions.SellingInstructions;
import strategies.ExitStrategy;

public class MACDOverCCIWIthATRLongExitStrategy1 implements ExitStrategy {
    public double ATRValue;
    private boolean cancelledTP = false;
    private SkippingExitTrailer skippingExitTrailer;
    private boolean isTrailing = false;


    public MACDOverCCIWIthATRLongExitStrategy1(){
        this.ATRValue = ATRValue;
        this.skippingExitTrailer = null;
    }

    @Override
    public SellingInstructions run(DataHolder realTimeData) {
//        if (!isTrailing) {
//            if (realTimeData.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE, 1.00)) {
//                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//                //syncRequestClient.cancelOrder(orderTP.getSymbol(), orderDCA.getOrderId(), orderDCA.getClientOrderId());
//                isTrailing = true;
//            }
//        } else if (isTrailing) {
//            double currentPrice = realTimeData.getCurrentPrice();
//            skippingExitTrailer.updateTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()));
//            if (realTimeData.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE, 1.00)) {
//                isTrailing = false;
//                //postTakeProfit(new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, Config.ONE_HANDRED), calculateTotalAmount(),currentPrice, realTimeData);
//                TelegramMessenger.sendToTelegram("stop trailing position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
//                return null;
//            } else {
//                if (skippingExitTrailer != null) {
//                    if (skippingExitTrailer.needToSell(currentPrice)) {
//                        TelegramMessenger.sendToTelegram("trailing position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
//                        return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
//                                MACDOverCCIWIthATRConstants.MACDOverCCIWIthATR_SELLING_PERCENTAGE);
//                    }
//                }else {
//                    TelegramMessenger.sendToTelegram("closing position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
//                    return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
//                            MACDOverCCIWIthATRConstants.MACDOverCCIWIthATR_SELLING_PERCENTAGE);}
//
//
//
//            }
//        }
        return null;
    }
}
