package com.transyslab.gui;

import java.awt.event.ActionEvent;
import java.util.EventObject;

public class EngineEvent extends ActionEvent {

	public static final int UPDATE = 1;
	public static final int BROADCAST = 2;
	private String msg;

	public EngineEvent(Object source, int id) {
		super(source, id, "");
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
}
