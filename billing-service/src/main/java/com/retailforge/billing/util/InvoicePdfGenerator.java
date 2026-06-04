package com.retailforge.billing.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.retailforge.dto.ProductDto;
import com.retailforge.billing.model.Order;
import com.retailforge.billing.model.OrderItem;
import com.retailforge.billing.model.Payment;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class InvoicePdfGenerator {

    public static byte[] generateInvoicePdf(Order order, Payment payment, String invoiceNumber, Map<Long, ProductDto> productMap,
                                            String businessName, String businessAddress, String businessGstin, String businessPhone) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font mainTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font boldTextFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // Store Info Title
            Paragraph title = new Paragraph(businessName, mainTitleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph storeDetails = new Paragraph(businessAddress + "\nGSTIN: " + businessGstin + "\nTel: " + businessPhone + "\n", textFont);
            storeDetails.setAlignment(Element.ALIGN_CENTER);
            document.add(storeDetails);

            document.add(new Paragraph("---------------------------------------------------------------------------------------------------------------------------------", textFont));

            // Invoice & Order Metadata
            Paragraph meta = new Paragraph();
            meta.setFont(textFont);
            meta.add(new Chunk("Invoice Number: " + invoiceNumber + "\n", boldTextFont));
            meta.add(new Chunk("Order Reference: " + order.getOrderNumber() + "\n", textFont));
            meta.add(new Chunk("Date/Time: " + order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n", textFont));
            meta.add(new Chunk("Cashier: POS Register #1\n", textFont));
            document.add(meta);

            document.add(new Paragraph("\n"));

            // Table Header
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 3f, 1f, 1.5f, 1.5f, 2f});

            addTableCell(table, "Sl.", boldTextFont, Element.ALIGN_CENTER);
            addTableCell(table, "Item Name", boldTextFont, Element.ALIGN_LEFT);
            addTableCell(table, "Qty", boldTextFont, Element.ALIGN_CENTER);
            addTableCell(table, "Rate (INR)", boldTextFont, Element.ALIGN_RIGHT);
            addTableCell(table, "GST Amt", boldTextFont, Element.ALIGN_RIGHT);
            addTableCell(table, "Total (INR)", boldTextFont, Element.ALIGN_RIGHT);

            int sl = 1;
            BigDecimal subTotal = BigDecimal.ZERO;
            BigDecimal totalGst = BigDecimal.ZERO;

            for (OrderItem item : order.getItems()) {
                ProductDto prod = productMap.get(item.getProductId());
                String prodName = prod != null ? prod.name() : "Product ID: " + item.getProductId();

                addTableCell(table, String.valueOf(sl++), textFont, Element.ALIGN_CENTER);
                addTableCell(table, prodName, textFont, Element.ALIGN_LEFT);
                addTableCell(table, String.valueOf(item.getQuantity()), textFont, Element.ALIGN_CENTER);
                addTableCell(table, item.getPrice().toString(), textFont, Element.ALIGN_RIGHT);
                addTableCell(table, item.getGstAmount().toString(), textFont, Element.ALIGN_RIGHT);

                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                        .add(item.getGstAmount());
                addTableCell(table, itemTotal.setScale(2, RoundingMode.HALF_UP).toString(), textFont, Element.ALIGN_RIGHT);

                subTotal = subTotal.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                totalGst = totalGst.add(item.getGstAmount());
            }

            document.add(table);

            document.add(new Paragraph("\n"));

            // Summary Math Block
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            BigDecimal cgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            BigDecimal sgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

            addSummaryRow(summaryTable, "Subtotal:", subTotal.setScale(2, RoundingMode.HALF_UP).toString(), textFont);
            addSummaryRow(summaryTable, "CGST (50%):", cgst.toString(), textFont);
            addSummaryRow(summaryTable, "SGST (50%):", sgst.toString(), textFont);
            addSummaryRow(summaryTable, "Total GST:", totalGst.setScale(2, RoundingMode.HALF_UP).toString(), textFont);
            addSummaryRow(summaryTable, "Grand Total:", order.getTotalAmount().toString(), boldTextFont);

            document.add(summaryTable);

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("---------------------------------------------------------------------------------------------------------------------------------", textFont));

            // Payment Metadata
            Paragraph payMeta = new Paragraph();
            payMeta.setFont(textFont);
            payMeta.add(new Chunk("Payment Method: " + payment.getMethod() + "\n", boldTextFont));
            payMeta.add(new Chunk("Payment Status: " + payment.getStatus() + "\n", textFont));
            payMeta.add(new Chunk("Transaction ID: " + payment.getTransactionId() + "\n", textFont));
            document.add(payMeta);

            document.add(new Paragraph("\n\n"));
            Paragraph thankYou = new Paragraph("Thank you for shopping with us! Please visit again.", boldTextFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            document.add(thankYou);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private static void addTableCell(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        table.addCell(cell);
    }

    private static void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, font));
        cellLabel.setBorder(Rectangle.NO_BORDER);
        cellLabel.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell cellValue = new PdfPCell(new Phrase(value, font));
        cellValue.setBorder(Rectangle.NO_BORDER);
        cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(cellLabel);
        table.addCell(cellValue);
    }
}
