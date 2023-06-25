package com.nplat.convert.entity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("file_entity")
public class FileEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer type;
    private String serializableNo;
    private String fileName;
    private String convertContent;
    private Integer status;
    private Date createTime;

}
