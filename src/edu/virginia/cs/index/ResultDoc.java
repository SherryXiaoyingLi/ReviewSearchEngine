package edu.virginia.cs.index;

public class ResultDoc {
	private int id;
	private String title = "[no title]";
	private String reviewText = "[no reviewText]"; // i.e. the reviewText is stored here
    private String reviewerID;
    private String asin;
    private String reviewerName;
    private int[] helpful = new int[2];
    private double overall;
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getReviewText() {
		return reviewText;
	}

	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}

	public String getReviewerID() {
		return reviewerID;
	}

	public void setReviewerID(String reviewerID) {
		this.reviewerID = reviewerID;
	}

	public String getAsin() {
		return asin;
	}

	public void setAsin(String asin) {
		this.asin = asin;
	}

	public String getReviewerName() {
		return reviewerName;
	}

	public void setReviewerName(String reviewerName) {
		this.reviewerName = reviewerName;
	}

	public int[] getHelpful() {
		return helpful;
	}

	public void setHelpful(int[] helpful) {
		this.helpful = helpful;
	}

	public double getOverall() {
		return overall;
	}

	public void setOverall(double overall) {
		this.overall = overall;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getUnixReviewTime() {
		return unixReviewTime;
	}

	public void setUnixReviewTime(String unixReviewTime) {
		this.unixReviewTime = unixReviewTime;
	}

	public String getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(String reviewTime) {
		this.reviewTime = reviewTime;
	}

	private String summary;
    private String unixReviewTime;
    private String reviewTime;

    public ResultDoc(int id) {
        this.id = id;
    }
    
    
   
}
