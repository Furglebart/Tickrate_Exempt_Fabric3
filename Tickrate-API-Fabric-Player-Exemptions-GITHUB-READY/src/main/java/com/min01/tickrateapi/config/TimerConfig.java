package com.min01.tickrateapi.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import net.fabricmc.loader.api.FabricLoader;

public class TimerConfig
{
    public static final BooleanValue disableTickrateLimit = new BooleanValue(false);

    public static void load()
    {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("tickrate-api.toml");
        if(!Files.exists(path))
        {
            try
            {
                Files.createDirectories(path.getParent());
                Files.writeString(path, "# Timer Settings\n# define whether remove tickrate limit of entities\ndisableTickrateLimit = false\n", StandardCharsets.UTF_8);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return;
        }

        try
        {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for(String line : lines)
            {
                String trimmed = line.trim();
                if(trimmed.startsWith("#") || !trimmed.contains("="))
                {
                    continue;
                }
                String[] split = trimmed.split("=", 2);
                if(split[0].trim().equals("disableTickrateLimit"))
                {
                    disableTickrateLimit.set(Boolean.parseBoolean(split[1].trim().toLowerCase(Locale.ROOT)));
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static class BooleanValue
    {
        private boolean value;

        public BooleanValue(boolean value)
        {
            this.value = value;
        }

        public boolean get()
        {
            return this.value;
        }

        public void set(boolean value)
        {
            this.value = value;
        }
    }
}
