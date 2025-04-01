package dev.mryd;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.zorsh.ZorshizenCommands;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import dev.zorsh.ZorshizenListener;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Objects;

import static dev.zorsh.ZorshizenStuffKt.updateManaLoop;

public class Main extends JavaPlugin implements Listener {
    @Getter
    public static Main instance;

    @Getter
    public static BukkitTask task = null;

    @Getter
    public static ProtocolManager protocolManager;

    @Getter
    public static HashMap<String, Integer> mana = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        protocolManager = ProtocolLibrary.getProtocolManager();
        Objects.requireNonNull(getCommand("zorshizen")).setExecutor(new ZorshizenCommands());
        Objects.requireNonNull(getCommand("zorshizen")).setTabCompleter(new ZorshizenCommands());

        Bukkit.getConsoleSender().sendMessage("§e<===== §b[ Zorshizen 2 ] §e=====>");
        Bukkit.getServer().getPluginManager().registerEvents(new ZorshizenListener(), this);
        Bukkit.getConsoleSender().sendMessage("§e+> §bPlugin enabled!");
        task = updateManaLoop();
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            Bukkit.getConsoleSender().sendMessage("§aABOBAAAAAAAAAAAAAAA");
        }
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