package com.nplat.convert.service;


import com.alibaba.fastjson.JSONObject;
import com.nplat.convert.config.ServiceException;
import com.nplat.convert.entity.domain.KeyWords;
import com.nplat.convert.entity.domain.WordWithPos;
import com.nplat.convert.entity.entity.FileEntity;
import com.nplat.convert.mapper.FileEntityMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 用户Service单元测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Test001 {
    @Autowired
    private FileEntityMapper fileEntityMapper;
    @Autowired
    private TextAnService textAnService;
    @Autowired
    private ToyotaService toyotaService;
    @Autowired
    private PicDisplayTable picDisplayTable;
    @Autowired
    private FileEntityService fileEntityService;
    @Autowired
    private JudgeParam judgeParam;
    @Autowired
    private StrToMatrixService strToMatrixService;
    @Autowired
    private IMService imService;

    @Autowired
    private LinKeService linKeService;

    @Test
    public void strToMatrix() throws ServiceException {


        imService.getFileToyotaData("LB37622ZXMC405507");
//        ArrayList<HashMap<Integer, WordWithPos>> newData = picDisplayTable.displayFileContent(fileEntity.getConvertContent());
    }


    @Test
    public void test1() {
        try {
// 路径

            File dir = new File("E:\\tmp\\图\\领克\\L6T7824Z3NZ350691");
            if (dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    System.out.println(dir.getPath() + "\\" + file.getName());
                    File ttm = new File(dir.getPath() + "\\" + file.getName());
                    FileInputStream inputStream = new FileInputStream(ttm);
                    String data = imService.uploadFile(inputStream);
                    FileEntity fileEntity = new FileEntity();
                    fileEntity.setSerializableNo("L6T7824Z3NZ350691");
                    fileEntity.setFileName(ttm.getName());
                    fileEntity.setConvertContent(data);
                    fileEntity.setStatus(1);
                    fileEntity.setType(3);
                    fileEntity.setCreateTime(new Date());
                    fileEntityService.insert(fileEntity);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void pay1() {
        try {
            ///HashMap data =  toyotaService.getFileData(null,1,5,"id","desc");
            //HashMap data = toyotaService.getOneFile(2L);
            HashMap data = imService.getFileToyotaData("LB3775228JL613234");
//            toyotaService.deleteFile(1L);
            System.out.println(JSONObject.toJSONString(data));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
