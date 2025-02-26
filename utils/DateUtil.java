package com.demo.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author sky
 */
public class DateUtil {

    public static final String DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_FULL_MS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATE_FORMAT_YMD = "yyyy-MM-dd";
    public static final String DATE_FORMAT_MM = "mm";
    public static final String DATE_FORMAT_YMDHM = "yyyy-MM-dd HH:mm";
    private static final String PATTERN_YYYYMMDD = "^\\d{8}$";
    private static final String PATTERN_YYYY_MM_DD_HH_MM_SS = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
    private static final String PATTERN_YYYY_MM_DD = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String PATTERN_SECOND_TIMESTAMP = "^\\d{10}$";
    private static final String PATTERN_MILLISECOND_TIMESTAMP = "^\\d{13}$";

    public static final int TRUNC_SECOND = 1;
    public static final int TRUNC_MIN = 2;
    public static final int TRUNC_HOUR = 3;
    public static final int TRUNC_DAY = 4;

    public static final long ONE_DAY_MILLS = 3600000 * 24L;
    public static final int WEEK_DAYS = 7;
    private static final int dateLength = DATE_FORMAT_YMDHM.length();

    private static final FastDateFormat DATE_TIME_DF = FastDateFormat.getInstance(DATE_FORMAT_FULL);

    /**
     * 日期转换为制定格式字符串
     *
     * @param time
     * @param format
     * @return
     */
    public static String formatDateToString(Date time, String format) {
        if (time != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(time);
        }
        return null;
    }

    /**
     * 将时间戳格式化后输出
     *
     * @param timestamp 时间戳
     * @param format    输出格式
     * @return
     */
    public static String format(long timestamp, String format) {
        return new SimpleDateFormat(format).format(new Date(timestamp));
    }

    /**
     * 字符串转换为制定格式日期
     * (注意：当你输入的日期是2014-12-21 12:12，format对应的应为yyyy-MM-dd HH:mm
     * 否则异常抛出)
     *
     * @param date
     * @param format
     * @return
     * @throws ParseException
     * @
     */
    public static Date formatStringToDate(String date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(date);
        } catch (ParseException ex) {
            throw new RuntimeException(ex.toString());
        }
    }

    /**
     * 字符串日期格式转换
     *
     * @param dateStr
     * @param inputFormat
     * @param outputFormat
     * @return
     */
    public static String formatDateString(String dateStr, String inputFormat, String outputFormat) {

        return formatDateToString(formatStringToDate(dateStr, inputFormat), outputFormat);
    }

    /**
     * 判断一个日期是否属于两个时段内
     *
     * @param time
     * @param timeRange
     * @return
     */
    public static boolean isTimeInRange(Date time, Date[] timeRange) {
        return (!time.before(timeRange[0]) && !time.after(timeRange[1]));
    }

    /**
     * 判断日期1是否严格早于日期2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isEarlierThan(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.before(date2);
    }

    /**
     * 判断日期1是否不晚于日期2（早于或等于）
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isEarlierThanOrEqualTo(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return !date1.after(date2);
    }

    /**
     * 判断日期1是否严格晚于日期2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isLaterThan(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return date1.after(date2);
    }

    /**
     * 判断日期1是否不早于日期2（晚于或等于）
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isLaterThanOrEqualTo(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return !date1.before(date2);
    }

    /**
     * 从完整的时间截取精确到分的时间
     *
     * @param fullDateStr
     * @return
     */
    public static String getDateToMinute(String fullDateStr) {
        return fullDateStr == null ? null : (fullDateStr.length() >= dateLength ? fullDateStr.substring(0, dateLength) : fullDateStr);
    }

    /**
     * 返回指定年度的所有周。List中包含的是String[2]对象 string[0]本周的开始日期,string[1]是本周的结束日期。
     * 日期的格式为YYYY-MM-DD 每年的第一个周，必须包含星期一且是完整的七天。
     * 例如：2009年的第一个周开始日期为2009-01-05，结束日期为2009-01-11。 星期一在哪一年，那么包含这个星期的周就是哪一年的周。
     * 例如：2008-12-29是星期一，2009-01-04是星期日，哪么这个周就是2008年度的最后一个周。
     *
     * @param year 格式 YYYY ，必须大于1900年度 小于9999年
     * @return @
     */
    public static List<String[]> getWeeksByYear(final int year) {
        int weeks = getWeekNumOfYear(year);
        List<String[]> result = new ArrayList<String[]>(weeks);
        int start = 1;
        int end = 7;
        for (int i = 1; i <= weeks; i++) {
            String[] tempWeek = new String[2];
            tempWeek[0] = getDateForDayOfWeek(year, i, start);
            tempWeek[1] = getDateForDayOfWeek(year, i, end);
            result.add(tempWeek);
        }
        return result;
    }

    /**
     * 计算指定年、周的上一年、周
     *
     * @param year
     * @param week
     * @return @
     */
    public static int[] getLastYearWeek(int year, int week) {
        if (week <= 0) {
            throw new IllegalArgumentException("周序号不能小于1！！");
        }
        int[] result = {week, year};
        if (week == 1) {
            // 上一年
            result[1] -= 1;
            // 最后一周
            result[0] = getWeekNumOfYear(result[1]);
        } else {
            result[0] -= 1;
        }
        return result;
    }

    /**
     * 下一个[周，年]
     *
     * @param year
     * @param week
     * @return @
     */
    public static int[] getNextYearWeek(int year, int week) {
        if (week <= 0) {
            throw new IllegalArgumentException("周序号不能小于1！！");
        }
        int[] result = {week, year};
        int weeks = getWeekNumOfYear(year);
        if (week == weeks) {
            // 下一年
            result[1] += 1;
            // 第一周
            result[0] = 1;
        } else {
            result[0] += 1;
        }
        return result;
    }

    /**
     * 计算指定年度共有多少个周。(从周一开始)
     *
     * @param year
     * @return @
     */
    public static int getWeekNumOfYear(final int year) {
        return getWeekNumOfYear(year, Calendar.MONDAY);
    }

    /**
     * 计算指定年度共有多少个周。
     *
     * @param year yyyy
     * @return @
     */
    public static int getWeekNumOfYear(final int year, int firstDayOfWeek) {
        // 每年至少有52个周 ，最多有53个周。
        int minWeeks = 52;
        int maxWeeks = 53;
        int result = minWeeks;
        int sIndex = 4;
        String date = getDateForDayOfWeek(year, maxWeeks, firstDayOfWeek);
        // 判断年度是否相符，如果相符说明有53个周。
        if (date.substring(0, sIndex).equals(year + "")) {
            result = maxWeeks;
        }
        return result;
    }

    public static int getWeeksOfWeekYear(final int year) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(WEEK_DAYS);
        cal.set(Calendar.YEAR, year);
        return cal.getWeeksInWeekYear();
    }

    /**
     * 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
     *
     * @param dateString 日期字符串
     * @param dateFormat 日期格式
     * @return
     */
    public static String getDayOfWeek(String dateString, String dateFormat) {
        Calendar cd = Calendar.getInstance();
        Date date = str2Date(dateString, dateFormat);
        if (date == null) {
            return null;
        }
        cd.setTime(date);
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            return "7";
        } else {
            return dayOfWeek + "";
        }
    }

    /**
     * 获取指定年份的第几周的第几天对应的日期yyyy-MM-dd(从周一开始)
     *
     * @param year
     * @param weekOfYear
     * @param dayOfWeek
     * @return yyyy-MM-dd 格式的日期 @
     */
    public static String getDateForDayOfWeek(int year, int weekOfYear, int dayOfWeek) {
        return getDateForDayOfWeek(year, weekOfYear, dayOfWeek, Calendar.MONDAY);
    }

    /**
     * 获取指定年份的第几周的第几天对应的日期yyyy-MM-dd，指定周几算一周的第一天（firstDayOfWeek）
     *
     * @param year
     * @param weekOfYear
     * @param dayOfWeek
     * @param firstDayOfWeek 指定周几算一周的第一天
     * @return yyyy-MM-dd 格式的日期
     */
    public static String getDateForDayOfWeek(int year, int weekOfYear, int dayOfWeek, int firstDayOfWeek) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(firstDayOfWeek);
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        cal.setMinimalDaysInFirstWeek(WEEK_DAYS);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, weekOfYear);
        return formatDateToString(cal.getTime(), DATE_FORMAT_YMD);
    }

    /**
     * 获取指定日期星期几(美国)
     *
     * @param datetime
     * @throws ParseException
     * @
     */
    public static int getWeekOfDate(String datetime) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(WEEK_DAYS);
        Date date = formatStringToDate(datetime, DATE_FORMAT_YMD);
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);

    }

    /**
     * 获取指定日期星期几(中国)
     *
     * @param datetime
     * @throws ParseException
     * @
     */
    public static int getChinessWeekOfDate(String datetime) throws ParseException {

        int week = getWeekOfDate(datetime) - 1;
        return week == 0 ? 7 : week;
    }

    /**
     * 获取当前指定日期最近一个星期几的
     *
     * @param dateString yyyy-MM-dd
     * @param weekDay    Calendar.MONDAY TUESDAY WEDNESDAY THURSDAY FRIDAY SATURDAY SUNDAY
     * @return
     * @throws ParseException
     */
    public static String getLastWeekDay(String dateString, int weekDay) throws ParseException {

        int dayOfWeek = com.demo.utils.DateUtil.getWeekOfDate(dateString);
        if (dayOfWeek == weekDay) {
            return dateString;
        }
        int diffDays;
        if (dayOfWeek > weekDay) {
            diffDays = dayOfWeek - weekDay;
        } else {
            diffDays = 7 - (weekDay - dayOfWeek);
        }
        return com.demo.utils.DateUtil.addDays(dateString, -diffDays, com.demo.utils.DateUtil.DATE_FORMAT_YMD);
    }

    /**
     * 计算某年某周内的所有日期(从周一开始 为每周的第一天)
     *
     * @param yearNum
     * @param weekNum
     * @return @
     */
    public static List<String> getWeekDays(int yearNum, int weekNum) {
        return getWeekDays(yearNum, weekNum, Calendar.MONDAY);
    }

    /**
     * 计算某年某周内的所有日期(七天)
     *
     * @param year
     * @param weekOfYear
     * @param firstDayOfWeek
     * @return
     */
    public static List<String> getWeekDays(int year, int weekOfYear, int firstDayOfWeek) {
        List<String> dates = new ArrayList<String>();
        int dayOfWeek = firstDayOfWeek;
        for (int i = 0; i < WEEK_DAYS; i++) {
            dates.add(getDateForDayOfWeek(year, weekOfYear, dayOfWeek++, firstDayOfWeek));
        }
        return dates;
    }

    /**
     * 获取目标日期的上周、或本周、或下周的年、周信息
     *
     * @param queryDate      传入的时间
     * @param weekOffset     -1:上周 0:本周 1:下周
     * @param firstDayOfWeek 每周以第几天为首日
     * @return
     * @throws ParseException
     */
    public static int[] getWeekAndYear(String queryDate, int weekOffset, int firstDayOfWeek) throws ParseException {

        Date date = formatStringToDate(queryDate, DATE_FORMAT_YMD);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setFirstDayOfWeek(firstDayOfWeek);
        calendar.setMinimalDaysInFirstWeek(WEEK_DAYS);
        int year = calendar.getWeekYear();
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        int[] result = {week, year};
        switch (weekOffset) {
            case 1:
                result = getNextYearWeek(year, week);
                break;
            case -1:
                result = getLastYearWeek(year, week);
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 去掉日期的时分秒
     */
    public static Date getDayOfDate(Date date) {

        if (date == null) return null;
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        // 将时分秒,毫秒域清零
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        return cal1.getTime();
    }

    /**
     * 截取日期，比如保留日期到日，小时，分，秒
     *
     * @param date
     * @param type 截取类型
     * @return
     */
    public static Date trunc(Date date, int type) {

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);

        //保留到秒
        if (type == TRUNC_SECOND) {
            cal1.set(Calendar.MILLISECOND, 0);
        } else if (type == TRUNC_MIN) {
            //保留到分钟
            cal1.set(Calendar.MILLISECOND, 0);
            cal1.set(Calendar.SECOND, 0);
        } else if (type == TRUNC_HOUR) {
            //保留到小时
            cal1.set(Calendar.MILLISECOND, 0);
            cal1.set(Calendar.SECOND, 0);
            cal1.set(Calendar.MINUTE, 0);
        } else if (type == TRUNC_DAY) {
            //保留到天
            cal1.set(Calendar.HOUR_OF_DAY, 0);
            cal1.set(Calendar.MINUTE, 0);
            cal1.set(Calendar.SECOND, 0);
            cal1.set(Calendar.MILLISECOND, 0);
        }
        return cal1.getTime();
    }

    /**
     * 计算个两日期的天数 endDate-startDate
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getDiffDaysBetween(Date startDate, Date endDate) {

        int dayCnt = 0;
        if (startDate != null && endDate != null) {
            dayCnt = getDaysBetween(getDayOfDate(startDate), getDayOfDate(endDate));
        }
        return dayCnt;
    }

    /**
     * 计算个两日期的天数
     *
     * @param startDate 开始日期字串
     * @param endDate   结束日期字串
     * @return
     */
    public static int getDaysBetween(String startDate, String endDate) {
        int dayGap = 0;
        if (startDate != null && startDate.length() > 0 && endDate != null && endDate.length() > 0) {
            Date end = formatStringToDate(endDate, DATE_FORMAT_YMD);
            Date start = formatStringToDate(startDate, DATE_FORMAT_YMD);
            dayGap = getDaysBetween(start, end);
        }
        return dayGap;
    }

    private static int getDaysBetween(Date startDate, Date endDate) {
        return (int) ((endDate.getTime() - startDate.getTime()) / ONE_DAY_MILLS);
    }


    /**
     * 根据指定的日期，增加或者减少天数
     */
    public static String addDays(String dateStr, int amount, String format) {
        Date dateData = add(formatStringToDate(dateStr, format), Calendar.DAY_OF_MONTH, amount);
        return formatDateToString(dateData, format);
    }

    /**
     * 根据指定的日期，类型，增加或减少数量
     */
    public static Date add(Date date, int calendarField, int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    /**
     * 获取指定日期星期几(中国版)
     *
     * @param pTime
     * @return
     * @throws Throwable
     */
    public static String dayForWeek(String pTime) throws ParseException {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = format.parse(pTime);
        return dayForWeek(date);
    }

    /**
     * 获取指定日期星期几(中国版)
     */
    public static String dayForWeek(Date pTime) {

        Calendar cal = Calendar.getInstance();
        String[] weekDays = {"7", "1", "2", "3", "4", "5", "6"};
        cal.setTime(pTime);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 获取上count个月(yyyyMM)
     */
    public static String getBeforeMonth(int count) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -count);
        return sdf.format(cal.getTime());
    }

    /**
     * 时间加n小时
     */
    public static Date addHours(Date date, double hour) {
        long unixTime = date.getTime() + (long) (hour * 60 * 60 * 1000);
        return new Date(unixTime);
    }

    public static String addHours(Date date, double hour, String format) {

        long unixTime = date.getTime() + (long) (hour * 60 * 60 * 1000);
        return formatDateToString(new Date(unixTime), format);
    }

    /**
     * 时间加n小时
     */
    public static Date addHours(Long date, int hour) {
        long unixTime = date + (long) hour * 60 * 60 * 1000;
        return new Date(unixTime);
    }

    /**
     * 时间加n分钟
     */
    public static Date addMinutes(Date date, int minutes) {
        long unixTime = date.getTime() + ((long) minutes * 60 * 1000);
        return new Date(unixTime);
    }

    /**
     * 时间加n分钟
     */
    public static String addMinutes(Date date, int minutes, String format) {
        long unixTime = date.getTime() + ((long) minutes * 60 * 1000);
        return formatDateToString(new Date(unixTime), format);
    }

    /**
     * 时间加n毫秒
     */
    public static Date addMillisecond(Date date, long millisecond) {
        long unixTime = date.getTime() + millisecond;
        return new Date(unixTime);
    }

    /**
     * Long转换为Date
     */
    public static String long2DateStr(Long lo) {
        if (lo == null) return null;
        Date date = new Date(lo);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sd.format(date);

    }

    public static String long2Date(long lo, String format) {
        Date date = new Date(lo);
        SimpleDateFormat sd = new SimpleDateFormat(format);
        return sd.format(date);

    }

    public static String date2str(Date date, String format) {
        if (date == null) return null;
        SimpleDateFormat sd = new SimpleDateFormat(format);
        return sd.format(date);
    }

    public static String date2str(Date date) {
        if (date == null) return null;
        SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_FULL);
        return sd.format(date);
    }

    public static String date2msStr(Date date) {
        if (date == null) return null;
        SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_FULL_MS);
        return sd.format(date);
    }

    public static String date2day(Date date) {
        if (date == null) return null;
        SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_YMD);
        return sd.format(date);
    }

    /**
     * String date转Long
     */
    public static Long date2Long(String strDate) {
        if (StringUtils.isBlank(strDate)) {
            return null;
        }
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT_FULL);
            Date date = df.parse(strDate);
            return date.getTime();
        } catch (Exception e) {
            System.out.format("String date转 Long异常：%s", strDate);
        }
        return null;

    }

    /**
     * String date转Long,增加String格式参数
     *
     * @param strDate
     * @param dateFormat
     * @return
     */
    public static Long date2Long(String strDate, String dateFormat) {
        if (StringUtils.isBlank(strDate)) {
            return null;
        }
        try {
            DateFormat df = new SimpleDateFormat(dateFormat);
            Date date = df.parse(strDate);
            return date.getTime();
        } catch (Exception e) {
            System.out.format("String date转 Long异常：%s|%s", strDate, dateFormat);
        }
        return null;

    }

    /**
     * 计算两个时间相差的小时差
     */
    public static double getDiffHours(Date startDt, Date endDt) {
        long startTime = startDt.getTime();
        long endTime = endDt.getTime();

        if (startTime > endTime) {
            return longToHours(startTime - endTime);
        } else {
            return 0;
        }

    }

    /**
     * 计算两个时间相差的分钟差
     */
    public static double getDiffMin(Date startDt, Date endDt) {
        long startTime = startDt.getTime();
        long endTime = endDt.getTime();

        if (startTime > endTime) {
            return longToMin(startTime - endTime);
        } else {
            return 0;
        }

    }

    public static Integer getDiffMinStr(String startDtStr, String endDtStr) {
        if (StringUtils.isEmpty(startDtStr) || StringUtils.isEmpty(endDtStr)) {
            return null;
        }
        try {
            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_FULL);

            // 将字符串转换为LocalDateTime

            LocalDateTime dateTime1 = LocalDateTime.parse(startDtStr, formatter);
            LocalDateTime dateTime2 = LocalDateTime.parse(endDtStr, formatter);

            // 计算两个日期的分钟差
            long minutes = Duration.between(dateTime1, dateTime2).toMinutes();
            return (int) minutes;
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }


    }

    /**
     * 时间戳转换成小时
     *
     * @param mills
     * @return
     */
    public static double longToHours(long mills) {
        BigDecimal result = BigDecimal.valueOf(0.0);
        result = new BigDecimal(mills).divide(new BigDecimal(3600000), 2, BigDecimal.ROUND_HALF_UP);

        return result.doubleValue();
    }

    /**
     * 时间戳转换成小时
     *
     * @param mills
     * @return
     */
    public static double longToMin(long mills) {
        BigDecimal result;
        result = new BigDecimal(mills).divide(new BigDecimal(60000), 2, BigDecimal.ROUND_HALF_UP);

        return result.doubleValue();
    }

    /**
     * @param day  yyyy-mm-dd
     * @param time hhmm
     * @return yyyy-mm-dd hh:mm:ss
     */
    public static Date assembleDate(String day, int time) {

        return com.demo.utils.DateUtil.formatStringToDate(assembleString(day, time), com.demo.utils.DateUtil.DATE_FORMAT_FULL);
    }

    /**
     * @param day  yyyy-mm-dd
     * @param time hhmm
     * @return yyyyMMddHHmmss
     */
    public static String assembleString(String day, int time) {

        String dayTemp = day.replaceAll("-", "");
        StringBuilder builder = new StringBuilder(dayTemp);

        DecimalFormat df = new DecimalFormat("0000");
        builder.append(df.format(time));
        builder.append("00");
        return builder.toString();
    }

    public static int compareDate(Date d1, Date d2) {
        if (d1 == null) {
            return -1;
        } else if (d2 == null) {
            return 1;
        } else {
            return d1.compareTo(d2);
        }
    }

    public static Date getNextHourTime(Date date, int n) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.HOUR_OF_DAY, ca.get(Calendar.HOUR_OF_DAY) + n);
        return ca.getTime();
    }

    public static Date getNextDayTimes(Date date, int n) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.DAY_OF_MONTH, ca.get(Calendar.DAY_OF_MONTH) + n);
        return ca.getTime();
    }


    public static Date getNextMinTime(Date date, int n) {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.SECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        ca.add(Calendar.MINUTE, 1);
        date = ca.getTime();
        return date;
    }

    /**
     * 将字符串转换为时间
     *
     * @return 时间对象
     */
    public static java.sql.Date str2SqlDate(String str) {
        Date date = str2Date(str);
        if (date == null) return null;
        return new java.sql.Date(date.getTime());
    }

    public static Date long2Date(Long timeStamp) {
        if (timeStamp == null) return null;
        return new Date(timeStamp);
    }

    public static Date str2Date(String str, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date str2Date(String str) {
        if (StringUtils.isBlank(str)) return null;
        if (Pattern.matches(PATTERN_YYYYMMDD, str)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            try {
                return sdf.parse(str);
            } catch (Exception e) {
                return null;
            }
        } else if (Pattern.matches(PATTERN_YYYY_MM_DD_HH_MM_SS, str)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse(str);
            } catch (Exception e) {
                return null;
            }
        } else if (Pattern.matches(PATTERN_YYYY_MM_DD, str)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return sdf.parse(str);
            } catch (Exception e) {
                return null;
            }
        } else if (Pattern.matches(PATTERN_SECOND_TIMESTAMP, str)) {
            long seconds = Long.parseLong(str);
            return new Date(seconds * 1000);
        } else if (Pattern.matches(PATTERN_MILLISECOND_TIMESTAMP, str)) {
            long milliseconds = Long.parseLong(str);
            return new Date(milliseconds);
        } else {
            return null;
        }
    }

    public static long str2Timestamp(String datetime) {
        Date date = str2Date(datetime);
        if (date != null) {
            return date.getTime();
        }

        return 0L;
    }


    public static Date getMaxDate(Date tm1, Date tm2) {
        if (tm1 == null) return tm2;
        if (tm2 == null) return tm1;

        if (tm1.after(tm2)) {
            return tm1;
        } else {
            return tm2;
        }
    }

    public static Date getMinDateWK(Date tm1, Date tm2) {
        if (tm1 == null) return tm2;
        if (tm2 == null) return tm1;
        if (tm1.before(tm2)) {
            return tm1;
        } else {
            return tm2;
        }
    }

    public static Long getMinLong(Long tm1, Long tm2) {
        if (tm1 == null) return tm2;
        if (tm2 == null) return tm1;
        if (tm1 <= tm2) {
            return tm1;
        }
        return tm2;
    }

    public static Date findMax(Date... dates) {
        Date max = null;
        for (Date date : dates) {
            if (date != null && (max == null || date.after(max))) {
                max = date;
            }
        }
        return max;
    }

    public static Long getMaxLong(Long tm1, Long tm2) {
        if (tm1 == null) return tm2;
        if (tm2 == null) return tm1;
        if (tm1 >= tm2) {
            return tm1;
        }
        return tm2;
    }

    public static String getDateStr() {
        return getDateStr(0);
    }

    /**
     * 获取当前日期往前\后n天的 yyyy-MM-dd 格式时间
     *
     * @param n 偏移天数
     * @return dateStr
     */
    public static String getDateStr(int n) {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        LocalDate result = today.plusDays(n);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return result.format(formatter);
    }

    public static int getCurrentMin(Date date) {
        String mm = formatDateToString(date, DATE_FORMAT_MM);
        return Integer.parseInt(mm);
    }

    public static String getCurrentTime() {
        return formatDateToString(new Date(), DATE_FORMAT_FULL);
    }

    /**
     * 计算 date1 - date2 = 多少分钟
     */
    public static long getMinuteDiff(Date date1, Date date2) {
        // 将 Date 转换为 Instant，然后计算时间差
        Instant instant1 = date1.toInstant();
        Instant instant2 = date2.toInstant();
        Duration duration = Duration.between(instant2, instant1);

        // 返回时间差的分钟数
        return duration.toMinutes();
    }

    /**
     * 计算两个日期的时差（按时差单位转换时向下取整）
     * 例：2024-01-01 00:00:59与2024-01-01 00:00:00按分钟的时差为0
     *
     * @param datetime1 日期1
     * @param datetime2 日期2
     * @param timeUnit  时差单位
     * @return (日期1 - 日期2)的时差
     */
    public static long diff(String datetime1, String datetime2, TimeUnit timeUnit) {
        long timestamp1 = str2Timestamp(datetime1);
        long timestamp2 = str2Timestamp(datetime2);
        return timeUnit.convert(timestamp1 - timestamp2, TimeUnit.MILLISECONDS);
    }

    //获取跨越的日期
    public static List<String> getBelongDates(Long planDepartTm, Long planArriveTm, String format) {
        List<String> dateList = new ArrayList<>();
        long startTm = planDepartTm;
        while (startTm < planArriveTm) {
            dateList.add(com.demo.utils.DateUtil.formatDateToString(new Date(startTm), format));
            startTm += 24 * 60 * 60 * 1000;
        }
        String lastDay = com.demo.utils.DateUtil.formatDateToString(new Date(planArriveTm), format);
        if (dateList.size() > 0 && !dateList.get(dateList.size() - 1).equals(lastDay)) {
            dateList.add(lastDay);
        }
        return dateList;
    }

    public static int getCurrentWeekNumbers(String dt, String format) {
        Date date = com.demo.utils.DateUtil.formatStringToDate(dt, format);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    // 获取对应的0点时间Date对象
    public static Date getZeroDate(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static String ms2desc(long ms) {
        long seconds = ms / 1000;
        long minute = seconds / 60;
        long hour = minute / 60;
        long day = hour / 24;
        if (seconds < 60) return seconds + "秒";
        else if (minute < 60) return minute + "分钟" + seconds % 60 + "秒";
        else if (minute < 1440) return hour + "小时" + minute % 60 + "分钟";
        else return day + "天" + hour % 24 + "小时";
    }

    public static long getLastTmOfDay(long tm) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tm);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTimeInMillis();
    }

    public static String addTm(String time, int field, int value) {
        Date date = null;
        try {
            date = DATE_TIME_DF.parse(time);
        } catch (ParseException e) {
            return "";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, value);
        return DATE_TIME_DF.format(cal.getTime());
    }

    /**
     * 多个日期里面取【最早】的（允许存在空值）
     *
     * @param dates
     * @return
     */
    public static Optional<Date> earliest(Date... dates) {
        Date earliestDate = null;
        for (Date date : dates) {
            if (date == null) {
                continue;
            }

            if (earliestDate == null) {
                earliestDate = date;
                continue;
            }

            if (date.before(earliestDate)) {
                earliestDate = date;
            }
        }

        return Optional.ofNullable(earliestDate);
    }

    /**
     * 多个日期里面取【最晚】的（允许存在空值）
     *
     * @param dates
     * @return
     */
    public static Optional<Date> latest(Date... dates) {
        Date latestDate = null;
        for (Date date : dates) {
            if (date == null) {
                continue;
            }

            if (latestDate == null) {
                latestDate = date;
                continue;
            }

            if (date.after(latestDate)) {
                latestDate = date;
            }
        }

        return Optional.ofNullable(latestDate);
    }

    public static void main(String[] args) {
        System.out.println(ts2hour(1729524751000L));
    }

    public static int ts2hour(long ts) {
        // 将时间戳转换成 LocalDateTime 对象
        LocalDateTime curDateTime = LocalDateTime.ofEpochSecond(ts / 1000, 0, getSystemDefaultZoneOffset());
        return curDateTime.getHour();
    }

    // 获取系统默认时区偏移量
    private static ZoneOffset getSystemDefaultZoneOffset() {
        return ZoneId.systemDefault().getRules().getOffset(Instant.now());
    }
}
