package me.wizos.loread.utils;

import com.elvishew.xlog.XLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;

public class OPMLUtils {
    public static void export(String title, File file, List<Feed> feeds) {
        if (feeds == null || feeds.size() == 0) {
            XLog.w("需要导出的feeds为空");
            return;
        }

        Document doc;
        Element bodyNode;
        Element categoryNode;
        Element feedNode;

        String baseUri = Contract.SCHEMA_LOREAD + file.getName();

        if (!file.exists()) {
            Document.OutputSettings settings = new Document.OutputSettings();
            settings.syntax(Document.OutputSettings.Syntax.xml).prettyPrint(true);
            doc = new Document(baseUri).outputSettings(settings);
            Element opml = doc.appendElement("opml").attr("version", "1.0");
            doc.charset(Charset.defaultCharset());
            opml.appendElement("head").appendElement("title").text(title);
            bodyNode = opml.appendElement("body");
        } else {
            doc = Jsoup.parse(FileUtils.readFile(file), baseUri, Parser.xmlParser());
            bodyNode = doc.selectFirst("opml > body");
        }

        List<Category> categories;
        for (Feed feed : feeds) {
            categories = CoreDB.i().categoryDao().getByFeedId(App.i().getUser().getId(),feed.getId());
            for (Category category:categories){
                categoryNode = doc.selectFirst("opml > body > outline[title=\"" + category.getTitle() + "\"]");
                if (categoryNode == null) {
                    categoryNode = bodyNode.appendElement("outline").attr("title", category.getTitle()).attr("text", category.getTitle());
                }

                feedNode = categoryNode.selectFirst("outline[title=\"" + feed.getTitle() + "\"]");
                if (feedNode != null) {
                    continue;
                }
                feedNode = Jsoup.parseBodyFragment("<outline/>").body().child(0);
                feedNode.attr("title", feed.getTitle())
                        .attr("text", feed.getTitle())
                        .attr("type", "rss")
                        .attr("xmlUrl", feed.getFeedUrl())
                        .attr("htmlUrl", feed.getHtmlUrl());
                categoryNode.appendChild(feedNode);
            }
        }

        FileUtils.save(file, doc.outerHtml());
    }
}
