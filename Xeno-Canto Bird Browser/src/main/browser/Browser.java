package main.browser;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import main.browser.database.DatabaseCard;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class Browser extends JPanel implements ActionListener, ListSelectionListener {
	private static final long serialVersionUID = 1L;

	public static final String SOUNDCLIP_DIRECTORY = "/home/brennus/xcdb/clips/";
	public static final String WAV_EXTENSION = ".wav";
	public static final int MAX_DATABASE_CONNECTIONS = 10;

	// Audio constants
	public static final int SAMPLE_RATE = 44100;
	

	// Xeno-Canto Table column constants
	private static final int XCID_COL = 0;
	private static final int SPECIES_COL = 1;
	private static final int LENGTH_COL = 2;
	private static final int FORMAT_COL = 3;
	private static final int DATE_COL = 4;
	private static final int COUNTRY_COL = 5;
	private static final int TYPE_COL = 6;
	private static final int SOLITARY_COL = 7;
	private static final int DB_COL = 8;
	private static final int[] COL_WIDTH = { 90, 400, 60, 50, 90, 130, 330, 70, 70 };

	private static enum EnumDataSource { XENO_CANTO, XENO_CANTO_AUTO, DATABASE }
	
	// Other
	static final int LAST_XENO_CANTO_PAGE = 11633;
	List<Recording> recordingList = null;
	int currentPage;
	EnumDataSource dataSource;
	Set<Species> speciesSet = null;
	
	// UI Components
	private JButton prevButton, nextButton;
	private JLabel currentPageLabel;
	public static final JLabel onsetSettingsLabel = new JLabel("ONSET SETTINGS");
	public static final JLabel sonogramSettingsLabel = new JLabel("SONOGRAM SETTINGS");
	public static final JLabel reviewSonogramsLabel = new JLabel("REVIEW SONOGRAMS");
	private static final String XENO_CANTO_CARD = "Xeno Canto Card",
			XENO_CANTO_AUTOMATED_CARD = "Xeno Canto Automated Card",
			DATABASE_CARD = "Database Card",
			ONSET_PREFERENCE_CARD = "Onset Preference Card",
			SONOGRAM_PREFERENCE_CARD = "Sonogram Preference Card";

	final XenoCantoAutoCard xenoCantoAutoCard;
	
	public final DatabaseCard databaseCard;
	private final OnsetPreferenceCard onsetPreferenceCard;
	private final SonogramPreferenceCard sonogramPreferenceCard;

	private Parser parser;
	private JTextField pageNumberTextField;
	private JPanel navBar;
	private JPanel gotoPagePanel;
	private JPanel infoPane;
	
	private JTable recordingTable;
	
	private JTextArea remarksLabel;
	private JButton arButton;
	private JButton playButton;
	private JScrollPane tableScrollPane;

	public Browser() {
		super(new BorderLayout());
		Fetcher.setParent(this);
		currentPage = 1;
		initializeLabels();
		speciesSet = Species.retrieve();
		setLayout(new CardLayout());
		add(xenoCantoCard(), XENO_CANTO_CARD);
		xenoCantoAutoCard = new XenoCantoAutoCard(this);
		add(xenoCantoAutoCard, XENO_CANTO_AUTOMATED_CARD);
		databaseCard = new DatabaseCard(this);
		add(databaseCard, DATABASE_CARD);
		onsetPreferenceCard = new OnsetPreferenceCard(this);
		add(onsetPreferenceCard, ONSET_PREFERENCE_CARD);
		sonogramPreferenceCard = new SonogramPreferenceCard(this);
		add(sonogramPreferenceCard, SONOGRAM_PREFERENCE_CARD);
		this.dataSource = EnumDataSource.DATABASE;
//		setDataSource(EnumDataSource.XENO_CANTO);
		showContent();
		Thread ffThread = new Thread(new FindFamiliesWorker());
		ffThread.start();
	}

	private void initializeLabels() {
		Color settingsLabelColor = new Color(96, 0, 0);
		onsetSettingsLabel.setForeground(settingsLabelColor);
		sonogramSettingsLabel.setForeground(settingsLabelColor);
		reviewSonogramsLabel.setForeground(settingsLabelColor);
		Font settingsLabelFont = new Font("Helvetica", Font.BOLD, 14);
		onsetSettingsLabel.setFont(settingsLabelFont);
		sonogramSettingsLabel.setFont(settingsLabelFont);
		reviewSonogramsLabel.setFont(settingsLabelFont);
		onsetSettingsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sonogramSettingsLabel.setHorizontalAlignment(SwingConstants.CENTER);	
	}
	
	private JComponent xenoCantoCard() {
		JPanel xenoCantoPanel = new JPanel();
		xenoCantoPanel.setLayout(new BorderLayout());
		xenoCantoPanel.add(navBar = (JPanel)navigationBar(), BorderLayout.NORTH);
		recordingTable = new JTable(new BrowserTableModel());
        recordingTable.setPreferredScrollableViewportSize(new Dimension(1500, 700));
        recordingTable.setFillsViewportHeight(true);
        recordingTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i=0; i<recordingTable.getModel().getColumnCount(); i++)
        	recordingTable.getColumnModel().getColumn(i).setPreferredWidth(COL_WIDTH[i]);
        ListSelectionModel lsm = recordingTable.getSelectionModel();
        lsm.addListSelectionListener(this);
        xenoCantoPanel.add(tableScrollPane = new JScrollPane(recordingTable), BorderLayout.CENTER);
        infoPane = (JPanel)ioBar();
 //       infoPane.setPreferredSize(new Dimension(1500, 60));
        xenoCantoPanel.add(infoPane, BorderLayout.SOUTH);
        return xenoCantoPanel;
	}
		
	void showContent() {
		if (dataSource == EnumDataSource.XENO_CANTO)
			xcSource();
		else if (dataSource == EnumDataSource.XENO_CANTO_AUTO)
			xcAutoSource();
		else if (dataSource == EnumDataSource.DATABASE)
			databaseSource();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String ac = e.getActionCommand();
		if (ac == "previous") previousPage();
		else if (ac == "next") nextPage();
		else if (ac == "play") playRecordingFromURL();
		else if (ac == "add") addRec(recordingTable.getSelectedRows());
		else if (ac == "remove") removeRec(recordingTable.getSelectedRows());
		else if (ac == "goto") gotoPage();
		else if (ac == "xc source") { xcSource(); }
		else if (ac == "xc auto source") { xcAutoSource(); }
		else if (ac == "database source") { databaseSource(); }
		else if (ac == "COD settings") onsetPreference();
		else if (ac == "sonogram settings") sonogramPreference();
		else if (ac == "exit")
			System.exit(0);
		else
			System.out.println("Unsupported action: "+e.getActionCommand());
	}

	private void xcSource() {
		if (recordingList == null)
			loadXenoCantoContent();
        CardLayout cl = (CardLayout)getLayout();
        cl.show(this, XENO_CANTO_CARD);
		revalidate();
	}
	
	private void xcAutoSource() {
		CardLayout cl = (CardLayout)getLayout();
		cl.show(this, XENO_CANTO_AUTOMATED_CARD);
		revalidate();
	}

	private void loadXenoCantoContent() {
		String html = "";
		LoadPageWorker loadPageWorker = new LoadPageWorker(this, currentPage);
		loadPageWorker.execute();
		try {
			html = loadPageWorker.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Create the recording list
		parser = new Parser(speciesSet);
		recordingList = parser.parsePage(html);
		currentPageLabel.setText("Current Page: "+currentPage);
		recordingTable.clearSelection();
		remarksLabel.setText("");
		recordingTable.repaint();
	}

	private void databaseSource() {
		CardLayout cl = (CardLayout)getLayout();
		cl.show(this, DATABASE_CARD);
		revalidate();
	}

	private void onsetPreference() {
		CardLayout cl = (CardLayout)getLayout();
		cl.show(this, ONSET_PREFERENCE_CARD);
		revalidate();
	}
	
	private void sonogramPreference() {
		CardLayout cl = (CardLayout)getLayout();
		cl.show(this, SONOGRAM_PREFERENCE_CARD);
		revalidate();
	}
	
	// Go to previous xc page
	private void previousPage() {
		if (currentPage > 1) currentPage--;
		else return;
		recordingList = null;
		xcSource();
	}
	
	// Go to next xc page
	private void nextPage() {
		if (currentPage < LAST_XENO_CANTO_PAGE) currentPage++;
		else return;
		recordingList = null;
		xcSource();
	}

	private void gotoPage() {
		String vs = pageNumberTextField.getText();
		int i;
		try {
			i = Integer.parseInt(vs);
		} catch (NumberFormatException e) {	
			pageNumberTextField.setText("");
			return;
		}
		if (i >=1 && i <= LAST_XENO_CANTO_PAGE) {
			currentPage = i;
			recordingList = null;
			xcSource();
		}
	}
	
	private boolean removeRecordingDialog(boolean plural) {
		String recordString = "Remove record?";
		if (plural)
			recordString = "records?";
		int dialogResult = JOptionPane.showConfirmDialog (null,
				recordString,
				recordString,
				JOptionPane.YES_NO_OPTION);
		return dialogResult == JOptionPane.YES_OPTION;
	}
	
	private void removeRec(int[] selectedRows) {
		for (int i = 0; i < selectedRows.length; i++)
			removeRec(selectedRows[i]);
	}	
	
	// Remove recording[i] from the database
	private void removeRec (int i) {
		recordingList.get(i).delete();	
		recordingList.get(i).setInDatabase(false);
		BrowserTableModel btm = (BrowserTableModel)(recordingTable.getModel());
		btm.fireTableCellUpdated(i, DB_COL);

		arButton.setText("Add");
    	arButton.setActionCommand("add");	
        revalidate();
	}
		
	// Add recording[i..n] to the database
	private void addRec(int[] selectedRows) {	
		final ExecutorService pool = Executors.newCachedThreadPool();
		for (int i = 0; i < selectedRows.length; i++) {
			recordingList.get(selectedRows[i]).create();
			pool.execute(new ConvertWorker(recordingList.get(selectedRows[i]), this));
			recordingList.get(selectedRows[i]).setInDatabase(true);
			BrowserTableModel btm = (BrowserTableModel)(recordingTable.getModel());
			btm.fireTableCellUpdated(selectedRows[i], DB_COL);
		    revalidate();
		}
	}
		
	public static class ConvertWorker extends SwingWorker<Void, Void> {
		final Recording rec;
		private final Browser browser;
		private final XenoCantoAutoCard xenoCantoAutoCard;
		
		public ConvertWorker(Recording r, Browser b) {
			rec = r;
			browser = b;
			xenoCantoAutoCard = null;
		}
		
		public ConvertWorker(Recording r, XenoCantoAutoCard xcac) {
			rec = r;
			browser = null;
			xenoCantoAutoCard = xcac;
		}

		@Override
		protected Void doInBackground() throws Exception {
			convertClip(rec);
			return null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((rec == null) ? 0 : rec.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConvertWorker other = (ConvertWorker) obj;
			if (rec == null) {
				if (other.rec != null)
					return false;
			} else if (!rec.equals(other.rec))
				return false;
			return true;
		}

		private void convertClip(Recording r) {
			Converter c = new Converter();
			InputStream is = null;
			if (browser != null)
				is = Fetcher.progressInputStream(browser, "Converting "+r.getId()+WAV_EXTENSION, r.getUrl());
			else {
				URL mp3URL = null;
				try {
					mp3URL = new URL(r.getUrl());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					is = mp3URL.openStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				c.convert(is, wavFileName(r.getId()), null, null);
			} catch (JavaLayerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void done() {
			if (browser == null)
				xenoCantoAutoCard.updateConvertWorkerSet(null, this, false);
			super.done();
		}
		
	}
	
	private static final Pattern XC_SUBDIR_PATTERN
		= Pattern.compile("XC(\\d{2}).*");
	public static String wavFileName(String xcid) {
		Matcher m = XC_SUBDIR_PATTERN.matcher(xcid);
		if (m.matches())
			return SOUNDCLIP_DIRECTORY+m.group(1)+"/"+xcid+WAV_EXTENSION;
		return null;
	}

	public File wavFile(Recording r) { return new File(wavFileName(r.getId())); }


	private static final Pattern FAMILY_PATTERN
		= Pattern.compile(".*<td>Family:<\\/td><td>(?:<span[^>]+>)?<a[^>]+>(.*?)<\\/a>.*");
	private class FindFamiliesWorker extends SwingWorker<Void, Void> {
		@Override
		protected Void doInBackground() throws Exception {
			while (true) {
				List<Species> speciesList = Species.retrieveNoFamily();
				Iterator<Species> sli = speciesList.iterator();
				while (sli.hasNext()) {
					Species species = sli.next();
					String family = findFamily(species);
					if (family != "") { 
						species.updateFamily(family.toUpperCase());
						System.out.println("Set family to "+family.toUpperCase()+" for "+species.getGenus()+" "+species.getSpecies());
					}
				}
				Thread.sleep(600000);
			}
		}
		
		private String findFamily(Species species) {
			String family = "";
			String urlString = "https://en.wikipedia.org/wiki/";
			urlString += species.getGenus().substring(0, 1)
					+species.getGenus().substring(1).toLowerCase()
					+"_"
					+species.getSpecies().toLowerCase();
			String wikiPage = Fetcher.fetch(urlString);
			Matcher m = FAMILY_PATTERN.matcher(wikiPage);
			if (m.matches())
				family = m.group(1);
			return family;
		}

		@Override
		protected void done() {
			System.out.println("No more NULL families found");
			super.done();
		}
	}

	/*
	private static String getHtml() {
		String content = "";
		try {
			content = new Scanner(new File("/home/brennus/Downloads/xeno-canto.html")).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
	
	*/
	
	 /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Xeno-Canto Bird Browser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        JComponent newContentPane = new Browser();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setJMenuBar(((Browser) newContentPane).menuBar());
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
	public JMenuBar menuBar() {
		JMenuBar mb = null;
		JMenu menu = null;
		JMenu submenu = null;
		JMenuItem mi = null;
		JRadioButtonMenuItem rbMenuItem = null;
		
		mb = new JMenuBar();
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_A);
	    menu.getAccessibleContext().setAccessibleDescription("File Operations");
        submenu = new JMenu("Data Source");
        submenu.setMnemonic(KeyEvent.VK_S);
        ButtonGroup group = new ButtonGroup();
        
        rbMenuItem = new JRadioButtonMenuItem("Xeno-Canto");
        rbMenuItem.setSelected(dataSource == EnumDataSource.XENO_CANTO);
        rbMenuItem.setMnemonic(KeyEvent.VK_X);
        group.add(rbMenuItem);
        rbMenuItem.setActionCommand("xc source");
        rbMenuItem.addActionListener(this);
        submenu.add(rbMenuItem);
 
        rbMenuItem = new JRadioButtonMenuItem("Xeno-Canto Automated");
        rbMenuItem.setSelected(dataSource == EnumDataSource.XENO_CANTO_AUTO);
        rbMenuItem.setMnemonic(KeyEvent.VK_A);
        group.add(rbMenuItem);
        rbMenuItem.setActionCommand("xc auto source");
        rbMenuItem.addActionListener(this);
        submenu.add(rbMenuItem);
 
        rbMenuItem = new JRadioButtonMenuItem("Local Database");
        rbMenuItem.setSelected(dataSource == EnumDataSource.DATABASE);
        rbMenuItem.setMnemonic(KeyEvent.VK_L);
        group.add(rbMenuItem);
        rbMenuItem.setActionCommand("database source");
        rbMenuItem.addActionListener(this);
        submenu.add(rbMenuItem);
        menu.add(submenu);
        mi = new JMenuItem("Exit", KeyEvent.VK_X);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        mi.getAccessibleContext().setAccessibleDescription("Exit the program");
        mi.setActionCommand("exit");
        mi.addActionListener(this);
        menu.add(mi);
        mb.add(menu);

	    menu = new JMenu("Settings");
	    menu.getAccessibleContext().setAccessibleDescription("Settings");
	    mi = new JMenuItem("Onsets", KeyEvent.VK_O);
	    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
	    mi.getAccessibleContext().setAccessibleDescription("Parameters for Complex Onset Detector");
	    mi.setActionCommand("COD settings");
	    mi.addActionListener(this);
	    menu.add(mi);
	    
	    mi = new JMenuItem("Sonogram", KeyEvent.VK_G);
	    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
	    mi.getAccessibleContext().setAccessibleDescription("Sonogram");
	    mi.setActionCommand("sonogram settings");
	    mi.addActionListener(this);
	    menu.add(mi);
	    mb.add(menu);
	    
		return mb;
	}

	public static void main(String[] args) {
		// Register the database driver
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }
	    //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}

	JComponent navigationBar() {
		JPanel nb = new JPanel(new BorderLayout());
		currentPageLabel = new JLabel("Current Page: "+currentPage);
		currentPageLabel.setFont(new Font("Helvetica", Font.BOLD, 16));
		currentPageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		nb.add(currentPageLabel, BorderLayout.NORTH);
		
		prevButton = new JButton("Previous Page");
		prevButton.setActionCommand("previous");
		prevButton.setMnemonic(KeyEvent.VK_PAGE_UP);
		prevButton.addActionListener(this);
		nb.add(prevButton, BorderLayout.WEST);
		
		nextButton = new JButton("Next Page");
		nextButton.setActionCommand("next");
		nextButton.setMnemonic(KeyEvent.VK_PAGE_DOWN);
		nextButton.addActionListener(this);
		nb.add(nextButton, BorderLayout.EAST);
		
		gotoPagePanel = (JPanel)gotoControl();
		nb.add(gotoPagePanel, BorderLayout.SOUTH);
				
		return nb;
	}

	JComponent gotoControl() {
		JPanel gtc = new JPanel();
		JLabel gotoLabel = new JLabel("Goto Page:");
		gtc.add(gotoLabel);
		pageNumberTextField = new JTextField();
		pageNumberTextField.setActionCommand("goto");
		pageNumberTextField.addActionListener(this);
		pageNumberTextField.setPreferredSize(new Dimension(50, 24));
		gtc.add(pageNumberTextField);
		return gtc;
	}
	
	private JButton allThisSpeciesButton;
	JComponent ioBar() {
		JPanel iob = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        remarksLabel = new JTextArea("");
        remarksLabel.setPreferredSize(new Dimension(700, 60));
        remarksLabel.setBounds(0, 0, 600, 100);
        remarksLabel.setLineWrap(true);
        iob.add(remarksLabel, c);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        playButton = new JButton("Play");
        playButton.setActionCommand("play");
        playButton.addActionListener(this);
        iob.add(playButton, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 0;
        arButton = new JButton("add/remove");
        arButton.addActionListener(this);
        iob.add(arButton, c);
        c = new GridBagConstraints();
        c.gridx = 4;
        c.gridy = 0;
        allThisSpeciesButton = new JButton("All This Species");
        allThisSpeciesButton.setActionCommand("all recordings");
        allThisSpeciesButton.addActionListener(this);
        iob.add(allThisSpeciesButton);
        return iob;
	}
	
	
	private void playRecordingFromURL() {
		PlayURLWorker playURLWorker = new PlayURLWorker(recordingTable.getSelectedRow());
		playURLWorker.execute();
	}
	
	private class PlayURLWorker extends SwingWorker<Void, Void> {
		private int which;
		private Player p = null;
		
		public PlayURLWorker(int w) {
			super();
			which = w;
		}
		@Override
		protected Void doInBackground() throws Exception {
			Recording rec = recordingList.get(which);
			InputStream is = Fetcher.progressInputStream(Browser.this,
					"Playing "+rec.getId()+"."+rec.getFormat(),
					rec.getUrl());
			p = new Player(is);
			p.play();
			return null;
		}
		
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages.
        if (e.getValueIsAdjusting()) return;
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if (lsm.isSelectionEmpty()) {
            arButton.setEnabled(false);
            playButton.setEnabled(false);
            allThisSpeciesButton.setEnabled(false);
            remarksLabel.setText("");
        } else {
            int selectedRow = lsm.getMinSelectionIndex();
            allThisSpeciesButton.setEnabled(true);
            if (recordingList.get(selectedRow).isInDatabase()) {
            	arButton.setText("Remove");
            	arButton.setActionCommand("remove");
            } else {
            	arButton.setText("Add");
//            	arButton.setActionCommand("+"+selectedRow);
            	arButton.setActionCommand("add");
            }
            arButton.setEnabled(true);
            playButton.setEnabled(true);
            remarksLabel.setText(recordingList.get(selectedRow).getRemarks());
        }
        revalidate();
	}
	
	private class BrowserTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnName = {
				"ID",
				"Species",
				"Length",
				"Format",
				"Date",
				"Country",
				"Type",
				"Solitary",
				"Database"
		};
		
		
		@Override
		public String getColumnName(int i) { return columnName[i]; }
		@Override
		public int getRowCount() {
			if (recordingList == null)
				return 0;
			return recordingList.size();
		}
		@Override
		public int getColumnCount() { return columnName.length; }
		@Override
		public Class<?> getColumnClass(int c) { return getValueAt(0, c).getClass(); }
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) { return columnIndex == SOLITARY_COL; }

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Recording r = recordingList.get(rowIndex);
			if (r == null) return null;
			if (columnIndex == XCID_COL) return r.getId();
			else if (columnIndex == SPECIES_COL) return r.getSpecies().getGenus()+" "
				+r.getSpecies().getSpecies()+" "
				+r.getSubspecies()
				+"("+r.getSpecies().getCommonName()+")";
			else if (columnIndex == LENGTH_COL) return new Integer(r.getLength());
			else if (columnIndex == FORMAT_COL) return r.getFormat();
			else if (columnIndex == DATE_COL) return r.getDate();
			else if (columnIndex == COUNTRY_COL) return r.getCountry();
			else if (columnIndex == TYPE_COL) return r.getType();
			else if (columnIndex == SOLITARY_COL) return new Boolean(r.isSolitary());
			else if (columnIndex == DB_COL) return new Boolean(r.isInDatabase());
			return null;
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			super.setValueAt(aValue, rowIndex, columnIndex);
			Recording r = recordingList.get(rowIndex);
			if (columnIndex == SOLITARY_COL
					&& r.isSolitary() != (Boolean)aValue) {
				r.setSolitary((Boolean)aValue);
				if (r.isInDatabase()) r.update();
			}
		}
	}

	public EnumDataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(EnumDataSource dataSource) {
		this.dataSource = dataSource;
	}
	public JPanel getInfoPane() {
		return infoPane;
	}
	public void setInfoPane(JPanel infoPane) {
		this.infoPane = infoPane;
	}
}
