package logisticspipes.network.packets.pipe;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

import java.util.UUID;

@StaticResolve
public class SetDestinationPacket extends CoordinatesPacket {

    @Setter
    @Getter
    private UUID destination;
    public SetDestinationPacket(int id) {
        super(id);
    }

    @Override
    public void processPacket(EntityPlayer player) {
        TileEntity pipe = getTileAs(player.world, TileEntity.class);
        if(pipe instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) pipe).pipe instanceof PipeItemsSystemEntranceLogistics) {
            ((PipeItemsSystemEntranceLogistics) ((LogisticsTileGenericPipe) pipe).pipe).destination = destination;
        }
    }

    @Override
    public void writeData(LPDataOutput output) {
        super.writeData(output);
        output.writeUUID(destination);
    }

    @Override
    public void readData(LPDataInput input) {
        super.readData(input);
        destination = input.readUUID();
    }

    @Override
    public ModernPacket template() {
        return new SetDestinationPacket(getId());
    }
}
