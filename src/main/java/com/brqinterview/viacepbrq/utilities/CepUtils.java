package com.brqinterview.viacepbrq.utilities;

public class CepUtils {

    private CepUtils(){
        throw new IllegalStateException("Utility class");
    }

    public static String formatCep(String cep) {
        if (cep == null || cep.isEmpty()) {
            return cep;
        }
        cep = cep.replaceAll("\\D", "");

        if (cep.length() != 8) {
            throw new IllegalArgumentException("Invalid CEP. The CEP must contain exactly 8 digits.");
        }

        return cep.substring(0, 5) + "-" + cep.substring(5);
    }
}
