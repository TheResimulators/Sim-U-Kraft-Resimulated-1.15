package com.Resimulators.simukraft.packets;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.tileentity.ITile;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.init.ModJobs;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class SimHireRequest implements IMessage {

    private UUID playerId;
    private int simId;
    private BlockPos pos;
    private String job;
    public SimHireRequest(){}

    public SimHireRequest(int simId, UUID playerId, BlockPos pos,String job){
        this.pos = pos;
        this.playerId = playerId;
        this.simId = simId;
        this.job = job;
    }
    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(simId);
        buf.writeUniqueId(playerId);
        buf.writeString(job);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.simId = buf.readInt();
        this.playerId = buf.readUniqueId();
        this.job = buf.readString();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        if (ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(playerId) != null){
            PlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(playerId);
            SavedWorldData data = SavedWorldData.get(player.world);
            int id = data.getFactionWithPlayer(player.getUniqueID()).getId();
            data.hireSim(id,(EntitySim) player.world.getEntityByID(simId));
            ((ITile)player.world.getTileEntity(pos)).setHired(true);
            EntitySim sim =  ((EntitySim) player.world.getEntityByID(simId));
            ((ITile)player.world.getTileEntity(pos)).setSimId(sim.getUniqueID());
            ((EntitySim) player.world.getEntityByID(simId)).setJob(ModJobs.JOB_LOOKUP.get(job).apply(sim));
            data.getFaction(id).sendPacketToFaction(new SimHirePacket(simId,id,pos,job));
        }

    }
}
