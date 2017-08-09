package main.sonogram;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.util.fft.FFT;
import be.tarsos.dsp.writer.WaveHeader;
import main.ConnectionFactory;
import main.browser.Browser;
import main.onset.Onset;

public class Sonogram implements Comparable<Sonogram> {
    // .wav file header length
    private  static final int HEADER_LENGTH=44;//byte
    private int[] multipleBufferSize;
    private int[] multipleOverlap;

    private final SonogramPreference sonogramPreference;
	private final Onset onset;
	private int type;
	private final int len;
	
	private long sonogramId;
	
	private SonogramPanel sonogramPanel = null;
	private SortedSet<SonogramData> sonogramDataSet;
//	private Deque<SonogramData> sonogramDataDeque;
	// used when creating .wav file
	private TarsosDSPAudioFormat tarsosDSPAudioFormat;
	private AudioFormat audioFormat;
	private int audioLen=0;
	public enum EnumSonogramQuality {
		CLEAN,
		OTHER_BIRD,
		NOISY,
		NOISY_OTHER_BIRD
	}
	private EnumSonogramQuality quality = null;
	
	public Sonogram(SonogramPreference sp, Onset o, int l) {
		sonogramPreference = sp;
		onset = o;
		len = l;
		type = -1;

		multipleBufferSize = sp.multipleBufferSize;
		multipleOverlap = sp.multipleOverlap;
//		this.sonogramDataDeque = new LinkedList<SonogramData>();
		sonogramDataSet = new TreeSet<SonogramData>();
	}

	public Sonogram(SonogramPreference sp, Onset o, int l, int t) {
		sonogramPreference = sp;
		onset = o;
		len = l;
		type = t;
		multipleBufferSize = sp.multipleBufferSize;
		multipleOverlap = sp.multipleOverlap;
		sonogramDataSet = new TreeSet<SonogramData>();
	}

	public String toString() {
		String recordingIdString = this.onset.getRecordingId();
		String onsetPreferenceIdString = "" + this.onset.getOnsetPreferenceId();
		String sonogramPreferenceIdString = "" + this.getSonogramPreference().getSpId();
		String onsetTimeString = "" + this.onset.getOnsetTime();
		String lengthString = "" + this.len;
		return recordingIdString + "-"
				+ onsetPreferenceIdString + "-"
				+ sonogramPreferenceIdString + "-"
				+ onsetTimeString + "-"
				+ type + "-"
				+ lengthString;
	}
	
	/* Need to change this to make a COPY of sd because it may be used in many sonograms
	 * 
	 */
	public void add(SonogramData sd) { 
		SonogramData sonogramData = new SonogramData(sd);
		sonogramDataSet.add(sonogramData);
		}

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(sonogramPreference).
            append(onset).
            append(len).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Sonogram))
            return false;
        if (obj == this)
            return true;

        Sonogram rhs = (Sonogram) obj;
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
            append(sonogramPreference, rhs.sonogramPreference).
            append(onset, rhs.onset).
            append(len, rhs.len).
            isEquals();
    }
		
	@Override
	public int compareTo(Sonogram o) {
		if (equals(o))
			return 0;
		long ospid = o.sonogramPreference.getSpId();
		if (sonogramPreference.getSpId() < ospid)
			return -1;
		if (sonogramPreference.getSpId() > ospid)
			return 1;
		int comp = onset.compareTo(o.onset);
		if (comp != 0)
			return comp;
		if (len < o.len)
			return -1;
		if (len > o.len)
			return 1;
		return 0;
	}

	public void play() {
		AudioDispatcher audioDispatcher = null;
		AudioPlayer audioPlayer = null;
		
		float[] floatArray = createFloatBuffer();
		try {
			audioDispatcher = AudioDispatcherFactory.fromFloatArray(floatArray,
					Browser.SAMPLE_RATE,
					multipleBufferSize[type],
					multipleOverlap[type]);

		} catch (UnsupportedAudioFileException e) {
			System.out.println("Sonogram.play(): Unsupported Audio File Exception");
			e.printStackTrace();
		}
		this.tarsosDSPAudioFormat = audioDispatcher.getFormat();
		if (tarsosDSPAudioFormat != null) {
			try {
				audioPlayer = new AudioPlayer(tarsosDSPAudioFormat);
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			audioDispatcher.addAudioProcessor(audioPlayer);
		}

		Thread audioDispatcherThread = new Thread(audioDispatcher, "audio dispatching");
		audioDispatcherThread.start();
	}
	
	private float[] createFloatBuffer() {
		float[] floatBuffer = new float[multipleBufferSize[type] * sonogramPreference.dataCount];
		int offset = 0;
		Iterator<SonogramData> sonogramDataIterator = this.sonogramDataSet.iterator();
		while (sonogramDataIterator.hasNext()) {
			SonogramData sonogramData = sonogramDataIterator.next();
//			System.out.println("TS: "+sonogramData.timeStamp);
			float[] resynthBuffer = new float[multipleBufferSize[type]];
			sonogramData.resynthesize(resynthBuffer, sonogramPreference);
			System.arraycopy(resynthBuffer, 0, floatBuffer, offset, resynthBuffer.length);
			offset += resynthBuffer.length;
		}
		return floatBuffer;
	}

	public static Sonogram retrieve(Onset o, SonogramPreference sp, long l) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select * from sonogram where onset_id = ? and sono_param_id = ? and length = ?";
		Sonogram s = null;
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, o.getOnsetId());
			stmt.setLong(2, sp.getSpId());
			stmt.setLong(3, l);
			rs = stmt.executeQuery();
			if (rs.first()) {
				s = new Sonogram(sp, o, rs.getInt(3), rs.getInt(4));
				Set<SonogramData> sonogramDataSet = SonogramData.retrieve(s.sonogramId, conn);
				for (Iterator<SonogramData> i = sonogramDataSet.iterator(); i.hasNext(); ) {
					SonogramData sd = i.next();
					if (sd != null)
						s.sonogramDataSet.add(sd);
				}
			    s.quality = EnumSonogramQuality.valueOf(rs.getString(5).toUpperCase());
			}
		} catch (SQLException e) {
			System.out.println("Sonogram.retrieve(Onset, SonogramPreference, long)");
			e.printStackTrace();
		}
		return s;
	}
	
	public static void deleteWithOnsetId(long onsetId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "delete from sonogram where onset_id = ?";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, onsetId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Sonogram.deleteWithOnsetId(onsetId)");
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
			e.printStackTrace();
		}
	}

	public void create() {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "insert into sonogram (sonogram_preference_id, onset_id, type, length, quality) values (?, ?, ?, ?)";
		String sql2 = "select last_insert_id()";
		ResultSet rs = null;
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, sonogramPreference.getSpId());
			stmt.setLong(2, onset.getOnsetId());
			stmt.setInt(3, type);
			stmt.setLong(4, len);
			stmt.setString(5, quality.name().toLowerCase());
			stmt.executeUpdate();
			stmt.close();
			stmt = conn.prepareStatement(sql2);
			rs = stmt.executeQuery();
			if (rs.next())
				sonogramId = rs.getLong(1);
			Iterator<SonogramData> sdi = sonogramDataSet.iterator();
			while (sdi.hasNext()) {
				SonogramData sd = sdi.next();
				sd.create(conn);
			}
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			ex.printStackTrace();
			System.out.println("Sonogram.create()");
			System.out.println(this.toString());
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public void create(Connection conn) {
		PreparedStatement stmt = null;
		String sql = "insert into sonogram (sonogram_preference_id, onset_id, type, length, quality) values (?, ?, ?, ?, ?)";
		String sql2 = "select last_insert_id()";
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, sonogramPreference.getSpId());
			stmt.setLong(2, onset.getOnsetId());
			stmt.setInt(3, type);
			stmt.setLong(4, len);
			stmt.setString(5, quality.name().toLowerCase());
			stmt.executeUpdate();
			stmt.close();
			stmt = conn.prepareStatement(sql2);
			rs = stmt.executeQuery();
			if (rs.next())
				sonogramId = rs.getLong(1);
			createSonogramData(conn);
		} catch (SQLException ex) {
			// handle any errors
			ex.printStackTrace();
			System.out.println("Sonogram.create(Connection c)");
			System.out.println(this.toString());
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public void createSonogramData(Connection conn) {
//		System.out.println("Creating "+sonogramDataSet.size()+" SonogramData");
		Iterator<SonogramData> sdi = sonogramDataSet.iterator();
		while (sdi.hasNext()) {
			SonogramData sd = sdi.next();
			sd.sonogramId = sonogramId;
			sd.create(conn);
		}
	}
	
	public void createWavFile(String wavFilePath) {
		RandomAccessFile output = null;
		System.out.println("Sonogram for '"+wavFilePath+"':");
		System.out.println(this.toString());

		try {
			output = new RandomAccessFile(wavFilePath, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createAudioFormat();
        try {
            output.write(new byte[HEADER_LENGTH]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterator<SonogramData> sonogramDataIterator = sonogramDataSet.iterator();
        while (sonogramDataIterator.hasNext()) {
        	SonogramData sd = sonogramDataIterator.next();
        	float[] audioBuffer = audioBufferFromData(sd);
        	AudioEvent audioEvent = new AudioEvent(this.tarsosDSPAudioFormat);
        	audioEvent.setFloatBuffer(audioBuffer);
            try {
                this.audioLen+=audioEvent.getByteBuffer().length;
                //write audio to the output
                output.write(audioEvent.getByteBuffer());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //write header and data to the result output
        WaveHeader waveHeader=new WaveHeader(WaveHeader.FORMAT_PCM,
                (short)audioFormat.getChannels(),
                (int)audioFormat.getSampleRate(),(short)16,audioLen);//16 is for pcm, Read WaveHeader class for more details
        ByteArrayOutputStream header=new ByteArrayOutputStream();
        try {
            waveHeader.write(header);
            output.seek(0);
            output.write(header.toByteArray());
            output.close();
        }catch (IOException e){
            e.printStackTrace();
        }
	}
	
	private void createAudioFormat() {
		this.audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				(float)Browser.SAMPLE_RATE,
				16,
				1,
				Browser.SAMPLE_RATE,
				1,
				false);
	}
	
	private float[] audioBufferFromData(SonogramData sd) {
		float[] ab = new float[multipleBufferSize[type]];
		float[] tb = new float[2 * multipleBufferSize[type]];
		int[] indices = sd.index;
		int sdSize = sd.dataSize;
		for (int i = 0; i < sdSize; i++) {
			tb[2*indices[i]] = sd.real[i];
			tb[2*indices[i]+1] = sd.imag[i];
		}
		FFT fft = new FFT(multipleBufferSize[type]);
		fft.backwardsTransform(tb);
		System.arraycopy(tb, 0, ab, 0, ab.length);
		return ab;
	}
	
	public static boolean find(String recordingId, long onsetPreferenceId, long sonogramPreferenceId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select sonogram.*, onset.* from sonogram natural left join onset "
				+"where sonogram.sonogram_preference_id = ? and onset.recording_id = ? and onset.cod_param_id = ?";
		boolean result = false;
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, sonogramPreferenceId);
			stmt.setString(2, recordingId);
			stmt.setLong(3, onsetPreferenceId);
			rs = stmt.executeQuery();
			result = rs.first();
			stmt.close();
			conn.close();
			
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Sonogram.find(onsetPreferenceId, sonogramPreferenceId)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return result;
	}

	public double getLastTimeStamp() {
		if (sonogramDataSet.isEmpty())
			return 0;
		return sonogramDataSet.last().timeStamp;
	}
	
	public SonogramPreference getSonogramPreference() {
		return sonogramPreference;
	}

	public long getSonogramId() {
		return sonogramId;
	}

	public void setSonogramId(long sonogramId) {
		this.sonogramId = sonogramId;
	}

	public Onset getOnset() {
		return onset;
	}

	public int getType() {
		return type;
	}
	
	public int getLen() {
		return len;
	}
	public SonogramPanel getSonogramPanel() {
		return sonogramPanel;
	}

	public void setSonogramPanel(SonogramPanel sonogramPanel) {
		this.sonogramPanel = sonogramPanel;
	}

	public int getNumberOfData() {
		return sonogramDataSet.size();
	}

	public EnumSonogramQuality getQuality() {
		return quality;
	}

	public void setQuality(EnumSonogramQuality quality) {
		this.quality = quality;
	}
}
