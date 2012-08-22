package com.projity.pm.graphic.spreadsheet;

import java.util.ListIterator;

import com.projity.pm.graphic.views.SearchContext;

public class SpreadSheetSearchContext extends SearchContext {
	int row = -1;
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

}
