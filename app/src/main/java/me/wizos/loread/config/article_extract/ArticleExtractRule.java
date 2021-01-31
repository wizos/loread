package me.wizos.loread.config.article_extract;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class ArticleExtractRule {
    private Selector selector = Selector.css;

    @SerializedName("document_trim")
    private String documentTrim;

    private String content;

    @SerializedName("content_strip")
    private String contentStrip;

    @SerializedName("content_trim")
    private String contentTrim;

    public Selector getSelector() {
        return selector;
    }

    public String getDocumentTrim() {
        return documentTrim;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentStrip() {
        return contentStrip;
    }

    public String getContentTrim() {
        return contentTrim;
    }

    @NotNull
    @Override
    public String toString() {
        return "ArticleExtractRule{" +
                "selector=" + selector +
                ", documentTrim='" + documentTrim + '\'' +
                ", content='" + content + '\'' +
                ", contentStrip='" + contentStrip + '\'' +
                ", contentTrim='" + contentTrim + '\'' +
                '}';
    }
}
