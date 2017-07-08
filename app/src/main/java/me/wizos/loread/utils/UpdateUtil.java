package me.wizos.loread.utils;

import com.socks.library.KLog;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;

/**
 * 一些升级用的业务数据
 * Created by Wizos on 2017/7/7.
 */

public class UpdateUtil {

    public static boolean filterAllDirName(File dir) {
        UpdateUtil.filterAllDirName(new File(App.i().getExternalFilesDir(null) + File.separator + "test"));
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                filterAllDirName(file);
            }
            Pattern emoji = Pattern.compile(EmojiUtil.getEmojiRegex(), Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);// 后2个参数要进行大小不明感的匹配
            Matcher emojiMatcher = emoji.matcher(dir.getName());
            if (emojiMatcher.find()) {
//                source = emojiMatcher.replaceAll("");
                KLog.e("====");
                FileUtil.moveDir(dir.getPath(), dir.getParent() + File.separator + StringUtil.filterTitle(dir.getName()));
            }
            return true;
        } else {
//            KLog.i( dir + "只是文件");
//            String xx = dir.getName(); // .replace(".html","").replace(".txt","")
            Pattern emoji = Pattern.compile(EmojiUtil.getEmojiRegex(), Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);// 后2个参数要进行大小不明感的匹配
            Matcher emojiMatcher = emoji.matcher(dir.getName());
            if (emojiMatcher.find()) {
//                source = emojiMatcher.replaceAll("");
                KLog.e("++++");
                String file = dir.getParent() + File.separator + StringUtil.filterTitle(dir.getName());
                FileUtil.moveFile(dir.getPath(), file);
                FileUtil.saveHtml(file, EmojiUtil.filterEmoji(FileUtil.readHtml(file)));
//                KLog.e("++++" +  dir.getPath() + "==" + dir.getParent() + File.separator + dir.getAbsolutePath() );
            }
            return true;
        }
    }


//    public boolean update() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<Article> arts = WithDB.i().loadArtWhere();
//                KLog.d("升级文章数量：" + arts.size());
//                if (arts.size() == 0) {
//                    return;
//                }
//                String imgState, folder;
//                Pattern r;
//                Matcher m;
//                for (Article article : arts) {
//                    imgState = article.getImgState();
//                    if (imgState.equals("") || imgState.equals("OK") || imgState.equals("ok")) {
//                        continue;
//                    } else {
//                        // 验证规则
////                        String filter = ",\"saveSrc\":.*?loread\"";
////                        // 编译正则表达式
////                        pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE); // 忽略大小写的写法
////                        matcher = pattern.matcher( imgState );
////                        matcher.replaceAll(""); // 字符串是否与正则表达式相匹配
////
////                        pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE); // 忽略大小写的写法
////                        matcher = pattern.matcher( imgState );
////                        matcher.replaceAll(""); // 字符串是否与正则表达式相匹配
//                        if (imgState.contains("\"folder\":\"")) {
//                            continue;
//                        }
//                        if (article.getSaveDir() == null) {
//                            article.setSaveDir(API.SAVE_DIR_CACHE);
//                        }
//                        folder = FileUtil.getFolder(article.getSaveDir(), StringUtil.stringToMD5(article.getId()), article.getTitle());
//
//                        r = Pattern.compile(",\"saveSrc\":.*?loread\"");// 创建 Pattern 对象
//                        m = r.matcher(imgState);// 现在创建 matcher 对象
//                        imgState = m.replaceAll("");
//
//                        r = Pattern.compile("\"localSrc\":.*?_files\\/(.*?)loread\"");// 创建 Pattern 对象
//                        m = r.matcher(imgState);// 现在创建 matcher 对象
//                        imgState = m.replaceAll("\"imgName\":\"$1loread\"");
//
////                        imgState.replaceAll(",\"saveSrc\":.*?loread\"", "" );
////                        imgState.replaceAll("\"localSrc\":.*?_files\\/(.*?)loread\"", "\"imgName\":\"$1loread\"" );
//                        KLog.d("升级文章含有：" + imgState.contains("\"imgStatus\":1,"));
//                        if (imgState.contains("\"imgStatus\":1,")) {
////                            int p = imgState.indexOf("\"imgStatus\":1,");
//                            imgState = imgState.replace("\"imgStatus\":1,", "\"imgStatus\":1,\"folder\":\"" + folder + "_files\",");
//                        }
//                        if (imgState.contains("\"imgStatus\":0,")) {
//                            imgState = imgState.replace("\"imgStatus\":0,", "\"imgStatus\":0,\"folder\":\"" + folder + "_files\",");
//                        }
//
//                        KLog.d("升级文章内容：" + imgState);
////                        try {
////                            Thread.sleep(21000);
////                        }catch (Exception e){
////
////                        }
////                        Gson gson = new Gson();
////                        Type type = new TypeToken<ExtraImg>() {}.getType();
////                        try {
////                            extraImg = gson.fromJson(imgState, type);
////                            if ( extraImg.getFolder()==null ){
////                                extraImg.setFolder( FileUtil.getFolder( article.getSaveDir(), StringUtil.stringToMD5(article.getId()), article.getTitle() ) );
////                            }
////                            folder = extraImg.getFolder();
////                            lossSrcList = extraImg.getLossImgs();
////                            obtainSrcList = extraImg.getObtainImgs();
//////                    KLog.e("重新进入获取到的imgState记录" + imgState + extraImg +  lossSrcList + obtainSrcList);
////                        } catch (RuntimeException e) {
////                            continue;
////                        }
//                    }
//                    article.setImgState(imgState);
//                    WithDB.i().saveArticle(article);
//                }
//                mainHandler.sendEmptyMessage(1000);
//            }
//        }).start();
////        KLog.d( "升级完成" );
//        return true;
//    }
}
