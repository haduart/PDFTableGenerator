package com.haduart.pdftablegenerator;

import com.haduart.pdftablegenerator.structure.Column;
import com.haduart.pdftablegenerator.structure.Table;
import com.haduart.pdftablegenerator.structure.TableBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;

import static org.junit.Assert.assertFalse;

/**
 * Created by haduart on 05/04/15.
 */
public class PDFTableGeneratorTest {
    // Page configuration
    private static final PDRectangle PAGE_SIZE = PDPage.PAGE_SIZE_A3;
    private static final float MARGIN = 20;
    private static final boolean IS_LANDSCAPE = true;

    // Font configuration
    private static final PDFont TEXT_FONT = PDType1Font.HELVETICA;
    private static final float FONT_SIZE = 10;

    // Table configuration
    private static final float ROW_HEIGHT = 15;
    private static final float CELL_MARGIN = 2;

    @Test
    public void generatePDFTest() throws Exception {
        ByteArrayOutputStream baos = new PDFTableGenerator().generatePDF(createContent());
        byte[] writtenBytes = baos.toByteArray();
        assertFalse(writtenBytes.length == 0);
        FileUtils.writeByteArrayToFile(new File("/Users/haduart/Documents/PDFProjects/Paginated-PDFBox-Table-Sample/edu777.pdf"),
                writtenBytes);
    }

    private static Table createContent() {
        // Total size of columns must not be greater than table width.
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("FirstName", 90));
        columns.add(new Column("LastName", 90));
        columns.add(new Column("Email", 230));
        columns.add(new Column("ZipCode", 43));
        columns.add(new Column("MailOptIn", 50));
        columns.add(new Column("Code", 80));
        columns.add(new Column("Branch", 39));
        columns.add(new Column("Product", 300));
        columns.add(new Column("Date", 120));
        columns.add(new Column("Channel", 43));

        SortedMap<Integer, LinkedList> dynamicContent = new TreeMap<Integer, LinkedList>();

        dynamicContent.put(1, generateRow("FirstName-1", "LastName-1", "fakemail@mock.com-1", "12345", "yes", "XH4234FSD", "4334", "yFone 5 XS", "31/05/2013 07:15 am", "WEB"));
        dynamicContent.put(2, generateRow("FirstName-2", "LastName-2", "fakemail@mock.com-2", "12345", "yes", "XH4234FSD", "4334", "yFone 5 XS", "31/05/2013 07:15 am", "WEB"));
        dynamicContent.put(3, generateRow("FirstName-3", "LastName-3", "fakemail@mock.com-3", "12345", "yes", "XH4234FSD", "4334", "yFone 5 XS", "31/05/2013 07:15 am", "WEB"));
        dynamicContent.put(4, generateRow("FirstName-4", "LastName-4", "fakemail@mock.com-4", "12345", "yes", "XH4234FSD", "4334", "yFone 5 XS", "31/05/2013 07:15 am", "WEB"));
        dynamicContent.put(5, generateRow("FirstName-5", "LastName-5", "fakemail@mock.com-5", "12345", "yes", "XH4234FSD", "4334", "yFone 5 XS", "31/05/2013 07:15 am", "WEB"));
        dynamicContent.put(6, generateRow("FirstName-6", "LastName-6", "fakemail@mock.com-6", "12345", "yes", "XH4234FSD", "4334", "yFone 5 XS", "31/05/2013 07:15 am", "WEB"));

        float tableHeight = IS_LANDSCAPE ? PAGE_SIZE.getWidth() - (2 * MARGIN) : PAGE_SIZE.getHeight() - (2 * MARGIN);

        Table table = new TableBuilder()
                .setCellMargin(CELL_MARGIN)
                .setColumns(columns)
                .setDynamicContent(dynamicContent)
                .setHeight(tableHeight)
                .setNumberOfRows(dynamicContent.size())
                .setRowHeight(ROW_HEIGHT)
                .setMargin(MARGIN)
                .setPageSize(PAGE_SIZE)
                .setLandscape(IS_LANDSCAPE)
                .setTextFont(TEXT_FONT)
                .setFontSize(FONT_SIZE)
                .build();
        return table;
    }

    private static LinkedList generateRow(String... rows) {
        LinkedList linkedList = new LinkedList();
        for (String row : rows)
            linkedList.add(row);
        return linkedList;
    }

}
