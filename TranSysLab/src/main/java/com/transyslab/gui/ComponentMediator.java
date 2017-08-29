package com.transyslab.gui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yali on 2017/8/26.
 */
public class ComponentMediator implements Mediator{
    private List<Component> components;
    public ComponentMediator(){
        this.components = new ArrayList<>();
    }
    public void register(Component c){
        this.components.add(c);
    }

    @Override
    public void componetChanged(Component c) {

    }
}
