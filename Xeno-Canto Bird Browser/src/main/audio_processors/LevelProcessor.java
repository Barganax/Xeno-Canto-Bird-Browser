package main.audio_processors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import main.browser.RecordingInfo;

/*
 * LevelProcessor merely looks at the absolute value of the amplitudes.
 * Then it writes the amplitude maximum and mean to the database. 
 */

public class LevelProcessor implements AudioProcessor {
	private String recordingId = "";
	private double maxAmplitude = 0;
	private long samples = 0;
	private double meanAmplitude = 0;
	
	public LevelProcessor(String recording_id) {
		recordingId = recording_id;
	}
	
	@Override
	public boolean process(AudioEvent audioEvent) {
		float[] floatBuffer = audioEvent.getFloatBuffer();
		int fbSize = floatBuffer.length;
		for (int i=0; i<fbSize; i++) {
			double absValue = Math.abs(floatBuffer[i]);
			if (absValue > maxAmplitude)
				maxAmplitude = absValue;
			meanAmplitude += absValue;
		}
		samples += fbSize;
		return true;
	}

	@Override
	public void processingFinished() {
		meanAmplitude /= samples;
		RecordingInfo recordingInfo = new RecordingInfo(recordingId,
				maxAmplitude,
				meanAmplitude);
		recordingInfo.create();
	}
}
