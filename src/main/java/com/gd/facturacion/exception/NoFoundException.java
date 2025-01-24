package com.gd.facturacion.exception;

import lombok.Data;

@SuppressWarnings("serial")
@Data
public class NoFoundException extends Exception {
     private final int codigo;
    public NoFoundException(int codigo,String message) {
         super(message);
         this.codigo = codigo;
     }
}
