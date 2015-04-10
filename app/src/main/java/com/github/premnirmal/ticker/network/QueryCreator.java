package com.github.premnirmal.ticker.network;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by premnirmal on 12/30/14.
 */
public class QueryCreator {


    public final static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static String googleStocksQuery(Object[] tickers) {
        final StringBuilder commaSeparator = new StringBuilder();
        for (Object o : tickers) {
            final String ticker = o.toString();
            if (ticker.length() < 10) { // not sure why I have this
                commaSeparator.append(ticker);
                commaSeparator.append(',');
            }
        }
        final int length = commaSeparator.length();
        if (length > 0) {
            commaSeparator.deleteCharAt(length - 1);
        }
        return commaSeparator.toString();
    }

    public static String buildStocksQuery(Object[] objects) {
        final StringBuilder commaSeparator = new StringBuilder();
        for (Object object : objects) {
            final String ticker = object.toString()
                    .replaceAll(" ", "").trim();
            if (ticker.length() < 10) { // not sure why I have this
                commaSeparator.append(ticker);
                commaSeparator.append(',');
            }
        }
        final int length = commaSeparator.length();
        if (length > 0) {
            commaSeparator.deleteCharAt(length - 1);
        }

        return "select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(\"" + commaSeparator.toString() + "\")";
    }

    public static String buildHistoricalDataQuery(String ticker, DateTime start, DateTime end) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22");
        stringBuilder.append(ticker);
        stringBuilder.append("%22%20and%20startDate%20%3D%20%22");
        stringBuilder.append(formatter.print(start));
        stringBuilder.append("%22%20and%20endDate%20%3D%20%22");
        stringBuilder.append(formatter.print(end));
        stringBuilder.append("%22");
        return stringBuilder.toString();
    }
}
