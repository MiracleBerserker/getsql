
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;


/**
 * @Description:
 * @Copyright: Dist
 * @Author: MengHui
 * @Date: 2019-07-09 ${MINUTF}
 * @Modified:
 * @Description:
 * @Date:
 */
public class MainApp {

    public static void main(String[] args)  throws Exception{
        System.out.println("-------------自动获取报表sql--------------");
        System.out.println("-------------简单说明--------------");
        System.out.println("1.config配置文件可以更改数据库地址 账号和密码");
        System.out.println("2.config配置文件可以配置基本信息表  配置之后关联表时会以id关联不会是cid");
        System.out.println("3.config配置文件可以配置关联主表 配置tschars字段可以过滤掉oracle特殊字符 但是要求需要自己进行ASCII编码逗号隔开");
        System.out.println("4.输入设计表单的的标识数字 就可以得到全部关联的sql语句 会在当下目录生产对应的文件");
        Scanner in = new Scanner(System.in);
        MainApp mainApp = new MainApp();
        while (true){
            System.out.print("请输入表单标识: ");
            int formid = in.nextInt();
            mainApp.getSql(formid);
        }
    }

    public void getSql(int formid) throws Exception{
        Properties properties = SqlTools.properties;
        //获取id关联表
        String [] idtables = properties.getProperty("idtables").split(",");
        SqlTools sqlTools = new SqlTools();
        Connection con = sqlTools.getConnect();
        //System.out.println(con);
        //查询对应标识的数据
        PreparedStatement preState = con.prepareStatement("select SHEETLAYOUTCONTENT from  DCC_FORMREVISION  t where FK_FORM_ID = ?");
        preState.setInt(1,formid);
        ResultSet resultSet = preState.executeQuery();
        if(resultSet.next()){
            Blob blob = resultSet.getBlob("SHEETLAYOUTCONTENT");
            String content = new String(blob.getBytes(1,(int)blob.length()),"GBK");
            content = content.replaceAll("\"host\":this","\"host\":\"this\"");
            //System.out.println(content);
            JSONArray jsonArray = JSONArray.parseArray(content);
            jsonArray = jsonArray.getJSONObject(0).getJSONArray("children");
            HashSet<String> set = new HashSet<>();
            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            //开始解析字符串生成sql语句
            if(jsonArray.size() > 0){
                for(int i=0;i<jsonArray.size();i++){
                    JSONObject js2 = jsonArray.getJSONArray(i).getJSONObject(0);
                    JSONArray jr = js2.getJSONArray("children");
                    for(int j = 0;j<jr.size();j++){
                        JSONArray jr3 = jr.getJSONArray(j);
                        for(int m=0;m<jr3.size();m++){
                            JSONObject js3 = jr3.getJSONObject(m);
                            try {
                                JSONObject js4 = js3.getJSONObject("properties");
                                String bindingdesc = js4.getString("bindingdesc").replaceAll("/","");
                                String dataBinder = js4.getString("dataBinder");
                                String dataField = js4.getString("dataField");
                                if(dataField.equals("id")){
                                    JSONArray jr5 = js4.getJSONArray("header");
                                    if(jr5!=null){
                                        //System.out.println("-----");
                                        //System.out.println("---子表--");
                                        for(int k = 0;k < jr5.size();k++){
                                            JSONObject js5 = jr5.getJSONObject(k);
                                            String id = js5.getString("id");
                                            String caption = js5.getString("caption").replaceAll("/","");
                                            //System.out.println(id+"-----"+caption);
                                            set.add(dataBinder);
                                            sb.append(dataBinder+"."+id+" as "+bindingdesc.split("/")[0].replaceAll("id","")+caption+", ");
                                        }
                                    }

                                }else {
                                    if(bindingdesc!=null&&!"".equals(bindingdesc)){
                                        //System.out.println(bindingdesc+":------"+dataBinder+":------"+dataField);
                                        set.add(dataBinder);
                                        sb.append(dataBinder+"."+dataField+"  as "+bindingdesc+", ");
                                    }
                                }


                            }catch (Exception e){

                            }

                        }
                    }
                }
                //System.out.println(set);
                sb.deleteCharAt(sb.lastIndexOf(","));

                if(set.size()>1){
                    sb.append(" from "+properties.getProperty("relationtable")+" rl ");
                    StringBuilder ss = new StringBuilder(" where 1=1 ");
                    tags:
                    for(String tab : set){
                        sb.append(","+tab);
                        for(int i = 0;i<idtables.length;i++){
                            if(tab.equals(idtables[i])){
                                ss.append(" and "+tab+".id = rl.cid");
                                continue tags;
                            }
                        }
                        ss.append(" and "+tab+".cid = rl.cid");
                    }
                    sb.append(ss.toString());
                }else {
                    sb.append(" from  "+set.iterator().next());
                }


                //去除特色字符
                String last = sb.toString();
                String [] tschars = properties.getProperty("tschars").split(",");
                for(int j=0;j<tschars.length;j++){
                    last = last.replaceAll(tschars[j],"");
                    //System.out.println(tschars[j]);
                }
                System.out.println(last);
                //将sql文件存储到当前目录下
                System.out.println();
                File file = new File(SqlTools.path+formid+".txt");
                System.out.println(SqlTools.path+formid+".txt");
                if(!file.exists()){
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file);
                out.write(last.getBytes());
                out.close();
                System.out.println("表单标识为"+formid+":生成完毕-------------------------------------------------------");
                System.out.println();
                System.out.println();
                System.out.println();
            }

        }else {
            System.out.println("查询不到指定标识的数据");
        }
        resultSet.close();
        preState.close();
    }

}
