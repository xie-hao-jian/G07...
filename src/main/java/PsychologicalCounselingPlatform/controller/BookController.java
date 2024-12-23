package PsychologicalCounselingPlatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import PsychologicalCounselingPlatform.exception.ApiResponse;
import PsychologicalCounselingPlatform.entity.Book;
import PsychologicalCounselingPlatform.service.BookService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    
    @Resource
    private BookService bookService;
    
    @GetMapping("/today-recommend")
    public ApiResponse<Book> getTodayRecommend() {
        return ApiResponse.success(bookService.getTodayRecommend());
    }
    
    @GetMapping("/history-recommends")
    public ApiResponse<IPage<Book>> getHistoryRecommends(@RequestParam(defaultValue = "1") int page) {
        return ApiResponse.success(bookService.getHistoryRecommends(page));
}
    
    @GetMapping("/search")
    public ApiResponse<List<Book>> searchBooks(@RequestParam String keyword) {
        return ApiResponse.success(bookService.searchBooks(keyword));
    }
} 