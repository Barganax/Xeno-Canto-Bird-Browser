package main.browser;

import java.awt.Cursor;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

/*
 * fetches a specific Xeno-Canto page
 */
public class LoadPageWorker extends SwingWorker<String, String> {
	private final JPanel container;
	private String html = "";
	private final int pageNumber;

	public LoadPageWorker(int pn) {
		super();
		container = null;
		pageNumber = pn;
		System.out.println("LoadPageWorker instantiated; page = "+pageNumber);
	}
	
	public LoadPageWorker(JPanel c, int pn) {
		super();
		container = c;
		pageNumber = pn;
	}

	@Override
	protected String doInBackground() throws Exception {
		if (container != null)
			container.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		html = Fetcher.fetch(pageNumber);
		return html;
	}

	@Override
	protected void done() {
		if (container != null)
			container.setCursor(Cursor.getDefaultCursor());
		super.done();
	}
}

