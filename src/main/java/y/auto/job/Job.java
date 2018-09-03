package y.auto.job;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import y.auto.entity.ClockInfo;
import y.auto.entity.UserInfo;
import y.auto.main.Main;

public class Job implements org.quartz.Job{

	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("机器人开始执行...【"+Main.getNow()+"】");
		String nowDate = Main.getNowDate();
		int week = dayForWeek(nowDate);
		if(week == 6 || week == 7) {
			System.out.println("礼拜6-7不执行打卡【"+nowDate+"】");			
		}
		String start = Main.getRandom(20) + "";
		String end = Main.getRandom(15) + "";
		if(start.length() < 2) {
			start = "0" + start;
		}
		if(end.length() < 2) {
			end = "0" + end;
		}
		if(StringUtils.isBlank(UserInfo.start)) {
			//设置打卡时间
			UserInfo.start = start;
			System.out.println(Main.getNowDate()+"的签到时间预计为：" + start);
		}
		if(StringUtils.isBlank(UserInfo.end)) {
			UserInfo.end = end;
			System.out.println(Main.getNowDate()+"的签退时间预计为：" + end);
		}
		//登录
		try {
			String now = Main.getNowMinutes();
			if(UserInfo.start.equals(now) || UserInfo.end.equals(now)) {
				if(!Main.Login(UserInfo.userName,UserInfo.password)) {
					System.out.println("登录失败");
					return;
				}
				System.out.println("登录成功..");
				ClockInfo clock = Main.getUserApplyNo();
				Main.executeClock(clock);
				Main.logout();
				if(UserInfo.start.equals(now)) {
					UserInfo.start = "";
				}
				if(UserInfo.end.equals(now)) {
					UserInfo.end = "";
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 判断当前日期是星期几
	 * 
	 * @param pTime
	 *            修要判断的时间
	 * @return dayForWeek 判断结果
	 * @Exception 发生异常
	 */
	public static int dayForWeek(String pTime) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			c.setTime(format.parse(pTime));
			int dayForWeek = 0;
			if (c.get(Calendar.DAY_OF_WEEK) == 1) {
				dayForWeek = 7;
			} else {
				dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
			}
			return dayForWeek;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
}
