package sk.leo;

public enum Endpoints {
    // ===== Equity Orders =====
    GET_ALL_ORDERS,
    PLACE_MARKET_ORDER,
    PLACE_LIMIT_ORDER,
    PLACE_STOP_ORDER,
    PLACE_STOP_LIMIT_ORDER,
    GET_ORDER_BY_ID,
    CANCEL_ORDER_BY_ID,

    // ===== Positions =====
    GET_POSITIONS,

    // ===== Account =====
    GET_ACCOUNT_CASH,
    GET_ACCOUNT_SUMMARY,

    // ===== Metadata =====
    GET_INSTRUMENTS,
    GET_EXCHANGES,

    // ===== Historical data (Equity History) =====
    GET_HISTORY_DIVIDENDS,
    GET_HISTORY_EXPORTS,
    POST_HISTORY_EXPORTS,
    GET_HISTORY_ORDERS,
    GET_HISTORY_TRANSACTIONS
}
