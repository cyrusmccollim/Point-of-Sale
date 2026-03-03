package pos.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import pos.model.Transaction;
import pos.model.TransactionItem;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReceiptGenerator {

    private static final DateTimeFormatter DATE_FORMAT  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final float MARGIN           = 50;
    private static final float LINE_HEIGHT      = 18;
    private static final float SMALL_LINE_HEIGHT = 14;

    public static boolean generateReceipt(Transaction transaction, String storeName, String storeAddress, String outputPath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            float pageWidth  = page.getMediaBox().getWidth();

            PDPageContentStream cs = new PDPageContentStream(document, page);
            try {
                yPosition = drawCenteredText(cs, storeName,    yPosition,     PDType1Font.HELVETICA_BOLD, 18, pageWidth);
                yPosition = drawCenteredText(cs, storeAddress, yPosition - 5, PDType1Font.HELVETICA,      10, pageWidth);
                yPosition -= 15;

                yPosition = drawSeparator(cs, yPosition, pageWidth);
                yPosition -= 10;
                yPosition = drawText(cs, "Receipt #" + transaction.getId(), MARGIN, yPosition, PDType1Font.HELVETICA_BOLD, 11);
                yPosition = drawText(cs, "Date: " + transaction.getTimestamp().format(DATE_FORMAT), MARGIN, yPosition - LINE_HEIGHT, PDType1Font.HELVETICA, 10);
                yPosition -= 20;

                yPosition = drawSeparator(cs, yPosition, pageWidth);
                yPosition = drawText(cs, String.format("%-25s %8s %10s", "Item", "Qty", "Price"), MARGIN, yPosition - 10, PDType1Font.HELVETICA_BOLD, 10);
                yPosition = drawSeparator(cs, yPosition - LINE_HEIGHT, pageWidth);
                yPosition -= 10;

                List<TransactionItem> items = transaction.getItems();
                for (TransactionItem item : items) {
                    if (yPosition < MARGIN + 100) {
                        cs.close();
                        page      = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        yPosition = page.getMediaBox().getHeight() - MARGIN;
                        pageWidth = page.getMediaBox().getWidth();
                        cs = new PDPageContentStream(document, page);
                    }

                    String itemName = truncate(item.getProductName(), 22);
                    String qty      = item.getWeight() > 0 ? String.format("%d x %.2f lb", item.getQuantity(), item.getWeight()) : String.format("%d", item.getQuantity());

                    yPosition = drawText(cs, itemName, MARGIN, yPosition, PDType1Font.HELVETICA, 9);
                    yPosition = drawText(cs, qty, MARGIN + 200, yPosition, PDType1Font.HELVETICA, 9);
                    yPosition = drawText(cs, Utility.formatPrice(item.getSubtotal()), MARGIN + 320, yPosition, PDType1Font.HELVETICA, 9);
                    yPosition -= SMALL_LINE_HEIGHT;

                    if (item.getWeight() > 0) {
                        yPosition = drawText(cs, "  @ " + Utility.formatPrice(item.getUnitPrice()) + "/lb", MARGIN, yPosition, PDType1Font.HELVETICA, 8);
                        yPosition -= SMALL_LINE_HEIGHT;
                    }
                }

                yPosition -= 10;
                yPosition = drawSeparator(cs, yPosition, pageWidth);
                yPosition -= 10;

                drawText(cs, "TOTAL:",                              MARGIN + 200, yPosition, PDType1Font.HELVETICA_BOLD, 12);
                drawText(cs, Utility.formatPrice(transaction.getTotal()), MARGIN + 280, yPosition, PDType1Font.HELVETICA_BOLD, 14);

                yPosition -= 40;
                yPosition = drawCenteredText(cs, "Thank you for your purchase!", yPosition,              PDType1Font.HELVETICA_OBLIQUE, 10, pageWidth);
                drawCenteredText(cs, "Please come again",             yPosition - LINE_HEIGHT, PDType1Font.HELVETICA_OBLIQUE,  9, pageWidth);

            } finally {
                cs.close();
            }

            document.save(outputPath);
            Logger.info("Generated PDF receipt: " + outputPath);
            return true;

        } catch (IOException e) {
            Logger.error("Failed to generate PDF receipt", e);
            return false;
        }
    }

    private static float drawText(PDPageContentStream cs, String text, float x, float y, PDType1Font font, float fontSize) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private static float drawCenteredText(PDPageContentStream cs, String text, float y, PDType1Font font, float fontSize, float pageWidth) throws IOException {
        float x = (pageWidth - font.getStringWidth(text) / 1000 * fontSize) / 2;
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - LINE_HEIGHT;
    }

    private static float drawSeparator(PDPageContentStream cs, float y, float pageWidth) throws IOException {
        cs.moveTo(MARGIN, y);
        cs.lineTo(pageWidth - MARGIN, y);
        cs.stroke();
        return y - LINE_HEIGHT;
    }

    private static String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 2) + "..";
    }
}
