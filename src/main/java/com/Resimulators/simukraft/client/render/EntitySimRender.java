package com.Resimulators.simukraft.client.render;

import com.Resimulators.simukraft.Reference;
import com.Resimulators.simukraft.client.data.SkinCacher;
import com.Resimulators.simukraft.client.model.EntitySimModel;
import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

import java.awt.*;
import java.text.Format;

import static net.minecraft.client.renderer.RenderType.func_228659_m_;

public class EntitySimRender extends LivingRenderer<EntitySim, EntitySimModel> {
    String DIR = "textures/entity/sim/";

    public EntitySimRender(EntityRendererManager manager) {
        super(manager, new EntitySimModel(0.0f), 0.5f);
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel(0.5f), new BipedModel(1.0f)));
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new HeadLayer<>(this));
    }

    @Override
    public void func_225623_a_(@Nonnull EntitySim entitySim, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        this.setModelVisibilities(entitySim);
        float f2 = 1.6F;
        float f3 = 0.01666667F * f2;
        float f6 = 0.2F;
        int d1 = entitySim.getPosition().getX();
        int d = entitySim.getPosition().getY();
        int d2 = entitySim.getPosition().getZ();
        //displayText("test 1" + "(10)", 0.8F, 0xFFFFFFFF, (float) d, (float) d1 + f3 + f6 - 0.4f, (float) d2, entitySim);
        //displayText("test 2", 0.7F, 0xFFFFFF00, (float) d, (float) d1 + f3 + f6 - 0.7f, (float) d2, entitySim);
        //displayText("test 3", 0.7F, 0xFFFFFF00, (float) d, (float) d1 + f3 + f6 - 1.0f, (float) d2, entitySim);
        if (entitySim.getCustomName() != null){
        func_225629_a_(entitySim,entitySim.getCustomName().getFormattedText(),matrix,renderer,1,1.1f);
        }
        func_225629_a_(entitySim,"excited for new release",matrix,renderer,1,0.8f);
        func_225629_a_(entitySim, new StringTextComponent("Full of Food").applyTextStyle(TextFormatting.WHITE).getFormattedText(),matrix,renderer,1,0.5f);

        this.canRenderName(entitySim);
        super.func_225623_a_(entitySim, entityYaw, partialTick, matrix, renderer, light);
    }


    private void setModelVisibilities(EntitySim entitySim) {
        EntitySimModel model = this.getEntityModel();
        ItemStack itemStack = entitySim.getHeldItemMainhand();
        ItemStack itemStack1 = entitySim.getHeldItemOffhand();
        model.setVisible(true, entitySim.getFemale());
        model.field_228270_o_ = entitySim.isCrouching();
        BipedModel.ArmPose bipedmodel$armpose = this.getArmpose(entitySim, itemStack, itemStack1, Hand.MAIN_HAND);
        BipedModel.ArmPose bipedmodel$armpose1 = this.getArmpose(entitySim, itemStack, itemStack1, Hand.OFF_HAND);
        if (entitySim.getPrimaryHand() == HandSide.RIGHT) {
            model.rightArmPose = bipedmodel$armpose;
            model.leftArmPose = bipedmodel$armpose1;
        } else {
            model.rightArmPose = bipedmodel$armpose;
            model.leftArmPose = bipedmodel$armpose1;
        }
    }
    @Override
    protected boolean canRenderName(EntitySim entity){
        return false;
    }

    private BipedModel.ArmPose getArmpose(EntitySim entitySim, ItemStack primary, ItemStack secondary, Hand hand) {
        BipedModel.ArmPose bipedmodel$armpose = BipedModel.ArmPose.EMPTY;
        ItemStack itemStack = hand == Hand.MAIN_HAND ? primary : secondary;
        if (!itemStack.isEmpty()) {
            bipedmodel$armpose = BipedModel.ArmPose.ITEM;
            if (entitySim.getItemInUseCount() > 0) {
                UseAction useAction = itemStack.getUseAction();
                if (useAction == UseAction.BLOCK) {
                    bipedmodel$armpose = BipedModel.ArmPose.BLOCK;
                } else if (useAction == UseAction.BOW) {
                    bipedmodel$armpose = BipedModel.ArmPose.BOW_AND_ARROW;
                } else if (useAction == UseAction.SPEAR) {
                    bipedmodel$armpose = BipedModel.ArmPose.THROW_SPEAR;
                } else if (useAction == UseAction.CROSSBOW && hand == entitySim.getActiveHand()) {
                    bipedmodel$armpose = BipedModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else {
                boolean flag = primary.getItem() == Items.CROSSBOW;
                boolean flag1 = CrossbowItem.isCharged(primary);
                boolean flag2 = secondary.getItem() == Items.CROSSBOW;
                boolean flag3 = CrossbowItem.isCharged(secondary);
                if (flag && flag1) {
                    bipedmodel$armpose = BipedModel.ArmPose.CROSSBOW_HOLD;
                }
                if (flag2 && flag3 && primary.getItem().getUseAction(primary) == UseAction.NONE) {
                    bipedmodel$armpose = BipedModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }
        return bipedmodel$armpose;
    }

    @Override
    public ResourceLocation getEntityTexture(EntitySim entitySim) {
        ResourceLocation location = SkinCacher.getSkinForSim(entitySim.getName().getFormattedText());
        if (location == null || !entitySim.getSpecial())
            location = new ResourceLocation(Reference.MODID, DIR + (entitySim.getFemale() ? "female/" : "male/") + "entity_sim" + entitySim.getVariation() + ".png");
        return location;
    }
    private void func_225629_a_(EntitySim sim, String string, MatrixStack matric, IRenderTypeBuffer renderBuffer, int num,float height) {
        double d0 = this.renderManager.func_229099_b_(sim);
        if (!(d0 > 600d)) {
            boolean flag = !sim.func_226273_bm_();
            float f = sim.getHeight() + height+0.2f;
            int i = "deadmau5".equals(string) ? -10 : 0;
            matric.func_227860_a_();
            matric.func_227861_a_(0.0D, (double)f, 0.0D);
            matric.func_227863_a_(this.renderManager.func_229098_b_());
            matric.func_227862_a_(-0.025F, -0.025F, 0.025F);
            Matrix4f matrix4f = matric.func_227866_c_().func_227870_a_();
            float f1 = Minecraft.getInstance().gameSettings.func_216840_a(0.25f);
            int j = (int)(f1 * 255.0F) << 24;

            FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
            float f2 = (float)(-fontrenderer.getStringWidth(string) / 2);
            //
            fontrenderer.func_228079_a_(string, f2, (float)i, 8421504, false, matrix4f, renderBuffer, flag, j, num);
            if (flag) {
               fontrenderer.func_228079_a_(string, f2, (float)i, 8421504, false, matrix4f, renderBuffer, false, 0, num);
                fontrenderer.drawString(string,f2,i,8421504);
            }

            matric.func_227865_b_();
        }
    }
}



