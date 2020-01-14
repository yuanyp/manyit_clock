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

        System.out.println("�����˿�ʼִ��...");
        String nowDate = Main.getNowDate("YYYYMMdd");

        if(!UserInfo.today.equals(nowDate)){
            UserInfo.today=nowDate;
            UserInfo.start = "";
            UserInfo.end = "";
        }
        
        SEND = Mail.resceive(MAIL_USERNAME,MAIL_PASSWORD,nowDate);
        if(!SEND){
            System.out.println("����������"+nowDate+"�������");
            return;
        }

        //int week = HolidayUtil.isWorkDay(nowDate);
        int week = HolidayUtil.isWorkDay_(nowDate);
        if (week != 0) {
            //System.out.println("���ǹ����գ����򿨡�" + nowDate + "��");
            System.out.println("�޴����񣬲��򿨡�" + nowDate + "��");
            return;
        }
        String start = getStart();
        String end = getEnd();
        if (StringUtils.isBlank(UserInfo.start)) {
            //���ô�ʱ��
            UserInfo.start = start;
            //System.out.println(nowDate + "��ǩ��ʱ��Ԥ��Ϊ��" + start);
        }
        if (StringUtils.isBlank(UserInfo.end)) {
            UserInfo.end = end;
            //System.out.println(nowDate + "��ǩ��ʱ��Ԥ��Ϊ��" + end);
        }


        //��¼
        try {
            String now = Main.getNowMinutes();
            if (UserInfo.start.equals(now) || UserInfo.end.equals(now)) {
                System.out.println("=======================================================================");
                if (!Main.Login(UserInfo.userName, UserInfo.password)) {
                    System.out.println("��¼ʧ��");
                    return;
                }
                System.out.println("��¼�ɹ�..");

                ClockInfo clock = Main.getUserApplyNo();

                //�ж�������
                int amOrpm = amOrpm();
                boolean swdk = (StringUtils.isBlank(clock.getKqStartTime()) && amOrpm == 0);
                boolean xwdk = (amOrpm == 1);
                if(swdk || xwdk){
                    Main.executeClock(clock);
                    if(swdk){
                        System.out.println("�������ɣ�"+UserInfo.start);
                    }

                    if(xwdk){
                        System.out.println("�������ɣ�"+UserInfo.end);
                    }
                }
                Main.logout();
                System.out.println("=======================================================================");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * �жϵ�ǰ���������ڼ�
     *
     * @param pTime ��Ҫ�жϵ�ʱ��
     * @return dayForWeek �жϽ��
     * @Exception �����쳣
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
     * �ж��Ǻ����绹�����磨���Ϊ��0�������� ���Ϊ��1�������磩
     *
     * @return
     */
    public static int amOrpm() {
        GregorianCalendar ca = new GregorianCalendar();
        return ca.get(GregorianCalendar.AM_PM);
    }


    /**
     * ��Χȡ�����
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
        if (r == 0) {//����
        } else {//����
            end = getRandom(Integer.parseInt(endConfig[0]), Integer.parseInt(endConfig[1])) + "";
        }
        return end;
    }

    public static String getStart() {
        String[] startConfig = getStartConfig();
        int r = amOrpm();
        String start = "";
        if (r == 0) {//����
            start = getRandom(Integer.parseInt(startConfig[0]), Integer.parseInt(startConfig[1])) + "";
        }
        return start;
    }
}
