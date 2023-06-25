package com.nplat.convert.service;

import com.nplat.convert.config.ServiceException;
import com.nplat.convert.entity.domain.ComponentItem;
import com.nplat.convert.entity.domain.KeyWords;
import com.nplat.convert.entity.domain.Maintanence;
import com.nplat.convert.entity.domain.RepairSearch;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.entity.entity.FileEntity;
import com.nplat.convert.mapper.FileEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IMService {
    @Autowired
    private ToyotaService toyotaService;

    @Autowired
    private LinKeService linKeService;

    @Autowired
    private QiYaService qiYaService;

    @Autowired
    private GuangQiChuanQiService guangQiChuanQiService;

    @Autowired
    private YiQiFengTianService yiQiFengTianService;

    @Autowired
    private FileEntityMapper fileEntityMapper;

    @Autowired
    private FileEntityService fileEntityService;

    @Autowired
    private TextAnService textAnService;

    @Autowired
    private PicDisplayTable picDisplayTable;

    public List<HashMap> fileTypes() {
        List<HashMap> data = new ArrayList<>();
        for (KeyWords words : KeyWords.values()) {
            HashMap map = new HashMap();
            map.put(words.getCode(), words.getMessage());
            data.add(map);
        }
        return data;
    }

    public List<HashMap> uploadFiles(String serializableNo, MultipartFile[] multipartFiles, KeyWords keyWords) throws IOException, InterruptedException, ExecutionException, ServiceException {
        List<Long> ids = fileEntityMapper.getFileEntityIdBySerializableNo(serializableNo);
        if (ids.size() > 0) {
            throw new ServiceException("序列号:" + serializableNo + " 已存在，可在列表页进行修改和删除");
        }
        List<HashMap> dataList = new ArrayList<>();
        for (MultipartFile file : multipartFiles) {
            HashMap picData = new HashMap();
            String data = uploadFile(serializableNo, file, keyWords.getCode());
            log.info("识别结果：{}", data);
            ArrayList<HashMap<Integer, WordWithPos>> newData = picDisplayTable.displayFileContent(data);
            picData.put("file_name", file.getOriginalFilename());
            picData.put("file_data", newData);
            dataList.add(picData);
        }
        return dataList;
    }

    public List<HashMap> getAnalyzeFiles(String serializableNo) {
        List<FileEntity> fileEntityList = fileEntityMapper.getFileEntityBySerializableNo(serializableNo);
        List<HashMap> dataList = new ArrayList<>();
        for (FileEntity file : fileEntityList) {
            HashMap picData = new HashMap();
            ArrayList<HashMap<Integer, WordWithPos>> newData = picDisplayTable.displayFileContent(file.getConvertContent());
            picData.put("file_name", file.getFileName());
            picData.put("file_data", newData);
            dataList.add(picData);
        }
        return dataList;
    }

    public void deleteFile(Long id) throws ServiceException {
        FileEntity fileEntity = fileEntityMapper.selectById(id);
        if (Objects.isNull(fileEntity)) {
            throw new ServiceException("id不存在");
        } else {
            FileEntity newFileEntity = new FileEntity();
            newFileEntity.setId(fileEntity.getId());
            newFileEntity.setStatus(0);
            fileEntityMapper.updateById(newFileEntity);
        }

    }

    public HashMap getOneFile(Long id) throws ServiceException {
        FileEntity fileEntity = fileEntityMapper.selectById(id);
        if (Objects.isNull(fileEntity)) {
            throw new ServiceException("id不存在");
        } else {
            return convertFileEntity(fileEntity);
        }

    }

    public void updateFileData(Long id, MultipartFile file) throws IOException, InterruptedException, ExecutionException, ServiceException {
        FileEntity fileEntity = fileEntityMapper.selectById(id);
        if (Objects.isNull(fileEntity)) {
            throw new ServiceException("id不存在");
        } else {
            fileEntity.setFileName(file.getOriginalFilename());
            String data = textAnService.convertFileToStr(file.getInputStream());
            fileEntity.setConvertContent(data);
            fileEntity.setStatus(1);
            fileEntity.setCreateTime(new Date());
        }
    }

    public HashMap getFileData(String serializableNo, Integer page, Integer size, String orderColumn, String order) {

        HashMap data = new HashMap();
        HashMap pageInfo = new HashMap();
        page = (page == null || page < 1) ? 1 : page;
        pageInfo.put("size", size);
        pageInfo.put("page", page);

        Integer userCount = fileEntityMapper.getUserCount(serializableNo);
        pageInfo.put("total_page", (userCount / size) + 1);
        pageInfo.put("total", userCount);
        List<HashMap> records = fileEntityMapper.getUserInfoList(serializableNo, orderColumn, order, (page - 1) * size, size).stream().map(user -> {
            return convertFileEntity(user);

        }).collect(Collectors.toList());
        data.put("pageInfo", pageInfo);
        data.put("records", records);
        return data;

    }

    private HashMap convertFileEntity(FileEntity fileEntity) {
        HashMap entity = new HashMap();
        entity.put("id", fileEntity.getId());
        entity.put("type", KeyWords.findByCode(fileEntity.getType()).getMessage());
        entity.put("file_name", fileEntity.getFileName());
        entity.put("serializableNo", fileEntity.getSerializableNo());
        ArrayList<HashMap<Integer, WordWithPos>> newData = picDisplayTable.displayFileContent(fileEntity.getConvertContent());
        entity.put("file_data", newData);
        entity.put("create_time", fileEntity.getCreateTime());
        return entity;


    }

    public String uploadFile(String serializableNo, MultipartFile file, Integer type) throws IOException, InterruptedException, ExecutionException, ServiceException {
        String data = textAnService.convertFileToStr(file.getInputStream());
        FileEntity fileEntity = new FileEntity();
        fileEntity.setType(type);
        fileEntity.setSerializableNo(serializableNo);
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setConvertContent(data);
        fileEntity.setStatus(1);
        fileEntity.setCreateTime(new Date());
        fileEntityService.insert(fileEntity);

        return data;
    }

    public String uploadFile(InputStream inputStream) throws InterruptedException, ExecutionException, ServiceException {
        String data = textAnService.convertFileToStr(inputStream);
        return data;
    }

    private List<HashMap> repairSearchListToToyotaData(List<RepairSearch> repairSearchList) {
        List<HashMap> hashMapList = new ArrayList<>();
        HashMap title = new HashMap();
        RepairSearch searchTiele = repairSearchList.get(0);
        title.put("1", searchTiele.getVinNo().getWord());
        title.put("2", searchTiele.getReceptionHours().getWord());
        title.put("3", searchTiele.getDrivenDistance().getWord());
        title.put("4", "维修类型");
        title.put("5", "项目描述");
        title.put("6", "更换材料");
        hashMapList.add(title);
        for (int i = 1; i < repairSearchList.size(); i++) {
            HashMap data = new HashMap();
            RepairSearch repairSearch = repairSearchList.get(i);
            if (Objects.nonNull(repairSearch.getVinNo())) {
                data.put("1", repairSearch.getVinNo().getWord());
            } else {
                data.put("1", "-");
            }
            if (Objects.nonNull(repairSearch.getReceptionHours())) {
                data.put("2", repairSearch.getReceptionHours().getWord());
            } else {
                data.put("2", "-");
            }
            if (Objects.isNull(repairSearch.getDrivenDistance())) {
                data.put("3", "-");
            } else {
                data.put("3", repairSearch.getDrivenDistance().getWord());
            }
            data.put("4", "");
            data.put("5", "");
            data.put("6", "");
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
                data.put("4", String.join(",", MaintanenceType));
                data.put("5", String.join(";", MaintanenceNames));
                ArrayList<ComponentItem> componentItemArrayList = repairSearch.getSecondPart().getComponentItem();
                if (Objects.nonNull(componentItemArrayList)) {
                    StringBuilder builder = new StringBuilder();
                    for (int k = 1; k < componentItemArrayList.size(); k++) {
                        ComponentItem componentItem = componentItemArrayList.get(k);
                        builder.append(componentItem.getName().getWord());
                        builder.append(";");
                    }
                    data.put("6", builder.toString());
                }

            }
            hashMapList.add(data);
        }
        return hashMapList;
    }

    public Workbook convertWorkbook(String serializableNo) throws ServiceException {
        List<FileEntity> fileEntityList = fileEntityMapper.getFileEntityBySerializableNo(serializableNo);
        if (fileEntityList.size() == 0) {
            throw new ServiceException("没有数据");
        }
        HashMap data = toyotaService.getConvert(fileEntityList);
        List<RepairSearch> repairSearchList = (List<RepairSearch>) data.get("data");
        return writeExcel(repairSearchList);
    }

    private Workbook writeExcel(List<RepairSearch> repairSearchList) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();

        Sheet sheet = workbook.createSheet("_" + System.currentTimeMillis());
        Row title = sheet.createRow(0);

        Cell vinNo = title.createCell(0);

        vinNo.setCellValue("VIN 码");
        Cell receptionHours = title.createCell(1);
        receptionHours.setCellValue("日期");
        Cell drivenDistance = title.createCell(2);
        drivenDistance.setCellValue("行驶里程");
        Cell maintanenceType = title.createCell(3);
        maintanenceType.setCellValue("维修类型");
        Cell maintanenceItem = title.createCell(4);
        maintanenceItem.setCellValue("项目描述");
        Cell componenet = title.createCell(5);
        componenet.setCellValue("更换材料");

        for (int i = 1; i < repairSearchList.size(); i++) {
            Row raw = sheet.createRow(i);
            Cell _vinNo = raw.createCell(0);
            Cell _receptionHours = raw.createCell(1);
            Cell _drivenDistance = raw.createCell(2);
            Cell _maintanenceType = raw.createCell(3);
            Cell _maintanenceItem = raw.createCell(4);
            Cell _componenet = raw.createCell(5);
            RepairSearch repairSearch = repairSearchList.get(i);

            _vinNo.setCellValue(repairSearch.getVinNo().getWord());
            _receptionHours.setCellValue(repairSearch.getReceptionHours().getWord());
            _drivenDistance.setCellValue(repairSearch.getDrivenDistance().getWord());

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
                _maintanenceType.setCellValue(String.join(",", MaintanenceType));
                _maintanenceItem.setCellValue(String.join(";", MaintanenceNames));

                StringBuilder componentItems = new StringBuilder();
                ArrayList<ComponentItem> componentItemArrayList = repairSearch.getSecondPart().getComponentItem();
                for (int k = 1; k < componentItemArrayList.size(); k++) {
                    ComponentItem componentItem = componentItemArrayList.get(k);
                    componentItems.append(componentItem.getName().getWord());
                    componentItems.append(";");
                }
                _componenet.setCellValue(componentItems.toString());
            }
        }

        return workbook;
    }

    public HashMap getFileToyotaData(String serializableNo) throws ServiceException {
        HashMap data = new HashMap();
        List<FileEntity> fileEntityList = fileEntityMapper.getFileEntityBySerializableNo(serializableNo);
        Integer type = -1;
        if (fileEntityList.size() == 0) {
            throw new ServiceException("没有数据");
        }else {
            type = fileEntityList.get(0).getType();
        }
        if(type == KeyWords.GUANG_QI_FENG_TIAN.getCode() || type == KeyWords.GUANG_QI_SAN_LING.getCode()) {

            HashMap tmp = toyotaService.getConvert(fileEntityList);
            List<RepairSearch> repairSearchList = (List<RepairSearch>) tmp.get("data");
            List<HashMap> hashMapList = repairSearchListToToyotaData(repairSearchList);
            data.put("data", hashMapList);
            data.put("failedFiles", tmp.getOrDefault("failedFiles", new ArrayList<>()));
            data.put("analiseFailds", tmp.getOrDefault("analiseFailds", new ArrayList<>()));

        }else  if( type == KeyWords.LING_KE.getCode()) {
            return linKeService.getConvert(fileEntityList,serializableNo);
        }else  if( type == KeyWords.QI_YA.getCode()) {
            return qiYaService.getConvert(fileEntityList,serializableNo);
        }else  if( type == KeyWords.GUANG_QI_CHUAN_QI.getCode()) {
            return guangQiChuanQiService.getConvert(fileEntityList);
        }else  if( type == KeyWords.YI_QI_FENG_TIAN.getCode()) {
            return yiQiFengTianService.getConvert(fileEntityList);
        }
        return data;
    }

}
