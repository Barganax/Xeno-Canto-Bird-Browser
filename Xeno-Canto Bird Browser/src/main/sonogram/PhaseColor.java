package main.sonogram;

import java.awt.Color;

public class PhaseColor {
	/*
	 * Interpret phase (in the range -pi to pi) as a point on a unit circle
	 * superimposed on a standard color wheel (max saturation on the circumference)
	 * with pure green as 0.  Negative values tend toward red, positive toward blue.
	 */
	
	public static int NEUTRAL_VALUE = 85;
	public static final double TWO_THIRDS_PI = 2*Math.PI/3;
	public static Color phaseColor(double phase) {
		Color c = null;
		
		int redValue = (int) (255*(Math.cos(phase+TWO_THIRDS_PI)+1)/2);
		int greenValue = (int) (255*(Math.cos(phase)+1)/2);
		int blueValue = (int) (255*(Math.cos(phase-TWO_THIRDS_PI)+1)/2);
		c = new Color(redValue, greenValue, blueValue);
		return c;
	}
	
	public static Color phaseColor(double phase, double saturation) {
		Color c = phaseColor(phase);
		int redValue = NEUTRAL_VALUE+(int)((c.getRed()-NEUTRAL_VALUE)*saturation);
		int greenValue = NEUTRAL_VALUE+(int)((c.getGreen()-NEUTRAL_VALUE)*saturation);
		int blueValue = NEUTRAL_VALUE+(int)((c.getBlue()-NEUTRAL_VALUE)*saturation);
		if (redValue < 0)
			redValue = 0;
		if (greenValue < 0)
			greenValue = 0;
		if (blueValue < 0)
			blueValue = 0;
		if (redValue > 255)
			redValue = 255;
		if (greenValue > 255)
			greenValue = 255;
		if (blueValue > 255)
			blueValue = 255;
		c = new Color(redValue, greenValue, blueValue);
		return c;
	}
}
