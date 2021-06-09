package data;

public class Config {

	public static final double DOUBLE_ZERO = 0.0;
	public static final String BASE_COIN = "usdt";
	public static final double DOUBLE_ONE = 1.0;
    public static final double ONE_HUNDRED = 100.0;
    public static final String BASE_SYMBOL = "bnbusdt";
    public static String API_KEY = "h4AdqbWrKlF5wOxWmbNTKLq7IpDReZOvVXeOujGwUfU0HjXdIejUmqKvoVLbcXJ6";
	public static String SECRET_KEY = "7qqNLwB7ZXiZu2nBJhrdv7ARVpNDhImNA1Rj1oXR6eGjZFkGsRC9ayEO7HLYVm6w";
	public static final int THREAD_NUM = 6;
	public static final String SYMBOL = "ethusdt";
	public static final int CANDLE_NUM = 1499;
	public static final String NEW = "NEW";
	public static final String PARTIALLY_FILLED = "PARTIALLY_FILLED";
	public static final String FILLED = "FILLED";
	public static final String CANCELED = "CANCELED";
	public static final String EXPIRED = "EXPIRED";
	public static final int ZERO = 0;
	public static final String REDUCE_ONLY = "true";


	public static void setApiKey(String apiKey) {
		API_KEY = apiKey;
	}

	public static void setSecretKey(String secretKey) {
		SECRET_KEY = secretKey;
	}

}