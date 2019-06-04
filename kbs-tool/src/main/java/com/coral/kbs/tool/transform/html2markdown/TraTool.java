package com.coral.kbs.tool.transform.html2markdown;

import java.io.IOException;

public class TraTool {
    public static void main(String[] args) throws IOException {
        tra("/Users/wuhao/data/code/github/book/guide-doc/1.language(编程语言)/1.java(server端)/6.framework(框架)/0.源码解析/2.dubbo/11.核心流程一览.tny",
                "/Users/wuhao/data/code/github/book/guide-doc/1.language(编程语言)/1.java(server端)/6.framework(框架)/0.源码解析/2.dubbo/md/核心流程一览.md");
    }
    public static void tra(String htmlFile, String mdFile) throws IOException {
        String content = FilesUtil.readAll(htmlFile);
        String contentMd = HTML2Md.convertHtml(content, "utf-8");
        FilesUtil.writeFile(mdFile, contentMd);
    }
}
