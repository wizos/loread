package me.wizos.loread.config.article_extract_rule;

public enum Selector {
    css("css"),xpath("xpath"),regex("regex");

    private String selector;

    Selector(String selector) { }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }
}
