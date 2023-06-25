package com.nplat.convert.entity.domain;

import java.util.List;

public class LingKeHead {
    private WordWithPos VIN;
    private WordWithPos drivenDistance;
    private WordWithPos LAD;//工单号码==服务委托书编号
    private WordWithPos receptionHours;//入场时间 yyyy-mm-dd
    private List<List<WordWithPos>> maintanence;
    private List<List<WordWithPos>> componentItem;
    private Integer isMate = 0;

    public WordWithPos getVIN() {
        return VIN;
    }

    public void setVIN(WordWithPos VIN) {
        this.VIN = VIN;
    }

    public WordWithPos getDrivenDistance() {
        return drivenDistance;
    }

    public void setDrivenDistance(WordWithPos drivenDistance) {
        this.drivenDistance = drivenDistance;
    }

    public WordWithPos getLAD() {
        return LAD;
    }

    public void setLAD(WordWithPos LAD) {
        this.LAD = LAD;
    }

    public WordWithPos getReceptionHours() {
        return receptionHours;
    }

    public void setReceptionHours(WordWithPos receptionHours) {
        this.receptionHours = receptionHours;
    }

    public List<List<WordWithPos>> getMaintanence() {
        return maintanence;
    }

    public void setMaintanence(List<List<WordWithPos>> maintanence) {
        this.maintanence = maintanence;
    }

    public List<List<WordWithPos>> getComponentItem() {
        return componentItem;
    }

    public void setComponentItem(List<List<WordWithPos>> componentItem) {
        this.componentItem = componentItem;
    }

    public Integer getIsMate() {
        return isMate;
    }

    public void setIsMate(Integer isMate) {
        this.isMate = isMate;
    }
}
