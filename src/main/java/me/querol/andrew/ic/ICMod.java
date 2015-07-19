package me.querol.andrew.ic;

import me.querol.andrew.ic.Gui.GUIHandler;
import me.querol.andrew.ic.block.ICBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.InstanceFactory;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = ICMod.MODID, version = ICMod.VERSION)
public class ICMod {
    public static final String MODID = "ic";
    public static final String VERSION = "DEV";
	@Instance
	public static ICMod INSTANCE = null;

	private ICMod() {

	}

	@InstanceFactory
	public static ICMod getICModInstance() {
		if (ICMod.INSTANCE == null) {
			ICMod.INSTANCE = new ICMod();
		}
		return ICMod.INSTANCE;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ICBlock.registerBlock();
	}
    @EventHandler
    public void init(FMLInitializationEvent event) {
	    NetworkRegistry.INSTANCE.registerGuiHandler(this, GUIHandler.getInstance());
    }
}
