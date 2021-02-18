package me.wizos.loread.utils;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.StringRes;

import com.elvishew.xlog.XLog;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;


@SuppressWarnings({"WeakerAccess"})
public class StringUtils {
    private final static HashMap<Character, Integer> ChnMap = getChnMap();

    public static boolean isEmpty(List list) {
        return list == null || list.isEmpty();
    }
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static String toFirstCapital(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String getString(@StringRes int id) {
        return App.i().getResources().getString(id);
    }

    public static String getString(@StringRes int id, Object... formatArgs) {
        return App.i().getString(id, formatArgs);
    }

    /**
     * 只对url中的汉字部分进行 urlEncode 。
     * @param url
     * @return
     */
    public static String urlEncode(String url){
        if(isEmpty(url)){
            return "";
        }
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, length = url.length(); i < length; i++) {
                char c = url.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append( URLEncoder.encode( String.valueOf(c),"utf-8") );
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
            return url;
        }
    }

    /**
     * 将文本中的半角字符，转换成全角字符
     */
    public static String halfToFull(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) //半角空格
            {
                c[i] = (char) 12288;
                continue;
            }
            //根据实际情况，过滤不需要转换的符号
            //if (c[i] == 46) //半角点号，不转换
            // continue;

            if (c[i] > 32 && c[i] < 127)    //其他符号都转换为全角
                c[i] = (char) (c[i] + 65248);
        }
        return new String(c);
    }

    //功能：字符串全角转换为半角
    public static String fullToHalf(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) //全角空格
            {
                c[i] = (char) 32;
                continue;
            }

            if (c[i] > 65280 && c[i] < 65375) {
                c[i] = (char) (c[i] - 65248);
            }

        }
        return new String(c);
    }

    private static HashMap<Character, Integer> getChnMap() {
        HashMap<Character, Integer> map = new HashMap<>();
        String cnStr = "零一二三四五六七八九十";
        char[] c = cnStr.toCharArray();
        for (int i = 0; i <= 10; i++) {
            map.put(c[i], i);
        }
        cnStr = "〇壹贰叁肆伍陆柒捌玖拾";
        c = cnStr.toCharArray();
        for (int i = 0; i <= 10; i++) {
            map.put(c[i], i);
        }
        map.put('两', 2);
        map.put('百', 100);
        map.put('佰', 100);
        map.put('千', 1000);
        map.put('仟', 1000);
        map.put('万', 10000);
        map.put('亿', 100000000);
        return map;
    }

    @SuppressWarnings("ConstantConditions")
    public static int chineseNumToInt(String chNum) {
        int result = 0;
        int tmp = 0;
        int billion = 0;
        char[] cn = chNum.toCharArray();

        // "一零二五" 形式
        if (cn.length > 1 && chNum.matches("^[〇零一二三四五六七八九壹贰叁肆伍陆柒捌玖]$")) {
            for (int i = 0; i < cn.length; i++) {
                cn[i] = (char) (48 + ChnMap.get(cn[i]));
            }
            return Integer.parseInt(new String(cn));
        }

        // "一千零二十五", "一千二" 形式
        try {
            for (int i = 0; i < cn.length; i++) {
                int tmpNum = ChnMap.get(cn[i]);
                if (tmpNum == 100000000) {
                    result += tmp;
                    result *= tmpNum;
                    billion = billion * 100000000 + result;
                    result = 0;
                    tmp = 0;
                } else if (tmpNum == 10000) {
                    result += tmp;
                    result *= tmpNum;
                    tmp = 0;
                } else if (tmpNum >= 10) {
                    if (tmp == 0)
                        tmp = 1;
                    result += tmpNum * tmp;
                    tmp = 0;
                } else {
                    if (i >= 2 && i == cn.length - 1 && ChnMap.get(cn[i - 1]) > 10)
                        tmp = tmpNum * ChnMap.get(cn[i - 1]) / 10;
                    else
                        tmp = tmp * 10 + tmpNum;
                }
            }
            result += tmp + billion;
            return result;
        } catch (Exception e) {
            return -1;
        }
    }

    public static int stringToInt(String str) {
        if (str != null) {
            String num = fullToHalf(str).replaceAll("\\s", "");
            try {
                return Integer.parseInt(num);
            } catch (Exception e) {
                return chineseNumToInt(num);
            }
        }
        return -1;
    }

    public static String base64Decode(String str) {
        byte[] bytes = Base64.decode(str, Base64.DEFAULT);
        try {
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new String(bytes);
        }
    }

    public static String escape(String src) {
        int i;
        char j;
        StringBuilder tmp = new StringBuilder();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j)
                    || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    public static boolean isJsonType(String str) {
        boolean result = false;
        if (!TextUtils.isEmpty(str)) {
            str = str.trim();
            if (str.startsWith("{") && str.endsWith("}")) {
                result = true;
            } else if (str.startsWith("[") && str.endsWith("]")) {
                result = true;
            }
        }
        return result;
    }

    public static boolean isJsonObject(String text) {
        boolean result = false;
        if (!TextUtils.isEmpty(text)) {
            text = text.trim();
            if (text.startsWith("{") && text.endsWith("}")) {
                result = true;
            }
        }
        return result;
    }

    public static boolean isJsonArray(String text) {
        boolean result = false;
        if (!TextUtils.isEmpty(text)) {
            text = text.trim();
            if (text.startsWith("[") && text.endsWith("]")) {
                result = true;
            }
        }
        return result;
    }

    public static boolean isTrimEmpty(String text) {
        if (text == null) return true;
        if (text.length() == 0) return true;
        return text.trim().length() == 0;
    }

    public static boolean startWithIgnoreCase(String src, String obj) {
        if (src == null || obj == null) return false;
        if (obj.length() > src.length()) return false;
        return src.substring(0, obj.length()).equalsIgnoreCase(obj);
    }

    public static boolean endWithIgnoreCase(String src, String obj) {
        if (src == null || obj == null) return false;
        if (obj.length() > src.length()) return false;
        return src.substring(src.length() - obj.length()).equalsIgnoreCase(obj);
    }

    /**
     * delimiter 分隔符
     * elements 需要连接的字符数组
     */
    public static String join(CharSequence delimiter, CharSequence... elements) {
        // 空指针判断
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);

        // Number of elements not likely worth Arrays.stream overhead.
        // 此处用到了StringJoiner(JDK 8引入的类）
        // 先构造一个以参数delimiter为分隔符的StringJoiner对象
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs : elements) {
            // 拼接字符
            joiner.add(cs);
        }
        return joiner.toString();
    }

    public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
        if (elements == null) return null;
        if (delimiter == null) delimiter = ",";
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs : elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }
    public static String joinLong(CharSequence delimiter, Iterable<? extends Long> elements) {
        if (elements == null) return null;
        if (delimiter == null) delimiter = ",";
        StringJoiner joiner = new StringJoiner(delimiter);
        for (Long cs : elements) {
            joiner.add(String.valueOf(cs));
        }
        return joiner.toString();
    }

    public static boolean isContainNumber(String company) {
        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(company);
        return m.find();
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static String getBaseUrl(String url) {
        if (url == null || !url.startsWith("http")) return null;
        int index = url.indexOf("/", 9);
        if (index == -1) {
            return url;
        }
        return url.substring(0, index);
    }

    // 移除字符串首尾空字符的高效方法(利用ASCII值判断,包括全角空格)
    public static String trim(String s) {
        if (isEmpty(s)) return "";
        int start = 0, len = s.length();
        int end = len - 1;
        while ((start < end) && ((s.charAt(start) <= 0x20) || (s.charAt(start) == '　'))) {
            ++start;
        }
        while ((start < end) && ((s.charAt(end) <= 0x20) || (s.charAt(end) == '　'))) {
            --end;
        }
        if (end < len) ++end;
        return ((start > 0) || (end < len)) ? s.substring(start, end) : s;
    }

    public static String repeat(String str, int n) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    public static String removeUTFCharacters(String data) {
        if (data == null) return null;
        Pattern p = Pattern.compile("\\\\u(\\p{XDigit}{4})");
        Matcher m = p.matcher(data);
        StringBuffer buf = new StringBuffer(data.length());
        while (m.find()) {
            String ch = String.valueOf((char) Integer.parseInt(m.group(1), 16));
            m.appendReplacement(buf, Matcher.quoteReplacement(ch));
        }
        m.appendTail(buf);
        return buf.toString();
    }

    public static String formatHtml(String html) {
        if (isEmpty(html)) return "";
        return html.replaceAll("(?i)<(br[\\s/]*|/*p.*?|/*div.*?)>", "\n")// 替换特定标签为换行符
                .replaceAll("<[script>]*.*?>|&nbsp;", "")// 删除script标签对和空格转义符
                .replaceAll("\\s*\\n+\\s*", "\n　　")// 移除空行,并增加段前缩进2个汉字
                .replaceAll("^[\\n\\s]+", "　　")//移除开头空行,并增加段前缩进2个汉字
                .replaceAll("[\\n\\s]+$", "");//移除尾部空行
    }


    public static String getSuffix(InputStream in) {
        byte[] b = new byte[10];
        int l = -1;
        try {
            l = in.read(b);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (l == 10) {
            byte b0 = b[0];
            byte b1 = b[1];
            byte b2 = b[2];
            byte b3 = b[3];
            byte b6 = b[6];
            byte b7 = b[7];
            byte b8 = b[8];
            byte b9 = b[9];
            XLog.i("得到头：" + b);
            if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F') {
                return "gif";
            } else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G') {
                return "png";
            } else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I' && b9 == (byte) 'F') {
                return "jpg";
            } else {
                return null;
            }
        } else {
            return null;
        }
    }




    // https://blog.csdn.net/yan3013216087/article/details/81450658
    // 保留合法字符
    public static String keepValidXMLChars(String str) {
        if (StringUtils.isEmpty(str)) return str; // vacancy test.
        StringBuilder out = new StringBuilder(); // Used to hold the output.
        char current; // Used to reference the current character.
        for (int i = 0; i < str.length(); i++) {
            current = str.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if (current == 0x9 || current == 0xA || current == 0xD || current >= 0x20 && current <= 0xD7FF || current >= 0xE000 && current <= 0xFFFD)
                out.append(current);
        }
        return out.toString();
    }
    //过滤非法字符
    //注意，以下正则表达式过滤不全面，过滤范围为：0x00 - 0x08, 0x0b - 0x0c, 0x0e - 0x1f
    public static String stripInvalidXMLChars(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return str.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
    }

}
