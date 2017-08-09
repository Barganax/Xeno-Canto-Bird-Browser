package main.sonogram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

public class Spacer extends JComponent {
	public static final int IMAGE_WIDTH = SonogramFrequencyScale.IMAGE_WIDTH;
	public static final int IMAGE_HEIGHT = SonogramTimeScale.IMAGE_HEIGHT;
	
	private BufferedImage bufferedImage = null;
	private Graphics2D bufferedGraphics = null;
	
	public Spacer() {
		super();
		setBufferedImage(new BufferedImage(IMAGE_WIDTH, SonogramPanel.IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB));
		setBufferedGraphics(bufferedImage.createGraphics());
		setPreferredSize(new Dimension(IMAGE_WIDTH, SonogramPanel.IMAGE_HEIGHT));
		bufferedGraphics.setColor(Color.CYAN);
		bufferedGraphics.drawLine(0, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);
	}

	@Override
	protected void paintComponent(final Graphics g) { g.drawImage(bufferedImage, 0, 0, null); }

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
}
