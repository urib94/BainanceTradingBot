package main.java.utils;

import com.binance.client.model.enums.CandlestickInterval;

public class IntervalMaker {

        private String code;

        public static CandlestickInterval makeCandlestickInterval(String code) {
        return CandlestickInterval.valueOf(code);
        }

        public String toString() {
            return this.code;
        }

    }

