package me.wizos.loread.db.rule;

import androidx.room.Embedded;
import androidx.room.Relation;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TriggerRule {
    @Embedded
    private Scope scope;
    @Relation(
            parentColumn = "id",
            entityColumn = "scopeId"
    )
    private List<Condition> conditions;
    @Relation(
            parentColumn = "id",
            entityColumn = "scopeId"
    )
    private List<Action> actions;


    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    @NotNull
    @Override
    public String toString() {
        return "TriggerRule{" +
                "scope=" + scope +
                ", conditions=" + conditions +
                ", actions=" + actions +
                '}';
    }
}
