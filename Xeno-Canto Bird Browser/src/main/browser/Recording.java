package main.browser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import main.ConnectionFactory;
import main.onset.Onset;
import main.sonogram.SonogramData;

public class Recording implements Comparable {
	private String id;
	private Species species;
	private String subspecies;
	private String format;	// eg mp3, wav
	private int length; // seconds
	private String recordist;
	private String date;
	private String time;
	private String country;
	private String location;
	private String elevation; // meters
	private String type; // eg male mating call, group sounds, etc
	private boolean solitary;
	private String remarks;
	private String url;
	private boolean inDatabase;
	
	public enum EnumRecordingOrder {
		NONE,
		XCID,
		SPECIES
	}
	
	public Recording() {
		this.id = "";
		this.species = null;
		this.subspecies = "";
		this.format = "";
		this.length = 0;
		this.recordist = "";
		this.date = "";
		this.time = "";
		this.country = "";
		this.location = "";
		this.elevation = "-20000";
		this.type = "";
		this.remarks = "";
		this.solitary = false;
		this.url = "";
		this.inDatabase = false;
	}
	public Recording(String id, Species species, String subspecies, String format, int length, String recordist, String date, String time,
			String country, String location, String elevation, String type, String remarks, boolean solitary, String url) {
		super();
		this.id = id;
		this.species = species;
		this.subspecies = subspecies;
		this.format = format;
		this.length = length;
		this.recordist = recordist;
		this.date = date;
		this.time = time;
		this.country = country;
		this.location = location;
		this.elevation = elevation;
		this.type = type;
		this.remarks = remarks;
		this.solitary = solitary;
		this.url = url;
		this.inDatabase = false;
	}

	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(523, 199). // two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
				append(id).
				toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Recording))
			return false;
		if (obj == this)
			return true;
		Recording rhs = (Recording) obj;
		return new EqualsBuilder()
				// if deriving: appendSuper(super.equals(obj)).
				.append(id, rhs.id)
				.isEquals();
	}

	@Override
	public int compareTo(Object o) {
		if (equals(o))
			return 0;
		Recording ro = (Recording)o;
		String oid = ro.id;
		return id.compareTo(oid);
	}

	public void create() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		if (find(id))
			return;
		String sql = "select species_id from species where genus = ? and species = ?";
		String sql2 = "insert into species (genus, species, common_name) values (?, ?, ?)";
		String sql3 = "select last_insert_id()";
		String sql4 = "insert into recording "
				+ "(recording_id, species_id, subspecies, format, length, recordist, date, time, country, location, elevation, type, solitary, url, remarks) "
				+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, this.getSpecies().getGenus());
			stmt.setString(2, this.getSpecies().getSpecies());
			rs = stmt.executeQuery();
			if (rs.first()) {
				this.getSpecies().setDbKey(rs.getLong(1));
				rs.close();
				stmt.close();
			}
			else {
				rs.close();
				stmt.close();
				stmt = conn.prepareStatement(sql2);
				stmt.setString(1, this.getSpecies().getGenus());
				stmt.setString(2, this.getSpecies().getSpecies());
				stmt.setString(3, this.getSpecies().getCommonName());
				stmt.executeUpdate();
				stmt.close();
				stmt = conn.prepareStatement(sql3);
				rs = stmt.executeQuery();
				if (rs.first())
					this.getSpecies().setDbKey(rs.getLong(1));
				rs.close();
				stmt.close();
			}
			stmt = conn.prepareStatement(sql4);
			stmt.setString(1, this.getId());
			stmt.setLong(2, this.getSpecies().getDbKey());
			stmt.setString(3, this.getSubspecies());
			stmt.setString(4, this.getFormat());
			stmt.setLong(5, this.getLength());
			stmt.setString(6, this.getRecordist());
			stmt.setString(7, this.getDate());
			stmt.setString(8, this.getTime());
			stmt.setString(9, this.getCountry());
			stmt.setString(10, this.getLocation());
			stmt.setString(11, this.getElevation());
			stmt.setString(12, this.getType());
			stmt.setBoolean(13, this.isSolitary());
			stmt.setString(14, this.getUrl());
			stmt.setString(15, this.getRemarks());
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Recording.create()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public void update() {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "update recording set subspecies=?, format=?, length=?, recordist=?, "
				+ "date=?, time=?, country=?, location=?, elevation=?, type=?, solitary=?, url=?, remarks=? "
				+ "where recording_id=?";
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, this.getSubspecies());
			stmt.setString(2, this.getFormat());
			stmt.setLong(3, this.getLength());
			stmt.setString(4, this.getRecordist());
			stmt.setString(5, this.getDate());
			stmt.setString(6, this.getTime());
			stmt.setString(7, this.getCountry());
			stmt.setString(8, this.getLocation());
			stmt.setString(9, this.getElevation());
			stmt.setString(10, this.getType());
			stmt.setBoolean(11, this.isSolitary());
			stmt.setString(12, this.getUrl());
			stmt.setString(13, this.getRemarks());
			stmt.setString(14, this.getId());
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Recording.update()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public void delete() {
		Connection conn = null;
		PreparedStatement stmt = null;
		String id = this.getId();
		String sql2 = "delete from recording_info where recording_id = ?";
		String sql3 = "delete from recording where recording_id = ?";

		Onset.deleteWithRecordingId(this.getId());
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql2);
			stmt.setString(1, id);
			stmt.executeUpdate();
			stmt.close();
			stmt = conn.prepareStatement(sql3);
			stmt.setString(1, id);
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Recording.delete()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public static boolean find(String rid) {
		Connection conn = null;
		Statement stmt = null;
		boolean ret = false;
		try {
		    conn = ConnectionFactory.getConnection();
		    stmt = conn.createStatement();
		      String sql;
		      sql = "SELECT * from recording where recording_id = '"+rid+"'";
		      ResultSet rs = stmt.executeQuery(sql);
		      ret = rs.first();
		      rs.close();
		      stmt.close();
		      conn.close();
		} catch (SQLException ex) {
		    // handle any errors
			System.out.println("Recording.find()");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
		return ret;
	}
	
	/*
	 * Return a list of recordings of species given by speciesId
	 * @param speciesId
	 */
	public static List<Recording> retrieve(long speciesId) {
		Species species = Species.retrieve(speciesId);
		List<Recording> recordingList = new ArrayList<Recording>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select * from recording where species_id = ?";
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, speciesId);
			rs = stmt.executeQuery();
			while (rs.next())
				recordingList.add(recordingResult(species, rs));
			stmt.close();
			conn.close();
		}  catch (SQLException ex) {
			// handle any errors
			System.out.println("Recording.retrieve(long)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return recordingList;
	}
	
	private static Recording recordingResult(Species species, ResultSet rs) throws SQLException {
		Recording rec = new Recording();
		rec.setId(rs.getString(1));
		rec.setSpecies(species);
		rec.setSubspecies(rs.getString(3));
		rec.setFormat(rs.getString(4));
		rec.setLength(rs.getInt(5));
		rec.setRecordist(rs.getString(6));
		rec.setDate(rs.getString(7));
		rec.setTime(rs.getString(8));
		rec.setCountry(rs.getString(9));
		rec.setLocation(rs.getString(10));
		rec.setElevation(rs.getString(11));
		rec.setType(rs.getString(12));
		rec.setSolitary(rs.getBoolean(13));
		rec.setUrl(rs.getString(14));
		rec.setRemarks(rs.getString(15));
		return rec;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Species getSpecies() {
		return species;
	}
	public String getSubspecies() {
		return subspecies;
	}
	public void setSubspecies(String subspecies) {
		this.subspecies = subspecies;
	}
	public void setSpecies(Species species) {
		this.species = species;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getRecordist() {
		return recordist;
	}
	public void setRecordist(String recordist) {
		this.recordist = recordist;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getElevation() {
		return elevation;
	}
	public void setElevation(String elevation) {
		this.elevation = elevation;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public boolean isSolitary() {
		return solitary;
	}
	public void setSolitary(boolean solitary) {
		this.solitary = solitary;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isInDatabase() {
		return inDatabase;
	}
	public void setInDatabase(boolean inDatabase) {
		this.inDatabase = inDatabase;
	}
}
