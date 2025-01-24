package com.gd.facturacion.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.gd.facturacion.entities.Factura;
import com.gd.facturacion.entities.response.FileResponseEnt;
import com.gd.facturacion.entities.response.GenericResponse;
import com.gd.facturacion.exception.GeneralException;
import com.gd.facturacion.exception.NoFoundException;
import com.gd.facturacion.service.FacturaService;
@RestController
@RequestMapping("/facturas")
@CrossOrigin(origins = "*")
@EnableWebMvc
public class FacturaController {
    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping("/generar/{idEmpresa}/{idPago}")
    public ResponseEntity<GenericResponse> generarFactura(@PathVariable("idEmpresa") Long idEmpresa,@PathVariable("idPago") Long idPago) throws GeneralException, NoFoundException  {
    	Factura factura= facturaService.generarFactura(idEmpresa,idPago);
       return generarPdf(factura.getId());
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<GenericResponse>  generarPdf(@PathVariable("id") Long id) throws NoFoundException {
    	GenericResponse response =new GenericResponse();
    	FileResponseEnt facturaFile=new FileResponseEnt();
    	HttpHeaders headers = new HttpHeaders();
    	Factura factura = facturaService.facturaRepository.findById(id).orElseThrow(() -> new NoFoundException(404,"Factura no encontrada"));
    	byte[]  arrayFile=facturaService.generarPdf(factura);
    	facturaFile.setFile(arrayFile);
    	facturaFile.setLength(arrayFile.length);
    	facturaFile.setNombre("Factura.pdf");
    	facturaFile.setType("application/pdf");
    	headers.add("Content-Disposition", "attachment;filename=" + "Factura.pdf");
    	response.setTransaccion(facturaFile);
    	
		return ResponseEntity.ok().headers(headers).body(response);

    }

    @GetMapping("/lista")
    public ResponseEntity<GenericResponse>  listarFacturas() {
        return ResponseEntity.ok().body(facturaService.listarFacturas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse> obtenerFactura(@PathVariable Long id) throws NoFoundException {
        return ResponseEntity.ok().body(facturaService.obtenerFactura(id));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse> deleteFactura(@PathVariable Long id)  {
    
        return ResponseEntity.ok(facturaService.elminarFactura(id));
    }
}