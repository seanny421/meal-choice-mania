package dev.sean.mealchoicemania;

public class PollOption {
	private int pollid;
	private int id;
	private String option_text;
	
	public PollOption(int id, int pollid, String option_text) {
		this.id = id;
		this.pollid = pollid;
		this.option_text = option_text;
	}
	
	public int getId() {
		return id;
	}
	public int getPollId() {
		return pollid;
	}
	public String getOptionText() {
		return option_text;
	}

}
