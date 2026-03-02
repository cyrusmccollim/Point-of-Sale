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

/**
 * Generates PDF receipts for transactions.
 */
public class PdfReceiptGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 18;
    private static final float SMALL_LINE_HEIGHT = 14;

    /**
     * Generates a PDF receipt for a transaction.
     *
     * @param transaction The transaction
     * @param storeName   The store name
     * @param storeAddress The store address
     * @param outputPath  The output file path
     * @return true if successful, false otherwise
     */
    public static boolean generateReceipt(Transaction transaction, String storeName, String storeAddress, String outputPath) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            float pageWidth = page.getMediaBox().getWidth();

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                // Header
                yPosition = drawCenteredText(contentStream, storeName, yPosition, PDType1Font.HELVETICA_BOLD, 18, pageWidth);
                yPosition = drawCenteredText(contentStream, storeAddress, yPosition - 5, PDType1Font.HELVETICA, 10, pageWidth);
                yPosition -= 15;

                // Separator
                yPosition = drawSeparator(contentStream, yPosition, pageWidth);
                yPosition -= 10;

                // Transaction info
                yPosition = drawText(contentStream, "Receipt #" + transaction.getId(), MARGIN, yPosition, PDType1Font.HELVETICA_BOLD, 11);
                yPosition = drawText(contentStream, "Date: " + transaction.getTimestamp().format(DATE_FORMAT), MARGIN, yPosition - LINE_HEIGHT, PDType1Font.HELVETICA, 10);
                yPosition -= 20;

                // Items header
                yPosition = drawSeparator(contentStream, yPosition, pageWidth);
                yPosition = drawText(contentStream, String.format("%-25s %8s %10s", "Item", "Qty", "Price"), MARGIN, yPosition - 10, PDType1Font.HELVETICA_BOLD, 10);
                yPosition = drawSeparator(contentStream, yPosition - LINE_HEIGHT, pageWidth);
                yPosition -= 10;

                // Items
                List<TransactionItem> items = transaction.getItems();
                for (TransactionItem item : items) {
                    if (yPosition < MARGIN + 100) {
                        // Need new page - close current and create new
                        contentStream.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        yPosition = page.getMediaBox().getHeight() - MARGIN;
                        pageWidth = page.getMediaBox().getWidth();
                        contentStream = new PDPageContentStream(document, page);
                    }

                    String itemName = truncate(item.getProductName(), 22);
                    String qty = item.getWeight() > 0
                            ? String.format("%d x %.2f lb", item.getQuantity(), item.getWeight())
                            : String.format("%d", item.getQuantity());

                    double totalPrice = item.getQuantity() * item.getWeight() * item.getUnitPrice();
                    if (item.getWeight() == 0) {
                        totalPrice = item.getQuantity() * item.getUnitPrice();
                    }

                    yPosition = drawText(contentStream, itemName, MARGIN, yPosition, PDType1Font.HELVETICA, 9);
                    yPosition = drawText(contentStream, qty, MARGIN + 200, yPosition, PDType1Font.HELVETICA, 9);
                    yPosition = drawText(contentStream, Utility.formatPrice(totalPrice), MARGIN + 320, yPosition, PDType1Font.HELVETICA, 9);
                    yPosition -= SMALL_LINE_HEIGHT;

                    if (item.getWeight() > 0) {
                        String details = String.format("  @ %s/lb", Utility.formatPrice(item.getUnitPrice()));
                        yPosition = drawText(contentStream, details, MARGIN, yPosition, PDType1Font.HELVETICA, 8);
                        yPosition -= SMALL_LINE_HEIGHT;
                    }
                }

                // Total section
                yPosition -= 10;
                yPosition = drawSeparator(contentStream, yPosition, pageWidth);
                yPosition -= 10;

                // Total
                drawText(contentStream, "TOTAL:", MARGIN + 200, yPosition, PDType1Font.HELVETICA_BOLD, 12);
                drawText(contentStream, Utility.formatPrice(transaction.getTotal()), MARGIN + 280, yPosition, PDType1Font.HELVETICA_BOLD, 14);

                // Footer
                yPosition -= 40;
                yPosition = drawCenteredText(contentStream, "Thank you for your purchase!", yPosition, PDType1Font.HELVETICA_OBLIQUE, 10, pageWidth);
                yPosition = drawCenteredText(contentStream, "Please come again", yPosition - LINE_HEIGHT, PDType1Font.HELVETICA_OBLIQUE, 9, pageWidth);

            } finally {
                contentStream.close();
            }

            document.save(outputPath);
            Logger.info("Generated PDF receipt: " + outputPath);
            return true;

        } catch (IOException e) {
            Logger.error("Failed to generate PDF receipt", e);
            return false;
        }
    }

    private static float drawText(PDPageContentStream contentStream, String text, float x, float y, PDType1Font font, float fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        return y - LINE_HEIGHT;
    }

    private static float drawCenteredText(PDPageContentStream contentStream, String text, float y, PDType1Font font, float fontSize, float pageWidth) throws IOException {
        float titleWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (pageWidth - titleWidth) / 2;

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        return y - LINE_HEIGHT;
    }

    private static float drawSeparator(PDPageContentStream contentStream, float y, float pageWidth) throws IOException {
        contentStream.moveTo(MARGIN, y);
        contentStream.lineTo(pageWidth - MARGIN, y);
        contentStream.stroke();
        return y - LINE_HEIGHT;
    }

    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 2) + "..";
    }
}
