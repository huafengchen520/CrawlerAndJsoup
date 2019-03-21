package com.chf.enilty;

import com.chf.Utils.CSVUtils;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author:chf
 * @description: 解析详情并导出出的线程
 * @date:2019/3/20
 **/
public class AnalyzedTask implements Runnable{

    //建立返回结果对象集
    List<PatentDoc> resultList=new ArrayList<>();

    private HtmlPage lastOnePage =null;

    private int curPage=0;

    public AnalyzedTask(HtmlPage lastOnePage,int curPage) {
        this.lastOnePage = lastOnePage;
        this.curPage=curPage;
    }

    @Override
    public void run() {
        /** 获取当前系统时间*/
        long startTime =  System.currentTimeMillis();
        System.out.println("线程开始第"+curPage+"页的解析数据。");
        //解析首页的数据
        try {
            startAnalyzed(lastOnePage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("第"+curPage+"页数据解析完成。耗时："+((System.currentTimeMillis()-startTime)/1000)+"s");
    }

    //开始解析列表数据
    private void startAnalyzed(HtmlPage thirdPage) throws Exception {
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
        String fileName = "导出专利";
        String fileds[] = new String[] { "patentName", "patentPerson","patentDate", "patentNo","patentAdress"};// 设置列英文名（也就是实体类里面对应的列名）
        CSVUtils.createCSVFile(resultList, fileds, map, path,fileName);
        resultList.clear();
    }

    private PatentDoc analyzeDetailPage(String detailPage) {
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
}
