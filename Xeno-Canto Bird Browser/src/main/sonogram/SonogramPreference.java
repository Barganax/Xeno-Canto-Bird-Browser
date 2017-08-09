package main.sonogram;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import main.ConnectionFactory;
import main.browser.Browser;

public class SonogramPreference {
	// Number of types
	public static final int NUMBER_OF_SONOGRAM_TYPES = 4;

	private static final int DEFAULT_BUFFER_SIZE = 2048;
	private static final EnumWindowFunction DEFAULT_WINDOW_FUNCTION = EnumWindowFunction.NONE;
	private static final int DEFAULT_LO_CUTOFF = 200;
	private static final int DEFAULT_HI_CUTOFF = 18000;
	private static final int DEFAULT_COMPONENTS = 100;
	
	private long spId;
	private String tag;
	
	// Overlap is fixed at 50%
	private int bufferSize;
	public int overlap;
	private EnumWindowFunction windowFunction;
	private int loCutoff;
	private int hiCutoff;
	private int components;
	
	public final int[] numberOfBuffers,
		multipleBufferSize,
		multipleOverlap;
	public final double[] baseFrequency;
	
	// Number of SonogramData per sonogram
	public int dataCount;

	
	public enum EnumWindowFunction {
		NONE,
		RECTANGULAR,
		TRIANGULAR,
		BARTLETT,
		BARTLETT_HANN,
		BLACKMAN,
		BLACKMAN_HARRIS_NUTTALL,
		COSINE, /* PARZEN, WELCH,*/
		HAMMING,
		GAUSS,
		HANN,
		LANCZOS,
		SCALED_HAMMING,
	}
	
	public SonogramPreference() {
		super();
		numberOfBuffers = new int[NUMBER_OF_SONOGRAM_TYPES];
		multipleBufferSize = new int[NUMBER_OF_SONOGRAM_TYPES];
		multipleOverlap = new int[NUMBER_OF_SONOGRAM_TYPES];
		baseFrequency = new double[NUMBER_OF_SONOGRAM_TYPES];
		setBufferSize(DEFAULT_BUFFER_SIZE);
		dataCount = 2*(int)((float)Browser.SAMPLE_RATE/bufferSize);
		overlap = bufferSize / 2;
		setEnumWindowFunction(DEFAULT_WINDOW_FUNCTION);
		setLoCutoff(DEFAULT_LO_CUTOFF);
		setHiCutoff(DEFAULT_HI_CUTOFF);
		setComponents(DEFAULT_COMPONENTS);
	}
	
	public void create() {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "insert into sonogram_preference (tag, buffer_size, window_function, lo_cutoff, hi_cutoff, components) "
				+ "values (?, ?, ?, ?, ?, ?)";
		String sql2 = "select last_insert_id()";
		ResultSet rs = null;
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, tag);
			stmt.setInt(2, bufferSize);
			stmt.setString(3, windowFunction.name().toLowerCase());
			stmt.setInt(4, loCutoff);
			stmt.setInt(5, hiCutoff);
			stmt.setInt(6, components);
			stmt.executeUpdate();
			stmt.close();
			stmt = conn.prepareStatement(sql2);
			rs = stmt.executeQuery();
			if (rs.first())
				setSpId(rs.getLong(1));
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SonogramPreference.create()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public static SonogramPreference retrieve(long key) {
		SonogramPreference sp = null;
		Connection conn;
		PreparedStatement stmt;
		ResultSet rs;
		String sql = "select * from sonogram_preference where sonogram_preference_id = ?";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, key);
			rs = stmt.executeQuery();
			if (rs.first()) {
				sp = new SonogramPreference();
				sp.setSpId(rs.getLong(1));
				sp.setTag(rs.getString(2));
				sp.setBufferSize(rs.getInt(3));
				sp.setEnumWindowFunction(EnumWindowFunction.valueOf(rs.getString(4).toUpperCase()));
				sp.setLoCutoff(rs.getInt(5));
				sp.setHiCutoff(rs.getInt(6));
				sp.setComponents(rs.getInt(7));
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SonogramPreference.retrieve(sonogramPreferenceId)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return sp;
	}

	public static SonogramPreference retrieve(String tag) {
		SonogramPreference sp = null;
		Connection conn;
		PreparedStatement stmt;
		ResultSet rs;
		String sql = "select * from sonogram_preference where tag = ?";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, tag);
			rs = stmt.executeQuery();
			if (rs.first()) {
				sp = new SonogramPreference();
				sp.setSpId(rs.getLong(1));
				sp.setTag(rs.getString(2));
				sp.setBufferSize(rs.getInt(3));
				sp.setEnumWindowFunction(EnumWindowFunction.valueOf(rs.getString(4).toUpperCase()));
				sp.setLoCutoff(rs.getInt(5));
				sp.setHiCutoff(rs.getInt(6));
				sp.setComponents(rs.getInt(7));
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SonogramPreference.retrieve(tag)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return sp;
	}
	
	public static List<SonogramPreference> retrieve() {
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;
	    List<SonogramPreference> results = new ArrayList<SonogramPreference>();
	    String sql = "select * from sonogram_preference";
	    try {
	    	conn = ConnectionFactory.getConnection();
	    	stmt = conn.prepareStatement(sql);
	    	rs = stmt.executeQuery();
	    	while (rs.next()) {
	    		SonogramPreference sop = new SonogramPreference();
	    		sop.setSpId(rs.getLong(1));
	    		sop.setTag(rs.getString(2));
	    		sop.setBufferSize(rs.getInt(3));
	    		sop.setWindowFunction(EnumWindowFunction.valueOf(rs.getString(4).toUpperCase()));
	    		sop.setLoCutoff(rs.getInt(5));
	    		sop.setHiCutoff(rs.getInt(6));
	    		sop.setComponents(rs.getInt(7));
	    		results.add(sop);
	    	}
	    	rs.close();
	    	stmt.close();
	    	conn.close();
	    } catch (SQLException ex) {
			// handle any errors
			System.out.println("SonogramPreference.retrieve()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	    return results;
		
	}

	public void remove() { remove(spId); }

	public static void remove (long key) {
//		System.out.println("SonogramPreference.remove(): key = "+key);
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "delete from sonogram_preference where sonogram_preference_id = ?";
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, key);
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SonogramPreference.remove(sonogramPreferenceId)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public double getBaseFrequency() { return Browser.SAMPLE_RATE/this.bufferSize; }
	
	public int getLoIndex() {
		int ret = (int)((double)this.loCutoff/this.getBaseFrequency());
		return ret + (ret & 1);
	}
	
	public int getHiIndex() {
		int ret = (int)((double)this.hiCutoff/this.getBaseFrequency());
		return ret - (ret & 1);
	}
	
	public long getSpId() {
		return spId;
	}
	public void setSpId(long spId) {
		this.spId = spId;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public int getBufferSize() {
		return bufferSize;
	}
	
	public void setBufferSize(int bufferSize) {
		// Hard-coding overlap to 50% of buffer size
		this.bufferSize = bufferSize;
		overlap = bufferSize / 2;
		dataCount = 2*(int)((float)Browser.SAMPLE_RATE/bufferSize);
		int m = 1;
		for (int i = 0; i < NUMBER_OF_SONOGRAM_TYPES; i++) {
			numberOfBuffers[i] = m;
			multipleBufferSize[i] = m*bufferSize;
			baseFrequency[i] = (double)Browser.SAMPLE_RATE/multipleBufferSize[i];
			multipleOverlap[i] = multipleBufferSize[i]/2;
			m *= 2;
		}
	}
	
	public EnumWindowFunction getEnumWindowFunction() {
		return windowFunction;
	}

	public void setEnumWindowFunction(EnumWindowFunction windowFunction) {
		this.windowFunction = windowFunction;
	}

	public int getLoCutoff() {
		return loCutoff;
	}
	public void setLoCutoff(int loCutoff) {
		this.loCutoff = loCutoff;
	}

	public int getHiCutoff() {
		return hiCutoff;
	}

	public void setHiCutoff(int hiCutoff) {
		this.hiCutoff = hiCutoff;
	}

	public int getComponents() {
		return components;
	}
	public void setComponents(int components) {
		this.components = components;
	}

	public int getDataCount() {
		return dataCount;
	}

	public void setDataCount(int dataCount) {
		this.dataCount = dataCount;
	}

	public EnumWindowFunction getWindowFunction() {
		return windowFunction;
	}

	public void setWindowFunction(EnumWindowFunction windowFunction) {
		this.windowFunction = windowFunction;
	}
	public int getOverlap() { return overlap; }
}
