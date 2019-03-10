/*******************************************************************************
 * The contents of this file are subject to the Common Public Attribution License 
 * Version 1.0 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at 
 * http://www.projectlibre.com/license . The License is based on the Mozilla Public 
 * License Version 1.1 but Sections 14 and 15 have been added to cover use of 
 * software over a computer network and provide for limited attribution for the 
 * Original Developer. In addition, Exhibit A has been modified to be consistent 
 * with Exhibit B. 
 *
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
 * specific language governing rights and limitations under the License. The 
 * Original Code is ProjectLibre. The Original Developer is the Initial Developer 
 * and is ProjectLibre Inc. All portions of the code written by ProjectLibre are 
 * Copyright (c) 2012-2019. All Rights Reserved. All portions of the code written by 
 * ProjectLibre are Copyright (c) 2012-2019. All Rights Reserved. Contributor 
 * ProjectLibre, Inc.
 *
 * Alternatively, the contents of this file may be used under the terms of the 
 * ProjectLibre End-User License Agreement (the ProjectLibre License) in which case 
 * the provisions of the ProjectLibre License are applicable instead of those above. 
 * If you wish to allow use of your version of this file only under the terms of the 
 * ProjectLibre License and not to allow others to use your version of this file 
 * under the CPAL, indicate your decision by deleting the provisions above and 
 * replace them with the notice and other provisions required by the ProjectLibre 
 * License. If you do not delete the provisions above, a recipient may use your 
 * version of this file under either the CPAL or the ProjectLibre Licenses. 
 *
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text of the notices 
 * in the Source Code files of the Original Code. You should use the text of this 
 * Exhibit A rather than the text found in the Original Code Source Code for Your 
 * Modifications.] 
 *
 * EXHIBIT B. Attribution Information for ProjectLibre required
 *
 * Attribution Copyright Notice: Copyright (c) 2012-2019, ProjectLibre, Inc.
 * Attribution Phrase (not exceeding 10 words): 
 * ProjectLibre, open source project management software.
 * Attribution URL: http://www.projectlibre.com
 * Graphic Image as provided in the Covered Code as file: projectlibre-logo.png with 
 * alternatives listed on http://www.projectlibre.com/logo 
 *
 * Display of Attribution Information is required in Larger Works which are defined 
 * in the CPAL as a work which combines Covered Code or portions thereof with code 
 * not governed by the terms of the CPAL. However, in addition to the other notice 
 * obligations, all copies of the Covered Code in Executable and Source Code form 
 * distributed must, as a form of attribution of the original author, include on 
 * each user interface screen the "ProjectLibre" logo visible to all users. 
 * The ProjectLibre logo should be located horizontally aligned with the menu bar 
 * and left justified on the top left of the screen adjacent to the File menu. The 
 * logo must be at least 144 x 31 pixels. When users click on the "ProjectLibre" 
 * logo it must direct them back to http://www.projectlibre.com. 
 *******************************************************************************/
// This file is already in the mpxj distrib. it was copied here for easier debugging as its hard to debug the packaged code

package com.projectlibre1.server.data.mspdi;

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
//	projectlibre
	        	 m_years = (int) Math.floor(Double.parseDouble(number.toString()));

//	 projectlibre commented        	 m_years = Integer.parseInt(number.toString());
	            break;
	         }

	         case 'M':
	         {
	            if (m_hasTime == false)
	            {
//	projectlibre            	
	           	 m_months = (int) Math.floor(Double.parseDouble(number.toString()));
//	 projectlibre commented               m_months = Integer.parseInt(number.toString());
	            }
	            else
	            {
//	projectlibre
	            	m_minutes += Math.floor(Double.parseDouble(number.toString()));
//	projectlibre commented                m_minutes = Integer.parseInt(number.toString());
	            }
	            break;
	         }

	         case 'D':
	         {
//	projectlibre        
	        	 m_days = (int) Math.floor(Double.parseDouble(number.toString()));
//	projectlibre commented            m_days = Integer.parseInt(number.toString());
	            break;
	         }

	         case 'T':
	         {
	            m_hasTime = true;
	            break;
	         }

	         case 'H':
	         {
//	projectlibre        
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
	        		
//	projectlibre commented            m_hours = Integer.parseInt(number.toString());
	            break;
	         }

	         case 'S':
	         {
	 //projectlibre       	 
	             m_seconds += Double.parseDouble(number.toString());
//	projectlibre commented            m_seconds = Double.parseDouble(number.toString());
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
