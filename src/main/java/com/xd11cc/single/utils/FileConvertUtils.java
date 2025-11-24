//package com.xd11cc.single.utils;
//
//import com.spire.pdf.FileFormat;
//import com.spire.pdf.PdfDocument;
//import lombok.extern.slf4j.Slf4j;
//
///**
// * @author xd11cc
// * @date 2025-09-15 17:54:08
// */
//@Slf4j
//public class FileConvertUtils {
//
//    public static void PdfToWord(String pdfPath, String wordPath) {
//        try {
//            // 加载PDF文件
//            PdfDocument pdf = new PdfDocument();
//            pdf.loadFromFile(pdfPath);
//
//            // 将PDF保存为Word格式
//            pdf.saveToFile(wordPath, FileFormat.DOCX);
//
//            // 关闭文档
//            pdf.close();
//
//            log.info("PDF转换为Word成功！输出路径：" + wordPath);
//        } catch (Exception e) {
//            log.error("转换失败：" + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}
