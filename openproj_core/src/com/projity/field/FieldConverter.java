/*
The contents of this file are subject to the Common Public Attribution License 
Version 1.0 (the "License"); you may not use this file except in compliance with 
the License. You may obtain a copy of the License at 
http://www.projity.com/license . The License is based on the Mozilla Public 
License Version 1.1 but Sections 14 and 15 have been added to cover use of 
software over a computer network and provide for limited attribution for the 
Original Developer. In addition, Exhibit A has been modified to be consistent 
with Exhibit B.

Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
specific language governing rights and limitations under the License. The 
Original Code is OpenProj. The Original Developer is the Initial Developer and 
is Projity, Inc. All portions of the code written by Projity are Copyright (c) 
2006, 2007. All Rights Reserved. Contributors Projity, Inc.

Alternatively, the contents of this file may be used under the terms of the 
Projity End-User License Agreeement (the Projity License), in which case the 
provisions of the Projity License are applicable instead of those above. If you 
wish to allow use of your version of this file only under the terms of the 
Projity License and not to allow others to use your version of this file under 
the CPAL, indicate your decision by deleting the provisions above and replace 
them with the notice and other provisions required by the Projity  License. If 
you do not delete the provisions above, a recipient may use your version of this 
file under either the CPAL or the Projity License.

[NOTE: The text of this license may differ slightly from the text of the notices 
in Exhibits A and B of the license at http://www.projity.com/license. You should 
use the latest text at http://www.projity.com/license for your modifications.
You may not remove this license text from the source files.]

Attribution Information: Attribution Copyright Notice: Copyright © 2006, 2007 
Projity, Inc. Attribution Phrase (not exceeding 10 words): Powered by OpenProj, 
an open source solution from Projity. Attribution URL: http://www.projity.com 
Graphic Image as provided in the Covered Code as file:  openproj_logo.png with 
alternatives listed on http://www.projity.com/logo

Display of Attribution Information is required in Larger Works which are defined 
in the CPAL as a work which combines Covered Code or portions thereof with code 
not governed by the terms of the CPAL. However, in addition to the other notice 
obligations, all copies of the Covered Code in Executable and Source Code form 
distributed must, as a form of attribution of the original author, include on 
each user interface screen the "OpenProj" logo visible to all users.  The 
OpenProj logo should be located horizontally aligned with the menu bar and left 
justified on the top left of the screen adjacent to the File menu.  The logo 
must be at least 100 x 25 pixels.  When users click on the "OpenProj" logo it 
must direct them back to http://www.projity.com.  
*/
package com.projity.field;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import com.projity.datatype.Duration;
import com.projity.datatype.DurationFormat;
import com.projity.datatype.Money;
import com.projity.datatype.Work;
import com.projity.options.EditOption;
import com.projity.strings.Messages;
import com.projity.util.DateTime;
/**
 * This class decorates ConvertUtils to use Projity specific types and validation
 */
public class FieldConverter  {
	HashMap<FieldContext,HashMap<Class,Converter>> contextMaps = new HashMap<FieldContext,HashMap<Class,Converter>>();
	private StringConverter stringConverter;
	private StringConverter compactStringConverter;
	
	public static String toString(Object value, Class clazz, FieldContext context ) {
		return getInstance()._toString(value,clazz,context);
	}
	public static String toString(Object value ) {
		return getInstance()._toString(value,value.getClass(),null);
	}
	public static Object fromString(String value, Class clazz) {
		return ConvertUtils.convert(value, clazz);
	}

	
	/**
	 * Convert from an object, usually a string, into another object
	 * @param value.  Convert from this value
	 * @param clazz. Convert to this clazz type.
	 * @param context Converter context to use 
	 * @return object of type clazz.
	 * @throws FieldParseException
	 */
	public static Object convert(Object value, Class clazz, FieldContext context) throws FieldParseException {
		return getInstance()._convert(value,clazz,context);
	}
        
	
	private static FieldConverter instance = null;
	public static FieldConverter getInstance() {
		if (instance == null)
			instance = new FieldConverter();
		return instance;
	}
	public static void reinitialize() {
		instance = null;
	}

	/**
	 * 
	 * @param value.  Convert from this value
	 * @param clazz. Convert to this clazz type.
	 * @return object of type clazz.
	 * @throws FieldParseException
	 */
	private Object _convert(Object value, Class clazz, FieldContext context) throws FieldParseException {
		try {
			if (value instanceof String) { 
				Object result = null;
				if (context == null)
					result = ConvertUtils.convert((String) value,clazz);
				else {
					Converter contextConverter = null;
					HashMap<Class,Converter> contextMap = contextMaps.get(context);
					if (contextMap != null)
						contextConverter = contextMap.get(clazz);
					if (contextConverter != null) {
						contextConverter.convert(clazz,value);
					} else {
						System.out.println("no context converter found ");
						result = ConvertUtils.convert((String) value,clazz);
					}
				}
	//			if (result instanceof java.util.Date) { //  dates need to be normalized
	//				result = new Date(DateTime.gmt((Date) result));
	//			}
				if (result == null) {
					throw new FieldParseException("Invalid type");
				}
				return result;
			}	
	
			// Because of stupidity of beanutils which assumes type string, I implement this by hand
			Converter converter = ConvertUtils.lookup(clazz);                       
			if (converter == null) {                         
				System.out.println("converter is null for class " + clazz + " instance " + instance.hashCode() + " resetting") ;
				instance = new FieldConverter();
				converter = ConvertUtils.lookup(String.class);  
			} 
			return converter.convert(clazz, value);
		} catch (ConversionException conversionException) {
			throw new FieldParseException(conversionException);
		}
	}
        
	
	
	private String _toString(Object value, Class clazz, FieldContext context) {
		if (context == COMPACT_CONVERTER_CONTEXT)
			return (String) compactStringConverter.convert(clazz, value);
		else
			return (String) stringConverter.convert(clazz, value);
	}
	

	public static final FieldContext COMPACT_CONVERTER_CONTEXT=new FieldContext();
	static {
		COMPACT_CONVERTER_CONTEXT.setCompact(true);
	}
	
	
	private FieldConverter() {
		instance = this;
		stringConverter = new StringConverter(false);
		compactStringConverter = new StringConverter(true);
		ConvertUtils.register(stringConverter, String.class);   // Wrapper class
		ConvertUtils.register(new DateConverter(), Date.class);   // Wrapper class
		ConvertUtils.register(new CalendarConverter(), GregorianCalendar.class);   // Wrapper class
		ConvertUtils.register(new DurationConverter(), Duration.class);   // Wrapper class
		ConvertUtils.register(new WorkConverter(), Work.class);   // Wrapper class
		ConvertUtils.register(new MoneyConverter(), Money.class);   // Wrapper class
		Converter longConverter = new LongConverter();
		ConvertUtils.register(longConverter, Long.TYPE);    // Native type
		ConvertUtils.register(longConverter, Long.class);   // Wrapper class
		Converter doubleConverter = new DoubleConverter();
		ConvertUtils.register(doubleConverter, Double.TYPE);    // Native type
		ConvertUtils.register(doubleConverter, Double.class);   // Wrapper class
		

		// short context converters
		HashMap<Class,Converter> compactMap = new HashMap<Class,Converter>();
		contextMaps.put(COMPACT_CONVERTER_CONTEXT, compactMap);
		compactMap.put(String.class,compactStringConverter);
		// no need for duration or money as parsing is done in long form
		
	}
	private static class StringConverter implements Converter {
		private boolean compact = false;
		StringConverter(boolean compact) {
			this.compact = compact;
		}
		public Object convert(Class clazz, Object value) {
			if (value instanceof Work) {
				if (compact) 
					return ((DurationFormat)DurationFormat.getWorkInstance()).formatCompact(value);
				else 
					return ((DurationFormat)DurationFormat.getWorkInstance()).format(value);
			} else if (value instanceof Duration) {
				if (compact) 
					return ((DurationFormat)DurationFormat.getInstance()).formatCompact(value);
				else 
					return ((DurationFormat)DurationFormat.getInstance()).format(value);
			} else if (value instanceof Money) {
				return Money.formatCurrency(((Money)value).doubleValue(),compact);
			} else if (value instanceof Date) {
				if (value.equals(DateTime.getZeroDate()))
					return null;
				return EditOption.getInstance().getDateFormat().format(value);
			} else {
				if (value == null)
					return null;
				else
					return value.toString();
			}
		}
	}
	// make a converter for long that can process dates and durations
	private static class LongConverter implements Converter {
		Converter baseConverter = new org.apache.commons.beanutils.converters.LongConverter(); 
		public Object convert(Class type, Object value) throws ConversionException {
			if (value == null)
				return null;
			if (value != null) {
				if (value instanceof Date) {
					return new Long(((Date)value).getTime());
				} else if (value instanceof GregorianCalendar) {
					return new Long(((GregorianCalendar)value).getTimeInMillis());
				} else if (value instanceof Duration || value instanceof Work) {
					return new Long(((Duration)value).getEncodedMillis());
				}
			}
			return baseConverter.convert(type,value);
		}
	};
	
	private static class DateConverter implements Converter {
		public Object convert(Class type, Object value) throws ConversionException {
			if (value == null)
				return null;
			if (value instanceof Long) {
				long longValue =  ((Long)value).longValue();
				if (longValue == 0)
					return null;
				return new Date(longValue);
			} else if (value instanceof Date) {
				return value;
			} else if (value instanceof Calendar) {
				return ((Calendar)value).getTime();
			} else if (value instanceof String) {
				try {
					return EditOption.getInstance().getDateFormat().parse((String)value);
				} catch (ParseException e) {
					try {
						return DateTime.utcShortDateFormatInstance().parse((String)value); // try without time
					} catch (ParseException e1) {
						throw new ConversionException(Messages.getString("Message.invalidDate"));
					}
				}
			}

			throw new ConversionException("Error: no conversion from " + value.getClass().getName() + " to " + type.getName() + " for value" + value);
		}
	};		
		
	// GregorianCalendar converter
	private static class CalendarConverter implements Converter {
		private static DateConverter dateConverter = new DateConverter();
		public Object convert(Class type, Object value) throws ConversionException {
			GregorianCalendar cal = DateTime.calendarInstance();
			if (value == null) {
				return null;
			} else if (value instanceof Long) {
				long longValue =  ((Long)value).longValue();
				if (longValue == 0)
					return null;
		
				cal.setTimeInMillis(longValue);
				return cal;
			} else if (value instanceof Date) {
				cal.setTime((Date)value);
				return cal;
			} else if (value instanceof String) {
				Date d = (Date) dateConverter.convert(Date.class,value);
				cal.setTime(d);
				return cal;
			}
			throw new ConversionException("Error: no conversion from " + value.getClass().getName() + " to " + type.getName() + " for value" + value);
		}
	};		
	private static class DurationConverter implements Converter {
		public Object convert(Class type, Object value) throws ConversionException {
			if (value == null)
				return Duration.getInstanceFromDouble(null);
			
			if (value instanceof Number) {
				return new Duration(((Number)value).longValue());
			} else if (value instanceof Work) {
				return new Duration(((Work)value).longValue());
			} else if (value instanceof Duration) {
				return value;
			} else if (value instanceof String) {
				try {
					return DurationFormat.getInstance().parseObject((String) value);
				} catch (ParseException e) {
					throw new ConversionException(Messages.getString("Message.invalidDuration"));
				}
			}
			throw new ConversionException("Error: no conversion from " + value.getClass().getName() + " to " + type.getName() + " for value" + value);
		}
	};		

	private static class WorkConverter implements Converter {
		public Object convert(Class type, Object value) throws ConversionException {
			if (value == null)
				return Duration.getInstanceFromDouble(null);
			
			if (value instanceof Number) {
				return new Work(((Number)value).longValue());
			} else if (value instanceof Work) {
				return new Work(((Work)value).longValue());
			} else if (value instanceof Duration) {
				return value;
			} else if (value instanceof String) {
				try {
					return DurationFormat.getWorkInstance().parseObject((String) value);
				} catch (ParseException e) {
					throw new ConversionException(Messages.getString("Message.invalidDuration"));
				}
			}
			throw new ConversionException("Error: no conversion from " + value.getClass().getName() + " to " + type.getName() + " for value" + value);
		}
	};		
	private static class DoubleConverter implements Converter {
		Converter baseConverter = new org.apache.commons.beanutils.converters.DoubleConverter(); 
		public Object convert(Class type, Object value) throws ConversionException {
			if (value != null) {
				if (value instanceof Double) {
					return value;
				} else if (value instanceof Money) {
					double num = ((Number)value).doubleValue();
				 	if (Double.isInfinite(num) || Double.isNaN(num)) {
				 		System.out.println("Error: number is invalid double in MoneyConverter " +value );
				 		num = 0.0;
				 	}
					return new Double(num);
				}
			}
			return baseConverter.convert(type,value);
		}
	};

	/* TODO I have also experimented with the JADE library's Money class.  It is probably more useful
	 * for performing currency conversions than as a datatype.  A possible source for currency exchange rates is the 
	 * web service here: 
	 * http://www.bindingpoint.com/service.aspx?skey=377e6659-061f-4956-8edb-19b5023bc33b
	 *  
	 */
	private static class MoneyConverter implements Converter {
		public Object convert(Class type, Object value) throws ConversionException {
			if (value == null)
				return Money.getInstance(0);
			if (value instanceof Money) {
				return value;
			} else if (value instanceof Number) {
				double num = ((Number)value).doubleValue();
			 	if (Double.isInfinite(num) || Double.isNaN(num)) {
			 		System.out.println("Error: number is invalid double in MoneyConverter " +value );
			 		num = 0.0;
			 	}
				return Money.getInstance(num);
			} else if (value instanceof String) {
				try {
					return Money.getFormat(false).parseObject((String) value);
				} catch (ParseException e) {
					throw new ConversionException(Messages.getString("Message.invalidDuration"));
				}
			}
			throw new ConversionException("Error: no conversion from " + value.getClass().getName() + " to " + type.getName() + " for value" + value);
		}
	}
}
