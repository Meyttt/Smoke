package ExcelFun;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.MainUtil;

import javax.naming.ldap.HasControls;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by a.chebotareva on 06.06.2017.
 */
public class ExcelParser {
    Workbook wb;
    public ExcelParser(File file){
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        switch (MainUtil.getExtension(file.getName())) {
            case "xls":
                try {
                    this.wb= new HSSFWorkbook(inputStream);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "xlsx":
                try {
                    this.wb = new XSSFWorkbook(inputStream);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "xlsm":
                try {
                    this.wb=new XSSFWorkbook(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                throw new Error("unknown type");
        }
    }

    static  Workbook excel(File file) throws IOException {
        Workbook wb;
        FileInputStream inputStream = new FileInputStream(file);
        switch (MainUtil.getExtension(file.getName())) {
            case "xls":
                return new HSSFWorkbook(inputStream);
            case "xlsx":
                return new XSSFWorkbook(inputStream);
            case "xlsm":
                return new XSSFWorkbook(inputStream);
            default:
                throw new Error("unknown type");
        }
    }

    //    public boolean compareFormWithHashmap(HashMap<String,>)
    public static HashMap<String,String> getValues(String name) {
        try {
            Workbook wb = null;
            wb = excel(new File(name));
            HashMap<String, String> map = new HashMap<>();

            for (Sheet sheet : wb) {
                Iterator<Row> rows = sheet.rowIterator();
                while (rows.hasNext()) {
                    Row row = rows.next();
                    if (row.getCell(1) != null && row.getCell(1).getCellType() == Cell.CELL_TYPE_STRING && row.getCell(1).getStringCellValue().equals("Код строки")) {
                        while (rows.hasNext()) {
                            row=rows.next();
                            map.put(row.getCell(1).getStringCellValue()+"."+row.getCell(2).getStringCellValue(), String.valueOf(row.getCell(3).getNumericCellValue()).split("\\.")[0]);
                        }
                    }
                }
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean rowEqualsValue(int rowNumber,String value){
        Sheet sheet=this.wb.getSheetAt(this.wb.getFirstVisibleTab());
        String row =sheet.getRow(rowNumber).getCell(0).getStringCellValue();
        return row.equals(value);
    }
    public boolean rowEqualsValue(int rowNumber,int columnNumber, String value){
        Sheet sheet=this.wb.getSheetAt(this.wb.getFirstVisibleTab());
        String row =sheet.getRow(rowNumber).getCell(columnNumber).getStringCellValue();
        return row.equals(value);
    }


}
