

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


public class KrakenDelegate implements ExchangeDelegate {

    //private String APIKey = "LieJhiBptOVOHMrNeHhaSSLwXnyiIh4jVPhifi3KiMDhU1sKAC44jPppThYVWtFG";
    //private String APISecret = "YUl29ccjRvk0twHhGsq0KIXbq5MM4dGe2WDSFRXfLr4tSvChcWY8POySU4BxajWn";


    public Orderbook getOrderBook(CurrencyPair pair) {
        Orderbook toReturn = new Orderbook();
        toReturn.pair = pair;

        String urlString = "https://api.kraken.com/0/public/Depth?pair=" + pair.getPairName().toUpperCase();

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

                        JSONArray asksArray = jsonInput.getJSONArray("ask");
                        for(int i = 0; i < asksArray.length(); i++) {
                            JSONArray askItem = asksArray.getJSONArray(i);

                            double askPrice = askItem.getDouble(0);
                            double askVol = askItem.getDouble(1);

                            LiveOrder ask = new LiveOrder(askPrice, 0, askVol);
                            toReturn.asks.add(ask);

                            //pair.setAsk(bestAsk);
                        }

                        JSONArray bidsArray = jsonInput.getJSONArray("bid");
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
    /*
     * End of Orderbook Fetch REST Request.
     *
     * Start of Websocket Handler
     */

}
