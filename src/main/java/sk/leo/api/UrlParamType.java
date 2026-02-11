package sk.leo.api;

public enum UrlParamType {
    API_KEY {
        @Override
        public String getAsString(String argument) {
            return "apikey=" + argument;
        }
    },
    SYMBOL {
        @Override
        public String getAsString(String argument) {
            return "symbol=" + argument;
        }
    },
    TIME_INTERVAL {
        @Override
        public String getAsString(String argument) {
            return "interval=" + argument;
        }
    },
    OUTPUT_SIZE {
        @Override
        public String getAsString(String argument) {
            return "outputsize=" + argument;
        }
    },
    ISIN{
        @Override
        public String getAsString(String argument) {
            return "isin=" + argument;
        }
    };

    public abstract String getAsString(String argument);
}
