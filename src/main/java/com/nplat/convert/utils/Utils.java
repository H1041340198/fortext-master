package com.nplat.convert.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nplat.convert.entity.domain.WordWithPos;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Utils {




    public static double jaccardSimilarity(String str1, String str2) {
        // 将字符串转换成词语的集合
        Set<String> set1 = new HashSet<>(Arrays.asList(str1.split("")));
        Set<String> set2 = new HashSet<>(Arrays.asList(str2.split("")));

        // 计算交集和并集的大小
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        // 计算 Jaccard 相似度
        return (double) intersection.size() / union.size();
    }


    public static int getTwoWordWithPosDistance(WordWithPos one ,WordWithPos two){
        return Math.abs(one.getX() - two.getX());
    }

    public static WordWithPos convertToWordWithPos(String word, JSONArray pos) {
        WordWithPos wordWithPos = new WordWithPos();
        wordWithPos.setWord(word);
        JSONObject posXY = pos.getJSONObject(0);
        JSONObject posX1Y1 = pos.getJSONObject(1);
        wordWithPos.setX(posXY.getInteger("x"));
        wordWithPos.setY(posXY.getInteger("y"));
        wordWithPos.setX1(posX1Y1.getInteger("x"));
        wordWithPos.setY1(posX1Y1.getInteger("y"));
        return wordWithPos;
    }

    public static WordWithPos convertToTimeWordWithPos(WordWithPos wordWithPos) {
        if(wordWithPos.getWord().length() > 10) {
            wordWithPos.setWord(wordWithPos.getWord().substring(0,10));
        }else {
            wordWithPos.setWord(wordWithPos.getWord());
        }
        return wordWithPos;
    }

    public static WordWithPos convertToWithOutPos(String word) {
        WordWithPos wordWithPos = new WordWithPos();
        wordWithPos.setWord(word);
        wordWithPos.setX(0);
        wordWithPos.setY(0);
        return wordWithPos;
    }



    /**
     * 下载文件
     *
     * @param filePath
     * @param response
     */
    public void download(String preFix , String filePath, HttpServletResponse response) {
        if (!StringUtils.isEmpty(filePath)) {
            File file = null;
            if (!filePath.startsWith(preFix)) {
                file = new File(preFix + filePath);
            } else {
                file = new File(filePath);
            }
            if (file.exists()) {
                byte[] buffer = null;
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new BufferedInputStream(new FileInputStream(filePath));
                    buffer = new byte[is.available()];
                    is.read(buffer);
                    response.reset();
                    response.addHeader("Content-Disposition", "attachment;filename=" + new String(file.getName().getBytes(), "UTF-8"));
                    response.addHeader("Content-Length", "" + file.length());
                    os = new BufferedOutputStream(response.getOutputStream());
                    response.setContentType("application/octet-stream;charset=UTF-8");
                    os.write(buffer);
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeCloseable(os);
                    closeCloseable(is);
//                    remove(filePath);
                }
            }
        }
    }
    public static void closeCloseable(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }





    public static boolean isDateTime(String timeStr){
        if(timeStr.length() > 10) {
            return MyDate.judgeStrIsDate(timeStr.substring(0,10));
        }else {
            return false;
        }
    }
}
