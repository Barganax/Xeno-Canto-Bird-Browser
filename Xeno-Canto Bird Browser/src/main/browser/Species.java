package main.browser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import main.ConnectionFactory;

public class Species {
	private long dbKey;
	private String family;
	private final String genus;
	private final String species;
	private String commonName;
	
	public Species(String g, String s) {
		dbKey = 0;
		family = "";
		genus = g;
		species = s;
		commonName = "";
	}
	public Species(long dbKey, String family, String genus, String species, String commonName) {
		super();
		this.dbKey = dbKey;
		this.family = family;
		this.genus = genus;
		this.species = species;
		this.commonName = commonName;
	}
	
	@Override
	public String toString() { return genus+" "+species+" ("+commonName+")"; }
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((genus == null) ? 0 : genus.hashCode());
		result = prime * result + ((species == null) ? 0 : species.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Species other = (Species) obj;
		if (genus == null) {
			if (other.genus != null)
				return false;
		} else if (!genus.equals(other.genus))
			return false;
		if (species == null) {
			if (other.species != null)
				return false;
		} else if (!species.equals(other.species))
			return false;
		return true;
	}
	
	public static Species retrieve(long long1) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select * from species where species_id = ?";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, long1);
			rs = stmt.executeQuery();
			if (rs.first()) {
				Species s = new Species(rs.getString(3), rs.getString(4));
				s.setDbKey(long1);
				s.setFamily(rs.getString(2));
				s.setCommonName(rs.getString(5));
				rs.close();
				stmt.close();
				conn.close();
				return s;
			}
			rs.close();
			stmt.close();
			conn.close();
		}  catch (SQLException ex) {
			// handle any errors
			System.out.println("Species.retrieve(speciesId)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		
		return null;
	}	

	public static Set<Species> retrieve() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select * from species order by family, genus, species";
		Set<Species> speciesList = new LinkedHashSet<Species>();
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Species s = new Species(rs.getLong(1),
						rs.getString(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5));
				speciesList.add(s);
			}
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Species.retrieve()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return speciesList;
	}
	

	public static List<Species> retrieveNoFamily() {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "select * from species where family is NULL";
		List<Species> speciesList = new LinkedList<Species>();
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Species s = new Species(rs.getLong(1),
						rs.getString(2),
						rs.getString(3),
						rs.getString(4),
						rs.getString(5));
				speciesList.add(s);
			}
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Species.retrieveNoFamily()");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return speciesList;
	}
	
	public void updateFamily(String family) {
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "update species set family = ? where species_id = ?";
		
		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, family);
			stmt.setLong(2, this.dbKey);
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("Species.updateFamily(family)");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		this.family = family;
	}


	public long getDbKey() {
		return dbKey;
	}
	public void setDbKey(long dbKey) {
		this.dbKey = dbKey;
	}
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public String getGenus() {
		return genus;
	}
	public String getSpecies() {
		return species;
	}
	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
}
