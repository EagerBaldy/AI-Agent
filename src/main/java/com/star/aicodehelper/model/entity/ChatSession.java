package com.star.aicodehelper.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value ="chat_session")
@Data
public class ChatSession implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String assistantType;

    private String name;

    private Date createTime;

    private Date updateTime;
}
