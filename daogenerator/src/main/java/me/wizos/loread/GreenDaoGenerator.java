package me.wizos.loread;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class GreenDaoGenerator {
    public static void main(String[] args) throws Exception {
        // http://www.open-open.com/lib/view/open1438065400878.html
        // 现在创建一个用于添加实体（Entity）的模式（Schema）对象，两个参数分别代表：数据库版本号与自动生成代码的包路径。
        Schema schema = new Schema(1, "me.wizos.loread.bean");
        schema.setDefaultJavaPackageDao("me.wizos.loread.data.dao");
//      如果要分别指定生成的 Bean 与 DAO 类所在的目录，只要：
//      Schema schema = new Schema(1, "me.wizos.loread.bean");
//      schema.setDefaultJavaPackageDao("me.wizos.loread.greendao");

        // 模式（Schema）同时也拥有两个默认的 flags，分别用来标示 entity 是否是 activie 以及是否使用 keep sections。
        // schema2.enableActiveEntitiesByDefault();
        // schema2.enableKeepSectionsByDefault();

        // 一旦你拥有了一个 Schema 对象后，你便可以使用它添加实体（Entities）了。
        addFeed(schema);
        addRequestLog(schema);

        // 最后我们将使用 DAOGenerator 类的 generateAll() 方法自动生成代码，此处你需要根据自己的情况更改输出目录（既之前创建的 java-gen)。
        // 其实，输出目录的路径可以在 build.gradle 中设置，有兴趣的朋友可以自行搜索，这里就不再详解。
        new DaoGenerator().generateAll(schema, "app/src/main/java");
    }

    /**
     * @param schema
     */
    private static void addFeed(Schema schema) {
        // 一个实体（类）就关联到数据库中的一张表，此处表名为「Note」（既类名）
        // 你也可以重新给表命名  note.setTableName("NODE");
        // greenDAO 会自动根据实体类的属性值来创建表字段，并赋予默认值

        Entity tag = schema.addEntity("Tag");// 接下来你便可以设置表中的字段：
        tag.addStringProperty("id").notNull().primaryKey();
        tag.addStringProperty("sortid").notNull();
        tag.addStringProperty("title");
        tag.addIntProperty("unreadcount");
//        tag.addStringProperty("feedssortid");
//        tag.addLongProperty("feedsnum");

        /**
         *    @SerializedName("id")
         *    private String id;
         *    @SerializedName("sortid")
         *    private String sortid;
         *    @SerializedName("title")
         *    private String title;
         */

        Entity feed = schema.addEntity("Feed");
        feed.addStringProperty("id").notNull().primaryKey();
        feed.addStringProperty("title").notNull();
        Property categoryid = feed.addStringProperty("categoryid").getProperty();
        feed.addStringProperty("categorylabel");
        feed.addStringProperty("sortid");
        feed.addLongProperty("firstitemmsec");
        feed.addStringProperty("url");
        feed.addStringProperty("htmlurl");
        feed.addStringProperty("iconurl");

        Entity article = schema.addEntity("Article");
        article.setHasKeepSections(true);
        article.addStringProperty("id").notNull().primaryKey();
        article.addLongProperty("crawlTimeMsec"); // crawlTimeMsec和 timestampUsec 是相同的日期，先用毫秒，第二个微秒
        Property articleTimestamp = article.addLongProperty("timestampUsec").getProperty(); // 尽可能地使用 timestampUsec ，因为我们需要微秒级的分辨率。
        Property articleCategory = article.addStringProperty("categories").getProperty();
        article.addStringProperty("title");
        article.addLongProperty("published");
        article.addLongProperty("updated");
        article.addStringProperty("enclosure");
        article.addStringProperty("canonical");
        article.addStringProperty("alternate");
        article.addStringProperty("summary");
        article.addStringProperty("author");
        article.addStringProperty("readState"); // read , unread .reading 保持未读（在读）
        article.addStringProperty("starState"); // star , unstar
        article.addStringProperty("imgState"); // ok 代表所有图片下载完成，空就要去加载图片，或者为加载失败的图片src
        article.addStringProperty("coverSrc");
        article.addStringProperty("origin");


        article.addToOne(feed, articleCategory);
        ToMany feedToItems = feed.addToMany(article, articleCategory);
        feedToItems.setName("items");
        feedToItems.orderDesc(articleTimestamp);


        ToMany tagTofeeds = tag.addToMany(feed, categoryid);
        tagTofeeds.setName("feeds");
        tagTofeeds.orderDesc(categoryid);
        // 与在 Java 中使用驼峰命名法不同，默认数据库中的命名是使用大写和下划线来分割单词的。
        // For example, a property called “creationDate” will become a database column “CREATION_DATE”.
    }

    private static void addRequestLog(Schema schema) {
        Entity requestLog = schema.addEntity("RequestLog");// 接下来你便可以设置表中的字段：
        requestLog.addLongProperty("logTime").notNull().primaryKey();
        requestLog.addStringProperty("url");
        requestLog.addStringProperty("method");
        requestLog.addStringProperty("headParamString");
        requestLog.addStringProperty("bodyParamString");
    }

}
