package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.hud.HUDSatellite;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.satpipe.SyncSatelliteNamePacket;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import network.rs485.logisticspipes.SatellitePipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;

public class PipeItemsSystemDestinationLogistics extends CoreRoutedPipe implements SatellitePipe, IHeadUpDisplayRendererProvider {
    public static final Set<PipeItemsSystemDestinationLogistics> AllDestinations = Collections.newSetFromMap(new WeakHashMap<>());
	public static void cleanup() {
		AllDestinations.clear();
	}
    public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
    private final HUDSatellite HUD = new HUDSatellite(this);
    @Getter
    private String satellitePipeName = "";

    public PipeItemsSystemDestinationLogistics(Item item) {
        super(item);
    }

    @Override
    public ItemSendMode getItemSendMode() {
        return ItemSendMode.Normal;
    }

    @Override
    public TextureType getCenterTexture() {
        return Textures.LOGISTICSPIPE_DESTINATION_TEXTURE;
    }

    @Override
    public LogisticsModule getLogisticsModule() {
        return null;
    }

    @Override
    public void onAllowedRemoval() {
//		dropFreqCard();
		if (MainProxy.isClient(getWorld())) {
			return;
		}
		AllDestinations.remove(this);
    }

//    private void dropFreqCard() {
//        final ItemIdentifierStack itemident = inv.getIDStackInSlot(0);
//        if (itemident == null) {
//            return;
//        }
//        EntityItem item = new EntityItem(getWorld(), getX(), getY(), getZ(), itemident.makeNormalStack());
//        getWorld().spawnEntity(item);
//        inv.clearInventorySlotContents(0);
//    }

//    @Override
//    public void onWrenchClicked(EntityPlayer entityplayer) {
//        entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Freq_Card_ID, getWorld(), getX(), getY(), getZ());
//    }

    @Override
    public void startWatching() {
        MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
    }

    @Override
    public void stopWatching() {
        MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
    }

    @Override
    public void playerStartWatching(EntityPlayer player, int mode) {
        if (mode == 1) {
            localModeWatchers.add(player);
            final ModernPacket packet = PacketHandler.getPacket(SyncSatelliteNamePacket.class).setString(satellitePipeName).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
            MainProxy.sendPacketToPlayer(packet, player);
        } else {
            super.playerStartWatching(player, mode);
        }
    }

    @Override
    public void playerStopWatching(EntityPlayer player, int mode) {
        super.playerStopWatching(player, mode);
        localModeWatchers.remove(player);
    }

    @Override
    public IHeadUpDisplayRenderer getRenderer() {
        return HUD;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        if (nbttagcompound.hasKey("satelliteid")) {
            int satelliteId = nbttagcompound.getInteger("satelliteid");
            satellitePipeName = Integer.toString(satelliteId);
        } else {
            satellitePipeName = nbttagcompound.getString("satellitePipeName");
        }
        if (MainProxy.isServer(getWorld())) {
            ensureAllSatelliteStatus();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setString("satellitePipeName", satellitePipeName);
        super.writeToNBT(nbttagcompound);
    }

    public void ensureAllSatelliteStatus() {
		if (satellitePipeName.isEmpty()) {
			AllDestinations.remove(this);
		}
		if (!satellitePipeName.isEmpty()) {
			AllDestinations.add(this);
		}
    }

    public void updateWatchers() {
        CoordinatesPacket packet = PacketHandler.getPacket(SyncSatelliteNamePacket.class).setString(satellitePipeName).setTilePos(this.getContainer());
        MainProxy.sendToPlayerList(packet, localModeWatchers);
        MainProxy.sendPacketToAllWatchingChunk(this.getContainer(), packet);
    }

    @Nonnull
    @Override
    public Set<SatellitePipe> getSatellitesOfType() {
		return Collections.unmodifiableSet(AllDestinations);
    }

    @NotNull
    @Override
    public List<ItemIdentifierStack> getItemList() {
        return Collections.emptyList();
    }

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		// Send the satellite id when opening gui
		final ModernPacket packet = PacketHandler.getPacket(SyncSatelliteNamePacket.class).setString(satellitePipeName).setPosX(getX()).setPosY(getY()).setPosZ(getZ());
		MainProxy.sendPacketToPlayer(packet, entityplayer);
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_SatellitePipe_ID, getWorld(), getX(), getY(), getZ());
	}

	@Override
	public void setSatellitePipeName(@Nonnull String satellitePipeName) {
		this.satellitePipeName = satellitePipeName;
	}
}
