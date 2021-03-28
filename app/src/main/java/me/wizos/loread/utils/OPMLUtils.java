package me.wizos.loread.utils;

import com.elvishew.xlog.XLog;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.bean.collectiontree.Collection;
import me.wizos.loread.bean.collectiontree.CollectionFeed;
import me.wizos.loread.bean.collectiontree.CollectionTree;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;

public class OPMLUtils {
    public static void export(String title, File file, List<Feed> feeds) {
        Document doc;
        Element bodyNode;
        Element categoryNode;
        Element feedNode;

        String baseUri = Contract.SCHEMA_LOREAD + file.getName();

        if (!file.exists()) {
            Document.OutputSettings settings = new Document.OutputSettings();
            settings.syntax(Document.OutputSettings.Syntax.xml).prettyPrint(false);
            doc = new Document(baseUri).parser(Parser.xmlParser()).outputSettings(settings);
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
                categoryNode = doc.selectFirst("opml > body > outline[text=\"" + category.getTitle() + "\"]");
                if (categoryNode == null) {
                    categoryNode = bodyNode.appendElement("outline").attr("text", category.getTitle());
                }

                feedNode = categoryNode.selectFirst("outline[title=\"" + feed.getTitle() + "\"]");
                if (feedNode != null) {
                    continue;
                }
                feedNode = Jsoup.parseBodyFragment("<outline/>").body().child(0);
                feedNode
                        .attr("type", "rss")
                        .attr("text", feed.getTitle())
                        .attr("title", feed.getTitle())
                        .attr("xmlUrl", feed.getFeedUrl())
                        .attr("htmlUrl", feed.getHtmlUrl());
                categoryNode.appendChild(feedNode);
            }
        }
        FileUtils.save(file, doc.outerHtml());
    }

    public static void export2(String title, File file, List<CollectionTree> collectionTrees) throws IOException, FeedException {
        Opml opml = new Opml();

        Outline outlineCategory, outlineFeed;
        List<Outline> outlines = new ArrayList<>();
        URL feedUrl, htmlUrl;
        for (CollectionTree collectionTree : collectionTrees) {
            if(collectionTree.getType() == CollectionTree.CATEGORY){
                // categoryNode = doc.selectFirst("opml > body > outline[title=\"" + collectionTree.getParent().getTitle() + "\"]");
                outlineCategory = new Outline();
                outlineCategory.setText(collectionTree.getParent().getTitle());
                outlineCategory.setTitle(collectionTree.getParent().getTitle());
                if(collectionTree.getChildren() != null && collectionTree.getChildren().size() > 0){
                    List<Outline> subOutlines = new ArrayList<>();
                    for(Collection collection: collectionTree.getChildren()){
                        CollectionFeed feed = (CollectionFeed) collection;
                        try {
                            feedUrl = new URL(feed.getFeedUrl());
                        }catch (IOException e){
                            feedUrl = null;
                        }
                        try {
                            htmlUrl = new URL(feed.getHtmlUrl());
                        }catch (IOException e){
                            htmlUrl = null;
                        }
                        outlineFeed = new Outline(feed.getTitle(), feedUrl, htmlUrl);
                        subOutlines.add(outlineFeed);
                    }
                    outlineCategory.setChildren(subOutlines);
                }
                outlines.add(outlineCategory);
            }else if(collectionTree.getType() == CollectionTree.FEED){
                CollectionFeed feed = (CollectionFeed) collectionTree.getParent();
                try {
                    feedUrl = new URL(feed.getFeedUrl());
                }catch (IOException e){
                    feedUrl = null;
                }
                try {
                    htmlUrl = new URL(feed.getHtmlUrl());
                }catch (IOException e){
                    htmlUrl = null;
                }
                outlineFeed = new Outline(feed.getTitle(), feedUrl, htmlUrl);
                outlines.add(outlineFeed);
            }
        }

        opml.setTitle(title);
        opml.setCreated(new Date());
        opml.setFeedType("opml_2.0");
        opml.setOutlines(outlines);


        new WireFeedOutput().output(opml, file);
        // FileUtils.save(file, opml.getDocs());

        XLog.d("导出的结果为：" + outlines);
        // XLog.d("导出的结果为B：" + opml.getDocs());
    }

    public static void export1(String title, File file, List<CollectionTree> collectionTrees) {
        Opml opml = new Opml();

        Outline outlineCategory, outlineFeed;
        List<Outline> outlines = new ArrayList<>();
        URL feedUrl, htmlUrl;
        for (CollectionTree collectionTree : collectionTrees) {
            if(collectionTree.getType() == CollectionTree.CATEGORY){
                outlineCategory = new Outline();
                outlineCategory.setText(collectionTree.getParent().getTitle());
                outlineCategory.setTitle(collectionTree.getParent().getTitle());
                if(collectionTree.getChildren() != null && collectionTree.getChildren().size() > 0){
                    List<Outline> subOutlines = new ArrayList<>();
                    for(Collection collection: collectionTree.getChildren()){
                        CollectionFeed feed = (CollectionFeed) collection;
                        try {
                            feedUrl = new URL(feed.getFeedUrl());
                        }catch (IOException e){
                            feedUrl = null;
                        }
                        try {
                            htmlUrl = new URL(feed.getHtmlUrl());
                        }catch (IOException e){
                            htmlUrl = null;
                        }
                        outlineFeed = new Outline(feed.getTitle(), feedUrl, htmlUrl);
                        subOutlines.add(outlineFeed);
                    }
                    outlineCategory.setChildren(subOutlines);
                }
                outlines.add(outlineCategory);
            }else if(collectionTree.getType() == CollectionTree.FEED){
                CollectionFeed feed = (CollectionFeed) collectionTree.getParent();
                try {
                    feedUrl = new URL(feed.getFeedUrl());
                }catch (IOException e){
                    feedUrl = null;
                }
                try {
                    htmlUrl = new URL(feed.getHtmlUrl());
                }catch (IOException e){
                    htmlUrl = null;
                }
                outlineFeed = new Outline(feed.getTitle(), feedUrl, htmlUrl);
                outlines.add(outlineFeed);
            }
        }

        opml.setTitle(title);
        opml.setCreated(new Date());
        opml.setOutlines(outlines);
        FileUtils.save(file, opml.getDocs());

        XLog.d("导出的结果为：" + outlines);
        XLog.d("导出的结果为B：" + opml.getDocs());
    }
}
