package com.chubaievskyi;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator implements ConstraintValidator<MyValidation, User> {

    public static final String REGEX = "^\\d{8}-?\\d{5}$";

    public static final int COEFFICIENT_1 = 7;
    public static final int COEFFICIENT_2 = 3;
    public static final int COEFFICIENT_3 = 1;

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        if (user == null) {
            return false;
        }

        if (constraintValidatorContext == null) {
            return false;
        }

        boolean isValidEddr = validateEDDRNumber(user.getEddr());
        boolean isValidName = validateName(user.getName());
        boolean isValidCount = validateCount(user.getCount());

        return isValidEddr && isValidName && isValidCount;
    }

    public static boolean validateEDDRNumber(String eddrNumber) {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(eddrNumber);

        if (!matcher.matches()) {
            return false;
        }

        eddrNumber = eddrNumber.replace("-", "");

        int sum = 0;
        int count = 0;
        for (int i = 0; i < eddrNumber.length() - 1; i++) {
            int temp = Integer.parseInt(eddrNumber.substring(i, i + 1));
            if (count == 0) {
                sum += temp * COEFFICIENT_1;
            } else if (count == 1){
                sum += temp * COEFFICIENT_2;

            } else if (count == 2) {
                sum += temp * COEFFICIENT_3;
            }
            count++;
            if (count == 3) {
                count = 0;
            }
        }

        int calculatedControlDigit = sum % 10;
        int controlDigit = Integer.parseInt(eddrNumber.substring(12, 13));

        return calculatedControlDigit == controlDigit;
    }

    public static boolean validateName(String name) {

        if (name.length() >= 7) {
            for (char c : name.toCharArray()) {
                if (c == 'a') {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean validateCount(int count) {
        return count >= 10;
    }
}
