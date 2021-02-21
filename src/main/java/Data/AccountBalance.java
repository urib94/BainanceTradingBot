package Data;

import com.binance.client.api.RequestOptions;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.trade.AccountInformation;
import com.binance.client.api.model.trade.Asset;
import com.binance.client.api.model.trade.Position;
import com.binance.client.api.model.user.AccountUpdate;
import com.binance.client.api.model.user.BalanceUpdate;
import com.binance.client.api.model.user.PositionUpdate;
import com.binance.client.api.model.user.UserDataUpdateEvent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AccountBalance {
    private HashMap<String, Asset> assets;
    private HashMap<String, Position> positions;
    private final ReadWriteLock assetsLock = new ReentrantReadWriteLock();
    private final ReadWriteLock positionsLock = new ReentrantReadWriteLock();

    private static class AccountBalanceHolder{
        private static AccountBalance accountBalance = new AccountBalance();
    }

    private AccountBalance(){
        assets = new HashMap<>();
        positions = new HashMap<>();
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        AccountInformation accountInformation = syncRequestClient.getAccountInformation();
        for (Position position: accountInformation.getPositions()) System.out.println(position);

        for (Position position: accountInformation.getPositions())positions.put(position.getSymbol(), position);
        for (Asset asset: accountInformation.getAssets())assets.put(asset.getAsset(), asset);
    }
    public static AccountBalance getAccountBalance() {
        return AccountBalanceHolder.accountBalance;
    }

    public BigDecimal getCoinBalance(String symbol) {
        assetsLock.readLock().lock();
        try {
            if (assets.containsKey(symbol)) return assets.get(symbol).getWalletBalance();
            return null;
        }finally {
            assetsLock.readLock().unlock();
        }
    }

    public Position getPosition(String symbol) {
        positionsLock.readLock().lock();
        try {
            if (positions.containsKey(symbol)) return positions.get(symbol);
            return null;
        }finally {
            positionsLock.readLock().unlock();
        }
    }

    public void updateBalance(UserDataUpdateEvent event) {
        AccountUpdate accountUpdate = event.getAccountUpdate();
        List<BalanceUpdate> balances = accountUpdate.getBalances();
        assetsLock.writeLock().lock();
        for (BalanceUpdate balanceUpdate : balances){
            if (assets.containsKey(balanceUpdate.getAsset())) assets.get(balanceUpdate.getAsset()).setWalletBalance(balanceUpdate.getWalletBalance());
            else{
                Asset asset = new Asset();
                asset.setAsset(balanceUpdate.getAsset());
                asset.setWalletBalance(balanceUpdate.getWalletBalance());
                assets.put(balanceUpdate.getAsset(), asset);
            }
        }
        assetsLock.writeLock().unlock();
        List<PositionUpdate> positionUpdates = accountUpdate.getPositions();
        positionsLock.writeLock().lock();
        for (PositionUpdate positionUpdate : positionUpdates){
            positions.get(positionUpdate.getSymbol()).setPositionAmt(positionUpdate.getAmount()); //update assets
            positions.get(positionUpdate.getSymbol()).setUnrealizedProfit(positionUpdate.getUnrealizedPnl()); //update assets
        }
        positionsLock.writeLock().unlock();
    }
}

//TODO: Think about the possibility where a new asset occurs
