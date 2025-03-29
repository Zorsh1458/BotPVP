package dev.mryd;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.zorsh.ZorshizenCommands;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import dev.zorsh.ZorshizenListener;

import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    @Getter
    public static Main instance;

    @Getter
    public static ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        instance = this;

        protocolManager = ProtocolLibrary.getProtocolManager();
        Objects.requireNonNull(getCommand("zorshizen")).setExecutor(new ZorshizenCommands());
        Objects.requireNonNull(getCommand("zorshizen")).setTabCompleter(new ZorshizenCommands());

        Bukkit.getConsoleSender().sendMessage("§e<===== §b[ Zorshizen 2 ] §e=====>");
        Bukkit.getServer().getPluginManager().registerEvents(new ZorshizenListener(), this);
        Bukkit.getConsoleSender().sendMessage("§e+> §bPlugin enabled!");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§cHyiillaa");
//        RTSManager.clearData();
//        NPCManager.clearNpcs();
    }

//    @Override
//    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
//        getLogger().info("[Zet Corporation] ZChunks is used!");
//        return new ZChunkGenerator();
//    }
}