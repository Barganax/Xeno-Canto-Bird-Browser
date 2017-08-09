package main.sonogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import main.browser.Browser;

public class SonogramFrequencyScale extends JComponent implements PitchDetectionHandler {
	public static final int MAX_FREQUENCY_SCALE = 30000;
	public static final int IMAGE_WIDTH = 100;
	
	private float lastPitch = -1;
	private BufferedImage bufferedImage = null;
	private Graphics2D bufferedGraphics = null;
	
	public SonogramFrequencyScale() {
		super();
		setBufferedImage(new BufferedImage(IMAGE_WIDTH, SonogramPanel.IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB));
		setBufferedGraphics(bufferedImage.createGraphics());
		setPreferredSize(new Dimension(IMAGE_WIDTH, SonogramPanel.IMAGE_HEIGHT));
		drawScale();
	}
	
	private void drawScale() {
		bufferedGraphics.setColor(Color.WHITE);
		int y;
		for (int i = 100; i < MAX_FREQUENCY_SCALE; i*=10) {
			y = SonogramPanel.frequencyToY(i);
			drawTic(3, y);
			drawLabel(i, y);
		}
		for (int i = 2; i<10; i++) {
			y = SonogramPanel.frequencyToY(100*i);
			if (i == 5) {
				drawTic(2, y);
				drawLabel(100*i, y);
			}
			else
				drawTic(1, y);
			y = SonogramPanel.frequencyToY(1000*i);
			if (i == 5) {
				drawTic(2, y);
				drawLabel(1000*i, y);
			}
			else
				drawTic(1, y);
		}
		y = SonogramPanel.frequencyToY(20000);
		drawTic(2, y);
		drawLabel(20000, y);
		repaint();
	}
	
	private void drawTic (int size, int y) { bufferedGraphics.drawLine(IMAGE_WIDTH-10*size, y, IMAGE_WIDTH, y); }
	
	private void drawLabel(int i, int y) { bufferedGraphics.drawString(String.valueOf(i), 0, y+5); }
	
	public void drawCutoffs(int low, int high) {
		Color c = new Color(127, 127, 127, 127);
		int highY = SonogramPanel.frequencyToY(high);
		int lowY = SonogramPanel.frequencyToY(low);
		bufferedGraphics.setColor(c);
		bufferedGraphics.fillRect(0, 0, IMAGE_WIDTH, highY);
		bufferedGraphics.fillRect(0, lowY, IMAGE_WIDTH, SonogramPanel.IMAGE_HEIGHT);
		repaint();
	}

	public void reset(SonogramPreference sp) {
		this.reset();
		this.drawCutoffs(sp.getLoCutoff(), sp.getHiCutoff());
	}
	
	public void reset() {
		bufferedGraphics.setColor(Color.BLACK);
		bufferedGraphics.fillRect(0, 0, IMAGE_WIDTH, SonogramPanel.IMAGE_HEIGHT);
		drawScale();
	}
	
	public float getLastPitch() {
		return lastPitch;
	}

	public void setLastPitch(float lastPitch) {
		this.lastPitch = lastPitch;
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

	@Override
	protected void paintComponent(final Graphics g) { g.drawImage(bufferedImage, 0, 0, null); }

	@Override
	public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
		float f = pitchDetectionResult.getPitch();
		if (f != -1)
			drawIndicator(f);
		if (this.getLastPitch() != -1)
			drawIndicator(this.getLastPitch());
		bufferedGraphics.setPaintMode();
		this.setLastPitch(f);
		repaint();
	}
	
	private void drawIndicator(float f) {
		int y = SonogramPanel.frequencyToY(f);
		bufferedGraphics.setXORMode(Color.BLUE);
		bufferedGraphics.drawString(String.valueOf((int)f)+" Hz", 30, y + 3);
		bufferedGraphics.fillRect(IMAGE_WIDTH -15, y - 1, 5, 3);
		bufferedGraphics.drawLine(IMAGE_WIDTH - 10, y, IMAGE_WIDTH, y);
	}
}
