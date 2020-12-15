package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.enums.BuildingType;
import com.resimulators.simukraft.common.item.ItemStructureTest;
import com.resimulators.simukraft.common.tileentity.TileCustomData;
import com.resimulators.simukraft.handlers.SimUKraftPacketHandler;
import com.resimulators.simukraft.packets.CustomDataSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;

public class GuiCustomData extends Screen {
    private BlockPos pos;
    private Button done;
    private Button calculatePrice;
    private Button calculateRent;
    private ItemStack stack;
    private TextFieldWidget rentInput;
    private TextFieldWidget priceInput;
    private ButtonScrollPanel buildingTypePanel;
    private ArrayList<Button> buildingTypes =  new ArrayList<>();
    private TileCustomData tile;
    private int buildingWidth;
    private int buildingHeight;
    private int buildingDepth;

    protected GuiCustomData(ITextComponent titleIn, BlockPos pos) {
        super(titleIn);
        this.pos = pos;
        stack = Minecraft.getInstance().player.getHeldItemMainhand();
        if ((stack.getItem() != Items.AIR)){
            //set width height and depth
        }

        tile = (TileCustomData) SimuKraft.proxy.getClientWorld().getTileEntity(pos);
        if (tile != null) {
            buildingHeight = tile.getHeight();
            buildingWidth = tile.getWidth();
            buildingDepth = tile.getDepth();

        }
    }


    @Override
    public void render(MatrixStack stack, int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground(stack); //Render Background
        buildingTypePanel.render(stack, p_render_1_, p_render_2_, p_render_3_);

        super.render(stack, p_render_1_, p_render_2_, p_render_3_);

        getMinecraft().fontRenderer.drawString(stack, "Currently Selected",width/2 - 40,height/2 - 100, Color.WHITE.getRGB());
        if (buildingTypePanel.selection.string != null) {
            minecraft.fontRenderer.drawString(stack, "Building: " + StringUtils.capitalize(buildingTypePanel.selection.string), width/2 - 40, height/2-80, Color.WHITE.getRGB());
        }
        priceInput.render(stack, p_render_1_, p_render_2_, p_render_3_);
        rentInput.render(stack, p_render_1_, p_render_2_, p_render_3_);
        rentInput.renderButton(stack, p_render_1_, p_render_2_, p_render_3_);

        if ((buildingWidth != 0 && buildingHeight != 0 && buildingDepth != 0) || rentInput != null){
            minecraft.fontRenderer.drawString(stack, ("Width: " + buildingWidth), width/2-40,height/2 - 40,Color.WHITE.getRGB());
            minecraft.fontRenderer.drawString(stack, ("Height: " + buildingHeight), width/2-40,height/2 - 20,Color.WHITE.getRGB());
            minecraft.fontRenderer.drawString(stack, ("Depth: " + buildingDepth), width/2-40,height/2,Color.WHITE.getRGB());

        }

        if (buildingTypePanel.selection.getType() != null){
            String category = buildingTypePanel.selection.getType().category.category;
            minecraft.fontRenderer.drawString(stack, ("Category: " + StringUtils.capitalize(category)),width/2-40, height/2 - 60, Color.WHITE.getRGB());
        }


    }


    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);

        addButton(done = new Button(width / 2 - 100, (height / 4) * 3 + 30, 200, 20, new StringTextComponent("Done"), done -> {
            closeScreen();
        }));

        addButton(calculatePrice = new Button(width - 120, height / 2 - 30, 100, 20, new StringTextComponent("Calculate Price"), priceCalculate -> {
            if (stack.getItem() instanceof ItemStructureTest) {
                //TODO: calculate price depending on the size of the structure

            }
        }));

        addButton(calculateRent = new Button(width - 120, (height / 2) + 50, 100, 20, new StringTextComponent("Calculate Rent"), calculateRent -> {
            if (stack.getItem() instanceof ItemStructureTest) {
                //TODO: calculate rent depending on the size of the structure
            }
        }));
        buildingTypes.clear();
        addBuildingTypeButtons();
        buildingTypePanel = new ButtonScrollPanel(minecraft,108,120,height/2 - 100,30, buildingTypes, 6,2);

        this.children.add(buildingTypePanel);

        rentInput = new TextFieldWidget(minecraft.fontRenderer,width-120,(height/2) + 80,100,20, new StringTextComponent("Rent"));
        priceInput = new TextFieldWidget(minecraft.fontRenderer, width-120,(height/2),100,20, new StringTextComponent("Price"));
        rentInput.setVisible(true);
        rentInput.setFocused2(true);
        rentInput.setMaxStringLength(6);
        rentInput.setText(String.valueOf(tile.getRent()));
        priceInput.setVisible(true);
        priceInput.setMaxStringLength(6);
        priceInput.setText(String.valueOf(tile.getPrice()));
        children.add(rentInput);
        children.add(priceInput);
        buildingTypePanel.selection.type = tile.getBuildingType();
        buildingTypePanel.selection.id = tile.getBuildingType().id;
        buildingTypePanel.selection.string = tile.getBuildingType().name;
    }




    private void addBuildingTypeButtons(){
        int i = 0;
        for (BuildingType type: BuildingType.values()){
            Button button = new Button(30,60 + (i*25), 100, 20 , new StringTextComponent(type.name),butn ->{
                buildingTypePanel.selection.id = type.id;
                buildingTypePanel.selection.string = type.name;
                buildingTypePanel.selection.type = type;
                reactivateButtons(buildingTypes);
                butn.active = false;
            });
            buildingTypes.add(button);
            addButton(button);
            i++;
        }
    }


    private void reactivateButtons(ArrayList<Button> buttons){
        for (Button button: buttons){
            button.active = true;
        }
    }

    @Override
    public void closeScreen() {
        super.closeScreen();
        UpdateServer();
    }

    private void UpdateServer() {
        try {
        tile.setRent(Float.parseFloat(rentInput.getText()));
        tile.setPrice(Float.parseFloat(priceInput.getText()));

        } catch (NumberFormatException e){
            SimuKraft.LOGGER().debug("Incorrect input" + e);
            tile.setRent(0);
            tile.setPrice(0);
        }
        tile.setBuildingType(buildingTypePanel.selection.type);
        tile.setWidth(buildingWidth);
        tile.setHeight(buildingHeight);
        tile.setDepth(buildingDepth);

        SimUKraftPacketHandler.INSTANCE.sendToServer(new CustomDataSyncPacket(tile.getPrice(), tile.getRent(), tile.getBuildingType(), pos));
    }


    private class ButtonScrollPanel extends ScrollPanel {

        private ArrayList<Button> buttons;
        private int maxButtonsVisible;
        private Selection selection;
        private int contentOffset = 0;
        public ButtonScrollPanel(Minecraft client, int width, int height, int top, int left, ArrayList<Button> buttons, int maxButtonsVisible, int contentOffset) {
            super(client, width, height, top, left);
            children.addAll(buttons);
            this.buttons = buttons;
            this.maxButtonsVisible = maxButtonsVisible;
            updateButtonPlacement();
            selection = new Selection(this);
            this.contentOffset = contentOffset;
        }

        @Override
        protected int getContentHeight() {
            return maxButtonsVisible * 21 + contentOffset;
        }

        @Override
        protected void drawPanel(MatrixStack mStack, int entryRight, int relativeY, Tessellator tess, int mouseX, int mouseY) {

        }


        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
            if (super.mouseScrolled(mouseX, mouseY, scroll)){
                updateButtonPlacement();
                return true;

            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)){
               updateButtonPlacement();
               return true;
            }
            return false;
        }

        @Override
        protected int getScrollAmount() {
            return (int) Math.ceil((double)this.buttons.size()/(double)maxButtonsVisible);
        }



        private void updateButtonPlacement(){
            int i = 0;
            for (Button button: buttons) {
                int index = (((int) this.scrollDistance)/getScrollAmount());
                button.y = ((this.top + i*30) - index* 30);
                i++;
                button.visible = disableOuterButton(button);
            }
        }

        private boolean disableOuterButton(Button button){
            return button.y >= this.top && button.y+ button.getHeightRealms() <= this.bottom;
        }
    }

    private class Selection{
        private String string;
        private int id;
        private BuildingType type;

        Selection(ButtonScrollPanel panel){
            string = panel.buttons.get(0).getMessage().getString();
            BuildingType type = tile.getBuildingType();
            this.type = type;
            if (type != null){
            id = type.id;
            }
        }

        public String getString(){
            return string;
        }

        public int getId(){
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setString(String string) {
            this.string = string;
        }

        public BuildingType getType(){ return type;}
    }
}

