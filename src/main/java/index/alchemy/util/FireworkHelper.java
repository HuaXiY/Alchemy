package index.alchemy.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.particle.ParticleFirework;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FireworkHelper {
	
	public static double[][] stringToDoubleArrayShape(String str) {
		//str = "Sayaka\\n	I love you ~";
		String strings[] = str.split("\\\\n");
		List<double[]> result = Lists.newLinkedList();
		double offsetY = 1;
		for (String string : strings) {
			offsetY--;
			double offsetX = 0;
			char chars[] = string.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				offsetX += 0.5D;
				if (Character.isSpaceChar(chars[i]))
					continue;
				BufferedImage image = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
				Graphics g = image.getGraphics();
				Font font = g.getFont();
				g.setColor(Color.WHITE);
				g.setFont(new Font(font.getFontName(), font.getStyle(), 15));
				g.drawString(String.valueOf(chars[i]), 0, 14);
				g.dispose();
				for (int x = 0; x < 18; x++)
					for (int y = 0; y < 18; y++)
						if (image.getRGB(x, y) == -1)
							result.add(new double[]{ offsetX + x / 18.0D, offsetY + (1 - y / 18.0D) });
			}
		}
		return result.toArray(new double[result.size()][2]);
	}
	
	public static void createShaped(ParticleFirework.Starter starter, String str, double speed, int[] colours,
			int[] fadeColours, boolean trail, boolean twinkle) {
		double shape[][] = stringToDoubleArrayShape(str);
		if (shape.length == 0)
			return;
		double fx = shape[0][0];
		double fy = shape[0][1];
		starter.createParticle(starter.posX, starter.posY, starter.posZ, fx * speed, fy * speed, 0.0D, colours, fadeColours,
				trail, twinkle);
		float f = starter.rand.nextFloat() * (float) Math.PI;
		double d = f + Math.PI;
		for (int s = 1; s < shape.length; s++) {
			double nx = shape[s][0];
			double ny = shape[s][1];
			double rx = nx * speed;
			double ry = ny * speed;
			double sinx = rx * Math.sin(d);
			rx = rx * Math.cos(d);
			starter.createParticle(starter.posX, starter.posY, starter.posZ, rx * -1, ry, sinx * -1, colours,
					fadeColours, trail, twinkle);
			starter.createParticle(starter.posX, starter.posY, starter.posZ, rx, ry, sinx, colours,
					fadeColours, trail, twinkle);
		}
	}

}
