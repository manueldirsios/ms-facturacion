package com.gd.facturacion.entities.response;

import lombok.Data;

@Data
public class FileResponseEnt {
 private byte[] file;
 private String nombre;
 private String type;
 private long length;
}
