package com.ywl01.baidu;

import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void mapTest() {
        HashMap<String, String> map=new HashMap();
        map.put("p1", "1");
        map.put("p2", "1");
        map.put("p3", "1");
        map.put("p4", "1");
        map.put("p5", "1");
        map.put("p6", "1");

        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            System.out.println(map.get(iterator.next()));
        }
    }

    @Test
    public void mapToArray() {
        Map fieldMap = new HashMap<>();
        fieldMap.put("编号", "monitorID");
        fieldMap.put("名称", "name");
        fieldMap.put("类型", "type");
        fieldMap.put("所有人", "owner");
        fieldMap.put("录入人员", "user");
        fieldMap.put("运行状态", "isRunning");
        fieldMap.put("录入时间", "insertTime");

        String[] items = new String[fieldMap.size()];
        fieldMap.keySet().toArray(items);

        for (int i = 0; i <items.length; i++) {
            System.out.println(items[i]);
        }
    }
}