package me.querol.andrew.ic.block;

import external.simulator.Simulator.CircuitElm;
import me.querol.andrew.ic.Gui.CircuitGUI;
import me.querol.andrew.ic.ICMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by winsock on 7/19/15.
 */
public class ICBlock extends Block{
	public static String BLOCK_ID = "ICBlock";

	private ICBlock() {
		super(Material.circuits);
	}

	public static void registerBlock() {
		GameRegistry.registerBlock(new ICBlock(), BLOCK_ID);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
		FMLNetworkHandler.openGui(playerIn, ICMod.getICModInstance(), CircuitGUI.GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}
}
