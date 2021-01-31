package me.wizos.loread.db.rule;

import android.text.TextUtils;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        indices = { @Index({"scopeId"})},
        foreignKeys = @ForeignKey(entity = Scope.class, parentColumns = "id", childColumns = "scopeId", onDelete = CASCADE))
public class Condition {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String uid;
    private long scopeId;
    private String attr;
    private String judge;
    private String value;

    public Condition() {
    }

    @Ignore
    public Condition(String uid) {
        this.uid = uid;
    }

    /**
     * 是否完整，各个值都不为空
     * @return
     */
    public boolean intact(){
        return !TextUtils.isEmpty(attr) && !TextUtils.isEmpty(judge) && !TextUtils.isEmpty(value);
    }

    /**
     * 是否有效，条件符合逻辑
     * @return
     */
    public boolean valid(){
        if(TextUtils.isEmpty(attr) || TextUtils.isEmpty(judge) || TextUtils.isEmpty(value)){
            return false;
        }
        // TODO: 2021/1/26  
        if(TextUtils.isEmpty(judge)){
            return false;
        }
        if(TextUtils.isEmpty(value)){
            return false;
        }
        
        return !TextUtils.isEmpty(attr) && !TextUtils.isEmpty(judge) && !TextUtils.isEmpty(value);
    }

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

    @Override
    public String toString() {
        return "Condition{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", scopeId='" + scopeId + '\'' +
                ", attr='" + attr + '\'' +
                ", judge='" + judge + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
