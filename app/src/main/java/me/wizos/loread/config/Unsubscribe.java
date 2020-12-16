package me.wizos.loread.config;


import android.os.Environment;

import com.elvishew.xlog.XLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.bean.domain.OutFeed;
import me.wizos.loread.bean.domain.OutTag;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.User;
import me.wizos.loread.utils.FileUtil;

/**
 * Created by Wizos on 2019/5/14.
 */

public class Unsubscribe {
    public static void genBackupFile2(User user, List<Feed> feeds) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            XLog.e("外置存储设备不可用");
            return;
        }
        if (feeds.size() == 0) {
            return;
        }
        File docDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!docDir.isDirectory()) {
            return;
        }

        String baseUri = "loread://unsubscribe.feed";
        File file = new File(docDir.getAbsolutePath() + File.separator + user.getSource() + "_" + user.getUserName() + "_unsubscribe.opml");
        Document doc;
        Element bodyNode;
        Element tagNode;
        Element feedNode;

        if (!file.exists()) {
            Document.OutputSettings settings = new Document.OutputSettings();
            settings.syntax(Document.OutputSettings.Syntax.xml).prettyPrint(true);
            doc = new Document(baseUri).outputSettings(settings);
            Element opml = doc.appendElement("opml").attr("version", "1.0");
            doc.charset(Charset.defaultCharset());
            String title = "Subscriptions of " + user.getUserName() + " from " + user.getSource();
            opml.appendElement("head").appendElement("title").text(title);
            bodyNode = opml.appendElement("body");
        } else {
            doc = Jsoup.parse(FileUtil.readFile(file), baseUri, Parser.xmlParser());
            bodyNode = doc.selectFirst("opml > body");
        }

        List<Category> categories;
        for (Feed feed : feeds) {
            //categories = WithDB.i().getCategoriesByFeedId(feed.getId());
            categories = CoreDB.i().categoryDao().getByFeedId(App.i().getUser().getId(),feed.getId());
            for (Category category:categories){
                tagNode = doc.selectFirst("opml > body > outline[title=\"" + category.getTitle() + "\"]");
                if (tagNode == null) {
                    tagNode = bodyNode.appendElement("outline").attr("title", category.getTitle()).attr("text", category.getTitle());
                }

                feedNode = tagNode.selectFirst("outline[title=\"" + feed.getTitle() + "\"]");
                if (feedNode != null) {
                    continue;
                }
                feedNode = createSelfClosingElement("outline");
                feedNode.attr("title", feed.getTitle())
                        .attr("text", feed.getTitle())
                        .attr("type", "rss")
                        .attr("xmlUrl", feed.getFeedUrl())
                        .attr("htmlUrl", feed.getHtmlUrl());
                tagNode.appendChild(feedNode);
            }
        }

        FileUtil.save(file, doc.outerHtml());
    }



    @Deprecated
    public static void genBackupFile(User user, String baseUri, ArrayList<OutTag> outTags) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            XLog.e("外置存储设备不可用");
            return;
        }
        File docDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!docDir.isDirectory()) {
            return;
        }

        File file = new File(docDir.getAbsolutePath() + File.separator + user.getSource() + "_" + user.getUserName() + "_unsubscribe.opml");
        Document doc;
        if (file.exists()) {
            // doc = Jsoup.parse(file, DataUtil.getCharsetFromContentType( FileUtil.readFile(file)),baseUri);
            doc = Jsoup.parse(FileUtil.readFile(file), baseUri, Parser.xmlParser());
            Element bodyNode = doc.selectFirst("opml > body");
            Element tagNode;
            Element feedNode;
            ArrayList<OutFeed> outFeeds;
            for (OutTag outTag : outTags) {
                tagNode = doc.selectFirst("opml > body > outline[title=\"" + outTag.getTitle() + "\"]");
                if (tagNode == null) {
                    tagNode = bodyNode.appendElement("outline").attr("title", outTag.getTitle()).attr("text", outTag.getTitle());
                }
                outFeeds = outTag.getOutFeeds();
                for (OutFeed outFeed : outFeeds) {
                    feedNode = tagNode.selectFirst("outline[title=\"" + outFeed.getTitle() + "\"]");
                    if (feedNode != null) {
                        continue;
                    }
                    feedNode = createSelfClosingElement("outline");
                    feedNode.attr("title", outFeed.getTitle())
                            .attr("text", outFeed.getTitle())
                            .attr("type", "rss")
                            .attr("xmlUrl", outFeed.getFeedUrl())
                            .attr("htmlUrl", outFeed.getHtmlUrl());
                    tagNode.appendChild(feedNode);
                }
            }
        } else {
            Document.OutputSettings settings = new Document.OutputSettings();
            settings.syntax(Document.OutputSettings.Syntax.xml).prettyPrint(true);
            doc = new Document(baseUri).outputSettings(settings);
            Element opml = doc.appendElement("opml").attr("version", "1.0");
            doc.charset(Charset.defaultCharset());
            String title = "Subscriptions of " + user.getUserName() + " from " + user.getSource();
            opml.appendElement("head").appendElement("title").text(title);
            Element body = opml.appendElement("body");
            Element tag;
            Element feed;

            ArrayList<OutFeed> outFeeds;
            //XLog.e("获取A：" + outTags.toString()  );
            for (OutTag outTag : outTags) {
                tag = body.appendElement("outline").attr("title", outTag.getTitle()).attr("text", outTag.getTitle());
                outFeeds = outTag.getOutFeeds();
                //XLog.e("获取B：" + outFeeds.toString()  );
                for (OutFeed outFeed : outFeeds) {
                    feed = createSelfClosingElement("outline");
                    feed.attr("title", outFeed.getTitle())
                            .attr("text", outFeed.getTitle())
                            .attr("type", "rss")
                            .attr("xmlUrl", outFeed.getFeedUrl())
                            .attr("htmlUrl", outFeed.getHtmlUrl());
                    tag.appendChild(feed);
                }
            }
        }
        FileUtil.save(file, doc.outerHtml());
    }

    private static Element createSelfClosingElement(String tagName) {
        return Jsoup.parseBodyFragment("<" + tagName + "/>").body().child(0);
    }

    private static Element createVoidElement(String tagName, String baseUri) {
        Document document = Jsoup.parse("<" + tagName + "/>", baseUri, Parser.xmlParser());
        document.outputSettings().prettyPrint(false);
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        return document.body() == null ? document.child(0) : document.body().child(0);
    }




    public static void genBackupFile3(User user, List<Feed> feeds) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            XLog.e("外置存储设备不可用");
            return;
        }
        File docDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!docDir.isDirectory()) {
            return;
        }

        String baseUri = "loread://unsubscribe.feed";
        File file = new File(docDir.getAbsolutePath() + File.separator + user.getSource() + "_" + user.getUserName() + "_unsubscribe.opml");
        Document doc;
        Element bodyNode;
        Element tagNode;
        Element feedNode;

        if (!file.exists()) {
            Document.OutputSettings settings = new Document.OutputSettings();
            settings.syntax(Document.OutputSettings.Syntax.xml).prettyPrint(true);
            doc = new Document(baseUri).outputSettings(settings);
            Element opml = doc.appendElement("opml").attr("version", "1.0");
            doc.charset(Charset.defaultCharset());
            String title = "Subscriptions of " + user.getUserName() + " from " + user.getSource();
            opml.appendElement("head").appendElement("title").text(title);
            bodyNode = opml.appendElement("body");
        } else {
            doc = Jsoup.parse(FileUtil.readFile(file), baseUri, Parser.xmlParser());
            bodyNode = doc.selectFirst("opml > body");
        }

        List<Category> categories;
        for (Feed feed : feeds) {
            //categories = WithDB.i().getCategoriesByFeedId(feed.getId());
            categories = CoreDB.i().categoryDao().getByFeedId(App.i().getUser().getId(),feed.getId());
            for (Category category:categories){
                tagNode = doc.selectFirst("opml > body > outline[title=\"" + category.getTitle() + "\"]");
                if (tagNode == null) {
                    tagNode = bodyNode.appendElement("outline").attr("title", category.getTitle()).attr("text", category.getTitle());
                }

                feedNode = tagNode.selectFirst("outline[title=\"" + feed.getTitle() + "\"]");
                if (feedNode != null) {
                    continue;
                }
                feedNode = createSelfClosingElement("outline");
                feedNode.attr("title", feed.getTitle())
                        .attr("text", feed.getTitle())
                        .attr("type", "rss")
                        .attr("xmlUrl", feed.getFeedUrl())
                        .attr("htmlUrl", feed.getHtmlUrl());
                tagNode.appendChild(feedNode);
            }
        }

        FileUtil.save(file, doc.outerHtml());
    }

}
