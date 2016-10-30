package index.alchemy.animation;

import index.alchemy.api.ICycle;

public class StdCycle implements ICycle {
	
	/*
	 * 1 cycle: min -> max
	 * if (!loop) 1 cycle over: update alway return max
	 * if (rotation) if (cycle % 2 == 0): min -> max, else: max -> min
	 */
	protected float min, max, dif;
	protected int lenght, now = -1, cycles;
	protected boolean loop, rotation;
	
	public StdCycle setMin(float min) {
		this.min = min;
		updateDif();
		return this;
	}
	
	public float getMin() {
		return min;
	}
	
	public StdCycle setMax(float max) {
		this.max = max;
		updateDif();
		return this;
	}
	
	public float getMax() {
		return max;
	}
	
	protected void updateDif() {
		dif = max - min;
	}
	
	public float getDif() {
		return dif;
	}
	
	public StdCycle setLenght(int lenght) {
		this.lenght = lenght;
		return this;
	}
	
	public int getLenght() {
		return lenght;
	}
	
	public StdCycle setNow(int now) {
		this.now = now;
		return this;
	}
	
	public int getNow() {
		return now;
	}
	
	public StdCycle setCycles(int cycles) {
		this.cycles = cycles;
		return this;
	}
	
	public int getCycles() {
		return cycles;
	}
	
	public StdCycle setLoop(boolean loop) {
		this.loop = loop;
		return this;
	}
	
	public boolean isLoop() {
		return loop;
	}
	
	public StdCycle setRotation(boolean rotation) {
		this.rotation = rotation;
		return this;
	}
	
	public boolean isRotation() {
		return rotation;
	}
	
	public boolean hasNext() {
		return loop || cycles == 0;
	}
	
	@Override
	public float next() {
		return update(++now);
	}
	
	@Override
	public float update(int tick) {
		if (!hasNext())
			return max;
		now = tick;
		while (lenght > 0 && now >= lenght) {
			now -= lenght;
			cycles++;
		}
		return rotation && cycles % 2 == 1 ? min + dif * now / lenght : max - dif * now / lenght;
	}
	
	@Override
	public ICycle copy() {
		return new StdCycle()
				.setMin(getMin())
				.setMax(getMax())
				.setCycles(getCycles())
				.setLenght(getLenght())
				.setNow(getNow())
				.setLoop(isLoop())
				.setRotation(isRotation());
	}

}