package com.gd.facturacion.entities.response;

import java.util.List;

import lombok.Data;

@Data
public class PagoResponseEnt {
	private long id;
	private String fechaPago;
	private String fechaModificacion;
	private String estatusPago;
	private double importeTotal;
	private String referenciaPago;
	private List<LineaPago> lineasPago;
	private int idFactura;
	private String descripcionPago;

	@Data
	public static class LineaPago {
		private long id;
		private long pagoId;
		private long productoId;
		private Producto producto;
		private int cantidad;
		private double subtotal;
		public LineaPago() {
			super();
		}
	}

	@Data
	public  static class  Producto {
		private int id;
		private String nombre;
		private String descripcion;
		private double precio;
		private String imagen;
		private String sku;
		private int descuento;
		private String categoria;
		private int stock;
	    private Double precioFinal;
	    private boolean promocion;
		public Producto() {
			super();
		}
	}

	public PagoResponseEnt() {
		super();
	}
	
	
}
