package strategies.MACDOverCCIWIthATR.Long;

import TradingTools.Trailers.SkippingExitTrailer;
import TradingTools.Trailers.TrailingExit;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.PositionSide;
import data.Config;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import positions.PositionHandler;
import positions.SellingInstructions;
import singletonHelpers.RequestClient;
import singletonHelpers.TelegramMessenger;
import strategies.MACDOverCCIWIthATR.BaseMACDOverCCIWIthATRExitStrategy;
import strategies.MACDOverCCIWIthATR.MACDOverCCIWIthATRConstants;

import java.util.Date;

public class MACDOverCCIWIthATRLongExitStrategy1 extends BaseMACDOverCCIWIthATRExitStrategy {
    public double ATRValue;
    private boolean cancelledTP=false;
    private SkippingExitTrailer skippingExitTrailer;
    private boolean isTrailing=false;


    public MACDOverCCIWIthATRLongExitStrategy1(double initiallPrice, int maxDCACount, double initialAmount, double amountFactor,
                                               PositionSide positionSide, double TPPrice, double DCAPrices, String symbol, boolean useTP, double step,double stepFactor, DataHolder dataHolder, TrailingExit trailingExit, DCAInstructions dcaInstructions) {
        super(initiallPrice, maxDCACount, initialAmount, amountFactor, positionSide, TPPrice,DCAPrices,symbol,useTP,step,stepFactor, dataHolder, dcaInstructions );
        this.tPPrice = TPPrice;
        this.ATRValue = ATRValue;
        this.skippingExitTrailer=(SkippingExitTrailer) trailingExit;
    }

    @Override
    public Instructions run(DataHolder realTimeData) {
        if (!isTrailing) {
            if (realTimeData.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE, 1.00)) {
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                //syncRequestClient.cancelOrder(orderTP.getSymbol(), orderDCA.getOrderId(), orderDCA.getClientOrderId());
                isTrailing = true;
            }
        } else if (isTrailing) {
            double currentPrice = realTimeData.getCurrentPrice();
            skippingExitTrailer.updateTrailer(realTimeData.getClosePriceAtIndex(realTimeData.getLastCloseIndex()));
            if (realTimeData.crossed(DataHolder.IndicatorType.PERECENT_BI, DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE, 1.00)) {
                isTrailing = false;
                //TakeProfit(new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT, Config.ONE_HANDRED), calculateTotalAmount(),currentPrice, realTimeData);
                TelegramMessenger.sendToTelegram("stop trailing position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
                return null;
            } else {
                if (skippingExitTrailer != null) {
                    if (skippingExitTrailer.needToSell(currentPrice)) {
                        TelegramMessenger.sendToTelegram("trailing position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
                                MACDOverCCIWIthATRConstants.MACDOverCCIWIthATR_SELLING_PERCENTAGE);
                    }
                }else {
                    TelegramMessenger.sendToTelegram("closing position with long exit 1" + "time: " + new Date(System.currentTimeMillis()));
                    return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_LIMIT,
                            MACDOverCCIWIthATRConstants.MACDOverCCIWIthATR_SELLING_PERCENTAGE);}



            }
        }
        return null;
    }
    @Override
    public void updateExitStrategy(){}



}
