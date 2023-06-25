package com.nplat.convert.service;

import com.nplat.convert.entity.domain.RepairSearch;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class FirstPage {
    @Autowired
    private StrToMatrixService strToMatrixService;


    public List<RepairSearch> toRepairSearchList(List<RepairSearch> repairSearchList, String str) {
        List<List<WordWithPos>> matrixData = dealHeaderData(str);
        int i = 0;
        if (Objects.isNull(repairSearchList)) {
            repairSearchList = new ArrayList<>();
        } else {
            i = 1;
        }
        for (; i < matrixData.size(); i++) {
            List<WordWithPos> data = matrixData.get(i);
            RepairSearch subItem = new RepairSearch();
            subItem.setId(data.get(0));//序号
            subItem.setNo(data.get(1));//工单号No
            subItem.setReceptionHours(data.get(2));
            subItem.setNumberPlate(data.get(3));
            subItem.setVinNo(data.get(4));
            subItem.setMajorMaintenanceItems(data.get(5));
            subItem.setDrivenDistance(data.get(6));
            repairSearchList.add(subItem);
        }
        return repairSearchList;
    }



    private List<List<WordWithPos>> dealHeaderData(String str) {
        List<List<WordWithPos>> matrixTmp = strToMatrixService.otherTextStringToMatrix(str);
        List<List<WordWithPos>> matrixData = new ArrayList<>();
        for (int i = 0; i < matrixTmp.size(); i++) { //循环每一行的数据
            List<WordWithPos> lineWords = matrixTmp.get(i);
            WordWithPos firstWord = lineWords.get(0);

            try {//第一个字符是数字
                Integer.valueOf(firstWord.getWord());
                if (matrixData.size() > 0) {//已经有一行数据了,且第一行数头
                    List<String> noList = matrixData.stream().map(matrixDatasub -> matrixDatasub.get(0).getWord()).collect(Collectors.toList());
                    if (!noList.contains(firstWord.getWord())) {//不在集合中
                        List<WordWithPos> head = matrixData.get(0);//拿到头
                        List<WordWithPos> dataSub = new ArrayList<>();
                        for (int inde = 0; inde < head.size(); inde++) {//遍历第下一行
                            WordWithPos tm = findElementFrom(head.get(inde), lineWords);
                            dataSub.add(inde, tm);
                        }
                        matrixData.add(dataSub);
                    }

                }
            } catch (Exception e) {
                //不是数字行
                if (matrixData.size() > 0) {//已经有头了
                    continue;
                } else if ("序号".equals(firstWord.getWord())) {//以序号开头，则创建头
                    List<WordWithPos> head = new ArrayList<>();
                    head.add(0, firstWord);
                    for (WordWithPos withPos : lineWords) {
                        if (Utils.jaccardSimilarity("工单号No.", withPos.getWord()) > 0.6 || "".equals(withPos.getWord())) {
                            head.add(1, withPos);
                        }
                        if (Utils.jaccardSimilarity("接待时间", withPos.getWord()) > 0.6 || "".equals(withPos.getWord())) {
                            head.add(2, withPos);
                        }
                        if (Utils.jaccardSimilarity("车牌号", withPos.getWord()) > 0.6 || "".equals(withPos.getWord())) {
                            head.add(3, withPos);
                        }
                        if (Utils.jaccardSimilarity("VINNo.", withPos.getWord()) > 0.6 || "".equals(withPos.getWord())) {
                            head.add(4, withPos);
                        }
                        if (Utils.jaccardSimilarity("主要维修项目", withPos.getWord()) > 0.6 || "主要维修项目".equals(withPos.getWord())) {
                            head.add(5, withPos);
                        }
                        if (Utils.jaccardSimilarity("行驶里程(公里)", withPos.getWord()) > 0.6 || "行驶里程(公里)".equals(withPos.getWord())) {
                            head.add(6, withPos);
                        }

                    }
                    matrixData.add(0, head);
                }
            }
        }
        return matrixData;
    }


    private WordWithPos findElementFrom(WordWithPos withPosHead, List<WordWithPos> lineWords) {
        for (WordWithPos tmp : lineWords) {
            if (Math.abs(withPosHead.getX() - tmp.getX()) < 5) {
                return tmp;
            }
        }
        return Utils.convertToWithOutPos("-");
    }




}
