package logisticspipes.network.packets.gui;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.BooleanCoordinatesPacket;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ExitRoute;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@StaticResolve
public class RequestDestinationPipeListPacket extends CoordinatesPacket {

	public RequestDestinationPipeListPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld(), LTGPCompletionCheck.PIPE);
		if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		CoreRoutedPipe rPipe = (CoreRoutedPipe) pipe.pipe;
		List<Pair<String, UUID>> list;
		if (rPipe.getRouter() == null || rPipe.getRouter().getRouteTable() == null) {
			return;
		}
		list = PipeItemsSystemDestinationLogistics.AllDestinations.stream()
				.filter(Objects::nonNull)
				.filter(it -> it.getRouter() != null)
				.filter(it -> {
					List<List<ExitRoute>> routingTable = rPipe.getRouter().getRouteTable();
					return routingTable.size() > it.getRouterId() && routingTable.get(it.getRouterId()) != null && !routingTable.get(it.getRouterId()).isEmpty();
				})
				.sorted(Comparator.comparingDouble(it -> rPipe.getRouter().getRouteTable().get(it.getRouterId()).stream().map(it1 -> it1.distanceToDestination).min(Double::compare).get()))
				.map(it -> new Pair<>(it.getSatellitePipeName(), it.getRouter().getId()))
				.collect(Collectors.toList());
		UUID currentlySelected = null;
		if(pipe.pipe instanceof PipeItemsSystemEntranceLogistics) {
			currentlySelected = ((PipeItemsSystemEntranceLogistics) pipe.pipe).destination;
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ProvideDestinationPipeListPacket.class).setList(list).setCurrentlySelected(currentlySelected), player);
	}

	@Override
	public ModernPacket template() {
		return new RequestDestinationPipeListPacket(getId());
	}
}
