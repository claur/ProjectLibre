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

Attribution Information: Attribution Copyright Notice: Copyright (c) 2006, 2007
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
package com.projity.dialog.calendar;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoableEditSupport;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projity.configuration.Settings;
import com.projity.contrib.calendar.ContribIntervals;
import com.projity.dialog.AbstractDialog;
import com.projity.dialog.ButtonPanel;
import com.projity.dialog.options.CalendarDialogBox;
import com.projity.options.CalendarOption;
import com.projity.pm.calendar.CalendarService;
import com.projity.pm.calendar.DayDescriptor;
import com.projity.pm.calendar.InvalidCalendarException;
import com.projity.pm.calendar.WorkRangeException;
import com.projity.pm.calendar.WorkingCalendar;
import com.projity.pm.calendar.WorkingHours;
import com.projity.pm.graphic.IconManager;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.pm.task.Project;
import com.projity.pm.task.ProjectFactory;
import com.projity.pm.time.HasStartAndEnd;
import com.projity.strings.Messages;
import com.projity.undo.CalendarEdit;
import com.projity.undo.UndoController;
import com.projity.util.Alert;
import com.projity.util.DateTime;


/**
 *
 */
public class ChangeWorkingTimeDialogBox extends AbstractDialog{
	private static final long serialVersionUID = 1L;
	public static class Form {
        protected WorkingCalendar calendar;
        public WorkingCalendar getCalendar() {
            return calendar;
        }
        public void setCalendar(WorkingCalendar calendar) {
            this.calendar = calendar;
        }
    }
    private UndoController undoController;
    private Form form;
    WorkingHours defaultWorkingHours = WorkingHours.getDefault();
    boolean unsaved = false;
    JComboBox calendarType;
    CalendarView sdCalendar;
    JRadioButton unknownWorkingTime;
    JRadioButton defaultWorkingTime;
    JRadioButton nonWorking;
    JRadioButton working;
    ButtonGroup datesSetting;
    JTextField[] timeStart;
    JTextField[] timeEnd;
    JLabel notEditable;
    JLabel caution;
    boolean dirtyWorkingHours = false;
    ContribIntervals lastSelection=new ContribIntervals();
    boolean lastWeekSelection[] = new boolean[7];
    JCheckBox test;
    JComponent cal;
    JButton newCalendar;
    JButton options;
    SimpleDateFormat hourFormat= DateTime.dateFormatInstance("H:mm"); //$NON-NLS-1$
    JLabel basedOnText;
    List documentCalendars;
    List projectCalendars;
    WorkingCalendar editedCalendar;
    boolean restrict;
	private Project project;


    private void setEditable(boolean editable) {
    	//TODO this code should be hooked in when calendars are persisted correclty
//        notEditable.setVisible(!editable);
//        caution.setVisible(editable);
//    	sdCalendar.setEnabled(editable);
//    	unknownWorkingTime.setEnabled(editable);
//    	defaultWorkingTime.setEnabled(editable);
//    	nonWorking.setEnabled(editable);
//    	working.setEnabled(editable);
//    	for (int i = 0; i < timeStart.length; i++) {
//    		timeStart[i].setEditable(editable);
//    		timeEnd[i].setEditable(editable);
//    	}

    }
	public static ChangeWorkingTimeDialogBox getInstance(Frame owner, Project project, WorkingCalendar cal, List documentCalendars, boolean restrict, UndoController undoController) {
		return new ChangeWorkingTimeDialogBox(owner, project,cal,documentCalendars, restrict,undoController);
	}

	private ChangeWorkingTimeDialogBox(Frame owner, Project project, WorkingCalendar cal, List documentCalendars, boolean restrict,UndoController undoController)  {
		super(owner, Messages.getString("ChangeWorkingTimeDialogBox.ChangeWorkingTime"), true); //$NON-NLS-1$
		this.documentCalendars = documentCalendars;
		this.project=project;
		this.restrict = restrict;
		this.undoController = undoController;
//		ProjectFactory projectFactory = ((MainFrame)owner).getProjectFactory();
		ProjectFactory projectFactory = GraphicManager.getInstance(this).getProjectFactory();
		ArrayList projCals = projectFactory.getPortfolio().extractCalendars();
		projectCalendars = new ArrayList();
		Iterator i = projCals.iterator();
		WorkingCalendar current;
		while (i.hasNext()) { // add all non base cals that are project cals
			current =(WorkingCalendar)i.next();
			if (!current.isBaseCalendar())
				projectCalendars.add(current);
		}

	    newCalendar = new JButton(Messages.getString("ChangeWorkingTimeDialogBox.New")); //$NON-NLS-1$
		addDocHelp("Change_Working_Time_Dialog");

		form = new Form();
		form.setCalendar(cal);
	}

	private void setCal(WorkingCalendar cal) {
		editedCalendar = cal;
		form.setCalendar(CalendarService.getInstance().makeScratchCopy(cal));
		calendarType.setSelectedItem(editedCalendar);


	}
	class ListRenderer extends DefaultListCellRenderer {
		private Icon resourceIcon = IconManager.getIcon("man");	 //$NON-NLS-1$
		private Icon greenCircle = IconManager.getIcon("greenCircle");	 //$NON-NLS-1$

		public Component getListCellRendererComponent(JList arg0, Object arg1,
				int arg2, boolean arg3, boolean arg4) {
			// TODO Auto-generated method stub
			Component c = super.getListCellRendererComponent(arg0, arg1, arg2, arg3,
					arg4);
			if (documentCalendars != null && documentCalendars.contains(arg1))
				setIcon(resourceIcon);
			else if (projectCalendars.contains(arg1))
				setIcon(greenCircle);
			return c;
		}
	}
	private void fillInCalendarNames() {
		ArrayList all = new ArrayList();
		CalendarService service = CalendarService.getInstance();
		all.addAll(service.getBaseCalendars());
		all.addAll(projectCalendars);

		if (documentCalendars != null)
			all.addAll(documentCalendars);
		ComboBoxModel calModel = new DefaultComboBoxModel(all.toArray());
		calendarType.setModel(calModel);
	}

	private void clearLastSelection() {
		lastSelection.clear();
		for (int i=0; i < 7; i++)
			lastWeekSelection[i] = false;
	}
	protected void initControls() {
	    calendarType = new JComboBox() ;
	    calendarType.setRenderer( new ListRenderer());
	    fillInCalendarNames();
	    basedOnText =  new JLabel();

	    sdCalendar=new CalendarView();

	    unknownWorkingTime= new JRadioButton();
	    notEditable = new JLabel(Messages.getString("ChangeWorkingTimeDialogBox.NotEdiableMessage")); // html provides word wrap //$NON-NLS-1$
	    caution = new JLabel(Messages.getString("ChangeWorkingTimeDialogBox.ModificationMessage")); // html provides word wrap //$NON-NLS-1$

	    // TODO persist calendars correctly and hook this up
	    notEditable.setVisible(false);
	    caution.setVisible(false);

	    defaultWorkingTime= new JRadioButton(Messages.getString("ChangeWorkingTimeDialogBox.UseDefault")); //$NON-NLS-1$
	    nonWorking= new JRadioButton(Messages.getString("ChangeWorkingTimeDialogBox.NonWorkingTime")); //$NON-NLS-1$
	    working= new JRadioButton(Messages.getString("ChangeWorkingTimeDialogBox.NonDefaultWorkingTime")); //$NON-NLS-1$
	    datesSetting= new ButtonGroup();
	    datesSetting.add(unknownWorkingTime);
	    datesSetting.add(defaultWorkingTime);
	    datesSetting.add(nonWorking);
	    datesSetting.add(working);

	    timeStart=new JTextField[Settings.CALENDAR_INTERVALS];
	    timeEnd=new JTextField[Settings.CALENDAR_INTERVALS];
	    DocumentListener makeDirtyListener = new DocumentListener(){
            public void changedUpdate(DocumentEvent e) {
		           dirtyWorkingHours=true;
	            }
	            public void insertUpdate(DocumentEvent e) {
	                dirtyWorkingHours=true;
	            }
	            public void removeUpdate(DocumentEvent e) {
	                dirtyWorkingHours=true;
	            }
	    	};

	    for (int i=0;i<timeStart.length;i++){
		    timeStart[i]=new JTextField(""); //$NON-NLS-1$
		    timeStart[i].setEnabled(false);
		    timeStart[i].getDocument().addDocumentListener(makeDirtyListener);
		    timeEnd[i]=new JTextField(""); //$NON-NLS-1$
		    timeEnd[i].setEnabled(false);
		    timeEnd[i].getDocument().addDocumentListener(makeDirtyListener);
	    }


	    defaultWorkingTime.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
		        setWorkingHours(null);
			    CalendarService service=CalendarService.getInstance();
			    WorkingCalendar wc=form.getCalendar();
			    service.makeDefaultDays(wc,sdCalendar.getSelectedFixedIntervals(), sdCalendar.getSelectedWeekDays());
			    dirtyWorkingHours = false;
			    updateWorkingHours();
			    updateView();
			    clearLastSelection();

		    }});

	    nonWorking.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
		        setWorkingHours(null);
			    CalendarService service=CalendarService.getInstance();
			    WorkingCalendar wc=form.getCalendar();
			    WorkingCalendar copy = wc.makeScratchCopy();
			    try {
			    	// try on copy first
					service.setDaysNonWorking(copy,sdCalendar.getSelectedFixedIntervals(), sdCalendar.getSelectedWeekDays());
				    service.setDaysNonWorking(wc,sdCalendar.getSelectedFixedIntervals(), sdCalendar.getSelectedWeekDays());
				} catch (InvalidCalendarException e1) {
					Alert.error(e1.getMessage(),ChangeWorkingTimeDialogBox.this);
					return;
				}
			    dirtyWorkingHours = false;
			    updateWorkingHours();
			    updateView();
	            clearLastSelection();
		    }});

	    working.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
			    CalendarService service=CalendarService.getInstance();
			    WorkingCalendar wc=form.getCalendar();

		        setWorkingHours(defaultWorkingHours);
			    WorkingCalendar copy = wc.makeScratchCopy();

			    try {
                    service.setDaysWorkingHours(copy,sdCalendar.getSelectedFixedIntervals(), sdCalendar.getSelectedWeekDays(),defaultWorkingHours);
                    service.setDaysWorkingHours(wc,sdCalendar.getSelectedFixedIntervals(), sdCalendar.getSelectedWeekDays(),defaultWorkingHours);
    			    dirtyWorkingHours = false;
    			    updateWorkingHours();
                    updateView();
                } catch (WorkRangeException e1) {
                    e1.printStackTrace();
                } catch (InvalidCalendarException e2) {
                	Alert.error(e2.getMessage(),ChangeWorkingTimeDialogBox.this);
                	return;
				}
                clearLastSelection();
		    }});
	    sdCalendar.addPropertyChangeListener(new PropertyChangeListener(){
	        final CalendarService service=CalendarService.getInstance();
	        public void propertyChange(PropertyChangeEvent e){
//	        	System.out.println("propery change");
	            String property=e.getPropertyName();
	            if ("lastDisplayedDate".equals(property)||"firstDisplayedDate".equals(property)){ //$NON-NLS-1$ //$NON-NLS-2$
	            	updateView();
	            }else if ("selectedDates".equals(property)){ //$NON-NLS-1$
	            	updateWorkingHours();
	            }
	        	dirtyWorkingHours = false;
	        }
	    });
		setCal(form.getCalendar());
		// add listener at end so above setCal won't trigger update
		calendarType.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e){
		    	WorkingCalendar cal = (WorkingCalendar)calendarType.getSelectedItem();
		    	if (cal != form.getCalendar()) {
		    		setNewCalendar(cal);
			    	setEditable(isCalEditable(cal));
		    	}
		    }});

		calendarType.setEnabled(!restrict);
		setEditable(isCalEditable(form.getCalendar()));
		newCalendar.setVisible(!restrict);
	}

	private boolean isCalEditable(WorkingCalendar cal) {
    	boolean editable = projectCalendars.contains(cal);
    	if (GraphicManager.getInstance().isEditingMasterProject()) // always editable if master project
    		editable = true;
    	return editable;

	}
	private void setNewCalendar(WorkingCalendar cal) {
		saveIfNeeded();
        setCal(cal);
		updateView();

	}
	private void saveWorkingHoursChanges(boolean saveCalendar){
	    try {
	        WorkingHours hours=new WorkingHours();
	        String startS,endS;
	        for (int i=0;i<timeStart.length;i++){
	            startS=timeStart[i].getText();
	            endS=timeEnd[i].getText();
	            if (startS!=null&&endS!=null&&startS.length()>0&&endS.length()>0){
	                hours.setInterval(i,parseTime(startS),parseTime(endS));
	            } else{
	                if (startS.length()==0&&endS.length()==0)
	                    break;
	                else{
	                    Alert.warn(Messages.getString("Message.badTimeFormat"),this); //$NON-NLS-1$
	                    return;
	                }
	            }
	        }
		    CalendarService service=CalendarService.getInstance();
		    WorkingCalendar wc=form.getCalendar();
		    WorkingCalendar copy = wc.makeScratchCopy();
		    service.setDaysWorkingHours(copy,lastSelection,lastWeekSelection,hours);
		    service.setDaysWorkingHours(wc,lastSelection,lastWeekSelection,hours);
		    unsaved = false;

		    if (saveCalendar) {
		    	saveCalendar();
		    } else {
		    	unsaved = true;
		    }
		    //System.out.println("Saved "+lastSelection);
	    } catch (WorkRangeException e) {
	        Alert.warn(Messages.getString("Message.badTimeIntervals"),this); //$NON-NLS-1$
	    } catch (ParseException e) {
	        Alert.warn(Messages.getString("Message.badTimeFormat"),this); //$NON-NLS-1$
	    } catch (InvalidCalendarException e) {
	    	Alert.warn(e.getMessage(),this);
	    	return;
		}
	    updateView();
	}

	private void saveCalendar() {
		unsaved = false;
		CalendarService service=CalendarService.getInstance();
		WorkingCalendar wc=form.getCalendar();
		UndoableEditSupport undoableEditSupport=undoController.getEditSupport();
		if (undoableEditSupport!=null){
			undoableEditSupport.postEdit(new CalendarEdit(editedCalendar,wc));
		}

		service.assignCalendar(editedCalendar,wc);
		service.saveAndUpdate(editedCalendar);

	}
	public void saveIfNeeded() {
		if (dirtyWorkingHours)
			saveWorkingHoursChanges(true);
		else if (unsaved)
			saveCalendar();
	}

	private Calendar _calendar=DateTime.calendarInstance();
	public long getTimeInMillis(int h,int m){
        _calendar.setTimeInMillis(0);
        _calendar.set(Calendar.HOUR_OF_DAY,h);
        _calendar.set(Calendar.MINUTE,m);
        return _calendar.getTimeInMillis();
	}


	private String formatTime(long time) {
		GregorianCalendar cal = DateTime.calendarInstance();;
		cal.setTimeInMillis(time);
	//	cal.roll(GregorianCalendar.HOUR_OF_DAY,false);
		return hourFormat.format(cal.getTime());
	}
	private long parseTime(String s) throws ParseException {
		GregorianCalendar cal = DateTime.calendarInstance();;
		cal.setTime(hourFormat.parse(s));
	//	cal.roll(GregorianCalendar.HOUR_OF_DAY,true);
		return cal.getTimeInMillis();
	}
	private void setWorkingHours(WorkingHours hours){
//	    System.out.println("setting working hours" + hours);
		// if not working treat as empty
		if (hours != null && hours.getDuration() == 0)
			hours = null;
	    for (int i=0;i<timeStart.length;i++){
	         timeStart[i].setEnabled(hours!=null);
	         timeEnd[i].setEnabled(hours!=null);
	         if (hours==null){
		         timeStart[i].setText(""); //$NON-NLS-1$
		         timeEnd[i].setText(""); //$NON-NLS-1$
	         }
	    }

	    if (hours!=null){
            HasStartAndEnd interval;
        //    int j=0;
            int intervals = hours.getIntervals().size();
            for (int j = 0; j < Settings.CALENDAR_INTERVALS; j++) {
//            for (Iterator i=hours.getIntervals().iterator();i.hasNext();j++){
                interval=(HasStartAndEnd)hours.getInterval(j);
//				i.next();
                if (interval!=null){
                    String startS=formatTime(interval.getStart());
                    timeStart[j].setText(startS);
                    timeEnd[j].setText(formatTime(interval.getEnd()));
                } else {
                	timeStart[j].setText(""); //$NON-NLS-1$
                	timeEnd[j].setText(""); //$NON-NLS-1$
                }
            }
            clearLastSelection();
            lastSelection.addAll(sdCalendar.getSelectedFixedIntervals());
            for (int i =0; i <7; i++)
            	lastWeekSelection[i] = sdCalendar.getSelectedWeekDays()[i];
	    }

	    //Call at the end because setText causes dirtyWorkingHours to become true
        dirtyWorkingHours=false;
	}


	protected void updateView(){

	    CalendarService service=CalendarService.getInstance();
	    WorkingCalendar wc=form.getCalendar();
	    if (wc.isBaseCalendar()) {
	    	basedOnText.setText(" "); // a space.  need a space for vertical spacing //$NON-NLS-1$
	    } else {
	    	basedOnText.setText(Messages.getString("ChangeWorkingTimeDialogBox.BasedOn") + wc.getBaseCalendar().getName()); //$NON-NLS-1$
	    }

	    long first=sdCalendar.getFirstDisplayedDate();
	    long last=sdCalendar.getLastDisplayedDate();
	    Calendar calendar=DateTime.calendarInstance();
	    calendar.setTimeInMillis(first);

	    sdCalendar.setFlaggedDates(null);
        sdCalendar.setColorDates(null);

	    DayDescriptor day;
	    ArrayList flaggedDates=new ArrayList();
	    ArrayList colorDates=new ArrayList();
	    while(calendar.getTimeInMillis()<=last){
	        day=service.getDay(wc,calendar.getTimeInMillis());
	        if (day.isModified())
	            flaggedDates.add(new Long(calendar.getTimeInMillis()));
	        if (!day.isWorking())
	            colorDates.add(new Long(calendar.getTimeInMillis()));
	        calendar.add(Calendar.DATE,1);
	    }


	    if (flaggedDates.size()>0)
	    	sdCalendar.setFlaggedDates(toLongArray(flaggedDates));
	    if (colorDates.size()>0)
	    	sdCalendar.setColorDates(toLongArray(colorDates));

	    boolean colorWeekDates[] = new boolean[7];
	    boolean flaggedWeekDates[] = new boolean[7];
	    for (int i =0; i < 7; i++) {
	    	day = service.getWeekDay(wc,i+1);
	    	if (day.isModified())
    			flaggedWeekDates[i] = true;
	    	if (!day.isWorking())
    			colorWeekDates[i] = true;
	    }
	    sdCalendar.setColorWeekDates(colorWeekDates);
	    sdCalendar.setFlaggedWeekDates(flaggedWeekDates);
	   // updateWorkingHours();
	    //System.out.println(service.dump(wc));
	}

	//stupid jdnc calendar use long[]
	public long[] toLongArray(ArrayList list){
	    //if (list.size()==0) return null;
	    long[] array=new long[list.size()];
	    int j=0;
	    for (Iterator i=list.iterator();i.hasNext();j++) array[j]=((Long)i.next()).longValue();
	    return array;
	}


	private void updateWorkingHours() {
//		System.out.println("updating working hours");
        final CalendarService service=CalendarService.getInstance();

		 if (dirtyWorkingHours){
	        saveWorkingHoursChanges(false);
	    }

        DayDescriptor day=service.getDay(form.getCalendar(),sdCalendar.getSelectedFixedIntervals(),sdCalendar.getSelectedWeekDays());
        if (day==null){
//            System.out.println("none");
            ChangeWorkingTimeDialogBox.this.datesSetting.setSelected(ChangeWorkingTimeDialogBox.this.unknownWorkingTime.getModel(),true);
            setWorkingHours(null);
        }else if (!day.isModified()){
//            System.out.println("default");
            ChangeWorkingTimeDialogBox.this.datesSetting.setSelected(ChangeWorkingTimeDialogBox.this.defaultWorkingTime.getModel(),true);
            setWorkingHours(day.getWorkingHours());
        }else if (!day.isWorking()){
//            System.out.println("non working");
            ChangeWorkingTimeDialogBox.this.datesSetting.setSelected(ChangeWorkingTimeDialogBox.this.nonWorking.getModel(),true);
            setWorkingHours(null);
        }else{
//            System.out.println("working");
            ChangeWorkingTimeDialogBox.this.datesSetting.setSelected(ChangeWorkingTimeDialogBox.this.working.getModel(),true);
            setWorkingHours(day.getWorkingHours());
        }
        dirtyWorkingHours = true;
	}

	private JComponent createSettingsPanel() {
		FormLayout settingsLayout = new FormLayout("100dlu", //$NON-NLS-1$
		"p,0dlu ,p,3dlu ,p,3dlu p,3dlu,p,0dlu ,p,0dlu ,p,3dlu ,p,3dlu ,p,3dlu ,p,3dlu ,p,3dlu ,p,3dlu ,p,3dlu ,p,3dlu ,p"); //$NON-NLS-1$
		DefaultFormBuilder settingBuilder = new DefaultFormBuilder(settingsLayout);

		settingBuilder.addLabel(Messages.getString("ChangeWorkingTimeDialogBox.For")); //$NON-NLS-1$
		settingBuilder.nextLine(2);
		settingBuilder.add(calendarType);
		settingBuilder.nextLine(2);
		settingBuilder.add(basedOnText);
		settingBuilder.nextLine(2);
		settingBuilder.add(notEditable);
		settingBuilder.add(caution);
		settingBuilder.nextLine(2);
		settingBuilder.add(defaultWorkingTime);
		settingBuilder.nextLine(2);
		settingBuilder.add(nonWorking);
		settingBuilder.nextLine(2);
		settingBuilder.add(working);
		settingBuilder.nextLine(2);

		JPanel time = new JPanel();
		time.setLayout(new GridLayout(1,2));
		time.add(new JLabel(Messages.getString("ChangeWorkingTimeDialogBox.From"))); //$NON-NLS-1$
		time.add(new JLabel(Messages.getString("ChangeWorkingTimeDialogBox.To"))); //$NON-NLS-1$
		settingBuilder.add(time);


		for (int i=0;i<timeStart.length;i++){
			JPanel timePanel = new JPanel();
			timePanel.setLayout(new GridLayout(1,2));
			timePanel.add(timeStart[i]);
			timePanel.add(timeEnd[i]);
			settingBuilder.nextLine(2);
			settingBuilder.add(timePanel);
		}
		return settingBuilder.getPanel();
}

	public JComponent createContentPanel() {

		initControls();

		FormLayout layout = new FormLayout(
		        "300dlu:grow", //$NON-NLS-1$
	    		  "p,p,fill:260dlu:grow"); //$NON-NLS-1$

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();



//	    JSplitPane settingsPanel=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//	    settingsPanel.setTopComponent(createSettingsPanel());
//	    settingsPanel.setBottomComponent(new JPanel());
//	    settingsPanel.setDividerSize(0);
	    JSplitPane panel=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//		panel.setLeftComponent(settingsPanel);
		panel.setLeftComponent(createSettingsPanel());

		panel.setRightComponent(sdCalendar);

		JPanel buttonPanel=new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton backButton=new JButton(IconManager.getIcon("calendar.back")); //$NON-NLS-1$
		backButton.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent e) {
	            long first=sdCalendar.getFirstDisplayedDate();
	            Calendar calendar=DateTime.calendarInstance();
	    	    calendar.setTimeInMillis(sdCalendar.getLastDisplayedDate());
	    	    int nbMonth=0;
	    	    while(calendar.getTimeInMillis()>first){
	    	        calendar.add(Calendar.MONTH,-1);
	    	        nbMonth++;
	    	    }
	    	    calendar.setTimeInMillis(first);
	    	    calendar.add(Calendar.MONTH,-nbMonth);
	    	    sdCalendar.setFirstDisplayedDate(calendar.getTimeInMillis());
	        }
        });
		JButton todayButton=new JButton(IconManager.getIcon("calendar.today")); //$NON-NLS-1$
		todayButton.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent e) {
	            Calendar calendar=DateTime.calendarInstance();
	    	    calendar.setTimeInMillis(System.currentTimeMillis());
	    	    calendar.set(Calendar.DATE,1);
	    	    sdCalendar.setFirstDisplayedDate(calendar.getTimeInMillis());
	        }
        });
		JButton forwardButton=new JButton(IconManager.getIcon("calendar.forward")); //$NON-NLS-1$
		forwardButton.addActionListener(new ActionListener(){
	        public void actionPerformed(ActionEvent e) {
	            Calendar calendar=DateTime.calendarInstance();
	    	    calendar.setTimeInMillis(sdCalendar.getLastDisplayedDate());
	    	    calendar.add(Calendar.DATE,1);
	    	    sdCalendar.setFirstDisplayedDate(calendar.getTimeInMillis());
	        }
        });
		buttonPanel.add(backButton);
		buttonPanel.add(todayButton);
		buttonPanel.add(forwardButton);
		builder.nextLine();
		builder.append(buttonPanel);
		builder.nextLine();
		builder.append(panel);

//		builder.append(newCalendar);


		return builder.getPanel();
	}


	public Object getBean(){
		return form;
	}

	public ButtonPanel createButtonPanel() {
		createOkCancelButtons();
	    newCalendar.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
			    	NewBaseCalendarDialog dialog = NewBaseCalendarDialog.getInstance(owner,null);
			    	if (dialog.doModal()) {
			    		fillInCalendarNames();
			    		WorkingCalendar cal = dialog.getNewCalendar();
			    		setNewCalendar(cal);
			    	}
	            }
			 });

	    options = new JButton(Messages.getString("ChangeWorkingTimeDialogBox.Options")); //$NON-NLS-1$
	    options.addActionListener(new ActionListener(){
			    public void actionPerformed(ActionEvent e){
			    	CalendarOption option = project.getCalendarOption();
			    	if (option == null)
			    		option = CalendarOption.getInstance();
			    	CalendarDialogBox dialog = CalendarDialogBox.getInstance((Frame) ChangeWorkingTimeDialogBox.this.getOwner(),option);
			    	if (dialog.doModal()) {
			    		option = CalendarOption.getNewInstance();
			    		dialog.getForm().copyToOption(option);
			    		CalendarOption.setInstance(option);
			    		project.setCalendarOption(option);
			    	}
			    }
			 });

		ButtonPanel buttonPanel = new ButtonPanel();
		buttonPanel.addButton(newCalendar);
		buttonPanel.addButton(options);
		buttonPanel.addButton(ok);
		buttonPanel.addButton(cancel);
		buttonPanel.add(getHelpButton());
		return buttonPanel;
	}
	public void onOk() {
		saveIfNeeded();
		super.onOk();
	}
}
