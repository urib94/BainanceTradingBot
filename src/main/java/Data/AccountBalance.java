package Data;

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
    //private List<PositionUpdate> positions;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private static class AccountBalanceHolder{
        private static AccountBalance accountBalance = new AccountBalance();
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
/*
    public positionU getPosition(String symbol) {
        lock.readLock().lock();
        try {
            for (PositionUpdate position:positions) if (position.getSymbol().equals(symbol)) return position;
            return null;
        }finally {
            lock.readLock().unlock();
        }
    }*/

    public void updateBalance(UserDataUpdateEvent event, String baseCurrency) {
        System.out.println("balance");
        lock.writeLock().lock();
        totalBalance = event.getAccountUpdate().getBalances();
        //positions = event.getAccountUpdate().getPositions();
        for (BalanceUpdate balanceUpdate:totalBalance) if (balanceUpdate.getAsset().equals(baseCurrency)) freeBalance = balanceUpdate.getWalletBalance();
        lock.writeLock().unlock();
    }
}
