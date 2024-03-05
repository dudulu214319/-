package com.dudulu.seckill_my.utils;


import com.dudulu.seckill_my.pojo.User;
import com.dudulu.seckill_my.vo.RespBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 生成用户工具类
 *
 * @author: LC
 * @date 2022/3/4 3:29 下午
 * @ClassName: UserUtil
 */
public class UserUtil { // 为了压测秒杀按钮
    private static void createUser(int count) throws SQLException, ClassNotFoundException, IOException {
        List<User> users = new ArrayList<>();
        for (int i = 0;i < count;i ++) {
            User user = new User();
            user.setId(13000000000L+i);
            user.setNickname("user:"+i);
            user.setSalt("1a2b3c");
            user.setPassword(MD5Utils.inputPassToDBPass("123456", user.getSalt()));
            user.setRegisterDate(new Date());
            users.add(user);
        }
        // 1.插入数据库
//        Connection conn = getconn();
//        String sql = "insert into t_user(id, nickname, password, salt, register_date) values(?, ?, ?, ?, ?)";
//        PreparedStatement pstmt = conn.prepareStatement(sql);
//        for (int i = 0;i < users.size();i ++) {
//            User user = users.get(i);
//            pstmt.setLong(1, user.getId());
//            pstmt.setString(2, user.getNickname());
//            pstmt.setString(3, user.getPassword());
//            pstmt.setString(4, user.getSalt());
//            pstmt.setTimestamp(5, new Timestamp(user.getRegisterDate().getTime()));
//            pstmt.addBatch();
//        }
//        pstmt.executeBatch();
//        pstmt.clearParameters();
//        conn.close();
        // 2.模拟HTTP访问并得到userTicket，再写入config.txt里，用于后面的多用户并发测试秒杀功能
        String uslString = "http://localhost:8888/login/doLogin";
        File file = new File("C:\\Users\\19263\\Desktop\\config.txt");
        if(file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(0);
        for (int i = 0; i < users.size();i ++) { // 从users列表里一个个读出user发起http访问,服务器生成(ticket, user)存储在redis里，并返回ticket。再和userId形成row，存储在conf.txt里
            User user = users.get(i);
            URL url = new URL(uslString);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
            String params = "mobile=" + user.getId() + "&password=" + MD5Utils.inputPassToFormPass("123456");
            out.write(params.getBytes()); // 把参数string转换为字节写入OutputStream
            out.flush();
            InputStream inputStream = co.getInputStream();
            /**
             * OutputStream 类的常用子类
             *
             * FileOutputStream 类：用于将数据写入到输出流文件；
             * ByteArrayOutputStream 类：在内存中模拟一个字节流输出
             */
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buff = new byte[1024]; //一次读取多个字节提高效率
            int len = 0; // 每次实际读取的字节数
            while ((len=inputStream.read(buff)) >= 0) { // -1是读取的末尾，len>=0说明还没读完
                bout.write(buff, 0, len); // 每次把buff[0, len]个字节写入bout
            }
            inputStream.close(); // 关闭此文件输入流并释放与该流关联的所有系统资源
            bout.close(); // 关闭此字节数组输出流并释放与该流关联的所有系统资源
            String response = new String(bout.toByteArray());
            ObjectMapper objectMapper = new ObjectMapper(); // 将JSON字符串反序列化为Java对象
            RespBean respBean = objectMapper.readValue(response, RespBean.class);
            String userTicket = (String) respBean.getObj(); // 需要提前放好
            String row = user.getId() + "," + userTicket;
            raf.seek(raf.length()); // 追加模式
            raf.write(row.getBytes()); // 把string转换为字节写入RandomAccessFile
            raf.write("\r\n".getBytes());
            System.out.println("write to file :" + user.getId());
        }
        raf.close();
        System.out.println("over!");
    }

    private static Connection getconn() throws ClassNotFoundException, SQLException {
        String url = "jdbc:mysql://127.0.0.1:3306/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "dudulu";
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        createUser(5000);
    }
}
