package com.min01.tickrateapi.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.min01.tickrateapi.util.CustomTimer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.AABB;
import com.min01.tickrateapi.TickrateAPI;

public class TickrateSavedData extends SavedData
{
	public static final String NAME = "tickrate_data";
	
	private CustomTimer currentTimer = new CustomTimer(20.0F, 0L);
	
	private final List<Pair<AABB, Float>> areas = new ArrayList<>();
	
    public static TickrateSavedData get(ResourceKey<Level> dimension)
    {
    	MinecraftServer server = TickrateAPI.getCurrentServer();
    	if(server != null)
    	{
        	ServerLevel serverLevel = server.getLevel(dimension);
            if(serverLevel != null) 
            {
                DimensionDataStorage storage = serverLevel.getDataStorage();
                TickrateSavedData data = storage.computeIfAbsent(TickrateSavedData::load, TickrateSavedData::new, NAME);
                return data;
            }
    	}
        return null;
    }

    public static TickrateSavedData load(CompoundTag nbt) 
    {
    	TickrateSavedData data = new TickrateSavedData();
    	data.currentTimer.setTickrate(nbt.getFloat("DimensionTickrate"));
    	ListTag areas = nbt.getList("Areas", 10);
		for(int i = 0; i < areas.size(); ++i)
		{
			CompoundTag tag = areas.getCompound(i);
			data.addTickrateArea(new AABB(tag.getDouble("MinX"), tag.getDouble("MinY"), tag.getDouble("MinZ"), tag.getDouble("MaxX"), tag.getDouble("MaxY"), tag.getDouble("MaxZ")), tag.getFloat("AreaTickrate"));
		}
        return data;
    }
	
	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		ListTag areas = new ListTag();
		this.areas.forEach(t -> 
		{
			CompoundTag tag = new CompoundTag();
			AABB aabb = t.getLeft();
			tag.putDouble("MinX", aabb.minX);
			tag.putDouble("MinY", aabb.minY);
			tag.putDouble("MinZ", aabb.minZ);
			tag.putDouble("MaxX", aabb.maxX);
			tag.putDouble("MaxY", aabb.maxY);
			tag.putDouble("MaxZ", aabb.maxZ);
			tag.putFloat("AreaTickrate", t.getRight());
			areas.add(tag);
		});
		nbt.putFloat("DimensionTickrate", this.currentTimer.tickrate);
		nbt.put("Areas", areas);
		return nbt;
	}
	
	public void setTickrate(float tickrate)
	{
		this.currentTimer.setTickrate(tickrate);
		this.setDirty();
	}
	
	public CustomTimer getTimer()
	{
		return this.currentTimer;
	}
	
	public void addTickrateArea(AABB aabb, float tickrate)
	{
		if(tickrate == 20)
		{
			for(Iterator<Pair<AABB, Float>> itr = this.areas.iterator(); itr.hasNext();)
			{
				Pair<AABB, Float> next = itr.next();
				if(next.getLeft().equals(aabb))
				{
					itr.remove();
				}
			}
			this.setDirty();
		}
		else
		{
			Pair<AABB, Float> pair = Pair.of(aabb, tickrate);
			this.areas.add(pair);
			this.setDirty();
		}
	}
	
	public List<Pair<AABB, Float>> getTickrateAreas()
	{
		return this.areas;
	}
}
