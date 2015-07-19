package me.querol.andrew.ic.Gui;

import external.simulator.Simulator.CirSim;
import me.querol.andrew.ic.block.ICBlock;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CircuitGUI implements IGui {
	public static final int GUI_ID = 0;

	@Override
	public int getId() {
		return GUI_ID;
	}

	@Override
	public Object getServerContainer(EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiScreen(EntityPlayer player, World world, int x, int y, int z) {
		if (world.getBlockState(new BlockPos(x, y ,z)).getBlock() instanceof ICBlock) {
			try {
				ClientCircuitGui gui = new ClientCircuitGui(new CirSim(new String(Files.readAllBytes(Paths.get("555int.txt")), StandardCharsets.UTF_8), 500, 500));
				gui.initGui();
				return gui;
			} catch (Exception e) { e.printStackTrace(); }
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	public class ClientCircuitGui extends GuiScreen {
		/** The location of the inventory background texture */
		protected final ResourceLocation blankBackground = new ResourceLocation("textures/gui/demo_background.png");
		/** The X size of the inventory window in pixels. */
		protected int xSize = 600;
		/** The Y size of the inventory window in pixels. */
		protected int ySize = 600;
		/** Starting X position for the Gui. Inconsistent use for Gui backgrounds. */
		protected int guiLeft;
		/** Starting Y position for the Gui. Inconsistent use for Gui backgrounds. */
		protected int guiTop;

		private final CirSim circuit;

		public ClientCircuitGui(CirSim circuit) {
			this.circuit = circuit;
		}

		public double getZLevel() {
			return zLevel;
		}

		@Override
		public void initGui() {
			super.initGui();
			this.guiLeft = (this.width - this.xSize) / 2;
			this.guiTop = (this.height - this.ySize) / 2;
		}

		/**
		 * Draws a gradient over the background screen plus rectangle
		 */
		@Override
		public void drawDefaultBackground() {
			super.drawDefaultBackground();
			this.mc.getTextureManager().bindTexture(blankBackground);
			Gui.drawScaledCustomSizeModalRect(this.guiLeft, this.guiTop, 0f, 0f, 1, 1, xSize, ySize, 1f, 1f);
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			super.drawScreen(mouseX, mouseY, partialTicks);

			// Draw the circuit diagram
			circuit.draw(this, mouseX, mouseY, partialTicks);
		}

		public int getXSize() {
			return xSize;
		}

		public int getYSize() {
			return ySize;
		}

		public int getGuiLeft() {
			return guiLeft;
		}

		public int getGuiTop() {
			return guiTop;
		}
	}
}