package y.auto.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONObject;
import y.auto.main.Main;

public class HolidayUtil {

    public static Map<String, Integer> cache = new HashMap<>();
    public static String YEAR = "";

    /**
     * 判断是否工作日
     *
     * @param pTime
     * @return
     */
    public static int isWorkDay(String pTime) {
        try {
            if (null == cache.get(pTime)) {
                String url = "http://api.goseek.cn/Tools/holiday?date=" + pTime;
                String ret = HttpUtil.sendGet(url, "", "UTF-8");
                System.out.println("判断节假日接口返回：" + ret);
                if (StringUtils.isNotBlank(ret)) {
                    JSONObject json = JSONObject.fromObject(ret);
                    int a = json.getInt("data");
                    cache.put(pTime, a);
                    return a;
                }
                return 0;
            } else {
                return cache.get(pTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int isWorkDay__(String pTime) {
        try {

            Date d = Main.StrToDate(pTime, "yyyyMMdd");
            String weekStr = Main.getWeekOfDate(d);
            if (weekStr.equals("星期六") || weekStr.equals("星期日")) {
                return 0;
            }

            String y = pTime.substring(0, 4);
            if (null == cache.get(pTime) && YEAR != y) {
                YEAR = y;
                String url = "https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?query=" + pTime.substring(0, 6) + "&co=&resource_id=6018";
                String ret = HttpUtil.sendGet(url, "", "GBK");

                if (StringUtils.isNotBlank(ret)) {
                    JSONObject json = JSONObject.fromObject(ret);
                    JSONArray holidayls = json.getJSONArray("data").getJSONObject(0).getJSONArray("holiday");

                    //先把一年的节假日存起来
                    for (int i = 0; i < holidayls.size(); i++) {
                        JSONArray holidaylList = holidayls.getJSONObject(i).getJSONArray("list");
                        for (int j = 0; j < holidaylList.size(); j++) {
                            String date = holidaylList.getJSONObject(j).get("date").toString();
                            cache.put(Main.DateToStr(Main.StrToDate(date)), 1);
                        }
                    }
                }
            }
            if (cache.get(pTime) == null) {
                return 0;
            }
            return 1;
        } catch (Exception e) {
            //e.printStackTrace();
            return HolidayUtil.isWorkDay_(pTime);
        }
    }

    // public static void main(String[] args) {
    //     System.out.println(isWorkDay__("20200114"));
    //     System.out.println(isWorkDay__("20200124"));
    //     // System.out.println(isWorkDay("20180929"));
    //     // System.out.println(isWorkDay("20180930"));
    //     // System.out.println(isWorkDay("20180930"));
    // }

    /**
     * 判断是否工作日(获取本地的配置文件)
     *
     * @param pTime
     * @return
     */
    public static int isWorkDay_(String pTime) {
        int rel = 1;
        try {
            jxl.Workbook wb;

            InputStream is = Config.class.getResourceAsStream("/holiday.xls");

            wb = Workbook.getWorkbook(is);

            Sheet sheet = wb.getSheet(0);
            int row_total = sheet.getRows();
            for (int j = 0; j < row_total; j++) {
                Cell[] cells = sheet.getRow(j);
                String day = cells[0].getContents();
                if (day.equals(pTime)) {
                    rel = 0;
                    break;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        return rel;
    }
}

