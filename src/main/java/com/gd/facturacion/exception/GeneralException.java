package com.gd.facturacion.exception;

import lombok.Data;

@SuppressWarnings("serial")
@Data
public class GeneralException extends Exception {
     private final int codigo;
    public GeneralException(int codigo,String message) {
         super(message);
         this.codigo = codigo;
     }
}
