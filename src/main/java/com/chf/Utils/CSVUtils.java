package com.chf.Utils;

import com.chf.enilty.PatentDoc;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author:chf
 * @description:
 * @date:2019/3/19
 **/
public class CSVUtils {
    /**
     * 生成为CVS文件
     * @param <T>
     * @param <T>
     *
     * @param exportData
     *            源数据List
     * @param fileds
     * @param map
     *            csv文件的列表头map
     * @param outPutPath
     *            文件路径
     * @param fileName
     *            文件名称
     * @return
     */
    public static  <T> File createCSVFile(List<T> exportData, String[] fileds,
                                          LinkedHashMap map, String outPutPath, String fileName) {
        File csvFile = null;
        BufferedWriter csvFileOutputStream = null;
        try {
            File file = new File(outPutPath);
            if (!file.exists()) {
                file.mkdir();
            }
            // 定义文件名格式并创建
            csvFile = File.createTempFile(fileName, ".csv", file);
            // UTF-8使正确读取分隔符","
            csvFileOutputStream = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"),1024);
            System.out.println("csvFileOutputStream：" + csvFileOutputStream);
            //手动加入BOM,解决UTF-8乱码问题
            csvFileOutputStream.write(new String(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF }, "UTF-8"));
            // 写入文件头部
            for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();) {
                java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
                csvFileOutputStream.write((String) propertyEntry.getValue() != null
                        ? new String(((String) propertyEntry.getValue()).getBytes("UTF-8"), "UTF-8")
                        : "");
                if (propertyIterator.hasNext()) {
                    csvFileOutputStream.write(",");
                }
            }
            csvFileOutputStream.write("\r\n");
            // 写入文件内容,
            // ============ //第一种格式：Arraylist<实体类>填充实体类的基本信息==================
            for (int j = 0; exportData != null && !exportData.isEmpty() && j < exportData.size(); j++) {
                T t = (T) exportData.get(j);
                Class clazz = t.getClass();
                String[] contents = new String[fileds.length];
                for (int i = 0; fileds != null && i < fileds.length; i++) {
                    String filedName = toUpperCaseFirstOne(fileds[i]);
                    Method method = clazz.getMethod(filedName);
                    method.setAccessible(true); //功能是启用或禁用安全检查，提高性能
                    Object obj = method.invoke(t);
                    String str = String.valueOf(obj);
                    if (str == null || str.equals("null")) {
                        str = "";
                    }
                    contents[i] = str;

                }

                for (int n = 0; n < contents.length; n++) {
                    // 将生成的单元格添加到工作表中
                    csvFileOutputStream.write(contents[n]);
                    csvFileOutputStream.write(",");

                }
                csvFileOutputStream.write("\r\n");
            }
            csvFileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                csvFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return csvFile;
    }

    /**
     * 将第一个字母转换为大写字母并和get拼合成方法
     *
     * @param origin
     * @return
     */
    private static String toUpperCaseFirstOne(String origin) {
        StringBuffer sb = new StringBuffer(origin);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "get");
        return sb.toString();
    }

    public static void main(String[] args) {
        // =======改成list的格式，支持（Arraylist传入实体类的形式），改造的方法============
        ArrayList<PatentDoc> pubList = new ArrayList<PatentDoc>();
        PatentDoc pc1 = new PatentDoc();
        pc1.setPatentAdress("一年");
        pc1.setPatentPerson("www/sda/dav.com");
        pc1.setPatentDate("www/sda/dav.com");

        PatentDoc pc2 = new PatentDoc();
        pc2.setPatentAdress("一年");
        pc2.setPatentPerson("www/sda/dav.com");
        pc2.setPatentDate("www/sda/dav.com");


        pubList.add(pc1);
        pubList.add(pc2);

        LinkedHashMap map = new LinkedHashMap();
        map.put("1", "文件名");
        map.put("2", "文件通知");
        map.put("3", "文件日期");

        String path = "C://Users//94068//Desktop//logs//exportParent";
        String fileName = "文件导出";
        String fileds[] = new String[] { "title", "url","fileDate"};// 设置列英文名（也就是实体类里面对应的列名）
        File file = CSVUtils.createCSVFile(null, fileds, map, path,
                fileName);
        String fileName2 = file.getName();
        System.out.println("文件名称：" + fileName2);
    }
}
