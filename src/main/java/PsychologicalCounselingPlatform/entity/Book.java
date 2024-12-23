package PsychologicalCounselingPlatform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("book")
public class Book {
    @TableId
    private Integer id;
    private String name;
    private String author;
    private String publishDate;
    private String description;
    private String coverUrl;
    
    @TableField(exist = false)
    private String[] tags;
} 