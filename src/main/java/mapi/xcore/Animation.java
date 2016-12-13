package mapi.xcore;

import java.io.Serializable;
import java.util.function.DoubleConsumer;

@SuppressWarnings("serial")
public class Animation implements Serializable {
	@FunctionalInterface
	public interface Calculate extends Serializable {
		public abstract double apply(double f);
	}
	private int frames, currentFrames, allFrames;
	private Calculate c;
	public Animation(int frames, int allFrames, Calculate c){
		this.frames = frames;
		this.allFrames = allFrames;
		this.c = c;
	}
	public final void setCurrentframes(int i){
		currentFrames = i;
	}
	public final int getCurrentframes(){
		return currentFrames;
	}
	public final int getAllframes(){
		return allFrames;
	}
	public final double getAnimation(){
		sleep(frames == 0 ? 0 : (1000 / frames));
		return c.apply(++currentFrames == allFrames + 1 ? currentFrames = 0 : currentFrames);
	}
	public final double getAnimation(int frame){
		return c.apply(frame);
	}
	public final void play(DoubleConsumer d){
		play(0, allFrames, d);
	}
	public final void play(int endFrames, DoubleConsumer d){
		play(0, endFrames, d);
	}
	public final void play(int startFrames, int endFrames, DoubleConsumer d){
		checkFrames(startFrames, endFrames);
		currentFrames = startFrames;
		while(currentFrames < endFrames)d.accept(getAnimation());
	}
	public final void playLoop(int startFrames, DoubleConsumer d){
		checkFrames(startFrames);
		currentFrames = startFrames;
		while(true){
			if(currentFrames > allFrames)currentFrames = 0;
			d.accept(getAnimation());
		}
	}
	public final void checkFrames(int... fa){
		for(int frames : fa)if(frames > allFrames)throw new RuntimeException("The frames is " + frames + ", should not be more than " + allFrames + ".");
		else if(frames < 0)throw new RuntimeException("The frames is " + frames + ", should not be less than 0.");
	}
	public static final void sleep(long ms){
		try {Thread.sleep(ms);} catch(Exception e){}
	}
	public static final void sleepSeconds(int s){
		try {Thread.sleep(s * 1000);} catch(Exception e){}
	}
	public static final void sleepMinute(int m){
		try {Thread.sleep(m * 1000 * 60);} catch(Exception e){}
	}
	public static final void sleepHour(int h){
		try {Thread.sleep(h * 1000 *60 *60);} catch(Exception e){}
	}
}
