package main.onset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import main.ConnectionFactory;
import main.browser.Browser;

public class OnsetPreference {
	private static final double DEFAULT_GAIN = 0;
	private static final double DEFAULT_SILENCE_THRESHOLD = -70;
	private static final double DEFAULT_PEAK_THRESHOLD = .3;
	private static final int DEFAULT_MIN_INTERONSET_INTERVAL = 4;
	
	private long opId;
	private String tag;
	private double gain;
	private double silenceThreshold;
	private double peakThreshold;
	private int minInteronsetInterval;	// msec
	
	public OnsetPreference() {
		super();
		setGain(DEFAULT_GAIN);
		setSilenceThreshold(DEFAULT_SILENCE_THRESHOLD);
		setPeakThreshold(DEFAULT_PEAK_THRESHOLD);
		setMinInteronsetInterval(DEFAULT_MIN_INTERONSET_INTERVAL);
	}
	
	public void create() {
		Connection conn = null;
		String sql = "insert into cod_param (tag, gain, silence_threshold, peak_threshold, min_interonset_interval) values (?, ?, ?, ?, ?)";
		String sql2 = "select last_insert_id()";
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, tag);
			stmt.setDouble(2, gain);
			stmt.setDouble(3, silenceThreshold);
			stmt.setDouble(4, peakThreshold);
			stmt.setInt(5, minInteronsetInterval);
			stmt.executeUpdate();
			stmt = conn.prepareStatement(sql2);
			rs = stmt.executeQuery();
			if (rs.first()) setOpId(rs.getInt(1));
			stmt.close();
			rs.close();
			conn.close();
		} catch (SQLException ex) {
		    // handle any errors
			System.out.println("OnsetPreference.create()");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public static OnsetPreference retrieve (long l) {
		Connection conn = null;
		String sql = "select * from cod_param where cod_param_id = ?";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		OnsetPreference osp = new OnsetPreference();
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, l);
			rs = stmt.executeQuery();
			if (rs.first()) {
				osp.opId = rs.getInt(1);
				osp.tag = rs.getString(2);
				osp.gain = rs.getDouble(3);
				osp.silenceThreshold = rs.getDouble(4);
				osp.peakThreshold = rs.getDouble(5);
				osp.minInteronsetInterval = rs.getInt(6);
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Onset.retrieve(onsetPreferenceId)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return osp;
	}
	
	public static OnsetPreference retrieve (String tag) {
		Connection conn = null;
		String sql = "select * from cod_param where tag = ?";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		OnsetPreference osp = new OnsetPreference();
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, tag);
			rs = stmt.executeQuery();
			if (rs.first()) {
				osp.opId = rs.getInt(1);
				osp.tag = rs.getString(2);
				osp.gain = rs.getDouble(3);
				osp.silenceThreshold = rs.getDouble(4);
				osp.peakThreshold = rs.getDouble(5);
				osp.minInteronsetInterval = rs.getInt(6);
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Onset.retrieve(tag)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return osp;
	}
	
	public static List<OnsetPreference> retrieve() {
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;
	    List<OnsetPreference> results = new ArrayList<OnsetPreference>();
	    String sql = "select * from cod_param";
	    try {
	    	conn = ConnectionFactory.getConnection();
	    	stmt = conn.prepareStatement(sql);
	    	rs = stmt.executeQuery();
	    	while (rs.next()) {
	    		OnsetPreference op = new OnsetPreference();
	    		op.setOpId(rs.getLong(1));
	    		op.setTag(rs.getString(2));
	    		op.setGain(rs.getDouble(3));
	    		op.setSilenceThreshold(rs.getDouble(4));
	    		op.setPeakThreshold(rs.getDouble(5));
	    		op.setMinInteronsetInterval(rs.getInt(6));
	    		results.add(op);
	    	}
	    	rs.close();
	    	stmt.close();
	    	conn.close();
	    } catch (SQLException ex) {
			// handle any errors
			System.out.println("Onset.retrieve()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	    return results;
	}
	
	public static void remove (long key) {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "delete from cod_param where cod_param_id = ?";
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, key);
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Onset.remove(onsetPreferenceId)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}	
	}
	
	public long getOpId() {
		return opId;
	}
	public void setOpId(long opId) {
		this.opId = opId;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public double getGain() {
		return gain;
	}
	public void setGain(double gain) {
		this.gain = gain;
	}
	public double getSilenceThreshold() {
		return silenceThreshold;
	}
	public void setSilenceThreshold(double silenceThreshold) {
		this.silenceThreshold = silenceThreshold;
	}
	public double getPeakThreshold() {
		return peakThreshold;
	}
	public void setPeakThreshold(double peakThreshold) {
		this.peakThreshold = peakThreshold;
	}
	public int getMinInteronsetInterval() {
		return minInteronsetInterval;
	}
	public void setMinInteronsetInterval(int minInteronsetInterval) {
		this.minInteronsetInterval = minInteronsetInterval;
	}	
}
