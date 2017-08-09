package main.browser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
	private final List<Recording> recordingList;
	private final Set<Species> speciesList;
	
	public Parser(Set<Species> sl) {
		recordingList = new LinkedList<Recording>();
		speciesList = sl;
	}
	
	/*
	 * Parse a Xeno-Canto results page
	 */
	static final Pattern RESULTS_PATTERN
	= Pattern.compile(".+<table class=\"results\">\\s*<thead>.*?<\\/thead>(.*?)<\\/table>.*", Pattern.DOTALL);
	List<Recording> parsePage (String html) {
		Matcher m = RESULTS_PATTERN.matcher(html);
		if (m.matches())
			parseTable(m.group(1).trim());
		else System.out.println("RESULTS_PATTERN: NO MATCH");
		return recordingList;
	}
	
	/*
	 * Parse parse html table on Xeno-Canto results page
	 */
	private static final Pattern TABLE_ROW_PATTERN
	= Pattern.compile("<\\s*tr\\s*>(.*?)<\\s*\\/tr\\s*>", Pattern.DOTALL);
	private void parseTable(String data) {
		Matcher m = TABLE_ROW_PATTERN.matcher(data);
		while (m.find()) {
			Recording recording = createRecording(m.group(1).trim());
			if (recording != null)
				recordingList.add(createRecording(m.group(1).trim()));
		}
	}
	
	private static final Pattern TABLE_DATA_PATTERN
	= Pattern.compile("<\\s*td\\s*>(.*?)<\\s*\\/td\\s*>", Pattern.DOTALL);
	private Recording createRecording(String tableRow) {
		Matcher m = TABLE_DATA_PATTERN.matcher(tableRow);
		Recording recording = new Recording();
		if (m.find()) {
			if (!parseFirstField(recording, m.group(1).trim()))
				return null;
//			System.out.println("URL: "+recording.getUrl());
			if (m.find()) {
				parseNameField(recording, m.group(1).trim());
//				System.out.println("Genus: "+recording.getSpecies().getGenus()
//						+", Species: "+recording.getSpecies().getSpecies()x
//						+", Common Name: "+recording.getSpecies().getCommonName()
//						+", ID: "+recording.getId());
				if (m.find()) {
					parseLength(recording, m.group(1).trim());
//					System.out.println("Length: "+recording.getLength());
					if (m.find()) {
						recording.setRecordist(removeTags(m.group(1)).trim());
//						System.out.println("Recordist: "+recording.getRecordist());
						if (m.find()) {
							recording.setDate(m.group(1).trim());
//							System.out.println("Date: "+recording.getDate());
							if (m.find()) {
								recording.setTime(m.group(1).trim());
//								System.out.println("Time: "+recording.getTime());
								if (m.find()) {
									recording.setCountry(m.group(1).trim());
//									System.out.println("Country: "+recording.getCountry());
									if (m.find()) {
										recording.setLocation(removeTags(m.group(1).trim()));
//										System.out.println("Location: "+recording.getLocation());
										if (m.find()) {
											recording.setElevation(m.group(1).trim());
//											System.out.println("Elevation: "+recording.getElevation());
											if (m.find()) {
												recording.setType(m.group(1).trim());
//												System.out.println("Type: "+recording.getType());
												if (m.find()) {
													recording.setRemarks(removeTags(m.group(1)));
//													System.out.println("Remarks: '"+recording.getRemarks()+"'");
													
												}
											}
										}
									}
								}
							}
						}
					}
				}
			} else
				return null;
		} else
			return null;
		return recording;
	}
	
	private static final Pattern XCID_PATTERN
	= Pattern.compile("data-xc-id='(.*?)'", Pattern.DOTALL);
	private static final Pattern RECORDING_URL_PATTERN
		= Pattern.compile("data-xc-filepath='([^']*)'", Pattern.DOTALL);
	private static final Pattern FORMAT_PATTERN
		= Pattern.compile(".+\\.([a-zA-Z0-9]{2,4})");
	private boolean parseFirstField(Recording r, String f) {
		Matcher m = XCID_PATTERN.matcher(f);
		if (m.find()) {
			String rid = "XC"+m.group(1);
			r.setId(rid);
			r.setInDatabase(Recording.find(rid));
		} else
			return false;
		
		m = RECORDING_URL_PATTERN.matcher(f);
		if (m.find()) {
			r.setUrl(m.group(1).trim());
			m = FORMAT_PATTERN.matcher(r.getUrl());
			if (m.matches()) r.setFormat(m.group(1));
		}
		else
			return false;
		return true;
	}

	private static final Pattern COMMON_NAME_PATTERN
		= Pattern.compile("<span class='common-name'>(.*?)<\\/span>", Pattern.DOTALL);
	private static final Pattern SCIENTIFIC_NAME_PATTERN
		= Pattern.compile("<span class='scientific-name'>(\\w+) +(.*?)<\\/span>", Pattern.DOTALL);
	private void parseNameField(Recording r, String f) {
		String genus, species, commonName = "";
		Matcher m = COMMON_NAME_PATTERN.matcher(f);
		if (m.find()) commonName = removeTags(m.group(1)).trim();
		m = SCIENTIFIC_NAME_PATTERN.matcher(f);
		if (m.find()) {
			genus = m.group(1).toUpperCase();
			String s = m.group(2).toUpperCase();
			int p = s.indexOf(' ');
			if (p > -1) {
				species = s.substring(0, p);
				r.setSubspecies(s.substring(p).trim());
			} else species = s;
			r.setSpecies(new Species(genus, species));
			r.getSpecies().setCommonName(commonName);
		}
		replaceExistingSpecies(r);
	}

	private void replaceExistingSpecies(Recording r) {
		Species species = r.getSpecies();
		Iterator<Species> speciesListIterator = speciesList.iterator();
		while (speciesListIterator.hasNext()) {
			Species existing = speciesListIterator.next();
			if (species == existing) {
				r.setSpecies(existing);
				return;
			}
		}
			
	}
	
	private static final Pattern TIME_PATTERN1
		= Pattern.compile("(\\d{1,2}):(\\d{2}):(\\d{2})");
	private static final Pattern TIME_PATTERN2
		= Pattern.compile("(\\d{1,2}):(\\d{2})");
	private void parseLength(Recording r, String f) {
		Matcher m = TIME_PATTERN1.matcher(f);
		if (m.matches())
			r.setLength(Integer.parseInt(m.group(1))*3600+Integer.parseInt(m.group(2))*60+Integer.parseInt(m.group(3)));
		else {
			m = TIME_PATTERN2.matcher(f);
			if (m.matches())
				r.setLength(Integer.parseInt(m.group(1))*60+Integer.parseInt(m.group(2)));
			else r.setLength(Integer.parseInt(f));
		}
	}
	
	private static final String TAG_PATTERN_STRING
		= "<[^>]+>";
	private String removeTags(String s) {
		return s.replace("\n", " ")
			.replaceAll(" {2,}", " ")
			.replace("&gt;", ">")
			.replace("&lt;", "<")
			.replace("&amp;", "&")
			.replace("\t", " ")
			.replaceAll(TAG_PATTERN_STRING, " ").trim();
	}
}
