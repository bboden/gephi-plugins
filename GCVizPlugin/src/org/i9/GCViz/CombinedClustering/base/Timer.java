package org.i9.GCViz.CombinedClustering.base;


public class Timer {

	
	private long mLastTime;

	
	private long mCounter;

	
	private boolean mCounting;

	
	public Timer() {
		mCounting = false;
	}

	
	public void start() {
		mLastTime = System.currentTimeMillis();
		mCounting = true;
	}

	
	public long getTime() {
		if (mCounting) {
			long actualTime = System.currentTimeMillis();
			mCounter += actualTime - mLastTime;
			mLastTime = actualTime;
		}

		return mCounter;
	}

	@Override
	public String toString() {
		long time = stop();
		long msecs = time;
		return "time: "+ msecs;
	}

	
	public long stop() {
		long counter = getTime();
		mCounting = false;
		mCounter = 0;
		return counter;
	}

	
	public void pause() {
		long actualTime = System.currentTimeMillis();
		mCounter += actualTime - mLastTime;
		mLastTime = actualTime;
		mCounting = false;
	}
}

