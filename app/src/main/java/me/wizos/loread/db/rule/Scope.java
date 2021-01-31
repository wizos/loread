package me.wizos.loread.db.rule;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import me.wizos.loread.db.User;

import static androidx.room.ForeignKey.CASCADE;
// onDelete = CASCADE 级联删除，也就是当删除主表的数据时候从表中的数据也随着一起删除

@Entity(
        indices = { @Index({"uid"})},
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "uid", onDelete = CASCADE) )
public class Scope {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String uid;
    private String type; // all, feed, category
    private String target; // all, feed_id, category_id

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @NotNull
    @Override
    public String toString() {
        return "Scope {" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", type='" + type + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
