package com.chf.Utils;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author:chf
 * @description: 解析html文档的工具类
 * @date:2019/3/11
 **/
public class JsoupUtil {

    public static void valiHtmll  (String html) {
        Document doc = Jsoup.parse(html);
        Elements boxCon = doc.getElementsByClass("box_con");
        Document boxConDoc = Jsoup.parse(boxCon.toString());
        Elements module = boxConDoc.getElementsByClass("movielist");
        Document moduleDoc = Jsoup.parse(module.toString());
        Elements img=moduleDoc.getElementsByTag("img");
        Elements aUrl=moduleDoc.select(".pic");

        for (Element ele:img) {
            String imgUrl=ele.attr("src");
            System.out.println("解析拿到所有的url地址："+imgUrl);
            String fileName=imgUrl.replace("/","");
            DownloadUtil.readFileFromWEBtoNative(imgUrl,fileName.substring(fileName.length()-10));
        }

        for (Element ele:aUrl) {
            String url=ele.attr("href");
            System.out.println("http://sou64.com"+url);
        }
    }

    public static void main(String[] args) throws Exception{
        String url = "https://www.zuixin2015.com/player/index52213.html?52213-0-0";

        // HtmlUnit 模拟浏览器，获取完整的HTML
        final WebClient webClient = new WebClient(BrowserVersion.CHROME);//新建一个模拟谷歌Chrome浏览器的浏览器客户端对象
        webClient.getOptions().setThrowExceptionOnScriptError(false);//当JS执行出错的时候是否抛出异常, 这里选择不需要
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);//当HTTP的状态非200时是否抛出异常, 这里选择不需要
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setCssEnabled(false);//是否启用CSS, 因为不需要展现页面, 所以不需要启用
        webClient.getOptions().setJavaScriptEnabled(true); //很重要，启用JS
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());//很重要，设置支持AJAX

        HtmlPage page = null;
        try {
            page = webClient.getPage(url);//尝试加载上面图片例子给出的网页
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            webClient.close();
        }

        webClient.waitForBackgroundJavaScript(30000);//异步JS执行需要耗时,所以这里线程要阻塞30秒,等待异步JS执行结束

        String pageXml = page.asXml();//直接将加载完成的页面转换成xml格式的字符串


        Document doc = Jsoup.parse(pageXml);
        Elements player=doc.getElementsByTag("iframe");
        System.out.println(pageXml);
    }
}
