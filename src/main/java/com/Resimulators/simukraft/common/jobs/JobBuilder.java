package com.Resimulators.simukraft.common.jobs;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.Resimulators.simukraft.common.jobs.core.EnumJobState;
import com.Resimulators.simukraft.common.jobs.core.IJob;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

public class JobBuilder implements IJob {
    private EntitySim sim;
    private Goal goal1;
    private int periodsworked = 0;

    public JobBuilder(EntitySim sim) {
        this.sim = sim;

    }

    @Override
    public EnumJobState state() {
        return EnumJobState.NOT_WORKING;
    }

    @Override
    public int intervalTime() {
        return 1000;
    }

    @Override
    public int workTime() {
        return 12000;
    }

    @Override
    public int maximumWorkPeriods() {
        return -1;
        //negative one so that it can work as much as it can. builder should work all day.
        // if it can't find resources it take a 1000 tick break
    }

    @Override
    public boolean nightShift() {
        return false;
    }

    @Override
    public int getPeriodsWorked() {
        return periodsworked;
    }

    @Override
    public void addJobAi() {
        sim.goalSelector.addGoal(4, goal1);
    }

    @Override
    public void removeJobAi() {
        sim.goalSelector.removeGoal(goal1);
    }

    @Override
    public ListNBT writeToNbt(ListNBT nbt) {
        CompoundNBT name = new CompoundNBT();
        name.putString("Builder", "Builder");
        nbt.add(name);
        CompoundNBT ints = new CompoundNBT();
        ints.putInt("periodsworked", periodsworked);
        nbt.add(ints);

        return nbt;
    }

    @Override
    public void readFromNbt(ListNBT nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            CompoundNBT list = nbt.getCompound(i);
            if (list.contains("periodsworked")) {
                periodsworked = list.getInt("periodsworked");
            }
        }
    }

    @Override
    public void finishedWorkPeriod() {
        setWorkedPeriods(++periodsworked);
    }

    @Override
    public void setWorkedPeriods(int periods) {
        periodsworked = periods;
    }

    @Override
    public void resetPeriodsWorked() {
        setWorkedPeriods(0);
    }


}
