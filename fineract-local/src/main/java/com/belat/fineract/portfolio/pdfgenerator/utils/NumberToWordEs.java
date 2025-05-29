package com.belat.fineract.portfolio.pdfgenerator.utils;

public class NumberToWordEs {

    private final static String[] UNIDADES = {
            "", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve",
            "diez", "once", "doce", "trece", "catorce", "quince",
            "dieciséis", "diecisiete", "dieciocho", "diecinueve", "veinte"
    };

    private final static String[] DECENAS = {
            "", "", "veinte", "treinta", "cuarenta", "cincuenta",
            "sesenta", "setenta", "ochenta", "noventa"
    };

    private final static String[] CENTENAS = {
            "", "ciento", "doscientos", "trescientos", "cuatrocientos",
            "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"
    };

    public static String convert(long number) {
        if (number == 0) return "cero";
        if (number == 100) return "cien";
        return convertNumber(number).trim();
    }

    private static String convertNumber(long number) {
        if (number < 21) {
            return UNIDADES[(int) number];
        } else if (number < 100) {
            int unidad = (int) (number % 10);
            int decena = (int) (number / 10);
            return DECENAS[decena] + (unidad > 0 ? " y " + UNIDADES[unidad] : "");
        } else if (number < 1000) {
            int centena = (int) (number / 100);
            return CENTENAS[centena] + " " + convertNumber(number % 100);
        } else if (number < 1_000_000) {
            int miles = (int) (number / 1000);
            long resto = number % 1000;
            return (miles == 1 ? "mil" : convertNumber(miles) + " mil") + " " + convertNumber(resto);
        } else if (number < 1_000_000_000) {
            long millones = number / 1_000_000;
            long resto = number % 1_000_000;
            return (millones == 1 ? "un millón" : convertNumber(millones) + " millones") + " " + convertNumber(resto);
        }
        return String.valueOf(number);
    }

}
