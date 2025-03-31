package hades.common;

import dobby.io.request.Request;
import dobby.util.logging.Logger;

public class Util {
    private static final Logger LOGGER = new Logger(Util.class);

    public static String prependZero(int number) {
        if (number < 10) {
            return "0" + number;
        }

        return "" + number;
    }

    public static boolean requestHasHeader(Request request, String headerName) {
        return request.getHeaderKeys().contains(headerName.toLowerCase());
    }
}
