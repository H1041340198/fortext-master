package com.nplat.convert.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyDate {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");



    public static Boolean judgeStrIsDate(String dateStr){
        try{
            //增加强判断条件，否则 诸如2022-02-29也可判断出去
            format.setLenient(false);
            format.parse(dateStr);
            return Boolean.TRUE;
        }catch(Exception e){
            return Boolean.FALSE;
        }
    }


    /**
     * 获取制定日期之前或者之后几天的日期对象
     *
     * @param days 正数为指定日期后days天，负数为指定日期之前days天
     * @return 返回Date对象
     */
    public static Date getRollDate(int days) {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.DATE, now.get(Calendar.DATE) + days);
        return now.getTime();
    }


    public static long getRollTimestampAfter(long curr, int days) {
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(curr);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + days);
        return now.getTime().getTime();
    }



    public static String getMonthBegin(int num) {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + (month + num) / 12 - 1);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + (month + num) % 12);
        month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        StringBuffer buffer = new StringBuffer();
        buffer.append(year);
        buffer.append("-");
        buffer.append(month >= 10 ? String.valueOf(month) : ("0" + month));
        buffer.append("-01 00:00:00");
        return buffer.toString();
    }

    public static String getMonthEnd(int num) {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + (month + num) / 12 - 1);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + (month + num) % 12);
        int year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        StringBuffer buffer = new StringBuffer();
        buffer.append(year);
        buffer.append("-");
        buffer.append(month >= 10 ? String.valueOf(month) : ("0" + month));
        buffer.append("-");
        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            buffer.append("31");
        }
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            buffer.append("30");
        }
        if (month == 2) {
            if (leapYear(year)) {
                buffer.append("29");
            } else {
                buffer.append("28");
            }
        }
        buffer.append(" 23:59:59");
        return buffer.toString();
    }


    public static Date getCurrentDateBegin(Date date) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.HOUR, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        return now.getTime();
    }

    public static Date getCurrentDateEnd(Date date) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.HOUR_OF_DAY, 23);
        now.set(Calendar.MINUTE, 59);
        now.set(Calendar.SECOND, 59);
        return now.getTime();
    }


    public static String format_YY_MM_DD_HH_mm_ss_OfDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        StringBuffer buffer = new StringBuffer();
        buffer.append(calendar.get(Calendar.YEAR));
        buffer.append("-");
        buffer.append(calendar.get(Calendar.MONTH) + 1);
        buffer.append("-");
        buffer.append(calendar.get(Calendar.DATE));
        buffer.append(" ");
        buffer.append(calendar.get(Calendar.HOUR_OF_DAY) == 0 ? "00" : calendar.get(Calendar.HOUR_OF_DAY));
        buffer.append(":");
        buffer.append(calendar.get(Calendar.MINUTE) == 0 ? "00" : calendar.get(Calendar.MINUTE));
        buffer.append(":");
        buffer.append(calendar.get(Calendar.SECOND) == 0 ? "00" : calendar.get(Calendar.SECOND));
        return buffer.toString();
    }

    public static String format_YY_MM_DD_HH_mm_ss_Of_Time(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        StringBuffer buffer = new StringBuffer();
        buffer.append(calendar.get(Calendar.YEAR));
        buffer.append("-");
        buffer.append(calendar.get(Calendar.MONTH) + 1);
        buffer.append("-");
        buffer.append(calendar.get(Calendar.DATE));
        buffer.append(" ");
        buffer.append(calendar.get(Calendar.HOUR_OF_DAY) == 0 ? "00" : calendar.get(Calendar.HOUR_OF_DAY));
        buffer.append(":");
        buffer.append(calendar.get(Calendar.MINUTE) == 0 ? "00" : calendar.get(Calendar.MINUTE));
        buffer.append(":");
        buffer.append(calendar.get(Calendar.SECOND) == 0 ? "00" : calendar.get(Calendar.SECOND));
        return buffer.toString();
    }

    public static String format_YY_MM_DD_OfDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        StringBuffer buffer = new StringBuffer();
        buffer.append(calendar.get(Calendar.YEAR));
        buffer.append("-");
        buffer.append(calendar.get(Calendar.MONTH) + 1);
        buffer.append("-");
        buffer.append(calendar.get(Calendar.DATE));
        return buffer.toString();
    }



    /**
     * 功能：判断输入年份是否为闰年<br>
     *
     * @param year
     * @return 是：true  否：false
     * @author pure
     */
    public static boolean leapYear(int year) {
        boolean leap;
        if (year % 4 == 0) {
            if (year % 100 == 0) {
                if (year % 400 == 0) leap = true;
                else leap = false;
            } else leap = true;
        } else leap = false;
        return leap;
    }
}
