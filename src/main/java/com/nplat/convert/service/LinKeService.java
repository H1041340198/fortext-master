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

@Service
@Slf4j
public class LinKeService {

    @Autowired
    private StrToMatrixService strToMatrixService;
    private Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");


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
                System.out.printf("{%s}========={%d}\n", fileEntity.getFileName(), type);
                if (type == 1) {
                    headers = dealHeaderData(fileEntity.getConvertContent());
                } else if (type == 2 || type == 3) {
                    LingKeHead second = dealSecondData(fileEntity.getConvertContent(), type);
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

    private List<HashMap> toHashMapList(List<List<WordWithPos>> headers, List<LingKeHead> lingKeHeads, String serializableNo) {
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
            WordWithPos types = heads.get(2);
            WordWithPos receptionHours = heads.get(3);

            Optional<LingKeHead> optionalLingKe = lingKeHeads.stream().filter(lingKeHead -> lingKeHead.getReceptionHours().getWord().equals(receptionHours.getWord())).findAny();

            hashMap.put("1", serializableNo);
            hashMap.put("2", receptionHours.getWord());
            hashMap.put("3", "");
            hashMap.put("4", types.getWord());
            hashMap.put("5", "");
            hashMap.put("6", "");
            if (optionalLingKe.isPresent()) {
                List<List<WordWithPos>> maintancesMatrix = optionalLingKe.get().getMaintanence();
                List<List<WordWithPos>> componentItemMatrix = optionalLingKe.get().getComponentItem();
                if (Objects.nonNull(maintancesMatrix) && maintancesMatrix.size() > 1) {
                    StringBuilder maintanceString = new StringBuilder();
                    for (int k = 1; k < maintancesMatrix.size(); k++) {
                        maintanceString.append(maintancesMatrix.get(k).get(1).getWord());
                    }
                    hashMap.put("5", maintanceString.toString());
                }
                if (Objects.nonNull(componentItemMatrix) && componentItemMatrix.size() > 1) {
                    StringBuilder componentItemString = new StringBuilder();
                    for (int m = 1; m < componentItemMatrix.size(); m++) {
                        componentItemString.append(componentItemMatrix.get(m).get(1).getWord());
                    }
                    hashMap.put("6", componentItemString.toString());
                }
                if (Objects.nonNull(optionalLingKe.get().getDrivenDistance())) {
                    hashMap.put("3", optionalLingKe.get().getDrivenDistance().getWord());
                }
            }
            hashMapList.add(hashMap);
        }

        return hashMapList;
    }

    private LingKeHead dealSecondData(String str, Integer type) {
        LingKeHead lingKeHead = new LingKeHead();
        List<List<WordWithPos>> matrixTmp = strToMatrixService.otherTextStringToMatrix(str);
        List<List<WordWithPos>> maintanence = null;
        List<List<WordWithPos>> componentItem = null;
        int step = 0;

        for (int i = 0; i < matrixTmp.size(); i++) {
            List<WordWithPos> lineWords = matrixTmp.get(i);
            WordWithPos firstWord = lineWords.get(0);
            try {//第一个字符是数字
                Integer.valueOf(firstWord.getWord());
                if (step == 1) {
                    List<WordWithPos> head = maintanence.get(0);
                    List<WordWithPos> subData = new ArrayList<>();
                    for (int j = 0; j < lineWords.size(); j++) {
                        if (Math.abs(head.get(0).getX() - lineWords.get(j).getX()) < 10) {
                            subData.add(0, lineWords.get(j));
                        }
                        if ((head.get(1).getX() > lineWords.get(j).getX() && lineWords.get(j).getX1()+50 > head.get(1).getX1()) ||
                                (head.get(1).getX() < lineWords.get(j).getX() && lineWords.get(j).getX1() < head.get(1).getX1()) ||

                                (Math.abs(head.get(1).getX() - lineWords.get(j).getX()) < 15)) {
                            subData.add(1, lineWords.get(j));
                        }
                    }
                    maintanence.add(subData);
                } else if (step == 2) {
                    List<WordWithPos> head = componentItem.get(0);
                    List<WordWithPos> subData = new ArrayList<>();
                    for (int j = 0; j < lineWords.size(); j++) {
                        if (Math.abs(head.get(0).getX() - lineWords.get(j).getX()) < 10) {
                            subData.add(0, lineWords.get(j));
                        }
                        if ((head.get(1).getX() > lineWords.get(j).getX() && lineWords.get(j).getX1()+55 > head.get(1).getX1()) ||
                                (head.get(1).getX() < lineWords.get(j).getX() && lineWords.get(j).getX1() < head.get(1).getX1()) ||
                                (Math.abs(head.get(1).getX() - lineWords.get(j).getX()) < 15)) {
                            subData.add(1, lineWords.get(j));
                        }
                    }
                    componentItem.add(subData);
                }

            } catch (Exception e) {

                StringBuilder builder = new StringBuilder();
                for (int k = 0; k < lineWords.size(); k++) {
                    WordWithPos keyWord = lineWords.get(k);
                    builder.append(keyWord.getWord());
                    if (Objects.isNull(lingKeHead.getVIN()) && Utils.jaccardSimilarity(keyWord.getWord(), "VIN") > 0.1) {
                        lingKeHead.setVIN(convertWithPosToVIN(keyWord));
                    }

                    if (Objects.isNull(lingKeHead.getLAD()) && keyWord.getWord().startsWith("LAD")) {
                        lingKeHead.setLAD(convertWithPosToVIN(keyWord));
                    }

                    if (Objects.isNull(lingKeHead.getDrivenDistance()) && keyWord.getWord().contains("KM")) {
                        lingKeHead.setDrivenDistance(convertWithPosToDrivenDistance(keyWord));
                    }

                    if (type == 2 && lineWords.size() > 4 && Utils.jaccardSimilarity(keyWord.getWord(), "维修项目名称") > 0.5) {
                        if (Objects.isNull(maintanence)) {
                            maintanence = new ArrayList<>();
                            List<WordWithPos> head = new ArrayList<>();
                            head.add(0, firstWord);
                            head.add(1, keyWord);
                            maintanence.add(0, head);
                        }
                        step = 1;
                    }
                    if (type == 2 && lineWords.size() > 4 && Utils.jaccardSimilarity(keyWord.getWord(), "配件名称") > 0.5) {
                        if (Objects.isNull(componentItem)) {
                            componentItem = new ArrayList<>();
                            List<WordWithPos> head = new ArrayList<>();
                            head.add(0, firstWord);
                            head.add(1, lineWords.get(k));
                            componentItem.add(0, head);

                        }
                        step = 2;
                    }

                    if (type == 3 && lineWords.size() > 4 && Utils.jaccardSimilarity(keyWord.getWord(), "项目名称") > 0.5) {
                        if (Objects.isNull(maintanence)) {
                            maintanence = new ArrayList<>();
                            List<WordWithPos> head = new ArrayList<>();
                            head.add(0, firstWord);
                            head.add(1, keyWord);
                            maintanence.add(0, head);
                        }
                        step = 1;
                    }
                    if (type == 3 && lineWords.size() > 4 && Utils.jaccardSimilarity(keyWord.getWord(), "材料名称") > 0.5) {
                        if (Objects.isNull(componentItem)) {
                            componentItem = new ArrayList<>();
                            List<WordWithPos> head = new ArrayList<>();
                            head.add(0, firstWord);
                            head.add(1, keyWord);
                            componentItem.add(0, head);

                        }
                        step = 2;
                    }

                }
                //时间是否已经寻找到了
                if (Objects.isNull(lingKeHead.getReceptionHours())) {
                    //每一行中寻找时间
                    Matcher matcher = pattern.matcher(builder.toString());
                    if (matcher.find()) {
                        String dateStr = matcher.group();
                        lingKeHead.setReceptionHours(Utils.convertToWithOutPos(dateStr));
                    }
                }
            }

        }
        lingKeHead.setMaintanence(maintanence);
        lingKeHead.setComponentItem(componentItem);
        return lingKeHead;
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
                    List<WordWithPos> head = matrixData.get(0);//拿到头
                    List<WordWithPos> dataSub = new ArrayList<>();
                    dataSub.add(0, lineWords.get(0));
                    for (int k = 1; k < lineWords.size(); k++) {
                        if (head.size() == dataSub.size()) {
                            break;
                        }
                        WordWithPos headerWord = head.get(dataSub.size());
                        if (lineWords.get(k).getX() > headerWord.getX() && dataSub.size() == 3) {
                            String[] times = lineWords.get(k - 1).getWord().split(" ");
                            dataSub.add(convertToTimeWordWithPos(lineWords.get(k - 1), times[times.length - 1]));
                        } else if (lineWords.get(k).getX() > headerWord.getX()) {
                            dataSub.add(lineWords.get(k - 1));
                        }
                    }
                    matrixData.add(dataSub);
                }
            } catch (Exception e) {
                //不是数字行
                if (matrixData.size() > 0) {//已经有头了
                    continue;
                } else if ("序号".equals(firstWord.getWord())) {//以序号开头，则创建头
                    List<WordWithPos> head = new ArrayList<>();
                    head.add(0, firstWord);
                    for (WordWithPos withPos : lineWords) {
                        if (Utils.jaccardSimilarity("服务委托书编号", withPos.getWord()) > 0.8) {
                            head.add(1, withPos);
                        }
                        if (Utils.jaccardSimilarity("工单类型", withPos.getWord()) > 0.8) {
                            head.add(2, withPos);
                        }
                        if (Utils.jaccardSimilarity("顾客到店时间", withPos.getWord()) > 0.8) {
                            head.add(3, withPos);
                        }
                    }
                    matrixData.add(0, head);
                }
            }
        }
        return matrixData;
    }

    private WordWithPos convertToTimeWordWithPos(WordWithPos wordWithPos, String time) {
        if (time.length() > 10) {
            wordWithPos.setWord(time.substring(0, 10));
        } else {
            wordWithPos.setWord(time);
        }
        return wordWithPos;
    }

    private WordWithPos appandWithPos(WordWithPos wordWithPos, WordWithPos next) {
        wordWithPos.setWord(wordWithPos.getWord() + next.getWord());
        return wordWithPos;
    }

    private WordWithPos convertWithPosToVIN(WordWithPos wordWithPos) {
        String[] words = wordWithPos.getWord().split("：");
        wordWithPos.setWord(words[words.length - 1]);
        return wordWithPos;
    }

    private WordWithPos convertWithPosToDrivenDistance(WordWithPos wordWithPos) {
        String wo = wordWithPos.getWord().replaceAll("入", "");
        wo = wo.replaceAll("厂", "");
        wo = wo.replaceAll("里", "");
        wo = wo.replaceAll("程", "");
        wo = wo.replaceAll("：", "");
        wo = wo.replaceAll(":", "");
        wo = wo.replaceAll(" ", "");
        wordWithPos.setWord(wo);
        return wordWithPos;
    }

    private Integer judge(FileEntity fileEntity) {
        JSONObject jsonObject = JSONObject.parseObject(fileEntity.getConvertContent());
        String content = jsonObject.getString("content");
        if (content.contains("序号")
                && content.contains("品牌")
                && content.contains("车型")
                && content.contains("经销商代码")
                && content.contains("经销商名称")
                && content.contains("服务委托书编号")
                && content.contains("工单类型")
                && content.contains("顾客到店时间")) {//文件总
            return 1;

        } else if (content.contains("VIN")
                && content.contains("入厂里程")
                && content.contains("入厂里程")
                && content.contains("维修项目代码")
                && content.contains("维修项目名称")
                && content.contains("配件代码")
                && content.contains("配件名称")) {

            return 2;
        } else if (content.contains("工单号码")
                && content.contains("进厂日期")
                && (content.contains("项目名称") || content.contains("项目代码"))
                && (content.contains("材料名称") || content.contains("材料代码"))) {

            return 3;
        } else {
            return -1;
        }
    }

}
