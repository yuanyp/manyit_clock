package y.auto.util;

import java.util.HashMap;
import java.util.Map;

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
				String ret = HttpUtil.sendGet(url, "", "utf-8");
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
	
	public static void main(String[] args) {
		System.out.println(isWorkDay("20180929"));
		System.out.println(isWorkDay("20180929"));
		System.out.println(isWorkDay("20180929"));
		System.out.println(isWorkDay("20180930"));
		System.out.println(isWorkDay("20180930"));
	}
}

