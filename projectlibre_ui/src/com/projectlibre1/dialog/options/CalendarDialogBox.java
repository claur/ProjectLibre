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
package com.projectlibre1.dialog.options;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projectlibre1.dialog.AbstractDialog;
import com.projectlibre1.configuration.Settings;
import com.projectlibre1.options.CalendarOption;
import com.projectlibre1.strings.Messages;

/**
 *
 */
public class CalendarDialogBox extends AbstractDialog{
	private static final long serialVersionUID = -6887419605301434923L;

	public static class Form {
        Double hoursPerDay;
        Double hoursPerWeek;
        Double daysPerMonth;
        String startTime;
        String endTime;
        String weekStart;
        String fiscalYearStart;
        Boolean useStartingYear;
        Boolean setAsDefault;
        
        Form(CalendarOption option) {
        	hoursPerDay = new Double(option.getHoursPerDay());
        	hoursPerWeek = new Double(option.getHoursPerWeek());
        	daysPerMonth = new Double(option.getDaysPerMonth());
        	startTime = option.getDefaultStartHour() +""; //$NON-NLS-1$
        	endTime = option.getDefaultEndHour()+""; //$NON-NLS-1$
        	
        }
        public void copyToOption(CalendarOption option) {
        	option.setHoursPerDay(hoursPerDay.doubleValue());
        	option.setHoursPerWeek(hoursPerWeek.doubleValue());
        	option.setDaysPerMonth(daysPerMonth.doubleValue());
        	
        }
        public Double getDaysPerMonth() {
            return daysPerMonth;
        }
        public void setDaysPerMonth(Double daysPerMonth) {
            this.daysPerMonth = daysPerMonth;
        }
        public String getEndTime() {
            return endTime;
        }
        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
        public String getFiscalYearStart() {
            return fiscalYearStart;
        }
        public void setFiscalYearStart(String fiscalYearStart) {
            this.fiscalYearStart = fiscalYearStart;
        }
        public Double getHoursPerDay() {
            return hoursPerDay;
        }
        public void setHoursPerDay(Double hoursPerDay) {
            this.hoursPerDay = hoursPerDay;
        }
        public Double getHoursPerWeek() {
            return hoursPerWeek;
        }
        public void setHoursPerWeek(Double hoursPerWeek) {
            this.hoursPerWeek = hoursPerWeek;
        }
        public Boolean getSetAsDefault() {
            return setAsDefault;
        }
        public void setSetAsDefault(Boolean setAsDefault) {
            this.setAsDefault = setAsDefault;
        }
        public String getStartTime() {
            return startTime;
        }
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
        public Boolean getUseStartingYear() {
            return useStartingYear;
        }
        public void setUseStartingYear(Boolean useStartingYear) {
            this.useStartingYear = useStartingYear;
        }
        public String getWeekStart() {
            return weekStart;
        }
        public void setWeekStart(String weekStart) {
            this.weekStart = weekStart;
        }
    }	
        
        private Form form;
       
        JSpinner hoursPerDay;
        JSpinner hoursPerWeek;
        JSpinner daysPerMonth;
        JTextField startTime;
        JTextField endTime;
        JComboBox weekStart;
        JComboBox fiscalYearStart;
        JCheckBox useStartingYear;
        JButton setAsDefault;

        
    	public static CalendarDialogBox getInstance(Frame owner, CalendarOption option) {
    		return new CalendarDialogBox(owner, option);
    	}

    	private CalendarDialogBox(Frame owner, CalendarOption option) {
    		super(owner, Messages.getString("CalendarDialogBox.DurationSettings"), true); //$NON-NLS-1$
   			this.form = new Form(option);
   			addDocHelp("Calendar_Options");
    	}
    	
    	protected void initControls() {
    	    
    	    String[] week =new String [] {Messages.getString("CalendarDialogBox.Monday"),Messages.getString("CalendarDialogBox.Tuesday"),Messages.getString("CalendarDialogBox.Wednesday"),Messages.getString("CalendarDialogBox.Thursday"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    	            Messages.getString("CalendarDialogBox.Friday"),Messages.getString("CalendarDialogBox.Saturday"),Messages.getString("CalendarDialogBox.Sunday") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	    };
    	    String[] year =new String [] {Messages.getString("CalendarDialogBox.January"),Messages.getString("CalendarDialogBox.February"),Messages.getString("CalendarDialogBox.March"),Messages.getString("CalendarDialogBox.April"),Messages.getString("CalendarDialogBox.May"),Messages.getString("CalendarDialogBox.June"),Messages.getString("CalendarDialogBox.July"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    	            Messages.getString("CalendarDialogBox.August"),Messages.getString("CalendarDialogBox.September"),Messages.getString("CalendarDialogBox.October"),Messages.getString("CalendarDialogBox.November"),Messages.getString("CalendarDialogBox.December") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    	    };
    	    weekStart=new JComboBox(week);
    	    fiscalYearStart=new JComboBox(year);   
    	    useStartingYear=new JCheckBox(Messages.getString("CalendarDialogBox.UserStartingYearForFVNumbering")); //$NON-NLS-1$
    	    useStartingYear.setEnabled(false);

            startTime= new JTextField (Messages.getString("CalendarDialogBox.EightAM")); //$NON-NLS-1$
            endTime= new JTextField (Messages.getString("CalendarDialogBox.SixPM")); //$NON-NLS-1$
    	              
    		hoursPerDay = new JSpinner(new SpinnerNumberModel(form.getHoursPerDay().doubleValue(),0,24.0,0.5));
    		JSpinner.NumberEditor editor1;
    		editor1 = new JSpinner.NumberEditor(hoursPerDay,"##.##"); //$NON-NLS-1$
    		hoursPerDay.setEditor(editor1);
    		
    		hoursPerWeek = new JSpinner(new SpinnerNumberModel(form.getHoursPerWeek().doubleValue(),0,168.0,0.5));
    		JSpinner.NumberEditor editor2;
    		editor2 = new JSpinner.NumberEditor(hoursPerWeek,"##.##"); //$NON-NLS-1$
    		hoursPerWeek.setEditor(editor2);
    		
    		daysPerMonth = new JSpinner(new SpinnerNumberModel(form.getDaysPerMonth().doubleValue(),0,31.0,1.0));
    		JSpinner.NumberEditor editor3;
    		editor3 = new JSpinner.NumberEditor(daysPerMonth,"##.##"); //$NON-NLS-1$
    		daysPerMonth.setEditor(editor3);
    		
            setAsDefault= new JButton(Messages.getString("CalendarDialogBox.SetAsDefault")); //$NON-NLS-1$

    		fiscalYearStart.addActionListener(new ActionListener(){
    		    public void actionPerformed(ActionEvent e){
    	    	    if (fiscalYearStart.getSelectedItem().equals(Messages.getString("CalendarDialogBox.January"))){ //$NON-NLS-1$
    	    	        useStartingYear.setEnabled(false);
    	    	    }else{
    	    	        useStartingYear.setEnabled(true);
    	    	    }
    		    }});
    		

    		
    	}
 
    
    	protected boolean bind(boolean get) {
    		if (form == null)
    			return false;
    		if (get) {
    		    weekStart.setSelectedItem(form.getWeekStart());
    		    fiscalYearStart.setSelectedItem(form.getFiscalYearStart()); 		    
    		    useStartingYear.setSelected((form.getUseStartingYear()).booleanValue());
    		    startTime.setText(/*form.getStartTime()*/Messages.getString("CalendarDialogBox.Eight")); //$NON-NLS-1$
    		    endTime.setText(/*form.getEndTime()*/Messages.getString("CalendarDialogBox.Seventeen")); //$NON-NLS-1$
    		    hoursPerDay.setValue(form.getHoursPerDay());
    		    hoursPerWeek.setValue(form.getHoursPerWeek());
    		    daysPerMonth.setValue(form.getDaysPerMonth());    		    
    		    setAsDefault.setSelected((form.getSetAsDefault()).booleanValue());
    		    
 
 
    		} else {
    			form.setWeekStart((String)weekStart.getSelectedItem());
    			form.setFiscalYearStart((String)fiscalYearStart.getSelectedItem());  		    
    			Boolean b1=new Boolean(useStartingYear.isSelected());
    			form.setUseStartingYear(b1);
    			form.setStartTime(startTime.getText());
    			form.setEndTime(endTime.getText());
    			form.setHoursPerDay((Double)hoursPerDay.getValue());
    			form.setHoursPerWeek((Double)hoursPerWeek.getValue());
    			form.setDaysPerMonth((Double)daysPerMonth.getValue());    			
    			Boolean b2=new Boolean(setAsDefault.isSelected());
    			form.setSetAsDefault(b2);

    		}
    		return true;
    	}
    	
    	public JComponent createContentPanel() {
    	
    		initControls();
    		
    		FormLayout layout = new FormLayout(
    		        "p,3dlu,p,p:grow", //$NON-NLS-1$
    	    		  "p,3dlu,p,3dlu,p,3dlu,p"); //$NON-NLS-1$

    		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
    		builder.setDefaultDialogBorder();
    		CellConstraints cc = new CellConstraints();
			builder.add(new JLabel(Messages.getString("CalendarDialogBox.TheseSettingsOnlyApplyToDuration")),cc.xyw(builder.getColumn(), builder //$NON-NLS-1$
					.getRow(), 4));
    		builder.nextLine(2);
    		builder.append(Messages.getString("CalendarDialogBox.HoursPerday"),hoursPerDay); //$NON-NLS-1$
    		builder.nextLine(2);
    		builder.append(Messages.getString("CalendarDialogBox.HoursPerWeek"),hoursPerWeek); //$NON-NLS-1$
    		builder.nextLine(2);
    		builder.append(Messages.getString("CalendarDialogBox.DaysPerMonth"),daysPerMonth); //$NON-NLS-1$
  

    		return builder.getPanel();
    	}
//    	public JComponent createContentPanel() {
//        	
//    		initControls();
//    		
//    		FormLayout layout = new FormLayout(
//    		        "left:80dlu,3dlu,50dlu, 3dlu,130dlu,3dlu",
//    	    		  "p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,p,3dlu,fill:10dlu:grow");
//
//    		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
//    		builder.setDefaultDialogBorder();
//    		CellConstraints cc = new CellConstraints();
//    		
//    		builder.addSeparator("Local calendar options");
//    		builder.nextLine(4);
//    		builder.append("Week starts on :",weekStart);
//    		builder.nextLine(2);
//    		builder.append("Fiscal year starts in :",fiscalYearStart);
//    		builder.nextLine(2);
//    		builder.nextColumn(4);
//    		builder.append(useStartingYear);
//    		builder.nextLine(4);
//    		builder.addSeparator("");
//    		builder.nextLine(4);
//    		builder.append("Default start time :",startTime);
//    		builder.nextLine(2);
//    		builder.append("Default end time :",endTime);
//    		builder.nextLine(4);
//    		builder.addSeparator("");
//    		builder.nextLine(4);
//    		builder.append("Hours per day :",hoursPerDay);
//    		builder.nextLine(2);
//    		builder.append("Hours per week :",hoursPerWeek);
//    		builder.nextLine(2);
//    		builder.append("Days per month :",daysPerMonth);
//    		builder.nextLine(4);
//    		builder.append(setAsDefault);
//    		builder.nextLine(2);
//  
//
//    		return builder.getPanel();
//    	}
    	 	
    	public Object getBean(){
    		return form;
    	}

		public Form getForm() {
			return form;
		}
}
