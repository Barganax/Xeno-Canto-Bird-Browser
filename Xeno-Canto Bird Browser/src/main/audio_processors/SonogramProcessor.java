package main.audio_processors;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.util.fft.BartlettHannWindow;
import be.tarsos.dsp.util.fft.BartlettWindow;
import be.tarsos.dsp.util.fft.BlackmanHarrisNuttall;
import be.tarsos.dsp.util.fft.BlackmanWindow;
import be.tarsos.dsp.util.fft.CosineWindow;
import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.util.fft.GaussWindow;
import be.tarsos.dsp.util.fft.HammingWindow;
import be.tarsos.dsp.util.fft.HannWindow;
import be.tarsos.dsp.util.fft.LanczosWindow;
import be.tarsos.dsp.util.fft.RectangularWindow;
import be.tarsos.dsp.util.fft.ScaledHammingWindow;
import be.tarsos.dsp.util.fft.TriangularWindow;
import be.tarsos.dsp.util.fft.WindowFunction;
import main.browser.Browser;
import main.browser.database.DatabaseCard;
import main.onset.Onset;
import main.sonogram.Sonogram;
import main.sonogram.SonogramData;
import main.sonogram.SonogramPreference;
import main.sonogram.SonogramPreference.EnumWindowFunction;

/*
 * SonogramProcessor replaces FFTProcessor for generating data for sonograms.  Now, SonogramProcessor generates data 
 * for types 0-3 (Audio buffer sizes 2, 4 and 8 times the audio buffer size from the audio event) concurrently.  Unfortunately,
 * to get everything to work without some fancy programming, I fixed the overlap at 50%
 */
public class SonogramProcessor implements AudioProcessor {
	// Number of buffers 
	public static final int AUDIO_BUFFER_CAPACITY = 12;
	// ttl for anonymous SonogramData
	public static final int TIME_TO_KEEP_SONOGRAM_DATA = 500;  // msec
	
	private final DatabaseCard databaseCard;
	
	// audioBuffer has space for AUDIO_BUFFER_CAPACITY buffers
	private final float[] audioBuffer;
	
	// length SonogramPreference.NUMBER_OF_SONOGRAM_TYPES arrays to hold number of regular buffers, buffer size, overlap and FFT instances for each type
	private final int[] numberOfBuffers;
	private  final int[] multipleBufferSize;
	private  final int[] multipleOverlap;
	private final FFT[] fft;
	
	// timeStamps array contains up to the last 2*AUDIO_BUFFER_CAPACITY timestamps
	private final double[] timeStamps;
		
	private final EnumWindowFunction enumWindowFunction;

	// Convenient references to SonogramPreference values
/*
	private final int bufferSize; // The regular (type 0) buffer size and overlap
	private final int overlap;
	private final int components;
	private final int loIndex;
	private final int hiIndex;
	*/
	
	private final SonogramPreference sonogramPreference;
	
	/*
	 * sonogramSet is a complete set of sonograms created for this recording with given OnsetPreference and SonogramPreference (q.v.)
	 * activeSonogramSet is the set of uncompleted sonograms, hopefully empty when processing is done.
	 */
	private final Set<Sonogram> sonogramSet;
	private final Set<Sonogram> activeSonogramSet;

	// sonogramDataSet contains all of the SonogramData instances created.
	private final Set<SonogramData> sonogramDataSet;

	// reference to the audio buffer from the AudioEvent
	private float[] thisAudioBuffer = null;
	
	// 0 <= thisAudioBufferNumber < 2*AUDIO_BUFFER_CAPACITY
	private int thisAudioBufferNumber;
	
	// To execute code specific to the first time process() is called for this instance
	private boolean firstTime = true;
	
	// Number of entries (floats) in audioBuffer
	private int audioEntryCount = 0;
	
	// The last timStamp
	private double timeStamp = 0;
	
	// The current SonogramData
	private SonogramData sonogramData;
	
	public SonogramProcessor(DatabaseCard dc) {
		databaseCard = dc;
		sonogramPreference = databaseCard.sonogramPreference;
		audioBuffer = new float[AUDIO_BUFFER_CAPACITY * sonogramPreference.getBufferSize()];
		numberOfBuffers = sonogramPreference.numberOfBuffers;
		multipleBufferSize = sonogramPreference.multipleBufferSize;
		multipleOverlap = sonogramPreference.multipleOverlap;
		timeStamps = new double[2*AUDIO_BUFFER_CAPACITY];
		fft = new FFT[SonogramPreference.NUMBER_OF_SONOGRAM_TYPES];
		enumWindowFunction = databaseCard.sonogramPreference.getEnumWindowFunction();
		sonogramSet = new TreeSet<Sonogram>();
		activeSonogramSet = new LinkedHashSet<Sonogram>();
		sonogramDataSet = new TreeSet<SonogramData>();
		for (int i = 0; i < SonogramPreference.NUMBER_OF_SONOGRAM_TYPES; i++)
			fft[i] = makeFFT(multipleBufferSize[i]);
		thisAudioBufferNumber = 0;
	}

	/*
	 * Creates a FFT of appropriate size
	 */
	private FFT makeFFT(int bufferSize) {
		FFT fft;
		if (enumWindowFunction == EnumWindowFunction.NONE)
			fft = new FFT(bufferSize);
		else {
			WindowFunction windowFunction = null;
			if (enumWindowFunction == EnumWindowFunction.HAMMING)
				windowFunction = new HammingWindow();
			else if (enumWindowFunction == EnumWindowFunction.RECTANGULAR)
				windowFunction = new RectangularWindow();
			else if (enumWindowFunction == EnumWindowFunction.TRIANGULAR)
				windowFunction = new TriangularWindow();
			else if (enumWindowFunction == EnumWindowFunction.BARTLETT)
				windowFunction = new BartlettWindow();
			else if (enumWindowFunction == EnumWindowFunction.BARTLETT_HANN)
				windowFunction = new BartlettHannWindow();
			else if (enumWindowFunction == EnumWindowFunction.BLACKMAN)
				windowFunction = new BlackmanWindow();
			else if (enumWindowFunction == EnumWindowFunction.BLACKMAN_HARRIS_NUTTALL)
				windowFunction = new BlackmanHarrisNuttall();
			else if (enumWindowFunction == EnumWindowFunction.GAUSS)
				windowFunction = new GaussWindow();
			else if (enumWindowFunction == EnumWindowFunction.COSINE)
				windowFunction = new CosineWindow();
			else if (enumWindowFunction == EnumWindowFunction.HANN)
				windowFunction = new HannWindow();
			else if (enumWindowFunction == EnumWindowFunction.LANCZOS)
				windowFunction = new LanczosWindow();
			else if (enumWindowFunction == EnumWindowFunction.SCALED_HAMMING)
				windowFunction = new ScaledHammingWindow();
			else
				System.out.println("Unsupported window function: "+enumWindowFunction.name());
			fft = new FFT(bufferSize, windowFunction);
		}
		return fft;
	}
	
	@Override
	public boolean process(AudioEvent audioEvent) {
		removeStaleSonogramData();
		// The current timestamp
		timeStamps[thisAudioBufferNumber] = timeStamp = audioEvent.getTimeStamp();;
		thisAudioBuffer = audioEvent.getFloatBuffer();
		int startingPosition;
		int entriesCopied;
		// If first time process() is called, copy the entire audio buffer
		if (firstTime) {
			startingPosition = 0;
			entriesCopied = sonogramPreference.getBufferSize();
			firstTime = false;
			// Else just copy the non-overlapped portion
		} else {
			startingPosition = sonogramPreference.overlap;
			entriesCopied = sonogramPreference.getBufferSize() - sonogramPreference.overlap;
		}
		System.arraycopy(thisAudioBuffer,
					startingPosition,
					audioBuffer,
					audioEntryCount,
					entriesCopied);
		audioEntryCount += entriesCopied;

		processBuffer(0);

		for (int i = 1; i < SonogramPreference.NUMBER_OF_SONOGRAM_TYPES; i++)
			if (audioEntryCount >= multipleBufferSize[i]
					&& thisAudioBufferNumber % numberOfBuffers[i] == 1)
				processBuffer(i);
		
		thisAudioBufferNumber++;
		
		if (audioEntryCount == audioBuffer.length)
			shiftBuffer();

		if (sonogramData.type == 0 && databaseCard.databaseOpPanel.resynthCheckBox.isSelected()) {
			sonogramData.resynthesize(thisAudioBuffer, sonogramPreference);
		}
		databaseCard.progressBar.setValue((int)timeStamp);

		return true;
	}

	private void removeStaleSonogramData() {
		Iterator<SonogramData> sdi = sonogramDataSet.iterator();
		while (sdi.hasNext())
			if (sdi.next().timeStamp < timeStamp - (double)TIME_TO_KEEP_SONOGRAM_DATA/1000)
				sdi.remove();
	}
	
	private void processBuffer(int sizeIndex) {
		float[] currentBuffer = new float[2 * multipleBufferSize[sizeIndex]];
		
//		int srcPos = bufferSize * (thisAudioBufferNumber + 1) - multipleBufferSize[sizeIndex];
		int srcPos = audioEntryCount - multipleBufferSize[sizeIndex];
		System.arraycopy(audioBuffer,
				srcPos,
				currentBuffer,
				0,
				multipleBufferSize[sizeIndex]);
		int tsIndex = thisAudioBufferNumber + 1 - numberOfBuffers[sizeIndex];
		createSonogramData(sizeIndex, timeStamps[tsIndex], currentBuffer);
		sonogramDataSet.add(sonogramData);
		updateActiveSonograms(sonogramData);
		databaseCard.getSonogramPanel().drawSonogramData(sonogramData);
	}

	private void createSonogramData(int sizeIndex, double timeStamp, float[] buffer) {
		FFT fft = this.fft[sizeIndex];
		fft.forwardTransform(buffer);
		sonogramData = new SonogramData(timeStamp,
				buffer,
				sonogramPreference.getComponents(),
				sonogramPreference.getLoIndex(),
				sonogramPreference.getHiIndex(),
				sizeIndex);
	}

	private void updateActiveSonograms(SonogramData sd) {
		Iterator<Sonogram> si = activeSonogramSet.iterator();
		while (si.hasNext()) {
			Sonogram sonogram = si.next();
			double startTime = (double)sonogram.getOnset().getOnsetTime()/1000;
			double endTime = startTime + (double)sonogram.getLen()/1000;
			if (sonogram.getType() == sd.type)
				if (sd.timeStamp >= startTime
					&& sonogram.getNumberOfData() < sonogramPreference.getDataCount()) {
					if (sd.timeStamp < endTime)
						sonogram.add(sd);
					else {
						si.remove();
						finalizeSonogram(sonogram);
					}
				}	
		}
	}
	
	private void finalizeSonogram(Sonogram sonogram) {
		int missingData = sonogram.getSonogramPreference().getDataCount() - sonogram.getNumberOfData();
		if (missingData > 0)
			addMissingData(sonogram, missingData);
	}
	
	private void addMissingData(Sonogram sonogram, int missingData) {
		double ts = sonogram.getLastTimeStamp();
		double tsInc = (double)multipleBufferSize[sonogram.getType()]/Browser.SAMPLE_RATE/2;
		for (int i = 0; i < missingData; i++) {
			ts += tsInc;
			SonogramData sd = new SonogramData(ts, sonogram.getSonogramPreference().getComponents());
			sonogram.add(sd);
		}
	}
		
	private void shiftBuffer() {
		System.arraycopy(audioBuffer,
				audioBuffer.length/2,
				audioBuffer,
				0,
				audioBuffer.length/2);
		audioEntryCount = audioBuffer.length/2;
		for (int i = 0; i < AUDIO_BUFFER_CAPACITY; i++)
			timeStamps[i] = timeStamps[AUDIO_BUFFER_CAPACITY + i];
		thisAudioBufferNumber = AUDIO_BUFFER_CAPACITY;
	}

	public void createSonogram(Onset o, int l) {
		int type;
		if (l == 1000)
			type = 0;
		else if (l == 2000)
			type = 1;
		else if (l == 4000)
			type = 2;
		else
			type = 3;
		/*
		// This is here until the multiple-buffer sonograms work correctly
		if (type > 0)
			return;
		*/
		Sonogram sonogram = new Sonogram(databaseCard.sonogramPreference, o, l, type);
		getSonogramSet().add(sonogram);
		activeSonogramSet.add(sonogram);
		if (firstTime)
			return;
		double startTime = (double)sonogram.getOnset().getOnsetTime()/1000;
		if (timeStamp > startTime)
			updateSonogram(sonogram, startTime);
	}
	
	/*
	 * This is just for getting the sonogram up to speed after its creation.
	 */
	private void updateSonogram(Sonogram sonogram, double startTime) {
		Iterator<SonogramData> sdi = sonogramDataSet.iterator();
		while (sdi.hasNext()) {
			SonogramData sd = sdi.next();
			if (sd.timeStamp > startTime)
				sonogram.add(sd);
		}
	}
	
	@Override
	public void processingFinished() {
		Iterator<Sonogram> si = activeSonogramSet.iterator();
		while (si.hasNext()) {
			Sonogram s = si.next();
			finalizeSonogram(s);
		}
		databaseCard.finishAnalyzing();
	}

	public void initialize() { firstTime = true; }

	public Set<Sonogram> getSonogramSet() {
		return sonogramSet;
	}
}
