package com.brqinterview.viacepbrq.utilities;

import java.util.regex.Pattern;

public class CepFormatter {
    private static final Pattern CEP_PATTERN = Pattern.compile("(\\d{5})(\\d{3})");

    public static String formatCep(String cep) {
        return cep.replaceAll(CEP_PATTERN.pattern(), "$1-$2");
    }
}
