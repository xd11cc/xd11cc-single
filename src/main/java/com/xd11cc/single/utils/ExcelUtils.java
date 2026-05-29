package com.xd11cc.single.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author xd11cc
 * @date 2026-05-29
 */
public class ExcelUtils {

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName,
                                  Class<T> clazz, List<T> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(sheetName)
                .doWrite(data);
    }

    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        return EasyExcel.read(inputStream)
                .head(clazz)
                .sheet()
                .doReadSync();
    }
}
