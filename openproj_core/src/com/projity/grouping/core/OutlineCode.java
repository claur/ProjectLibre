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
package com.projity.grouping.core;
import java.util.ArrayList;
/**
 *
 */

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

import org.apache.commons.lang.StringUtils;

/**
 * Class used in converting outline codes to hierarchy
 */
public class OutlineCode extends Format {
	static final int NUMBERS = 0;
	static final int UPPERCASE_LETTERS = 1;
	static final int LOWERCASE_LETTERS = 2;
	static final int CHARACTERS = 3;
	private static final int ANY_LENGTH = 0;
	
	private ArrayList masks = new ArrayList();
	private transient Pattern pattern = null;

	/* (non-Javadoc)
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	public Object parseObject(String code, ParsePosition pos) {
		Object result = null;
		Iterator i = masks.iterator();
		String current = code.substring(pos.getIndex());
		Matcher matcher = pattern.matcher(current);
		if (matcher.matches()) {
			pos.setIndex(pos.getIndex() + matcher.end());
			return current;
		}
		else
			return null;
	}

	public StringBuffer format(Object arg0, StringBuffer arg1, FieldPosition arg2) {
		
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isValid(String code) {
		try {
			parseObject(code);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}
	
	public void addMask(Mask mask) {
		masks.add(mask);
		rebuildPattern();
	}
	
	
	private void rebuildPattern() {
		Iterator i = masks.iterator();
		pattern = Pattern.compile(getPattern(i,""));
	}
	
	/**
	 * Recursively build the pattern.  Any given sublevel is made optional for its parent
	 * @param i
	 * @return
	 */
	private String getPattern( Iterator i, String previousSeparator) {
		StringBuffer pattern = new StringBuffer();
		Mask mask = (Mask)i.next();
		pattern.append(mask.getPattern(previousSeparator));
		if (i.hasNext()) {
			pattern.append("(?:" + getPattern(i,mask.getSeparatorRegex()) + ")?"); // add next level as optional
		}
		return pattern.toString();
		
	}
	
	
	public static void main(String[] args) {
		OutlineCode code = new OutlineCode();
		code.addMask(new Mask(NUMBERS,ANY_LENGTH,"."));
		code.addMask(new Mask(UPPERCASE_LETTERS,2,"."));
		code.addMask(new Mask(LOWERCASE_LETTERS,ANY_LENGTH,"."));
		
		boolean res;
		res = code.isValid("12.AA.a");
		res = code.isValid("22212.AA.absdf");
		res = code.isValid("1");
		res = code.isValid("12.AA");
		res = code.isValid("12.");
		res = code.isValid(".AA");
		res = code.isValid("A2");
		res = code.isValid("132");
		res = code.isValid("12.11");
		res = code.isValid("12.AA.");
	}
	

	public static class Mask  {
		static final int NUMBERS = 0;
		static final int UPPERCASE_LETTERS = 1;
		static final int LOWERCASE_LETTERS = 2;
		static final int CHARACTERS = 3;
		private static final int ANY_LENGTH = 0;
	
		int type = NUMBERS;
		int length; //The maximum length in characters of the outline code values, from 1-255. If length is any, the value is zero.
		String separator; // must be non-alphanumeric
		
		/**
		 * Gets a regular expression for this mask
		 * @param previousSeparator - if not empty, this expression is prefixed with previous mask's separator
		 * @return
		 */
		StringBuffer getPattern(String previousSeparator) {
			StringBuffer result = new StringBuffer("(");
			result.append(previousSeparator);
			switch (type) {
				case NUMBERS:
					result.append("\\d");
					break;
				case UPPERCASE_LETTERS:
					result.append("[A-Z]");
					break;
				case LOWERCASE_LETTERS:
					result.append("[a-z]");
					break;
				case CHARACTERS:
					result.append("[^" + separator + "]");
					break;
			}
			if (length == ANY_LENGTH)
				result.append("+"); // at least one
			else
				result.append("{" + length + "}"); // exactly <length> times

			result.append(")");
			return result;
		}
		
		String nextValue(String current) {
			String result = current; //TODO is this needed?
			switch (type) {
				case NUMBERS:
					int value = Integer.parseInt(current) + 1;
					if (length == ANY_LENGTH)
						result = "" + value;
					else
						result = new DecimalFormat(StringUtils.repeat("0",length)).format( Integer.getInteger(current));
					break;
			}
			return result;		
		}
		
		public Mask() {}
		public Mask(int type, int length, String separator) {
			this.type = type;
			this.length = length;
			this.separator = separator;
		}
		
		/**
		 * @return Returns the length.
		 */
		public int getLength() {
			return length;
		}
		/**
		 * @param length The length to set.
		 */
		public void setLength(int length) {
			this.length = length;
		}
		/**
		 * @return Returns the separator.
		 */
		public String getSeparator() {
			return separator;
		}
		
		public String getSeparatorRegex() {
			return "\\" + separator; // it is allowed to escape any nonalpha character
		}
		
		/**
		 * @param separator The separator to set.
		 */
		public void setSeparator(String separator) {
			this.separator = separator;
		}
		/**
		 * @return Returns the type.
		 */
		public int getType() {
			return type;
		}
		/**
		 * @param type The type to set.
		 */
		public void setType(int type) {
			this.type = type;
		}
	}

}