package com.Resimulators.simukraft.common.entity;

import com.Resimulators.simukraft.Configs;
import com.Resimulators.simukraft.init.ModEntities;
import com.Resimulators.simukraft.utils.Utils;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.registries.DataSerializerEntry;

import javax.annotation.Nullable;
import java.util.Random;

public class EntitySim extends AgeableEntity implements INPC {
    private static final DataParameter<Integer> VARIATION = EntityDataManager.createKey(EntitySim.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PROFESSION = EntityDataManager.createKey(EntitySim.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> FEMALE = EntityDataManager.createKey(EntitySim.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SPECIAL = EntityDataManager.createKey(EntitySim.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEFTHANDED = EntityDataManager.createKey(EntitySim.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> HUNGER = EntityDataManager.createKey(EntitySim.class, DataSerializers.FLOAT);

    Random rand = new Random();

    public EntitySim(EntityType<? extends AgeableEntity> type, World worldIn) {
        super(ModEntities.ENTITY_SIM, worldIn);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(VARIATION, 0);
        this.dataManager.register(PROFESSION, 0);
        this.dataManager.register(FEMALE, false);
        this.dataManager.register(SPECIAL, false);
        this.dataManager.register(LEFTHANDED, false);
        this.dataManager.register(HUNGER,20.0f);

    }

    @Override
    public ILivingEntityData onInitialSpawn(IWorld world, DifficultyInstance difficultyInstance, SpawnReason spawnReason, @Nullable ILivingEntityData livingEntityData, @Nullable CompoundNBT nbt) {
        ILivingEntityData livingData = super.onInitialSpawn(world, difficultyInstance, spawnReason, livingEntityData, nbt);

        //TODO: Add configuration for special spawn chance
        this.setSpecial(Utils.randomizeBooleanWithChance(20));

        //TODO: Add professions
        //this.setProfession(rand.nextInt(/*Amount of professions*/));

        this.setLefthanded(Utils.randomizeBooleanWithChance(10));

        if (this.getSpecial()) {
            String name = Configs.SIMS.specialSimNames.get().get(rand.nextInt(Configs.SIMS.specialSimNames.get().size()));
            this.setCustomName(new StringTextComponent(name));
            this.setFemale(Configs.SIMS.specialSimGenders.get().contains(name));
        } else {
            this.setFemale(Utils.randomizeBoolean());
            if (this.getFemale()) {
                //TODO: Add female name database
                this.setVariation(rand.nextInt(13));
            } else {
                //TODO: Add male name database
                this.setVariation(rand.nextInt(8));
            }
        }

        return livingData;
    }

    @Override
    protected void registerGoals(){
        this.goalSelector.addGoal(0, new SwimGoal(this));

        //Unimportant "make more alive"-goals
        this.goalSelector.addGoal(9, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(10, new LookAtGoal(this, PlayerEntity.class, 2.0f, 1.0f));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomWalkingGoal(this, 0.6d));
        this.goalSelector.addGoal(12, new LookAtGoal(this, PlayerEntity.class,8f));
        this.goalSelector.addGoal(13, new LookRandomlyGoal(this));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1);
    }

    @Nullable
    @Override
    public AgeableEntity createChild(AgeableEntity ageable) {
        EntitySim entitySim = new EntitySim(ModEntities.ENTITY_SIM, world);
        entitySim.onInitialSpawn(this.world, this.world.getDifficultyForLocation(new BlockPos(entitySim)), SpawnReason.BREEDING, new AgeableData(), null);
        return entitySim;
    }

    @Override
    public boolean canDespawn(double p_213397_1_) {
        return false;
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("Variation", this.getVariation());
        compound.putInt("Profession", this.getProfession());
        compound.putBoolean("Female", this.getFemale());
        compound.putBoolean("Special", this.getSpecial());
        compound.putBoolean("Lefthanded", this.getLefthanded());
        compound.putFloat("Hunger",this.getHunger());
    }


    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        if (compound.contains("Variation"))
            this.setVariation(compound.getInt("Variation"));
        if (compound.contains("Profession"))
            this.setProfession(compound.getInt("Profession"));
        if (compound.contains("Female"))
            this.setFemale(compound.getBoolean("Female"));
        if (compound.contains("Special"))
            this.setSpecial(compound.getBoolean("Special"));
        if (compound.contains("Lefthanded"))
            this.setLefthanded(compound.getBoolean("Lefthanded"));
    }

    //Data Manager Interaction
    public void setVariation(int variationID) {
        this.dataManager.set(VARIATION, variationID);
    }

    public int getVariation() {
        try {
            return Math.max(this.dataManager.get(VARIATION), 0);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public void setProfession(int professionID) {
        this.dataManager.set(PROFESSION, professionID);
    }

    public int getProfession() {
        try {
            return Math.max(this.dataManager.get(PROFESSION), 0);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public void setFemale(boolean female) {
        this.dataManager.set(FEMALE, female);
    }

    public boolean getFemale() {
        try {
            return this.dataManager.get(FEMALE);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void setSpecial(boolean special) {
        this.dataManager.set(SPECIAL, special);
    }

    public boolean getSpecial() {
        try {
            return this.dataManager.get(SPECIAL);
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void setLefthanded(boolean lefthanded) {
        this.dataManager.set(LEFTHANDED, lefthanded);
    }

    public boolean getLefthanded() {
        try {
            return this.dataManager.get(LEFTHANDED);
        } catch (NullPointerException e) {
            return false;
        }

    }


    public float getHunger() {
        return dataManager.get(HUNGER);
    }

    public void setHunger(float hunger){
        this.dataManager.set(HUNGER,hunger);
    }
}