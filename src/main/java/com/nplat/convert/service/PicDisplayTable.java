package com.nplat.convert.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.entity.entity.FileEntity;
import com.nplat.convert.mapper.FileEntityMapper;
import com.nplat.convert.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Service
public class PicDisplayTable {

    @Autowired
    private FileEntityMapper fileEntityMapper;


    public void display(Long id){
        FileEntity fileEntity = fileEntityMapper.selectById(id);
        ArrayList<HashMap<Integer, WordWithPos>> data = displayFileContent(fileEntity.getConvertContent());

        for (int index = 0; index < data.size(); index++) {
//            if (!removeIds.contains(index)) {
                HashMap<Integer, WordWithPos> dataMap = data.get(index);
                Iterator<Integer> iterable = dataMap.keySet().iterator();
                while (iterable.hasNext()) {
                    Integer key = iterable.next();
                    System.out.print(dataMap.get(key).getWord() + "\t\t");
                }
                System.out.println();
//            }

        }


    }

    public ArrayList<HashMap<Integer, WordWithPos>> displayFileContent(String fileData) {

        JSONObject jsonObject = JSONObject.parseObject(fileData);
//        String content = jsonObject.getString("content");
//        JSONArray tableHeadTail = jsonObject.getJSONArray("tableHeadTail");//表头、表尾信息。
        JSONArray prism_wordsInfo = jsonObject.getJSONArray("prism_wordsInfo");//文字块信息。
//        JSONArray prism_tablesInfo = jsonObject.getJSONArray("prism_tablesInfo");//表格信息。



        ArrayList<HashMap<Integer, WordWithPos>> data = new ArrayList<>();

        ////获取数据位的第一个字符
        Integer prex = 10000;
        Integer x = 0;
        HashMap<Integer, WordWithPos> hashMap = null;
        List<Integer> removeIds = new ArrayList<>();
        for (int index = 0; index < prism_wordsInfo.size(); index++) {

            JSONObject wordsInfo = prism_wordsInfo.getJSONObject(index);
            String word = wordsInfo.getString("word");
            JSONArray pos = wordsInfo.getJSONArray("pos");
            x = pos.getJSONObject(0).getInteger("x");
//            y = pos.getJSONObject(0).getInteger("y");

            if (x - prex > 0 && index > 0) {//游标未换行其他字符,从1开始
//                System.out.printf("%s-%d-%d\t", word, x, y);

                if (hashMap.size() == 1) {//到了第二个参数
                    WordWithPos preWordWithPos = hashMap.get(1);

                    try {
                        Integer number = Integer.valueOf(preWordWithPos.getWord());
                        HashMap<Integer, WordWithPos> preHashMap = data.get(data.size() > 1 ? data.size() - 2 : data.size() - 1);
                        if (preHashMap.size() == 1 && number < 100) {//上一个是1个
                            hashMap.put(2, preHashMap.get(1));
                            hashMap.put(hashMap.size() + 1, Utils.convertToWordWithPos(word, pos));
                            removeIds.add(data.size() > 1 ? data.size() - 2 : data.size() - 1);
                        } else {
                            hashMap.put(hashMap.size() + 1, Utils.convertToWordWithPos(word, pos));
                        }
                    } catch (Exception e) {
                        hashMap.put(hashMap.size() + 1, Utils.convertToWordWithPos(word, pos));
                    }
                } else {
                    hashMap.put(hashMap.size() + 1, Utils.convertToWordWithPos(word, pos));

                }



            } else {//新的第一行
                double res = Utils.jaccardSimilarity("去了解>", word);
                if (res > 0.8) {
                    continue;
                } else {
                    hashMap = new HashMap();
                    hashMap.put(1, Utils.convertToWordWithPos(word, pos));
                    data.add(hashMap);
                }

            }

            prex = x;
        }

        ArrayList<HashMap<Integer, WordWithPos>> newData = new ArrayList<>();
        for (int index = 0; index < data.size(); index++) {
            if (!removeIds.contains(index)) {
                HashMap<Integer, WordWithPos> dataMap = data.get(index);
                newData.add(dataMap);
//                Iterator<Integer> iterable = dataMap.keySet().iterator();
//                while (iterable.hasNext()) {
//                    Integer key = iterable.next();
//                    System.out.print(dataMap.get(key).getWord() + "\t\t");
//                }
//                System.out.println();
            }

        }

        return newData;
    }




}
