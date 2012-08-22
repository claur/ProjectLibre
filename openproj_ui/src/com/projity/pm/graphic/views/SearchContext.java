package com.projity.pm.graphic.views;

import com.projity.configuration.Configuration;
import com.projity.field.Field;

public abstract class SearchContext {
	Field field =Configuration.getFieldFromId("Field.name");
	String searchValue="";
	boolean forward = true;
	boolean caseSensitive = false;
	
//	public abstract boolean isDone();
	public boolean matches(Object obj) {
		String val = field.getText(obj, null);
		if (val == null)
			return false;;
		if (!caseSensitive) {
			if (val.toUpperCase().contains(searchValue.toUpperCase()))
				return true;
		} else {
			if (val.contains(searchValue)) {
				return true;
			}
		}
		return false;
	}

	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public String getSearchValue() {
		return searchValue;
	}
	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}
	public boolean isForward() {
		return forward;
	}
	public void setForward(boolean forward) {
		this.forward = forward;
	}
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

}
