import java.util.ArrayList;

public class Monitor {

    public BinanceDelegate binanceDelegate = new BinanceDelegate();
    public HitBTCDelegate hitbtcDelegate = new HitBTCDelegate();
    public BitBuyDelegate bitbuyDelegate = new BitBuyDelegate();
    public KrakenDelegate krakenDelegate = new KrakenDelegate();

    public String[] CurrencySymbols = {"btc", "eth", "bch", "xrp", "ltc"};
    public ArrayList<Currency> Currencies = new ArrayList<>();

    public APIOutput apiOutput = new APIOutput();

    private int fileID = 0;

    private static Monitor self = new Monitor();
    public static Monitor getSelf() {
        return self;
    }
    private Monitor() {
        binanceDelegate = new BinanceDelegate();
        hitbtcDelegate = new HitBTCDelegate();
        bitbuyDelegate = new BitBuyDelegate();
        krakenDelegate = new KrakenDelegate();


    }




    public static void main(String[] args) {
        for(int i = 0; i < self.CurrencySymbols.length; i++) {
            Currency tempCurrency = new Currency(self.CurrencySymbols[i]);
            self.Currencies.add(tempCurrency);
        }


    }




}
