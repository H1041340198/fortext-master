package com.nplat.convert.entity.domain;

import java.util.ArrayList;

public class SecondPart {

    private String fileName;
    private ArrayList<Maintanence> maintanence;
    private ArrayList<ComponentItem> componentItem;
    private boolean maintanenceDone = false;
    private boolean componentItemDone = false;

    public boolean isMaintanenceDone() {
        return maintanenceDone;
    }

    public void setMaintanenceDone(boolean maintanenceDone) {
        this.maintanenceDone = maintanenceDone;
    }

    public boolean isComponentItemDone() {
        return componentItemDone;
    }

    public void setComponentItemDone(boolean componentItemDone) {
        this.componentItemDone = componentItemDone;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ArrayList<Maintanence> getMaintanence() {
        return maintanence;
    }

    public void setMaintanence(ArrayList<Maintanence> maintanence) {
        this.maintanence = maintanence;
    }

    public ArrayList<ComponentItem> getComponentItem() {
        return componentItem;
    }

    public void setComponentItem(ArrayList<ComponentItem> componentItem) {
        this.componentItem = componentItem;
    }


}
