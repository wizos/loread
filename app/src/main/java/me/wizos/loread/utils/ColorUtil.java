package me.wizos.loread.utils;

import com.socks.library.KLog;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.data.WithPref;

/**
 * 屏幕工具
 *
 * @author Wizos on 2016/2/13.
 */
public class ColorUtil {

    /**
     * 计算2个颜色的距离（相似度）
     */

    private static double distance(RGB e1) {
        RGB e2 = null;
        if (WithPref.i().getThemeMode() == App.Theme_Day) {
            e2 = new RGB(255, 255, 255);
        } else {
            e2 = new RGB(32, 43, 47);
        }
        return distance(e1, e2);
    }

    private static double distance(RGB e1, RGB e2) {
        long rmean = ((long) e1.r + (long) e2.r) / 2;
        long r = (long) e1.r - (long) e2.r;
        long g = (long) e1.g - (long) e2.g;
        long b = (long) e1.b - (long) e2.b;
        return Math.sqrt((((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8));
    }

    private final static String P_BACKGROUND_COLOR = "background(\\s|:|-color).*?(;|$)";
    private final static String P_RGB = "rgb\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)";
    private final static String P_HEX3 = ":\\s*#\\s*(\\d{3})\\s*(;|$)";
    private final static String P_HEX6 = ":\\s*#\\s*([0-9a-zA-Z]{6})\\s*(;|$)";
    private final static String P_HEX8 = ":\\s*#\\s*[0-9a-zA-Z]{2}([0-9a-zA-Z]{6})\\s*(;|$)";

    private final static int Color_Distance = 200;

    public static Document mod(Document doc) {
        Elements elements = doc.select("[style*=color]");
//        KLog.e("颜色，获取到数量：" + elements.size());
        String style = null;
        int r = 0, g = 0, b = 0;
        double distance = 0;
        Pattern pattern;
        Matcher m;
        for (Element element : elements) {
            style = element.attr("style");
            KLog.e("获取到的style： " + style);

            // 先去掉背景色。在css中去掉？
            pattern = Pattern.compile(P_BACKGROUND_COLOR, Pattern.CASE_INSENSITIVE);
            m = pattern.matcher(style);
            style = m.replaceAll("");

            pattern = Pattern.compile(P_RGB, Pattern.CASE_INSENSITIVE);
            m = pattern.matcher(style);
            if (m.find()) {
                r = Integer.valueOf(m.group(1));
                g = Integer.valueOf(m.group(2));
                b = Integer.valueOf(m.group(3));
                distance = ColorUtil.distance(new RGB(r, g, b));
                if (distance < Color_Distance) {
                    style = m.replaceFirst("rgb(" + (255 - r) + ", " + (255 - g) + ", " + (255 - b) + ")");
                }
            } else {
                pattern = Pattern.compile(P_HEX6, Pattern.CASE_INSENSITIVE);
                m = pattern.matcher(style);
                if (m.find()) {
                    r = Integer.parseInt(m.group(1).substring(0, 2), 16);
                    g = Integer.parseInt(m.group(1).substring(2, 4), 16);
                    b = Integer.parseInt(m.group(1).substring(4, 6), 16);
                    distance = ColorUtil.distance(new RGB(r, g, b));
                    if (distance < Color_Distance) {
                        style = m.replaceFirst(":rgb(" + (255 - r) + ", " + (255 - g) + ", " + (255 - b) + ");");
                    }
                } else {
                    pattern = Pattern.compile(P_HEX8, Pattern.CASE_INSENSITIVE);
                    m = pattern.matcher(style);
                    if (m.find()) {
                        r = Integer.parseInt(m.group(1).substring(0, 2), 16);
                        g = Integer.parseInt(m.group(1).substring(2, 4), 16);
                        b = Integer.parseInt(m.group(1).substring(4, 6), 16);
                        distance = ColorUtil.distance(new RGB(r, g, b));
                        if (distance < Color_Distance) {
                            style = m.replaceFirst(":rgb(" + (255 - r) + ", " + (255 - g) + ", " + (255 - b) + ");");
                        }
                    } else {
                        pattern = Pattern.compile(P_HEX3, Pattern.CASE_INSENSITIVE);
                        m = pattern.matcher(style);
                        String tmp = "";
                        if (m.find()) {
                            tmp = m.group(1).substring(0, 1);
                            r = Integer.parseInt(tmp + tmp, 16);
                            tmp = m.group(1).substring(1, 2);
                            g = Integer.parseInt(tmp + tmp, 16);
                            tmp = m.group(1).substring(2, 3);
                            b = Integer.parseInt(tmp + tmp, 16);
                            distance = ColorUtil.distance(new RGB(r, g, b));
                            if (distance < Color_Distance) {
                                style = m.replaceFirst(":rgb(" + (255 - r) + ", " + (255 - g) + ", " + (255 - b) + ");");
                            }
                        }
                    }
                }
            }
            KLog.e(" 修改后的style： " + style);
            KLog.e(" 颜色相似度为： " + distance);
            element.attr("style", style);
        }
        KLog.e("==========================");
//        KLog.e(  doc.outerHtml() );
        return doc;
    }


}
