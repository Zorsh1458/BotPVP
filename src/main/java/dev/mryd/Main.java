package dev.mryd;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.zorsh.commands.BotPVPCommands;
import dev.zorsh.engine.BotPVPManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import dev.zorsh.listeners.BotPVPListener;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Objects;

import static dev.zorsh.engine.ZorshizenStuffKt.updateManaLoop;

public class Main extends JavaPlugin implements Listener {
    @Getter
    public static Main instance;

    @Getter
    public static ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        instance = this;

        protocolManager = ProtocolLibrary.getProtocolManager();
        Objects.requireNonNull(getCommand("bot")).setExecutor(new BotPVPCommands());
        Objects.requireNonNull(getCommand("bot")).setTabCompleter(new BotPVPCommands());

        Bukkit.getConsoleSender().sendMessage("§e<===== §b[ BotPVP ] §e=====>");
        Bukkit.getServer().getPluginManager().registerEvents(new BotPVPListener(), this);
        Bukkit.getConsoleSender().sendMessage("§e+> §bPlugin enabled!");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§cHyiillaa");
        BotPVPManager.clearPvpBots();

//        RTSManager.clearData();
//        NPCManager.clearNpcs();
    }

//    @Override
//    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
//        getLogger().info("[Zet Corporation] ZChunks is used!");
//        return new ZChunkGenerator();
//    }
}