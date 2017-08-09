package main.browser;

import java.awt.Dimension;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogPanel extends JScrollPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int logSize = 40;
	private final XenoCantoAutoCard xenoCantoAutoCard;
	private final Deque<String> logEntryDeque;
	private final JPanel scrolledPanel;
	private final JTextArea textArea;
	
	public LogPanel(XenoCantoAutoCard xcac) {
		super();
		xenoCantoAutoCard = xcac;
		scrolledPanel = new JPanel();
		logEntryDeque = new LinkedList<String>();
		textArea = new JTextArea(" ");
		textArea.setLineWrap(true);
		textArea.setPreferredSize(new Dimension(820, 620));
		scrolledPanel.add(textArea);
		setViewportView(scrolledPanel);
	}
	
	public synchronized void addLogEntry(String logEntry) {
		System.out.println(logEntry);
		if (logEntryDeque.size() == logSize)
			logEntryDeque.poll();
		logEntryDeque.offer(logEntry);
		textArea.setText(log());
		repaint();
	}
	
	private String log() {
		String l = "";
		Iterator<String> i = logEntryDeque.iterator();
		while (i.hasNext())
			l += i.next() + "\n";
		return l;
	}
}
