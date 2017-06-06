/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qmaks.cmdlimiter;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.ListenerPriority;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 *
 * @author Qmaks
 */
public class Main extends JavaPlugin implements Listener {

    private static FileConfiguration cfg;

    @Override
    public void onEnable() {
        cfg = getConfig();
        cfg.options().copyDefaults(true);
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);

        ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener) new PacketAdapter(this, ListenerPriority.NORMAL, new PacketType[]{PacketType.Play.Client.TAB_COMPLETE}) {
            @Override
            public void onPacketReceiving(final PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE && !event.getPlayer().hasPermission("cmdblocker.bypass")) {
                    try {
                        PacketContainer packet = event.getPacket();
                        String command = ((String) packet.getSpecificModifier((Class) String.class).read(0)).replaceAll("\\s+", " ").toLowerCase().split(" ")[0];
                        if (!cfg.getStringList("allowed-commands").contains(command)) {
                            event.setCancelled(true);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        if (!p.hasPermission("cmdlimiter.bypass")) {
            String command = event.getMessage().replaceAll("\\s+", " ").toLowerCase().split(" ")[0];
            if (!cfg.getStringList("allowed-commands").contains(command)) {
                for (String s : cfg.getStringList("block-msg")) {
                    p.sendMessage(s.replace("&", "§")
                            .replace("<player>", p.getName())
                            .replace("<command>", command));
                }
                event.setCancelled(true);
            }
        }
    }
}
