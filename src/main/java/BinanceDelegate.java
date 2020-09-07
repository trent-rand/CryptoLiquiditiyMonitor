/**
 * Created by Trent Rand on 30/Sep/18.
 *
 * Ivory Tuna API - Binance
 * API Key: NzlusZKEFHbIPCRh5xFwunOBCEcas1CFug9xfDFuVMb21200pKf43zSjRJYAW8RU
 * API Secret: Grifip9joIzdyqv52noYVL5Lu0ZZYKjZjckXpVBDA8tCtj67YnJV6JrquvVay6Xl
 *
 * Binance ETH Address : 0x11fd895988f155eab4cfac9c9b40724e90b55ab5
 * Binance BTC Address : 1GyhgoddgV7fHeExVAoT8zaaDcb1HXmrAK
 * Binance LTC Address :
 * Binance XRP Address :
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Hex;

import org.springframework.core.annotation.Order;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.json.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class BinanceDelegate implements ExchangeDelegate {

    //pmME _2
    //Key: LieJhiBptOVOHMrNeHhaSSLwXnyiIh4jVPhifi3KiMDhU1sKAC44jPppThYVWtFG
    //Secret: YUl29ccjRvk0twHhGsq0KIXbq5MM4dGe2WDSFRXfLr4tSvChcWY8POySU4BxajWn

    //Rye
    //Key: //"NzlusZKEFHbIPCRh5xFwunOBCEcas1CFug9xfDFuVMb21200pKf43zSjRJYAW8RU";
    //Secret:   //"Grifip9joIzdyqv52noYVL5Lu0ZZYKjZjckXpVBDA8tCtj67YnJV6JrquvVay6Xl";


    private String APIKey = "LieJhiBptOVOHMrNeHhaSSLwXnyiIh4jVPhifi3KiMDhU1sKAC44jPppThYVWtFG";
    private String APISecret = "YUl29ccjRvk0twHhGsq0KIXbq5MM4dGe2WDSFRXfLr4tSvChcWY8POySU4BxajWn";


    public Orderbook getOrderBook(CurrencyPair pair) {
        Orderbook toReturn = new Orderbook();
        toReturn.pair = pair;

        String urlString = "https://www.binance.com/api/v1/depth?symbol="+pair.getPairName().toUpperCase()+"&limit=1000";

        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);

            //Setup HTTP Headers//
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);


            con.connect();

            //Parse HTTP Response Code//
            int statusCode = con.getResponseCode();
            switch (statusCode) {

                //"Handle Successful Response"//
                case 200:

                    //System.out.println("Status 200! We're good to Parse! Output incoming!");

                    //Successful Connection, attempting to parse response!//
                    InputStreamReader inputReader = new InputStreamReader(con.getInputStream());
                    BufferedReader in = new BufferedReader(inputReader);

                    String tempLine;
                    String toParse = "";
                    StringBuffer content = new StringBuffer();
                    while ((tempLine = in.readLine()) != null) {
                        toParse = tempLine;
                    }
                    try {
                        JSONObject jsonInput = new JSONObject(toParse);
                        //System.out.println("Line to parse: "+jsonInput);

                        JSONArray asksArray = jsonInput.getJSONArray("asks");
                        for(int i = 0; i < asksArray.length(); i++) {
                            JSONArray askItem = asksArray.getJSONArray(i);

                            double askPrice = askItem.getDouble(0);
                            double askVol = askItem.getDouble(1);

                            LiveOrder ask = new LiveOrder(askPrice, 0, askVol);
                            toReturn.asks.add(ask);

                            //pair.setAsk(bestAsk);
                        }

                        JSONArray bidsArray = jsonInput.getJSONArray("bids");
                        for(int t = 0; t < bidsArray.length(); t++) {
                            JSONArray bidItem = bidsArray.getJSONArray(t);

                            double bidPrice = bidItem.getDouble(0);
                            double bidVol = bidItem.getDouble(1);

                            LiveOrder bid = new LiveOrder(bidPrice, 1, bidVol);
                            toReturn.bids.add(bid);

                            //pair.setBid(bestBid);
                        }
                        //System.out.println("Pair Fetched!");
                        //System.out.println("Pair: "+pair.getPairName()+" Bid: "+toReturn.bids.get(0).getPrice()+" Ask: "+toReturn.asks.get(0).getPrice());

                    } catch(Exception e) {
                        System.out.println("Parsing Error! ... "+e);
                        //temp.source = "Error parsing input!";
                    }

                    in.close();

                    //System.out.println("Returning Complete SetupArray!");
                    toReturn.sortBooks();

                    pair.setDepth(toReturn);

                    return toReturn;

                //"Strike Colors!"//
                default:
                    System.out.println("HTTP Error: ... "+statusCode);
                    return toReturn;
            }


        } catch (Exception e) {
            System.out.print("Error setting up and executing HTTP Request. Prices are not real time!");
        }
        //toReturn.sortBooks();

        return toReturn;
    }


    public String encode(String key, String data) throws Exception {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Unable to sign message.", e);
        }
    }

}