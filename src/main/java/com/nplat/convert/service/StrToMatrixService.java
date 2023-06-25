package com.nplat.convert.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.utils.Utils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StrToMatrixService {

    public List<List<WordWithPos>> otherTextStringToMatrix(String str) {
        JSONObject jsonObject = JSONObject.parseObject(str);
        JSONArray prism_wordsInfo = jsonObject.getJSONArray("prism_wordsInfo");//文字块信息。
        List<List<WordWithPos>> matrixTmp = new ArrayList<>();
        Integer preX = 10000;
        Integer currentX = 0;
        Integer lineNumber = -1;

        WordWithPos lineFirstNumberWordWithPos = null;
        Integer preY = 0;
        for (int index = 0; index < prism_wordsInfo.size(); index++) {
            JSONObject wordsInfo = prism_wordsInfo.getJSONObject(index);
            String word = wordsInfo.getString("word");
            JSONArray pos = wordsInfo.getJSONArray("pos");
            currentX = pos.getJSONObject(0).getInteger("x");
            int currentY = pos.getJSONObject(0).getInteger("y");
            double res = Utils.jaccardSimilarity("去了解>", word);
            if (res > 0.8) {
                continue;
            }

            if (currentX - preX > 0 && index > 0 && Math.abs(currentY - preY) < 10) {//游标未换行其他字符,从1开始
                List<WordWithPos> data = matrixTmp.get(lineNumber);//
                int dataY = data.get(0).getY();
                if (Math.abs(dataY - currentY) < 15) {
                    data.add(Utils.convertToWordWithPos(word.replaceAll(" ", ""), pos));
                    matrixTmp.set(lineNumber, data);
                }
            } else {//新的一行

                if (Objects.nonNull(lineFirstNumberWordWithPos) && Math.abs(lineFirstNumberWordWithPos.getY() - currentY) < 5) { //当前为上一行内容
                    List<WordWithPos> data = matrixTmp.get(lineNumber);//
                    data.add(Utils.convertToWordWithPos(word.replaceAll(" ", ""), pos));
                    matrixTmp.set(lineNumber, data);
                } else {
                    lineNumber += 1;
                    List<WordWithPos> lineData = new ArrayList<>();
                    lineData.add(Utils.convertToWordWithPos(word.replaceAll(" ", ""), pos));
                    matrixTmp.add(lineNumber, lineData);
                }


                try {
                    Integer.valueOf(word);
                    lineFirstNumberWordWithPos = Utils.convertToWordWithPos(word, pos);
                } catch (Exception e) {

                }



            }
            preX = currentX;
            preY = currentY;
        }

        return matrixTmp;
//        display(dealData);
//        return dealData;
    }

    public List<List<WordWithPos>> getOhterTextdealData(String str) {
        List<List<WordWithPos>> matrixTmp = otherTextStringToMatrix(str);
        List<List<WordWithPos>> matrixData = new ArrayList<>();
        boolean ask = false;
        WordWithPos preWord = null;
        for (int i = 0; i < matrixTmp.size(); i++) { //循环每一行的数据
            List<WordWithPos> tmpListxData = matrixTmp.get(i).stream().sorted(Comparator.comparing(WordWithPos::getX).thenComparing(WordWithPos::getX)).collect(Collectors.toList());//获取每一行数据对其按照x的值进行排序
            WordWithPos firstWord = tmpListxData.get(0);
            try {//第一个字符是数字
                Integer.valueOf(firstWord.getWord());
                if (ask) {//上一行不是数字,询问当前行是否需要上一行的字符
                    if (matrixData.size() > 0) {//已经有一行数据了
                        //判断上一行数据的第二列是否正确
                        if (preWord.getX() > firstWord.getX()) {//上一行的字符x轴要大于当前行第一个字符的x轴
                            List<WordWithPos> tmpData = matrixData.get(matrixData.size() - 1);//获取新矩阵最后一行
                            if ((Math.abs(tmpData.get(1).getX() - firstWord.getX()) - Math.abs(preWord.getX() - firstWord.getX())) > 10) {//判断上一行数据不正确，可以清除
                                tmpListxData.add(1, preWord);
                                matrixData.set(matrixData.size() - 1, tmpListxData);//当前行替换矩阵中最后一行
                            } else {//上一行数据正确，则以上一行为基准继续
                                if (Math.abs(tmpData.get(1).getX() - preWord.getX()) < 5) {//上一行数据的x轴距和矩阵最后一行的轴距查在合理范围内
                                    tmpListxData.add(1, preWord);
                                    matrixData.add(tmpListxData);
                                } else {
                                    System.out.println("上一行数据轴距不合理，不需要 " + preWord.getWord());
                                }
                            }
                        } else {
                            System.out.println("不需要上一行数据 " + preWord.getWord());
                        }
                    } else {//矩阵为空，添加第一行
                        tmpListxData.add(1, preWord);
                        matrixData.add(tmpListxData);
                    }
                } else {
                    //本行第一个字符是数字，直接增本行
                    matrixData.add(tmpListxData);
                }
                ask = false;
            } catch (Exception e) {
                preWord = firstWord;
                ask = true;

                //判断其是否是最后一行的结尾
                if(Utils.jaccardSimilarity("丰云商城订单付款明细", firstWord.getWord()) > 0.8 ) {
                    List<WordWithPos> lastLine = new ArrayList<>();
                    lastLine.add(Utils.convertToWithOutPos("10000"));
                    lastLine.add(Utils.convertToWithOutPos("10000"));
                    matrixData.add(matrixData.size(),lastLine);
                }
            }
        }
        return matrixData;
    }


    private void display(List<List<WordWithPos>> matrixTmp) {

        for (int i = 0; i < matrixTmp.size(); i++) { //循环每一行的数据

            matrixTmp.get(i).forEach(matdata -> {
                System.out.print(matdata.getWord() + "\t");
            });

            System.out.println();
        }


    }




}
