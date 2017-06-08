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
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by a.chebotareva on 06.06.2017.
 */
public class ExcelParser {
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


}
