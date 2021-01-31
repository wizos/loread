package me.wizos.loread.activity.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.rule.TriggerRule;

public class ActionManagerViewModel extends AndroidViewModel {
    public ActionManagerViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<TriggerRule>> loadGroupedTriggerRules(String uid) {
        return CoreDB.i().triggerRuleDao().getRulesLiveDate(uid);
    }

    public LiveData<List<TriggerRule>> loadAboveCategoryGroupedTriggerRules(String uid, String targetId) {
        return CoreDB.i().triggerRuleDao().getAboveCategoryRulesLiveDate(uid, targetId);
    }

    public LiveData<List<TriggerRule>> loadAboveFeedGroupedTriggerRules(String uid, String targetId) {
        return CoreDB.i().triggerRuleDao().getAboveFeedRulesLiveDate(uid, targetId);
    }
}
