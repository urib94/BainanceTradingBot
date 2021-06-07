package main.java.TradingTools.DCA;//package TradingTools.DCA;
//
//import com.binance.client.model.enums.PositionSide;
//import data.DataHolder;
//import positions.DCAInstructions;
//import positions.PositionHandler;
//import strategies.DCAStrategy;
//
//
//
//public class StrategyBacedDCA extends BaseDCA{
//    DCAStrategy DCAStrategy;
//
//
//    public StrategyBacedDCA(double step, double stepFactor, double initiallPrice, double maxDCACount, double initialAmount,
//                            double amountFactor, PositionSide positionSide, DCAStrategy DCAStrategy, PositionHandler positionHandler) {
//        super(step, stepFactor, initiallPrice, maxDCACount, initialAmount, amountFactor, positionSide);
//        this.DCAStrategy=DCAStrategy;
//    }
//
//    @Override
//    public DCAInstructions run(DataHolder realTimeData) {
//        if(strategies.DCAStrategy.needToDCA(realTimeData) ){
//            switch (positionSide){
//                //// TODO: 4/5/2021  includ position side BOTH
//                case SHORT:
//                    return DCAStrategy.getDCAInstructions();//
//                case LONG:
//                    return new DCAInstructions(PositionHandler.DCAType.LONG_DCA_LIMIT,calculateDCASize());
//            }
//        }
//        return null;
//    }
//
//}
