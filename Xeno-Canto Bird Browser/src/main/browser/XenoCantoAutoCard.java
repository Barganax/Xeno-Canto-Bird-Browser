package main.browser;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import main.browser.Browser.ConvertWorker;

public class XenoCantoAutoCard extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private static final double SLEEP_SECONDS_PER_RECORDING = 1.5;
	private static final Pattern COUNTRY_PATTERN
	= Pattern.compile(" *United States|U\\.S\\.|Canada *");
	private static final Pattern ADDITIONAL_PAGE_PATTERN
	= Pattern.compile(".*?<li><a href=\"(\\?pg=\\d)\">Next.*", Pattern.DOTALL);
	
	private final Browser browser;
	private final GroupLayout layout;
	private final ExecutorService pool;
	private final XCACActionListener actionListener;
	private final XCACControlPanel controlPanel;
	private final LogPanel logPanel;
	private final WorkerListPanel workerListPanel;
	private final JLabel currentPageLabel;
	private final List<SpeciesConvertWorkerSet> convertWorkerSetList;
	
	private Set<Species> allSpeciesSet;
	private Parser parser;
	
	public XenoCantoAuto xenoCantoAuto;
	
	private int startPage = 1;
	private int currentPage = startPage;
	private volatile boolean running = false;
	
	public XenoCantoAutoCard(Browser b) {
		super();
		browser = b;
		setLayout(layout = new GroupLayout(this));
		pool = Executors.newCachedThreadPool();
		convertWorkerSetList = new LinkedList<SpeciesConvertWorkerSet>();
		actionListener = new XCACActionListener();
		currentPageLabel = new JLabel("Current Page: ");
		currentPageLabel.setFont(new Font("Helvetica", Font.BOLD, 14));
		controlPanel = new XCACControlPanel();
		logPanel = new LogPanel(this);
		workerListPanel = new WorkerListPanel();
		
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(currentPageLabel, 200, 200, 200)
				.addComponent(controlPanel, 800, 800, 800)
				.addGroup(layout.createSequentialGroup()
						.addComponent(logPanel, 850, 850, 850)
						.addComponent(workerListPanel)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(currentPageLabel, 30, 30, 30)
				.addComponent(controlPanel, 30, 30, 30)
				.addGroup(layout.createParallelGroup()
						.addComponent(logPanel, 650, 650, 650)
						.addComponent(workerListPanel)));
	}
	
	void addConvertWorkerSet(SpeciesConvertWorkerSet scws) {
		updateConvertWorkerSet(scws, null, true);
		workerListPanel.add(new ConvertWorkerSetPanel(scws));
		workerListPanel.revalidate();
	}
	
	void removeConvertWorkerSet(SpeciesConvertWorkerSet scws) {
		workerListPanel.remove(scws.convertWorkerSetPanel);
		workerListPanel.revalidate();
	}
	
	public synchronized void updateConvertWorkerSet(SpeciesConvertWorkerSet scws, ConvertWorker convertWorker, boolean add) {
		if (scws == null) {
			Species cwSpecies = convertWorker.rec.getSpecies();
			Iterator<SpeciesConvertWorkerSet> i = convertWorkerSetList.iterator();
			while (i.hasNext()) {
				SpeciesConvertWorkerSet speciesConvertWorkerSet = i.next();
				if (speciesConvertWorkerSet.species.equals(cwSpecies)) {
					if (add)
						speciesConvertWorkerSet.add(convertWorker);
					else
						if (speciesConvertWorkerSet.remove(convertWorker)) {
							removeConvertWorkerSet(speciesConvertWorkerSet);
							i.remove();
						}
				}
			}
		} else
			convertWorkerSetList.add(scws);
	}
	
	class WorkerListPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		WorkerListPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}		
	}
	
	class SpeciesConvertWorkerSet {
		final Species species;
		final Set<ConvertWorker> convertWorkerSet;
		final JProgressBar progressBar;
		
		ConvertWorkerSetPanel convertWorkerSetPanel;
		
		SpeciesConvertWorkerSet(Species s, int n) {
			species = s;
			convertWorkerSet = new LinkedHashSet<ConvertWorker>();
			progressBar = new JProgressBar(0, n);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
		}
		
		void add(ConvertWorker convertWorker) { convertWorkerSet.add(convertWorker); }
		boolean remove(ConvertWorker convertWorker) {
			convertWorkerSet.remove(convertWorker);
			progressBar.setValue(progressBar.getValue()+1);
			convertWorkerSetPanel.setSpeciesLabel();
			convertWorkerSetPanel.repaint();
			if (progressBar.getValue() == progressBar.getMaximum())
				return true;
			return false;
			}
	}
	
	class ConvertWorkerSetPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		final SpeciesConvertWorkerSet speciesConvertWorkerSet;
		final JLabel speciesLabel;
		
		ConvertWorkerSetPanel(SpeciesConvertWorkerSet scws) {
			super();
			speciesConvertWorkerSet = scws;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			speciesLabel = new JLabel();
			setSpeciesLabel();
			add(speciesLabel);
			add(speciesConvertWorkerSet.progressBar);
			speciesConvertWorkerSet.convertWorkerSetPanel = this;
		}
		
		void setSpeciesLabel() {
			speciesLabel.setText(speciesConvertWorkerSet.species.toString()
					+" ("+speciesConvertWorkerSet.progressBar.getValue()+"/"
					+speciesConvertWorkerSet.progressBar.getMaximum()
					+" recordings)");
		}
	}
	
	private class XCACControlPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final GroupLayout layout;
		private final JLabel pageNumberLabel;
		
		final JTextField pageNumberTextField;
		final JButton startButton;

		XCACControlPanel() {
			super();
			setLayout(layout = new GroupLayout(this));
			pageNumberLabel = new JLabel("Starting Page:");
			pageNumberTextField = new JTextField();
			pageNumberTextField.setActionCommand("page number");
			pageNumberTextField.addActionListener(actionListener);
			startButton = new JButton("Start");
			startButton.setActionCommand("start");
			startButton.addActionListener(actionListener);
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(pageNumberLabel, 150, 150, 150)
					.addComponent(pageNumberTextField, 70, 80, 90)
					.addComponent(startButton));
			layout.setVerticalGroup(layout.createParallelGroup()
					.addComponent(pageNumberLabel, 25, 30, 30)
					.addComponent(pageNumberTextField, 24, 24, 24)
					.addComponent(startButton));
		}
	}
	
	private class XCACActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();
			if (actionCommand == "page number") {
				try {
					startPage = Integer.parseInt(controlPanel.pageNumberTextField.getText());
				} catch (NumberFormatException e1) {
					controlPanel.pageNumberTextField.setText("");
				}
				logPanel.addLogEntry("Set starting page to "+startPage+".");
			}
			else if (actionCommand == "start")
				start();
			else if (actionCommand == "stop")
				stop();
			else
				System.out.println("Unsupported Action: "+actionCommand);
		}
	}
	
	private void start() {
		logPanel.addLogEntry("Starting at page "+startPage);
		allSpeciesSet = browser.speciesSet;
		currentPage = startPage;
		controlPanel.startButton.setText("Stop");
		controlPanel.startButton.setActionCommand("stop");
		controlPanel.startButton.repaint();
		running = true;
		pool.execute(xenoCantoAuto = new XenoCantoAuto(this));
	}

	class XenoCantoAuto extends SwingWorker<Void, Void> {
		private final XenoCantoAutoCard xenoCantoAutoCard;
		
		private List<Recording> pageRecordingList;
		private List<Recording> speciesRecordingList;

		XenoCantoAuto(XenoCantoAutoCard xcac) {
			super();
			xenoCantoAutoCard = xcac;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			String html;
			while (running && currentPage <= Browser.LAST_XENO_CANTO_PAGE) {		
				currentPageLabel.setText("Current Page: "+currentPage);
				currentPageLabel.repaint();
				logPanel.addLogEntry("Fetching page "+currentPage);
				html = Fetcher.fetch(currentPage);
				parser = new Parser(allSpeciesSet);
				pageRecordingList = parser.parsePage(html);
				examineList();
				currentPage++;
			}

			return null;
		}
		
		private void examineList() {
			Iterator<Recording> recordingListIterator = pageRecordingList.iterator();
			while (recordingListIterator.hasNext()) {
				Recording recording = recordingListIterator.next();
				Matcher m = COUNTRY_PATTERN.matcher(recording.getCountry());
				if (m.matches()) {
					Species species = recording.getSpecies();
					if (allSpeciesSet.add(species)) {
						logPanel.addLogEntry("Adding species "+species.toString());
						loadSpecies(species.getGenus(), species.getSpecies());
						addConvertWorkerSet(new SpeciesConvertWorkerSet(species, speciesRecordingList.size()));
						importRecordingList();
						/*
						// Allow average of 3 sec per recording to import
						long sleepTime = (long)(1000*SLEEP_SECONDS_PER_RECORDING*speciesRecordingList.size());
						logPanel.addLogEntry("Sleeping for "+sleepTime/1000+" seconds: ZZZZZZZ");
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							logPanel.addLogEntry("INTERRUPTED");
							e.printStackTrace();
						}
						logPanel.addLogEntry("Thread woke up");
						*/
					}
				}
			}
		}

		private void loadSpecies(String genus, String species) {
			String speciesString = genus.substring(0, 1).toUpperCase()
				+ genus.substring(1).toLowerCase()
				+ "-"
				+ species.toLowerCase();
			String urlString = "http://xeno-canto.org/species/"+speciesString;
			String workingURLString = urlString;
			speciesRecordingList = new LinkedList<Recording>();
			boolean additionalPage;
			do {
				logPanel.addLogEntry("Fetching " + workingURLString);
				String html = Fetcher.fetch(workingURLString);
				parser = new Parser(allSpeciesSet);
				List<Recording> recordingList = parser.parsePage(html);
				for (Iterator<Recording> i = recordingList.iterator(); i.hasNext(); )
					speciesRecordingList.add(i.next());
				Matcher m = ADDITIONAL_PAGE_PATTERN.matcher(html);
				additionalPage = m.matches();
				if (additionalPage)
					workingURLString = urlString+m.group(1);
				else
					logPanel.addLogEntry("END OF SPECIES RECORDINGS");
			} while (additionalPage);
		}

		// Add all recordings in rList to the database
		void importRecordingList() {
			int howMany = speciesRecordingList.size();
			for (int i = 0; i < howMany; i++) {
				Recording recording = speciesRecordingList.get(i);
				logPanel.addLogEntry("Importing "+recording.getId());
				recording.create();
				Browser.ConvertWorker convertWorker = new Browser.ConvertWorker(recording, xenoCantoAutoCard);
				updateConvertWorkerSet(null, convertWorker, true);
				pool.execute(convertWorker);
				recording.setInDatabase(true);
			}
		}
	}

	private void stop() {
		logPanel.addLogEntry("Stopping.  Current Page: "+currentPage);
		running = false;
		controlPanel.startButton.setText("Start");
		controlPanel.startButton.setActionCommand("start");
		startPage = currentPage+1;
		controlPanel.pageNumberTextField.setText(""+startPage);
		repaint();
		}
}
