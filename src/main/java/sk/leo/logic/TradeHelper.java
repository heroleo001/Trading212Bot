package sk.leo.logic;

import sk.leo.api.records.Instrument;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

public class TradeHelper {
    private TradeHelper(){}

    public static boolean isValidBaseStock(Instrument i) {
        if (!"STOCK".equals(i.type()))
            return false;

        if (i.isin() == null || i.isin().length() < 2)
            return false;

        String country = i.isin().substring(0, 2);
        if (!Set.of("US", "GB", "DE", "FR", "IE", "NL").contains(country))
            return false;

        if (!Set.of("USD", "EUR", "GBX").contains(i.currencyCode()))
            return false;

        if (i.maxOpenQuantity() < 1000000) // Ideal 250 000 or less
            return false;

        return true;
    }

    public static double round2(double v) {
        return BigDecimal.valueOf(v)
                .setScale(2, RoundingMode.DOWN)
                .doubleValue();
    }
}
