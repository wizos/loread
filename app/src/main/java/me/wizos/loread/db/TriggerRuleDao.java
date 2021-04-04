package me.wizos.loread.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;
import java.util.Set;

import me.wizos.loread.db.rule.Action;
import me.wizos.loread.db.rule.Condition;
import me.wizos.loread.db.rule.Scope;
import me.wizos.loread.db.rule.TriggerRule;

@Dao
public interface TriggerRuleDao {
    @Transaction
    @Query("SELECT * FROM Scope WHERE uid = :uid AND id = :id LIMIT 1")
    TriggerRule getRule(String uid, long id);

    @Transaction
    @Query("SELECT * FROM Scope WHERE uid = :uid ORDER BY CASE WHEN type = 'global' THEN 0 WHEN type = 'category' THEN 1 ELSE 2 END, target COLLATE NOCASE ASC")
    List<TriggerRule> getRules(String uid);

    @Transaction
    @Query("SELECT * FROM Scope WHERE uid = :uid ORDER BY CASE WHEN type = 'global' THEN 0 WHEN type = 'category' THEN 1 ELSE 2 END, target COLLATE NOCASE ASC")
    LiveData<List<TriggerRule>> getRulesLiveDate(String uid);

    @Transaction
    @Query("SELECT * FROM Scope WHERE uid = :uid AND ((type = 'global') OR (type = 'category' AND target = :targetId)) ORDER BY CASE WHEN type = 'global' THEN 0 WHEN type = 'category' THEN 1 ELSE 2 END, target COLLATE NOCASE ASC")
    LiveData<List<TriggerRule>> getAboveCategoryRulesLiveDate(String uid, String targetId);

    @Transaction
    @Query("SELECT Scope.* FROM Scope " +
            // "LEFT JOIN Feed ON (target.uid = feed.uid AND target.target = feed.id AND target.type = 'feed')" +
            "WHERE uid = :uid AND ((type = 'global') OR (type = 'category' AND target in (SELECT categoryId FROM FeedCategory WHERE uid = :uid AND feedId = :targetId)) OR (type = 'feed' AND target = :targetId)) ORDER BY CASE WHEN type = 'global' THEN 0 WHEN type = 'category' THEN 1 ELSE 2 END, target COLLATE NOCASE ASC")
    LiveData<List<TriggerRule>> getAboveFeedRulesLiveDate(String uid, String targetId);

    // @Transaction
    // @Query("SELECT * FROM Scope WHERE uid = :uid AND target = :target")
    // List<TriggerRule> getRules(String uid, String target);
    //
    // @Transaction
    // @Query("SELECT * FROM Scope WHERE uid = :uid AND type = :type AND target = :target")
    // List<TriggerRule> getRules(String uid, String type, String target);
    //
    // @Transaction
    // @Query("SELECT * FROM Scope WHERE uid = :uid AND type = 'global'") // global
    // List<TriggerRule> getGlobalRules(String uid);
    //
    // @Transaction
    // @Query("SELECT * FROM Scope WHERE uid = :uid AND type = 'category' AND target = :target")
    // List<TriggerRule> getCategoryRules(String uid, String target);
    //
    // @Transaction
    // @Query("SELECT * FROM Scope WHERE uid = :uid AND type = 'category' AND target in (:targets)")
    // List<TriggerRule> getCategoryRules(String uid, List<String> targets);
    //
    // @Transaction
    // @Query("SELECT * FROM Scope WHERE uid = :uid AND type = 'feed' AND target = :target")
    // List<TriggerRule> getFeedRules(String uid, String target);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertTarget(Scope target);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertTargets(Scope... targets);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertTargets(List<Scope> targets);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertConditions(Condition... conditions);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertConditions(List<Condition> conditions);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertActions(Action... actions);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertActions(List<Action> actions);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertActions(Set<Action> actions);

    @Delete
    @Transaction
    void delete(Scope... scopes);
}
