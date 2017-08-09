package main.browser;

import java.awt.Component;
import java.awt.Cursor;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingWorker;

public class Fetcher {
	private static final String BASE_URL = "http://www.xeno-canto.org/explore";
	private static final String PAGE_ARG_SUFFIX = "?pg=";
	
	private static ProgressMonitor progressMonitor = null;
	private static Component parent = null;
	

	/**
	 * Open a buffered input stream with progress monitor
	 *
	 * @param parent: parent component for the dialog
	 * @param title: Title of the dialog
	 * @param urlString: url to read
	 * @return: buffered input stream from the url or null if something goes wrong
	 */
		
	public static InputStream progressInputStream(Component parent, String title, String urlString) {
		ProgressMonitorInputStream pmis = null;
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStream is = null;
		try {
			URLConnection connection = url.openConnection();
			int size = connection.getContentLength();
			is = new BufferedInputStream(pmis = new ProgressMonitorInputStream(
					parent,
					title,
					url.openStream()));
			setProgressMonitor(pmis.getProgressMonitor());
			getProgressMonitor().setMinimum(0);
			getProgressMonitor().setMaximum(size);
			getProgressMonitor().setMillisToDecideToPopup(0);
			getProgressMonitor().setMillisToPopup(10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return is;
	}

	public static InputStream progressInputStream(Component parent, String title, File file) {
		ProgressMonitorInputStream pmis = null;
		InputStream is = null;
		int size = (int)file.length();
		try {
			FileInputStream fis = new FileInputStream(file);
			is = new BufferedInputStream(pmis = new ProgressMonitorInputStream(
					parent,
					title,
					fis));
			setProgressMonitor(pmis.getProgressMonitor());
			getProgressMonitor().setMinimum(0);
			getProgressMonitor().setMaximum(size);
			getProgressMonitor().setMillisToDecideToPopup(0);
			getProgressMonitor().setMillisToPopup(10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return is;
	}

	public static String fetch(String urlString) {
		URL url = null;
		String urlContent = "";
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				urlContent += inputLine;
			in.close();
		} catch (IOException e) {
			return "";
		}
		return urlContent.trim();
	}
	
	public static String fetch(int page) {
		String urlString = BASE_URL;
		if (page > 1) urlString += PAGE_ARG_SUFFIX+page;
		InputStream is = progressInputStream(parent, "Loading Xeno-Canto web page", urlString);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        StringBuilder response = new StringBuilder();
        String inputLine;
        try {
			while ((inputLine = in.readLine()) != null) 
			    response.append(inputLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return response.toString();
    }

	public static ProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public static void setProgressMonitor(ProgressMonitor progressMonitor) {
		Fetcher.progressMonitor = progressMonitor;
	}

	public static Component getParent() {
		return parent;
	}

	public static void setParent(Component parent) {
		Fetcher.parent = parent;
	}	
}