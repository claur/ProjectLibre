/*

 ============================================================================
 The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
 this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
 include  the following  acknowledgment:  "This product includes  software
 developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 Alternately, this  acknowledgment may  appear in the software itself,  if
 and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
 used to  endorse or promote  products derived from  this software without
 prior written permission. For written permission, please contact
 apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
 "Apache" appear  in their name,  without prior written permission  of the
 Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

 */

package org.apache.batik.util.gui.resource;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JToolTip;
import javax.swing.ToolTipManager;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

import com.projity.menu.HyperLinkToolTip;
import com.projity.pm.graphic.IconManager;
import com.projity.util.ClassLoaderUtils;

/**
 * This class represents a button factory which builds buttons from the content
 * of a resource bundle. <br>
 * 
 * The resource entries format is (for a button named 'Button'): <br>
 * 
 * <pre>
 * 
 *    Button.text      = text
 *    Button.icon      = icon_name 
 *    Button.mnemonic  = mnemonic 
 *    Button.action    = action_name
 *    Button.selected  = true | false
 *    Button.tooltip   = tool tip text
 *  where
 *    text, icon_name and action_name are strings
 *    mnemonic is a character
 *  
 * </pre>
 * 
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion </a>
 * @version $Id: ButtonFactory.java,v 1.2 2007/09/19 03:17:43 nekosej Exp $
 */
public class ButtonFactory extends ResourceManager {
	// Constants
	//
	private final static String ICON_SUFFIX = ".icon";
	private final static String TITLE_SUFFIX = ".title";

	private final static String TEXT_SUFFIX = ".text";

	private final static String MNEMONIC_SUFFIX = ".mnemonic";

	private final static String ACTION_SUFFIX = ".action";

	private final static String SELECTED_SUFFIX = ".selected";

	public final static String TOOLTIP_SUFFIX = ".tooltip";
	public final static String HELP_SUFFIX = ".help";
	public final static String DEMO_SUFFIX = ".demo";
	public final static String DOC_SUFFIX = ".doc";

	
	/** The table which contains the actions */
	private ActionMap actions;
	protected ClassLoader classLoader;  //LC MODIF

	/**
	 * Creates a new button factory
	 * 
	 * @param rb
	 *            the resource bundle that contains the buttons description.
	 * @param am
	 *            the actions to bind to the button
	 */
	public ButtonFactory(ActionMap am, ResourceBundle...rb) {
		super(rb);
		actions = am;
		classLoader=ClassLoaderUtils.getLocalClassLoader(); //LC MODIF
	}
	/**
	 * Creates and returns a new swing button
	 * 
	 * @param name
	 *            the name of the button in the resource bundle
	 * @throws MissingResourceException
	 *             if key is not the name of a button. It is not thrown if the
	 *             mnemonic and the action keys are missing
	 * @throws ResourceFormatException
	 *             if the mnemonic is not a single character
	 * @throws MissingListenerException
	 *             if the button action is not found in the action map
	 */
	public JButton createJButton(String name) throws MissingResourceException,
			ResourceFormatException, MissingListenerException {
		JButton result;
		try {
			result = new JButton(getString(name + TEXT_SUFFIX));
		} catch (MissingResourceException e) {
			result = new JButton();
		}
		initializeButton(result, name);
		return result;
	}

	/**
	 * Creates and returns a new swing button initialised to be used as a
	 * toolbar button
	 * 
	 * @param name
	 *            the name of the button in the resource bundle
	 * @throws MissingResourceException
	 *             if key is not the name of a button. It is not thrown if the
	 *             mnemonic and the action keys are missing
	 * @throws ResourceFormatException
	 *             if the mnemonic is not a single character
	 * @throws MissingListenerException
	 *             if the button action is not found in the action map
	 */
	public AbstractButton createJToolbarButton(final String name)
			throws MissingResourceException, ResourceFormatException,
			MissingListenerException {
	//	ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
//		result = new JToolbarButton(getString(name + TEXT_SUFFIX)) Don't want this - don't know why it is here
		
		JButton result = new JToolbarButton() {
			public Point getToolTipLocation(MouseEvent event) { // the tip MUST be touching the button if html because you can click on links
				if (getToolTipText().startsWith("<html>"))
					return new Point(0, getHeight()-2);
				else
					return super.getToolTipLocation(event);
			}

			public JToolTip createToolTip() {
				if (getToolTipText().startsWith("<html>")) {
					JToolTip tip = new HyperLinkToolTip();
					tip.setComponent(this);
					return tip;
				} else {
					return super.createToolTip();
				}
			}};
			initializeButton(result, name);
			return result;
	}
	
	/**
	 * Creates and returns a new swing button initialised to be used as a
	 * ribbon button
	 * 
	 * @param name
	 *            the name of the button in the resource bundle
	 * @throws MissingResourceException
	 *             if key is not the name of a button. It is not thrown if the
	 *             mnemonic and the action keys are missing
	 * @throws ResourceFormatException
	 *             if the mnemonic is not a single character
	 * @throws MissingListenerException
	 *             if the button action is not found in the action map
	 */
	public AbstractCommandButton createRibbonButton(final String name)
			throws MissingResourceException, ResourceFormatException,
			MissingListenerException {
		// Icon
		ResizableIcon icon=null;
		try {
			String s = getString(name + ICON_SUFFIX);
			icon =  IconManager.getRibbonIcon(s);
		} catch (MissingResourceException e) {
		}

		// Title
		String title=null;
		try {
			title = getString(name + TEXT_SUFFIX);
		} catch (MissingResourceException e) {
		}
		
		JCommandButton b=null;
		if (icon!=null&&title!=null)
			b = new JCommandButton(title,icon);
		else if (icon!=null)
			b = new JCommandButton(icon);
		else if (title!=null)
			b = new JCommandButton(title);
			
			
			try {
				Action a = actions.getAction(getString(name + ACTION_SUFFIX));
				if (a == null) {
					throw new MissingListenerException("", "Action", name
							+ ACTION_SUFFIX);
				}
				b.addActionListener(a);
				b.setText(getString(name + TEXT_SUFFIX));
				if (a instanceof JComponentModifier) {
					((JComponentModifier) a).addJComponent(b);
				}
			} catch (MissingResourceException e) {
			}


			// Mnemonic
//			try {
//				String str = getString(name + MNEMONIC_SUFFIX);
//				if (str.length() == 1) {
//					b.setMnemonic(str.charAt(0));
//				} else {
//					throw new ResourceFormatException("Malformed mnemonic", bundle
//							.getClass().getName(), name + MNEMONIC_SUFFIX);
//				}
//			} catch (MissingResourceException e) {
//			}

			// ToolTip
			try {
				String s = getStringOrNull(name + TOOLTIP_SUFFIX);
				if (s != null) {
					String help = getStringOrNull(name+HELP_SUFFIX);
					String demo = getStringOrNull(name+DEMO_SUFFIX);
					String doc = getStringOrNull(name+DOC_SUFFIX);
					
					if (doc != null)
						s = HyperLinkToolTip.helpTipText(s,help,demo, doc);
					b.setActionRichTooltip(new RichTooltip(" ", s));
				}
			} catch (MissingResourceException e) {
			}

			
			
			
			
			return b;
	}


	/**
	 * Creates and returns a new swing radio button
	 * 
	 * @param name
	 *            the name of the button in the resource bundle
	 * @throws MissingResourceException
	 *             if key is not the name of a button. It is not thrown if the
	 *             mnemonic and the action keys are missing.
	 * @throws ResourceFormatException
	 *             if the mnemonic is not a single character.
	 * @throws MissingListenerException
	 *             if the button action is not found in the action map.
	 */
	public JRadioButton createJRadioButton(String name)
			throws MissingResourceException, ResourceFormatException,
			MissingListenerException {
		JRadioButton result = new JRadioButton(getString(name + TEXT_SUFFIX));
		initializeButton(result, name);

		// is the button selected?
		try {
			result.setSelected(getBoolean(name + SELECTED_SUFFIX));
		} catch (MissingResourceException e) {
		}

		return result;
	}

	/**
	 * Creates and returns a new swing check box
	 * 
	 * @param name
	 *            the name of the button in the resource bundle
	 * @throws MissingResourceException
	 *             if key is not the name of a button. It is not thrown if the
	 *             mnemonic and the action keys are missing
	 * @throws ResourceFormatException
	 *             if the mnemonic is not a single character.
	 * @throws MissingListenerException
	 *             if the button action is not found in the action map.
	 */
	public JCheckBox createJCheckBox(String name)
			throws MissingResourceException, ResourceFormatException,
			MissingListenerException {
		JCheckBox result = new JCheckBox(getString(name + TEXT_SUFFIX));
		initializeButton(result, name);

		// is the button selected?
		try {
			result.setSelected(getBoolean(name + SELECTED_SUFFIX));
		} catch (MissingResourceException e) {
		}

		return result;
	}

	/**
	 * Initializes a button
	 * 
	 * @param b
	 *            the button to initialize
	 * @param name
	 *            the button's name
	 * @throws ResourceFormatException
	 *             if the mnemonic is not a single character.
	 * @throws MissingListenerException
	 *             if the button action is not found in the action map.
	 */
	protected void initializeButton(AbstractButton b, String name)
			throws ResourceFormatException, MissingListenerException {
		// Action
		try {
			Action a = actions.getAction(getString(name + ACTION_SUFFIX));
			if (a == null) {
				throw new MissingListenerException("", "Action", name
						+ ACTION_SUFFIX);
			}
			b.setAction(a);
			b.setText(getString(name + TEXT_SUFFIX));
			if (a instanceof JComponentModifier) {
				((JComponentModifier) a).addJComponent(b);
			}
		} catch (MissingResourceException e) {
		}

		// Icon
		try {
			String s = getString(name + ICON_SUFFIX);
			URL url = classLoader.getResource(s); //LC MODIF
			//URL url = actions.getClass().getResource(s);
			if (url != null) {
				b.setIcon(new ImageIcon(url));
			}
		} catch (MissingResourceException e) {
		}

		// Mnemonic
		try {
			String str = getString(name + MNEMONIC_SUFFIX);
			if (str.length() == 1) {
				b.setMnemonic(str.charAt(0));
			} else {
				throw new ResourceFormatException("Malformed mnemonic", bundleNames, name + MNEMONIC_SUFFIX);
			}
		} catch (MissingResourceException e) {
		}

		// ToolTip
		try {
			String s = getStringOrNull(name + TOOLTIP_SUFFIX);
			if (s != null) {
				String help = getStringOrNull(name+HELP_SUFFIX);
				String demo = getStringOrNull(name+DEMO_SUFFIX);
				String doc = getStringOrNull(name+DOC_SUFFIX);
				
				if (doc != null)
					s = HyperLinkToolTip.helpTipText(s,help,demo, doc);
				b.setToolTipText(s);
			}
		} catch (MissingResourceException e) {
		}
	}
	
}