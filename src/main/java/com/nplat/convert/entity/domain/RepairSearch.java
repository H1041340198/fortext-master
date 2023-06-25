package com.nplat.convert.entity.domain;

import com.nplat.convert.utils.Utils;

import java.util.List;

public class RepairSearch {
    private WordWithPos id;
    private WordWithPos no;//S....
    private WordWithPos receptionHours;//yyyy-mm-dd
    private WordWithPos numberPlate; //鄂A。。。。
    private WordWithPos vinNo; //LVGB....
    private WordWithPos majorMaintenanceItems;
    private WordWithPos drivenDistance;

    private SecondPart secondPart;

    private List<String> secondPartMultiParts;


    public WordWithPos getId() {
        return id;
    }

    public void setId(WordWithPos id) {
        this.id = id;
    }

    public WordWithPos getNo() {
        return no;
    }

    public void setNo(WordWithPos no) {
        this.no = no;
    }

    public WordWithPos getReceptionHours() {
        return receptionHours;
    }

    public void setReceptionHours(WordWithPos receptionHours) {
        this.receptionHours = Utils.convertToTimeWordWithPos(receptionHours);
    }

    public WordWithPos getNumberPlate() {
        return numberPlate;
    }

    public void setNumberPlate(WordWithPos numberPlate) {
        this.numberPlate = numberPlate;
    }

    public WordWithPos getVinNo() {
        return vinNo;
    }

    public void setVinNo(WordWithPos vinNo) {
        this.vinNo = vinNo;
    }

    public WordWithPos getMajorMaintenanceItems() {
        return majorMaintenanceItems;
    }

    public void setMajorMaintenanceItems(WordWithPos majorMaintenanceItems) {
        this.majorMaintenanceItems = majorMaintenanceItems;
    }

    public WordWithPos getDrivenDistance() {
        return drivenDistance;
    }

    public void setDrivenDistance(WordWithPos drivenDistance) {
        this.drivenDistance = drivenDistance;
    }

    public SecondPart getSecondPart() {
        return secondPart;
    }

    public void setSecondPart(SecondPart secondPart) {
        this.secondPart = secondPart;
    }

    public List<String> getSecondPartMultiParts() {
        return secondPartMultiParts;
    }

    public void setSecondPartMultiParts(List<String> secondPartMultiParts) {
        this.secondPartMultiParts = secondPartMultiParts;
    }
}
