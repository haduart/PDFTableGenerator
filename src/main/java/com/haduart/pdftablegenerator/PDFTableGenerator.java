package com.haduart.pdftablegenerator;

import com.haduart.pdftablegenerator.structure.Table;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.SortedMap;

public class PDFTableGenerator {
    public static final boolean APPEND_CONTENT = false;
    public static final boolean COMPRESS = false;

    public ByteArrayOutputStream generatePDF(Table table) throws PDFTableGeneratorSavingException {
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        PDDocument doc = null;
        try {
            try {
                doc = new PDDocument();
                if (table.getTextFont() == null) {
                    String dir = "/usr/share/fonts/msttcorefonts/";
                    PDFont font = PDType0Font.load(doc, new File(dir + "verdana.ttf"));
                    table.setTextFont(font);
                }

                drawTable(doc, table);
                doc.save(pdfOutputStream);
            } catch (Exception e) {
                throw new PDFTableGeneratorSavingException(e);
            } finally {
                if (doc != null) {
                    doc.close();
                }
            }
        } catch (IOException e) {
            throw new PDFTableGeneratorSavingException(e);
        }
        return pdfOutputStream;
    }

    public void drawTable(PDDocument doc, Table table) throws IOException {
        // Calculate pagination
        Integer rowsPerPage = new Double(Math.floor(table.getHeight() / table.getRowHeight())).intValue() - 1;
        Integer numberOfPages = new Double(Math.ceil(table.getNumberOfRows().floatValue() / rowsPerPage)).intValue();

        // Generate each page, get the content and draw it

        int pageCount = 0;
        firstPage(doc, table, rowsPerPage - 2, pageCount);
        pageCount++;

        while (pageCount < numberOfPages) {
            PDPage page = generatePage(doc, table);
            PDPageContentStream contentStream = generateContentStream(doc, page, table);
            SortedMap<Integer, LinkedList> currentPageDynamicContent = getDynamicContentForCurrentPage(table, rowsPerPage, pageCount);
            drawCurrentPage(table, currentPageDynamicContent, contentStream);
            pageCount++;
        }
    }

    private void firstPage(PDDocument doc, Table table, int rowsPerPage, int pageCount) throws IOException {
        PDPage page = generatePage(doc, table);
        PDPageContentStream contentStream = generateContentStream(doc, page, table);
        SortedMap<Integer, LinkedList> currentPageContent = getDynamicContentForCurrentPage(table, rowsPerPage, pageCount);

        float tableTopY = table.isLandscape() ? table.getPageSize().getWidth() - table.getMargin() : table.getPageSize().getHeight() - table.getMargin();
        float nextTextX = table.getMargin() + table.getCellMargin();
        float nextTextY = tableTopY - (table.getRowHeight() / 2)
                - ((table.getTextFont().getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * table.getFontSize()) / 4);

        // Write column headers
        nextTextY -= table.getRowHeight();
//            nextTextX = table.getMargin() + table.getCellMargin();
        String text = table.getTitle();

        contentStream.beginText();
        contentStream.moveTextPositionByAmount(nextTextX, nextTextY);
        contentStream.drawString(text != null ? text : "");
        contentStream.endText();

        nextTextY -= table.getRowHeight();
        nextTextX = table.getMargin() + table.getCellMargin();

        drawFirstTableGrid(table, currentPageContent, contentStream, tableTopY);

        // Write column headers
        writeContentLine(table.getColumnsNamesAsArray(), contentStream, nextTextX, nextTextY, table);
        nextTextY -= table.getRowHeight();
        nextTextX = table.getMargin() + table.getCellMargin();

        // Write content
        if (currentPageContent.isEmpty()) {
            contentStream.close();
            return;
        }

        for (int i = currentPageContent.firstKey(); i <= currentPageContent.lastKey(); i++) {
            writeContentLine(currentPageContent.get(i), contentStream, nextTextX, nextTextY, table);
            nextTextY -= table.getRowHeight();
            nextTextX = table.getMargin() + table.getCellMargin();
        }
        contentStream.close();
    }

    private void drawCurrentPage(Table table, SortedMap<Integer, LinkedList> currentPageContent, PDPageContentStream contentStream)
            throws IOException {
        float tableTopY = table.isLandscape() ? table.getPageSize().getWidth() - table.getMargin() : table.getPageSize().getHeight() - table.getMargin();

        // Draws grid and borders
        drawTableGrid(table, currentPageContent, contentStream, tableTopY);

        // Position cursor to start drawing content
        float nextTextX = table.getMargin() + table.getCellMargin();
        // Calculate center alignment for text in cell considering font height
        float nextTextY = tableTopY - (table.getRowHeight() / 2)
                - ((table.getTextFont().getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * table.getFontSize()) / 4);

        // Write column headers
        writeContentLine(table.getColumnsNamesAsArray(), contentStream, nextTextX, nextTextY, table);
        nextTextY -= table.getRowHeight();
        nextTextX = table.getMargin() + table.getCellMargin();

        // Write content
        for (int i = currentPageContent.firstKey(); i <= currentPageContent.lastKey(); i++) {
            writeContentLine(currentPageContent.get(i), contentStream, nextTextX, nextTextY, table);
            nextTextY -= table.getRowHeight();
            nextTextX = table.getMargin() + table.getCellMargin();
        }

        contentStream.close();
    }

    // Writes the content for one line
    private void writeContentLine(LinkedList<String> lineContent, PDPageContentStream contentStream, float nextTextX, float nextTextY,
                                  Table table) throws IOException {
        for (int i = 0; i < table.getNumberOfColumns(); i++) {
            String text = lineContent.get(i);
            contentStream.beginText();
            contentStream.moveTextPositionByAmount(nextTextX, nextTextY);
            contentStream.drawString(text != null ? text : "");
            contentStream.endText();
            nextTextX += table.getColumns().get(i).getWidth();
        }
    }

    private void drawTableGrid(Table table, SortedMap<Integer, LinkedList> currentPageContent, PDPageContentStream contentStream, float tableTopY)
            throws IOException {
        // Draw row lines
        float nextY = tableTopY;
        for (int i = 0; i <= currentPageContent.size() + 1; i++) {
            contentStream.drawLine(table.getMargin(), nextY, table.getMargin() + table.getWidth(), nextY);
            nextY -= table.getRowHeight();
        }

        // Draw column lines
        final float tableYLength = table.getRowHeight() + (table.getRowHeight() * currentPageContent.size());
        final float tableBottomY = tableTopY - tableYLength;
        float nextX = table.getMargin();
        for (int i = 0; i < table.getNumberOfColumns(); i++) {
            contentStream.drawLine(nextX, tableTopY, nextX, tableBottomY);
            nextX += table.getColumns().get(i).getWidth();
        }
        contentStream.drawLine(nextX, tableTopY, nextX, tableBottomY);
    }

    private void drawFirstTableGrid(Table table, SortedMap<Integer, LinkedList> currentPageContent, PDPageContentStream contentStream, float tableTopY)
            throws IOException {
        // Draw row lines
        float nextY = tableTopY;
        for (int i = 0; i <= currentPageContent.size() + 1 + 2; i++) {
            if (i > 1)
                contentStream.drawLine(table.getMargin(), nextY, table.getMargin() + table.getWidth(), nextY);
            nextY -= table.getRowHeight();
        }

        // Draw column lines
        final float tableYLength = table.getRowHeight() + (table.getRowHeight() * currentPageContent.size());
        final float tableBottomY = tableTopY - tableYLength;
        float nextX = table.getMargin();
        for (int i = 0; i < table.getNumberOfColumns(); i++) {
            contentStream.drawLine(nextX, tableTopY - (2 * table.getRowHeight()), nextX, tableBottomY - (2 * table.getRowHeight()));
            nextX += table.getColumns().get(i).getWidth();
        }
        contentStream.drawLine(nextX, tableTopY - (2 * table.getRowHeight()), nextX, tableBottomY - (2 * table.getRowHeight()));
    }

    private SortedMap<Integer, LinkedList> getDynamicContentForCurrentPage(Table table, Integer rowsPerPage, int pageCount) {
        int startRange = pageCount * rowsPerPage;
        int endRange = (pageCount * rowsPerPage) + rowsPerPage;
        if (endRange > table.getNumberOfRows())
            endRange = table.getNumberOfRows();

        return table.getDynamicContent().subMap(startRange, endRange + 1);
    }

    private PDPage generatePage(PDDocument doc, Table table) {
        PDPage page = new PDPage();
        page.setMediaBox(table.getPageSize());
        page.setRotation(table.isLandscape() ? 90 : 0);
        doc.addPage(page);
        return page;
    }

    private PDPageContentStream generateContentStream(PDDocument doc, PDPage page, Table table) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(doc, page, APPEND_CONTENT, COMPRESS);

        contentStream = useTransformationMatrixToChangeReferenceWhenDrawing(table, contentStream);

        //TODO: dirty hack for the font... should be removed
        if (table.getTextFont() != null)
            contentStream.setFont(table.getTextFont(), table.getFontSize());
        return contentStream;
    }

    private PDPageContentStream useTransformationMatrixToChangeReferenceWhenDrawing(Table table, PDPageContentStream contentStream) throws IOException {
        if (table.isLandscape())
            contentStream.concatenate2CTM(0, 1, -1, 0, table.getPageSize().getWidth(), 0);
        return contentStream;
    }

    private class PDFTableGeneratorSavingException extends RuntimeException {
        public PDFTableGeneratorSavingException(Throwable e) {
            super(e);
        }
    }

}
