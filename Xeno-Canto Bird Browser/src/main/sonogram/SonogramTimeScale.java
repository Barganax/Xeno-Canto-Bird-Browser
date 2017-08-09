package main.sonogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

public class SonogramTimeScale extends JComponent {
	private BufferedImage bufferedImage = null;
	private Graphics2D bufferedGraphics = null;
	public static final int IMAGE_HEIGHT = 30;
	public static final int MAX_TIME_SCALE = 300;
	private static final int ONSET_MARK_LENGTH = 8;
	
	public SonogramTimeScale() {
		super();
		setBufferedImage(new BufferedImage(SonogramPanel.IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB));
		setBufferedGraphics(bufferedImage.createGraphics());
		setPreferredSize(new Dimension(SonogramPanel.IMAGE_WIDTH, IMAGE_HEIGHT));
		drawScale();
	}
	
	private void drawScale() {
		bufferedGraphics.setColor(Color.WHITE);
		for (int i = 0; i < MAX_TIME_SCALE; i++) {
        	int markPosition = SonogramPanel.timeToX((double)i);
        	bufferedGraphics.drawLine(markPosition, IMAGE_HEIGHT - 10, markPosition, IMAGE_HEIGHT);
        	if (i % 2 == 0) {
            	bufferedGraphics.drawLine(markPosition, IMAGE_HEIGHT - 15, markPosition, IMAGE_HEIGHT - 10);
        		bufferedGraphics.drawString(String.valueOf((double)i), markPosition - 10, IMAGE_HEIGHT - 20);
        	}
        }
	}
	
	public void markOnset(double time, double salience) {
//		System.out.println("Marking onset at "+time+", salience: "+salience);
		int x = SonogramPanel.timeToX(time);
		int b = (int)(salience/2);
		if (b > 255) b = 255;
		bufferedGraphics.setColor(new Color(b, 0, 0, 255));
		bufferedGraphics.drawLine(x, IMAGE_HEIGHT-ONSET_MARK_LENGTH, x, IMAGE_HEIGHT);
		repaint();
	}
	
	public void reset() {
		bufferedGraphics.setColor(Color.BLACK);
		bufferedGraphics.fillRect(0, 0, SonogramPanel.IMAGE_WIDTH, IMAGE_HEIGHT);
		drawScale();
//		this.revalidate();
		repaint();

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


}
