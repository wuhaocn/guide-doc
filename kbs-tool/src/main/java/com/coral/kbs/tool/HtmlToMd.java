package com.coral.kbs.tool;

import com.coral.kbs.tool.sort.DocIndexUtils;
import com.coral.kbs.tool.transform.html2markdown.HTML2Md;
import com.coral.kbs.tool.transform.html2markdown.TraTool;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class HtmlToMd {
    public static void main(String[] args) throws IOException {
        URL url = new URL("https://www.jianshu.com/p/f777abb7b251");
        HTML2Md.convert(url, 30000);
    }

}
