package com.nplat.convert.service;

import com.nplat.convert.entity.domain.ComponentItem;
import com.nplat.convert.entity.domain.Maintanence;
import com.nplat.convert.entity.domain.SecondPart;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SecondPage {
    @Autowired
    private StrToMatrixService strToMatrixService;


    //完整文件   ||   只有维修项目
    public SecondPart toSecondPart(String str) {
        SecondPart secondPart = new SecondPart();
        List<List<WordWithPos>> matrixTmp = strToMatrixService.otherTextStringToMatrix(str);
        List<List<WordWithPos>> matrixMaintanceData = new ArrayList<>();
        List<List<WordWithPos>> matrixComponentData = new ArrayList<>();
        Integer step = 0;
        for (int i = 0; i < matrixTmp.size(); i++) { //循环每一行的数据
            List<WordWithPos> lineWords = matrixTmp.get(i);//每一行数据
            WordWithPos firstWord = lineWords.get(0);
            try {//第一个字符是数字
                Integer.valueOf(firstWord.getWord());
                if (step == 1 && matrixMaintanceData.size() > 0) {//维修项目
                    List<WordWithPos> dataSub = new ArrayList<>();
                    dataSub.add(0, firstWord);
                    WordWithPos headerSecond = matrixMaintanceData.get(0).get(1);//名称
                    WordWithPos two = findElementFrom(headerSecond, lineWords);
                    if (Objects.isNull(two)) {//当前行没有,从上一行查找
                        List<WordWithPos> preLineWords = matrixTmp.get(i - 1);//每一行数据
                        two = findElementWidth(firstWord, headerSecond, preLineWords);
                    }
                    WordWithPos headerThree = matrixMaintanceData.get(0).get(2);//类型
                    WordWithPos three = findElementWithoutNull(headerThree, lineWords);
                    dataSub.add(1, two);
                    dataSub.add(2, three);
                    matrixMaintanceData.add(dataSub);
                } else if (step == 2 && matrixComponentData.size() > 0) {//零件
                    List<WordWithPos> dataSub = new ArrayList<>();
                    dataSub.add(firstWord);
                    WordWithPos headerSecond = matrixComponentData.get(0).get(1);
                    WordWithPos two = findElementFrom(headerSecond, lineWords);
                    if (Objects.isNull(two)) {//当前行没有,从上一行查找
                        List<WordWithPos> preLineWords = matrixTmp.get(i - 1);//每一行数据
                        two = findElementWidth(firstWord, headerSecond, preLineWords);
                    }
                    dataSub.add(two);
                    matrixComponentData.add(dataSub);
                }
            } catch (Exception e) {
                //不是数字行
                String lineWordsString = String.join(",",lineWords.stream().map(wordWithPos -> wordWithPos.getWord()).collect(Collectors.toList()));
                if(Utils.jaccardSimilarity("丰云商城订单付款明细", firstWord.getWord()) > 0.8 ) {
                    secondPart.setComponentItemDone(true);
                }
                if (matrixMaintanceData.size() > 0 && matrixComponentData.size() > 0) {//已经有头了
                    continue;
                } else if ("维修项目".equals(firstWord.getWord())) {//
                    step = 1;
                } else if ("零件".equals(firstWord.getWord())) {//
                    step = 2;
                    secondPart.setMaintanenceDone(true);
                }else if(lineWordsString.contains("维修项目名称")){//此行是维修项目名称
                    step = 1;
                    List<WordWithPos> head = new ArrayList<>();
                    head.add(0, firstWord);
                    for (WordWithPos withPos : lineWords) {
                        if (Utils.jaccardSimilarity("维修项目名称", withPos.getWord()) > 0.8) {
                            head.add(1, withPos);
                        }
                        if (Utils.jaccardSimilarity("维修类型", withPos.getWord()) > 0.8) {
                            head.add(2, withPos);
                        }
                    }
                    matrixMaintanceData.add(0, head);
                    continue;
                }else if(lineWordsString.contains("零件名称")){
                    List<WordWithPos> head = new ArrayList<>();
                    head.add(0, firstWord);
                    for (WordWithPos withPos : lineWords) {
                        if (Utils.jaccardSimilarity("零件名称", withPos.getWord()) > 0.6) {
                            head.add(1, withPos);
                        }
                    }
                    matrixComponentData.add(0, head);
                    continue;
                }

                if (step == 2 && (Utils.jaccardSimilarity("NO.", firstWord.getWord()) > 0.8 || Utils.jaccardSimilarity("NO", firstWord.getWord()) > 0.8)) {//这一行是头信息
                    List<WordWithPos> head = new ArrayList<>();
                    head.add(0, firstWord);
                    for (WordWithPos withPos : lineWords) {
                        if (Utils.jaccardSimilarity("零件名称", withPos.getWord()) > 0.8) {
                            head.add(1, withPos);
                        }
                    }
                    matrixComponentData.add(0, head);
                } else if (step == 1 && (Utils.jaccardSimilarity("NO.", firstWord.getWord()) > 0.8 || Utils.jaccardSimilarity("NO", firstWord.getWord()) > 0.8)) {
                    List<WordWithPos> head = new ArrayList<>();
                    head.add(0, firstWord);
                    for (WordWithPos withPos : lineWords) {
                        if (Utils.jaccardSimilarity("维修项目名称", withPos.getWord()) > 0.8) {
                            head.add(1, withPos);
                        }
                        if (Utils.jaccardSimilarity("维修类型", withPos.getWord()) > 0.8) {
                            head.add(2, withPos);
                        }
                    }
                    matrixMaintanceData.add(0, head);
                }
            }
        }

        if (matrixMaintanceData.size() > 0) {
            ArrayList<Maintanence> maintanenceList = new ArrayList<>();

            for (int m = 0; m < matrixMaintanceData.size(); m++) {
                Maintanence maintanence = new Maintanence();
                List<WordWithPos> wordWithPos = matrixMaintanceData.get(m);
                maintanence.setNo(wordWithPos.get(0));
                maintanence.setItemName(wordWithPos.get(1));
                maintanence.setType(wordWithPos.get(2));
                maintanenceList.add(maintanence);
            }
            secondPart.setMaintanence(maintanenceList);
        }
        if (matrixComponentData.size() > 0) {
            ArrayList<ComponentItem> componentItemList = new ArrayList<>();
            for (int m = 0; m < matrixComponentData.size(); m++) {
                List<WordWithPos> wordWithPos = matrixComponentData.get(m);
                ComponentItem componentItem = new ComponentItem();
                componentItem.setNo(wordWithPos.get(0));
                componentItem.setName(wordWithPos.get(1));
                componentItemList.add(componentItem);
            }
            secondPart.setComponentItem(componentItemList);

        }
        return secondPart;
    }




    private WordWithPos findElementFrom(WordWithPos head, List<WordWithPos> lineWords) {
        for (WordWithPos tmp : lineWords) {
            if (Math.abs(head.getX() - tmp.getX()) < 5) {
                return tmp;
            }
        }
        return null;
    }

    private WordWithPos findElementWithoutNull(WordWithPos head, List<WordWithPos> lineWords) {
        for (WordWithPos tmp : lineWords) {
            if (Math.abs(head.getX() - tmp.getX()) < 5) {
                return tmp;
            }
        }
        return Utils.convertToWithOutPos("-");
    }


    private WordWithPos findElementWidth(WordWithPos firstWord, WordWithPos head, List<WordWithPos> lineWords) {
        for (WordWithPos tmp : lineWords) {
            if (Math.abs(head.getX() - tmp.getX()) < 5 && Math.abs(tmp.getY() - firstWord.getY()) < 20) {
                return tmp;
            }
        }
        return Utils.convertToWithOutPos("-");
    }

}
