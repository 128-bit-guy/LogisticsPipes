package logisticspipes.network.packets.gui;

import logisticspipes.gui.GuiSelectDestination;
import logisticspipes.gui.popup.GuiSelectSatellitePopup;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.tuples.Pair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

import java.util.List;
import java.util.UUID;

@StaticResolve
public class ProvideDestinationPipeListPacket extends ModernPacket {

	@Getter
	@Setter
	private List<Pair<String, UUID>> list;
	@Getter
	@Setter
	private UUID currentlySelected;

	public ProvideDestinationPipeListPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		list = input.readArrayList(input1 -> new Pair<>(input1.readUTF(), input1.readUUID()));
		if(input.readBoolean()) {
			currentlySelected = input.readUUID();
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeCollection(list, (output1, object) -> {
			output1.writeUTF(object.getValue1());
			output1.writeUUID(object.getValue2());
		});
		output.writeBoolean(currentlySelected != null);
		if(currentlySelected != null) {
			output.writeUUID(currentlySelected);
		}
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiSelectDestination) {
			((GuiSelectDestination) Minecraft.getMinecraft().currentScreen).handleSatelliteList(list, currentlySelected);
		}
	}

	@Override
	public ModernPacket template() {
		return new ProvideDestinationPipeListPacket(getId());
	}
}
