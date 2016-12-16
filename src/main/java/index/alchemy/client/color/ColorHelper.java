package index.alchemy.client.color;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.awt.Color;
import java.util.Iterator;

import index.project.version.annotation.Omega;
import net.minecraft.client.renderer.GlStateManager;

@Omega
public class ColorHelper {
	
	public static Iterator<Color> argbStep(final Color start, final Color end, final int steps, final boolean keepA) {
		return new Iterator<Color>() {
			int r = end.getRed() - start.getRed();
			int g = end.getGreen() - start.getGreen();
			int b = end.getBlue() - start.getBlue();
			int a = keepA ? 0 : end.getAlpha() - start.getAlpha();
			
			int nSteps = steps > 0 ? steps : max(max(max(abs(r), max(abs(g), abs(b))), abs(a)), 1), i = nSteps, directions = -1;
			
			float rStep = r / (float) nSteps;
			float gStep = g / (float) nSteps;
			float bStep = b / (float) nSteps;
			float aStep = a / (float) nSteps;
			
			float fr, fg, fb, fa;
			
			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Color next() {
				if (++i > nSteps) {
					directions *= -1;
					i = 0;
					Color now = directions > 0 ? start : end;
					fr = now.getRed();
					fg = now.getGreen();
					fb = now.getBlue();
					fa = (keepA ? start : now).getAlpha();
				} else {
					fr += rStep * directions;
				    fg += gStep * directions;
				    fb += bStep * directions;
				    fa += aStep * directions;
				}
				
				return new Color((int) fr, (int) fg, (int) fb, (int) fa);
			}
			
			@Override
			public void remove() { }
		};
	}
	
	public static Iterator<Color> ahsbStep(final Color start, final Color end, final int steps, final boolean keepS, final boolean keepB, final boolean keepA) {
		return new Iterator<Color>() {
			float start_hsb[] = new float[4], end_hsb[] = new float[4];
			{
				Color.RGBtoHSB(start.getRed(), start.getGreen(), start.getBlue(), start_hsb);
				Color.RGBtoHSB(end.getRed(), end.getGreen(), end.getBlue(), end_hsb);
				start_hsb[3] = start.getAlpha();
				end_hsb[3] = end.getAlpha();
				if (keepS)
					end_hsb[1] = start_hsb[1];
				if (keepB)
					end_hsb[2] = start_hsb[2];
				if (keepA)
					end_hsb[3] = start_hsb[3];
			}
			
			float h = end_hsb[0] - start_hsb[0];
			float s = end_hsb[1] - start_hsb[1];
			float b = end_hsb[2] - start_hsb[2];
			float a = end_hsb[3] - start_hsb[3];
			
			int nSteps = (int) (steps > 0 ? steps : max(max(max(abs(h * 360), max(abs(s * 100), abs(b * 100))), abs(a)), 1)), i = nSteps, directions = -1;
			
			float hStep = h / (float) nSteps;
			float sStep = s / (float) nSteps;
			float bStep = b / (float) nSteps;
			float aStep = a / (float) nSteps;
			
			float fh, fs, fb, fa;

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Color next() {
				if (++i > nSteps) {
					directions *= -1;
					i = 0;
					float now[] = directions > 0 ? start_hsb : end_hsb;
					fh = now[0];
					fs = now[1];
					fb = now[2];
					fa = (directions > 0 ? start : end).getAlpha();
				} else {
					fh += hStep * directions;
				    fs += sStep * directions;
				    fb += bStep * directions;
				    fa += aStep * directions;
				}
				
				int rgb = Color.HSBtoRGB(fh, fs, fb);
				return new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, (int) fa);
			}
			
			@Override
			public void remove() { }
		};
	}
	
	public static Color[] toArray(Iterator<Color> iterator, int size) {
		Color result[] = new Color[size];
		for (int i = 0; i < size; i++)
			result[i] = iterator.next();
		return result;
	}
	
	public static void setColor(Color color) {
		GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
	}

}
