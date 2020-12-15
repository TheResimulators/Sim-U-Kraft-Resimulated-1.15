package com.resimulators.simukraft.packets;


import com.resimulators.simukraft.common.entity.sim.SimEntity;
import com.resimulators.simukraft.common.tileentity.ITile;
import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.init.ModJobs;
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
    private int job;
    public SimHireRequest(){}

    public SimHireRequest(int simId, UUID playerId, BlockPos pos, int job){
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
        buf.writeInt(job);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.simId = buf.readInt();
        this.playerId = buf.readUniqueId();
        this.job = buf.readInt();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide() {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer) {
        if (ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(playerId) != null) {
            PlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(playerId);
            if (player != null) {
                SavedWorldData data = SavedWorldData.get(player.world);
                int id = data.getFactionWithPlayer(player.getUniqueID()).getId();
                data.hireSim(id, (SimEntity) player.world.getEntityByID(simId));
                ((ITile) player.world.getTileEntity(pos)).setHired(true);
                SimEntity sim = ((SimEntity) player.world.getEntityByID(simId));
                if (sim != null){
                    sim.setJob(ModJobs.JOB_LOOKUP.get(job).apply(sim));
                    sim.setProfession(job);

                    ITile tile = (ITile) player.world.getTileEntity(pos);
                    if (tile != null){
                    tile.setSimId(sim.getUniqueID());

                    sim.getJob().setWorkSpace(pos);
                    data.getFaction(id).subCredits(sim.getJob().getWage() + 4);
                    data.getFaction(id).sendPacketToFaction(new SimHirePacket(simId, id, pos, job));
                    sim.getController().setTick(sim.getJob().intervalTime());
                    }
                }
            }
        }
    }
}
