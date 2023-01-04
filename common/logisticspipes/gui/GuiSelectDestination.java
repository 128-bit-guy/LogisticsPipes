package logisticspipes.gui;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.RequestDestinationPipeListPacket;
import logisticspipes.network.packets.gui.RequestSatellitePipeListPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.*;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.BlockPos;
import network.rs485.logisticspipes.util.TextUtil;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class GuiSelectDestination extends LogisticsBaseGuiScreen {

	String GUI_LANG_KEY = "gui.popup.selectdestination.";

	private final Consumer<UUID> handleResult;
	private List<Pair<String, UUID>> pipeList = Collections.EMPTY_LIST;
	private UUID currentlySelected;
	private final TextListDisplay textList;

	public GuiSelectDestination(BlockPos pos, boolean fluidSatellites, Consumer<UUID> handleResult) {
		super(150, 170, 0, 0);
		this.handleResult = handleResult;
		this.textList = new TextListDisplay(this, 6, 16, 6, 30, 12, new TextListDisplay.List() {

			@Override
			public int getSize() {
				return pipeList.size();
			}

			@Override
			public String getTextAt(int index) {
				return pipeList.get(index).getValue1();
			}

			@Override
			public int getTextColor(int index) {
				if(Objects.equals(pipeList.get(index).getValue2(), currentlySelected)) {
					return 16777120;
				} else {
					return 0xFFFFFF;
				}
			}
		});
		MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestDestinationPipeListPacket.class).setBlockPos(pos));
	}

//	@Override
//	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//		RenderHelper.disableStandardItemLighting();
//		renderGuiBackground(mouseX, mouseY);
//		GlStateManager.disableRescaleNormal();
//		GlStateManager.disableLighting();
//		GlStateManager.disableDepth();
//		super.drawScreen(mouseX, mouseY, partialTicks);
//		RenderHelper.enableGUIStandardItemLighting();
//
//		GlStateManager.pushMatrix();
//		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//		GlStateManager.enableRescaleNormal();
//		short short1 = 240;
//		short short2 = 240;
//		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) short1 / 1.0F, (float) short2 / 1.0F);
//		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//
//		this.drawGuiContainerForegroundLayer(mouseX, mouseY);
//
//		GlStateManager.popMatrix();
//
//		GlStateManager.enableLighting();
//		GlStateManager.enableDepth();
//		if (subGui != null) {
//			GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT);
//			if (!subGui.hasSubGui()) {
//				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
//				super.drawDefaultBackground();
//			}
//			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
//			subGui.drawScreen(mouseX, mouseY, partialTicks);
//			GL11.glPopAttrib();
//		}
//	}

	protected void drawTitle() {
		mc.fontRenderer.drawStringWithShadow(TextUtil.translate(GUI_LANG_KEY + "title"), xCenter - (mc.fontRenderer.getStringWidth(TextUtil.translate(GUI_LANG_KEY + "title")) / 2f), guiTop + 6, 0xFFFFFF);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, xCenter + 16, bottom - 27, 50, 10, TextUtil.translate(GUI_LANG_KEY + "select")));
		buttonList.add(new SmallGuiButton(1, xCenter + 16, bottom - 15, 50, 10, TextUtil.translate(GUI_LANG_KEY + "exit")));
		buttonList.add(new SmallGuiButton(2, xCenter - 66, bottom - 27, 50, 10, TextUtil.translate(GUI_LANG_KEY + "unset")));
		buttonList.add(new SmallGuiButton(4, xCenter - 12, bottom - 27, 25, 10, "/\\"));
		buttonList.add(new SmallGuiButton(5, xCenter - 12, bottom - 15, 25, 10, "\\/"));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);
		renderGuiBackground(i, j);
	}

	protected void renderGuiBackground(int mouseX, int mouseY) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		drawTitle();

		textList.renderGuiBackground(mouseX, mouseY);
	}

	@Override
	protected void mouseClicked(int i, int j, int k) throws IOException {
		textList.mouseClicked(i, j, k);
		super.mouseClicked(i, j, k);
	}

	@Override
	public void handleMouseInputSub() throws IOException {
		int wheel = org.lwjgl.input.Mouse.getDWheel() / 120;
		if (wheel == 0) {
			super.handleMouseInputSub();
		}
		if (wheel < 0) {
			textList.scrollUp();
		} else if (wheel > 0) {
			textList.scrollDown();
		}
	}
	
	void exitGui() {
		mc.displayGuiScreen(null);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 0) { // Select
			int selected = textList.getSelected();
			if (selected >= 0) {
				handleResult.accept(pipeList.get(selected).getValue2());
				exitGui();
			}
		} else if (guibutton.id == 1) { // Exit
			exitGui();
		} else if (guibutton.id == 2) { // UnSet
			handleResult.accept(null);
			exitGui();
		} else if (guibutton.id == 4) {
			textList.scrollDown();
		} else if (guibutton.id == 5) {
			textList.scrollUp();
		} else {
			super.actionPerformed(guibutton);
		}
	}

	public void handleSatelliteList(List<Pair<String, UUID>> list, UUID currentlySelected) {
		pipeList = list;
		this.currentlySelected = currentlySelected;
	}
}
