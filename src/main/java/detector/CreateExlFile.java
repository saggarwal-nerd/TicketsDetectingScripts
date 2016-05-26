package detector;

import  java.io.*;

import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.*;

public class CreateExlFile{
	public static void main(String[]args) {
        try {
            String filename = "src/main/java/detector/NewExcelFile.xls" ;
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("FirstSheet");
            HSSFSheetConditionalFormatting cf =sheet.getSheetConditionalFormatting();
            HSSFConditionalFormattingRule cfrole=cf.createConditionalFormattingRule(ComparisonOperator.NOT_EQUAL, "$A$1");

            HSSFPatternFormatting fill_pattern = cfrole.createPatternFormatting();
            fill_pattern.setFillBackgroundColor(IndexedColors.YELLOW.index);

			CellRangeAddress[] my_data_range = {(CellRangeAddress) CellRangeAddress.valueOf("A2:D2")};
            cf.addConditionalFormatting(my_data_range,cfrole);
            sheet.setColumnWidth(0,2000);
            sheet.setColumnWidth(1,5000);
            sheet.setColumnWidth(2,8000);
            sheet.setColumnWidth(3,8000);
            HSSFCellStyle style = workbook.createCellStyle();
            HSSFRow rowhead = sheet.createRow((short)1);
            HSSFCell cell =rowhead.createCell(0);
            cell.setCellValue("No.");
            cell.setCellStyle(style);
            rowhead.createCell(1).setCellValue("Name");
            rowhead.createCell(2).setCellValue("Address");
            rowhead.createCell(3).setCellValue("Email");

            HSSFRow row = sheet.createRow((short)2);
            row.createCell(0).setCellValue("1");
            row.createCell(1).setCellValue("Sankumarsingh");
            row.createCell(2).setCellValue("India");
            row.createCell(3).setCellValue("sankumarsingh@gmail.com");

            FileOutputStream fileOut = new FileOutputStream(filename);
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("Your excel file has been generated!");

        } catch ( Exception ex ) {
            System.out.println(ex);
        }
    }
}
