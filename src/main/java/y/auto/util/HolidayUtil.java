package y.auto.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONObject;

public class HolidayUtil {
	
	public static Map<String,Integer> cache = new HashMap<>();
	
	/**
	 * 判断是否工作日
	 * @param pTime
	 * @return
	 */
	public static int isWorkDay(String pTime){
		try {
			if(null == cache.get(pTime)) {
				String url = "http://api.goseek.cn/Tools/holiday?date=" + pTime;
				String ret = HttpUtil.sendGet(url, "", "UTF-8");
				System.out.println("判断节假日接口返回：" + ret);
				if(StringUtils.isNotBlank(ret)) {
					JSONObject json = JSONObject.fromObject(ret);
					int a = json.getInt("data");
					cache.put(pTime, a);
					return a;
				}
				return 0;
			}else {
				return cache.get(pTime);
			}
		}catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	// public static void main(String[] args) {
	// 	System.out.println(isWorkDay("20180929"));
	// 	System.out.println(isWorkDay("20180929"));
	// 	System.out.println(isWorkDay("20180929"));
	// 	System.out.println(isWorkDay("20180930"));
	// 	System.out.println(isWorkDay("20180930"));
	// }

	/**
	 * 判断是否工作日(获取本地的配置文件)
	 * @param pTime
	 * @return
	 */
	public static int isWorkDay_(String pTime){
		int rel = 1;
		try{
			jxl.Workbook wb;

			InputStream is = Config.class.getResourceAsStream("/holiday.xls");

			wb = Workbook.getWorkbook(is);

			Sheet sheet = wb.getSheet(0);
			int row_total = sheet.getRows();
			for (int j = 0; j < row_total; j++) {
				Cell[] cells = sheet.getRow(j);
				String day = cells[0].getContents();
				if(day.equals(pTime)){
					rel = 0;
					break;
				}
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BiffException e){
			e.printStackTrace();
		}
		return rel;
	}
}

