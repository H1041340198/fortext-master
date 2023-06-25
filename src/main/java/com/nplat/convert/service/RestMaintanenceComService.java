package com.nplat.convert.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nplat.convert.entity.domain.ComponentItem;
import com.nplat.convert.entity.domain.Maintanence;
import com.nplat.convert.entity.domain.SecondPart;
import com.nplat.convert.entity.domain.SecondPartRestMaintanenceCom;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.utils.Utils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@Service
public class RestMaintanenceComService {




    public SecondPartRestMaintanenceCom convert(String str){

        JSONObject jsonObject = JSONObject.parseObject(str);
        JSONArray prism_wordsInfo = jsonObject.getJSONArray("prism_wordsInfo");//文字块信息。

        SecondPartRestMaintanenceCom secondPartRestMaintanenceCom = new SecondPartRestMaintanenceCom();

        HashMap<Integer,String> maintanences = new HashMap<>();
        ArrayList<WordWithPos> tempWords = new ArrayList<>();
        ArrayList<ComponentItem> componentItemList = new ArrayList<>();
        ComponentItem componentItemHead = null;
        ComponentItem componentItem = null;
        StringBuilder lastNo = new StringBuilder();
        //记录lines = 0;//如果lines=0 表示在处理维修项目，lines=1 表示在处理零件
        Integer lines = -1;
        Integer lineNumber = 0;
        Integer prex = 10000;
        Integer current = 0;




        for (int index = 0; index < prism_wordsInfo.size(); index++) {

            JSONObject wordsInfo = prism_wordsInfo.getJSONObject(index);
            String word = wordsInfo.getString("word");
            JSONArray pos = wordsInfo.getJSONArray("pos");
            current = pos.getJSONObject(0).getInteger("x");

            if (current - prex > 0 && index > 0) {//游标未换行其他字符,从1开始

                if (lines == 1) {
                    if (Objects.nonNull(componentItemHead)
                            && Objects.nonNull(componentItemHead.getName())
                            && Objects.nonNull(componentItemHead.getNo()) && lineNumber > 0) {
//                        System.out.println(lineNumber + " :" + word);
                        componentItemList = convertComponentItem(lineNumber, componentItemList, componentItemHead, word, pos);

                    } else {//处理头信息
                        componentItemHead = componentHead(componentItemHead, prism_wordsInfo, index, word, pos);
                    }
                }

            } else {//新的一行第一个字符

                tempWords = collectTempWords(wordsInfo, componentItemHead, tempWords);//收集错行信息

                if ("零件".equals(word)) {//维修项目头
                    componentItemHead = new ComponentItem();
                    lines = 1;
                    lineNumber = 0;
                    lastNo.delete(0, lastNo.length());
                }
                if (Objects.nonNull(componentItemHead)
                        && Objects.nonNull(componentItemHead.getNo())
                        && Objects.nonNull(componentItemHead.getName())
                        && lineNumber == 0) {//头部信息，可能重复
                    if (componentItemList.size() == 0) {
                        componentItemList.add(componentItemHead);
                    }
                }



                //此处用于判断新的一行第一个字符如果为数字，则是此行为数据行
                try {
                    lineNumber = Integer.valueOf(word);
                    //判断是否存在零件信息，否则就是维修记录信息
                    if(Objects.isNull(componentItemHead)) {
                        JSONObject nextWordsInfo = prism_wordsInfo.getJSONObject(index+1);
                        String netWord = nextWordsInfo.getString("word");
                        maintanences.put(lineNumber,netWord);
                        lastNo.append(lineNumber);
                        lastNo.append(",");
                    } else {
                        componentItem = new ComponentItem();
                        componentItem.setNo(Utils.convertToWordWithPos(word, pos));
                        componentItemList.add(lineNumber, componentItem);

                        lastNo.append(lineNumber);
                        lastNo.append(",");
                    }

                } catch (Exception e) {
//                    System.out.println("这行不是数字 " + word);
                }


            }

            prex = current;
        }

        //处理临时数据
        if(tempWords.size() > 0 ) {
            componentItemList = dealOthers(componentItemList, tempWords);
        }

        secondPartRestMaintanenceCom.setComponentItem(componentItemList);
        secondPartRestMaintanenceCom.setMaintanences(maintanences);




        return secondPartRestMaintanenceCom;



    }


    public ArrayList<WordWithPos> collectTempWords(JSONObject wordsInfo,
                                                   ComponentItem componentItemHead,
                                                   ArrayList<WordWithPos> tempWords) {

        String word = wordsInfo.getString("word");
        JSONArray pos = wordsInfo.getJSONArray("pos");
        Integer current = pos.getJSONObject(0).getInteger("x");
        if (Objects.nonNull(componentItemHead) && Objects.nonNull(componentItemHead.getName())) {//维修信息
            if (Math.abs(current - componentItemHead.getName().getX()) < 5) {
                tempWords.add(Utils.convertToWordWithPos(word, pos));
            }
        }
        return tempWords;
    }

    //处理零件清单
    public ArrayList<ComponentItem> convertComponentItem(Integer lineNumber,
                                                         ArrayList<ComponentItem> componentItemArrayList,
                                                         ComponentItem componentItemHead,
                                                         String word,
                                                         JSONArray pos) {
        int currentX = pos.getJSONObject(0).getInteger("x");
        int currentY = pos.getJSONObject(0).getInteger("y");
        if (componentItemArrayList.size() > 0) {//防止没有头信息
            ComponentItem componentItem = componentItemArrayList.get(lineNumber);//根据行号获取对象信息
            //根据x坐标来判断位置
            if (Math.abs(currentX - componentItemHead.getName().getX()) < 5 && Math.abs(currentY - componentItemHead.getNo().getY()) < 5) {
                componentItem.setName(Utils.convertToWordWithPos(word, pos));
                componentItemArrayList.set(lineNumber, componentItem);
            }
        }

        return componentItemArrayList;
    }


    public ComponentItem componentHead(ComponentItem componentItemHead, JSONArray prism_wordsInfo, int currentIndex, String word, JSONArray pos) {
        if (Utils.jaccardSimilarity("零件名称", word) > 0.8) {
            componentItemHead.setName(Utils.convertToWordWithPos(word, pos));
            JSONObject NowordsInfo = prism_wordsInfo.getJSONObject(currentIndex - 1);
            //再找No.
            componentItemHead.setNo(Utils.convertToWordWithPos(NowordsInfo.getString("word"), NowordsInfo.getJSONArray("pos")));
        }
        return componentItemHead;
    }

    public ArrayList<ComponentItem> dealOthers(ArrayList<ComponentItem> componentItemList,
                                               ArrayList<WordWithPos> tempWords) {

        if (Objects.nonNull(componentItemList) && componentItemList.size() > 1) {
            for (int k = 1; k < componentItemList.size(); k++) {
                ComponentItem componentItem = componentItemList.get(k);
                if (Objects.isNull(componentItem.getName())) {
                    for (int j = 0; j < tempWords.size(); j++) {
                        if (Math.abs(componentItem.getNo().getY() - tempWords.get(j).getY()) < 15) {
                            componentItem.setName(tempWords.get(j));
                            componentItemList.set(k, componentItem);
                        }
                    }
                }
            }
        }
        return componentItemList;

    }
}
