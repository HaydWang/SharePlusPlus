package com.droidrise.snaptext;

import org.joda.time.DateTime;
import java.util.HashMap;

/**
 * Created by f10210c on 3/9/2017.
 */
public class CNDateUtility {
    private final static String YEAR_CHINESE = "年";
    private final static String MONTH_CHINESE = "月";
    private final static String DAY_CHINESE = "日";
    private int year = 0;
    private int month = 0;
    private int day = 0;
    private static final HashMap<Integer, String> intToChinese = new HashMap<>();
    private HashMap<Integer, String> yearMap = new HashMap<>();
    private HashMap<Integer, String> monthMap = new HashMap<>();
    private HashMap<Integer, String> dayMap = new HashMap<>();

    static {
        intToChinese.put(0, "零");
        intToChinese.put(1, "一");
        intToChinese.put(2, "二");
        intToChinese.put(3, "三");
        intToChinese.put(4, "四");
        intToChinese.put(5, "五");
        intToChinese.put(6, "六");
        intToChinese.put(7, "七");
        intToChinese.put(8, "八");
        intToChinese.put(9, "九");
        intToChinese.put(10, "十");
    }

    static private String yearToChinese(int year) {
        StringBuilder yearString = new StringBuilder();
        while (year > 0) {
            int y = year % 10;
            yearString.insert(0, intToChinese.get(y));
            year = year / 10;
        }
        return yearString.toString();
    }

    static private String otherToChinese(int dayOrMonth) {
        if (dayOrMonth < 0) {
            return "";
        }
        if (dayOrMonth < 10) {
            return intToChinese.get(dayOrMonth);
        }
        StringBuilder otherString = new StringBuilder();
        int tens = dayOrMonth / 10;
        otherString.append((tens == 1 ? "" : intToChinese.get(tens)) + "十");
        int units = dayOrMonth - tens * 10;
        otherString.append((units <= 0) ? "" : intToChinese.get(units));
        return otherString.toString();
    }

    static public String getFullCNDate() {
        DateTime now = new DateTime();
        return getFullCNDate(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth());
    }

    // format : "二零一五年 九月 十一日"
    static public String getFullCNDate(int year, int month, int day) {
        return getYear(year) + getMonth(month) + getDay(day);
    }

    // format : "十月二十五日"
    static public String getMonthDayCNDate(int month, int day) {
        return getMonth(month) + getDay(day);
    }

    // format : "二零一六年 九月"
    static public String getYearMonthCNData(int year, int month) {
        return getYear(year) + getMonth(month);
    }

    // format : 二十五日
    static public String getDayCNData(int day) {
        return getDay(day);
    }

    static public String getDay(int day) {
        return getPureDay(day) + DAY_CHINESE + " ";
    }

    static public String getYear(int year) {
        return getPureYear(year) + YEAR_CHINESE + " ";
    }

    static public String getMonth(int month) {
        return getPureMonth(month) + MONTH_CHINESE + " ";
    }

    // format
    static public String getPureDay(int day) {
        return otherToChinese(day);
    }

    static public String getPureYear(int year) {
        return yearToChinese(year);
    }

    static public String getPureMonth(int month) {
        return otherToChinese(month);
    }
}
