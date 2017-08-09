package main.browser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import main.ConnectionFactory;

public class RecordingInfo {
	private final String recordingId;
	private final double maxAmplitude;
	private final double avgAmplitude;
	private final double varAmplitude;
	
	public RecordingInfo(String recordingId, double maxAmplitude, double avgAmplitude) {
		super();
		this.recordingId = recordingId;
		this.maxAmplitude = maxAmplitude;
		this.avgAmplitude = avgAmplitude;
		this.varAmplitude = -1;
	}
	

	public RecordingInfo(String recordingId, double maxAmplitude, double avgAmplitude, double varAmplitude) {
		super();
		this.recordingId = recordingId;
		this.maxAmplitude = maxAmplitude;
		this.avgAmplitude = avgAmplitude;
		this.varAmplitude = varAmplitude;
	}
	
	public void create() {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "insert into recording_info (recording_id, max_amplitude, avg_amplitude, var_amplitude) values (?, ?, ?, ?)";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, recordingId);
			stmt.setDouble(2, maxAmplitude);
			stmt.setDouble(3, avgAmplitude);
			stmt.setDouble(4, varAmplitude);
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("RecordingInfo.create()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public void delete() {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "delete from recording_info where recording_id = ?";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, recordingId);
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("RecordingInfo.delete()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	public static RecordingInfo retrieve(String recordingId) {
		RecordingInfo ri = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select max_amplitude, avg_amplitude, var_amplitude from recording_info where recording_id = ?";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, recordingId);
			rs = stmt.executeQuery();
			if (rs.first())
				ri = new RecordingInfo(recordingId, rs.getDouble(1), rs.getDouble(2), rs.getDouble(3));			
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("RecordingInfo.retrieve(recordingId)");

			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return ri;
	}


	public double getMaxAmplitude() {
		return maxAmplitude;
	}


	public double getAvgAmplitude() {
		return avgAmplitude;
	}


	public double getVarAmplitude() {
		return varAmplitude;
	}
	
}
