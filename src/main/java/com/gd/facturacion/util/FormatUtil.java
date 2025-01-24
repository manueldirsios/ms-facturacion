package com.gd.facturacion.util;

import java.text.DecimalFormat;

public class FormatUtil {
	private FormatUtil(){}
    private static final String[] UNIDADES = {"", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"};
    private static final String[] DECENAS = {"", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
    private static final String[] DECENAS_ESPECIALES = {"diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve"};
    private static final String[] CENTENAS = {"", "cien", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"};

    public static String convertir(double monto) {
        if (monto == 0) {
            return "cero pesos 00/100 M.N.";
        }

        String montoEnTexto = "";

        // Formatear el monto a dos decimales
        DecimalFormat formatter = new DecimalFormat("#.00");
        String montoFormateado = formatter.format(monto);
        String[] partes = montoFormateado.split("\\.");
        int parteEntera = Integer.parseInt(partes[0]);
        int parteDecimal = Integer.parseInt(partes[1]);

        // Convertir la parte entera
        montoEnTexto += convertirNumero(parteEntera) + " pesos";

        // Agregar la parte decimal
        montoEnTexto += " " + String.format("%02d", parteDecimal) + "/100 M.N.";

        return montoEnTexto;
    }
    private static String convertirNumero(int numero) {
        if (numero == 0) {
            return "";
        } else if (numero < 10) {
            return convertirUnidades(numero);
        } else if (numero < 20) {
            return convertirDecenasEspeciales(numero);
        } else if (numero < 100) {
            return convertirDecenas(numero);
        } else if (numero < 1000) {
            return convertirCentenas(numero);
        } else if (numero < 1000000) {
            return convertirMiles(numero);
        } else if (numero < 1000000000) {
            return convertirMillones(numero);
        }
        return "";
    }

    private static String convertirUnidades(int numero) {
        return UNIDADES[numero];
    }

    private static String convertirDecenasEspeciales(int numero) {
        return DECENAS_ESPECIALES[numero - 10];
    }

    private static String convertirDecenas(int numero) {
        int unidad = numero % 10;
        int decena = numero / 10;
        return DECENAS[decena] + (unidad > 0 ? " y " + UNIDADES[unidad] : "");
    }

    private static String convertirCentenas(int numero) {
        int centena = numero / 100;
        int resto = numero % 100;
        if (numero == 100) {
            return "cien";
        }
        return CENTENAS[centena] + (resto > 0 ? " " + convertirNumero(resto) : "");
    }

    private static String convertirMiles(int numero) {
        int miles = numero / 1000;
        int resto = numero % 1000;
        if (miles == 1) {
            return "mil" + (resto > 0 ? " " + convertirNumero(resto) : "");
        } else {
            return convertirNumero(miles) + " mil" + (resto > 0 ? " " + convertirNumero(resto) : "");
        }
    }

    private static String convertirMillones(int numero) {
        int millones = numero / 1000000;
        int resto = numero % 1000000;
        if (millones == 1) {
            return "un millón" + (resto > 0 ? " " + convertirNumero(resto) : "");
        } else {
            return convertirNumero(millones) + " millones" + (resto > 0 ? " " + convertirNumero(resto) : "");
        }
    }

}
