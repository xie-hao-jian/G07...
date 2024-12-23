package PsychologicalCounselingPlatform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId
    private String id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String qq;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 