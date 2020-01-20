package com.Resimulators.simukraft.init;


import com.Resimulators.simukraft.common.world.Faction;
import com.Resimulators.simukraft.common.world.SavedWorldData;
import com.Resimulators.simukraft.handlers.SimUKraftPacketHandler;
import com.Resimulators.simukraft.packets.SyncPlayerCapability;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkDirection;

public class FactionEvents {

    @SubscribeEvent
    public void PlayerJoinEvent(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getPlayer() != null){
            World world = event.getPlayer().world;
            SavedWorldData data = SavedWorldData.get(world);
            Faction faction = data.getFactionWithPlayer(event.getPlayer().getUniqueID());
            if (faction == null){
                faction = data.createNewFaction();
                data.addPlayerToFaction(faction.getId(),event.getPlayer());
            }
            SimUKraftPacketHandler.INSTANCE.sendTo(new SyncPlayerCapability(faction.write(new CompoundNBT()),faction.getId()),((ServerPlayerEntity) event.getPlayer()).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);

        }
    }

}

