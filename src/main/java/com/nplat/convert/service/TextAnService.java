package com.nplat.convert.service;

import com.aliyun.sdk.service.ocr_api20210707.AsyncClient;
import com.aliyun.sdk.service.ocr_api20210707.models.RecognizeTableOcrRequest;
import com.aliyun.sdk.service.ocr_api20210707.models.RecognizeTableOcrResponse;
import com.aliyun.sdk.service.ocr_api20210707.models.RecognizeTableOcrResponseBody;
import com.nplat.convert.config.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class TextAnService {
    @Value("${AccessKey.ID}")
    private String accessKeyId;
    @Value("${AccessKey.Secret}")
    private String accessKeySecret;


    @Autowired
    @Qualifier("getAsyncClient")
    private AsyncClient asyncClient;



    public String convertFileToStr(InputStream in) throws ServiceException, ExecutionException, InterruptedException {
        RecognizeTableOcrRequest tableOcrRequest = RecognizeTableOcrRequest.builder().body(in).build();
        CompletableFuture<RecognizeTableOcrResponse> responseCompletableFuture = asyncClient.recognizeTableOcr(tableOcrRequest);
        RecognizeTableOcrResponse resp = responseCompletableFuture.get();

        RecognizeTableOcrResponseBody recognizeTableOcrResponseBody = resp.getBody();
        if (Objects.isNull(recognizeTableOcrResponseBody.getCode())) {//code为null，则返回正常
            return recognizeTableOcrResponseBody.getData();
        } else {
            throw new ServiceException("code:"+recognizeTableOcrResponseBody.getCode() +"\nmessage:"+recognizeTableOcrResponseBody.getMessage());
        }


    }




}
