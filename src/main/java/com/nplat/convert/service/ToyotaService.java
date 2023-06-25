package com.nplat.convert.service;

import com.nplat.convert.config.ServiceException;
import com.nplat.convert.entity.domain.ComponentItem;
import com.nplat.convert.entity.domain.SecondPartRestMaintanenceCom;
import com.nplat.convert.entity.entity.FileEntity;
import com.nplat.convert.entity.domain.Maintanence;
import com.nplat.convert.entity.domain.RepairSearch;
import com.nplat.convert.entity.domain.SecondPart;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.mapper.FileEntityMapper;
import com.nplat.convert.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class ToyotaService {

    @Autowired
    private FileEntityMapper fileEntityMapper;
    @Autowired
    private JudgeParam judgeParam;

    @Autowired
    private FirstPage firstPage;
    @Autowired
    private SecondPage secondPage;
    @Autowired
    private PicDisplayTable picDisplayTable;
    @Autowired
    private RestMaintanenceComService restMaintanenceComService;
    @Autowired
    private StrToMatrixService strToMatrixService;

    public HashMap getConvert(List<FileEntity> fileEntityList) throws ServiceException {
        HashMap data = new HashMap();
        List<String> failedFiles = new ArrayList<>();
        List<RepairSearch> repairSearchList = null;
        List<SecondPart> secondPartList = new ArrayList<>();
        List<List<List<WordWithPos>>> dataList = new ArrayList<>(); //纯文本信息
        List<SecondPartRestMaintanenceCom> secondPartRestMaintanenceComArrayList = new ArrayList<>();


        for (FileEntity fileEntity : fileEntityList) {
            Integer type = judgeParam.judge(fileEntity);
            System.out.printf("{%s}========={%d}\n", fileEntity.getFileName(), type);
            if (type == -1) {
                failedFiles.add(fileEntity.getFileName());
                //throw new ServiceException("文件:" + fileEntity.getFileName() + "无法识别");
            }
            if (type == 1) {//文件总
                //repairSearchList = firstPage.header(fileEntity.getConvertContent());
                repairSearchList = firstPage.toRepairSearchList(repairSearchList, fileEntity.getConvertContent());
            } else if (type == 2 || type == 3) {//完整文件   ||   只有维修项目
                SecondPart secondPart = secondPage.toSecondPart(fileEntity.getConvertContent());
                secondPart.setFileName(fileEntity.getFileName());
                secondPartList.add(secondPart);
            } else if (type == 4) {//维修项目剩余+零件
                SecondPartRestMaintanenceCom secondPartRestMaintanenceCom = restMaintanenceComService.convert(fileEntity.getConvertContent());
                secondPartRestMaintanenceCom.setFileName(fileEntity.getFileName());
                secondPartRestMaintanenceComArrayList.add(secondPartRestMaintanenceCom);
            } else if (type == 5) {//单纯信息
                List<List<WordWithPos>> wordWithPosMatrix = strToMatrixService.getOhterTextdealData(fileEntity.getConvertContent());
                dataList.add(wordWithPosMatrix);
            } else {//other
                System.out.println(fileEntity.getFileName());
                failedFiles.add(fileEntity.getFileName());
                //throw new ServiceException("未知文件信息:" + fileEntity.getFileName());
            }

        }

        //处理维修项目剩余+零件
        if (secondPartRestMaintanenceComArrayList.size() > 0) {
            secondPartList = dealMaintanenceComponentItem(secondPartList, secondPartRestMaintanenceComArrayList);
        }
        //处理单纯信息
        if (dataList.size() > 0) {
            repairSearchList = appandTextToRepairSearch(repairSearchList, dataList);
            appandOthers(secondPartList, dataList);
        }
        if (Objects.isNull(repairSearchList) || repairSearchList.size() == 0) {
            throw new ServiceException("这组图片缺少主要文件类，请检查原始文件");
        }
        //匹配文件总+完整文件维修项目
        repairSearchList = look(repairSearchList, secondPartList);
        //display(repairSearchList);

        List<String> analiseFailds = repairSearchList.
                stream().
                filter(repairSearch -> Objects.nonNull(repairSearch.getSecondPartMultiParts())).
                flatMap(repairSearch -> repairSearch.getSecondPartMultiParts().stream().map(s -> s.toString())).
                collect(Collectors.toList());
        data.put("data", repairSearchList);
        data.put("failedFiles", Objects.isNull(failedFiles) ? new ArrayList<>() : failedFiles);
        data.put("analiseFailds", Objects.isNull(analiseFailds) ? new ArrayList<>() : analiseFailds);
        return data;

    }

    private List<RepairSearch> look(List<RepairSearch> repairSearchList, List<SecondPart> secondPartList) {

        for (int j = 0; j < secondPartList.size(); j++) {
            SecondPart secondPart = secondPartList.get(j);

            double secondScore = 0;
            ArrayList<Maintanence> maintanenceArrayList = secondPart.getMaintanence();
            if (Objects.nonNull(maintanenceArrayList)) {
                int indexI = -1;
                for (int k = 1; k < maintanenceArrayList.size(); k++) {
                    Maintanence subMaintanence = maintanenceArrayList.get(k);
                    if (Objects.isNull(subMaintanence) || Objects.isNull(subMaintanence.getItemName()) || Objects.isNull(subMaintanence.getItemName().getWord())) {
                        continue;
                    }
                    String word = maintanenceArrayList.get(k).getItemName().getWord();

                    for (int i = 1; i < repairSearchList.size(); i++) {


                        RepairSearch repairSearch = repairSearchList.get(i);
                        String gram = repairSearch.getMajorMaintenanceItems().getWord();

                        double tmpScore = Utils.jaccardSimilarity(gram, word);
                        if (tmpScore > secondScore) {
                            secondScore = tmpScore;
                            indexI = i;
                        }
                    }
                }

                //找到打分最高的一组second和主序列号索引
                if (indexI != -1) {
                    RepairSearch repairSearch = repairSearchList.get(indexI);
                    if (Objects.nonNull(repairSearch.getSecondPart())) {
                        //又找到了新的第二页
                        List<String> faidList = repairSearch.getSecondPartMultiParts();
                        if (Objects.isNull(faidList)) {
                            faidList = new ArrayList<>();
                        }
                        faidList.add(secondPart.getFileName());
                        repairSearch.setSecondPartMultiParts(faidList);
                    } else {
                        repairSearch.setSecondPart(secondPart);
                    }
                    repairSearchList.set(indexI, repairSearch);
                }
            }
        }





        return repairSearchList;
    }

    private List<SecondPart> dealMaintanenceComponentItem(List<SecondPart> secondPartList, List<SecondPartRestMaintanenceCom> secondPartRestMaintanenceComList) {

        for (int k = 0; k < secondPartRestMaintanenceComList.size(); k++) {
            SecondPartRestMaintanenceCom secondPartRestMaintanenceCom = secondPartRestMaintanenceComList.get(k);
            HashMap<Integer, String> maintances = secondPartRestMaintanenceCom.getMaintanences();
//            ArrayList<ComponentItem>  componentItemArrayList = secondPartRestMaintanenceCom.getComponentItem();
            //maintances 维修项目剩余+  零件 componentItemArrayList
            List<String> sourceStrMaintances = new ArrayList<>();
            if (Objects.nonNull(maintances)) {
                sourceStrMaintances.addAll(maintances.values());
            }
            //零件
//            List<String> sourceStrComponents = new ArrayList<>();
//            if(Objects.nonNull(componentItemArrayList)) {
//                sourceStrComponents = componentItemArrayList.stream().map(componentItem -> componentItem.getName().getWord()).collect(Collectors.toList());
//            }

            int index = -1;//定义一个索引，用于存储secondPartList的匹配的数据
            int count = 0; //相同的个数
            for (int i = 0; i < secondPartList.size(); i++) {
                SecondPart secondPart = secondPartList.get(i);
                ArrayList<Maintanence> targetMaintanceList = secondPart.getMaintanence();
                List<String> targetStrMaintances = targetMaintanceList.stream().map(maintanence -> maintanence.getItemName().getWord()).collect(Collectors.toList());
                List<String> resultStrList = new ArrayList<>(targetStrMaintances);
                resultStrList.retainAll(sourceStrMaintances);
                if (resultStrList.size() > 1) {
                    if (resultStrList.size() >= count) {
                        count = resultStrList.size();
                        index = i;
                    }
                }
            }

            if (index != -1) {
                SecondPart secondPart = secondPartList.get(index);
                secondPart.setComponentItem(secondPartRestMaintanenceCom.getComponentItem());
                ArrayList<Maintanence> maintanenceArrayList = secondPart.getMaintanence();
                Iterator<Integer> iterator = maintances.keySet().iterator();
                while (iterator.hasNext()) {
                    Integer key = iterator.next();
                    String value = maintances.get(key);
                    if (key >= maintanenceArrayList.size()) {
                        Maintanence maintanenceItem = new Maintanence();
                        maintanenceItem.setNo(Utils.convertToWithOutPos(key.toString()));
                        maintanenceItem.setItemName(Utils.convertToWithOutPos(value));
                        maintanenceArrayList.add(maintanenceItem);
                    }

                }
                secondPart.setMaintanence(maintanenceArrayList);
                secondPartList.set(index, secondPart);
            }
        }
        return secondPartList;
    }

    private void display(List<RepairSearch> repairSearchList) {

        for (int i = 0; i < repairSearchList.size(); i++) {
            RepairSearch repairSearch = repairSearchList.get(i);
            StringBuilder builder = new StringBuilder();
            builder.append(repairSearch.getVinNo().getWord());
            builder.append("\t\t");
            if (Objects.isNull(repairSearch.getReceptionHours())) {
                System.out.println("===");
            } else {
                builder.append(repairSearch.getReceptionHours().getWord());
            }
            builder.append("\t\t");
            builder.append(repairSearch.getDrivenDistance().getWord());
            builder.append("\t\t");

            if (Objects.nonNull(repairSearch.getSecondPart())) {


                List<String> MaintanenceType = new ArrayList<>();
                List<String> MaintanenceNames = new ArrayList();
                ArrayList<Maintanence> maintanenceArrayList = repairSearch.getSecondPart().getMaintanence();
                if (Objects.nonNull(maintanenceArrayList)) {
                    for (int k = 1; k < maintanenceArrayList.size(); k++) {
                        Maintanence maintanence = maintanenceArrayList.get(k);
                        if (Objects.nonNull(maintanence.getType()) && !MaintanenceType.contains(maintanence.getType().getWord())) {
                            MaintanenceType.add(maintanence.getType().getWord());
                        }
                        if (Objects.nonNull(maintanence.getItemName())) {
                            MaintanenceNames.add(maintanence.getItemName().getWord());
                        }
                    }
                }
                builder.append(String.join(",", MaintanenceType));
                builder.append("\t\t");
                builder.append(String.join(";", MaintanenceNames));
                builder.append("\t\t");
                ArrayList<ComponentItem> componentItemArrayList = repairSearch.getSecondPart().getComponentItem();
                for (int k = 1; k < componentItemArrayList.size(); k++) {

                    ComponentItem componentItem = componentItemArrayList.get(k);
                    if (Objects.nonNull(componentItem.getName())) {
                        builder.append(componentItem.getName().getWord());
                    }
                    builder.append(";");
                }
            }
            System.out.println(builder.toString());
        }
    }

    private List<RepairSearch> appandTextToRepairSearch(List<RepairSearch> repairSearchList, List<List<List<WordWithPos>>> data) {
        List<String> repairSearchStrList = repairSearchList.stream().map(repairSearch -> repairSearch.getNo().getWord()).collect(Collectors.toList());
        List<String> repairSearchIdStrList = repairSearchList.stream().filter(o -> Objects.nonNull(o.getId())).map(repairSearch -> repairSearch.getId().getWord()).collect(Collectors.toList());
        int score = 0;
        int indexJ = -1;
        for (int j = 0; j < data.size(); j++) {
            List<String> targetStrList = data.get(j).stream().map(line -> line.get(1).getWord()).collect(Collectors.toList());
            List<String> mergeTargetStrList = new ArrayList<>();
            mergeTargetStrList.addAll(targetStrList);
            mergeTargetStrList.retainAll(repairSearchStrList);
            if (mergeTargetStrList.size() > score) {
                score = mergeTargetStrList.size();
                indexJ = j;
            }
            if (indexJ != -1 && score > 0) {
                //获取到了矩阵信息
                for (int mi = 0; mi < data.get(indexJ).size(); mi++) {//匹配到矩阵信息
                    List<WordWithPos> withPosList = data.get(indexJ).get(mi);//矩阵中一行
                    if (!repairSearchIdStrList.contains(withPosList.get(0).getWord()) && !"10000".equals(withPosList.get(0).getWord())) {//新增
                        RepairSearch repairSearch = repairSearchList.get(1);//获得表格数据第1行
                        RepairSearch tmpRepairSearch = new RepairSearch();
                        tmpRepairSearch.setId(withPosList.get(0));//第一列肯定是序号
                        tmpRepairSearch.setNo(withPosList.get(1));//第二列肯定是工单号No
                        for (int i = 2; i < withPosList.size(); i++) {
                            WordWithPos wordWithPosTmp = withPosList.get(i);
                            if (repairSearch.getVinNo().getWord().equals(wordWithPosTmp.getWord())) {//这一列是WinNo.
                                tmpRepairSearch.setNumberPlate(withPosList.get(i - 1));//上一列
                                tmpRepairSearch.setVinNo(wordWithPosTmp);
                                tmpRepairSearch.setMajorMaintenanceItems(withPosList.get(i + 1));//下一列
                                tmpRepairSearch.setDrivenDistance(withPosList.get(i + 2));//下一列
                            }

                            //判断当前列是否是时间
                            if (Utils.isDateTime(wordWithPosTmp.getWord())) {
                                tmpRepairSearch.setReceptionHours(Utils.convertToTimeWordWithPos(wordWithPosTmp));
                            }
                        }
                        if (Objects.nonNull(tmpRepairSearch.getNumberPlate()) &&
                                Objects.nonNull(tmpRepairSearch.getVinNo()) &&
                                Objects.nonNull(tmpRepairSearch.getMajorMaintenanceItems()) &&
                                Objects.nonNull(tmpRepairSearch.getDrivenDistance()) &&
                                Objects.nonNull(tmpRepairSearch.getReceptionHours())) {
                            repairSearchList.add(tmpRepairSearch);
                        }
                    } else {//更新

//                        System.out.println("=====");
//                        for(int k = 0 ; k <repairSearchList.size() ; k++){
//                            RepairSearch search = repairSearchList.get(k);
//                            if(search.getId().getWord().equals(withPosList.get(0).getWord())) {
//                                for (int i = 2; i < withPosList.size(); i++) {
//                                    WordWithPos wordWithPosTmp = withPosList.get(i);
//                                    if (search.getVinNo().getWord().equals(wordWithPosTmp.getWord())) {//这一列是WinNo.
//                                        if(Objects.isNull(search.getNumberPlate())) {
//                                            search.setNumberPlate(withPosList.get(i - 1));//上一列
//                                        }
//                                        if(Objects.isNull(search.getMajorMaintenanceItems())) {
//                                            search.setMajorMaintenanceItems(withPosList.get(i + 1));//下一列
//                                        }
//                                        if(Objects.isNull(search.getDrivenDistance())) {
//                                            search.setDrivenDistance(withPosList.get(i + 2));//下一列
//                                        }
//                                    }
//
//                                    //判断当前列是否是时间
//                                    if (Utils.isDateTime(wordWithPosTmp.getWord()) && Objects.isNull(search.getReceptionHours())) {
//                                        search.setReceptionHours(Utils.convertToTimeWordWithPos(wordWithPosTmp));
//                                    }
//                                }
//                                repairSearchList.set(k,search);
//                            }
//                        }
                    }
                }
            }
        }
        return repairSearchList;
    }

    private List<SecondPart> appandOthers(List<SecondPart> secondPartList, List<List<List<WordWithPos>>> data) {
        //先完全匹配零件
        for (int kp = 0; kp < secondPartList.size(); kp++) {
            ArrayList<ComponentItem> componentItemSourceArrayList = secondPartList.get(kp).getComponentItem();
            if (Objects.nonNull(componentItemSourceArrayList) && componentItemSourceArrayList.size() > 1 && !secondPartList.get(kp).isComponentItemDone()) {
                int indexJ = -1;
                for (int j = 0; j < data.size(); j++) {
                    List<List<WordWithPos>> matrixData = data.get(j);//矩阵信息
                    WordWithPos lineFirstWord = data.get(j).get(0).get(0);//矩阵中第一行第一个字符
                    Integer firstLineNumber = Integer.valueOf(lineFirstWord.getWord());

                    if (componentItemSourceArrayList.size() == firstLineNumber && !secondPartList.get(kp).isComponentItemDone()) {
                        for (int mi = 0; mi < matrixData.size(); mi++) {
                            if ("10000".equals(matrixData.get(mi).get(0).getWord())) {
                                secondPartList.get(kp).setComponentItemDone(true);
                            } else {
                                ComponentItem componentItem = new ComponentItem();
                                componentItem.setNo(matrixData.get(mi).get(0));
                                componentItem.setName(matrixData.get(mi).get(1));
                                componentItemSourceArrayList.add(Integer.valueOf(matrixData.get(mi).get(0).getWord()), componentItem);
                            }
                        }
                        secondPartList.get(kp).setComponentItem(componentItemSourceArrayList);
                        indexJ = j;
                    }
                }
                if (indexJ != -1) {
                    data.remove(indexJ);
                }
            }
        }

        //根据内容模糊匹配
        for (int j = 0; j < data.size(); j++) {
            List<String> targetStrList = data.get(j).stream().map(line -> line.get(1).getWord()).collect(Collectors.toList());

            int maintanenceScore = 0;
            int componentScore = 0;


            int indexI = -1;
            for (int i = 0; i < secondPartList.size(); i++) {
                ArrayList<Maintanence> maintanenceSourceArrayList = secondPartList.get(i).getMaintanence();

                if (Objects.nonNull(maintanenceSourceArrayList)) {
                    List<String> maintanenceSouceStrList = maintanenceSourceArrayList.stream().map(maintanence -> {
                        if (Objects.nonNull(maintanence) && Objects.nonNull(maintanence.getItemName())) {
                            return maintanence.getItemName().getWord();
                        } else {
                            return "";
                        }
                    }).collect(Collectors.toList());

                    if (maintanenceSouceStrList.size() > 0) {
                        List<String> mergeTargetStrList = new ArrayList<>();
                        mergeTargetStrList.addAll(targetStrList);
                        mergeTargetStrList.retainAll(maintanenceSouceStrList);
                        if (mergeTargetStrList.size() > maintanenceScore) {
                            maintanenceScore = mergeTargetStrList.size();
                            indexI = i;
                        }
                    }
                }



                ArrayList<ComponentItem> componentItemSourceArrayList = secondPartList.get(i).getComponentItem();
                if (Objects.nonNull(componentItemSourceArrayList)) {
                    List<String> componentItemSourceStrList = componentItemSourceArrayList.stream().map(componentItem -> componentItem.getName().getWord()).collect(Collectors.toList());
                    if (componentItemSourceStrList.size() > 0) {
                        List<String> mergeTargetStrList = new ArrayList<>();
                        mergeTargetStrList.addAll(targetStrList);
                        mergeTargetStrList.retainAll(componentItemSourceStrList);
                        if (mergeTargetStrList.size() > componentScore) {
                            componentScore = mergeTargetStrList.size();
                            indexI = i;
                        }
                    }
                }
            }
            if (indexI != -1) {//找到打分最高的一组second
                if (maintanenceScore > 0) {//匹配到了维修信息
                    ArrayList<Maintanence> maintanenceArrayList = secondPartList.get(indexI).getMaintanence();
                    for (int mi = 0; mi < data.get(j).size(); mi++) {//矩阵信息
                        List<WordWithPos> withPosList = data.get(j).get(mi);//矩阵中一行
                        List<String> maintanceNoWordStrList = maintanenceArrayList.stream().map(maintanence -> maintanence.getNo().getWord()).collect(Collectors.toList());
                        if (!maintanceNoWordStrList.contains(withPosList.get(0).getWord()) && !"10000".equals(withPosList.get(0).getWord())) {
                            Maintanence maintanenceItem = new Maintanence();
                            maintanenceItem.setNo(withPosList.get(0));
                            maintanenceItem.setItemName(withPosList.get(1));
                            maintanenceArrayList.add(Integer.valueOf(withPosList.get(0).getWord()), maintanenceItem);
                        }
                    }
                    secondPartList.get(indexI).setMaintanence(maintanenceArrayList);
                } else if (componentScore > 0) {//匹配到零件
                    ArrayList<ComponentItem> componentItemArrayList = secondPartList.get(indexI).getComponentItem();
                    for (int mi = 0; mi < data.get(j).size(); mi++) {//矩阵信息
                        List<WordWithPos> withPosList = data.get(j).get(mi);//矩阵中一行
                        List<String> comNoWordStrList = componentItemArrayList.stream().map(componentItem1 -> componentItem1.getNo().getWord()).collect(Collectors.toList());
                        if (!comNoWordStrList.contains(withPosList.get(0).getWord()) && !"10000".equals(withPosList.get(0).getWord())) {
                            ComponentItem componentItem = new ComponentItem();
                            componentItem.setNo(withPosList.get(0));
                            componentItem.setName(withPosList.get(1));
                            componentItemArrayList.add(Integer.valueOf(withPosList.get(0).getWord()), componentItem);
                        }
                    }
                    secondPartList.get(indexI).setComponentItem(componentItemArrayList);
                }
            } else {//未找到匹配的内容
                System.out.println("未找到匹配的内容");

            }
        }

        return secondPartList;
    }

}
