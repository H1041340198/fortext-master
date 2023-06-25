package com.nplat.convert.entity.domain;

import java.util.ArrayList;
import java.util.HashMap;

public class SecondPartRestMaintanenceCom {

    String fileName;
    HashMap<Integer,String> maintanences;
    ArrayList<ComponentItem> componentItem;




    public HashMap<Integer, String> getMaintanences() {
        return maintanences;
    }

    public void setMaintanences(HashMap<Integer, String> maintanences) {
        this.maintanences = maintanences;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public ArrayList<ComponentItem> getComponentItem() {
        return componentItem;
    }

    public void setComponentItem(ArrayList<ComponentItem> componentItem) {
        this.componentItem = componentItem;
    }

}
