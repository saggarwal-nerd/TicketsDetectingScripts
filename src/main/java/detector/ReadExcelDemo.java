package detector;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadExcelDemo 
{
    public static void main(String[] args) 
    {
        try
        {
            FileInputStream file = new FileInputStream(new File("src/main/java/detector/JIRA-2.xls"));
 
            //Create Workbook instance holding reference to .xlsx file
            HSSFWorkbook workbook = new HSSFWorkbook(file);
 
            //Get first/desired sheet from the workbook
            HSSFSheet sheet = workbook.getSheetAt(0);
            int ctr=5;
            Row row=null;
            Cell cell=null;
            Cell cell2=null;
            boolean isNull = false;
                    do{
                        try{
                        row = sheet.getRow(ctr);
                        cell = row.getCell(1);
                        cell2=row.getCell(6);
                        if(cell2.toString().equals("Code Review") || cell2.toString().equals("In Progress"))
                        {
                        System.out.print(cell.toString());
                        System.out.println("    "+cell2.toString());
                        }
                        ctr++;
                        } catch(Exception e) {
                            isNull = true;
                        }
                    }while(isNull!=true);
                    file.close();
 
    }catch (Exception e){ 
            e.printStackTrace();
        }
    }
}
