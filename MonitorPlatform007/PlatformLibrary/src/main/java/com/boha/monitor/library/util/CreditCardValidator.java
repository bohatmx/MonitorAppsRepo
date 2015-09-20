package com.boha.monitor.library.util;

/**
 * Created by aubreyM on 15/09/15.
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class CreditCardValidator {

    public CreditCardValidator() {
    }

    private String message = null;

    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    public static final int MASTERCARD = 0, VISA = 1;
    public static final int AMEX = 2, DISCOVER = 3, DINERS = 4;

    private static final String[] messages = {
            "Not a valid number for MasterCard.",
            "Not a valid number for Visa.",
            "Not a valid number for American Express.",
            "Not a valid number for Discover.",
            "Not a valid number for Diner’s Club"};

    public boolean isCreditCardValid(String number, int type) {

        if (number.equals("")) {
            setMessage("Field cannnot be blank.");
            return false;
        }

        Matcher m = Pattern.compile("[^\\d\\s.-]").matcher(number);

        if (m.find()) {
            setMessage("Credit card number can only contain numbers, spaces, \"-\", and \".\"");
            return false;
        }

        setMessage(messages[type]);
        Matcher matcher = Pattern.compile("[\\s.-]").matcher(number);
        number = matcher.replaceAll("");

        return validate(number, type);

    }

// Check that cards start with proper digits for
// selected card type and are also the right length.

    private boolean validate(String number, int type) {

        if (null == number || number.length() < 12)
            return false;

        switch (type) {

            case MASTERCARD:
                if (number.length() != 16
                        || Integer.parseInt(number.substring(0, 2)) < 51
                        || Integer.parseInt(number.substring(0, 2)) > 55) {
                    return false;
                }
                break;

            case VISA:
                if ((number.length() != 13 && number.length() != 16)
                        || Integer.parseInt(number.substring(0, 1)) != 4) {
                    return false;
                }
                break;

            case AMEX:
                if (number.length() != 15
                        || (Integer.parseInt(number.substring(0, 2)) != 34 && Integer
                        .parseInt(number.substring(0, 2)) != 37)) {
                    return false;
                }
                break;

            case DISCOVER:
                if (number.length() != 16
                        || Integer.parseInt(number.substring(0, 4)) != 6011) {
                    return false;
                }
                break;

            case DINERS:
                if (number.length() != 14
                        || ((Integer.parseInt(number.substring(0, 2)) != 36 && Integer
                        .parseInt(number.substring(0, 2)) != 38)
                        && Integer.parseInt(number.substring(0, 3)) < 300 || Integer
                        .parseInt(number.substring(0, 3)) > 305)) {
                    return false;
                }
                break;
        }

        if (type == DISCOVER) { // no luhn validate for DISCOVER
            return true;
        }

        return luhnValidate(number);
    }

// The Luhn algorithm is basically a CRC type
// system for checking the validity of an entry.
// All major credit cards use numbers that will
// pass the Luhn check. Also, all of them are based
// on MOD 10.

    private boolean luhnValidate(String numberString) {

        char[] charArray = numberString.toCharArray();
        int[] number = new int[charArray.length];
        int total = 0;

        for (int i = 0; i < charArray.length; i++) {
            number[i] = Character.getNumericValue(charArray[i]);
        }

        for (int i = number.length - 2; i > -1; i -= 2) {
            number[i] *= 2;

            if (number[i] > 9)
                number[i] -= 9;
        }

        for (int i = 0; i < number.length; i++)
            total += number[i];

        if (total % 10 != 0)
            return false;

        return true;
    }

}