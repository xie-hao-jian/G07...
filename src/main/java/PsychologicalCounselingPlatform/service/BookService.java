package PsychologicalCounselingPlatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import PsychologicalCounselingPlatform.entity.Book;
import java.util.List;

public interface BookService extends IService<Book> {
    Book getTodayRecommend();
    IPage<Book> getHistoryRecommends(int page); 
    List<Book> searchBooks(String keyword);
}