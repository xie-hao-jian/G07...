package PsychologicalCounselingPlatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import PsychologicalCounselingPlatform.entity.Book;
import PsychologicalCounselingPlatform.mapper.BookMapper;
import PsychologicalCounselingPlatform.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {
    
    @Override
    public Book getTodayRecommend() {
        long count = count();
        if (count == 0) {
            return null;
        }
        
        int randomIndex = new Random().nextInt((int) count);
        Page<Book> page = new Page<>(randomIndex + 1, 1);
        page = page(page);
        
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    @Override
    public IPage<Book> getHistoryRecommends(int page) {
        page = Math.max(1, page);
        final int pageSize = 3;
        
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Book::getPublishDate);
        
        Page<Book> pageParam = new Page<>(page, pageSize);
        return page(pageParam, queryWrapper);
    }

    @Override
    public List<Book> searchBooks(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Book::getName, keyword)
                .or()
                .like(Book::getAuthor, keyword)
                .orderByDesc(Book::getPublishDate)
                .last("LIMIT 10");
        
        return list(queryWrapper);
    }
} 