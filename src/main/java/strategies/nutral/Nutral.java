//package strategies.nutral;
//
//import com.binance.client.SyncRequestClient;
//import com.binance.client.model.enums.*;
//import com.binance.client.model.market.Candlestick;
//import com.binance.client.model.trade.Order;
//import data.AccountBalance;
//import data.Config;
//import data.DataHolder;
//import data.RealTimeData;
//import positions.PositionHandler;
//import singletonHelpers.BinanceInfo;
//import singletonHelpers.RequestClient;
//import strategies.EntryStrategy;
//import utils.Utils;
//
//import java.util.List;
//
//public class Nutral implements EntryStrategy {
//    public  final double TP_STEP = 0.12;
//    public  final double SL_STEP = 0.06;
//    public  final double QTY = 0.01;
//    private boolean postedEntry =false;
//    AccountBalance accountBalance;
//    Order[] entry = new Order [4];
//    Order[] exit = new Order [4];
//    PositionHandler positionHandler;
//    SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
//    DataHolder dataHolder;
//    RealTimeData realTime;
//
//    public Nutral (){
//        accountBalance = AccountBalance.getAccountBalance();
//        System.out.println("nutral");
//        realTime = new RealTimeData("btcusdt",CandlestickInterval.ONE_MINUTE);
//
//
//    }
//
//
//    public Nutral copyNutral (){
//        Nutral other= new Nutral();
//        other.entry[0] = this.entry[0] ;
//        other.entry[1] = this.entry[1];
//        other.exit[0] = this.exit[0] ;
//        other.exit[1] = this.exit[1] ;
//        return other;
//    }
//
//    public synchronized PositionHandler run(DataHolder realTimeData, String symbol) {
//        if (positionHandler == null ) {
//            RealTimeData realTime = new RealTimeData(Config.BASE_SYMBOL,CandlestickInterval.ONE_MINUTE);
//            double currentPrice = realTimeData.getCurrentPrice();
//            List<Candlestick> candlestick = syncRequestClient.getCandlestick(Config.SYMBOL,CandlestickInterval.ONE_MINUTE,null, null, 1);
//            double baseCurrPrice = candlestick.get(0).getClose().doubleValue();
//            return buyAndCreatePositionHandler(realTimeData, QTY, syncRequestClient, currentPrice,baseCurrPrice, symbol);
//        }
//        return null;
//    }
//
//    @Override
//    public void setLeverage(int leverage) {
//
//    }
//
//    @Override
//    public void setRequestedBuyingAmount(double requestedBuyingAmount) {
//
//    }
//
//    @Override
//    public void positionClosed() {
//
//    }
//
//
//    public PositionHandler buyAndCreatePositionHandler(DataHolder realTimeData, double qty, SyncRequestClient syncRequestClient, double currentPrice, double baseCurrPrice, String symbol) {
//        String[] prices = calculatePrice(currentPrice, symbol);
//        String baseEntry = BinanceInfo.formatPrice(baseCurrPrice, "btcusdt");
//        String inputEntry = BinanceInfo.formatPrice(currentPrice, symbol);
//        String sellingQty = Utils.fixQuantity(BinanceInfo.formatQty(qty, symbol));
//        String baseEntryQty = Utils.fixQuantity(BinanceInfo.formatQty(qty, "btcusdt"));
//        try {
//            entry[0] = syncRequestClient.postOrder("btcusdt", OrderSide.BUY, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
//                    baseEntryQty, baseEntry, "false", null, null, null,
//                    null, null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            entry[1] = syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.BOTH, OrderType.LIMIT, TimeInForce.GTC,
//                    sellingQty, inputEntry, "false", null, null, null, null, null, WorkingType.MARK_PRICE,
//                    "true", NewOrderRespType.RESULT);
//        } catch (Exception e) { e.printStackTrace(); }postedEntry = true;
//
//        try {
//            exit[0] = syncRequestClient.postOrder("btcusdt", OrderSide.SELL, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
//                    baseEntryQty, null, "true", null, prices[0], null,
//                    prices[0], null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            exit[1] = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.STOP_MARKET, TimeInForce.GTC,
//                    sellingQty, null, "true", null, prices[1], null,
//                    prices[1], null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            exit[2] = syncRequestClient.postOrder("btcusdt", OrderSide.SELL, PositionSide.BOTH, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
//                    baseEntryQty, null, "true", null, prices[2], null,
//                    prices[2], null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            exit[3] = syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.BOTH, OrderType.TAKE_PROFIT_MARKET, TimeInForce.GTC,
//                    sellingQty, null, "true", null, prices[3], null,
//                    prices[3], null, WorkingType.MARK_PRICE, "true", NewOrderRespType.RESULT);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        System.out.println("posting new butch");
//        NutralExit exitNutral= new NutralExit (exit);
//        Nutral other =copyNutral();
//        return positionHandler = new PositionHandler(entry[0],other , exitNutral);
//    }
//
//    public String[] calculatePrice(double currentPrice, String symbol){
//        String[] prices= new String[4];
//        System.out.println(symbol);
//        prices[0] = BinanceInfo.formatPrice(currentPrice - (currentPrice / 100) * SL_STEP, "btcusdt");
//        prices[1] = BinanceInfo.formatPrice(currentPrice + (currentPrice / 100) * SL_STEP, symbol);
//        prices[2] = BinanceInfo.formatPrice((currentPrice + (currentPrice / 100) * TP_STEP), "btcusdt");
//        prices[3] = BinanceInfo.formatPrice(currentPrice - (currentPrice / 100) * TP_STEP,symbol);
//
//        return prices;
//    }
//
//
//    private void updateOrders(SyncRequestClient syncRequestClient){
//        for (Order order : entry){
//            order = syncRequestClient.getOrder(order.getSymbol(), order.getOrderId(),order.getClientOrderId() );
//        }
//
//    }
//
//    public void clearPositionHandler(){
//        positionHandler = null;
//    }
//
//
//    public boolean entryIsFilld(){
//        updateOrders(syncRequestClient);
//        for(Order order: entry){
//            if(!order.getStatus().equals(OrderStatus.FILLED)){
//                return false;
//            }
//            postedEntry = false;
//        }
//        System.out.println("finished current entry butch");
//        return true;
//    }
//
//
//    public enum NeutralCons {;
//        public  final double TP_STEP = 0.07;
//        public  final double SL_STEP = 0.03;
//        public  final double QTY = 0.01;
//    }
//}
