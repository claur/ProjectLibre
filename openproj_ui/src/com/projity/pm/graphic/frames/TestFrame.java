package com.projity.pm.graphic.frames;
import static org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority.MEDIUM;
import static org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority.TOP;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingUtilities;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;

import com.projity.menu.MenuManager;

/**
 * Main Frame to demonstrate ribbon use.
 * 
 * @author <a href="http://blog.frankel.ch/">Nicolas Frankel</a>
 * @date 26 juin 2010
 * @version 1.0
 * 
 */
public class TestFrame extends JRibbonFrame {

    /** Serial version unique id. */
    private static final long serialVersionUID = 1L;

    public static ResizableIcon getResizableIconFromResource(String resource) {

	return ImageWrapperResizableIcon.getIcon(TestFrame.class
		.getClassLoader().getResource(resource), new Dimension(48, 48));
    }

    /**
     * Entry point method.
     * 
     * @param args
     *            Application arguments
     */
    public static void main(String[] args) {

	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {

		TestFrame frame = new TestFrame();

		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		GraphicManager manager=GraphicManager.getInstance();
		
		manager.setRibbon(frame,MenuManager.getInstance(null));

	    }
	});
    }
}
