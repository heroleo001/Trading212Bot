package sk.leo.logic;

import sk.leo.api.records.Instrument;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

public class TradeHelper {
    private TradeHelper(){}

    private static final Set<String> validForeignCurrencies = Set.of("GBX", "USD", "GBP");

    public static boolean isValidBaseStock(Instrument i) {
        if (!"STOCK".equals(i.type()))
            return false;

        if (i.isin() == null || i.isin().length() < 2)
            return false;

        String country = i.isin().substring(0, 2);
        if (!Set.of("US", "GB", "DE", "FR", "IE", "NL").contains(country))
            return false;

        if (!isAValidCurrency(i.currencyCode()))
            return false;

        String name = i.name().toLowerCase();
        if (name.contains("otc")
                || name.contains("pink")
                || name.contains("test")
                || name.contains("rights")
                || name.contains("warrant")
                || name.contains("unit"))
            return false;

        if (!i.extendedHours())
            return false;

        // Ideal 250 000 or less
        return (i.maxOpenQuantity() > 250_0000);
    }

    public static double round2(double v) {
        if (Double.isNaN(v))
            return 0;
        return BigDecimal.valueOf(v)
                .setScale(2, RoundingMode.DOWN)
                .doubleValue();
    }

    public static boolean isAValidCurrency(String currency){
        return validForeignCurrencies.contains(currency)
                || currency.equals("EUR");
    }
    public static Set<String> getValidForeignCurrencies(){
        return Set.copyOf(validForeignCurrencies);
    }
}
