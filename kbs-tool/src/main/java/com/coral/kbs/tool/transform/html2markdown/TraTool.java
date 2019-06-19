package com.coral.kbs.tool.transform.html2markdown;

import java.io.IOException;

public class TraTool {
    public static void main(String[] args) throws IOException {
        tra("/Users/wuhao/data/code/github/book/guide-doc/1.language(编程语言)/1.java(server端)/1.base(基础知识)/19.浅析java中的语法糖.tny",
                "/Users/wuhao/data/code/github/book/guide-doc/1.language(编程语言)/1.java(server端)/1.base(基础知识)/19.syntacticsugar(语法糖).md");
    }
    public static void tra(String htmlFile, String mdFile) throws IOException {
        String content = FilesUtil.readAll(htmlFile);
        String contentMd = HTML2Md.convertHtml(content, "utf-8");
        FilesUtil.writeFile(mdFile, contentMd);
    }
}
