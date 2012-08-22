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

Attribution Information: Attribution Copyright Notice: Copyright � 2006, 2007
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JTextField;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.projity.configuration.Configuration;
import com.projity.configuration.FieldDictionary;
import com.projity.contrib.util.Log;
import com.projity.contrib.util.LogFactory;
import com.projity.datatype.Duration;
import com.projity.datatype.DurationFormat;
import com.projity.datatype.Hyperlink;
import com.projity.datatype.Money;
import com.projity.datatype.PercentFormat;
import com.projity.datatype.RateFormat;
import com.projity.datatype.TimeUnit;
import com.projity.datatype.Work;
import com.projity.document.Document;
import com.projity.field.Select.InvalidChoiceException;
import com.projity.grouping.core.GroupNodeImpl;
import com.projity.grouping.core.Node;
import com.projity.grouping.core.VoidNodeImpl;
import com.projity.grouping.core.hierarchy.BelongsToHierarchy;
import com.projity.grouping.core.model.NodeModel;
import com.projity.grouping.core.model.WalkersNodeModel;
import com.projity.grouping.core.summaries.NodeWalker;
import com.projity.grouping.core.summaries.SummaryNames;
import com.projity.grouping.core.summaries.SummaryVisitor;
import com.projity.grouping.core.summaries.SummaryVisitorFactory;
import com.projity.options.CalendarOption;
import com.projity.options.EditOption;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.task.BelongsToDocument;
import com.projity.pm.task.Project;
import com.projity.pm.time.Interval;
import com.projity.scripting.ScriptedFormula;
import com.projity.server.data.DataObject;
import com.projity.strings.Messages;
import com.projity.util.ClassUtils;

/**
 *
 */
public class Field implements SummaryNames, Cloneable, Comparable, Finder, Comparator {
	static Log log = LogFactory.getLog(FieldDictionary.class);
	private static final String EMPTY_STRING = "";
	private static final String PASSWORD_MASK = "********";
	private static final String NON_IMPLEMENTED = "<not implemented>";
	public static final String NOT_APPLICABLE = "<N/A>";
	public static final String NO_CHOICE = "";
	public static final String MULTIPLE_VALUES = Messages.getString("Symbol.multipleValues");
	private String configurationId; // what's read in from config file.
	private String id; // id read from config or modified if array field
	private String name; // id converted to string from properties file
	private String englishName;
	private Select select = null;
	private OptionsFilter filter = null;
	private FieldAccessible accessControl = null;
	private ScriptedFormula formula = null;
	private int summary = NONE;
	private int groupSummary = NONE;
	private Range range = null;
	private Integer ZERO = new Integer(0);
	private boolean nameField = false;
	private boolean map = false;
	private String url = null;
	private boolean image = false;
	private boolean startValue = false; // for dates, whether to use start  of day
	private boolean endValue = false; // for dates, whether to end of day
	boolean dateOnly = false; // for date fields, whether to show time
	private String extraCategory = null; // for extra fields, such as those from salesforce
	// reflection info - these do not change, so that can be reused across all
	// fields
	private static Class[] getterParams = new Class[] {};
	private static Class[] getterIndexedParams = new Class[] { int.class };
	private static Class[] getterContextParams = new Class[] { FieldContext.class }; // context
	private static Class[] getterIndexedContextParams = new Class[] { int.class, FieldContext.class }; // context
	private String property;
	private String referencedObjectProperty = null;
	private String referencedIdProperty = null;
	private String finder = null;
	private Class clazz = null;
	private Method methodGet = null;
	private Method methodSet = null;
	private Method methodReset = null;
	private Method methodReadOnly = null;
	private Method methodHide = null;
	private Method methodOptions = null;
	private Method finderMethod = null;
	private Class internalType = null; // return type of getter
	private Class externalType = null; // if non null then its the logical
										// type. For example, externalType=Date,
										// internalType=long for date values
	private Class displayType = null;
	private Object defaultValue = null;
	private String errorMessage = null;
	private int textWidth = Integer.MAX_VALUE;
	private int columnWidth = 0; // column width for spreadsheet
	private int svgColumnWidth = 0; //due to font conversion svg column width can different
	private Comparator comparator = null;
	private boolean getHasNoContext = false; // if the getter is just a property getter
	private boolean setHasNoContext = false; // if the setter is just a property setter
	private boolean resetHasNoContext = false; // if reset is just a getter
	private boolean readOnlyHasNoContext = false; // if read-only is just a getter
	private boolean hideHasNoContext = false; // if hide is just a getter
	private boolean optionsHasNoContext = false; // if options is just a getter

	// properties that can be set from XML
	private boolean readOnly = false; // if value is always read only
	private boolean dontLimitToChoices = false; // if a choice list exists and
												// value need not be in the list
	private boolean scalar = true; // if the value can be accessed as a scalar
									// (normal case)
	private boolean vector = false; // if the value can be accessed as a vector
	private boolean cantReset = false; // if the field can't be reset
	private boolean hideZeroValues = false; // if the field text should be null
											// if value is default
	private boolean callValidateOnClear = false; // if the value must be
													// validated on clearing

	private boolean password = false; // if the value is a password and should
										// not be displayed
	private boolean money = false; // if the value is a money
	private boolean percent = false; // if the value is a percentage and
										// should be displayed as such
	private boolean duration = false; // if the field holds a duration
	private boolean rate = false; // if the field holds a value
	private boolean work = false; // if the field holds a work value
	private boolean date = false; // if the field holds a date
	private boolean zeroBasedIndex = false; // if should create a 0 index too.
	private int indexes = 0; // nonzero number of elements
	private int index = 0; // index of this field in indexed property
	private boolean memo = false; // if the field is a multiline edit.
	private boolean dynamicOptions = false; // whether a combo needs to be
											// evaluated every time shown
	private boolean hasToolTip = false;
	private boolean validOnObjectCreate = true; // whether this field can appear on New Item Dialogs.
	private boolean dirtiesWholeDocument = false; // wehter modifying this field causes all parts of its document to be dirty
	private String action = null;
	private boolean graphical = false; // to flag for fields like indicator
	private FieldContext specialFieldContext = null; // for web to set exact context
	public String lookupTypes = null; // types separated by semicolons
	private boolean server;
	private String help = null;
	private String alias = null;
	private boolean custom = false;
	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	/**
	 * @param range
	 *            The range to set.
	 */
	public void setRange(Range range) {
		this.range = range;
	}

	public Range getRange() {
		return range;
	}

	/**
	 * Fields are constructed using chained properties
	 *
	 */
	public Field() {
	}

	public final void setProperty(String property) {
		this.property = property;
	}

	public final void setClass(Class clazz) {
		this.clazz = clazz;
	}

	public final Class getClazz() {
		return clazz;
	}

	/**
	 * To see if the field applies to the object, see if the field's type is a
	 * supertype or same as object
	 *
	 * @param object
	 * @return
	 */
	public boolean isApplicable(Object object) {
		if (object == null)
			return false;
		if (object instanceof DelegatesFields) {// for objects that delegate, they should be able to display anything
			if (((DelegatesFields)object).delegates(this))
			   return true;
		}

		return isApplicable(object.getClass());
	}

	public boolean isApplicable(Class type) {
		return clazz.isAssignableFrom(type);
	}

	/**
	 * Is field applicable to any type in types array
	 *
	 * @param types
	 * @return
	 */
	public boolean isApplicable(Class[] types) {
		for (int i = 0; i < types.length; i++) {
			if (isApplicable(types[i]))
				return true;
		}
		return false;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final void setEnglishName(String englishName) {
		this.englishName = englishName;
	}

	/**
	 * @return Returns the select.
	 */
	public Select getSelect() {
		return select;
	}

	public final void setSelect(Select select) {
		this.select = select;
	}

	public final String convertIdToString(Object id) {
		if (select == null) {
			log.error("calling convertIdToString on non select field" + getName());
			return null;
		}
		return (String) select.getKey(id);
	}

	// Strange: digester doesn't set summary property. sum is used instead
	public final void setSum(String summaryType) {
		setSummary(summaryType);
	}

	public final void setSummary(String summaryType) {
		this.summary = SummaryVisitorFactory.getSummaryId(summaryType);
		if (summary == NONE)
			log.warn("unknown summary type: " + summaryType + " for field " + getName());
	}

	public final void setGroupSum(String summaryType) {
		this.groupSummary = SummaryVisitorFactory.getSummaryId(summaryType);
		if (groupSummary == NONE)
			log.warn("unknown summary type: " + summaryType + " for field " + getName());
	}

	public final void setExternalType(Class externalType) {
		this.externalType = externalType;
	}

	public final void setTextWidth(int textWidth) {
		this.textWidth = textWidth;
	}

	public int getTextWidth(Object object, FieldContext context) { // can
																		// override
																		// in
																		// weird
																		// circumstances
		return textWidth;
	}

	public int getTextWidth() {
		return textWidth;
	}
	/**
	 * @return Returns the .
	 */
	public int getColumnWidth() {
		return columnWidth;
	}

	/**
	 * @param columnWidth
	 *            The columnWidth to set.
	 */
	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}

	public int getColumnWidth(boolean svg) {
		return (svg)?getSvgColumnWidth():getColumnWidth();
	}
	public int getSvgColumnWidth() {
		return svgColumnWidth;
	}

	public void setSvgColumnWidth(int svgColumnWidth) {
		this.svgColumnWidth = svgColumnWidth;
	}

	public final void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}

	public Comparator getComparator() {
		if (comparator == null)
			return ClassUtils.getComparator(getDisplayType());
		return comparator;
	}

	public int getSummary() {
		return summary;
	}

	public int getGroupSummary() {
		return groupSummary;
	}

	// added for groups
	public SummaryVisitor getSummaryVisitor(int summary, boolean forceDeep) {
		return SummaryVisitorFactory.getInstance(summary, getDisplayType(),forceDeep); // TODO
																				// use
																				// internalType
																				// instead?
	}

	public SummaryVisitor getSummaryVisitor(boolean forceDeep) {
		return getSummaryVisitor(summary,forceDeep);
	}

	public boolean hasOptions() {
		return (select != null);
	}

	public boolean isHasOptions() {
		return hasOptions();
	}
	public boolean hasFilter() {
		return (filter != null);
	}

	public boolean isHasFilter() {
		return hasFilter();
	}

	/**
	 * For use in populating a list box
	 *
	 * @param object
	 *            TODO
	 * @return
	 */
	public Object[] getOptions(Object object) {
		if (select == null)
			return null;
		Object[] options=select.getKeyArray();
		if (filter==null)
			return options;
		else return filter.getOptions(options, select.getValueList(), object);
	}

	public boolean hasDynamicSelect() {
		return select != null && !select.isStatic();
	}

	/**
	 * @return Returns the type to use for this field in spreadsheets and
	 *         dialogs.
	 *
	 */
	public Class getDisplayType() {
		return displayType;
	}

/**
 * Return the name in shortned form: e.g. Long, String, etc.
 * @return
 */
	public String typeName() {
		if (isPercent())
			return "Percent";
		if (isImage())
			return "Image";
		if (isDate())
			return "Date";
		if (isDuration())
			return "Duration";
		String t = getDisplayType().toString();
		int i = t.lastIndexOf(".");
		if (i != -1)
			return t.substring(i+1);
		return t;

	}
	public String internalTypeName() {
		String t = internalType.toString();
		int i = t.lastIndexOf(".");
		if (i != -1)
			return t.substring(i+1);
		return t;

	}
	private String toText(Object value, Object object) {
		if (value == null)
			return EMPTY_STRING;
		if (defaultValue != null && hideZeroValues && defaultValue.equals(value))
			return EMPTY_STRING;
		Format f = getFormat(object);
		if (f != null) {
			return f.format(value);
		} else {
			if (isHyperlink())
				return ((Hyperlink)value).toString();
			return FieldConverter.toString(value, getDisplayType(), null); // Convert
																			// to
																			// string
		}
	}

	public String toExternalText(Object value,Object obj) {
		if (hasOptions())
			return convertValueToStringUsingOptions(value);
		else
			return toText(value,obj);
	}
	public final String getText(Object object, FieldContext context) {
		if (!isApplicable(object))
			return NOT_APPLICABLE;

		if (password) // don't show passwords
			return PASSWORD_MASK;

		if (context == null)
			context = specialFieldContext;
		Object value = null;
		try {
			value = getValue(object, context);
			if (hasOptions()) {
				return convertValueToStringUsingOptions(value);
			}
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		}
		return toText(value, object);
	}

	public final String getText(Node node, WalkersNodeModel nodeModel, FieldContext context) {
		Object object = node.getImpl();
		if (!isApplicable(object))
			return NOT_APPLICABLE;

		if (password) // don't show passwords
			return PASSWORD_MASK;
		if (context == null)
			context = specialFieldContext;
		Object value = null;
		try {
			value = getValue(node, nodeModel, context);
			if (hasOptions()) {
				return convertValueToStringUsingOptions(value);
			}
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		}
		return toText(value, object);
	}

	public final String getText(ObjectRef objectRef, FieldContext context) {
		if (context == null)
			context = specialFieldContext;

		if (objectRef.getCollection() != null) {
			return ""+getCommonValue(objectRef.getCollection(), true, true);
		}
		if (objectRef.getNode() != null)
			return getText(objectRef.getNode(), objectRef.getNodeModel(), context);
		else
			return getText(objectRef.getObject(), context);
	}

	public String convertValueToStringUsingOptions(Object value) {
		String result = (String) select.getKey(value);
		if (result != null)
			return result;
		else if (value instanceof String)
			return value.toString();
		if (!dontLimitToChoices)
			return NO_CHOICE;
		return value.toString();
	}

	public int getSummaryForGroup() {
		if (groupSummary != NONE)
			return groupSummary;
//		else
			return summary;
	}

	/**
	 * See if this node displays its own data for the field or uses summary
	 * value
	 *
	 * @param node
	 * @param nodeModel
	 * @return true if the node dispalys own value
	 */
	private boolean nodeHasNonSummarizedValue(Node node, WalkersNodeModel nodeModel) {
		// special behaviour for groups
		int sum = summary;
		if (node.getImpl() instanceof GroupNodeImpl) {
			sum = getSummaryForGroup();
		}
//		if (node.getImpl() instanceof Document) // for projects which don't roll up
//			return true;

		return (sum == NONE || sum == THIS || !nodeModel.isSummary(node));
	}

	public boolean hasSummary() {
		return summary != NONE;
	}

	public Object getValue(Node node, WalkersNodeModel nodeModel, FieldContext context) {
		Object result;
		Object object = node.getImpl();
		if (object instanceof VoidNodeImpl)
			return null;
//		if ("Field.userRole".equals(id) && object instanceof ResourceImpl){
//			ResourceImpl r=(ResourceImpl)object;
//			if (!r.isUser()) return null;
//		}z
		if (!(object instanceof GroupNodeImpl) && isHidden(object, context))
			return null;
		if (context == null)
			context = specialFieldContext;

		if ("Field.duration".equals(id) && (object instanceof GroupNodeImpl)) {
			Document document = nodeModel.getDocument();
			if (document == null || !(document instanceof Project))
				return null;
			WorkingCalendar wc = (WorkingCalendar) ((Project) document).getWorkCalendar();

			// startDate, endDate calculated twice. Can find better
			Field startField = FieldDictionary.getInstance().getFieldFromId("Field.start");
			Field endField = FieldDictionary.getInstance().getFieldFromId("Field.finish");
			Date start = (Date) getSummarizedValueForField(startField, node, nodeModel, context);
			Date end = (Date) getSummarizedValueForField(endField, node, nodeModel, context);

			double t = wc.compare(end.getTime(), start.getTime(), false);
			result = new Duration(Duration.getInstance(t / CalendarOption.getInstance().getMillisPerDay(), TimeUnit.DAYS));
			// TODO 8 IS A HACK REPLACE ALL THIS SECTION
		} else {
			if (nodeHasNonSummarizedValue(node, nodeModel)) {// if no summary
																// or leaf
				result = getValue(object, context);
				if (hasOptions()) {
					result = convertValueToStringUsingOptions(result);
				}
			} else {
				result = getSummarizedValueForField(this, node, nodeModel, context);
			}
		}
		if (isWork() && result != null) { // work must be formatted correctly
			((Work) result).setWork(true);
		}
		return result;
	}

	public Object getValue(ObjectRef objectRef, FieldContext context) {
		if (context == null)
			context = specialFieldContext;

		if (objectRef.getCollection() != null) {
			return getCommonValue(objectRef.getCollection(), true, false);
		}

		if (objectRef.getNode() != null)
			return getValue(objectRef.getNode(), objectRef.getNodeModel(), context);
		else
			return getValue(objectRef.getObject(), context);
	}

	private static Object getSummarizedValueForField(Field field, Node node, WalkersNodeModel nodeModel, FieldContext context) {
		// group's special summaries handled here
		if (context == null)
			context = field.specialFieldContext;

		Object object = node.getImpl();
		NodeWalker walkingVisitor = (NodeWalker) field.getSummaryVisitor((object instanceof GroupNodeImpl) ? field.getSummaryForGroup() : field
				.getSummary(),object instanceof Document);
		walkingVisitor.setNode(node);
		walkingVisitor.setNodeModel(nodeModel);
		walkingVisitor.setContext(context);
		walkingVisitor.setField(field);
		Object result = walkingVisitor.getSummary();
		if (result instanceof Double) { // convert to proper display type
			result = ClassUtils.doubleToObject((Double) result, field.getDisplayType());
		}
		if ((object instanceof GroupNodeImpl) && field.hasOptions()) { // TODO
																		// should
																		// apply
																		// to
																		// summaries
																		// other
																		// than
																		// group
			result = field.convertValueToStringUsingOptions(result);
		}
		return result;
	}

	private Object getPropertyValue(Object object, FieldContext context) {
		Object result = null;
		if (context == null)
			context = specialFieldContext;

		if (isFormula()) {
			result = this.evaluateFormula(object); // for now not time distrib
		} else {
			if (methodGet == null)
				return null;
			try {

				if (getHasNoContext)
					result = methodGet.invoke(object, (isIndexed() ? new Object[] { new Integer(index) } : new Object[] {}));
				else {
					result = methodGet.invoke(object, (isIndexed() ? new Object[] { new Integer(index), context }
							: new Object[] { context }));
				}

			} catch (IllegalArgumentException e) {
				System.out.println("Bad field " + this);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				System.out.println("Bad field " + this);
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				System.out.println("Bad field " + this);
				e.printStackTrace();
			}
		}
		return result;
	}

	public Object getValue(Object object) {
		return getValue(object,null);
	}

 	public Object getValue(Object object, FieldContext context) {
		if (!isApplicable(object))
			return null;
		if (context == null)
			context = specialFieldContext;

		Object result = null;
		if (object instanceof DelegatesFields) {
			DelegatesFields delegator = (DelegatesFields)object;
			if (delegator.delegates(this)) {
				result = delegator.getDelegatedFieldValue(this);
				return result;
			}
		}
		result = getPropertyValue(object,context);
		if (isMap()) {
			if (result == null) // if no map
				return null;
			result = ((Map)result).get(getId());
		}
		if (hasExternalType()) { // need to convert once more
			if (FieldContext.isScripting(context)) {
				if (isDuration()) // for durations get rid of unit when scripting
					result = Long.valueOf(Duration.millis(((Long)result).longValue()));
			} else {
				try {// convert to external type
					result = FieldConverter.convert(result, externalType, context); // convert a long to date for example
				} catch (FieldParseException e1) {
					e1.printStackTrace();
					result = null;
				}
			}
		}
		if (hideZeroValues && isZero(result))
			return null;

//		if (result != null && url != null) {
//			result = "<html><a href=\"" + result + "\">" + url + "</a></html>";
//		}
		return result;
	}

	public boolean isZero(Object value) {
		if (value instanceof Number)
			return (((Number) value).doubleValue() == 0.0);
		else if (value instanceof String)
			return (((String) value).length() == 0);
		return false;
	}

	public final void setText(Object object, String textValue, FieldContext context) throws FieldParseException {
		if (context == null)
			context = specialFieldContext;
		if (isReadOnly(object, context)) { // don't allow setting of read only
											// fields
		// log.warn("Tried to set text of read only field" + getId());
			return;
		}
		Object value = preprocessText(object, textValue, context);
		if (value == textValue) {
			Format f = getFormat(object);
			if (f != null) {
				try {
					value = f.parseObject(textValue);
				} catch (ParseException e) {
					f = getSecondaryFormat(object); // allow money to parse as number too
					boolean secondOK = false;
					if (f != null) {
						try {
							value = f.parseObject(textValue);
							secondOK = true;
						} catch (ParseException e1) {
						}
					}
					if (!secondOK)
						throw new FieldParseException(e);
				}
			} else {
				value = FieldConverter.convert(textValue, hasExternalType() ? externalType : internalType, context); // converts
																														// to
																														// Date
			}
		}
		setInternalValueAndUpdate(object, this, value, context);
	}

	public final void setText(Node node, WalkersNodeModel nodeModel, String textValue, FieldContext context) throws FieldParseException {
		if (context == null)
			context = specialFieldContext;
		if (isReadOnly(node, nodeModel, context)) { // don't allow setting of
													// read only fields
		// log.warn("Tried to set text of read only field" + getId());
			return;
		}
		Object object = node.getImpl();
		Object value = preprocessText(object, textValue, context);
		if (value == textValue) {
			Format f = getFormat(object);
			if (f != null) {
				try {
					value = f.parseObject(textValue);
				} catch (ParseException e) {
					throw new FieldParseException(e);
				}
			} else {
				value = FieldConverter.convert(textValue, hasExternalType() ? externalType : internalType, context); // converts
																														// to
																														// Date
			}
		}
		setValue(node, nodeModel,this, value, context);
	}

	public final void setText(ObjectRef objectRef, String textValue, FieldContext context) throws FieldParseException {
		if (context == null)
			context = specialFieldContext;
		if (objectRef.getCollection() != null) {
			Iterator i = objectRef.getCollection().iterator();
			while (i.hasNext()) {
				setText(i.next(), textValue, context);
			}
		} else if (objectRef.getNode() != null)
			setText(objectRef.getNode(), objectRef.getNodeModel(), textValue, context);
		else
			setText(objectRef.getObject(), textValue, context);
	}

	/**
	 * Called from spreadsheet
	 *
	 * @param node
	 *            Node modifield
	 * @param nodeModel
	 *            nodeModel where node lives
	 * @param source
	 *            Source of change (for event)
	 * @param value
	 *            value
	 * @param context
	 * @throws FieldParseException
	 */
	public void setValue(Node node, WalkersNodeModel nodeModel, Object source, Object value, FieldContext context) throws FieldParseException {
		if (context == null)
			context = specialFieldContext;
		Object object = node.getImpl();
		setValue(object, source, value, context);
	}

	public void setValue(Object object, Object source, Object value, FieldContext context) throws FieldParseException {
		if (context == null)
			context = specialFieldContext;
		if (hasOptions()) {
			if (value instanceof String) value = preprocessText(object, (String) value, context);
		} else {
			if (value instanceof String && hasExternalType()) // do a first
																// pass,
																// converting
																// say, from
																// string to
																// Duration
				value = FieldConverter.convert(value, externalType, context);
			if (value == null)
				throw new FieldParseException(errorMessage(value, object));
		}
		setInternalValueAndUpdate(object, source, value, context);
	}

	private void setInternalValueAndUpdate(Object object, Object source, Object value, FieldContext context) throws FieldParseException {
		if (context == null)
			context = specialFieldContext;
		if (setInternalValue(object, value, context)) { // if succeeded in setting value
			if (context == null || !context.isNoUpdate())
				fireEvent(object,source,context);
		}
	}

	public void fireEvent(Object object, Object source,FieldContext context) {
		if (context == null)
			context = specialFieldContext;
		if (object instanceof BelongsToDocument && source != null) { // if no source then no update
			if (!FieldContext.isNoUpdate(context)) {
				Document document = ((BelongsToDocument) object).getDocument();
				document.getObjectEventManager().fireUpdateEvent(source, object, this);
				if (isDirtiesWholeDocument())
					document.setAllChildrenDirty(true);
			}
		}
	}

	public boolean setValue(Object object, Object source, Object value) {
		try {
			setValue(object, source, value, null);
			return true;
		} catch (FieldParseException e) {
			return false;
		}
	}

	public void setValue(ObjectRef objectRef, Object source, Object value, FieldContext context) throws FieldParseException {
		if (context == null)
			context = specialFieldContext;
		if (objectRef.getCollection() != null) {
			Iterator i = objectRef.getCollection().iterator();
			while (i.hasNext()) {
				setValue(i.next(), source, value, context);
			}
		} else if (objectRef.getNode() != null)
			setValue(objectRef.getNode(), objectRef.getNodeModel(), source, value, context);
		else
			setValue(objectRef.getObject(), source, value, context);
	}

	public String syntaxErrorForField() {
		return errorMessage(null, null);
	}

	private String errorMessage(Object value, Object object) {
		String message;
		if (errorMessage != null)
			message = errorMessage;
		else if (isDuration())
			message = "Message.invalidDuration";
		else if (isDate())
			message = "Message.invalidDate";
		else if (isRate())
			message = "Message.invalidRate";
		else if (isMoney())
			message = "Message.invalidCost";
		else
			message = "Message.invalidInput";
		return Messages.getString(message);

	}

	public Object getMultipleValueForType() {
		if (isDuration())
			return Duration.ZERO;
		else if (isDate())
			return null;
		else if (isPercent())
			return ClassUtils.PERCENT_MULTIPLE_VALUES;
		else
			return ClassUtils.getMultipleValueForType(internalType);
	}

	public boolean setInternalValue(Object object, Object value, FieldContext context) throws FieldParseException {
		if (context == null)
			context = specialFieldContext;
		if (!FieldContext.isForceValue(context) && isReadOnly(object, context)) { // don't allow setting of read only
											// fields
		// log.warn("Tried to set value of read only field" + getId());
			return false;
		}

		if (value != null && value.equals(getValue(object, context))) // if
																		// not
																		// change,
																		// do
																		// nothing
			return false; // TODO certain time-distibued fields need to be
							// changed

		if (hasExternalType()) { // does the second pass, converting from
									// say, Date to long
			value = FieldConverter.convert(value, internalType, context); // convert
																			// from
																			// date
																			// to
																			// long
																			// for
																			// example
			if (value == null && !isMap()) // TODO is this how to treat null values?
				return false;

		}

		if (range != null) {
			range.validate(value, this);
		}

		if (methodSet == null)
			return false;
		if (FieldContext.isParseOnly(context)) // if just parsing, do not set
			return false;
		try {
			if (isMap()) {
				Map map = (Map)getPropertyValue(object,context);
				map.put(getId(), value);
			} else if (setHasNoContext) {
				methodSet.invoke(object, (isIndexed() ? new Object[] { new Integer(index), value } : new Object[] { value }));
			} else {
				methodSet.invoke(object, (isIndexed() ? new Object[] { new Integer(index), value, context }
						: new Object[] { value, context }));
			}
			//LC
			if (object instanceof DataObject){
				if (context == null || !context.isNoDirty())
					((DataObject)object).setDirty(true);
			}

		} catch (IllegalArgumentException e) {
			throw new FieldParseException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			e.printStackTrace();
			if (cause != null && cause instanceof FieldParseException)
				throw (FieldParseException) cause;
			else {
				// setters can throw other values, so don't treat as bad exception
				throw new FieldParseException(cause.getMessage());
			}
		}
		return true;
	}

	/**
	 * @return Returns the readOnly. It's a static vlue
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * See if a node is read only. Before calling the object-based version of
	 * read-only, it checks to see if the node is a summarized parent and if the
	 * field is a summarized value, thus read only
	 *
	 * @param node
	 * @param nodeModel
	 * @param context
	 * @return
	 */
	public boolean isReadOnly(Node node, WalkersNodeModel nodeModel, FieldContext context) {
		if (context == null)
			context = specialFieldContext;
		if (node.getImpl() instanceof GroupNodeImpl)
			return true;
		if (summary == THIS && nodeModel.isSummary(node)) // for parents with
															// This summary type
			return true;
		if (!nodeHasNonSummarizedValue(node, nodeModel))
			return true;
		return isReadOnly(node.getImpl(), context);

	}

	public boolean isReadOnly(ObjectRef objectRef, FieldContext context) {
		if (context == null)
			context = specialFieldContext;
		if (objectRef.getCollection() != null) {
			Iterator i = objectRef.getCollection().iterator();
			while (i.hasNext()) {
				if (isReadOnly(i.next(), context))
					return true;
			}
			return false;
		}
		if (objectRef.getNode() != null)
			return isReadOnly(objectRef.getNode(), objectRef.getNodeModel(), context);
		else
			return isReadOnly(objectRef.getObject(), context);
	}

	/**
	 * See if the field is read only
	 *
	 * @param object
	 * @param context
	 * @return
	 */
	public boolean isReadOnly(Object object, FieldContext context) {
		if (context == null)
			context = specialFieldContext;

		if (readOnly) {
			return true;
		}
		if (!isApplicable(object)) { // if the object doesn't treat this
										// field
			return true;
		}
		if (isFormula())
			return true;
		// if (isHidden(object,context))
		// return true;

		if (object instanceof BelongsToHierarchy) { // for dialogs
		// for parents with This summary type
		// System.out.println("summary is " + summary + " THIS " + THIS + " NONE
		// " + NONE + " parent " + ((BelongsToHierarchy)object).isParent());
			if (summary != NONE)
				if (((BelongsToHierarchy) object).isParent())
					return true;
		}

		if (ClassUtils.isObjectReadOnly(object))
			return true;
		if (ClassUtils.isObjectFieldReadOnly(object,this))
			return true;
		if ((object instanceof DelegatesFields) && ((DelegatesFields)object).delegates(this))
			return true;
		Boolean value = (Boolean) invokeContextMethod(methodReadOnly, object, context, readOnlyHasNoContext);
		if (value != null)
			return value.booleanValue();

		return false;
	}


	public boolean isHidden(Object object, FieldContext context) {
		if (context == null)
			context = specialFieldContext;
		if (methodHide == null)
			return false;
		Boolean value = (Boolean) invokeContextMethod(methodHide, object, context, hideHasNoContext);
		if (value != null)
			return value.booleanValue();
		// TODO maybe test if objet itself is hidden
		return false;
	}

	public Object mapStringToValue(String textValue) {
		if (select == null)
			return null;
		try {
			return select.getValue(textValue);
		} catch (InvalidChoiceException e) {
			return null;
		}
	}

	public String mapValueToString(Object value) {
		if (select == null)
			return null;
		return (String) select.getKey(value);
	}

	protected Object preprocessText(Object object, String textValue, FieldContext context) throws FieldParseException {
		if (context == null)
			context = specialFieldContext;
		Object value;
		if (select != null) {
			if (textValue == null)
				return null;
			if (textValue.trim().length() == 0 && select.isAllowNull()) // special
																		// case
				textValue = Select.EMPTY;

			try {
				value = select.getValue(textValue);
			} catch (InvalidChoiceException e) {
				throw new FieldParseException(Messages.getString("Message.invalidChoice") + ": " + textValue);
			}
			if (value == null && (!select.isAllowNull() || textValue != Select.EMPTY))
				throw new FieldParseException(Messages.getString("Message.invalidChoice") + ": " + textValue);
		} else if (this.isBoolean()) {
			value = Boolean.valueOf(textValue);
		} else {
			value = textValue;
		}
		return value;
	}

	public boolean isValidChoice(String textValue) {
		try {
			preprocessText(null,textValue,null);
		} catch (FieldParseException e) {
			return false;
		}
		return true;
	}

	private final void setAccessorMethods() {
		if (clazz != null && property != null) {
			StringBuffer javaName = new StringBuffer(property);
			javaName.setCharAt(0, Character.toUpperCase(javaName.charAt(0)));

			// First look for a getter that has a context (indexed or not)
			methodGet = MethodUtils.getAccessibleMethod(clazz, "get" + javaName, (isIndexed() ? getterIndexedContextParams : getterContextParams));
			if (methodGet == null) // try is instead of get
				methodGet = MethodUtils.getAccessibleMethod(clazz, "is" + javaName, (isIndexed() ? getterIndexedContextParams : getterContextParams));

			// If not found, then use standard getter (indexed or not)
			if (methodGet == null) {
				getHasNoContext = true;
				methodGet = MethodUtils.getAccessibleMethod(clazz, "get" + javaName, (isIndexed() ? getterIndexedParams : getterParams));
				if (methodGet == null) // try is instead of get
					methodGet = MethodUtils.getAccessibleMethod(clazz, "is" + javaName, (isIndexed() ? getterIndexedParams : getterParams));
			}
			if (methodGet != null)
				internalType = methodGet.getReturnType();
			else
				log.error("Not getter found for field " + getId());

			// First look for a setter that has a context (indexed or not)
			methodSet = MethodUtils.getAccessibleMethod(clazz, "set" + javaName, (isIndexed() ? new Class[] { int.class, internalType,
					FieldContext.class } : new Class[] { internalType, FieldContext.class }));

			// If not found, then use standard setter (indexed or not)
			if (methodSet == null) {
				setHasNoContext = true;
				methodSet = MethodUtils.getAccessibleMethod(clazz, "set" + javaName, (isIndexed() ? new Class[] { int.class, internalType }
						: new Class[] { internalType }));
			}
			if (methodSet == null && !readOnly) {
				log.warn("No setter found for non-read-only field: " + getId());
			}
			methodReset = MethodUtils.getAccessibleMethod(clazz, "fieldReset" + javaName, getterContextParams);

			if (resetHasNoContext = (methodReset == null))
				methodReset = MethodUtils.getAccessibleMethod(clazz, "fieldReset" + javaName, getterParams);

			methodReadOnly = MethodUtils.getAccessibleMethod(clazz, "isReadOnly" + javaName, getterContextParams);
			if (readOnlyHasNoContext = (methodReadOnly == null))
				methodReadOnly = MethodUtils.getAccessibleMethod(clazz, "isReadOnly" + javaName, getterParams);
			//lc
//			methodObjectReadOnly = MethodUtils.getAccessibleMethod(clazz, "isReadOnly", getterParams);

			methodHide = MethodUtils.getAccessibleMethod(clazz, "fieldHide" + javaName, (isIndexed() ? getterIndexedContextParams
					: getterContextParams));
			if (hideHasNoContext = (methodHide == null))
				methodHide = MethodUtils.getAccessibleMethod(clazz, "fieldHide" + javaName, (isIndexed() ? getterIndexedParams : getterParams));
			methodOptions = MethodUtils.getAccessibleMethod(clazz, "fieldOptions" + javaName, getterContextParams);
			if (optionsHasNoContext = (methodOptions == null))
				methodOptions = MethodUtils.getAccessibleMethod(clazz, "fieldOptions" + javaName, getterParams);
		}
	}

	private final Object invokeContextMethod(Method method, Object object, FieldContext context, boolean noContext) {
		if (context == null)
			context = specialFieldContext;
		if (method == null)
			return null;
		try {
			if (noContext) {
				if (isIndexed())
					return method.invoke(object, new Object[] { new Integer(getIndex()), null });
				else
					return method.invoke(object, (Object[])null);
			} else {
				if (isIndexed())
					return method.invoke(object, new Object[] { new Integer(getIndex()), context });
				else
					return method.invoke(object, new Object[] { context });
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean hasExternalType() {
		return (externalType != null && externalType != internalType);
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getIdWithoutPrefix() {
		int i = id.indexOf('.');
		if (i != -1)
			return id.substring(i+1);
		else
			return id;
	}
	private int getDefaultColumnWidth() {
		if (isDuration()) {
			return 75;
		} else if (isDate()) {
			return 115;
		} else if (isMoney()) {
			return 100;
		} else if (isRate()) {
			return 75;
		} else if (getDisplayType() == Boolean.class) {
			return 40;
		} else {
			return 150;
		}
	}
	private int getSvgDefaultColumnWidth() {
		if (isDate()) return 130;
		else return getDefaultColumnWidth();
	}

	public boolean build() {
		configurationId = id; // id can change if array field, so store off
								// initial value
		boolean result = true;
		if (id == null) {
			log.error("Field has no id!");
			result = false;
		}
		if (property == null) {
			log.error("Field has no property:" + id);
			result = false;
		}

		if (name == null) // if not explicitly set, use id as string id
			name = Messages.getString(id);
		setAccessorMethods();
		map = Map.class.isAssignableFrom(internalType); // see if map

		if (isWork()) {
			setExternalType(Work.class);
		} else if (isDuration()) {
			setExternalType(Duration.class);
		} else if (isDate()) {
			setExternalType(Date.class);
		} else if (isMoney() && !isRate()) {
			setExternalType(Money.class);
		}

		displayType = (externalType == null) ? internalType : externalType;

		if (displayType != null && displayType.isPrimitive()) {
			displayType = ClassUtils.primitiveToObjectClass(displayType);
			externalType = displayType; // is this necessary?
		}
		if (finder != null) {
			finderMethod = ClassUtils.staticMethodFromFullName(finder, new Class[] { Object.class, Object.class });
			if (finderMethod == null)
				Field.log.error("invalid finder method " + finder + " for field" + name);

		}
		if (columnWidth == 0)
			columnWidth = getDefaultColumnWidth();
		if (svgColumnWidth == 0)
			svgColumnWidth = getSvgDefaultColumnWidth();

		return result;
	}

	public void setType(String type) {
		try {
			setExternalType(Class.forName(type));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		if (getAlias() != null)
			return getAlias();
		return name;
	}

	public String getDefaultName() {
		return name;
	}
	public boolean isBoolean() {
		return getDisplayType() == Boolean.class;
	}

	/**
	 * @return Returns the duration.
	 */
	public boolean isDuration() {
		return duration;
	}

	/**
	 * @param duration
	 *            The duration to set.
	 */
	public void setDuration(boolean duration) {
		this.duration = duration;
	}

	/**
	 * @return Returns the callValidateOnClear.
	 */
	public boolean isCallValidateOnClear() {
		return callValidateOnClear;
	}

	/**
	 * @param callValidateOnClear
	 *            The callValidateOnClear to set.
	 */
	public void setCallValidateOnClear(boolean callValidateOnClear) {
		this.callValidateOnClear = callValidateOnClear;
	}

	/**
	 * @return Returns the cantReset.
	 */
	public boolean isCantReset() {
		return cantReset;
	}

	/**
	 * @param cantReset
	 *            The cantReset to set.
	 */
	public void setCantReset(boolean cantReset) {
		this.cantReset = cantReset;
	}

	/**
	 * @return Returns the money.
	 */
	public boolean isMoney() {
		return money;
	}

	/**
	 * @param money
	 *            The money to set.
	 */
	public void setMoney(boolean money) {
		this.money = money;
	}

	/**
	 * @return Returns the defaultValue.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue
	 *            The defaultValue to set.
	 */
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return Returns the dontLimitToChoices.
	 */
	public boolean isDontLimitToChoices() {
		return dontLimitToChoices;
	}

	/**
	 * @param dontLimitToChoices
	 *            The dontLimitToChoices to set.
	 */
	public void setDontLimitToChoices(boolean dontLimitToChoices) {
		this.dontLimitToChoices = dontLimitToChoices;
	}

	/**
	 * @return Returns the formula.
	 */
	public boolean isFormula() {
		return formula != null;
	}

	/**
	 * @param formula
	 *            The formula to set.
	 */
	public void setFormula(String formulaName, String variableName, String formulaText) {
//		formula = FormulaFactory.addScripted("Field", formulaName, variableName, formulaText);
		throw new RuntimeException("setFormula"); //TODO if used, need to handle addNormal too
	}

	public void clearFormula() {
		formula = null;
	}

	public Object evaluateFormula(Object object) {
		try {
			return formula.evaluate(object);
		} catch (InvalidFormulaException e) {
			log.error("Formula is invalid " + formula.getText());
			return null;
		}
	}

	/**
	 * @return Returns the hideZeroValues.
	 */
	public boolean isHideZeroValues() {
		return hideZeroValues;
	}

	/**
	 * @param hideZeroValues
	 *            The hideZeroValues to set.
	 */
	public void setHideZeroValues(boolean hideZeroValues) {
		this.hideZeroValues = hideZeroValues;
	}

	/**
	 * @return Returns the password.
	 */
	public boolean isPassword() {
		return password;
	}

	/**
	 * @param password
	 *            The password to set.
	 */
	public void setPassword(boolean password) {
		this.password = password;
	}

	/**
	 * @return Returns the percent.
	 */
	public boolean isPercent() {
		return percent;
	}

	/**
	 * @param percent
	 *            The percent to set.
	 */
	public void setPercent(boolean percent) {
		this.percent = percent;
	}

	/**
	 * @param readOnly
	 *            The readOnly to set.
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * @return Returns the scalar.
	 */
	public boolean isScalar() {
		return scalar;
	}

	/**
	 * @param scalar
	 *            The scalar to set.
	 */
	public void setScalar(boolean scalar) {
		this.scalar = scalar;
	}

	/**
	 * @return Returns the vector.
	 */
	public boolean isVector() {
		return vector;
	}

	/**
	 * @param vector
	 *            The vector to set.
	 */
	public void setVector(boolean vector) {
		this.vector = vector;
	}

	/**
	 * @return Returns the indexes.
	 */
	public int getIndexes() {
		return indexes;
	}

	/**
	 * @param indexes
	 *            The indexes to set.
	 */
	public void setIndexes(int indexes) {
		this.indexes = indexes;
	}

	/**
	 * Set the array size of the custom field this applies to
	 *
	 * @param boundsField
	 */
	public void setBoundsField(String boundsField) {
		if (indexes>0) //Pb with IBM JDK, some boundsField with indexes=0 are reseting some settings
		ClassUtils.setStaticField(boundsField, indexes);
	}

	/**
	 * @return Returns the index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 *            The index to set.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isIndexed() {
		return indexes != 0;
	}


	public Object clone()  {
		// TODO Auto-generated method stub
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Field createIndexedField(int index) {
		Field indexedField = (Field) clone();
		indexedField.setIndex(index);
		String indexSuffix = "";
		if (indexedField.isZeroBasedIndex()) {
			if (index > 0)
				indexSuffix += index;
		} else {
			indexSuffix += index + 1;
		}
		indexedField.setId(getId().replaceFirst("#", indexSuffix));
		indexedField.setName(getName().replaceFirst("#", indexSuffix));
		return indexedField;
	}
	/**
	 * Make a new field with an integrated context that uses the given interval
	 * @param f
	 * @param interval
	 * @return
	 */
	public static Field createIntervalField(Field f, Interval interval) {
		FieldContext fieldContext = new FieldContext();
		fieldContext.setInterval(interval);
		Field field =(Field) f.clone();
		field.specialFieldContext = new FieldContext();
		return field;
	}

	static SimpleDateFormat f=new SimpleDateFormat("E");
	public String getLabel() {
		if (specialFieldContext == null)
			return getName();
		long start = specialFieldContext.getInterval().getStart();
		return f.format(new Date(start));
	}


	/**
	 * @return Returns the date.
	 */
	public boolean isDate() {
		return date;
	}

	/**
	 * @param date
	 *            The date to set.
	 */
	public void setDate(boolean date) {
		this.date = date;
	}

	/**
	 * Compares two fields. Normally a simple String compareTo is used, but in
	 * the case of array fields, I compare their indexes - we want such fields
	 * to sort numerically and not alphabetically so that for example, Cost11
	 * appears after Cost2 and not before.
	 */
	public int compareTo(Object to) {
		if (to == null)
			throw new NullPointerException();
		if (!(to instanceof Field))
			throw new ClassCastException();
		Field toField = (Field) to;
		if (configurationId == toField.configurationId) { // if array field,
															// then compare
															// indexes
			return index - toField.index;
		} else {
			return getName().compareTo(toField.getName());
		}
	}

	/**
	 * Sets each object in a collection to value. Exceptions are ignored
	 *
	 * @param collection
	 * @param value
	 */
	public void setValueForEach(Collection collection, Object value, FieldContext context, Object eventSource) {
		if (context == null)
			context = specialFieldContext;
		if (collection == null)
			return;
		Iterator i = collection.iterator();
		Object current;
		while (i.hasNext()) {
			current = i.next();
			try {
				setValue(current, eventSource, value, context);
			} catch (FieldParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Given a collection, if each elements shares the same value for this
	 * field, the value is returned, Otherwise null is returned.
	 *
	 * @param collection
	 * @param useMultipleValue
	 *            If true, the default value will be used if no elements or
	 *            values differ
	 * @return
	 */
	public Object getCommonValue(Collection collection, boolean useMultipleValue, boolean text) {
		if (collection == null)
			return null;
		Iterator i = collection.iterator();
		Object value = null;
		Object current;
		Object currentValue;
		Comparator comparatorToUse = (text ? ComparableComparator.getInstance() : getComparator());
		while (i.hasNext()) {
			current = i.next();
			if (text)
				currentValue = getText(current, null);
			else
				currentValue = getValue(current, null);
			if (value == null)
				value = currentValue;
			else if (0 != comparatorToUse.compare(currentValue, value)) {
				value = null;
				break;
			}
		}
		if (value == null && useMultipleValue) {
			value = getMultipleValueForType();
		}
		return value;
	}

	/**
	 * Given a collection, return the text for a field if each elements shares
	 * the same value, otherwise return "--"
	 *
	 * @param collection
	 * @return
	 */
	public String getCommonValueString(Collection collection) {
		if (collection == null || collection.size() == 0)
			return null;
		Object common = getCommonValue(collection, false, true);
		if (common == null)
			return MULTIPLE_VALUES;
		else
			return common.toString();
	}

	public Object findFirstInCollection(Object value, Collection collection) {
		Iterator i = collection.iterator();
		Object current;
		while (i.hasNext()) {
			current = i.next();
			if (0 == getComparator().compare(getValue(current, null), value))
				return current;
		}
		return null;
	}

	public Object[] findAllInCollection(Object value, Collection collection) {
		ArrayList result = new ArrayList();
		Iterator i = collection.iterator();
		Object current;
		while (i.hasNext()) {
			current = i.next();
			if (0 == getComparator().compare(getValue(current, null), value))
				result.add(current);
		}
		return result.toArray();
	}

	/**
	 * @return Returns the work.
	 */
	public boolean isWork() {
		return work;
	}

	public boolean isDurationOrWork() {
		return isWork() || isDuration();
	}

	/**
	 * @param work
	 *            The work to set.
	 */
	public void setWork(boolean work) {
		setDuration(work); // work is always a duration too
		this.work = work;
	}

	public void setFinder(String finder) {
		this.finder = finder;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.projity.field.Finder#find(java.lang.Object)
	 */
	public Object find(Object key, Collection container) {
		if (finderMethod == null)
			return findFirstInCollection(key, container);

		try {
			return finderMethod.invoke(null, new Object[] { key, container });
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Compare two objects using this field. In this way i
	 */
	public int compare(Object arg0, Object arg1) {
		return getComparator().compare(getValue(arg0, null), getValue(arg1, null));
	}

	/**
	 * @return Returns the value.
	 */
	public boolean isRate() {
		return rate;
	}

	/**
	 * @param value
	 *            The value to set.
	 */
	public void setRate(boolean rate) {
		this.rate = rate;
	}

	/**
	 * @return Returns the zeroBasedIndex.
	 */
	public boolean isZeroBasedIndex() {
		return zeroBasedIndex;
	}

	/**
	 * @param zeroBasedIndex
	 *            The zeroBasedIndex to set.
	 */
	public void setZeroBasedIndex(boolean zeroBasedIndex) {
		this.zeroBasedIndex = zeroBasedIndex;
	}

	public double getScaleFactor() {
		if (isWork() || isDuration())
			return CalendarOption.getInstance().getMillisPerDay();
		else
			return 1.0;
	}

	public Format getFormat() {
		return getFormat(null);
	}

	public Format getFormat(Object object) {
		if (isWork()) {
			return DurationFormat.getWorkInstance();
		} else if (isRate()) {
			return RateFormat.getInstance(object, isMoney(), isPercent(),true);
		} else if (isMoney()) {
			return Money.getMoneyFormatInstance();
		} else if (isDuration()) {
			return DurationFormat.getInstance();
		} else if (isPercent()) {
			return PercentFormat.getInstance();
		} else if (isDate()) {
			return EditOption.getInstance().getDateFormat();
		} else if (displayType == Double.class || displayType == Float.class || displayType == Integer.class) {
			return NumberFormat.getInstance();
		}
		return null;
	}

	private Format getSecondaryFormat(Object object) {
		if (isMoney())
			return NumberFormat.getInstance();
		return null;

	}
	public boolean isNumber() {
		return  displayType == Double.class
				|| displayType == Float.class
				|| displayType == Integer.class
				|| displayType == Long.class;

	}

	public int getHorizontalAlignment() {
		if (isImage() || isBoolean())
			return JTextField.CENTER;
		else if (isWork()
		 || isRate()
		 || isMoney()
		 || isDuration()
		 || isDate()
		 || isPercent()
		 || isNumber())
			return JTextField.RIGHT;
		else
			return JTextField.LEFT;

	}
	public static Object value(Field field, Node node, NodeModel nodeModel) {
		return field.getValue(node, nodeModel, null);
	}

	public static Object value(Field field, Object object) {
		return field.getValue(object, null);
	}

	/**
	 * @return Returns the property.
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * @return Returns the referencedObjectProperty.
	 */
	public String getReferencedObjectProperty() {
		return referencedObjectProperty;
	}

	/**
	 * @param referencedObjectProperty
	 *            The referencedObjectProperty to set.
	 */
	public void setReferencedObjectProperty(String referencedObjectProperty) {
		this.referencedObjectProperty = referencedObjectProperty;
	}

	/**
	 * @return Returns the referencedIdProperty.
	 */
	public String getReferencedIdProperty() {
		return referencedIdProperty;
	}

	/**
	 * @param referencedIdProperty
	 *            The referencedIdProperty to set.
	 */
	public void setReferencedIdProperty(String referencedIdProperty) {
		this.referencedIdProperty = referencedIdProperty;
	}

	public Object getValueFromProperty(Object obj) {
		if (property == null)
			return null;
		try {
			return PropertyUtils.getProperty(obj, property);
		} catch (Exception e) { //claur
		}
		return null;

	}

	public Object getReferencedObject(Object obj) {
		if (referencedObjectProperty == null)
			return null;
		try {
			return PropertyUtils.getProperty(obj, referencedObjectProperty);
		} catch (Exception e) { //claur
		}
		return null;
	}

	public Long getReferencedId(Object obj) {
		Long result = null;
		if (referencedIdProperty != null) {
			try {
				result = (Long) PropertyUtils.getProperty(obj, referencedIdProperty);
//				System.out.println("____ref id = " + result);
			} catch (Exception e) { //claur
			}
		}
		return result;
	}

	public boolean isLink() {
		return referencedObjectProperty != null
		|| referencedIdProperty != null;
	}

	/**
	 * @return Returns the memo.
	 */
	public boolean isMemo() {
		return memo;
	}

	/**
	 * @param memo
	 *            The memo to set.
	 */
	public void setMemo(boolean memo) {
		this.memo = memo;
	}

	/**
	 * @return Returns the nameField.
	 */
	public boolean isNameField() {
		return nameField;
	}

	/**
	 * @param nameField
	 *            The nameField to set.
	 */
	public void setNameField(boolean nameField) {
		this.nameField = nameField;
	}

	public final String getErrorMessage() {
		return errorMessage;
	}

	public final void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean isStandardType() {
		boolean nonStandard = isDuration() || isRate() || hasOptions() || isMoney();
		return !nonStandard;
	}

	final public boolean isDynamicOptions() {
		return dynamicOptions;
	}

	final public void setDynamicOptions(boolean dynamicOptions) {
		this.dynamicOptions = dynamicOptions;
	}

	/**
	 * Copies field data from one object to another. Does not copy read only
	 * fields
	 *
	 * @param to
	 * @param from
	 */
	public void copyData(Object to, Object from) {
		if (isReadOnly(to, null))
			return;
		Object value = getValue(from, null);
		setValue(to, null, value);
	}

	/**
	 * Copies multiple fields from one object to another
	 *
	 * @param fieldArray
	 * @param to
	 * @param from
	 */
	public static void copyData(Collection fieldArray, Object to, Object from) {
		Iterator i = fieldArray.iterator();
		while (i.hasNext()) {
			((Field) i.next()).copyData(to, from);
		}

	}

	/**
	 * Copies data from a map which contains Field Ids (e.g. Field.work) as keys
	 * and values (either string or object) to the destination to. Read Only
	 * fields are ignored.
	 *
	 * @param to
	 * @param fromMap
	 * @throws FieldParseException
	 */
	public static void copyData(Object to, Map fromMap) throws FieldParseException {
		Iterator i = fromMap.keySet().iterator();
		FieldContext context;
		while (i.hasNext()) {
			String fieldId = (String) i.next();
			Field field = Configuration.getFieldFromId(fieldId);
			context = field.specialFieldContext;
			if (!field.isReadOnly(to, context)) {
				Object data = fromMap.get(fieldId);
				if (data instanceof String)
					field.setText(to, (String) fromMap.get(fieldId), context);
				else
					field.setValue(to, data, context);
			}
		}
	}

	/**
	 * Copies a set of fields, defined by the fieldArray to a map with their
	 * values
	 *
	 * @param toMap
	 * @param from
	 * @param fieldArray
	 */
	public static void copyData(Map toMap, Object from, Collection fieldArray) {
		FieldContext context = null;
		Iterator i = fieldArray.iterator();
		while (i.hasNext()) {
			Field field = (Field) i.next();
			context = field.specialFieldContext;
			String value = field.getText(from, context);
			toMap.put(field.getId(), value);
		}

	}

	public final boolean isHasToolTip() {
		return hasToolTip;
	}

	public final void setHasToolTip(boolean hasToolTip) {
		this.hasToolTip = hasToolTip;
	}

	public final boolean isMap() {
		return map;
	}

	public final String getExtraCategory() {
		return extraCategory;
	}

	public final void setExtraCategory(String extraCategory) {
		this.extraCategory = extraCategory;
	}
	public boolean isExtra() {
		return extraCategory != null;
	}
	public Object convertValueForExport(Object value) {
		if (value instanceof Duration)
			value = Double.valueOf(((Duration)value).getAsDays());
		return value;
	}

	public final String getUrl() {
		return url;
	}

	public final void setUrl(String url) {
		this.url = url;
	}

	public final boolean isHyperlink() {
		return getUrl() != null;
	}

	public final boolean isValidOnObjectCreate() {
		return validOnObjectCreate;
	}

	public final void setValidOnObjectCreate(boolean validOnObjectCreate) {
		this.validOnObjectCreate = validOnObjectCreate;
	}

	public final boolean isDirtiesWholeDocument() {
		return dirtiesWholeDocument;
	}

	public final void setDirtiesWholeDocument(boolean dirtiesWholeDocument) {
		this.dirtiesWholeDocument = dirtiesWholeDocument;
	}

	public final String getAction() {
		return action;
	}

	public final void setAction(String action) {
		this.action = action;
	}

	public void invokeAction(Object obj) {
		if (action == null || obj == null)
			return;
		Object value = getValue(obj,null);
		if (value instanceof Hyperlink)
			((Hyperlink)value).invoke();
	}

	public final boolean isImage() {
		return image;
	}

	public final void setImage(boolean image) {
		this.image = image;
	}
	public String dump() {
		return ToStringBuilder.reflectionToString(this);
	}

	public FieldContext getSpecialFieldContext() {
		return specialFieldContext;
	}

	public void setSpecialFieldContext(FieldContext specialFieldContext) {
		this.specialFieldContext = specialFieldContext;
	}
	public Comparator getComparator(boolean ascending) {
		if (ascending == true)
			return this;
		else {
			return new Comparator() {
				public int compare(Object o1, Object o2) {
					return Field.this.compare(o2,o1);
				}};
		}
	}
	public boolean isComparable() {
		return !isImage();
	}

	public OptionsFilter getFilter() {
		return filter;
	}

	public void setFilter(OptionsFilter filter) {
		this.filter = filter;
	}

	public FieldAccessible getAccessControl() {
		return accessControl;
	}

	public void setAccessControl(FieldAccessible accessControl) {
		this.accessControl = accessControl;
	}

	public boolean isAuthorized(int role){
		return accessControl==null||accessControl.isAuthorized(role);
	}

	public boolean isGraphical() {
		return graphical;
	}

	public void setGraphical(boolean graphical) {
		this.graphical = graphical;
	}

	public String getLookupTypes() {
		return lookupTypes;
	}

	public void setLookupTypes(String lookupTypes) {
		this.lookupTypes = lookupTypes;
	}

	public boolean isServer() {
		return server;
	}

	public void setServer(boolean server) {
		this.server = server;
	}

	public static String getMetadataStringHeader() {
		return "Name"
		+ "\t" + "Id (for API)"
		+ "\t" + "API type"
		+ "\t" + "POD type"
		+ "\t" + "Read Only"
		+ "\t" + "Notes";

	}
	public String getMetadataString() {
		String result = getName()
		+ "\t" + getIdWithoutPrefix()
		+ "\t" + internalTypeName()
		+ "\t" + typeName()
		+ "\t" + isReadOnly()
		+ "\t";
		if (hasDynamicSelect()) {
			result += "Choices are dynamic";
		} else if (select != null) {
			result += select.documentOptions();
		}
		return result;
	}

	public boolean isStartValue() {
		return startValue;
	}

	public void setStartValue(boolean startValue) {
		this.startValue = startValue;
	}

	public boolean isEndValue() {
		return endValue;
	}

	public void setEndValue(boolean endValue) {
		this.endValue = endValue;
	}

	public boolean isDateOnly() {
		return dateOnly;
	}

	public void setDateOnly(boolean dateOnly) {
		this.dateOnly = dateOnly;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	public String getSummaryType() {
		if (isStartValue())
			return "min";
		if (isEndValue())
			return "max";
		int summary = getGroupSummary();
		if (summary == SummaryNames.NONE)
			summary = getSummary();
//System.out.println("Field "+ this + " group " + getGroupSummary() +  " sum " + getSummary());
		switch (summary) {
		case SummaryNames.SUM:
			return "sum";
		case SummaryNames.COUNT_ALL:
		case SummaryNames.COUNT_FIRST_SUBLEVEL:
		case SummaryNames.COUNT_NONSUMMARIES:
			return "count";
		case SummaryNames.AVERAGE:
		case SummaryNames.AVERAGE_FIRST_SUBLEVEL:
			return "average";

		}
		return null;
	}


}