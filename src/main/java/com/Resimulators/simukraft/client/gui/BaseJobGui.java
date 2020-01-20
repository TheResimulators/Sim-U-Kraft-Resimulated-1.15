package com.Resimulators.simukraft.client.gui;

import com.Resimulators.simukraft.common.entity.sim.EntitySim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;
import java.util.ArrayList;

public class BaseJobGui extends Screen {
    private Button Hire;
    private Button Fire;
    private Button ShowEmployees;
    private Button Done;
    private Button Back;
    private PlayerEntity player;
    private ArrayList<Integer> ids;
    private ArrayList<SimButton> simButtons = new ArrayList<>();
    private EntitySim selectedsim;
    private State state = State.MAIN;

    
    public BaseJobGui(ITextComponent component,ArrayList<Integer> ids) {
        super(component);
        this.player = Minecraft.getInstance().player;
        this.ids = ids;

    }

    @Override
    public void init(Minecraft minecraft, int width, int height){
        super.init(minecraft,width,height);
       addButton(Done = new Button(width-120,height-30,110,20,"Done",(Done)->{
            minecraft.displayGuiScreen(null);
        }));

        addButton(Hire = new Button (20,height-60,110,20,"Hire",(Hire->{
          ShowHiring();
          state = State.HIRE_INFO;//hire_info is used to select a sim for hiring
        })));

        addButton(Fire =new Button (20,height-30,110,20,"Fire",(Fire->{
            //TODO: FIRING COMPATIBILITY
        })));
        addButton(ShowEmployees = new Button (width-120,height-60,110,20,"Show Employees",(ShowEmployees->{
        })));
        Fire.active = false;

        addButton(Back = new Button(width-120,height-30,110,20,"Back",(Back ->{
            if (state == State.HIRE_INFO){
                state = State.MAIN;
                showMainMenu();
            }
            if (state == State.SHOW_EMPLOYEES){
                state = State.MAIN;
                showMainMenu();
            }
            if (state == State.SIM_INFO){
                state= State.HIRE_INFO;
                ShowHiring();
            }

        })));
        Back.visible = false;
        if (state == State.HIRE_INFO){
            ShowHiring();
            Hire.visible = false;
            Fire.visible = false;
            ShowEmployees.visible = false;
        }


    }

    public void showMainMenu(){
        hideAll();
        Hire.visible = true;
        Fire.visible = true;
        ShowEmployees.visible = true;
        Done.visible = true;


    }
    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground();
        super.render(p_render_1_,p_render_2_,p_render_3_);
        if (state == State.MAIN){
            //do nothing right now;
        }else if (state == State.HIRE_INFO){
            font.drawString("Hiring",(float)(width/2-font.getStringWidth("Hiring")/2),10, Color.white.getRGB());
        }

    }

    @Override
    public void renderBackground(){
        super.renderBackground();
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }

    private void ShowHiring(){
        hideAll();

        Back.visible = true;
        int x = 0;
        int y = 0;
        int ConstantXSpacing = (width/5)*2;
        int ConstantYSpacing = height/4;
        for (int i = 0;i<ids.size();i++){
            EntitySim sim = (EntitySim)player.getEntityWorld().getEntityByID(ids.get(i));
            simButtons.add(addButton(new SimButton(20 + x*ConstantXSpacing,40 + y*ConstantYSpacing,100,20,sim.getName().getFormattedText(),ids.get(i),this)));
            x++;
            if (x >4){
                x = 0;
                y++;
            }

        }

    }
    private void hideAll(){
        for(Widget button:buttons){
            button.visible = false;
        }
    }

    private void showSimInfo(int id){
        EntitySim sim = (EntitySim)player.world.getEntityByID(id);
        selectedsim = sim;
        state = State.HIRE_INFO;
    }



    class SimButton extends Button{
        private int id;
        SimButton(int widthIn, int heightIn, int width, int height, String text,int id,BaseJobGui gui) {
            super(widthIn, heightIn, width, height, text, (Sim ->{
                gui.showSimInfo(id);


            }));
            this.id = id;
        }


    }


    private enum State{
        MAIN,
        SIM_INFO,
        HIRE_INFO,
        SHOW_EMPLOYEES







    }
}


