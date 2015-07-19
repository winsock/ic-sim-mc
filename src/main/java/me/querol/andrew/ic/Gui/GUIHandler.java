package me.querol.andrew.ic.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by winsock on 7/19/15.
 */
public class GUIHandler implements IGuiHandler {
	private final Map<Integer, IGui> registeredGuiDictionary = new HashMap<Integer, IGui>();
	private static GUIHandler INSTANCE;

	static {
		GUIHandler.getInstance().registerGui(new CircuitGUI());
	}

	public static GUIHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new GUIHandler();
		}
		return INSTANCE;
	}

	public void registerGui(IGui gui) {
		registeredGuiDictionary.put(gui.getId(), gui);
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return registeredGuiDictionary.get(ID) != null ? registeredGuiDictionary.get(ID).getServerContainer(player, world, x, y, z) : null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return registeredGuiDictionary.get(ID) != null ? registeredGuiDictionary.get(ID).getClientGuiScreen(player, world, x, y, z) : null;
	}
}
