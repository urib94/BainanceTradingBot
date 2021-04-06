//package TradingTools.DCA;
//
//import data.DataHolder;
//import positions.DCAInstructions;
//import positions.PositionHandler;
//import strategies.DCAStrategy;
//
//public class DCABaceStrategy implements DCAStrategy {
//    PositionHandler positionHandler;
//    public boolean didDCA=false;
//
//    public DCABaceStrategy (PositionHandler positionHandler){
//        this.positionHandler=positionHandler;
//    }
//
//    @Override
//    public void setDidDCA(boolean valToSet) {
//        didDCA=valToSet;
//    }
//
//    @Override
//    public boolean getDidDCA() {
//        return didDCA;
//    }
//
//    @Override
//    public void setTimeToDCA(DataHolder dataHolder) {
//
//    }
//
//    @Override
//    public void setneedToDCA(DataHolder dataHolder) {
//        return false;
//    }
//
//    @Override
//    public boolean needToDCA(DataHolder dataHolder, double[] closePrices) {
//        return false;
//    }
//
//    @Override
//    public DCAInstructions getDCAInstructions() {
//        return null;
//    }
//
//    @Override
//    public void setDCAInstructions(DCAInstructions dcaInstructions) {
//
//    }
//
//    @Override
//    public DCAInstructions run(DataHolder realTimeData) {
//        return null;
//    }
//}
