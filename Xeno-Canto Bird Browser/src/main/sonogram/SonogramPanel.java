package main.sonogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import be.tarsos.dsp.util.PitchConverter;
import main.browser.database.DatabaseCard;
import main.onset.Onset;


public class SonogramPanel extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int IMAGE_WIDTH = 8000;
	public static final int IMAGE_HEIGHT = 480;
	public static final int MIN_FREQUENCY = 50;
	public static final int MAX_FREQUENCY = 25000;
	public static final int MAX_TIME = 300;
	public static final double DEFAULT_MAX_POWER = .5;
	private static final int MAX_POSITION_INC = 3;
	private static final int DEFAULT_DISPLAYED_TYPE = 0;
	
	private final DatabaseCard databaseCard;
	
	private SonogramData lastSonogramData = null;
	private JScrollPane scrollPane = null;
	private BufferedImage bufferedImage = null;
	private Graphics2D bufferedGraphics = null;
	private int displayedType = DEFAULT_DISPLAYED_TYPE;
	private int position = 0;
	private int viewPosition = 0;
	private double maxPower;
	private double baseFrequency;
	private int loIndex;
	private int hiIndex;
	
	public SonogramPanel(DatabaseCard dbc) {
		super();
		databaseCard = dbc;
		setBufferedImage(new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB));
		setBufferedGraphics(getBufferedImage().createGraphics());
		setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
		reset(databaseCard.sonogramPreference);
	}
	
	public void reset(SonogramPreference sp) {
		databaseCard.sonogramPreference = sp;
//		baseFrequency = databaseCard.sonogramPreference.getBaseFrequency();
		baseFrequency = databaseCard.sonogramPreference.baseFrequency[displayedType];
		setMaxPower(DEFAULT_MAX_POWER);
		loIndex = sp.getLoIndex();
		hiIndex = sp.getHiIndex();
        reset();
	}

	public void reset() {
		getBufferedGraphics().setColor(Color.BLACK);
		getBufferedGraphics().fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
		if (getScrollPane() != null)
			getScrollPane().getViewport().setViewPosition(new Point(0, 0));
		setViewPosition(0);
		setPosition(0);
	}

	@Override
	protected void paintComponent(final Graphics g) { g.drawImage(bufferedImage, 0, 0, null); }
	
	public static int timeToX(final double time) { return (int) (time*IMAGE_WIDTH/MAX_TIME); }

	public void drawSonogramData(SonogramData sonogramData) {
		if (sonogramData.type != displayedType)
			return;
		sonogramData.calculatePhase();
		updatePosition(sonogramData.timeStamp);
		drawSonogramData(sonogramData, position, 1);
		JViewport viewport = scrollPane.getViewport();
		if ((position - viewPosition > viewport.getWidth()))
			bumpViewport(viewport);
		lastSonogramData = sonogramData;
		repaint();
	}
	
	private void updatePosition(double timeStamp) {
        setPosition(timeToX(timeStamp));
        if (lastSonogramData != null) {
        	int lastPosition = timeToX(lastSonogramData.timeStamp);
            erasePosition(lastPosition + 1);
        	int width = position - timeToX(lastSonogramData.timeStamp);
        	if (width > 0)
        		drawSonogramData(lastSonogramData, lastPosition, width);
        }
	}
	
	private void drawSonogramData(SonogramData sonogramData, int x, int width) {
		int[] indices = sonogramData.index;
		sonogramData.calculatePhase();
		for (int i = 0; i < sonogramData.dataSize; i++) {
			int index = indices[i];
			int y = frequencyToY(index * baseFrequency);
			Color c = PhaseColor.phaseColor(sonogramData.phs[i]);
			c = scaleColor(c, sonogramData.pwr[i]/maxPower);
			bufferedGraphics.setColor(c);
			bufferedGraphics.fillRect(x, y, width, 1);
		}
		markCursor();
	}

	private Color scaleColor(Color c, double b) {
		if (b < 0.0)
			return Color.BLACK;
		if (b >= 1.0)
			return c;
		int redValue = (int)(b*c.getRed());
		int greenValue = (int)(b*c.getGreen());
		int blueValue = (int)(b*c.getBlue());
		return new Color(redValue, greenValue, blueValue);
	}
	
	private void erasePosition(int p) {
		getBufferedGraphics().setColor(Color.BLACK);
		getBufferedGraphics().fillRect(p, 0, p + MAX_POSITION_INC, IMAGE_HEIGHT);
	}
	
	private void bumpViewport(JViewport viewport) {
		viewport.setViewPosition(new Point((int) position, 0));
		setViewPosition(position);
	}

	public void markSonogram(Sonogram sonogram) {
		int start = timeToX((double)sonogram.getOnset().getOnsetTime()/1000);
		int end = timeToX((double)(sonogram.getOnset().getOnsetTime()+sonogram.getLen())/1000);
		bufferedGraphics.setColor(Color.GRAY);
		bufferedGraphics.setXORMode(Color.BLACK);
		bufferedGraphics.fillRect(0, 0, start - 1, IMAGE_HEIGHT);
		bufferedGraphics.fillRect(end + 1, 0, IMAGE_WIDTH - end, IMAGE_HEIGHT);
		bufferedGraphics.setPaintMode();
		if (start > 15)
			position = start - 15;
		else
			position = 0;
		bumpViewport(scrollPane.getViewport());
		repaint();
	}
	
	private void markCursor() {
		int newCursorPosition = getPosition()+1;
		getBufferedGraphics().setColor(Color.YELLOW);
		getBufferedGraphics().drawLine(newCursorPosition, 0, newCursorPosition, IMAGE_HEIGHT);
	}

	public static int frequencyToY(final double frequency) {
		int y = 0;
		if (frequency > MIN_FREQUENCY && frequency < MAX_FREQUENCY) {
			final double minCent = PitchConverter.hertzToAbsoluteCent(MIN_FREQUENCY);
			final double maxCent = PitchConverter.hertzToAbsoluteCent(MAX_FREQUENCY);
			y = (int) (((PitchConverter.hertzToAbsoluteCent(frequency*2)-minCent) / maxCent) * IMAGE_HEIGHT); 
		}

		return IMAGE_HEIGHT - 1 - y;
	}
				
	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}
	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}
	public Graphics2D getBufferedGraphics() {
		return bufferedGraphics;
	}
	public void setBufferedGraphics(Graphics2D bufferedGraphics) {
		this.bufferedGraphics = bufferedGraphics;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getViewPosition() {
		return viewPosition;
	}

	public void setViewPosition(int viewPosition) {
		this.viewPosition = viewPosition;
	}

	public double getBaseFrequency() {
		return baseFrequency;
	}

	public void setBaseFrequency(double baseFrequency) {
		this.baseFrequency = baseFrequency;
	}

	public double getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}
}
