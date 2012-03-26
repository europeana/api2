package eu.europeana.api2.web.model.abstracts;

import java.util.Date;

public abstract class ApiResponse {
	
	private String apikey;
	
	private String action;
	
	private boolean success = true;
	
	private Date statsStartTime;

	private long statsDuration = 0;
	
	public boolean getSuccess() {
		return success;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Date getStatsStartTime() {
		return statsStartTime;
	}

	public void setStatsStartTime(Date statsStartTime) {
		this.statsStartTime = statsStartTime;
	}

	public long getStatsDuration() {
		return statsDuration;
	}

	public void setStatsDuration(long statsDuration) {
		this.statsDuration = statsDuration;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
