package com.nplat.convert.service;

import com.alibaba.fastjson.JSONObject;
import com.nplat.convert.config.ServiceException;
import com.nplat.convert.entity.domain.LingKeHead;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.entity.entity.FileEntity;
import com.nplat.convert.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * describe 一汽丰田
 * @author HanLing
 * @date 2023-06-20 11:05
 **/

@Service
public class YiQiFengTianService {

    @Autowired
    private StrToMatrixService strToMatrixService;

    /**
     * describe 一汽丰田
     * @author HanLing
     * @date 2023-06-19
     * @param fileEntityList
     * @return {@link HashMap}
     */
    public HashMap getConvert(List<FileEntity> fileEntityList) throws ServiceException {
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
        data.put("data", toHashMapList(headers, lingKeHeads));
        data.put("failedFiles", new ArrayList<>());
        data.put("analiseFailds", analiseFailds);
        return data;
    }

    private Object toHashMapList(List<List<WordWithPos>> headers, List<LingKeHead> lingKeHeads) {
        List<HashMap> hashMapList = new ArrayList<>();
        HashMap title = new HashMap();
        title.put("1", "vin码");
        title.put("2", "日期");
        title.put("3", "行驶里程");
        title.put("4", "维修类型");
        title.put("5", "项目描述");
        title.put("6", "更换材料");
        hashMapList.add(title);
        for (int i = 1; i < headers.size(); i++) {
            HashMap hashMap = new HashMap();
            List<WordWithPos> heads = headers.get(i);
            WordWithPos receptionHours = heads.get(1);
            WordWithPos vin = heads.get(2);
            WordWithPos drivenDistance = heads.get(3);
            hashMap.put("1", vin.getWord());
            hashMap.put("2", receptionHours.getWord());
            hashMap.put("3", drivenDistance.getWord());
            hashMap.put("4", "");
            hashMap.put("5", "");
            hashMap.put("6", "");
            for (LingKeHead lingKeHead:lingKeHeads){
                if(lingKeHead.getDrivenDistance().getWord().equals(drivenDistance.getWord()) && lingKeHead.getIsMate().intValue() == 0){
                    lingKeHead.setIsMate(1);
                    List<List<WordWithPos>> maintancesMatrix = lingKeHead.getMaintanence();
                    if (Objects.nonNull(maintancesMatrix) && maintancesMatrix.size() > 1) {
                        StringBuilder maintanceString = new StringBuilder();
                        StringBuilder typeString = new StringBuilder();
                        StringBuilder componentItemString = new StringBuilder();
                        Set<String> supplierSet = new HashSet<>();
                        for (int k = 1; k < maintancesMatrix.size(); k++) {
                            if(maintancesMatrix.get(k).get(1).getWord().equals("工时")){
                                if(!maintancesMatrix.get(k).get(2).getWord().contains("名称")){
                                    maintanceString.append(maintancesMatrix.get(k).get(2).getWord() + " ");
                                }
                                if(!supplierSet.contains(maintancesMatrix.get(k).get(3).getWord())){
                                    supplierSet.add(maintancesMatrix.get(k).get(3).getWord());
                                    if(!maintancesMatrix.get(k).get(3).getWord().contains("类型")){
                                        typeString.append(maintancesMatrix.get(k).get(3).getWord() + " ");
                                    }
                                }
                            }else if(maintancesMatrix.get(k).get(1).getWord().equals("零件")){
                                componentItemString.append(maintancesMatrix.get(k).get(2).getWord() + " ");
                            }
                        }
                        hashMap.put("4", typeString.toString());
                        hashMap.put("5", maintanceString.toString());
                        hashMap.put("6", componentItemString.toString());
                    }
                    break;
                }
            }
            /*Optional<LingKeHead> optionalLingKe = lingKeHeads.stream().filter(lingKeHead -> lingKeHead.getDrivenDistance().getWord().equals(drivenDistance.getWord())).findAny();
            if (optionalLingKe.isPresent()) {
                List<List<WordWithPos>> maintancesMatrix = optionalLingKe.get().getMaintanence();
                if (Objects.nonNull(maintancesMatrix) && maintancesMatrix.size() > 1) {
                    StringBuilder maintanceString = new StringBuilder();
                    StringBuilder typeString = new StringBuilder();
                    StringBuilder componentItemString = new StringBuilder();
                    Set<String> supplierSet = new HashSet<>();
                    for (int k = 1; k < maintancesMatrix.size(); k++) {
                        if(maintancesMatrix.get(k).get(1).getWord().equals("工时")){
                            if(!maintancesMatrix.get(k).get(2).getWord().contains("名称")){
                                maintanceString.append(maintancesMatrix.get(k).get(2).getWord() + " ");
                            }
                            if(!supplierSet.contains(maintancesMatrix.get(k).get(3).getWord())){
                                supplierSet.add(maintancesMatrix.get(k).get(3).getWord());
                                if(!maintancesMatrix.get(k).get(3).getWord().contains("类型")){
                                    typeString.append(maintancesMatrix.get(k).get(3).getWord() + " ");
                                }
                            }
                        }else if(maintancesMatrix.get(k).get(1).getWord().equals("零件")){
                            componentItemString.append(maintancesMatrix.get(k).get(2).getWord() + " ");
                        }
                    }
                    hashMap.put("4", typeString.toString());
                    hashMap.put("5", maintanceString.toString());
                    hashMap.put("6", componentItemString.toString());
                }
            }*/
            hashMapList.add(hashMap);
        }

        return hashMapList;
    }

    private LingKeHead dealSecondData(String convertContent) {
        List<List<WordWithPos>> matrixTmp = strToMatrixService.otherTextStringToMatrix(convertContent);
        LingKeHead lingKeHead = new LingKeHead();
        List<List<WordWithPos>> maintanence = new ArrayList<>();
        int step = 0;
        for (int i = 0; i < matrixTmp.size(); i++) {
            List<WordWithPos> lineWords = matrixTmp.get(i);
            WordWithPos firstWord = lineWords.get(0);
            try {
                Integer.valueOf(firstWord.getWord());
                if (step == 1) {
                    List<WordWithPos> head = maintanence.get(0);
                    List<WordWithPos> dataSub = new ArrayList<>();
                    dataSub.add(0, lineWords.get(0));
                    for (int k = 1; k < lineWords.size(); k++) {
                        if (head.size() == dataSub.size()) {
                            break;
                        }
                        WordWithPos headerWord = head.get(dataSub.size());
                        if(dataSub.size() == 1){
                            if (headerWord.getX() + 5 > lineWords.get(k).getX()) {
                                dataSub.add(lineWords.get(k));
                            }
                        } else if(dataSub.size() == 2){
                            if ((headerWord.getX() < lineWords.get(k).getX() + 5) ||
                                    (headerWord.getX() < lineWords.get(k).getX() + 60 && headerWord.getX1() + 300 > lineWords.get(k).getX1())) {
                                dataSub.add(lineWords.get(k));
                            }
                        } else if(dataSub.size() == 3){
                            if ((headerWord.getX() + 20 > lineWords.get(k).getX()) ||
                                    (headerWord.getX() < lineWords.get(k).getX() && headerWord.getX1() + 300 > lineWords.get(k).getX1())) {
                                dataSub.add(lineWords.get(k));
                            }
                        }
                    }
                    maintanence.add(dataSub);
                }
            } catch (Exception e) {
                StringBuilder builder = new StringBuilder();
                for (int k = 0; k < lineWords.size(); k++) {
                    WordWithPos keyWord = lineWords.get(k);
                    builder.append(keyWord.getWord());
                    if (Objects.isNull(lingKeHead.getDrivenDistance()) && keyWord.getWord().contains("行驶里程")) {
                        lingKeHead.setDrivenDistance(lineWords.get(1));
                    }
                    if ("#".equals(firstWord.getWord())) {
                        List<WordWithPos> head = new ArrayList<>();
                        head.add(0,firstWord);
                        for (WordWithPos withPos : lineWords) {
                            if (Utils.jaccardSimilarity("属性", withPos.getWord()) > 0.8) {
                                head.add(1,withPos);
                            }
                            if (Utils.jaccardSimilarity("名称", withPos.getWord()) > 0.9) {
                                head.add(2,withPos);
                            }
                            if (Utils.jaccardSimilarity("类型", withPos.getWord()) > 0.9) {
                                head.add(3,withPos);
                            }
                        }
                        maintanence.add(head);
                        step = 1;
                    }
                }
            }
        }
        lingKeHead.setMaintanence(maintanence);
        return lingKeHead;
    }

    private List<List<WordWithPos>> dealHeaderData(String convertContent) {
        List<List<WordWithPos>> matrixTmp = strToMatrixService.otherTextStringToMatrix(convertContent);
        List<List<WordWithPos>> matrixData = new ArrayList<>();
        for (int i = 0; i < matrixTmp.size(); i++) { //循环每一行的数据
            List<WordWithPos> lineWords = matrixTmp.get(i);
            WordWithPos firstWord = lineWords.get(0);
            try {
                Integer.valueOf(firstWord.getWord());
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
                            dataSub.add(lineWords.get(k - 1));
                        } else if (lineWords.get(k).getX() + 1 > headerWord.getX() && dataSub.size() == 2) {
                            dataSub.add(lineWords.get(k));
                        } else if (lineWords.get(k).getX() + 5 > headerWord.getX() && dataSub.size() == 3) {
                            dataSub.add(lineWords.get(k));
                        }
                    }
                    matrixData.add(dataSub);
                }
            } catch (Exception e) {
                if (matrixData.size() > 0) {//已经有一行数据了,且第一行数头
                    continue;
                }else if ("序号".equals(firstWord.getWord())) {
                    List<WordWithPos> head = new ArrayList<>();
                    head.add(0, firstWord);
                    for (WordWithPos withPos : lineWords) {
                        if (Utils.jaccardSimilarity("工单编号", withPos.getWord()) > 0.8) {
                            head.add(1, withPos);
                        }
                        if (Utils.jaccardSimilarity("VIN", withPos.getWord()) > 0.9) {
                            head.add(2, withPos);
                        }
                        if (Utils.jaccardSimilarity("进场行驶里程", withPos.getWord()) > 0.9) {
                            head.add(3, withPos);
                        }
                    }
                    matrixData.add(0, head);
                }
            }
        }
        return matrixData;
    }

    private Integer judge(FileEntity fileEntity) {
        JSONObject jsonObject = JSONObject.parseObject(fileEntity.getConvertContent());
        String content = jsonObject.getString("content");
        if (content.contains("序号")
                && content.contains("经销商编号")
                && content.contains("工单编号")
                && content.contains("车牌号")
                && content.contains("车型")
                && content.contains("送修人")
                && content.contains("进场行驶里程")
                && content.contains("工单类型")) {//文件总
            return 1;
        } else if (content.contains("属性")
                && content.contains("编码")
                && content.contains("名称")
                && content.contains("类型")
                && content.contains("零件数量")
                && content.contains("维修班组")) {
            return 2;
        } else {
            return -1;
        }
    }

}
