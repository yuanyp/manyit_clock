package y.auto.entity;

/**
 * 当日打卡信息
 * @author yuanyp
 *"dataMap":{"PMTIME":"18:00","KQ_STARTTIME":"09:00","AMTIME":"09:00","KQ_ENDTIME":""}
 */
public class ClockInfo {
	
	private String pmTime;
	
	private String amTime;
	
	private String kqStartTime;
	
	private String kqEndTime;

	public String getPmTime() {
		return pmTime;
	}

	public void setPmTime(String pmTime) {
		this.pmTime = pmTime;
	}

	public String getAmTime() {
		return amTime;
	}

	public void setAmTime(String amTime) {
		this.amTime = amTime;
	}

	public String getKqStartTime() {
		return kqStartTime;
	}

	public void setKqStartTime(String kqStartTime) {
		this.kqStartTime = kqStartTime;
	}

	public String getKqEndTime() {
		return kqEndTime;
	}

	public void setKqEndTime(String kqEndTime) {
		this.kqEndTime = kqEndTime;
	}

	@Override
	public String toString() {
		return amTime + "," + pmTime + ";签到时间：" + kqStartTime+",签退时间：" + kqEndTime;
	}
}
