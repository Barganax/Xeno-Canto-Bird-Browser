package main.browser.database;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class DatabaseOpPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final DatabaseCard databaseCard;
	final JButton databaseRemoveButton;
	final JButton analyzeButton;
	public final JCheckBox resynthCheckBox;
	final JCheckBox createSonogramCheckBox;
	
	public DatabaseOpPanel(DatabaseCard dbc) {
		super();
		databaseCard = dbc;
		databaseRemoveButton = new JButton("Remove");
		databaseRemoveButton.setActionCommand("remove");
		databaseRemoveButton.addActionListener(databaseCard.databaseContentListener);
		databaseRemoveButton.setEnabled(false);
		add(databaseRemoveButton);
		analyzeButton = new JButton("Analyze!");
		analyzeButton.setActionCommand("analyze");
		analyzeButton.addActionListener(databaseCard.databaseContentListener);
		analyzeButton.setEnabled(false);
		add(analyzeButton);
		resynthCheckBox = new JCheckBox("Resynthesize");
		resynthCheckBox.setSelected(false);
		add(resynthCheckBox);
		createSonogramCheckBox = new JCheckBox("Create Sonogram Records");
		createSonogramCheckBox.setSelected(false);
		add(createSonogramCheckBox);
	}
	
	void enableButtons() {
		databaseRemoveButton.setEnabled(true);
		analyzeButton.setEnabled(true);
	}

	void disableButtons() {
		databaseRemoveButton.setEnabled(false);
		analyzeButton.setEnabled(false);
	}
}
