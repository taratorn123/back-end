package com.angular.donationblock.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;

import com.angular.donationblock.entity.AccountDonation;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class GeneratePdfReport 
{
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static ByteArrayInputStream test(AccountDonation transaction) throws MalformedURLException, IOException
	{
		Document document = new Document(); 
		/*
		 * 
		 * InputStream is used for many things that you read from.
		 * OutputStream is used for many things that you write to.
		 * */
		ByteArrayOutputStream out = new ByteArrayOutputStream();
    	try 
    	{
    		Font fontHeader = new Font(FontFamily.TIMES_ROMAN,20f,
    				Font.UNDERLINE,BaseColor.BLACK);
    		Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
	    	Paragraph header = new Paragraph ("DONATION RECEIPT",fontHeader); 
	    	header.setAlignment(Element.ALIGN_CENTER);
	    	
	    	Paragraph firstNameLastName = new Paragraph ("Name : "+transaction.getUser().getFirstName()+" "+transaction.getUser().getLastName(),headFont); 
	    	firstNameLastName.setAlignment(Element.ALIGN_LEFT);
	    	
	    	Paragraph email = new Paragraph("Email : "+transaction.getUser().getEmail(),headFont);
	    	email.setAlignment(Element.ALIGN_LEFT);
	    	
	    	Paragraph transactionNumber = new Paragraph("Transaction Number : "+transaction.getId(),headFont);
	    	transactionNumber.setAlignment(Element.ALIGN_LEFT);
	    	Paragraph transactionHash = new Paragraph("Transaction Hash : "+transaction.getTransactionHash(),headFont);
	    	transactionNumber.setAlignment(Element.ALIGN_LEFT);
	    	
	    	/* Create Transaction table*/
	    	PdfPTable table = new PdfPTable(4);
	    	float[] columnWidths = new float[] {20f, 25f, 15f, 15f};
	    	table.setWidths(columnWidths);
	    	table.setWidthPercentage(100);
            //table.setWidths(new int[]{3, 3, 3});
            
      
            /*Table header*/
    		PdfPCell hcell;
    		hcell = new PdfPCell(new Phrase("Date", headFont));
            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hcell);

            hcell = new PdfPCell(new Phrase("Campaign Name", headFont));
            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hcell);
            
            hcell = new PdfPCell(new Phrase("Lumen(XLM)", headFont));
            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hcell);
            
            hcell = new PdfPCell(new Phrase("Thai baht(THB)", headFont));
            hcell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(hcell);
            
            
	    	/*Table detail*/
            PdfPCell cell;
            
            cell = new PdfPCell(new Phrase(dateFormat.format(transaction.getTimestamp())));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(transaction.getCampaign().getCampaignName()));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
            
            cell = new PdfPCell(new Phrase(transaction.getAmount()));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
            
            cell = new PdfPCell(new Phrase(DatabaseUtil.decimalConverter(String.valueOf(Double.parseDouble(transaction.getAmount())*transaction.getExchageRate()))));
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

			
			/*Add image to pdf file*/
			Image image1 = Image.getInstance(transaction.getCampaign().getUser().getRouteSignatureImage());
			image1.setAlignment(Element.ALIGN_CENTER);
			image1.scaleAbsolute(100,100);
			image1.setIndentationLeft(150f);
			
			/*Insert Campaign Owner*/
			Paragraph owner = new Paragraph(transaction.getCampaign().getUser().getFirstName()+" "+transaction.getCampaign().getUser().getLastName(),headFont);
			Paragraph campaignOwner = new Paragraph("(Campaign Owner)");
			owner.setIndentationLeft(355f);
			campaignOwner.setIndentationLeft(355f);
			
			
			
            /*Stellar Donation*/
            Paragraph companyName = new Paragraph("Company name : Stellar Donation",headFont);
            Paragraph companyEmail = new Paragraph("Company email : stellardonation053@gmail.com",headFont);

            companyName.setIndentationLeft(240f);
            companyEmail.setIndentationLeft(240f);
            
                     
	    	PdfWriter.getInstance(document, out);
	    	document.open();
	    	document.add(header);	
	    	document.add(Chunk.NEWLINE);
	    	document.add(firstNameLastName);
	    	document.add(email);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(transactionNumber);
	    	document.add(transactionHash);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(table);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(image1);
	    	document.add(owner);
	    	document.add(campaignOwner);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(Chunk.NEWLINE);
	    	document.add(companyName);
	    	document.add(companyEmail);
	    	
	    	
	    	document.close();
		} 
    	catch (DocumentException e) 
    	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return new ByteArrayInputStream(out.toByteArray());
	}
}
