package com.star.aicodehelper.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value ="chat_message")
@Data
public class ChatMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long memoryId;

    private String assistantType;

    private String role; // "user" or "ai"

    private String content;

    private Date createTime;
    
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Boolean isUser;
}
