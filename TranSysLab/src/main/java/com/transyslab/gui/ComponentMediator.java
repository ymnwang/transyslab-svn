/*
 * Copyright 2019 The TranSysLab Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
