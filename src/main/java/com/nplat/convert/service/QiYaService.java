package com.nplat.convert.service;

import com.alibaba.fastjson.JSONObject;
import com.nplat.convert.config.ServiceException;
import com.nplat.convert.entity.domain.LingKeHead;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.entity.entity.FileEntity;
import com.nplat.convert.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * describe 起亚
 * @author HanLing
 * @date 2023-06-14 09:42
 **/

@Service
@Slf4j
public class QiYaService {

    @Autowired
    private StrToMatrixService strToMatrixService;

    private Integer judge(FileEntity fileEntity) {
        JSONObject jsonObject = JSONObject.parseObject(fileEntity.getConvertContent());
        String content = jsonObject.getString("content");
        if (content.contains("销商代码")
                && content.contains("R/ONO")
                && content.contains("索赔号")
                && content.contains("入库日期")
                && content.contains("维修主操作")
                && content.contains("原因配件")
                && content.contains("工作结束日")) {//文件总
            return 1;
        } else if (content.contains("工时代码")
                && content.contains("配件号码")
                && content.contains("工作类型")
                && content.contains("索赔类型")
                && content.contains("配件名称")
                && content.contains("说明")) {
            return 2;
        } else {
            return -1;
        }
    }

    /**
     * describe 起亚
     * @author HanLing
     * @date 2023-06-14
     * @param fileEntityList
     * @param serializableNo
     * @return {@link HashMap}
     */
    public HashMap getConvert(List<FileEntity> fileEntityList, String serializableNo) throws ServiceException {
        HashMap data = new HashMap();

        List<List<WordWithPos>> headers = null;
        List<LingKeHead> lingKeHeads = new ArrayList<>();

        List<String> analiseFailds = new ArrayList<>();
        if (fileEntityList.size() == 0) {
            throw new ServiceException("没有数据");
        } else {
            for (FileEntity fileEntity : fileEntityList) {
                Integer type = judge(fileEntity);
                if (type == 1) {
                    headers = dealHeaderData(fileEntity.getConvertContent());
                } else if (type == 2) {
                    LingKeHead second = dealSecondData(fileEntity.getConvertContent());
                    lingKeHeads.add(second);
                } else {
                    analiseFailds.add(fileEntity.getFileName());
                }
            }
        }
        data.put("data", toHashMapList(headers, lingKeHeads, serializableNo));
        data.put("failedFiles", new ArrayList<>());
        data.put("analiseFailds", analiseFailds);
        return data;
    }

    /**
     * describe 起亚品牌整理
     * @author HanLing
     * @date 2023-06-16
     * @param headers
     * @param lingKeHeads
     * @param serializableNo
     * @return {@link Object}
     */
    private Object toHashMapList(List<List<WordWithPos>> headers, List<LingKeHead> lingKeHeads, String serializableNo) {
        List<HashMap> hashMapList = new ArrayList<>();
        HashMap title = new HashMap();
        title.put("1", "vin码");
        title.put("2", "日期");
        title.put("3", "行驶里程");
        title.put("4", "维修类型");
        title.put("5", "项目描述");
        title.put("6", "更换材料");
        hashMapList.add(title);
        for (int i = 0; i < headers.size(); i++) {
            HashMap hashMap = new HashMap();
            List<WordWithPos> heads = headers.get(i);
            WordWithPos receptionHours = heads.get(1);
            Optional<LingKeHead> optionalLingKe = lingKeHeads.stream().filter(lingKeHead -> lingKeHead.getReceptionHours().getWord().equals(receptionHours.getWord())).findAny();

            hashMap.put("1", serializableNo);
            hashMap.put("2", receptionHours.getWord());
            hashMap.put("3", "");
            hashMap.put("4", "");
            hashMap.put("5", "");
            hashMap.put("6", "");
            if (optionalLingKe.isPresent()) {
                List<List<WordWithPos>> maintancesMatrix = optionalLingKe.get().getMaintanence();
                List<List<WordWithPos>> componentItemMatrix = optionalLingKe.get().getComponentItem();
                if (Objects.nonNull(maintancesMatrix) && maintancesMatrix.size() > 1) {
                    StringBuilder maintanceString = new StringBuilder();
                    Set<String> supplierSet = new HashSet<>();
                    for (int k = 1; k < maintancesMatrix.size(); k++) {
                        if(maintancesMatrix.get(k).size() > 2){
                            if(!supplierSet.contains(maintancesMatrix.get(k).get(2).getWord())){
                                supplierSet.add(maintancesMatrix.get(k).get(2).getWord());
                                maintanceString.append(maintancesMatrix.get(k).get(2).getWord() + " ");
                            }
                        }
                    }
                    hashMap.put("4", maintanceString.toString());
                }
                if (Objects.nonNull(maintancesMatrix) && maintancesMatrix.size() > 1) {
                    StringBuilder maintanceString = new StringBuilder();
                    for (int k = 1; k < maintancesMatrix.size(); k++) {
                        if(maintancesMatrix.get(k).size() > 1){
                            maintanceString.append(maintancesMatrix.get(k).get(1).getWord() + " ");
                        }
                    }
                    hashMap.put("5", maintanceString.toString());
                }
                if (Objects.nonNull(componentItemMatrix) && componentItemMatrix.size() > 1) {
                    StringBuilder componentItemString = new StringBuilder();
                    for (int m = 1; m < componentItemMatrix.size(); m++) {
                        if (componentItemMatrix.get(m).size() > 1){
                            componentItemString.append(componentItemMatrix.get(m).get(1).getWord() + " ");
                        }
                    }
                    hashMap.put("6", componentItemString.toString());
                }
                if (Objects.nonNull(optionalLingKe.get().getDrivenDistance())) {
                    hashMap.put("3", optionalLingKe.get().getDrivenDistance().getWord());
                }
                if (Objects.nonNull(optionalLingKe.get().getVIN())) {
                    hashMap.put("1", optionalLingKe.get().getVIN().getWord());
                }
            }
            hashMapList.add(hashMap);
        }

        return hashMapList;
    }

    /**
     * describe 起亚综合图整理
     * @author HanLing
     * @date 2023-06-16
     * @param convertContent
     * @return {@link List < List< WordWithPos>>}
     */
    private List<List<WordWithPos>> dealHeaderData(String convertContent) {
        List<List<WordWithPos>> matrixTmp = strToMatrixService.otherTextStringToMatrix(convertContent);
        List<List<WordWithPos>> matrixData = new ArrayList<>();
        for (int i = 0; i < matrixTmp.size(); i++) { //循环每一行的数据
            List<WordWithPos> lineWords = matrixTmp.get(i);
            WordWithPos firstWord = lineWords.get(0);
            try {
                if (matrixData.size() > 0) {//已经有一行数据了,且第一行数头
                    List<WordWithPos> head = matrixData.get(0);//拿到头
                    List<WordWithPos> dataSub = new ArrayList<>();
                    dataSub.add(0, lineWords.get(0));
                    for (int k = 1; k < lineWords.size(); k++) {
                        if (head.size() == dataSub.size()) {
                            break;
                        }
                        WordWithPos headerWord = head.get(dataSub.size());
                        if (lineWords.get(k).getX() > headerWord.getX() && dataSub.size() == 1) {
                            String[] times = lineWords.get(k - 1).getWord().split(" ");
                            dataSub.add(convertToTimeWordWithPos(lineWords.get(k - 1), times[times.length - 1]));
                        } else if (lineWords.get(k).getX() > headerWord.getX()) {
                            dataSub.add(lineWords.get(k - 1));
                        }
                    }
                    matrixData.add(dataSub);
                }else if (firstWord.getWord().contains("销商代码")) {
                    List<WordWithPos> head = new ArrayList<>();
                    head.add(0, firstWord);
                    for (WordWithPos withPos : lineWords) {
                        if (Utils.jaccardSimilarity("入库日期", withPos.getWord()) > 0.8) {
                            head.add(1, withPos);
                        }
                    }
                    matrixData.add(0, head);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<List<WordWithPos>> resultList = new ArrayList<>(); // 用于存储去重后的结果
        Set<String> supplierSet = new HashSet<>();
        for (int i = 1; i < matrixData.size(); i++) {
            WordWithPos head = matrixData.get(i).get(1);
            if(!supplierSet.contains(head.getWord())){
                supplierSet.add(head.getWord());
                resultList.add(matrixData.get(i));
            }
        }
        return resultList;
    }

    /**
     * describe 起亚详情图整理
     * @author HanLing
     * @date 2023-06-16
     * @param convertContent
     * @return {@link LingKeHead}
     */
    private LingKeHead dealSecondData(String convertContent) {
        List<List<WordWithPos>> matrixTmp = strToMatrixService.otherTextStringToMatrix(convertContent);
        LingKeHead lingKeHead = new LingKeHead();
        List<List<WordWithPos>> maintanence = new ArrayList<>();
        List<List<WordWithPos>> componentItem = new ArrayList<>();
        int step = 0;
        int accessory = 0;
        for (int i = 0; i < matrixTmp.size(); i++) {
            List<WordWithPos> lineWords = matrixTmp.get(i);
            WordWithPos firstWord = lineWords.get(0);
            try {
                if (step == 1) {
                    List<WordWithPos> head = maintanence.get(0);//拿到头
                    List<WordWithPos> dataSub = new ArrayList<>();
                    dataSub.add(0, lineWords.get(0));
                    for (int k = 1; k < lineWords.size(); k++) {
                        if (head.size() == dataSub.size()) {
                            break;
                        }
                        WordWithPos headerWord = head.get(dataSub.size());
                        if(dataSub.size() == 1){
                            if (headerWord.getX() > lineWords.get(k).getX() + 70) {
                                dataSub.add(lineWords.get(k));
                            }
                        }else if(dataSub.size() == 2){
                            if (Math.abs(headerWord.getX() - lineWords.get(k).getX()) < 15 && lineWords.get(k).getX1() + 40 > headerWord.getX1()) {
                                dataSub.add(lineWords.get(k));
                            }
                        }
                    }
                    maintanence.add(dataSub);
                }else if ("工时代码★".equals(firstWord.getWord())) {//以经销商代码开头，则创建头
                    List<WordWithPos> head = new ArrayList<>();
                    head.add(0, firstWord);
                    for (WordWithPos withPos : lineWords) {
                        if (Utils.jaccardSimilarity("说明", withPos.getWord()) > 0.8) {
                            head.add(1, withPos);
                        }
                        if (Utils.jaccardSimilarity("工作类型", withPos.getWord()) > 0.8) {
                            head.add(2, withPos);
                        }
                    }
                    maintanence.add(0, head);
                    step = 1;
                }
                if (accessory == 1) {
                    List<WordWithPos> head = componentItem.get(0);//拿到头
                    List<WordWithPos> dataSub = new ArrayList<>();
                    dataSub.add(0, lineWords.get(0));
                    for (int k = 1; k < lineWords.size(); k++) {
                        if (head.size() == dataSub.size()) {
                            break;
                        }
                        WordWithPos headerWord = head.get(dataSub.size());
                        if(dataSub.size() == 1){
                            if (headerWord.getX() > lineWords.get(k).getX() + 90) {
                                dataSub.add(lineWords.get(k));
                            }
                        }
                    }
                    componentItem.add(dataSub);
                }else if ("配件号码★".equals(firstWord.getWord())) {//以经销商代码开头，则创建头
                    List<WordWithPos> head = new ArrayList<>();
                    head.add(0, firstWord);
                    for (WordWithPos withPos : lineWords) {
                        if (Utils.jaccardSimilarity("配件名称", withPos.getWord()) > 0.8) {
                            head.add(1, withPos);
                        }
                    }
                    componentItem.add(0, head);
                    accessory = 1;
                }

                StringBuilder builder = new StringBuilder();
                for (int k = 0; k < lineWords.size(); k++) {
                    WordWithPos keyWord = lineWords.get(k);
                    builder.append(keyWord.getWord());
                    if (Objects.isNull(lingKeHead.getVIN()) && Utils.jaccardSimilarity(keyWord.getWord(), "车架号") > 0.1) {
                        lingKeHead.setVIN(convertWithPosToVIN(lineWords.get(1)));
                    }

                    if (Objects.isNull(lingKeHead.getDrivenDistance()) && keyWord.getWord().contains("行驶里程")) {
                        lingKeHead.setDrivenDistance(convertWithPosToVIN(lineWords.get(1)));
                    }
                }
                //时间是否已经寻找到了
                if (Objects.isNull(lingKeHead.getReceptionHours())) {
                    //每一行中寻找时间
                    String regex = "\\d{4}\\.\\d{1,2}\\.\\d{1,2}";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(builder.toString());
                    if (matcher.find()) {
                        String dateStr = matcher.group();
                        lingKeHead.setReceptionHours(Utils.convertToWithOutPos(dateStr));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        lingKeHead.setMaintanence(maintanence);
        lingKeHead.setComponentItem(componentItem);
        return lingKeHead;
    }

    private WordWithPos convertToTimeWordWithPos(WordWithPos wordWithPos, String time) {
        if (time.length() > 10) {
            wordWithPos.setWord(time.substring(0, 10));
        } else {
            wordWithPos.setWord(time);
        }
        return wordWithPos;
    }

    private WordWithPos convertWithPosToVIN(WordWithPos wordWithPos) {
        String[] words = wordWithPos.getWord().split("：");
        wordWithPos.setWord(words[words.length - 1]);
        return wordWithPos;
    }

}
