package com.chf;

import com.chf.Utils.CSVUtils;
import com.chf.Utils.HtmlUtil;
import com.chf.enilty.AnalyzedTask;
import com.chf.enilty.PatentDoc;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.html.HTMLElement;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author:chf
 * @description:
 * @date:2019/3/19
 **/
public class ZhiwangCraeler{
    static final String orgUrl="http://kns.cnki.net/kns/brief/result.aspx?dbprefix=SCPD";

    //建立返回结果对象集
    static List<PatentDoc> resultList=new ArrayList<>();

    static HtmlPage lastOnePage =null;
    static HtmlPage nextOnePage =null;

    public static void main(String[] args) throws Exception {
        /** 获取当前系统时间*/
        long startTime =  System.currentTimeMillis();

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        System.out.println("======================开始爬虫！===========================");
        //获取客户端，禁止JS
        WebClient webClient = HtmlUtil.iniParam_Js();
        //获取搜索页面，搜索页面包含多个学者，机构通常是非完全匹配，姓名是完全匹配的，我们需要对所有的学者进行匹配操作
        HtmlPage page = webClient.getPage(orgUrl);

        // 根据名字得到一个表单，查看上面这个网页的源代码可以发现表单的名字叫“f”
        final HtmlForm form = page.getFormByName("Form1");

        // 同样道理，获取”检 索“这个按钮
        final HtmlButtonInput button = form.getInputByValue("检 索");
        // 得到搜索框
        final HtmlTextInput from = form.getInputByName("publishdate_from");
        final HtmlTextInput to = form.getInputByName("publishdate_to");
        //设置搜索框的value
        from.setValueAttribute("2016-01-01");
        to.setValueAttribute("2016-12-31");
        // 设置好之后，模拟点击按钮行为。
        final HtmlPage nextPage = button.click();

        HtmlAnchor date=nextPage.getAnchorByText("申请日");
        final HtmlPage secondPage = date.click();
        HtmlAnchor numNow=secondPage.getAnchorByText("50");
        final HtmlPage thirdPage = numNow.click();

        lastOnePage=thirdPage;
//        //拿到当前页数，便于后面翻页
//        List<HtmlElement> currentPage=thirdPage.getByXPath("//table[@class='pageBar_bottom']/tbody/tr/td/div[@class='TitleLeftCell']/font[@class='Mark']");
//        Integer pageNum=Integer.parseInt(currentPage.get(0).getTextContent());
//
//        for (int j=pageNum;j<18;j++){
//            //循环点击下一页拿到上一次爬虫短掉那一页
//            HtmlAnchor next=lastOnePage.getAnchorByText("下一页");
//            nextOnePage=next.click();
//            lastOnePage=nextOnePage;
//        }
        System.out.println("===========拿到检索结果==============");
        //利用线程池开启线程解析首页的数据
        fixedThreadPool.execute(new AnalyzedTask(lastOnePage,1));

        //定义总页数
        int size=200;
        for (int i=2;i<size;i++){
            //休眠一分钟防止频繁按下一页
            Thread.sleep(40000);
            try {
                //将一页的结果抓取完之后开始点击下一页
                HtmlAnchor next=lastOnePage.getAnchorByText("下一页");
                nextOnePage=next.click();
            }catch (ElementNotFoundException e){
                e.printStackTrace();
                System.out.println("需要输入验证码！直接跳出循环。结束爬虫！");
                break;
            }
            //利用线程池开启线程解析数据
            fixedThreadPool.execute(new AnalyzedTask(nextOnePage,i));
            lastOnePage=nextOnePage;
        }

        /** 获取当前的系统时间，与初始时间相减就是程序运行的毫秒数，除以1000就是秒数*/
        long endTime =  System.currentTimeMillis();
        long usedTime = (endTime-startTime)/1000;
        System.out.println("程序运行时间："+usedTime+"s");
    }

    //开始解析列表数据
    private static void startAnalyzed(HtmlPage thirdPage) throws Exception {
        System.out.println("开始解析数据。");
        //解析知网原网页，获取列表的所有链接
        List<HtmlAnchor> anchorList=thirdPage.getByXPath("//table[@class='GridTableContent']/tbody/tr/td/a[@class='fz14']");

        //遍历点击链接，抓取数据
        for (HtmlAnchor anchor:anchorList) {
            HtmlPage detailPage = anchor.click();
            PatentDoc pc=analyzeDetailPage(detailPage.asXml());
            resultList.add(pc);
        }

        LinkedHashMap map = new LinkedHashMap();
        map.put("1", "专利名");
        map.put("2", "申请人");
        map.put("3", "申请日期");
        map.put("4", "申请号");
        map.put("5", "申请地址");

        String path = "C://Users//94068//Desktop//logs//exportParent";
        String fileName = "导出专利"+new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(new Date());
        String fileds[] = new String[] { "patentName", "patentPerson","patentDate", "patentNo","patentAdress"};// 设置列英文名（也就是实体类里面对应的列名）
        CSVUtils.createCSVFile(resultList, fileds, map, path,fileName);
        resultList.clear();
    }

    private static PatentDoc analyzeDetailPage(String detailPage) {
        PatentDoc pc=new PatentDoc();
        Document doc = Jsoup.parse(detailPage);

        Element title=doc.select("td[style=font-size:18px;font-weight:bold;text-align:center;]").first();
        Elements table=doc.select("table[id=box]>tbody>tr>td");

        for (Element td:table) {
            if (td.attr("width").equals("471") && td.attr("bgcolor").equals("#FFFFFF") && td.attr("class").equals("checkItem")){
                String patentNo=td.text().replace("&nbsp;","");
                pc.setPatentNo(patentNo);
            }
            if (td.attr("width").equals("294") && td.attr("bgcolor").equals("#FFFFFF")){
                String patentDate=td.text().replace("&nbsp;","");
                pc.setPatentDate(patentDate);
            }
            if (td.attr("bgcolor").equals("#FFFFFF") && td.attr("class").equals("checkItem")){
                String patentPerson=td.text().replace("&nbsp;","");
                pc.setPatentPerson(patentPerson);
            }
            if (td.attr("bgcolor").equals("#f8f0d2") && td.text().equals(" 【地址】")){
                int index=table.indexOf(td);
                String patentAdress=table.get(index+1).text().replace("&nbsp;","");
                pc.setPatentAdress(patentAdress);
                break;
            }
        }
        pc.setPatentName(title.text());
        return pc;
    }


    /**
     * 将文本文件中的内容读入到buffer中
     * @param buffer buffer
     * @param filePath 文件路径
     * @throws IOException 异常
     * @author cn.outofmemory
     * @date 2013-1-7
     */
    public static String readToBuffer(String filePath) throws IOException {
        StringBuffer buffer = new StringBuffer();
        InputStream is = new FileInputStream(filePath);
        String line; // 用来保存每行读取的内容
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        line = reader.readLine(); // 读取第一行
        while (line != null) { // 如果 line 为空说明读完了
            buffer.append(line); // 将读到的内容添加到 buffer 中
            buffer.append("\n"); // 添加换行符
            line = reader.readLine(); // 读取下一行
        }
        reader.close();
        is.close();
        return buffer.toString();
    }

}
