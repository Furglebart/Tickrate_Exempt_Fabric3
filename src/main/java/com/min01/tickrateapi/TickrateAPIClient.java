package com.min01.tickrateapi;

import com.min01.tickrateapi.util.TickrateUtil;
import com.min01.tickrateapi.network.TickrateClientNetwork;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class TickrateAPIClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        TickrateClientNetwork.registerReceivers();
        ClientTickEvents.START_WORLD_TICK.register(TickrateUtil::onLevelTickStart);
        ClientTickEvents.START_CLIENT_TICK.register(client ->
        {
            Minecraft mc = Minecraft.getInstance();
            if(mc.player == null && mc.level == null)
            {
                TickrateUtil.AABB_LIST.clear();
                TickrateUtil.LEVEL_MAP.clear();
            }
        });
    }
}
