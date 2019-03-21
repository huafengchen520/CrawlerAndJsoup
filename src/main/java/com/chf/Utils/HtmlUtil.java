package com.chf.Utils;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @author:chf
 * @description:模拟浏览器执行各种操作
 * @date:2019/3/20
 **/
public class HtmlUtil {
        /*
         * 启动JS
         */
        public static WebClient iniParam_Js() {
            final WebClient webClient = new WebClient(BrowserVersion.CHROME);
            // 启动JS
            webClient.getOptions().setJavaScriptEnabled(true);
            //将ajax解析设为可用
            webClient.getOptions().setActiveXNative(true);
            //设置Ajax的解析器
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            // 禁止CSS
            webClient.getOptions().setCssEnabled(false);
            // 启动客户端重定向
            webClient.getOptions().setRedirectEnabled(true);
            // JS遇到问题时，不抛出异常
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            // 设置超时
            webClient.getOptions().setTimeout(10000);
            //禁止下载照片
            webClient.getOptions().setDownloadImages(false);
            return webClient;
        }

        /*
         * 禁止JS
         */
        public static WebClient iniParam_NoJs() {
            final WebClient webClient = new WebClient(BrowserVersion.CHROME);
            // 禁止JS
            webClient.getOptions().setJavaScriptEnabled(false);
            // 禁止CSS
            webClient.getOptions().setCssEnabled(false);
            // 将返回错误状态码错误设置为false
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            // 启动客户端重定向
            webClient.getOptions().setRedirectEnabled(true);
            // 设置超时
            webClient.getOptions().setTimeout(5000);
            //禁止下载照片
            webClient.getOptions().setDownloadImages(false);
            return webClient;
        }

        /**
         * 根据url获取页面，这里需要加载JS
         * @param url
         * @return 网页
         * @throws FailingHttpStatusCodeException
         * @throws MalformedURLException
         * @throws IOException
         */
        public static HtmlPage getPage_Js(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException{
            final WebClient webClient = iniParam_Js();
            HtmlPage page = webClient.getPage(url);
            //webClient.waitForBackgroundJavaScriptStartingBefore(5000);
            return page;
        }

        /**
         * 根据url获取页面，这里不加载JS
         * @param url
         * @return 网页
         * @throws FailingHttpStatusCodeException
         * @throws MalformedURLException
         * @throws IOException
         */
        public static HtmlPage getPage_NoJs(String url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
            final WebClient webClient = iniParam_NoJs();
            HtmlPage page = webClient.getPage(url);
            return page;
        }

}
