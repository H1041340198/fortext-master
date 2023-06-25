package com.nplat.convert.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.utils.Utils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class TextsPartsService {

    public HashMap<Integer, WordWithPos>  onlyTextPart(String str){
        JSONObject jsonObject = JSONObject.parseObject(str);
        JSONArray prism_wordsInfo = jsonObject.getJSONArray("prism_wordsInfo");//文字块信息。




        HashMap<Integer,WordWithPos> data = new HashMap<>();
        Integer lineNumber = 0;
        Integer prex = 10000;
        Integer current = 0;

        JSONObject firstWordsInfo = prism_wordsInfo.getJSONObject(0);
        String firstWord = firstWordsInfo.getString("word");
        JSONArray firstPos = firstWordsInfo.getJSONArray("pos");
        try {//第一个字符是数字
            lineNumber = Integer.valueOf(firstWord);
            prex = firstPos.getJSONObject(0).getInteger("x");
            for (int index = 1; index < prism_wordsInfo.size(); index++) {
                JSONObject wordsInfo = prism_wordsInfo.getJSONObject(index);
                String word = wordsInfo.getString("word");
                JSONArray pos = wordsInfo.getJSONArray("pos");
                current = pos.getJSONObject(0).getInteger("x");

                if (current - prex > 0 && index > 0  && lineNumber > 0) {//游标未换行其他字符,从1开始
                    // Integer no =  prism_wordsInfo.getJSONObject(index-1).getInteger("word");
//                System.out.print(no+"\t" + word+"\n");
                    if(Objects.isNull(data.get(lineNumber))) {
                        data.put(lineNumber, Utils.convertToWordWithPos(word, pos));
                    }


                } else {//新的一行第一个字符
                    //此处用于判断新的一行第一个字符如果为数字，则是此行为数据行
                    try {
                        lineNumber = Integer.valueOf(word);

                        if(lineNumber >0) {


                        }
                    } catch (Exception e) {
//                    System.out.println("这行不是数字 " + word);
                    }

                }
                prex = current;
            }


        }catch (Exception e) {//第一个字符不是数字的情况

            for (int index = 1; index < prism_wordsInfo.size(); index++) {
                JSONObject wordsInfo = prism_wordsInfo.getJSONObject(index);
                String word = wordsInfo.getString("word");
                JSONArray pos = wordsInfo.getJSONArray("pos");
                current = pos.getJSONObject(0).getInteger("x");

                if (current - prex > 0 && index > 0  && lineNumber > 0) {//游标未换行其他字符,从1开始

                } else {//新的一行第一个字符
                    //此处用于判断新的一行第一个字符如果为数字，则是此行为数据行
                    try {
                        lineNumber = Integer.valueOf(word);

                        if(lineNumber >0) {
                            data.put(lineNumber, Utils.convertToWordWithPos(firstWord, pos));

                        }
                    } catch (Exception e1) {
//                    System.out.println("这行不是数字 " + word);
                        firstWord = word;
                    }

                }
                prex = current;
            }

        }





        return data;
    }




}
