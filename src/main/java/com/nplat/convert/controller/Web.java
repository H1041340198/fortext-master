package com.nplat.convert.controller;

import com.nplat.convert.config.ApiMsgEnum;
import com.nplat.convert.config.BaseResponse;
import com.nplat.convert.config.ServiceException;
import com.nplat.convert.config.WxUser;
import com.nplat.convert.entity.entity.User;
import com.nplat.convert.entity.request.UserLoginRequest;
import com.nplat.convert.service.CacheService;
import com.nplat.convert.service.IMService;
import com.nplat.convert.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@Controller
public class Web {
    @Autowired
    private UserService userService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private IMService toyotaService;




    //用户登录
    @PostMapping(path = "/common/login")
    @ResponseBody
    public BaseResponse loginWithPhonePassword(@RequestBody UserLoginRequest request) {
        BaseResponse response = new BaseResponse();
        try {
            log.info("login password=" + request.getPassword());
            String email = request.getUser();
            String password = request.getPassword();
            User currentUser = userService.getInfoByEmailAndPassword(email, password);
            if (Objects.nonNull(currentUser)) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(currentUser.getId());
                buffer.append(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 1 << 4));
                buffer.append(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 1 << 4));
                buffer.append(1);//普通用户
                HashMap data = new HashMap<>();
                data.put("token", buffer);
                cacheService.set(currentUser.getId(), buffer.toString(), "", System.currentTimeMillis() + 24 * 60 * 60 * 1000);
                response.setData(data);
                return response;
            } else {
                response.setMsgEnum(ApiMsgEnum.USER_NOT_EXISTS);
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setData(e.getMessage());
            response.setMsgEnum(ApiMsgEnum.INTERNAL_SERVER_ERROR);
            return response;
        }
    }






    @GetMapping(value = "/common/file/json")
    @ResponseBody
    public BaseResponse getFileJson(@WxUser User user,
                                    @RequestParam(value = "serializableNo") String serializableNo) throws ServiceException {
        BaseResponse response = new BaseResponse();
        response.setData(toyotaService.getFileToyotaData(serializableNo));
        return response;
    }



    @GetMapping(value = "/thirdpart/data/json/v1")
    @ResponseBody
    public BaseResponse thirdPartGetFileJson(@WxUser User user,
                                             @RequestParam(value = "serializableNo") String serializableNo) throws ServiceException {
        BaseResponse response = new BaseResponse();
        response.setData(toyotaService.getFileToyotaData(serializableNo));
        return response;
    }



}
