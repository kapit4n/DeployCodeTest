package com.salesforce.deploy;

import com.sforce.soap.metadata.CodeCoverageResult;

/**
 * 
 * @author Luis Arce
 *
 */
public class CodeCoverageComparable implements Comparable<CodeCoverageComparable> {
	
	private CodeCoverageResult data;
	
	private double percent;
	
	/**
	 * 
	 * @param data
	 */
	public CodeCoverageComparable(CodeCoverageResult data){
		this.data = data;
		percent = 100;
		if(data.getNumLocations() > 0) {
			percent -= (((double)data.getLocationsNotCovered().length / (double)data.getNumLocations()) * 100.0);
		}
	}
	
	@Override
	public int compareTo(CodeCoverageComparable o) {
		return (int)(percent*100.0) - (int)(o.percent*100.0);
	}
	/**
	 * 
	 * @return
	 */
	public CodeCoverageResult getData()
	{
		return data;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getPercent()
	{
		return percent;
	}
	
	
	
}
