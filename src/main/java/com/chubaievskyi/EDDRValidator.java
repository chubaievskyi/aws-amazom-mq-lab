package com.chubaievskyi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EDDRValidator {
    public static boolean validateEDDRNumber(String eddrNumber) {

        String regex = "^\\d{8}-?\\d{4}$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(eddrNumber);

        if (!matcher.matches()) {
            return false;
        }

        eddrNumber = eddrNumber.replace("-", "");

        int sum = 0;
        for (int i = 0; i < eddrNumber.length() - 1; i++) {
            sum += eddrNumber.charAt(i) * ((i % 3 == 0) ? 7 : (i % 3 == 1) ? 3 : 1);
        }

        int calculatedControlDigit = sum % 10;
        int controlDigit = Integer.parseInt(eddrNumber.substring(12, 13));

        return calculatedControlDigit == controlDigit;
    }

    public static void main(String[] args) {

        System.out.println(validateEDDRNumber("19911120-07605"));
    }
}
