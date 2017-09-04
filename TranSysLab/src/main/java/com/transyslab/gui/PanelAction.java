package com.transyslab.gui;

import com.transyslab.roadnetwork.NetworkObject;

/**
 * Created by yali on 2017/9/3.
 */
public interface PanelAction {
	void resetTxtComponents();
	void writeTxtComponents(NetworkObject object);
}
