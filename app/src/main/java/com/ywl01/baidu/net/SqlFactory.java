package com.ywl01.baidu.net;

import com.baidu.mapapi.model.LatLngBounds;

import java.util.Map;

/**
 * Created by ywl01 on 2017/3/12.
 */

public class SqlFactory {
    public static String selectMarkersByBound(LatLngBounds bound, float mapLevel) {
        double minX = bound.southwest.latitude;
        double minY = bound.southwest.longitude;
        double maxX = bound.northeast.latitude;
        double maxY = bound.northeast.longitude;

        String sql = "select m.*,u.realName insertUser from monitor m left join user u on m.userID = u.id where" +
                " m.x > " + minX +
                " and m.x < " + maxX +
                " and m.y > " + minY +
                " and m.y < " + maxY +
                " and m.displayLevel <= " + mapLevel;
        return sql;
    }

    public static String selectMarkerBySearch(String keyword) {
        String sql = "select * from monitor where " +
                "monitorID like '%" + keyword + "%' or " +
                "name like '%" + keyword + "%' or " +
                "owner like '%" + keyword + "%'";
        return sql;
    }

    public static String selectMonitorImage(long monitorID){
        String sql = "select * from monitor_image where monitorID = " + monitorID;
        return sql;
    }

    public static String selectUser(String userName, String password) {
        String sql = "select * from user where userName = '" + userName + "' and password = '" + password + "'";
        return sql;
    }

    public static String checkUser(String userName) {
        String sql = "select * from user where userName = '" + userName + "'" ;
        return sql;
    }

    public static String insert(String tableName, Map<String, String> data) {
        String sql = "insert into " + tableName + " (";
        for(String key: data.keySet()){
            sql += key + ",";
        }
        sql = sql.substring(0,sql.length() -1) + ") values (";

        for(String key: data.keySet()){
            String value = data.get(key);
            if(value == "now()")//php now（）函数，不能带引号
                sql += value + "," ;
            else
                sql += "'" + value + "',";
        }

        sql = sql.substring(0,sql.length() -1) + ")";
        System.out.println(sql);
        return sql;
    }

    public static String delete(String tableName, long id) {
        String sql = "delete from " + tableName + " where id = " + id;
        return sql;
    }

    public static String update(String tableName,Map<String,String> data,long id) {
        String sql = "update " + tableName + " set ";

        for(String key: data.keySet()){
            String value = data.get(key);
            sql += (key + "='" + value + "',");
        }
        sql = sql.substring(0,sql.length() - 1) + " where id =" + id;
        System.out.println(sql);
        return sql;
    }
}
