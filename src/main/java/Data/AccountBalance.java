package Data;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.trade.AccountInformation;
import com.binance.client.model.trade.Position;
import com.binance.client.model.user.BalanceUpdate;
import com.binance.client.model.user.PositionUpdate;
import com.binance.client.model.user.UserDataUpdateEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AccountBalance {
    private BigDecimal freeBalance;
    private List<BalanceUpdate> totalBalance;
    private List<Position> positions;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static class AccountBalanceHolder{
        private static AccountBalance accountBalance = new AccountBalance();
    }

    private AccountBalance(){
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, options);
        AccountInformation accountInformation = syncRequestClient.getAccountInformation();
        positions = accountInformation.getPositions();
    }
    public static AccountBalance getAccountBalance() {
        return AccountBalanceHolder.accountBalance;
    }

    public BigDecimal getFreeBalance() {
        lock.readLock().lock();
        try {
            return freeBalance;
        }finally {
            lock.readLock().unlock();
        }
    }

    public BigDecimal getCoinBalance(String symbol) {
        lock.readLock().lock();
        try {
            for (BalanceUpdate balanceUpdate:totalBalance) if (balanceUpdate.getAsset().equals(symbol)) return balanceUpdate.getWalletBalance();
            return null;
        }finally {
            lock.readLock().unlock();
        }
    }

    public Position getPosition(String symbol) {
        lock.readLock().lock();
        try {
            for (Position position:positions) if (position.getSymbol().equals(symbol)) return position;
            return null;
        }finally {
            lock.readLock().unlock();
        }
    }

    public void updateBalance(UserDataUpdateEvent event, String baseCurrency) {
        System.out.println("balance");
        lock.writeLock().lock();
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, options);
        AccountInformation accountInformation = syncRequestClient.getAccountInformation();
        positions = accountInformation.getPositions();
        totalBalance = event.getAccountUpdate().getBalances();
        for (BalanceUpdate balanceUpdate:totalBalance) if (balanceUpdate.getAsset().equals(baseCurrency)) freeBalance = balanceUpdate.getWalletBalance();
        lock.writeLock().unlock();
    }
}
