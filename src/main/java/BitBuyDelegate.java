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

import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitBuyDelegate implements ExchangeDelegate {

    final String CHAR_SET = "UTF-8";
    final String HMAC_ALGORITHM = "HmacSHA256";
    private final String APIKey = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1MDI0NiIsImF1dGgiOiJST0xFX1BBUlRORVIiLCJleHAiOjE1OTU3ODg4OTl9.aO2tB-H1bw4fnbucvjcAA4ToJoImUwoBdEwCfMfOoEYun5PqtPvC0gyzkDqq4I7928gFYGX4M_jJI5gfJovE0A";
    private final String APISecret = "CmjoUhKdkIwEcAuQCmgdByqAM6v0J73d9XFNbJlFgyF0TILBcRZ4ZXMcY3kTU52R0EQRhe013KpLURwnIprJv8hgwfPV7DE5pAaqD6rX9suA8KXTqjE0lGkTXnJmZ5y5W0rfdb1UsG1LTzW9LvkJLH7GMavGJykkkzUfQfFnkCZCE06JHADLJ2U0jF8VzN8tXt7YAZJI6FWlLkEmXvSAQw601vpPKh7kv3SMFzwdW6PDOZNGXYKziDBE6eXrf05H";


    public Orderbook getOrderBook(CurrencyPair pair) {
        Orderbook toReturn = new Orderbook();
        toReturn.pair = pair;

        String url = "https://partner.bcm.exchange/api/v1/markets/" + pair.getPairName().toUpperCase() + "/order-book";
        Long stamp = System.currentTimeMillis();
        HttpHeaders headers = new HttpHeaders();
        JSONObject body = null;

        headers.setContentType(MediaType.APPLICATION_JSON);
        UriComponents builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("apikey", APIKey)
                .queryParam("stamp", stamp).build();

        try {
            JSONObject json = new JSONObject();
            json.put("path", builder.getPath());
            json.put("query", builder.getQuery());
            json.put("content-length", body == null ? -1 : body.length());
            String sigContent = json.toString();
            String signature;

            SecretKeySpec key;
            try {
                key = new SecretKeySpec(APISecret.getBytes(CHAR_SET), HMAC_ALGORITHM);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
                return null;
            }

            byte[] data = sigContent.getBytes(CHAR_SET);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(key);
            signature = javax.xml.bind.DatatypeConverter.printBase64Binary(mac.doFinal(data));

            headers.set("signature", signature);
            System.out.println(builder.toUriString());
            System.out.println(headers);
            System.out.println(body);
            //System.out.println(new RestTemplate().exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>("{}", headers), String.class).getBody());

            String toParse = new RestTemplate().exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<>("{}", headers), String.class).getBody();
            System.out.println(toParse);


            try {
                JSONObject jsonInput = new JSONObject(toParse);
                //System.out.println("Line to parse: "+jsonInput);

                JSONArray asksArray = jsonInput.getJSONArray("sellOrders");
                for (int i = 0; i < asksArray.length(); i++) {
                    JSONObject askItem = asksArray.getJSONObject(i);

                    double askPrice = askItem.getDouble("pricePerUnit");
                    double askVol = askItem.getDouble("quantity");

                    LiveOrder ask = new LiveOrder(askPrice, 0, askVol);
                    toReturn.asks.add(ask);

                    //pair.setAsk(bestAsk);
                }

                JSONArray bidsArray = jsonInput.getJSONArray("buyOrders");
                for (int t = 0; t < bidsArray.length(); t++) {
                    JSONObject bidItem = bidsArray.getJSONObject(t);

                    double bidPrice = bidItem.getDouble("pricePerUnit");
                    double bidVol = bidItem.getDouble("quantity");

                    LiveOrder bid = new LiveOrder(bidPrice, 1, bidVol);
                    toReturn.bids.add(bid);

                    //pair.setBid(bestBid);
                }
                System.out.println("Pair Fetched!");
                System.out.println("Pair: " + pair.getPairName() + " Bid: " + toReturn.bids.get(0).getPrice() + " Ask: " + toReturn.asks.get(0).getPrice());
            } catch (Exception e) {
                System.out.println("Error Parsing BitBuy Orderbook! " + e);
            }


        } catch (Exception e) {
            System.out.println(e);
        }

        return toReturn;
    }
    /* REST API Implementation : END */
}


