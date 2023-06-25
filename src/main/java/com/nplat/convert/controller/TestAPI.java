package com.nplat.convert.controller;

import com.nplat.convert.config.ApiMsgEnum;
import com.nplat.convert.config.BaseResponse;
import com.nplat.convert.config.ServiceException;
import com.nplat.convert.config.WxUser;
import com.nplat.convert.entity.domain.KeyWords;
import com.nplat.convert.entity.entity.User;
import com.nplat.convert.entity.request.FileUploadIdRequest;
import com.nplat.convert.service.IMService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RestController
public class TestAPI {

    @Autowired
    private IMService toyotaService;

    @GetMapping(value = "/common/files")
    @ResponseBody
    public BaseResponse listFiles(@WxUser User user,
                                  @RequestParam(value = "serializableNo", required = false) String serializableNo,
                                  @RequestParam("page") Integer page,
                                  @RequestParam("size") Integer size) {
        BaseResponse response = new BaseResponse();

        HashMap data = toyotaService.getFileData(serializableNo, page, size, "id", "desc");
        response.setData(data);
        return response;
    }


    @GetMapping(value = "/common/file/type")
    @ResponseBody
    public BaseResponse getFileOne() {
        BaseResponse response = new BaseResponse();
        response.setData(toyotaService.fileTypes());
        return response;
    }


    @GetMapping(value = "/common/file/one")
    @ResponseBody
    public BaseResponse getFileOne(@WxUser User user,
                                   @RequestParam("id") Long id) throws ServiceException {
        BaseResponse response = new BaseResponse();

        HashMap data = toyotaService.getOneFile(id);
        response.setData(data);
        return response;
    }

    @GetMapping(value = "/common/analyze/files")
    @ResponseBody
    public BaseResponse searchFiles(@WxUser User user,
                                    @RequestParam(value = "serializableNo") String serializableNo) {
        BaseResponse response = new BaseResponse();
        response.setData(toyotaService.getAnalyzeFiles(serializableNo));
        return response;
    }



    @PostMapping("/upload")
    @ResponseBody
    public BaseResponse handleFileUpload1(@WxUser User user,
                                          @RequestParam("serializableNo") String serializableNo,
                                          @RequestParam("type") Integer type,
                                          @RequestParam("files") MultipartFile[] files) throws IOException, InterruptedException, ExecutionException, ServiceException {
        BaseResponse response = new BaseResponse();
        KeyWords keyWords =KeyWords.findByCode(type);
        if (Objects.isNull(serializableNo) || Objects.isNull(keyWords)) {
            response.setMsgEnum(ApiMsgEnum.PARAM_FAILED);
            return response;
        }
        List<HashMap> data = toyotaService.uploadFiles(serializableNo, files,keyWords);
        response.setData(data);

        return response;
    }

    @GetMapping("/common/download")
    public void downloadFile(
            @RequestParam("serializableNo") String serializableNo,

            HttpServletResponse response) throws IOException, ServiceException {
        // 获取文件路径
//        String filePath = "E:/tmp/ip.txt";

        // 从文件系统或其他位置获取文件输入流
//        InputStream inputStream = new FileInputStream(filePath);


        System.out.println(serializableNo);
        Workbook workbook = toyotaService.convertWorkbook(serializableNo);
        if (Objects.isNull(workbook)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(200);
            response.getWriter().append("输入参数serializableNo为空或输入错误，文件类型type不存在");
            response.getWriter().flush();
            response.getWriter().close();

        } else {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + serializableNo + ".xlsx");
            // 将文件内容写入响应输出流
            // 将Excel内容写入响应输出流
            workbook.write(response.getOutputStream());
            workbook.close();
            response.flushBuffer();
        }


    }




    @PostMapping("/common/file/up")
    @ResponseBody
    public BaseResponse handleFileUpload(@WxUser User user,
                                         @RequestParam(value = "id") Long id,
                                         @RequestParam("file") MultipartFile file) throws IOException, InterruptedException, ExecutionException, ServiceException {
        BaseResponse response = new BaseResponse();
        toyotaService.updateFileData(id, file);
        return response;
    }

    @PostMapping("/common/file/delete")
    @ResponseBody
    public BaseResponse deleteHandleFile(@WxUser User user,
                                         @RequestBody FileUploadIdRequest fileUploadIdRequest) throws ServiceException {
        BaseResponse response = new BaseResponse();
        toyotaService.deleteFile(fileUploadIdRequest.getId());
        return response;
    }
}
