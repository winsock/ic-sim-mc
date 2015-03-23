package me.querol.andrew.ic;

import external.simulator.Simulator.CirSim;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Mod(modid = ICMod.MODID, version = ICMod.VERSION)
public class ICMod {
    public static final String MODID = "ic";
    public static final String VERSION = "DEV";
    private CirSim simulator;
    private BufferedImage interfaceBuffer;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        interfaceBuffer = new BufferedImage(480, 320, BufferedImage.TYPE_INT_ARGB);

        simulator = new CirSim(interfaceBuffer.createGraphics());
        simulator.init();

        try {
            simulator.importCircuit(new String(Files.readAllBytes(Paths.get("555int.txt")), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent serverTickEvent) {
        //System.out.println(simulator.elmList.get(32).volts[0]);
        simulator.updateCircuit();
    }
}
