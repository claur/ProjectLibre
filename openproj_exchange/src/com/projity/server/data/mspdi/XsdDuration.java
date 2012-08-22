// This file is already in the mpxj distrib. it was copied here for easier debugging as its hard to debug the packaged code

package com.projity.server.data.mspdi;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * This class parses and represents an xsd:duration value.
 */
public final class XsdDuration {
	/**
	 * Constructor. Parses the xsd:duration value and extracts the duration data
	 * from it.
	 * 
	 * @param duration
	 *            value formatted as an xsd:duration
	 */
	XsdDuration(String duration) {
		if (duration != null) {
			int length = duration.length();
			if (length > 0) {
				if (duration.charAt(0) != 'P') {
					if (length < 2
							|| (duration.charAt(0) != '-' && duration.charAt(1) != 'P')) {
						throw new IllegalArgumentException(duration);
					}
				}

				int index;
				boolean negative;
				if (duration.charAt(0) == '-') {
					index = 2;
					negative = true;
				} else {
					index = 1;
					negative = false;
				}

				while (index < length) {
					index = readComponent(duration, index, length);
				}

				if (negative == true) {
					m_years = -m_years;
					m_months = -m_months;
					m_days = -m_days;
					m_hours = -m_hours;
					m_minutes = -m_minutes;
					m_seconds = -m_seconds;
				}
			}
		}
	}

	/**
	 * This constructor allows an xsd:duration to be created from an MPX
	 * duration.
	 * 
	 * @param duration
	 *            An MPX duration.
	 */
	public XsdDuration(net.sf.mpxj.Duration duration) {
		if (duration != null) {
			double amount = duration.getDuration();

			if (amount != 0) {
				switch (duration.getUnits()) {  //claur mpxj now using enum
				case MINUTES:
				case ELAPSED_MINUTES: {
					m_minutes = (int) amount;
					m_seconds = (amount * 60) - (m_minutes * 60);
					break;
				}

				case HOURS:
				case ELAPSED_HOURS: {
					m_hours = (int) amount;
					amount = (amount * 60) - (m_hours * 60);
					m_minutes = (int) amount;
					m_seconds = (amount * 60) - (m_minutes * 60);
					break;
				}

				case DAYS:
				case ELAPSED_DAYS: {
					m_days = (int) amount;
					amount = (amount * 24) - (m_days * 24);
					m_hours = (int) amount;
					amount = (amount * 60) - (m_hours * 60);
					m_minutes = (int) amount;
					m_seconds = (amount * 60) - (m_minutes * 60);
					break;
				}

				case WEEKS:
				case ELAPSED_WEEKS: {
					amount *= 7;
					m_days = (int) amount;
					amount = (amount * 24) - (m_days * 24);
					m_hours = (int) amount;
					amount = (amount * 60) - (m_hours * 60);
					m_minutes = (int) amount;
					m_seconds = (amount * 60) - (m_minutes * 60);
					break;
				}

				case MONTHS:
				case ELAPSED_MONTHS: {
					m_months = (int) amount;
					amount = (amount * 28) - (m_months * 28);
					m_days = (int) amount;
					amount = (amount * 24) - (m_days * 24);
					m_hours = (int) amount;
					amount = (amount * 60) - (m_hours * 60);
					m_minutes = (int) amount;
					m_seconds = (amount * 60) - (m_minutes * 60);
					break;
				}

				case YEARS:
				case ELAPSED_YEARS: {
					m_years = (int) amount;
					amount = (amount * 12) - (m_years * 12);
					m_months = (int) amount;
					amount = (amount * 28) - (m_months * 28);
					m_days = (int) amount;
					amount = (amount * 24) - (m_days * 24);
					m_hours = (int) amount;
					amount = (amount * 60) - (m_hours * 60);
					m_minutes = (int) amount;
					m_seconds = (amount * 60) - (m_minutes * 60);
					break;
				}
				}
			}
		}
	}

/**
 * Convert from a string xsd duration value to a number of milliseconds
 * @param s
 * @return
 */	public static long millis(String s) {
		// most of the time, the value is either 8 hours or 0 hours, so treat these specially for speed
		if (s == null || s.equals("0") || s.equals("PT0H0M0S"))
			return 0L;
		if (s.equals("PT8H0M0S"))
			return 8*60*60*1000L;
		return new XsdDuration(s).getMillis();
	}

	/**
	 * This method is called repeatedly to parse each duration component from
	 * srting data in xsd:duration format. Each component consists of a number,
	 * followed by a letter representing the type.
	 * 
	 * @param duration
	 *            xsd:duration formatted string
	 * @param index
	 *            current position in the string
	 * @param length
	 *            length of string
	 * @return current position in the string
	 */
	private int readComponent2(String duration, int index, int length) {
		char c = 0;
		StringBuffer number = new StringBuffer();

		while (index < length) {
			c = duration.charAt(index);
			if (Character.isDigit(c) == true || c == '.') {
				number.append(c);
			} else {
				break;
			}

			++index;
		}

		switch (c) {
		case 'Y': {
			m_years = Integer.parseInt(number.toString());
			break;
		}

		case 'M': {
			if (m_hasTime == false) {
				m_months = Integer.parseInt(number.toString());
			} else {
				m_minutes = Integer.parseInt(number.toString());
			}
			break;
		}

		case 'D': {
			m_days = Integer.parseInt(number.toString());
			break;
		}

		case 'T': {
			m_hasTime = true;
			break;
		}

		case 'H': {
			m_hours = Integer.parseInt(number.toString());
			break;
		}

		case 'S': {
			m_seconds = Double.parseDouble(number.toString());
			break;
		}

		default: {
			throw new IllegalArgumentException(duration);
		}
		}

		++index;

		return (index);
	}

	/**
	 * Retrieves the number of days.
	 * 
	 * @return int
	 */
	public int getDays() {
		return (m_days);
	}

	/**
	 * Retrieves the number of hours.
	 * 
	 * @return int
	 */
	public int getHours() {
		return (m_hours);
	}

	/**
	 * Retrieves the number of minutes.
	 * 
	 * @return int
	 */
	public int getMinutes() {
		return (m_minutes);
	}

	/**
	 * Retrieves the number of months.
	 * 
	 * @return int
	 */
	public int getMonths() {
		return (m_months);
	}

	/**
	 * Retrieves the number of seconds.
	 * 
	 * @return double
	 */
	public double getSeconds() {
		return (m_seconds);
	}

	/**
	 * Retrieves the number of years.
	 * 
	 * @return int
	 */
	public int getYears() {
		return (m_years);
	}

	/**
	 * This method generates the string representation of an xsd:duration value.
	 * 
	 * @return xsd:duration value
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer("P");
		boolean negative = false;

		if (m_years != 0 || m_months != 0 || m_days != 0) {
			if (m_years < 0) {
				negative = true;
				buffer.append(-m_years);
			} else {
				buffer.append(m_years);
			}
			buffer.append("Y");

			if (m_months < 0) {
				negative = true;
				buffer.append(-m_months);
			} else {
				buffer.append(m_months);
			}
			buffer.append("M");

			if (m_days < 0) {
				negative = true;
				buffer.append(-m_days);
			} else {
				buffer.append(m_days);
			}
			buffer.append("D");
		}

		buffer.append("T");

		if (m_hours < 0) {
			negative = true;
			buffer.append(-m_hours);
		} else {
			buffer.append(m_hours);
		}
		buffer.append("H");

		if (m_minutes < 0) {
			negative = true;
			buffer.append(-m_minutes);
		} else {
			buffer.append(m_minutes);
		}
		buffer.append("M");

		if (m_seconds < 0) {
			negative = true;
			buffer.append(FORMAT.format(-m_seconds));
		} else {
			buffer.append(FORMAT.format(m_seconds));
		}
		buffer.append("S");

		if (negative == true) {
			buffer.insert(0, '-');
		}

		return (buffer.toString());
	}

	private boolean m_hasTime;

	private int m_years;

	private int m_months;

	private int m_days;

	private int m_hours;

	private int m_minutes;

	private double m_seconds;

	public long getMillis() {
		return 1000L * ((long) m_seconds + 60L * (m_minutes + 60L * (m_hours + 24L * (m_days + (28L * m_months + 365L * m_years)

		))));

	}

	/**
	 * Configure the decimal separator to be independent of the one used by the
	 * default locale.
	 */
	private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols();
	static {
		SYMBOLS.setDecimalSeparator('.');
	}

	private static final DecimalFormat FORMAT = new DecimalFormat("#", SYMBOLS);

	
	
	   private int readComponent (String duration, int index, int length)
	   {
	      char c = 0;
	      StringBuffer number = new StringBuffer ();

//	System.out.println("read componenent " + duration);  
//	 if (duration.contains("."))
//		 System.out.println("dot");
	      
	      while (index < length)
	      {
	         c = duration.charAt(index);
	         if (Character.isDigit(c) == true || c == '.')
	         {
	            number.append (c);
	         }
	         else
	         {
	            break;
	         }

	         ++index;
	      }

	      switch (c)
	      {
	         case 'Y':
	         {
//	projity
	        	 m_years = (int) Math.floor(Double.parseDouble(number.toString()));

//	 projity commented        	 m_years = Integer.parseInt(number.toString());
	            break;
	         }

	         case 'M':
	         {
	            if (m_hasTime == false)
	            {
//	projity            	
	           	 m_months = (int) Math.floor(Double.parseDouble(number.toString()));
//	 projity commented               m_months = Integer.parseInt(number.toString());
	            }
	            else
	            {
//	projity
	            	m_minutes += Math.floor(Double.parseDouble(number.toString()));
//	projity commented                m_minutes = Integer.parseInt(number.toString());
	            }
	            break;
	         }

	         case 'D':
	         {
//	projity        
	        	 m_days = (int) Math.floor(Double.parseDouble(number.toString()));
//	projity commented            m_days = Integer.parseInt(number.toString());
	            break;
	         }

	         case 'T':
	         {
	            m_hasTime = true;
	            break;
	         }

	         case 'H':
	         {
//	Projity        
	        	double h = Double.parseDouble(number.toString());
	        	if (Math.round(h) != h) {
	        		m_hours = (int) Math.floor(h);
	        		double min =  (h - m_hours) * 60; 
	        		m_minutes += (int) Math.floor(min);
	        		double sec = (min - m_minutes) * 60;
	        		m_seconds += Math.round(sec);	
	        		
	        	} else {
	        		m_hours = (int) h;
	        	}
	        		
//	projity commented            m_hours = Integer.parseInt(number.toString());
	            break;
	         }

	         case 'S':
	         {
	 //projity       	 
	             m_seconds += Double.parseDouble(number.toString());
//	projity commented            m_seconds = Double.parseDouble(number.toString());
	            break;
	         }

	         default:
	         {
	            throw new IllegalArgumentException (duration);
	         }
	      }

	      ++index;

	      return (index);
	   }
	
	
}
