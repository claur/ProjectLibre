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
package com.projectlibre1.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.lang.StringEscapeUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.projectlibre.ui.ribbon.ProjectLibreRibbonUI;
import com.projectlibre1.pm.graphic.frames.GraphicManager;
import com.projectlibre1.configuration.Settings;
import com.projectlibre1.menu.MenuManager;
import com.projectlibre1.pm.snapshot.SnapshottableImpl;
import com.projectlibre1.preference.ConfigurationFile;
import com.projectlibre1.strings.Messages;
import com.projectlibre1.util.Alert;

import net.sf.mpxj.common.RtfHelper;

public final class LocaleDialog extends AbstractDialog {
	private static final long serialVersionUID = 1L;

	JComboBox<String> languageCombo;
	JComboBox<Country> countryCombo;
	JCheckBox externalCheckbox=new JCheckBox(Messages.getString("Text.ExternalLocaleUse"));
	JTextField directoryField=new JTextField();
	JButton directorySetButton=new JButton(Messages.getString("Text.ExternalLocaleDirectorySet"));
	JButton directoryUpdateButton=new JButton(Messages.getString("Text.ExternalLocaleDirectoryUpdate"));
	JButton directoryExportButton=new JButton(Messages.getString("Text.ExternalLocaleDirectoryExport"));
	JTextPane messageArea;
	JTable externalList;
	AbstractTableModel externalListModel;
	String[] externalListColumns={"code", "client.properties", "menu.properties"};
	
	ArrayList<LanguageProperties> files=new ArrayList<LanguageProperties>();
	
	public enum FileStatus {
	    OK(Messages.getString("LocaleDialog.FileStatusOk")),
	    MISSING(Messages.getString("LocaleDialog.FileStatusMissing")),
	    ERROR(Messages.getString("LocaleDialog.FileStatusFormatError"));

	    public final String message;

	    private FileStatus(String message) {
	        this.message = message;
	    }
	}
	
	
	class LanguageProperties implements Comparable<String>{
		String code;
		File client;
		FileStatus clientStatus;
		File menu;
		FileStatus menuStatus;

		
		/**
		 * @param code
		 * @param client
		 * @param clientStatus
		 * @param menu
		 * @param menuStatus
		 */
		public LanguageProperties(String code, File client, FileStatus clientStatus, File menu, FileStatus menuStatus) {
			super();
			this.code = code;
			this.client = client;
			this.clientStatus = clientStatus;
			this.menu = menu;
			this.menuStatus = menuStatus;
		}
		
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public File getClient() {
			return client;
		}
		public void setClient(File client) {
			this.client = client;
		}
		public File getMenu() {
			return menu;
		}
		public void setMenu(File menu) {
			this.menu = menu;
		}
	
		public FileStatus getClientStatus() {
			return clientStatus;
		}
		public void setClientStatus(FileStatus clientStatus) {
			this.clientStatus = clientStatus;
		}
		public FileStatus getMenuStatus() {
			return menuStatus;
		}
		public void setMenuStatus(FileStatus menuStatus) {
			this.menuStatus = menuStatus;
		}
		@Override
		public int compareTo(String o) {
			return code.compareTo(o);
		}	
		
	}
	
	
	String[] slocales=Settings.LANGUAGES.split(";");
	Set<String> allLocales=new TreeSet();
	Map<String, String> transOri=new HashMap<String, String>();
	Map<String, String> oriTrans=new HashMap<String, String>();


	protected boolean bind(boolean get) {
		Preferences pref=Preferences.userNodeForPackage(ConfigurationFile.class);
		if (get) {
			updateLocales(true);
			boolean custom=pref.getBoolean("useExternalLocales",false);
			externalCheckbox.setSelected(custom);
			directoryField.setEnabled(custom);
			directorySetButton.setEnabled(custom);
			directoryUpdateButton.setEnabled(custom);
			directoryExportButton.setEnabled(custom);
			
			directoryField.setText(pref.get("externalLocalesDirectory",""));
		} else {
			String language=(String)languageCombo.getSelectedItem();
			String country=((Country)countryCombo.getSelectedItem()).getCode();
			String trans=transOri.get(language);
			String code=trans==null?language:trans;
			if (!"".equals(country))
				code+="_"+country;
			pref.put("locale",code);
			pref.putBoolean("useExternalLocales", externalCheckbox.isSelected());
			pref.put("externalLocalesDirectory", directoryField.getText());
		}
		return true;
	}
	
	static class Country implements Comparable<Country>{
		String code;
		String name;
		public Country() {
			super();
			this.code = "";
			this.name = "";
		}
		/**
		 * @param code
		 * @param name
		 */
		public Country(String code, String name) {
			super();
			this.code = code;
			this.name = name;
		}
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		@Override
		public int hashCode() {
			return code.hashCode();
		}
		@Override
		public String toString() {
			return name.toString();
		}
		@Override
		public int compareTo(Country o) {
			return code.compareTo(o.getCode());
		}
		@Override
		public boolean equals(Object o) {
			if (o==null || ! (o instanceof Country))
				return false;
			else return code.equals(((Country)o).getCode());
		}
		
	}
	
	public static final Country DEFAULT_COUNTRY=new Country();

	
	public void updateLocales(boolean init) {
		String currentLocaleCode=Preferences.userNodeForPackage(ConfigurationFile.class).get("locale","default");
		String currentLanguage;
		Country currentCountry;
		if ("default".equals(currentLocaleCode)) {
			currentLanguage=currentLocaleCode;
			currentCountry=DEFAULT_COUNTRY;
		}else {
			String[] codes=ConfigurationFile.getLocaleCodes(currentLocaleCode);
			currentLanguage=codes[0];
			String code=codes[1];
			if (code==null ||
					"".equals(code))
				currentCountry=DEFAULT_COUNTRY;
			else {
			    Locale locale = new Locale("en", code);
	        	currentCountry=new Country(code, locale.getDisplayCountry(locale));
			}
		}
		
		
		if (init/*countryCombo.getItemCount()==0*/) { //only set once
	        TreeSet<Country> countrySet=new TreeSet<Country>();

	        countrySet.add(DEFAULT_COUNTRY);
	        String[] countryCodes=Locale.getISOCountries();
	        for (String countryCode: countryCodes) {
			    Locale locale = new Locale("en", countryCode);
	        	countrySet.add(new Country(countryCode, locale.getDisplayCountry(locale)));
	        }
	        for (Country country: countrySet) {
	        	 countryCombo.addItem(country);
	        }
			countryCombo.setSelectedItem(currentCountry);
		}


		for (String locale:slocales) {
			String trans;
			if ("default".equals(locale))
				trans=Messages.getString("Text.DefaultLocale"); 
			else trans=locale;
			oriTrans.put(locale, trans);
			transOri.put(trans, locale);
		}
		String selectedItem;
		if (init){
			String trans=oriTrans.get(currentLanguage);
			selectedItem=trans==null? currentLanguage: trans;
		}else selectedItem=(String)languageCombo.getSelectedItem();
				
             
			
				
		allLocales.clear();		
		languageCombo.removeAllItems();
		


		//add custom locales
		for (LanguageProperties file:files) {
			if (file.getClientStatus()==FileStatus.OK ||
					file.getMenuStatus()==FileStatus.OK)
				allLocales.add(file.code);
		}		
		
		//add preference locale if missing
				
		boolean found=false;
		String defaultLocale=null;
		for (String locale:slocales) {
			if ("default".equals(locale)) {
				defaultLocale=locale;
				continue;
			}
			allLocales.add(locale);
			if (locale.equals(currentLanguage)) {
				found=true;
			}
		}
		if (!found)
			allLocales.add(currentLanguage);

		
		if (defaultLocale!=null)
				languageCombo.addItem(oriTrans.get(defaultLocale));
		for (String locale: allLocales) {
			String trans=oriTrans.get(locale);
			if (!"default".equals(locale))
				languageCombo.addItem(trans==null?locale:trans);
		}
		
		languageCombo.setSelectedItem(selectedItem);
		
	}
	
	public static LocaleDialog getInstance(GraphicManager graphicManager) {
		LocaleDialog instance =null;
		if (instance == null) {
			instance = new LocaleDialog(graphicManager.getFrame());
		} else
			instance.setTitle(Messages.getString("Text.LocaleDialog"));
			instance.addDocHelp("Locale_Dialog");
		
		return instance;
	}

	private LocaleDialog(Frame owner) {
		super(owner, "", true);
		languageCombo=new JComboBox();
		countryCombo=new JComboBox();
		
		messageArea=new JTextPane();
		messageArea.setBackground(null);

		
		externalList=new JTable();
	}

	
	protected void displayMessage(String message) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet attr = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED);
        attr = sc.addAttribute(attr, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        attr = sc.addAttribute(attr, StyleConstants.Italic, true);


        messageArea.setCaretPosition(0);
        messageArea.selectAll();
        messageArea.setCharacterAttributes(attr, false);
        messageArea.replaceSelection(message);
	}
	protected void displayChangeMessage() {
		displayMessage(Messages.getString("Message.localeChange"));
	}

	protected void initControls() {
		bind(true);
        languageCombo.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				displayChangeMessage();
			}
		});
        countryCombo.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				displayChangeMessage();
			}
		});
        externalCheckbox.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				directoryField.setEnabled(externalCheckbox.isSelected());
				directorySetButton.setEnabled(externalCheckbox.isSelected());
				directoryUpdateButton.setEnabled(externalCheckbox.isSelected());
				directoryExportButton.setEnabled(externalCheckbox.isSelected());
				displayChangeMessage();
			}
		});
        directoryField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				displayChangeMessage();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				displayChangeMessage();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				displayChangeMessage();
			}
		});
        
        directorySetButton.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String spath=directoryField.getText();
				File path;
				if (spath==null || spath=="")
					path=FileSystemView.getFileSystemView().getHomeDirectory();
				else {
					path=new File(spath);
					if (!path.isDirectory())
						path=FileSystemView.getFileSystemView().getHomeDirectory();
				}
				JFileChooser fileChooser = new JFileChooser(path);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		        if (fileChooser.showOpenDialog(LocaleDialog.this) == JFileChooser.APPROVE_OPTION) {
		        	displayChangeMessage();
		            File selectedFile = fileChooser.getSelectedFile();
		            directoryField.setText(selectedFile.getPath());
		            refreshFiles();
		        }
		        
			}
		});
        
        directoryUpdateButton.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
		        	displayChangeMessage();
		        	refreshFiles();
			}
		});
        
        directoryExportButton.addActionListener(new ActionListener() {		
 			@Override
 			public void actionPerformed(ActionEvent e) {
 		        	displayChangeMessage();
 		        	refreshExportFiles();
 			}
 		});
        
        externalListModel=new AbstractTableModel() {
			@Override
        	public String getColumnName(int col) {
        		return externalListColumns[col];
        	}
        	
			@Override
			public Object getValueAt(int row, int col) {
				if (row<0 || 
						col<0 ||
						row>=getRowCount() ||
						col>=getColumnCount())
					return null;
				LanguageProperties prop=files.get(row);
				if (col==0)
					return prop.getCode();
				else if (col==1)
					return prop.getClientStatus();
				else return prop.getMenuStatus();
			}
			
			@Override
			public int getRowCount() {
				return files.size();
			}
			
			@Override
			public int getColumnCount() {
				return 3;
			}
		};
        externalList.setModel(externalListModel);
        externalList.setEnabled(false);
//		refreshFiles();
	}
	
//	public void exportResourceFile(String packageDir, String file) {
//        File generatedDir=ConfigurationFile.getGeneratedDirectory(directoryField.getText());
//        if (generatedDir==null)
//        	return;
//		
//        ClassLoader cl = getClass().getClassLoader();
//        try {
//        	InputStream in = cl.getResourceAsStream(packageDir+"/"+file+".properties");
//            if (in == null)
//                throw new FileNotFoundException();
//            
//            byte[] buf = new byte[in.available()];
//            in.read(buf);
//            File target = new File(generatedDir,file+".properties");
//            OutputStream out = new FileOutputStream(target);
//            out.write(buf);
//            out.close();            	   
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//	public void exportResourceFileUTF8(String packageDir, String file) {
//        File generatedDir=ConfigurationFile.getExportDirectory(directoryField.getText());
//        if (generatedDir==null)
//        	return;
//		
//        ClassLoader cl = getClass().getClassLoader();
//        try {
//        	InputStream in = cl.getResourceAsStream(packageDir+"/"+file+".properties");        	
//            if (in == null)
//            	throw new FileNotFoundException();
//            
//            String text=getFileContent(in, "ISO-8859-1",true);
//            File target = new File(generatedDir,file+".txt");
//            FileWriter out = new FileWriter(target);
//            out.write(text);
//            out.close();            	   
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
	public void exportResourceFile(Class sibling, String file) {
        File generatedDir=ConfigurationFile.getGeneratedDirectory(directoryField.getText());
        if (generatedDir==null)
        	return;
		
        ClassLoader cl = getClass().getClassLoader();
        try {
        	InputStream in = sibling.getResourceAsStream(file+".properties");
            if (in == null)
                throw new FileNotFoundException();
            
            byte[] buf = new byte[in.available()];
            in.read(buf);
            File target = new File(generatedDir,file+".properties");
            OutputStream out = new FileOutputStream(target);
            out.write(buf);
            out.close();            	   
        } catch (IOException e) {
        }
    }
	public void exportResourceFileUTF8(Class sibling, String file) {
        File generatedDir=ConfigurationFile.getExportDirectory(directoryField.getText());
        if (generatedDir==null)
        	return;
		
        ClassLoader cl = getClass().getClassLoader();
        try {
        	InputStream in = sibling.getResourceAsStream(file+".properties");        	
            if (in == null)
            	throw new FileNotFoundException();
            
            String text=getFileContent(in, "ISO-8859-1",true);
            File target = new File(generatedDir,file+".txt");
            FileWriter out = new FileWriter(target);
            out.write(text);
            out.close();            	   
        } catch (IOException e) {
        }
    }
	
	public void refreshFiles() {
		files.clear();
		
		File directory=new File(directoryField.getText());
		if (!directory.isDirectory()) {
			displayMessage(Messages.getString("Message.badLocaleDirectory"));
			if (externalListModel!=null)
				externalListModel.fireTableDataChanged();
			return;
		}
		
		File generated=new File(directory, "import");
		if (generated.exists()) {
			if (!generated.isDirectory()) {
				displayMessage(Messages.getString("Message.badLocaleDirectory"));
				if (externalListModel!=null)
					externalListModel.fireTableDataChanged();
				return;				
			}
		}else {
			if (!generated.mkdir()) {
				displayMessage(Messages.getString("Message.badLocaleDirectory"));
				if (externalListModel!=null)
					externalListModel.fireTableDataChanged();
				return;								
			}
		}


//		Pattern clientPattern = Pattern.compile("client_([^_]{2,3}(_[^_]{2,3})?(_[^_]{2,3})?)(\\.properties)(\\.([^\\.]*))?$");
//		Pattern menuPattern = Pattern.compile("menu_([^_]{2,3}(_[^_]{2,3})?(_[^_]{2,3})?)(\\.properties)(\\.([^\\.]*))?$");
		final Pattern clientPattern = Pattern.compile("client_([^_]{2,3}(_[^_]{2,3})?(_[^_]{2,3})?)(\\.(properties|txt|rtf))$");
		final Pattern menuPattern = Pattern.compile(    "menu_([^_]{2,3}(_[^_]{2,3})?(_[^_]{2,3})?)(\\.(properties|txt|rtf))$");
		
		
		File[] clientFiles=directory.listFiles(new FileFilter() {			
			@Override
			public boolean accept(File f) {
				if (f.isFile() &&
						clientPattern.matcher(f.getName()).matches())
					return true;
				return false;
			}
		});
		File[] menuFiles=directory.listFiles(new FileFilter() {			
			@Override
			public boolean accept(File f) {
				if (f.isFile() &&
						menuPattern.matcher(f.getName()).matches())
					return true;
				return false;
			}
		});
		
		
		exportResourceFile(Messages.class,"client");
		
		Map<String, LanguageProperties> fileMap=new TreeMap<String, LanguageProperties>();
		for (File file:clientFiles) {
			Matcher m = clientPattern.matcher(file.getName());
			if(m.matches()) {
				String code=m.group(1);
				String extension=m.group(5);
				if (extension==null)
					continue;
				File generatedFile=null;
				generatedFile=convertToPropertyFile(file,new File(generated,"client_"+code+".properties"),extension);
				if (generatedFile==null)
					continue;
				if (fileMap.containsKey(code)) {
					LanguageProperties p=fileMap.get(code);
					p.setClient(file);	
					p.setClientStatus(generatedFile==null?
								FileStatus.ERROR:
									FileStatus.OK);
				}else fileMap.put(code, new LanguageProperties(code, 
						file, 
						generatedFile==null?
								FileStatus.ERROR:
									FileStatus.OK,
						null, 
						FileStatus.MISSING));
			}
		}
		
		
		exportResourceFile(MenuManager.class,"menu");
		
		for (File file:menuFiles) {
			Matcher m = menuPattern.matcher(file.getName());
			if(m.matches()) {
				String code=m.group(1);
				String extension=m.group(5);
				if (extension==null)
					continue;
				File generatedFile=null;
				generatedFile=convertToPropertyFile(file,new File(generated,"menu_"+code+".properties"),extension);
				if (generatedFile==null)
					continue;
				if (fileMap.containsKey(code)) {
					LanguageProperties p=fileMap.get(code);
					p.setMenu(file);	
					p.setMenuStatus(generatedFile==null?
							FileStatus.ERROR:
								FileStatus.OK);
				}else fileMap.put(code, new LanguageProperties(code, 
						null, 
						FileStatus.MISSING, 
						file, 
						generatedFile==null?
								FileStatus.ERROR:
									FileStatus.OK));
			}
		}
		
		files.addAll(fileMap.values());
		
		updateLocales(false);
		
		externalListModel.fireTableDataChanged();
	
			
	}

	
	public void refreshExportFiles() {
		File directory=new File(directoryField.getText());
		if (!directory.isDirectory()) {
			displayMessage(Messages.getString("Message.badLocaleDirectory"));
			if (externalListModel!=null)
				externalListModel.fireTableDataChanged();
			return;
		}
		
		File export=new File(directory, "export");
		if (export.exists()) {
			if (!export.isDirectory()) {
				displayMessage(Messages.getString("Message.badLocaleDirectory"));
				if (externalListModel!=null)
					externalListModel.fireTableDataChanged();
				return;				
			}
		}else {
			if (!export.mkdir()) {
				displayMessage(Messages.getString("Message.badLocaleDirectory"));
				if (externalListModel!=null)
					externalListModel.fireTableDataChanged();
				return;								
			}
		}
		exportResourceFileUTF8(Messages.class,"client");
		exportResourceFileUTF8(MenuManager.class,"menu");
		for (String locale:slocales) {
			if ("default".equals(locale))
				continue;
			exportResourceFileUTF8(Messages.class,"client_"+locale);
			exportResourceFileUTF8(MenuManager.class,"menu_"+locale);
		}
	}

	public static String getFileContent(InputStream in,String encoding ,boolean unescape) throws IOException {
		BufferedReader br =new BufferedReader( new InputStreamReader(in, encoding ));
		StringBuilder sb = new StringBuilder();
		String line;
		while(( line = br.readLine()) != null ) {
			sb.append( unescape?unescape(line):line );
			sb.append( '\n' );
		}
		return sb.toString();
	}

	
	public static String escapeProperties(String text) {
//	    CharsetEncoder targetEncoder = Charset.forName("US-ASCII").newEncoder();
	    CharsetEncoder targetEncoder = Charset.forName("ISO-8859-1").newEncoder();
	    final StringBuilder result = new StringBuilder();
	    for (Character character : text.toCharArray()) {
	        if (targetEncoder.canEncode(character)) {
	            result.append(character);
	        } else {
	            result.append("\\u");
	            result.append(Integer.toHexString(0x10000 | character).substring(1).toUpperCase()); //hack to add leading zeros
	        }
	    }
	    return result.toString();
	 }
	
	public static String unescape(String text) {
		text = StringEscapeUtils.unescapeJava(text);
		return text;
	 }
	
	public File convertToPropertyFile(File file, File newFile, String type) {
		try {
			FileInputStream in = new FileInputStream(file);
			String text=null;
			
			if ("rtf".equals(type)) {
				try {
					RTFEditorKit kit = new RTFEditorKit();
					DefaultStyledDocument doc = new DefaultStyledDocument();
					kit.read(in, doc, 0);
					text = doc.getText(0, doc.getLength());
					text=escapeProperties(text);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			} else if ("txt".equals(type)) {
				text=getFileContent(in, "UTF-8",false);
				text=escapeProperties(text);
			} else {
				text=getFileContent(in, "ISO-8859-1", false);
			}
			if (text==null)
				return null;
			
			byte[] outputData = text.getBytes();
			
			
			FileOutputStream out=new FileOutputStream(newFile);
			out.write(outputData);
			out.close();
			
			return newFile;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	



	public JComponent createContentPanel() {
		initControls();
		FormLayout dLayout = new FormLayout("default, 3dlu, fill:80dlu:grow, 3dlu, default", "p"); 
		DefaultFormBuilder dbuilder = new DefaultFormBuilder(dLayout);
		dbuilder.setDefaultDialogBorder();
		dbuilder.append(Messages.getString("Text.ExternalLocaleDirectory"));
		dbuilder.append(directoryField);
		dbuilder.append(directorySetButton);
		JComponent directoryPanel=dbuilder.getPanel();

		FormLayout layout = new FormLayout("default, 3dlu, default, 3dlu, default, 3dlu, fill:80dlu:grow", 
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 100dlu, 20dlu, p"); 

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();
		builder.append(Messages.getString("Text.Language"));
		builder.append(languageCombo);
		builder.nextLine(2);
		builder.append(Messages.getString("Text.Country"));
		builder.append(countryCombo);
		builder.nextLine(2);
		builder.addSeparator(Messages.getString("Text.ExternalLocale")); 
		builder.nextLine(2);

		builder.add(externalCheckbox,cc.xyw(builder.getColumn(), builder
				.getRow(), 7)); 
		builder.nextLine(2);
		builder.add(directoryPanel,cc.xyw(builder.getColumn(), builder
				.getRow(), 7));
		builder.nextLine(2);
		builder.append(Messages.getString("Text.Locales"));
		builder.append(directoryUpdateButton);
		builder.append(directoryExportButton);
		builder.nextLine(2);
		builder.add(new JScrollPane(externalList),cc.xyw(builder.getColumn(), builder
				.getRow(), 7));
		builder.nextLine(2);
		builder.add(messageArea,cc.xyw(builder.getColumn(), builder
				.getRow(), 7)); 
		return builder.getPanel();
	}
}
