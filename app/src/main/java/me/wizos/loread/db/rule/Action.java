package me.wizos.loread.db.rule;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        indices = { @Index({"scopeId"})},
        foreignKeys = @ForeignKey(entity = Scope.class, parentColumns = "id", childColumns = "scopeId", onDelete = CASCADE))
public class Action {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String uid;
    private long scopeId;
    private String action;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getScopeId() {
        return scopeId;
    }

    public void setScopeId(long scopeId) {
        this.scopeId = scopeId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @NotNull
    @Override
    public String toString() {
        return "Action{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", scopeId=" + scopeId +
                ", action='" + action + '\'' +
                '}';
    }
}
