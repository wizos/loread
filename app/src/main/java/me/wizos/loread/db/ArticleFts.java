package me.wizos.loread.db;

import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.FtsOptions;

/**
 * 参照这篇文章设置的全文搜索：https://gist.github.com/joaocruz04/4667d9ae9fa884cd6c70f93f66bb6fd4
 */
@Fts4(contentEntity = Article.class, tokenizer = FtsOptions.TOKENIZER_UNICODE61 )
@Entity
public class ArticleFts {
    public String uid;
    public String id;
    public String title;
    public String content;
}
