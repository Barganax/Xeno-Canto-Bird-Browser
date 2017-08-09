package main.onset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import main.ConnectionFactory;
import main.sonogram.Sonogram;

public class Onset implements Comparable<Onset> {
	long onsetId = -1;
	final String recordingId;
	final long onsetPreferenceId;
	// onset time in msec
	final long onsetTime;
	final double onsetSalience;

	private Onset() {
		recordingId = "";
		onsetPreferenceId = 0;
		onsetTime = 0;
		onsetSalience = 0;
	}
	
	public Onset(String recId, long opId, long ot, double os) {
		recordingId = recId;
		onsetPreferenceId = opId;
		onsetTime = ot;
		onsetSalience = os;
	}
	
	public Onset(String recId, long opId, long oid, long ot, double os) {
		recordingId = recId;
		onsetPreferenceId = opId;
		onsetId = oid;
		onsetTime = ot;
		onsetSalience = os;		
	}
	
    @Override
    public int hashCode() {
        return new HashCodeBuilder(53, 11). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(recordingId).
            append(onsetPreferenceId).
            append(onsetTime).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Onset))
            return false;
        if (obj == this)
            return true;

        Onset rhs = (Onset) obj;
        return new EqualsBuilder().
            // if deriving: appendSuper(super.equals(obj)).
            append(recordingId, rhs.recordingId).
            append(onsetPreferenceId, rhs.onsetPreferenceId).
            append(onsetTime, rhs.onsetTime).
            isEquals();
    }


	@Override
	public int compareTo(Onset o) {
		if (equals(o))
			return 0;
		int comp = recordingId.compareTo(o.recordingId);
		if (comp != 0)
			return comp;
		if (onsetPreferenceId < o.onsetPreferenceId)
			return -1;
		if (onsetPreferenceId > o.onsetPreferenceId)
			return 1;
		if (onsetTime < o.onsetTime)
			return -1;
		if (onsetTime > o.onsetTime)
			return 1;
		return 0;
	}

	/*
	 * Return list of onsets with a recording_id
	 */
	public static List<Onset> retrieve(String recordingId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select * from onset where recording_id = ?";
		List<Onset> onsetList = new ArrayList<Onset>();
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, recordingId);
			rs = stmt.executeQuery();
			for (rs.first(); rs.next(); ) {
				Onset onset = new Onset(recordingId,
						rs.getLong(3),
						rs.getLong(4),
						rs.getDouble(5));
				onset.onsetId = rs.getLong(1);
				onsetList.add(onset);
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Onset.retrieve(recordingId)");
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
		return onsetList;
	}

	/*
	 * Delete all onsets with given recording id
	 * @param recordingId
	 */
	public static void deleteWithRecordingId(String recordingId) {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "select onset_id from onset where recording_id = ?";
		ResultSet rs = null;
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, recordingId);
			rs = stmt.executeQuery();
			Onset o = new Onset();
			while (rs.next()) {
				o.onsetId = rs.getLong(1);
				o.delete();
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("Onset.deleteWithRecordingId(recordingId): "+recordingId);
			e.printStackTrace();
		}
	}
	
	/*
	 * Delete onset.
	 */
	public void delete() {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "delete from onset where onset_id = ?";
		Sonogram.deleteWithOnsetId(onsetId);
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, this.onsetId);
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Onset.delete()");
			System.out.println("SQLException: " + e.getMessage());
			System.out.println("SQLState: " + e.getSQLState());
			System.out.println("VendorError: " + e.getErrorCode());
		}
	}
	
	public void create() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "insert into onset (recording_id, cod_param_id, onset_time, onset_salience) values (?, ?, ?, ?)";
		String sql2 = "select last_insert_id()";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, recordingId);
			stmt.setLong(2, onsetPreferenceId);
			stmt.setLong(3, onsetTime);
			stmt.setDouble(4, onsetSalience);
			stmt.executeUpdate();
			stmt.close();
			stmt = conn.prepareStatement(sql2);
			rs = stmt.executeQuery();
			if (rs.first())
				onsetId = rs.getLong(1);
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			ex.printStackTrace();
			System.out.println("Onset.create()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}	
	}
	
	public static Onset retrieve(long onsetId) {
		Onset onset = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select * from onset where onset_id = ?";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			if (rs.first()) {
				onset = new Onset(rs.getString(2),
						rs.getLong(3),
						rs.getLong(1),
						rs.getLong(4),
						rs.getDouble(5));
			}
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Onset.retrieve(onsetId)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return onset;
	}
	
	public static List<Onset> retrieve(String recordingId, long onsetPreferenceId) {
		List<Onset> onsetList = new ArrayList<Onset>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select onset_id, onset_time, onset_salience from onset"
				+ " where recording_id = ? and cod_param_id = ?"
				+ " order by onset_id";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, recordingId);
			stmt.setLong(2, onsetPreferenceId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Onset onset = new Onset(recordingId, onsetPreferenceId, rs.getLong(1), rs.getLong(2), rs.getDouble(3));
				onsetList.add(onset);
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Onset.retrieve(recordingId, onsetPreferenceId)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return onsetList;
	}
	
	public long getOnsetId() {
		return onsetId;
	}
	public void setOnsetId(long onsetId) {
		this.onsetId = onsetId;
	}
	public String getRecordingId() {
		return recordingId;
	}
	public long getOnsetPreferenceId() {
		return onsetPreferenceId;
	}
	public long getOnsetTime() {
		return onsetTime;
	}
	public double getOnsetSalience() {
		return onsetSalience;
	}
}
