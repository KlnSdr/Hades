package hades.common;

public class Util {
    public static String prependZero(int number) {
        if (number < 10) {
            return "0" + number;
        }

        return "" + number;
    }
}
