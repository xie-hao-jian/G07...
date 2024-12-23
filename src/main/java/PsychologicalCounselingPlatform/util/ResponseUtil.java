package PsychologicalCounselingPlatform.util;

import PsychologicalCounselingPlatform.exception.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(code);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(code, message)));
    }

    public static void writeSuccessResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.success(data)));
    }
} 