package Data;
import Strategies.ExitStrategy;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PositionEntry {
    private BigDecimal balance;
    private String asset;
    private ArrayList<ExitStrategy> exitStrategies;
}
