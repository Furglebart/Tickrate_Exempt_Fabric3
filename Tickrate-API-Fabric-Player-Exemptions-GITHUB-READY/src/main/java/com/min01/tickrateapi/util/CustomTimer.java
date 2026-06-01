package com.min01.tickrateapi.util;

import net.minecraft.Util;

public class CustomTimer
{
	public float partialTick;
	public float tickDelta;
	public long lastMs;
	public float msPerTick;
	public float tickrate = 20.0F;
    
    public int advancedTime;
    
    //server side;
    public long nextTickTime = Util.getMillis();
    public long lastOverloadWarning;
    public long delayedTasksMaxNextTickTime;
    public boolean mayHaveDelayedTasks;

	public CustomTimer(float pTicksPerSecond, long pLastMs)
	{
		this.tickrate = pTicksPerSecond;
		this.msPerTick = 1000.0F / pTicksPerSecond;
		this.lastMs = pLastMs;
	}

	public int advanceTime(long pGameTime) 
	{
		this.tickDelta = (float)(pGameTime - this.lastMs) / this.msPerTick;
		this.lastMs = pGameTime;
		this.partialTick += this.tickDelta;
		int i = (int)this.partialTick;
		this.partialTick -= (float)i;
		return i;
	}
	
	public void setTickrate(float tickrate)
	{
		tickrate = Math.max(tickrate, 0.00001F);
		this.tickrate = tickrate;
		this.msPerTick = 1000.0F / tickrate;
	}
	
	public boolean haveTime()
	{
		return Util.getMillis() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTime : this.nextTickTime);
	}
}