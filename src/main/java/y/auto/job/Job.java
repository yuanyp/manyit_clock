package y.auto.job;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import y.auto.entity.ClockInfo;
import y.auto.entity.UserInfo;
import y.auto.main.Main;
import y.auto.util.Config;
import y.auto.util.HolidayUtil;
import y.auto.util.Mail;

public class Job implements org.quartz.Job {

    private static String MAIL_USERNAME=Config.getInstance().getConfig("mailName") + "";
    private static String MAIL_PASSWORD=Config.getInstance().getConfig("mailPassWord") + "";
    private boolean SEND = true;
    public void execute(JobExecutionContext context) throws JobExecutionException {

        //System.out.println("机器人开始执行...");
        String nowDate = Main.getNowDate("YYYYMMdd");

        if(!UserInfo.today.equals(nowDate)){
            UserInfo.today=nowDate;
            UserInfo.start = "";
            UserInfo.end = "";
        }

        SEND = Mail.resceive(MAIL_USERNAME,MAIL_PASSWORD,nowDate);
        if(!SEND){
            System.out.println("邮箱有任务【"+nowDate+"】无需打卡");
            return;
        }

        //int week = HolidayUtil.isWorkDay(nowDate);
        int week = HolidayUtil.isWorkDay__(nowDate);
        if (week != 0) {
            System.out.println("不是工作日，不打卡【" + nowDate + "】");
            //System.out.println("无打卡任务，不打卡【" + nowDate + "】");
            return;
        }
        String start = getStart();
        String end = getEnd();
        if (StringUtils.isBlank(UserInfo.start)) {
            //设置打卡时间
            UserInfo.start = start;
            //System.out.println(nowDate + "的签到时间预计为：" + start);
        }
        if (StringUtils.isBlank(UserInfo.end)) {
            UserInfo.end = end;
            //System.out.println(nowDate + "的签退时间预计为：" + end);
        }


        //登录
        try {

            //判断上下午
            int amOrpm = amOrpm();
            String now = Main.getNowMinutes();
            if ((UserInfo.start.equals(now)  && amOrpm == 0) || (UserInfo.end.equals(now) && amOrpm == 1 )) {
                if (!Main.Login(UserInfo.userName, UserInfo.password)) {
                    System.out.println("登录失败");
                    return;
                }
                System.out.println("登录成功..");

                ClockInfo clock = Main.getUserApplyNo();


                boolean swdk = (StringUtils.isBlank(clock.getKqStartTime()) && amOrpm == 0);
                boolean xwdk = (amOrpm == 1);
                if(swdk || xwdk){
                    Main.executeClock(clock);
                    if(swdk){
                        System.out.println("=======================================================================");
                        System.out.println("上午打卡完成（随机分钟"+UserInfo.start+"）打卡时间："+Main.getNow());
                    }

                    if(xwdk){
                        System.out.println("下午打卡完成（随机分钟"+UserInfo.end+"）打卡时间："+Main.getNow());
                        System.out.println("=======================================================================");
                    }
                }
                Main.logout();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 判断当前日期是星期几
     *
     * @param pTime 修要判断的时间
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 判断是和上午还是下午（结果为“0”是上午 结果为“1”是下午）
     *
     * @return
     */
    public static int amOrpm() {
        GregorianCalendar ca = new GregorianCalendar();
        return ca.get(GregorianCalendar.AM_PM);
    }


    /**
     * 范围取随机数
     *
     * @return
     */
    public static int getRandom(int min, int max) {
        int ran2 = (int) (min+Math.random() * (max - min+1));
        return ran2;
    }

    public static String[] getRandomConfig(int index) {
        String random = Config.getInstance().getConfig("random") + "";
        if (StringUtils.isNotBlank(random)) {
            return random.split(",")[index].split("-");
        }
        return null;
    }

    public static String[] getStartConfig() {
        return getRandomConfig(0);
    }

    public static String[] getEndConfig() {
        return getRandomConfig(1);
    }

    public static String getEnd() {
        String[] endConfig = getEndConfig();
        int r = amOrpm();
        String end = "";
        if (r == 0) {//上午
        } else {//下午
            end = getRandom(Integer.parseInt(endConfig[0]), Integer.parseInt(endConfig[1])) + "";
        }
        return end;
    }

    public static String getStart() {
        String[] startConfig = getStartConfig();
        int r = amOrpm();
        String start = "";
        if (r == 0) {//上午
            start = getRandom(Integer.parseInt(startConfig[0]), Integer.parseInt(startConfig[1])) + "";
        }
        return start;
    }
}
