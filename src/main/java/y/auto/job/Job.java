package y.auto.job;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import y.auto.entity.ClockInfo;
import y.auto.entity.UserInfo;
import y.auto.main.Main;

public class Job implements org.quartz.Job{

	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("机器人开始执行...【"+Main.getNow()+"】");
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

}
