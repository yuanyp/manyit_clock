package y.auto.main;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.google.gson.Gson;

import y.auto.entity.ClockInfo;
import y.auto.entity.UserInfo;
import y.auto.job.Job;
import y.auto.util.CookieManager;
import y.auto.util.HttpUtil;

public class Main {
	
	public static String basePath = "https://oa.many-it.com/MANYIT/";
	
	public static String defaultBasePath = "https://oa.many-it.com/MANYIT/";
	
	public static String url = basePath + "Login.do?_funccode_=C_Login";
	
	public static CookieManager cookieManager;
	public static Map<String, Object> requestPropertys;
	
	/**
	 * 登录
	 * @param name
	 * @param pass
	 * @return
	 * @throws Exception 
	 */
	public static boolean Login(String name,String pass) throws Exception {
		if(StringUtils.isBlank(name) || StringUtils.isBlank(pass)) {
			return false;
		}
		String params = "name=" + name + "&pass=" + pass + "&async=true";
		requestPropertys = new HashMap<String, Object>();
		cookieManager = new CookieManager();
		URL _url = new URL(basePath);
		// getting cookies: 
		URLConnection conn = _url.openConnection();
		conn.connect(); 
		cookieManager.storeCookies(conn);//先存储cookie,然后后续所有请求带着cookie
		String ret = HttpUtil.send("post", url, params, requestPropertys, HttpUtil.ENC_UTF_8, cookieManager);
		if(StringUtils.isBlank(ret)){
			return false;
		}
		Gson gson = new Gson();
		Map<String,Object> info = gson.fromJson(ret, Map.class);
		if(null == info && info.size() <=0) {
			return false;
		}
		if(!success(info)) {
			return false;
		}
		UserInfo.userName = name;
		UserInfo.password = pass;
		return true;
	}
	
	public static String obj2Str(Object o) {
		if(null == o) {
			return "";
		}else {
			return o.toString();
		}
	}
	
	/**
	 * 获取考勤记录
	 */
	public static ClockInfo getUserApplyNo() {
		System.out.println("开始获取考勤记录..");
		String params = "action=getWorkSetTimeData&_page_request_=1&_records_perpage_=999";
		String url = basePath + "AsyncAction.do?_funccode_=C_KQ_GetUserApplyNo";
		String ret = HttpUtil.send("get", url, params, requestPropertys, HttpUtil.ENC_UTF_8, cookieManager);
		Gson gson = new Gson();
		Map<String,Object> info = gson.fromJson(ret, Map.class);
		if(null == info && info.size() <=0) {
			return null;
		}
		if(!success(info)) {
			return null;
		}
		info = (Map<String, Object>) info.get("dataMap");
		ClockInfo clockInfo = new ClockInfo();
		clockInfo.setAmTime(obj2Str(info.get("AMTIME")));
		clockInfo.setPmTime(obj2Str(info.get("PMTIME")));
		clockInfo.setKqStartTime(obj2Str(info.get("KQ_STARTTIME")));
		clockInfo.setKqEndTime(obj2Str(info.get("KQ_ENDTIME")));
		System.out.println("获取结果：" + clockInfo.toString());
		return clockInfo;
	}
	
	/**
	 * 1. 登录
	 * 2. 查询考勤记录
	 * 3. 检查是否未打卡
	 * 4. 退出
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner(new InputStreamReader(System.in));
		String name = "";
		do {
			System.out.print("Username: ");
			name = scanner.nextLine();
		}while(StringUtils.isBlank(name));
		String pass = "";
		do {
			System.out.print("Password: ");
			pass = scanner.nextLine();	
		}while(StringUtils.isBlank(pass));
		System.out.print("URL: ");
		basePath = scanner.nextLine();
		if(StringUtils.isBlank(basePath)) {
			basePath = defaultBasePath;
		}
		System.out.println(basePath);
		//登录
		if(!Login(name,pass)) {
			System.out.println("登录失败");
			return;
		}
		System.out.println("登录成功（验证用户名密码通过）..");
//		postClock();
		//[0 * * * * ?] 每1分钟触发一次
		runJob("0 0/1 8,18 * * ?");//[]在每天上午8点到8:55期间和下午6点到6:55期间的每1分钟触发
	}
	
	/**
	 * 执行打卡
	 * @param clock
	 */
	public static void executeClock(ClockInfo clock) {
		System.out.println("自动打卡开始..");
		boolean ret = false;
		String a = StringUtils.isBlank(clock.getKqStartTime()) ? "未签到": clock.getKqStartTime();
		String b = StringUtils.isBlank(clock.getKqEndTime()) ? "未签退": clock.getKqEndTime();
		System.out.println("签到时间：" + a);
		System.out.println("签退时间：" + b);
		ret = postClock();
		System.out.println("自动打卡结束("+ret+")..");
	}
	
	public static String getNow() {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}
	
	public static String getNowDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
		return sdf.format(new Date());
	}
	
	public static String getNowMinutes() {
		SimpleDateFormat sdf = new SimpleDateFormat("mm");
		return sdf.format(new Date());
	}
	
	/**
	 * 退出
	 */
	public static void logout() {
		System.out.println("执行退出...");
		String url = basePath + "Logout.do?_funccode_=C_Logout";
		String ret = HttpUtil.send("get", url, "", requestPropertys, HttpUtil.ENC_UTF_8, cookieManager);
	}
	
	/**
	 * 判断接口的调用是否成功
	 * @param info
	 * @return
	 */
	public static boolean success(Map<String,Object> info) {
		if(null == info && info.size() <=0) {
			return false;
		}else {
			System.out.println("接口返回信息：" + info);
			String code = obj2Str(info.get("_returncode_"));
			if(StringUtils.isBlank(code)) {
				code = obj2Str(info.get("errcode"));
			}
			return "0".equals(code) || "0.0".equals(code) || info.containsKey("LOGINNAME");
		}
	}
	
	/**
	 * (1-60取随机数)
	 * @return
	 */
	public static int getRandom(int condition) {
		Random random = new Random();
		int num = random.nextInt(60);
		if(num < condition) {
			return getRandom(condition);
		}
		return num;
	}
	

	/**
	 * 发送请求执行签到和签退
	 * @return
	 */
	public static boolean postClock() {
		try {
			System.out.println("发送请求执行打卡,当前时间" + getNow());
			String url = basePath + "mobile/kq/mobilesignwork";
			String addr = URLEncoder.encode("中国上海市长宁区长宁路1027号","utf-8");
			String param = "mobileinfo={\"type\":\"gps\",\"longitude\":121.42449600000000,\"latitude\":31.22365500000000,\"address\":\""+addr+"\",\"machine_key\":\"ac145e085f2a896\"}";
			requestPropertys.put("user-agent", "MANYIT-MobileOA-Android");
			String ret = HttpUtil.send("post", url, param, requestPropertys, HttpUtil.ENC_UTF_8, cookieManager);
			Gson gson = new Gson();
			Map<String,Object> info = gson.fromJson(ret, Map.class);
			return success(info);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 初始化机器人
	 * @param cron
	 * @throws Exception
	 */
	public static void runJob(String cron1) throws Exception {
		System.out.println("初始化机器人....");
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

		Scheduler sched = schedFact.getScheduler();

		sched.start();

		// define the job and tie it to our HelloJob class
		JobDetail job1 = JobBuilder.newJob(Job.class).withIdentity("myJob1", "group1").build();
		
		// Trigger the job to run now, and then every 40 seconds
		Trigger trigger1 = TriggerBuilder.newTrigger()
				.withIdentity("myTrigger1", "group1")
				.startNow()
				.withSchedule(CronScheduleBuilder.cronSchedule(cron1)).build();
		
		// Tell quartz to schedule the job using our trigger
		sched.scheduleJob(job1, trigger1);
	}
	
}
