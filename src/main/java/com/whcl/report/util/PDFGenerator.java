package com.whcl.report.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.whcl.report.model.Order;
import com.whcl.report.model.OrderItem;

@Service
public class PDFGenerator {
	
	private static final String TOTAL = "Total : ";
	private static final String DELIVERY_CHARGE_1_RS_PER_KM = "Delivery charge @1 Rs per km: ";
	private static final String TAX_5_ON_ITEM_COST = "Tax @5% on item cost : ";
	protected final Log logger = LogFactory.getLog(PDFGenerator.class);
	
	@Value("${report.header}")
	private String reportHeader;
	
	/**
	 * Generate pdf report for an order
	 * @param order
	 * @return
	 */
	public ByteArrayInputStream generateInviceReport(Order order) {
		Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
        	
        	PdfWriter.getInstance(document, out);
            document.open();
        	
            // Add Text to PDF file ->
        	Font font = FontFactory.getFont(FontFactory.COURIER, 14, BaseColor.BLACK);
        	Paragraph para = new Paragraph(reportHeader, font);
        	para.setAlignment(Element.ALIGN_CENTER);
        	document.add(para);
        	
        	Paragraph orderpara = new Paragraph("ORDER No.#"+String.valueOf(order.getOrderId()), font);
        	orderpara.setAlignment(Element.ALIGN_CENTER);
        	document.add(orderpara);
        	document.add(Chunk.NEWLINE);
        	
        	
        	//TODO, This field information will come from user service, that need to be developed later
        	Paragraph name = new Paragraph("Customer Name : "+ "XYZ", font);
        	orderpara.setAlignment(Element.ALIGN_LEFT);
        	document.add(name);
        	
        	//TODO, This field information will come from user service, that need to be developed later
        	Paragraph add = new Paragraph("Address : "+ "XYZ, Delhi-110012", font);
        	orderpara.setAlignment(Element.ALIGN_LEFT);
        	document.add(add);
        	document.add(Chunk.NEWLINE);
        	
        	PdfPTable table = new PdfPTable(4);
        	// Add PDF Table Header ->
            Stream.of("S.No", "Item", "Quantity", "Unit Price")
	            .forEach(headerTitle -> {
		              PdfPCell header = new PdfPCell();
		              Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		              header.setBackgroundColor(BaseColor.LIGHT_GRAY);
		              header.setHorizontalAlignment(Element.ALIGN_CENTER);
		              header.setBorderWidth(2);
		              header.setPhrase(new Phrase(headerTitle, headFont));
		              table.addCell(header);
	            });
            int sno=1;
            double totalbase = 0.0;
            List<OrderItem> items = order.getList();
            for (OrderItem item: items) {
            		totalbase+=(item.getQuantity() * item.getUnitPrice());
	            	
            		PdfPCell idCell = new PdfPCell(new Phrase(String.valueOf(sno++)));
	            	idCell.setPaddingLeft(4);
	            	idCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
	            	idCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	                table.addCell(idCell);
	
	                PdfPCell itemname = new PdfPCell(new Phrase(item.getName()));
	                itemname.setPaddingLeft(4);
	                itemname.setVerticalAlignment(Element.ALIGN_MIDDLE);
	                itemname.setHorizontalAlignment(Element.ALIGN_LEFT);
	                table.addCell(itemname);
	
	                PdfPCell quantity = new PdfPCell(new Phrase(String.valueOf(item.getQuantity())));
	                quantity.setVerticalAlignment(Element.ALIGN_MIDDLE);
	                quantity.setHorizontalAlignment(Element.ALIGN_CENTER);
	                quantity.setPaddingRight(4);
	                table.addCell(quantity);
	                
	                PdfPCell price = new PdfPCell(new Phrase(String.valueOf(item.getUnitPrice())));
	                price.setVerticalAlignment(Element.ALIGN_MIDDLE);
	                price.setHorizontalAlignment(Element.ALIGN_CENTER);
	                price.setPaddingRight(4);
	                table.addCell(price);
            }
            
            
            
            document.add(table);
            
            Paragraph tax = new Paragraph(TAX_5_ON_ITEM_COST+ String.valueOf((totalbase*5)/100), font);
            tax.setAlignment(Element.ALIGN_RIGHT);
        	document.add(tax);
        	
        	Paragraph deliveryCharge = new Paragraph(DELIVERY_CHARGE_1_RS_PER_KM+ String.valueOf(order.getDistance()), font);
        	deliveryCharge.setAlignment(Element.ALIGN_RIGHT);
        	document.add(deliveryCharge);
        	
        	Paragraph totalCost = new Paragraph(TOTAL+ String.valueOf(order.getTotalPrice()), FontFactory.getFont(FontFactory.HELVETICA_BOLD));
        	totalCost.setAlignment(Element.ALIGN_RIGHT);
        	document.add(totalCost);
            
            
            document.close();
        } catch(DocumentException e) {
        	logger.error(e.toString());
        }
        
		return new ByteArrayInputStream(out.toByteArray());
	}

}
