package com.projity.timescale;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.projity.strings.Messages;

public class ExtendedDateFormat extends SimpleDateFormat {
	protected boolean quarter,half,normal;
	protected String text;

	public ExtendedDateFormat() {
		super();
	}

	public ExtendedDateFormat(String pattern, DateFormatSymbols formatSymbols) {
		super(pattern, formatSymbols);
	}

	public ExtendedDateFormat(String pattern, Locale locale) {
		super(pattern, locale);
	}

	public ExtendedDateFormat(String pattern) {
		super(pattern);
	}

	
	@Override
	public void applyPattern(String pattern) {
		quarter=false;
		half=false;
		normal=false;
		if (pattern.startsWith("Q")){
			int l=pattern.startsWith("QQ")?2:1;
			text=Messages.getString("Date.Quarter"+l);
			if (l==2) text+=" ";
			pattern=pattern.substring(l);
			quarter=true;
		}else if (pattern.startsWith("L")){
			int l=pattern.startsWith("LL")?2:1;
			text=Messages.getString("Date.Half"+l);
			if (l==2) text+=" ";
			pattern=pattern.substring(l);
			half=true;
		}
		if (pattern.length()>0){
			super.applyPattern(pattern);
			normal=true;
		}
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo,
			FieldPosition pos) {
		if (quarter||half){
			calendar.setTime(date);
			int month=calendar.get(Calendar.MONTH);
			toAppendTo.append(text).append(month/(quarter?3:6)+1);
		}
		if (normal) super.format(date, toAppendTo, pos);
		return toAppendTo;
	}


}
