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
        // 如果要分别指定生成的 Bean 与 DAO 类所在的目录，只要：
        Schema schema = new Schema(4, "me.wizos.loread.bean");
        schema.setDefaultJavaPackageDao("me.wizos.loread.data.dao");

        // 模式（Schema）同时也拥有两个默认的 flags，分别用来标示 entity 是否是 activie 以及是否使用 keep sections。
        // schema.enableActiveEntitiesByDefault();
        // schema.enableKeepSectionsByDefault(); // 通过此 schema 创建的实体类都不会覆盖自定义的代码

        // 有些时候，我们会在生成的实体类中添加一些属性和方法，但是每次重新运行 Java 项目的时候都会覆盖掉以前的代码，如果不想覆盖自己添加的代码，可以这样设置：
        // schema.enableKeepSectionsByDefault(); // 通过此 schema 创建的实体类都不会覆盖自定义的代码
        // entity.setHasKeepSections(true); // 此实体类不会覆盖自定义的代码

        // 一旦你拥有了一个 Schema 对象后，你便可以使用它添加实体（Entities）了。
        addFeed(schema);
        addImgTable(schema);
        addRequestLogTable(schema);

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

//        @SerializedName("id")
//        private String id;
//        @SerializedName("sortid")
//        private String sortid;
//        @SerializedName("title")
//        private String title;
//        @SerializedName("unreadcount")
//        private Integer unreadcount;
        Entity tag = schema.addEntity("Tag");// 接下来你便可以设置表中的字段：
        tag.setHasKeepSections(true);
        tag.addStringProperty("id").notNull().primaryKey();
        tag.addStringProperty("sortid").notNull();
        tag.addStringProperty("title");
        tag.addIntProperty("unreadcount");
//        tag.addStringProperty("feedssortid");
//        tag.addLongProperty("feedsnum");

        Entity feed = schema.addEntity("Feed");
        feed.addStringProperty("id").notNull().primaryKey();
        feed.addStringProperty("title").notNull();
        Property feedCategoryid = feed.addStringProperty("categoryid").getProperty();
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
        article.addLongProperty("starred"); // 新增的加星时间排序
        article.addStringProperty("enclosure");
        article.addStringProperty("canonical");
        article.addStringProperty("alternate");
        article.addStringProperty("summary");
        article.addStringProperty("author");
        article.addStringProperty("readState"); // read , unread .reading 保持未读（在读）
        article.addStringProperty("starState"); // star , unstar
        article.addStringProperty("saveDir");
        article.addStringProperty("imgState"); // ok 代表所有图片下载完成，空就要去加载图片，或者为加载失败的图片src
        article.addStringProperty("coverSrc");
//        article.addStringProperty("origin");
        article.addStringProperty("originStreamId");
        article.addStringProperty("originTitle");
        article.addStringProperty("originHtmlUrl");


//        article.addToOne(feed, articleCategory); //Note: 改了，但是还未生效（未同步）
        ToMany feedToArticles = feed.addToMany(article, articleCategory);
        feedToArticles.setName("items");
        feedToArticles.orderDesc(articleTimestamp);


        ToMany tagTofeeds = tag.addToMany(feed, feedCategoryid);
        tagTofeeds.setName("feeds");
        tagTofeeds.orderDesc(feedCategoryid);
        // 与在 Java 中使用驼峰命名法不同，默认数据库中的命名是使用大写和下划线来分割单词的。
        // For example, a property called “creationDate” will become a database column “CREATION_DATE”.
        // 表关系 imgId，imgNo，imgName，imgSrc，articleId，downState
        // 可以在数据库关系图中的表间创建关系以显示某个表中的列如何链接到另一表中的列。
        // 在关系数据库中，关系能防止冗余的数据。例如，如果正在设计一个数据库来跟踪有关书的信息，而每本书的信息（如书名、出版日期和出版商）都保存在一个名为 titles 的表中。同时还有一些想保存的有关出版商的信息，例如出版商的电话号码、地址和邮政编码。如果将所有这些信息都保存在 titles 表中，则对于某个出版商出版的每本书，出版商的电话号码将是重复的。
    }

    private static void addRequestLogTable(Schema schema) {
        Entity requestLog = schema.addEntity("RequestLog");// 接下来你便可以设置表中的字段：
        requestLog.addLongProperty("logTime").notNull().primaryKey();
        requestLog.addStringProperty("url");
        requestLog.addStringProperty("method");
        requestLog.addStringProperty("headParamString");
        requestLog.addStringProperty("bodyParamString");
    }

    private static void addImgTable(Schema schema) {
        Entity img = schema.addEntity("Img");
        img.addIdProperty().autoincrement();
        img.addIntProperty("no");
        img.addStringProperty("name");
        img.addStringProperty("src");
        img.addStringProperty("articleId");
        img.addIntProperty("downState");
    }
}
