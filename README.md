# Cryptocurrency Trading Bot

## Overview
The first Machine-Dance traiding bot!

This is a bot written in Java that buys and sells cryptocurrency using Binance's API, while also displays user information.
This project incorporate multiple threads, use of WebSocket and Rest APIs and object-oriented programming.
It required research on cryptocurrency strategies and their employment in Java with Binance's API.

## Strategies
The bot acquires the 500 most recent candles from Binance based on the symbol typed in by the user, and then runs on one or more from the following strategies.
Metholodgy - each entry strategy corresponds to its similary-named exit strategies. For example, RSI entry strategy will enter into position that listens on the four possible RSI exit strategies. 

### Entry Strategies
1. [Relative Strength Index (RSI)](https://www.investopedia.com/terms/r/rsi.asp) with 9 candles.
* To enter position the RSI needs to cross 27,30 and 35 fast (last 2 close candles).
3. [Long MACD Over RSI](https://www.investopedia.com/terms/m/macd.asp) with 9 rsi values, fast bar: 15, slow bar: 24.
* If the price is above SMA 150, we need that the signal line is below 0 and we have a 3 negative candle upwards pyramid, or we cross 0 upwards.
5. [Short MACD Over RSI](https://www.investopedia.com/terms/m/macd.asp)
6. * If the price is below SMA 150, we need that the signal line is below 0 and we have a 3-candle down pyramid, or we cross 0 downwards.

### Exit Strategies
1. Four possible RSI Exit strategies.
* Cross 65 and until 73 up -> cross 60 down (selling 50% of position) -> cross 50 down (selling all position).
* Cross 73 up -> cross 70 down (selling 40% of position) -> cross 60 down (selling all position).
* The RSI loses a value of at least 15 in the last 2 candles (including the open one) - selling all position
* Crossing 30 down (selling all positio).
3. Five possible Long MACD Over RSI strategies.
* Current price below SMA 150 -> Market
* Crossing the zero line downwards -> Limit
* Not currently trailing -> If we have three positive candles downwards pyramid, activate trailing. Currently trailing -> two positive candles pyramid, deactivate trailing. Currently trailing -> If we need to sell by our trailing rules -> sell. 
* Not currently trailing -> If we have three negative downwards pyramid, activate trailing. Not currenly trailing -> three negative candle values and the current candle is not bigger than the prev, deactivate trailing. Not Currently trailing -> If we need to sell by trailing rules -> sell.
* Safety net.
5. Five possible Short MACD Over RSI strategies.
* Current price above SMA 150 -> Market
* Crosing the zero line upwards -> Limit
* Not currently trailing -> If we have three negative downwards pyramid, activate trailing. Currently trailing ->  if the current candle is bigger than the previous and are both negative, deactivate trailing. Not currently trailing -> If wee need to sell by our trailing rules -> sell.
* Not currently trailing -> If we have three positive upwards pyramid, activate trailing. Currently trailing -> if the current candle is not bigger than the previous one and are positive, deactivate trailing. Not currently trailing -> If we need to sell by our own trailing rules -> sell.
* Saftety net.

## Project Structure
We have 6 packages in our project:
1. codeExecution - 
2. data
3. positions
4. singleton Helpers
5. strategies
6. utils

and one Main.java file that...

## How to run
In order to run the bot, the user needs to press "help" to view all the possible commands the bots offers.


## Creators

