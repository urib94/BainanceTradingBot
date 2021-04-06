package strategies;

import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;

public interface DCAStrategy  {
    public boolean needToDCA =false;
    DCAInstructions DCA_INSTRUCTIONS = null;

    public void setDidDCA(boolean valToSet);
    public boolean getDidDCA ();
    public void updateExitPrice(double qty,DataHolder realTimeData);
    public void setNeedToDCA(DataHolder dataHolder);
    public void setNeedToDCA(DataHolder dataHolder , double[] closePrices);
    public double[] getexitPrices();
    public void setNeedToDCA(boolean valToSet);

    public default boolean getNeedToDCA(){
        return needToDCA;
    }

    public DCAInstructions getDCAInstructions ();
    public void setDCAInstructions(DCAInstructions dcaInstructions);


    Instructions run(DataHolder realTimeData);
}
