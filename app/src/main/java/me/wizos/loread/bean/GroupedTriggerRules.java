package me.wizos.loread.bean;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import me.wizos.loread.db.rule.TriggerRule;

public class GroupedTriggerRules {
    private String type;
    private String target;
    private List<TriggerRule> triggerRules;

    public String getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public List<TriggerRule> getTriggerRules() {
        return triggerRules;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTriggerRules(List<TriggerRule> triggerRules) {
        this.triggerRules = triggerRules;
    }

    @NotNull
    @Override
    public String toString() {
        return "GroupedTriggerRules{" +
                "type='" + type + '\'' +
                ", target='" + target + '\'' +
                ", triggerRules=" + triggerRules +
                '}';
    }
}
