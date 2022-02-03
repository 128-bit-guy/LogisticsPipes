package logisticspipes.network.packets.block;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

@StaticResolve
public class SortAllPacket extends CoordinatesPacket {
    public SortAllPacket(int id) {
        super(id);
    }

    @Override
    public void processPacket(EntityPlayer player) {
        TileEntity table = this.getTileAs(player.getEntityWorld(), TileEntity.class);
        if (table instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) table).pipe instanceof PipeBlockRequestTable) {
            ((PipeBlockRequestTable) ((LogisticsTileGenericPipe) table).pipe).beginSortAll();
        }
    }

    @Override
    public ModernPacket template() {
        return new SortAllPacket(getId());
    }
}
