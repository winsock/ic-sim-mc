package me.querol.andrew.ic.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Created by winsock on 7/19/15.
 */
public interface IGui {
	int getId();
	Object getServerContainer(EntityPlayer player, World world, int x, int y, int z);
	Object getClientGuiScreen(EntityPlayer player, World world, int x, int y, int z);
}
