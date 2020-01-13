package com.Resimulators.simukraft.common.entity.sim;

import com.Resimulators.simukraft.Configs;
import com.Resimulators.simukraft.common.entity.goals.PickupItemGoal;
import com.Resimulators.simukraft.handlers.FoodStats;
import com.Resimulators.simukraft.init.ModEntities;
import com.Resimulators.simukraft.utils.Utils;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class EntitySim extends AgeableEntity implements INPC {
    private static final DataParameter<Integer> VARIATION = EntityDataManager.createKey(EntitySim.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PROFESSION = EntityDataManager.createKey(EntitySim.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> FEMALE = EntityDataManager.createKey(EntitySim.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SPECIAL = EntityDataManager.createKey(EntitySim.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEFTHANDED = EntityDataManager.createKey(EntitySim.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> HUNGER = EntityDataManager.createKey(EntitySim.class, DataSerializers.FLOAT);

    private final SimInventory inventory;

    FoodStats foodStats = new FoodStats();

    Random rand = new Random();

    public EntitySim(EntityType<? extends AgeableEntity> type, World worldIn) {
        super(ModEntities.ENTITY_SIM, worldIn);
        this.inventory = new SimInventory(this, "Items", false, 27);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(VARIATION, 0);
        this.dataManager.register(PROFESSION, 0);
        this.dataManager.register(FEMALE, false);
        this.dataManager.register(SPECIAL, false);
        this.dataManager.register(LEFTHANDED, false);
    }

    @Override
    public ILivingEntityData onInitialSpawn(IWorld world, DifficultyInstance difficultyInstance, SpawnReason spawnReason, @Nullable ILivingEntityData livingEntityData, @Nullable CompoundNBT nbt) {
        ILivingEntityData livingData = super.onInitialSpawn(world, difficultyInstance, spawnReason, livingEntityData, nbt);

        //TODO: Add configuration for special spawn chance
        this.setSpecial(Utils.randomizeBooleanWithChance(Configs.SIMS.specialSpawnChance.get()));

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
                this.setVariation(rand.nextInt(10));
            }
        }

        return livingData;
    }

    @Override
    protected void registerGoals(){
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(8, new PickupItemGoal(this));

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

    //Logic
    @Override
    public boolean canDespawn(double p_213397_1_) {
        return false;
    }

    public boolean canPickupStack(@Nonnull ItemStack stack) {
        return Utils.canInsertStack(inventory.getHandler(), stack);
    }

    //NBT Data
    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("Variation", this.getVariation());
        compound.putInt("Profession", this.getProfession());
        compound.putBoolean("Female", this.getFemale());
        compound.putBoolean("Special", this.getSpecial());
        compound.putBoolean("Lefthanded", this.getLefthanded());
        compound.put("Inventory", this.inventory.write(new ListNBT()));
        this.foodStats.write(compound);
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
        if (compound.contains("Inventory"))
            this.inventory.read(compound.getList("Inventory", 10));
        this.foodStats.read(compound);
    }

    //Interaction
    @Override
    public boolean processInteract(PlayerEntity player, Hand hand) {
        if (!player.isCrouching())
            player.openContainer(inventory);
        return super.processInteract(player, hand);
    }

    //Updates
    @Override
    public void tick() {
        super.tick();
        if (!world.isRemote()) {
            foodStats.tick(this);
        }
    }

    @Override
    public void livingTick() {
        if (this.world.getDifficulty() == Difficulty.PEACEFUL && this.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION)) {
            if (this.getHealth() < this.getMaxHealth() && this.ticksExisted % 20 == 0) {
                this.heal(1.0F);
            }

            if (this.foodStats.needFood() && this.ticksExisted % 10 == 0) {
                this.foodStats.setFoodLevel(this.foodStats.getFoodLevel() + 1);
            }
        }

        super.livingTick();
    }

    public boolean shouldHeal() {
        return this.getHealth() > 0.0F && this.getHealth() < this.getMaxHealth();
    }

    @Override
    public void onItemPickup(Entity entity, int quantity) {
        super.onItemPickup(entity, quantity);
        if (entity instanceof ItemEntity) {
            ItemStack itemStack = ((ItemEntity) entity).getItem();
            Item item = itemStack.getItem();
            if (!(item instanceof ToolItem || item instanceof SwordItem || item instanceof CrossbowItem || item instanceof BowItem)) {
                if (this.getHeldItemMainhand() == itemStack) {
                    this.inventory.addItem(itemStack);
                    this.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                } else if (this.getHeldItemOffhand() == itemStack) {
                    this.inventory.addItem(itemStack);
                    this.setHeldItem(Hand.OFF_HAND, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        this.dropInventory();
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        this.inventory.dropAllItems();
    }

    @Nullable
    public ItemEntity dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem) {
        if (droppedItem.isEmpty()) {
            return null;
        } else {
            double d0 = this.func_226280_cw_() - (double)0.3F;
            ItemEntity itementity = new ItemEntity(this.world, this.func_226277_ct_(), d0, this.func_226281_cx_(), droppedItem);
            itementity.setPickupDelay(40);
            if (traceItem) {
                itementity.setThrowerId(this.getUniqueID());
            }

            if (dropAround) {
                float f = this.rand.nextFloat() * 0.5F;
                float f1 = this.rand.nextFloat() * ((float)Math.PI * 2F);
                itementity.setMotion((double)(-MathHelper.sin(f1) * f), (double)0.2F, (double)(MathHelper.cos(f1) * f));
            } else {
                float f7 = 0.3F;
                float f8 = MathHelper.sin(this.rotationPitch * ((float)Math.PI / 180F));
                float f2 = MathHelper.cos(this.rotationPitch * ((float)Math.PI / 180F));
                float f3 = MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F));
                float f4 = MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
                float f5 = this.rand.nextFloat() * ((float)Math.PI * 2F);
                float f6 = 0.02F * this.rand.nextFloat();
                itementity.setMotion((double)(-f3 * f2 * 0.3F) + Math.cos((double)f5) * (double)f6, (double)(-f8 * 0.3F + 0.1F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), (double)(f4 * f2 * 0.3F) + Math.sin((double)f5) * (double)f6);
            }

            return itementity;
        }
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

    public void addExhaustion(float exhaustion) {
        if (!this.isInvulnerable()) {
            if (!this.world.isRemote) {
                this.foodStats.addExhaustion(exhaustion);
            }
        }
    }

    public FoodStats getFoodStats() {
        return this.foodStats;
    }

    public boolean canEat(boolean ignoreHunger) {
        return this.isInvulnerable() || ignoreHunger || this.foodStats.needFood();
    }

    public SimInventory getInventory() {
        return inventory;
    }
}