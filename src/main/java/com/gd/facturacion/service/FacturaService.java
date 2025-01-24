package com.gd.facturacion.service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gd.facturacion.entities.Concepto;
import com.gd.facturacion.entities.Emisor;
import com.gd.facturacion.entities.Factura;
import com.gd.facturacion.entities.Receptor;
import com.gd.facturacion.entities.response.GenericResponse;
import com.gd.facturacion.entities.response.PagoResponseEnt;
import com.gd.facturacion.entities.response.PagoResponseEnt.LineaPago;
import com.gd.facturacion.exception.GeneralException;
import com.gd.facturacion.exception.NoFoundException;
import com.gd.facturacion.repository.EmisorRepository;
import com.gd.facturacion.repository.FacturaRepository;
import com.gd.facturacion.repository.ReceptorRepository;
import com.gd.facturacion.util.CypherUtil;
import com.gd.facturacion.util.FormatUtil;
import com.gd.facturacion.util.Util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;


@Service
public class FacturaService {
	public final FacturaRepository facturaRepository;
	public final EmisorRepository emisorRepository;
	public final ReceptorRepository receptorRepository;
	private final S3Service s3Service;
	public final CounterService countService;
	public final Environment enviroment;
	private   String apiUrlOrdenes = System.getenv("API_URL_ORDENES");

	String bucketName = "storage-ecommerce-dirsio"; // Cambia al nombre de tu bucket
	public FacturaService(FacturaRepository facturaRepository,EmisorRepository emisorRepository
			,ReceptorRepository receptorRepository,CounterService counterService,S3Service s3Service,Environment enviroment) {
		this.facturaRepository=facturaRepository;
		this.emisorRepository=emisorRepository;
		this.receptorRepository=receptorRepository;
		this.countService=counterService;
		this.s3Service=s3Service;
		this.enviroment=enviroment;
	}
	public GenericResponse listarFacturas()  {
    	GenericResponse response =new GenericResponse();

    	response.setTransaccion(facturaRepository.findAll());
    	return response;

	}
	
	public GenericResponse obtenerFactura(Long id) throws NoFoundException  {
    	GenericResponse response =new GenericResponse();
    	Factura factura=facturaRepository.findById(id).orElseThrow(() -> new NoFoundException(404,"Factura no encontrada"));
    	response.setTransaccion(factura);
    	return response;

	}
	
	public GenericResponse elminarFactura(Long id)  {
    	GenericResponse response =new GenericResponse();
    	facturaRepository.deleteById(id);
    	response.setTransaccion(true);
    	return response;

	}
	public Factura generarFactura(long idEmpresa,Long idPago) throws GeneralException  {
		long idFactura=countService.getNextId("PagoCounter");
		Factura factura = new Factura(idFactura);
		String llavePrivada = "";
		
		try {
			PagoResponseEnt pagoResponse =obtenerPago(idPago);
			
			// Asegurar que el Emisor está persistido
			Optional<Emisor> emisorPersistido = emisorRepository.findById(1l);
			if (emisorPersistido.isPresent()) {
				factura.setEmisor(emisorPersistido.get());
				llavePrivada = emisorPersistido.get().getLlavePrivada();
			}

			// Asegurar que el Receptor está persistido
			Optional<Receptor> receptorPersistido = receptorRepository.findById(idEmpresa);
			if (receptorPersistido.isPresent()) {
				factura.setReceptor(receptorPersistido.get());
			}

			
			
			factura.setFecha(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));

			setConceptos(pagoResponse, factura);

			// Generar XML
			String xml = generarCfdiXml(factura);
			factura.setCfdiXml(xml);

			// Firmar cadena
			String sello = firmarCadena(xml, llavePrivada);
			factura.setSelloDigital(sello);

			facturaRepository.save(factura);
			actualizarFacturaPago(idFactura, idPago);
		} catch (Exception e) {
			throw new GeneralException(500,"Ocurrio un error al generar la factura "+Util.imprimirStackTrace(e));
		}
		
		return factura;
	}

	private String generarCfdiXml(Factura factura) throws JAXBException  {
		JAXBContext context = JAXBContext.newInstance(Factura.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		StringWriter writer = new StringWriter();
		marshaller.marshal(factura, writer);

		return writer.toString();
	}

	private String firmarCadena(String cadenaOriginal, String rutaKey) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException  {
		PrivateKey privateKey = CypherUtil.convertStringToPrivateKey(rutaKey);

		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(cadenaOriginal.getBytes());

		return Base64.getEncoder().encodeToString(signature.sign());
	}

	

	public byte[] generarPdf(Factura factura)  {
		/* Crear archivo PDF*/
		byte[]  stream = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {

			PdfWriter writer = new PdfWriter(outputStream);
			PdfDocument pdf = new PdfDocument(writer);
			Document document = new Document(pdf);

			/*AGREGA HEADERS Y LOGO*/
			setReportHeaderAndLogo(document, factura);

			/*AGREGA EMISORES*/
			setCellEmisores(document, factura);
			/*AGREGA DETALLES*/
			setDetalles(document, factura);
			
			/*AGREGA BODY*/
			setBody(document, factura);
		
			/*AGREGA QR Y CONCEPTOS*/
			setQRAndFooter(document, factura);


			/* CERRAR DOCUMENTO*/
			document.close();
			/* OBTNER EL ARREGLO DE BYTES DEL PDF*/
			stream=outputStream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stream;
	}
	
	private void setQRAndFooter(Document document,Factura factura) throws WriterException, IOException {
		/*** BODY QR *****/
		Table bodyTable2 = new Table(UnitValue.createPercentArray(new float[] { 1, 2 })).useAllAvailableWidth()
				.setMarginTop(20);
		

		
		// Código QR
		ByteArrayOutputStream qrCodeStream = generarQrCode(factura);
		ImageData qrCodeData = ImageDataFactory.create(qrCodeStream.toByteArray());
		Image qrCodeImage = new Image(qrCodeData).setWidth(125).setHeight(125);

		Cell importeQRCell = new Cell().add(qrCodeImage).setTextAlignment(TextAlignment.LEFT)
				.setBorder(Border.NO_BORDER);

		bodyTable2.addCell(importeQRCell);

		Cell desgloceConceptos = new Cell()
				.add(new Paragraph().add(new Text("Tipo Pago: ").setBold()).add(new Text("PUE-1").setFontSize(10)))
				.add(new Paragraph().add(new Text("Uso de CFDI: ").setBold())
						.add(new Text("Gastos en General").setFontSize(10)))
				.add(new Paragraph().add(new Text("Serie del Certificado del Emisor: ").setBold())
						.add(new Text("$16.00").setFontSize(10)))
				.add(new Paragraph().add(new Text("Impuestos Referidos: ").setBold())
						.add(new Text("0.00").setFontSize(10)))
				.add(new Paragraph().add(new Text("Folio Fiscal: ").setBold())
						.add(new Text("0.00").setFontSize(10)))
				.setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER);

		bodyTable2.addCell(desgloceConceptos);

		document.add(bodyTable2);

		// Sello digital
		document.add(new Paragraph("Sello Digital del CFDI:").setBold().setFontSize(12));
		document.add(new Paragraph(factura.getSelloDigital()).setFontSize(10).setMarginBottom(10));

		document.add(new Paragraph("Sello Digital del SAT:").setBold().setFontSize(12));
		document.add(new Paragraph(factura.getSelloDigital()).setFontSize(10).setMarginBottom(10));
	}
	private void setBody(Document document,Factura factura) {
		/*** tabla para bodys parrafos intermedios *****/
		Table bodyTable = new Table(UnitValue.createPercentArray(new float[] { 1, 2 })).useAllAvailableWidth()
				.setMarginTop(20);

		Cell importeLetraCell = new Cell()
				.add(new Paragraph().add(new Text("Total Con Letra: ").setBold())
						.add(new Text(FormatUtil.convertir(factura.getTotal()+(factura.getTotal()*0.16))).setFontSize(10)))
				.setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER);

		bodyTable.addCell(importeLetraCell);

		Cell desglocePagoCell = new Cell()
				.add(new Paragraph().add(new Text("Subtotal: ").setBold())
						.add(new Text(String.valueOf(factura.getTotal())).setFontSize(10)))
				.add(new Paragraph().add(new Text("Descuentos: ").setBold()).add(new Text("$0.0").setFontSize(10)))
				.add(new Paragraph().add(new Text("Impuesto Traslado: ").setBold())
						.add(new Text(String.valueOf(factura.getTotal()*0.16)).setFontSize(10)))
				.add(new Paragraph().add(new Text("Impuestos Referidos: ").setBold())
						.add(new Text("0.00").setFontSize(10)))
				.add(new Paragraph().add(new Text("Total: ").setBold()).add(new Text(String.valueOf(factura.getTotal()+(factura.getTotal()*0.16))).setFontSize(10)))
				.setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER);

		bodyTable.addCell(desglocePagoCell);

		document.add(bodyTable);
	}
	private void setDetalles(Document document,Factura factura) {
		// Detalles de Productos/Servicios
		Table detailTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1, 2, 3, 1, 1 }))
				.useAllAvailableWidth().setMarginTop(20);

		// Encabezados de la tabla
		detailTable.addHeaderCell("Cantidad");
		detailTable.addHeaderCell("Unidad");
		detailTable.addHeaderCell("Clave Producto/Servicio");
		detailTable.addHeaderCell("Descripción");
		detailTable.addHeaderCell("Valor Unitario");
		detailTable.addHeaderCell("Importe");

		for (Concepto concepto : factura.getConceptos()) {
			detailTable
					.addCell(new Cell().add(new Paragraph(String.valueOf(concepto.getCantidad())).setFontSize(10)));
			detailTable
					.addCell(new Cell().add(new Paragraph(String.valueOf(concepto.getUnidad())).setFontSize(10)));
			detailTable.addCell(new Cell().add(new Paragraph(concepto.getClaveProdServ()).setFontSize(10)));
			detailTable.addCell(new Cell().add(new Paragraph(concepto.getDescripcion()).setFontSize(10)));
			detailTable.addCell(
					new Cell().add(new Paragraph(String.valueOf(concepto.getValorUnitario())).setFontSize(10)));
			detailTable
					.addCell(new Cell().add(new Paragraph(String.valueOf(concepto.getImporte())).setFontSize(10)));
		}

		// Agregar tabla de detalles
		document.add(detailTable);

	}
	private void setReportHeaderAndLogo(Document document,Factura factura) {
		// Encabezado con logo y datos principales
		Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 1, 2 })).useAllAvailableWidth();
		// Logo
		try {
			// Logo cargado de s3
		    byte[] imageBytes = s3Service.getImageBytes(bucketName, factura.getEmisor().getLogo());

			Image logo = new Image(ImageDataFactory.create(imageBytes)); // Reemplaza con la ruta del logo
			logo.setWidth(100).setHeight(50);
			Cell logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER).setMarginLeft(50);
			headerTable.addCell(logoCell);
		} catch (Exception e) {
			// Si no hay logo, usar texto
			Cell logoCell = new Cell().add(new Paragraph("LOGO")).setBorder(Border.NO_BORDER);
			headerTable.addCell(logoCell);
		}

		// Datos del encabezado
		Cell headerDataCell = new Cell()
				.add(new Paragraph("Fecha de Expedición: " + java.time.LocalDateTime.now().toString()))
				.add(new Paragraph("Lugar de Expedición (CP): 09876"))
				.add(new Paragraph("Tipo de Comprobante: I - Ingreso")).setTextAlignment(TextAlignment.RIGHT)
				.setBorder(Border.NO_BORDER);
		headerTable.addCell(headerDataCell);

		document.add(headerTable);

		
		// Configuración del separador con línea azul
		SolidLine solidLine = new SolidLine(2f); // Grosor de la línea
		solidLine.setColor(new DeviceRgb(0, 74, 173)); // Color azul
		LineSeparator lineSeparator = new LineSeparator(solidLine);

		document.add(new Paragraph("\n")); // Espaciado antes de la línea
		document.add(lineSeparator);
		document.add(new Paragraph("\n")); // Espaciado después de la línea

		setCellEmisores(document, factura);

	}

	private void setCellEmisores(Document document,Factura factura) {

		
		// Tabla para Emisor y Receptor en paralelo
		Table emisoresTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 })).useAllAvailableWidth()
				.setMarginTop(20);
		

		// Columna de Emisor
		Cell emisorCell = new Cell().add(new Paragraph("Emisor:").setBold().setFontSize(12))
				.add(new Paragraph("Nombre: " + factura.getEmisor().getNombre()))
				.add(new Paragraph("Sucursal: Centro"))
				.add(new Paragraph("Domicilio Fiscal: " + factura.getEmisor().getDomicilioFiscal()))
				.add(new Paragraph("RFC: " + factura.getEmisor().getRfc()))
				.add(new Paragraph("Régimen Fiscal: " + factura.getEmisor().getRegimenFiscal()))
				.setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER);
		emisoresTable.addCell(emisorCell);

		// Columna de Receptor
		Cell receptorCell = new Cell()
				.add(new Paragraph("Receptor:").setBold().setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
				.add(new Paragraph("Receptor:" + factura.getReceptor().getNombre()))
				.add(new Paragraph("Dirección: " + factura.getReceptor().getDomicilioFiscal()))
				.add(new Paragraph("RFC: " + factura.getReceptor().getRfc()))
				.add(new Paragraph("Régimen Fiscal: " + factura.getReceptor().getRegimenFiscal()))
				.setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER);
		emisoresTable.addCell(receptorCell);
		// Agregar tabla al documento
		document.add(emisoresTable);

	}
	public void actualizarFacturaPago(long idFactura, long idPago) throws JsonProcessingException {
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper obj = new ObjectMapper();

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		Map<String, Object> actRequest = new HashMap<>();

		actRequest.put("idPago", idPago);
		actRequest.put("idFactura", idFactura);

		HttpEntity<String> entity = new HttpEntity<>(obj.writeValueAsString(actRequest), headers);
		restTemplate.exchange(apiUrlOrdenes + "/ordenes/actualizar", HttpMethod.PUT, entity, String.class);

	}

	private ByteArrayOutputStream generarQrCode(Factura factura) throws WriterException, IOException {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		String qrContent = "||" + factura.getEmisor().getRfc() + "|" + factura.getReceptor().getRfc() + "|"
				+ factura.getTotal() + "|" + factura.getSelloDigital() + "||";
		BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);

		ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
		return pngOutputStream;
	}
	

	public PagoResponseEnt obtenerPago(long idPago) throws  JsonProcessingException  {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		ObjectMapper obj = new ObjectMapper();

		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<String> entity = new HttpEntity<>("", headers);
		ResponseEntity<String>  response=restTemplate.exchange(apiUrlOrdenes + "/ordenes/getPago/"+idPago, HttpMethod.GET, entity, String.class);
		return obj.readValue(response.getBody(), PagoResponseEnt.class);

	}
	
	private void setConceptos(PagoResponseEnt pago ,Factura factura ) {
		List<Concepto> listConceptos=new ArrayList<>();	
		for (LineaPago linea : pago.getLineasPago()) {
			Concepto concepto=new Concepto();
			concepto.setCantidad(linea.getCantidad());
			concepto.setImporte(linea.getSubtotal());
			concepto.setClaveProdServ(linea.getProducto().getSku());
			concepto.setDescripcion(linea.getProducto().getNombre());
			concepto.setValorUnitario(linea.getProducto().getPrecioFinal());
			concepto.setUnidad("PIEZA");
			listConceptos.add(concepto);
		}
		factura.setConceptos(listConceptos);
		factura.setTotal(pago.getImporteTotal());
	}
	
	
	
 	@PostConstruct
    public void loadData() throws GeneralException {
        try {
         if(apiUrlOrdenes==null || apiUrlOrdenes.isEmpty()) {
        	 apiUrlOrdenes=enviroment.getProperty("API_URL_ORDENES");
         }
        } catch (Exception e) {
            throw new GeneralException(500,"Error al cargar datos de articulos"+Util.imprimirStackTrace(e));
        }
    }
  
}
