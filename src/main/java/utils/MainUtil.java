package utils;

import org.testng.Assert;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 * Created by a.chebotareva on 06.06.2017.
 */
public class MainUtil {
    private static void setClipboard(String str) {
        StringSelection ss = new StringSelection(str);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }
    private static void pasteAndEnter() throws AWTException {
        Robot robot = new Robot();
// Ctrl-V + Enter on Win
        robot.delay(3000);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_ENTER);
    }

    public static void closeWindow() throws AWTException {
        Robot robot = new Robot();
        robot.delay(3000);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_F4);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyRelease(KeyEvent.VK_F4);
    }
    public static void typeAndEnter(String str) throws AWTException {
        setClipboard(str);
        pasteAndEnter();
    }
    public  static String getExtension(String fileName){

        return fileName.split("\\.")[fileName.split("\\.").length-1];
    }
    public static boolean compareMaps(HashMap<String,String> site, HashMap<String,String> doc){
        try{
            Assert.assertEquals(doc.size(),site.size());
            for(String skey:site.keySet()){
                if(doc.containsKey(skey)){
                    if(!(doc.get(skey).equals(site.get(skey)))){
                        return false;
                    }
                }else{
                    return false;                }
            }
            return true;
        }catch (AssertionError e){
            return false;
        }
    }

}
