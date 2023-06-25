package com.nplat.convert.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nplat.convert.entity.entity.FileEntity;
import com.nplat.convert.mapper.FileEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@Slf4j
public class FileEntityService extends ServiceImpl<FileEntityMapper, FileEntity> {
    @Async("loggingExecutor")
    @DS("master")
    public void insert(FileEntity globalAuditLog) {
        this.save(globalAuditLog);
    }

    @Async("loggingExecutor")
    @DS("master")
    public void insert(List<FileEntity> fileEntities) {
        fileEntities.forEach(item -> log.info(item.toString()));
        this.saveBatch(fileEntities);
    }

    @Transactional
    @DS("master")
    public Boolean syncInsert(FileEntity fileEntity) {
        return this.save(fileEntity);
    }

    @Transactional
    @DS("master")
    public Boolean syncInsert(List<FileEntity> fileEntities) {
        return this.saveBatch(fileEntities);
    }



}