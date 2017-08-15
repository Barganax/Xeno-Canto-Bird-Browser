package main.browser.database;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.pitch.PitchProcessor;
import javazoom.jl.player.Player;
import main.audio_processors.LevelProcessor;
import main.audio_processors.SonogramProcessor;
import main.browser.Browser;
import main.browser.Browser.ConvertWorker;
import main.browser.Fetcher;
import main.browser.Recording;
import main.browser.RecordingInfo;
import main.onset.Onset;
import main.onset.OnsetPreference;
import main.sonogram.Sonogram;
import main.sonogram.SonogramFrequencyScale;
import main.sonogram.SonogramPanel;
import main.sonogram.SonogramPreference;
import main.sonogram.SonogramTimeScale;

public class DatabaseCard extends JPanel implements OnsetHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int PREFERENCE_REVIEW_WIDTH = 260;
	public static final int PREFERENCE_REVIEW_HEIGHT = 400;
	private static final double MAX_AMPLITUDE_COEFFICIENT = 10;
	public static final String RESYNTH_DIRECTORY = Browser.SOUNDCLIP_DIRECTORY+"resynth/";
	public static final String RESYNTH_STRING = "-resynth-";
	private static final int[] SONOGRAM_LENGTH = { 1000, 2000, 4000, 8000 };
	private static final int SONOGRAM_HEIGHT_FUDGE = 20;
		
	private final DatabaseBrowserPanel databaseBrowserPanel;
	private final BrowserSonogramPanel browserSonogramPanel;
	
	SonogramProcessor sonogramProcessor = null;

	// Map showing which recordings have been analyzed for particular settings
	public final SortedSet<HasSonogramsKey> hasSonogramsSet;
	
	public List<SonogramPreference> sonogramPreferenceList;
	public OnsetPreference onsetPreference;
	public SonogramPreference sonogramPreference = null;
	private AudioDispatcher audioDispatcher = null;
	public JProgressBar progressBar;
	private final Browser browser;
	private File resynthWavFile = null;

	static final int SONOGRAM_VIEWPORT_WIDTH = 640;
	final DatabaseContentListener databaseContentListener;
	final RecordingRemarksPanel recordingRemarksPanel;
	final SonogramPreferenceAndReviewPanel sonogramPreferenceAndReviewPanel;
	
	List<OnsetPreference> onsetPreferenceList;
	boolean existingOnsets;
	SonogramPanel sonogramPanel = null;
	String currentRecordingId;
	public DatabaseOpPanel databaseOpPanel;
	List<Onset> onsetList = null;
	
	public DatabaseCard(Browser b) {
		super();
		browser = b;
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		databaseContentListener = new DatabaseContentListener();
		databaseBrowserPanel = new DatabaseBrowserPanel(this);
		recordingRemarksPanel = new RecordingRemarksPanel();
		onsetPreferenceList = OnsetPreference.retrieve();
		onsetPreference = OnsetPreference.retrieve("DEFAULT");
		sonogramPreferenceList = SonogramPreference.retrieve();
		sonogramPreference = SonogramPreference.retrieve("DEFAULT");
		sonogramPanel = new SonogramPanel(this);
		hasSonogramsSet = Sonogram.hasSonograms();
		browserSonogramPanel = new BrowserSonogramPanel(this);
		databaseOpPanel = new DatabaseOpPanel(this);
		sonogramPreferenceAndReviewPanel = new SonogramPreferenceAndReviewPanel(this);
		int spWidth = SonogramFrequencyScale.IMAGE_WIDTH+SONOGRAM_VIEWPORT_WIDTH;
		int spHeight = SonogramTimeScale.IMAGE_HEIGHT+SonogramPanel.IMAGE_HEIGHT+SONOGRAM_HEIGHT_FUDGE;
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addComponent(databaseBrowserPanel)
				.addGroup(layout.createSequentialGroup()
						.addComponent(recordingRemarksPanel)
						.addComponent(browserSonogramPanel, spWidth, spWidth, spWidth)
						.addComponent(sonogramPreferenceAndReviewPanel,
								PREFERENCE_REVIEW_WIDTH, PREFERENCE_REVIEW_WIDTH, PREFERENCE_REVIEW_WIDTH))
				.addComponent(databaseOpPanel));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(databaseBrowserPanel)
				.addGroup(layout.createParallelGroup()
						.addComponent(recordingRemarksPanel)
						.addComponent(browserSonogramPanel, spHeight, spHeight, spHeight)
						.addComponent(sonogramPreferenceAndReviewPanel))
				.addComponent(databaseOpPanel));
	}

	/*
	private String resynthWavFile() {
		Recording r = databaseBrowserPanel.getCurrentRecording();
		if (r == null)
			return "";
		return RESYNTH_DIRECTORY+r.getId()+"-resynth-"+sonogramPreference.getSpId()+Browser.WAV_EXTENSION;
	}
	*/
	
	private RecordingInfo recordingInfo = null;
	/*
	 * Only in database source mode.  Analyze the currently selected recording
	 */
	private void analyzeClip() {
		double maxAmplitude = 1.75;
		Recording r = databaseBrowserPanel.getCurrentRecording();
		recordingInfo = RecordingInfo.retrieve(currentRecordingId = r.getId());
		progressBar = new JProgressBar(0, r.getLength());
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    databaseOpPanel.add(progressBar);
	    databaseOpPanel.analyzeButton.setText("Stop");
	    databaseOpPanel.analyzeButton.setActionCommand("stop");
	    revalidate();
	    if (recordingInfo != null)
	    	maxAmplitude = MAX_AMPLITUDE_COEFFICIENT*recordingInfo.getMaxAmplitude();
		String fileName = Browser.wavFileName(currentRecordingId);
		File audioFile = new File(fileName);
		if (audioFile.exists())
			analyzeFile(r.getId(), audioFile, maxAmplitude);
		else {
			ConvertWorker cw = new ConvertWorker(r, browser);
			cw.execute();
			}
	}
	
	/*
	 * convert dB gain to amplitude multiplier
	 */
	private static double dBToMultiplier(double d) { return Math.pow(10, d/10); }
	
	private void analyzeFile(String recordingId, File audioFile, double maxAmplitude) {
		this.onsetList = Onset.retrieve(recordingId, this.onsetPreference.getOpId());
		existingOnsets = this.onsetList.size() > 0;
		if (sonogramPanel != null) {
			browserSonogramPanel.timeScale.reset();
			browserSonogramPanel.frequencyScale.reset(sonogramPreference);
			sonogramPanel.reset(sonogramPreference);
			sonogramPanel.setMaxPower(maxAmplitude);
		}
		/*
		if (databaseOpPanel.resynthCheckBox.isSelected())
        	resynthWavFile = new File(resynthWavFile());
        	*/
		try {
			audioDispatcher = AudioDispatcherFactory.fromFile(audioFile,
																sonogramPreference.getBufferSize(),
																sonogramPreference.getOverlap());
		} catch (FileNotFoundException e) {
			
		}
		catch (UnsupportedAudioFileException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TarsosDSPAudioFormat tarsosDSPAudioFormat = audioDispatcher.getFormat();
		TarsosDSPAudioFormat audioFormat = null;
		System.out.println("Encoding: "+tarsosDSPAudioFormat.getEncoding()
				+ ", Sample Rate: "+tarsosDSPAudioFormat.getSampleRate()
				+ ", Sample Size (bits): "+tarsosDSPAudioFormat.getSampleSizeInBits()
				+ ", Channels: "+tarsosDSPAudioFormat.getChannels()
				+ ", Frame Size: "+tarsosDSPAudioFormat.getFrameSize()
				+ ", Frame Rate: "+tarsosDSPAudioFormat.getFrameRate()
				+ ", BigEndian: "+tarsosDSPAudioFormat.isBigEndian());
		audioFormat = new TarsosDSPAudioFormat(tarsosDSPAudioFormat.getEncoding(),
				tarsosDSPAudioFormat.getSampleRate(),
				tarsosDSPAudioFormat.getSampleSizeInBits(),
				1,
				tarsosDSPAudioFormat.getFrameSize(),
				tarsosDSPAudioFormat.getFrameRate(),
				tarsosDSPAudioFormat.isBigEndian());
		if (this.recordingInfo == null) {
			LevelProcessor levelProcessor = new LevelProcessor(this.currentRecordingId);
			this.audioDispatcher.addAudioProcessor(levelProcessor);
		}
		AudioPlayer audioPlayer = null;
//		FFTProcessor fftProcessor = null;
		sonogramProcessor = new SonogramProcessor(this);
		GainProcessor gainProcessor = null;
		PitchProcessor pitchProcessor = null;

		/*
		 * GAIN PROCESSOR
		 */
		double gain = this.onsetPreference.getGain();
		if (gain != 0) {
			gainProcessor = new GainProcessor(dBToMultiplier(gain));
			this.audioDispatcher.addAudioProcessor(gainProcessor);
		}

		/*
		 * ONSET DETECTOR
		 */
		if (!this.existingOnsets) {
			ComplexOnsetDetector complexOnsetDetector = new ComplexOnsetDetector(sonogramPreference.getBufferSize(),
																				onsetPreference.getPeakThreshold(),
																				(double)(onsetPreference.getMinInteronsetInterval())/1000,
																				onsetPreference.getSilenceThreshold());
			complexOnsetDetector.setHandler(this);
			this.audioDispatcher.addAudioProcessor(complexOnsetDetector);
		} else
			useOnsetList();
		
		/*
		 * PITCH PROCESSOR
		 
		pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN,
											SAMPLE_RATE,
											sonogramPreference.getBufferSize(),
											frequencyScale);
		audioDispatcher.addAudioProcessor(pitchProcessor);
		*/

		/*
		 * SONOGRAM PROCESSOR
		 * (replaces fftProcessor
		 */
		audioDispatcher.addAudioProcessor(sonogramProcessor);

		/*
		 * AUDIO FILE WRITER
		 
		if (resynthCheckBox.isSelected()) {
			RandomAccessFile randomAccessFile = null;
			String outFileName = resynthWavFile();
			try {
				randomAccessFile = new RandomAccessFile(outFileName, "rw");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			WriterProcessor writerProcessor = new WriterProcessor(tarsosDSPAudioFormat, randomAccessFile);
			audioDispatcher.addAudioProcessor(writerProcessor);
		}
		
		*/
		/*
		 * AUDIO PLAYER
		 */
//		audioFormat = audioDispatcher.getFormat();
		try {
			audioPlayer = new AudioPlayer(audioFormat);
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		audioDispatcher.addAudioProcessor(audioPlayer);
		
		Thread audioDispatcherThread = new Thread(this.audioDispatcher, "audio dispatching");
		audioDispatcherThread.start();
	}
	
	private void useOnsetList() {
		Iterator<Onset> onsetIterator = this.onsetList.iterator();
		while (onsetIterator.hasNext()) {
			Onset onset = onsetIterator.next();
			// Onset times for SonogramTimeScale are in seconds		currentPageLabel.setFont(settingsLabelFont);

			browserSonogramPanel.timeScale.markOnset((double)onset.getOnsetTime()/1000, onset.getOnsetSalience());
			if (databaseOpPanel.createSonogramCheckBox.isSelected())
				createSonogramsFromOnset(onset);
		}
		browserSonogramPanel.timeScale.invalidate();
		browserSonogramPanel.revalidate();
	}
	
	
	private void stopAnalyzing() {
		if (audioDispatcher == null)
			return;
		audioDispatcher.stop();
		finishAnalyzing();
	}
	
	public void finishAnalyzing() {
		databaseOpPanel.remove(progressBar);
		databaseOpPanel.analyzeButton.setText("Analyze");
		databaseOpPanel.analyzeButton.setActionCommand("analyze");
		databaseOpPanel.repaint();
		if (databaseOpPanel.createSonogramCheckBox.isSelected())
			sonogramPreferenceAndReviewPanel.reviewSonograms();
	}
	
	private class DatabaseContentListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String ac = e.getActionCommand();
			if (ac == "remove")
				removeRecording();
			else if (ac == "analyze")
				analyzeClip();
			else if (ac == "stop")
				stopAnalyzing();
			else
				System.out.println("DatabaseContentListener: Unsupported Action");
		}
	}
	
	private void removeRecording() {
		Recording r = databaseBrowserPanel.getCurrentRecording();
		File rFile = browser.wavFile(r);
		if (rFile.exists())
			rFile.delete();
		r.delete();
		databaseBrowserPanel.recordingTable.invalidate();
		revalidate();
	}

	/*
	 * (non-Javadoc)
	 * @see be.tarsos.dsp.onsets.OnsetHandler#handleOnset(double, double)
	 */
	@Override
	public void handleOnset(double time, double salience) {
//		System.out.println("Onset at "+time+", salience: "+salience);
		browserSonogramPanel.timeScale.markOnset(time, salience);
		// Onset times for Onset are in msec
		Onset onset = new Onset(this.currentRecordingId, this.onsetPreference.getOpId(), (long)(1000*time), salience);
		onsetList.add(onset);
		if (databaseOpPanel.createSonogramCheckBox.isSelected())
			createSonogramsFromOnset(onset);
	}

	private void createSonogramsFromOnset(Onset onset) {
		for (int i = 0; i < SONOGRAM_LENGTH.length; i++)
			sonogramProcessor.createSonogram(onset, SONOGRAM_LENGTH[i]);
//		this.sonogramPanel.createSonogram(onset, SONOGRAM_LENGTH[i]);			
	}

	private void playFile(String fileName) {
		File file = new File(fileName);
		PlayFileWorker playFileWorker = new PlayFileWorker(file);
		playFileWorker.execute();
	}
	
	private class PlayFileWorker extends SwingWorker<Void, Void> {
		private File file = null;
		private Player p = null;
		
		public PlayFileWorker(File f) {
			super();
			file = f;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			InputStream is = Fetcher.progressInputStream(browser,
					"Playing File "+file.getName(),
					file);
			p = new Player(is);
			p.play();
			return null;
		}
		
	}
	
	public void disableDatabaseControls() {
		databaseOpPanel.disableButtons();
		databaseOpPanel.resynthCheckBox.setEnabled(false);
		databaseOpPanel.createSonogramCheckBox.setEnabled(false);		
	}

	public void enableDatabaseControls() {
		databaseOpPanel.enableButtons();
		databaseOpPanel.resynthCheckBox.setEnabled(true);
		databaseOpPanel.createSonogramCheckBox.setEnabled(true);
	}	


	public SonogramPanel getSonogramPanel() {
		return sonogramPanel;
	}
}
