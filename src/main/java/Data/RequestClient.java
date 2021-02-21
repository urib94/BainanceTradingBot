package Data;

import com.binance.client.api.RequestOptions;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.trade.AccountInformation;
import com.binance.client.api.model.trade.Asset;
import com.binance.client.api.model.trade.Position;

import java.util.HashMap;

public class RequestClient {
	private  SyncRequestClient syncRequestClient;

	private static class RequestClientHolder{
		private static RequestClient RequestClient = new RequestClient();
	}
	private RequestClient(){
		RequestOptions options = new RequestOptions();
		syncRequestClient = SyncRequestClient.create(Config.API_KEY, Config.SECRET_KEY, options);
	}
	public static RequestClient getRequestClient() {
		return RequestClientHolder.RequestClient;
	}
	public SyncRequestClient getSyncRequestClient() {
		return syncRequestClient;
	}

}
