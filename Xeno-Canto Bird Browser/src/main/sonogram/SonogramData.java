package main.sonogram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import be.tarsos.dsp.util.fft.FFT;
import main.ConnectionFactory;

public class SonogramData implements Comparable<SonogramData> {
	// Unique Id for this record
	public long sonogramDataId;
	
	// Original timestamp for the buffer used to create this SonogramData
	public final double timeStamp;
	
	// 0-3, i.e. 1, 2, 4 or 8 seconds
	public int type;
	
	// Number of frequency components from SonogramPreference.components
	public final int dataSize;
	
	// Low and High index from SonogramPreference
	public final int loIndex;
	public final int hiIndex;

	// Indices into the fft
	public int[] index;
	
	// Power and phase of the components
	public float[] pwr;
	public float[] phs;
	
	// Real and imaginary values from the fft
	public float[] real;
	public float[] imag;

	// Copy constructor
	/*
	Unneeded since last database revision

	SonogramData(SonogramData sd) {
		sonogramDataId = sd.sonogramDataId;
		timeStamp = sd.timeStamp;
		type = sd.type;
		dataSize = sd.dataSize;
		loIndex = sd.loIndex;
		hiIndex = sd.hiIndex;
		index = new int[dataSize];
		pwr = new float[dataSize];
		phs = new float[dataSize];
		real = new float[dataSize];
		imag = new float[dataSize];
		for (int i = 0; i < dataSize; i++) {
			index[i] = sd.index[i];
			pwr[i] = sd.pwr[i];
			phs[i] = sd.phs[i];
			real[i] = sd.real[i];
			imag[i] = sd.imag[i];
		}
	}
*/
	
	// Constructor used by retrieve() and Sonogram.add()
	public SonogramData(long sdid, int ds, double ts) {
		sonogramDataId = sdid;
		dataSize = ds;
		timeStamp = ts;
		loIndex = hiIndex = 0;
		index = new int[dataSize];
		pwr = new float[dataSize];
		phs = new float[dataSize];
		real = new float[dataSize];
		imag = new float[dataSize];
	}
	
	// Constructor used by SonogramProcessor.  Creates an anonymous SonogramData with zeroed data	
	public SonogramData(double ts, int c) {
		sonogramDataId = -1;
		timeStamp = ts;
		dataSize = c;
		loIndex = hiIndex = 0;
		index = new int[dataSize];
		pwr = new float[dataSize];
		phs = new float[dataSize];
		real = new float[dataSize];
		imag = new float[dataSize];
	}
	
	// Constructor used by SonogramProcessor.  Creates an anonymous SonogramData
	public SonogramData(double ts, float[] tBuffer, int c, int li, int hi, int t) {
		sonogramDataId = -1;
		timeStamp = ts;
		dataSize = c;
		loIndex = li;
		hiIndex = hi;
		type = t;
		index = new int[dataSize];
		pwr = new float[dataSize];
		phs = new float[dataSize];
		real = new float[dataSize];
		imag = new float[dataSize];
//		new PrimeComponentsFromPower(powerBuffer(tBuffer));
		new PrimeComponentsFromTransform(tBuffer);
//		fillComplexValues(tBuffer);
	}

	@Override
	public String toString() {
		return "Type: "
				+ type
				+ ", Data Size: "
				+ dataSize
				+ ", Low Index: "
				+ loIndex
				+", High Index: "
				+ hiIndex;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(149, 311). // two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
				append(timeStamp).
				toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SonogramData))
			return false;
		if (obj == this)
			return true;
		SonogramData rhs = (SonogramData) obj;
		return new EqualsBuilder().
				// if deriving: appendSuper(super.equals(obj)).
				append(timeStamp, rhs.timeStamp).
				isEquals();
	}

	@Override
	public int compareTo(SonogramData o) {
		if (equals(o))
			return 0;
		double comp2 = timeStamp - o.timeStamp;
		if (comp2 != 0)
			return comp2<0?-1:1;
		return 0;
	}

	void calculatePhase() {
		phs = new float[dataSize];
		for (int i = 0; i < dataSize; i++)
			if (real[i] == 0)
				phs[i] = (float) (Math.PI/2);
			else
				phs[i] = (float) Math.atan(imag[i]/real[i]);
	}
	
	private class PrimeComponentsFromTransform {
		private int nc = 0;
		
		public PrimeComponentsFromTransform(float[] tBuffer) {
			for (int i=loIndex; i<hiIndex; i++)
				add(i, tBuffer);
		}
		
		private void add(int ci, float[] tBuffer) {
			float rl = tBuffer[2*ci];
			float im = tBuffer[2*ci+1];
			float power = (float) Math.sqrt(rl*rl+im*im);
			for (int i=0; i<nc; i++) {
				if (pwr[i] < power) {
					index[i] ^= ci;
					ci ^= index[i];
					index[i] ^= ci;
					float temp = pwr[i];
					pwr[i] = power;
					power = temp;
					temp = real[i];
					real[i] = rl;
					rl = temp;
					temp = imag[i];
					imag[i] = im;
					im = temp;
				}
			}
			if (nc < dataSize) {
				pwr[nc] = power;
				real[nc] = rl;
				imag[nc] = im;
				index[nc++] = ci;
			}
		}
	}

	public float[] resynthesize(float[] audioBuffer, SonogramPreference sp) {
		int bSize = audioBuffer.length;
		float[] tb = new float[bSize*2];
		FFT fft = new FFT(bSize);
		for (int i = 0; i < dataSize; i++) {
			tb[2*index[i]] = real[i];
			tb[2*index[i]+1] = imag[i];
		}
		fft.backwardsTransform(tb);
		System.arraycopy(tb, 0, audioBuffer, 0, audioBuffer.length);
		return audioBuffer;
	}
	
	public void create(Sonogram sonogram, Connection conn) {
		PreparedStatement stmt = null;
		String sql = "insert into sonogram_data (time_stamp, data) values (?, ?)";
		String sql2 = "select last_insert_id()";
		ResultSet rs = null;
		
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setDouble(1, timeStamp);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
		    DataOutputStream dout = new DataOutputStream(bout);
	    	try {
	    		for (int i = 0; i < dataSize; i++) {
	    			dout.writeFloat((float) (sonogram.getSonogramPreference().baseFrequency[type]*(index[i]+1)));
			    	dout.writeFloat(real[i]);
					dout.writeFloat(imag[i]);
	    		}
	    		dout.close();
			} catch (IOException e) {
				System.out.println("SonogramData.create()");
				e.printStackTrace();
			}
	    	stmt.setBytes(2, bout.toByteArray());
	    	stmt.executeUpdate();
			stmt.close();
			stmt = conn.prepareStatement(sql2);
			rs = stmt.executeQuery();
			if (rs.first())
				sonogramDataId = rs.getLong(1);
			else
				throw new SQLException();
			createIntersect(sonogram, conn);
		}  catch (SQLException ex) {
			// handle any errors
			System.out.println("SonogramData.create()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}		
	}

	public void createIntersect(Sonogram sonogram, Connection conn) {
		PreparedStatement stmt = null;
		String sql = "insert into sonogram_data_intersect values (?, ?, ?)";
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, sonogram.getSonogramId());
			stmt.setLong(2, sonogramDataId);
			stmt.setDouble(3, timeStamp - sonogram.getStartTime());
			stmt.executeUpdate();
			stmt.close();
		}  catch (SQLException ex) {
			// handle any errors
			System.out.println("SonogramData.create()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public static Set<SonogramData> retrieve(long sonogramId) {
		Connection conn = null;
		Set<SonogramData> sonogramDataSet = null;
		try {
			conn = ConnectionFactory.getConnection();
			sonogramDataSet = retrieve(sonogramId, conn);
			conn.close();
		}  catch (SQLException ex) {
			// handle any errors
			System.out.println("SonogramData.retrieve()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return sonogramDataSet;
	}

	public static Set<SonogramData> retrieve(long sonogramId, Connection conn) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select t2.time_stamp, t1.sonogram_data_id, t1.data, t4.components "
				+ "from sonogram_data as t1, sonogram_data_intersect as t2, sonogram as t3, sonogram_preference as t4 "
				+ "where t3.sonogram_id = ? "
				+ "and t2.sonogram_id = t3.sonogram_id "
				+ "and t2.sonogram_data_id = t1.sonogram_data_id "
				+ "and t4.sonogram_preference_id = t3.sonogram_preference_id";
		Set<SonogramData> sonogramDataSet = new TreeSet<SonogramData>();
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, sonogramId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				SonogramData sonogramData = new SonogramData(rs.getLong(2),
						rs.getInt(4),
						rs.getDouble(1));
		        byte[] asBytes = rs.getBytes(3);
		        ByteArrayInputStream bin = new ByteArrayInputStream(asBytes);
		        DataInputStream din = new DataInputStream(bin);
		        for (int i = 0; i < sonogramData.dataSize; i++) {
		            try {
						sonogramData.real[i] = din.readFloat();
						sonogramData.imag[i] = din.readFloat();
					} catch (IOException e) {
						System.out.println("SonogramData.retrieve(sonogramId, conn)");
						e.printStackTrace();
					}
		        }
				sonogramDataSet.add(sonogramData);
			}
			stmt.close();
		}  catch (SQLException ex) {
			// handle any errors
			System.out.println("SonogramData.retrieve(sonogramId, conn)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return sonogramDataSet;
	}
}
