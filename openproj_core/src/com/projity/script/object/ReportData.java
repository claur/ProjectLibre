/*
 * Created on Oct 11, 2007
 *
 * Copyright 2004, Projity Inc.
 */
package com.projity.script.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReportData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7661171774472765421L;
	ArrayList<List<Object>> field = new ArrayList<List<Object>>();

	


	public ArrayList<List<Object>> getField() {
		return field;
	}
	
	public void add(List<Object> row) {
		field.add(row);
	}
}
