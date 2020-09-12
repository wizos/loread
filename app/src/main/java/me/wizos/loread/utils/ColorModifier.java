package me.wizos.loread.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;

/**
 * Created by Wizos on 2019/6/7.
 */

public class ColorModifier {
    private static ColorModifier app;

    private ColorModifier() {
    }

    public static ColorModifier i() {
        if (app == null) {
            synchronized (ColorModifier.class) {
                if (app == null) {
                    app = new ColorModifier();
                }
            }
        }
        return app;
    }

    private Matcher m;
    private String tmp = "";
    private int r = 0, g = 0, b = 0;

    public Document handleColor(Document doc) {
        RGB backgroundColor;
        if (App.i().getUser().getThemeMode() == App.THEME_DAY) {
            backgroundColor = new RGB(255, 255, 255);
        } else {
            // return new RGB(69, 73, 82);
            backgroundColor = new RGB(32, 43, 47);
        }

        Elements elements;
        doc.select("[bgcolor]").removeAttr("bgcolor");
        elements = doc.select("[style*=color]");
        for (Element element : elements) {
            tmp = element.attr("style");
            // 去掉原生背景色
            m = Pattern.compile("background(\\s*:|-color).*?(;|$)", Pattern.CASE_INSENSITIVE).matcher(tmp);
            tmp = m.replaceAll("");
            element.attr("style", tmp);

            // 优化前景色
            m = Pattern.compile("(^|\\s*)color\\s*:(.*?)($|;)", Pattern.CASE_INSENSITIVE).matcher(tmp);
            if (m.find()) {
                tmp = m.replaceFirst("color:" + modifyColor(m.group(2), backgroundColor) + ";");
                element.attr("style", tmp);
            }
        }

        elements = doc.select("[color]");
        for (Element element : elements) {
            element.attr("style", "color:" + modifyColor(element.attr("color"), backgroundColor));
            element.removeAttr("color");
        }
        return doc;
    }

    public Document inverseColor(Document doc) {
        if (App.i().getUser().getThemeMode() == App.THEME_DAY) {
            return inverseColor(doc, new RGB(255, 255, 255));
        } else {
            // return new RGB(69, 73, 82);
            return inverseColor(doc, new RGB(32, 43, 47));
        }
    }

    private Document inverseColor(Document doc, RGB backgroundColor) {
        Elements elements;
        doc.select("[bgcolor]").removeAttr("bgcolor");
        elements = doc.select("[style*=color]");
        for (Element element : elements) {
            tmp = element.attr("style");
            // 先去掉背景色
            m = Pattern.compile("background(\\s*:|-color).*?(;|$)", Pattern.CASE_INSENSITIVE).matcher(tmp);
            tmp = m.replaceAll("");
            element.attr("style", tmp);

            m = Pattern.compile("(^|\\s*)color\\s*:(.*?)($|;)", Pattern.CASE_INSENSITIVE).matcher(tmp);
            if (m.find()) {
                tmp = m.replaceFirst("color:" + modifyColor(m.group(2), backgroundColor) + ";");
                element.attr("style", tmp);
            }
        }

        elements = doc.select("[color]");
        for (Element element : elements) {
            //element.attr("color",modifyColor(element.attr("color"),backgroundColor) );
            element.attr("style", "color:" + modifyColor(element.attr("color"), backgroundColor));
            element.removeAttr("color");
        }
        return doc;
    }

    private String modifyColor(String color, RGB backgroundColor) {
        // 处理 RGB 颜色
        m = Pattern.compile("\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            r = Integer.valueOf(m.group(1));
            g = Integer.valueOf(m.group(2));
            b = Integer.valueOf(m.group(3));
            return modifyColor(backgroundColor, new RGB(r, g, b));
        }

        // 处理 #FF000000 类型的颜色
        m = Pattern.compile("#\\W*\\w{2}(\\w{6})\\W*$", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            r = Integer.parseInt(m.group(1).substring(0, 2), 16);
            g = Integer.parseInt(m.group(1).substring(2, 4), 16);
            b = Integer.parseInt(m.group(1).substring(4, 6), 16);
            return modifyColor(backgroundColor, new RGB(r, g, b));
        }

        // 处理 #000000 类型的颜色
        m = Pattern.compile("#\\W*(\\w{6})\\W*$", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            r = Integer.parseInt(m.group(1).substring(0, 2), 16);
            g = Integer.parseInt(m.group(1).substring(2, 4), 16);
            b = Integer.parseInt(m.group(1).substring(4, 6), 16);
            return modifyColor(backgroundColor, new RGB(r, g, b));
        }

        // 处理 #0F0 类型的颜色
        m = Pattern.compile("#\\W*(\\w{3})\\W*$", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            tmp = m.group(1).substring(0, 1);
            r = Integer.parseInt(tmp + tmp, 16);
            tmp = m.group(1).substring(1, 2);
            g = Integer.parseInt(tmp + tmp, 16);
            tmp = m.group(1).substring(2, 3);
            b = Integer.parseInt(tmp + tmp, 16);
            return modifyColor(backgroundColor, new RGB(r, g, b));
        }

        // 处理颜色
        m = Pattern.compile("LightPink", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 182, 193));
        }
        m = Pattern.compile("Pink", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 192, 203));
        }
        m = Pattern.compile("Crimson", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(220, 20, 60));
        }
        m = Pattern.compile("LavenderBlush", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 240, 245));
        }
        m = Pattern.compile("PaleVioletRed", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(219, 112, 147));
        }
        m = Pattern.compile("HotPink", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 105, 180));
        }
        m = Pattern.compile("DeepPink", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 20, 147));
        }
        m = Pattern.compile("MediumVioletRed", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(199, 21, 133));
        }
        m = Pattern.compile("Orchid", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(218, 112, 214));
        }
        m = Pattern.compile("Thistle", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(216, 191, 216));
        }
        m = Pattern.compile("plum", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(221, 160, 221));
        }
        m = Pattern.compile("Violet", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(238, 130, 238));
        }
        m = Pattern.compile("Magenta", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 0, 255));
        }
        m = Pattern.compile("Fuchsia", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 0, 255));
        }
        m = Pattern.compile("DarkMagenta", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(139, 0, 139));
        }
        m = Pattern.compile("Purple", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(128, 0, 128));
        }
        m = Pattern.compile("MediumOrchid", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(186, 85, 211));
        }
        m = Pattern.compile("DarkVoilet", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(148, 0, 211));
        }
        m = Pattern.compile("DarkOrchid", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(153, 50, 204));
        }
        m = Pattern.compile("Indigo", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(75, 0, 130));
        }
        m = Pattern.compile("BlueViolet", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(138, 43, 226));
        }
        m = Pattern.compile("MediumPurple", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(147, 112, 219));
        }
        m = Pattern.compile("MediumSlateBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(123, 104, 238));
        }
        m = Pattern.compile("SlateBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(106, 90, 205));
        }
        m = Pattern.compile("DarkSlateBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(72, 61, 139));
        }
        m = Pattern.compile("Lavender", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(230, 230, 250));
        }
        m = Pattern.compile("GhostWhite", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(248, 248, 255));
        }
        m = Pattern.compile("Blue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 0, 255));
        }
        m = Pattern.compile("MediumBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 0, 205));
        }
        m = Pattern.compile("MidnightBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(25, 25, 112));
        }
        m = Pattern.compile("DarkBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 0, 139));
        }
        m = Pattern.compile("Navy", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 0, 128));
        }
        m = Pattern.compile("RoyalBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(65, 105, 225));
        }
        m = Pattern.compile("CornflowerBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(100, 149, 237));
        }
        m = Pattern.compile("LightSteelBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(176, 196, 222));
        }
        m = Pattern.compile("LightSlateGray", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(119, 136, 153));
        }
        m = Pattern.compile("SlateGray", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(112, 128, 144));
        }
        m = Pattern.compile("DodgerBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(30, 144, 255));
        }
        m = Pattern.compile("AliceBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(240, 248, 255));
        }
        m = Pattern.compile("SteelBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(70, 130, 180));
        }
        m = Pattern.compile("LightSkyBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(135, 206, 250));
        }
        m = Pattern.compile("SkyBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(135, 206, 235));
        }
        m = Pattern.compile("DeepSkyBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 191, 255));
        }
        m = Pattern.compile("LightBLue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(173, 216, 230));
        }
        m = Pattern.compile("PowDerBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(176, 224, 230));
        }
        m = Pattern.compile("CadetBlue", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(95, 158, 160));
        }
        m = Pattern.compile("Azure", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(240, 255, 255));
        }
        m = Pattern.compile("LightCyan", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(225, 255, 255));
        }
        m = Pattern.compile("PaleTurquoise", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(175, 238, 238));
        }
        m = Pattern.compile("Cyan", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 255, 255));
        }
        m = Pattern.compile("Aqua", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 255, 255));
        }
        m = Pattern.compile("DarkTurquoise", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 206, 209));
        }
        m = Pattern.compile("DarkSlateGray", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(47, 79, 79));
        }
        m = Pattern.compile("DarkCyan", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 139, 139));
        }
        m = Pattern.compile("Teal", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 128, 128));
        }
        m = Pattern.compile("MediumTurquoise", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(72, 209, 204));
        }
        m = Pattern.compile("LightSeaGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(32, 178, 170));
        }
        m = Pattern.compile("Turquoise", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(64, 224, 208));
        }
        m = Pattern.compile("BabyGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(127, 255, 170));
        }
        m = Pattern.compile("MediumAquamarine", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 250, 154));
        }
        m = Pattern.compile("MediumSpringGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(245, 255, 250));
        }
        m = Pattern.compile("MintCream", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 255, 127));
        }
        m = Pattern.compile("SpringGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(60, 179, 113));
        }
        m = Pattern.compile("SeaGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(46, 139, 87));
        }
        m = Pattern.compile("Honeydew", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(240, 255, 0));
        }
        m = Pattern.compile("LightGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(144, 238, 144));
        }
        m = Pattern.compile("PaleGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(152, 251, 152));
        }
        m = Pattern.compile("DarkSeaGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(143, 188, 143));
        }
        m = Pattern.compile("LimeGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(50, 205, 50));
        }
        m = Pattern.compile("Lime", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 255, 0));
        }
        m = Pattern.compile("ForestGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(34, 139, 34));
        }
        m = Pattern.compile("Green", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 128, 0));
        }
        m = Pattern.compile("DarkGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 100, 0));
        }
        m = Pattern.compile("Chartreuse", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(127, 255, 0));
        }
        m = Pattern.compile("LawnGreen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(124, 252, 0));
        }
        m = Pattern.compile("GreenYellow", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(173, 255, 47));
        }
        m = Pattern.compile("OliveDrab", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(85, 107, 47));
        }
        m = Pattern.compile("Beige", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(107, 142, 35));
        }
        m = Pattern.compile("LightGoldenrodYellow", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(250, 250, 210));
        }
        m = Pattern.compile("Ivory", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 255, 240));
        }
        m = Pattern.compile("LightYellow", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 255, 224));
        }
        m = Pattern.compile("Yellow", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 255, 0));
        }
        m = Pattern.compile("Olive", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(128, 128, 0));
        }
        m = Pattern.compile("DarkKhaki", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(189, 183, 107));
        }
        m = Pattern.compile("LemonChiffon", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 250, 205));
        }
        m = Pattern.compile("PaleGodenrod", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(238, 232, 170));
        }
        m = Pattern.compile("Khaki", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(240, 230, 140));
        }
        m = Pattern.compile("Gold", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 215, 0));
        }
        m = Pattern.compile("Cornislk", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 248, 220));
        }
        m = Pattern.compile("GoldEnrod", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(218, 165, 32));
        }
        m = Pattern.compile("FloralWhite", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 250, 240));
        }
        m = Pattern.compile("OldLace", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(253, 245, 230));
        }
        m = Pattern.compile("Wheat", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(245, 222, 179));
        }
        m = Pattern.compile("Moccasin", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 228, 181));
        }
        m = Pattern.compile("Orange", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 165, 0));
        }
        m = Pattern.compile("PapayaWhip", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 239, 213));
        }
        m = Pattern.compile("BlanchedAlmond", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 235, 205));
        }
        m = Pattern.compile("NavajoWhite", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 222, 173));
        }
        m = Pattern.compile("AntiqueWhite", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(250, 235, 215));
        }
        m = Pattern.compile("Tan", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(210, 180, 140));
        }
        m = Pattern.compile("BrulyWood", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(222, 184, 135));
        }
        m = Pattern.compile("Bisque", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 228, 196));
        }
        m = Pattern.compile("DarkOrange", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 140, 0));
        }
        m = Pattern.compile("Linen", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(250, 240, 230));
        }
        m = Pattern.compile("Peru", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(205, 133, 63));
        }
        m = Pattern.compile("PeachPuff", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 218, 185));
        }
        m = Pattern.compile("SandyBrown", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(244, 164, 96));
        }
        m = Pattern.compile("Chocolate", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(210, 105, 30));
        }
        m = Pattern.compile("SaddleBrown", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(139, 69, 19));
        }
        m = Pattern.compile("SeaShell", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 245, 238));
        }
        m = Pattern.compile("Sienna", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(160, 82, 45));
        }
        m = Pattern.compile("LightSalmon", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 160, 122));
        }
        m = Pattern.compile("Coral", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 127, 80));
        }
        m = Pattern.compile("OrangeRed", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 69, 0));
        }
        m = Pattern.compile("DarkSalmon", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(233, 150, 122));
        }
        m = Pattern.compile("Tomato", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 99, 71));
        }
        m = Pattern.compile("MistyRose", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 228, 225));
        }
        m = Pattern.compile("Salmon", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(250, 128, 114));
        }
        m = Pattern.compile("Snow", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 250, 250));
        }
        m = Pattern.compile("LightCoral", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(240, 128, 128));
        }
        m = Pattern.compile("RosyBrown", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(188, 143, 143));
        }
        m = Pattern.compile("IndianRed", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(205, 92, 92));
        }
        m = Pattern.compile("Red", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 0, 0));
        }
        m = Pattern.compile("Brown", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(165, 42, 42));
        }
        m = Pattern.compile("FireBrick", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(178, 34, 34));
        }
        m = Pattern.compile("DarkRed", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(139, 0, 0));
        }
        m = Pattern.compile("Maroon", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(128, 0, 0));
        }
        m = Pattern.compile("White", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(255, 255, 255));
        }
        m = Pattern.compile("WhiteSmoke", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(245, 245, 245));
        }
        m = Pattern.compile("Gainsboro", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(220, 220, 220));
        }
        m = Pattern.compile("LightGray", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(211, 211, 211));
        }
        m = Pattern.compile("Silver", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(192, 192, 192));
        }
        m = Pattern.compile("DarkGray", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(169, 169, 169));
        }
        m = Pattern.compile("Gray", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(128, 128, 128));
        }
        m = Pattern.compile("DimGray", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(105, 105, 105));
        }
        m = Pattern.compile("Black", Pattern.CASE_INSENSITIVE).matcher(color);
        if (m.find()) {
            return modifyColor(backgroundColor, new RGB(0, 0, 0));
        }
        return color;
    }

    /**
     * 计算2个颜色的距离，若小于阈值则返回反色
     */
    private String modifyColor(RGB e1, RGB e2) {
        // System.out.println("对比颜色：" + e1 + "   =   " +e2 );
        long rmean = ((long) e1.r + (long) e2.r) / 2;
        long r = (long) e1.r - (long) e2.r;
        long g = (long) e1.g - (long) e2.g;
        long b = (long) e1.b - (long) e2.b;
        // 黑白色距离约764.8，这里居中取382
        if (Math.sqrt((((512 + rmean) * r * r) >> 8) + 4 * g * g + (((767 - rmean) * b * b) >> 8)) < 382) {
            return "rgb(" + getInverseColor(e2.r) + ", " + getInverseColor(e2.g) + ", " + getInverseColor(e2.b) + ")";
//            return "#" + Integer.toHexString(getInverseColor(e2.r)) + Integer.toHexString(getInverseColor(e2.g)) + Integer.toHexString(getInverseColor(e2.b));
        } else {
            return "rgb(" + e2.r + ", " + e2.g + ", " + e2.b + ")";
        }
    }


    // https://blog.csdn.net/do168/article/details/51619656
    private static int getInverseColor(int color) {
        return 255 - color;
    }

    private static int getInverseColor2(int color) {
        if (color > 64 && color < 128)
            color -= 64;
        else if (color >= 128 && color < 192)
            color += 64;
        return 255 - color;
    }
}
