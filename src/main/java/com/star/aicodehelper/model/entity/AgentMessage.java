package com.star.aicodehelper.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value ="agent_message")
@Data
public class AgentMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long sessionId;

    private String role; // "user" or "agent"

    private String content;

    private String thought;

    private String action;

    private String actionInput;

    private String observation;

    private Date createTime;
}
