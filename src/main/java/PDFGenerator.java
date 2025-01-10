import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;

public class PDFGenerator {
    public static void convertTextToPdf(String text, String outputPath) {
        Document document = new Document();

        try {
            // Create PDF writer
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));

            // Open document
            document.open();

            // Add content
            document.add(new Paragraph(text));

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            // Close document
            document.close();
        }
    }

    public static void main(String[] args) {
        // Example usage
        String text = "This is a sample text that will be converted to PDF.\n\n"
                + "You can include multiple paragraphs and format them as needed.\n\n"
                + "The text will maintain its formatting in the PDF.";

        String outputPath = "output.pdf";

        convertTextToPdf(text, outputPath);
        System.out.println("PDF created successfully at: " + outputPath);
    }
}