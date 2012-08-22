package com.projity.pm.graphic.views;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JComponent;

import com.projity.dialog.AbstractDialog;
import com.projity.dialog.ButtonPanel;
import com.projity.pm.graphic.frames.GraphicManager;
import com.projity.pm.task.Portfolio;
import com.projity.strings.Messages;

public class ProjectsDialog extends AbstractDialog{
	private static ProjectsDialog instance = null;
	private GraphicManager graphicManager =null;
	private ProjectView projectView;
	public static void show(GraphicManager graphicManager) {
		if (instance == null) {
			instance = new ProjectsDialog(graphicManager);
			instance.pack();
			instance.setModal(false);
    	}
		instance.setLocationRelativeTo(graphicManager.getFrame());
		instance.setVisible(true);
	}
	private ProjectsDialog(GraphicManager graphicManager) {
		super(graphicManager.getFrame(), Messages.getString("File.projects"), false);
		this.graphicManager = graphicManager;
	}
	@Override
	public JComponent createContentPanel() {
		Portfolio portfolio = graphicManager.getProjectFactory().getPortfolio();
		projectView = new ProjectView(portfolio.getNodeModel(), portfolio);
		instance.setPreferredSize(new Dimension(800,250));
		return projectView;
	}
	public ButtonPanel createButtonPanel() {
		return null;
	}

}
