package com.nplat.convert.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nplat.convert.entity.entity.FileEntity;
import org.springframework.stereotype.Service;


@Service
public class JudgeParam {



    public Integer judge(FileEntity fileEntity) {

        JSONObject jsonObject = JSONObject.parseObject(fileEntity.getConvertContent());
        String content = jsonObject.getString("content");
//        JSONArray tableHeadTail = jsonObject.getJSONArray("tableHeadTail");//表头、表尾信息。
        JSONArray prism_wordsInfo = jsonObject.getJSONArray("prism_wordsInfo");//文字块信息。
//        JSONArray prism_tablesInfo = jsonObject.getJSONArray("prism_tablesInfo");//表格信息。

        if(content.contains("维修履历查询")){
            return 1;
        }else if ( content.contains("接待时间")
                && content.contains("行驶里程")
                && content.contains("车牌号")
                && content.contains("主要维修项目")) {//文件总
            return 1;

        } else if (
                (content.contains("维修项目编码") && content.contains("维修项目名称") && content.contains("维修类型"))  &&
                        (content.contains("零件名称") && (content.contains("零件编码")))
        ) {//完整文件
            return 2;
        } else if (( content.contains("维修项目名称") && content.contains("维修项目编码") &&content.contains("维修类型") ) &&
                (!content.contains("零件名称") && (!content.contains("零件编码")))) {//只有维修项目
            return 3;
        } else {
            Integer lineNumber = 0;
            //处理这种纯文
            //获取数据位的第一个字符
            JSONObject firstWordsInfo = prism_wordsInfo.getJSONObject(0);
            String firstWord = firstWordsInfo.getString("word");
            try {
                lineNumber = Integer.valueOf(firstWord);
            } catch (Exception e) {
                JSONObject secondWordsInfo = prism_wordsInfo.getJSONObject(1);
                String secondWord = secondWordsInfo.getString("word");
                try {
                    lineNumber = Integer.valueOf(secondWord);
                }catch (Exception e1) {
                    JSONObject thirdWordsInfo = prism_wordsInfo.getJSONObject(2);
                    String thirdWord = thirdWordsInfo.getString("word");
                    try {
                        lineNumber = Integer.valueOf(thirdWord);
                    }catch (Exception e2) {
                        return -1;
                    }
                }
            }


            //字符是数字
            if (lineNumber > 0 && content.contains("零件名称") && (content.contains("零件编码"))) {//维修项目剩余+零件
                //此种情况属于某剩余维修项目+零件信息
                return 4;
            }else if (lineNumber > 0 &&
                    ( !content.contains("零件名称") && (!content.contains("零件编码"))) &&
                    ( !content.contains("维修项目编码") && !content.contains("维修项目名称") && !content.contains("维修类型") )) {
                //单纯信息
                //此种情况下一个图片第一个字符大于0 并且不包含零件等信息，说明是其他剩余信息

                //可能是文件总剩余信息
                //可能是维修项目剩余信息
                //可能是零件剩余信息
                return 5;
            }else {
                return  -1;
            }

        }
    }





}
