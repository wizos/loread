package me.wizos.loread.config.article_action_rule;

import java.util.Set;

public class ArticleActionRule {
    private String target; // all, feed;    // category:
    private String attr; // title, content, author, link
    //private String type; // keyword, regex;     js
    private String judge; // 匹配正则，未匹配正则；包含关键词，未包含关键词
    private String value;
    private Set<String> actions;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getJudge() {
        return judge;
    }

    public void setJudge(String judge) {
        this.judge = judge;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return "ActionRule{" +
                "target='" + target + '\'' +
                ", attr='" + attr + '\'' +
                ", judge='" + judge + '\'' +
                ", value='" + value + '\'' +
                ", actions=" + actions +
                '}';
    }
}
