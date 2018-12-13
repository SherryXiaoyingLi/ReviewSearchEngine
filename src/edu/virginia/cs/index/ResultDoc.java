package edu.virginia.cs.index;

import java.util.ArrayList;

public class ResultDoc {
	private int id;
	private String content = "[no content]";
    private ArrayList<Double> weights;
    private String productID;
    
	private String overall;
    public ResultDoc(int id) {
    		this.id = id;
    }
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public ArrayList<Double> getWeights() {
		return weights;
	}
	public void setWeights(ArrayList<Double> weights) {
		this.weights = weights;
	}
	public String getProductID() {
		return productID;
	}
	public void setProductID(String productID) {
		this.productID = productID;
	}
	public String getOverall() {
		return overall;
	}
	public void setOverall(String overall) {
		this.overall = overall;
	}
}
    
    