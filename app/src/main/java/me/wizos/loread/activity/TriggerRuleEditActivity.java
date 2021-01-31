package me.wizos.loread.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.adapter.ConditionEditAdapter;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.rule.Action;
import me.wizos.loread.db.rule.Condition;
import me.wizos.loread.db.rule.Scope;
import me.wizos.loread.db.rule.TriggerRule;


public class TriggerRuleEditActivity extends AppCompatActivity {
    @BindView(R.id.action_target)
    TextView targetView;

    @BindView(R.id.action_conditions_recycler_view)
    RecyclerView conditionsView;

    @BindView(R.id.action_add_condition)
    TextView addConditionButton;

    @BindView(R.id.action_mark_read)
    CheckBox markReadCheckBox;

    @BindView(R.id.action_mark_star)
    CheckBox markStarCheckBox;

    @BindView(R.id.action_save_rule_button)
    Button saveRuleButton;

    private TriggerRule triggerRule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // BackgroundLibrary.inject2(this);
        setContentView(R.layout.activity_trigger_rule_edit);
        ButterKnife.bind(this);
        setSupportActionBar(findViewById(R.id.action_toolbar));
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (savedInstanceState != null) {
            onRecoveryInstanceState(savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        // private String type; // all, category, feed
        // private String targetId; // all, category_id, feed_id
        // private String targetName;
        long ruleId = intent.getLongExtra(Contract.RULE_ID, -1L);
        if( ruleId >= 0){
            triggerRule = CoreDB.i().triggerRuleDao().getRule(App.i().getUser().getId(), ruleId);
        }else {
            triggerRule = new TriggerRule();

            Scope target = new Scope();
            target.setUid(App.i().getUser().getId());
            target.setType(intent.getStringExtra(Contract.TYPE));
            target.setTarget(intent.getStringExtra(Contract.TARGET_ID));

            triggerRule.setScope(target);

            List<Condition> conditionList = new ArrayList<>();
            conditionList.add(new Condition(App.i().getUser().getId()));
            triggerRule.setConditions(conditionList);


            triggerRule.setActions(new ArrayList<>());
        }

        XLog.d("获取规则Id：" + ruleId + ", " + triggerRule);

        targetView.setText(getTargetName(triggerRule.getScope().getType(), triggerRule.getScope().getTarget()));
        initListView();
    }

    private String getTargetName(String type, String target){
        if(Contract.TYPE_GLOBAL.equals(type)){
            return getString(R.string.global);
        }else if(Contract.TYPE_CATEGORY.equals(type)){
            Category category = CoreDB.i().categoryDao().getById(App.i().getUser().getId(), target);
            return category.getTitle();
        }else if(Contract.TYPE_FEED.equals(type)){
            Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), target);
            return feed.getTitle();
        }
        return "";
    }

    public void initListView() {
        conditionsView.setLayoutManager(new LinearLayoutManager(this));
        ConditionEditAdapter conditionEditAdapter = new ConditionEditAdapter(this, triggerRule.getConditions());
        conditionsView.setAdapter(conditionEditAdapter);
        addConditionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XLog.d("点击添加条件: " + triggerRule.getConditions().size() );
                triggerRule.getConditions().add(new Condition(App.i().getUser().getId()));
                conditionEditAdapter.notifyItemInserted(triggerRule.getConditions().size() - 1);
            }
        });

        saveRuleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validCondition = true;
                for (Condition condition: triggerRule.getConditions()) {
                    if(!condition.intact()){
                        validCondition = false;
                        break;
                    }

                    // if(CoreDB.i().triggerRuleDao().getRule(uid, triggerRule.getTarget().getType(), triggerRule.getTarget().getTarget(), condition.getAttr(), condition.getJudge(), condition.getValue()) != null){
                    //
                    // }
                }

                if(!validCondition){
                    ToastUtils.show(R.string.failed_condition_is_incomplete);
                    return;
                }

                XLog.d("条件为：" + triggerRule.getConditions());

                boolean hasAction = false;
                String uid = App.i().getUser().getId();
                List<Action> actions = new ArrayList<>();

                if(markReadCheckBox.isChecked()){
                    Action action = new Action();
                    action.setAction(Contract.MARK_READ);
                    action.setUid(uid);
                    actions.add(action);
                    hasAction = true;
                }

                if(markStarCheckBox.isChecked()){
                    Action action = new Action();
                    action.setAction(Contract.MARK_STAR);
                    action.setUid(uid);
                    actions.add(action);
                    hasAction = true;
                }

                if(!hasAction){
                    ToastUtils.show(R.string.failed_action_is_not_checked);
                    return;
                }

                Long ruleId = CoreDB.i().triggerRuleDao().insertTarget(triggerRule.getScope());

                for (Condition condition:triggerRule.getConditions()) {
                    condition.setScopeId(ruleId);
                }
                CoreDB.i().triggerRuleDao().insertConditions(triggerRule.getConditions());

                for (Action action:actions) {
                    action.setScopeId(ruleId);
                }
                CoreDB.i().triggerRuleDao().insertActions(actions);

                triggerRule.setActions(actions);

                ToastUtils.show(R.string.success);
                XLog.d("需要新增的规则为：" + triggerRule);
                finish();
            }
        });


        if(triggerRule.getActions() != null){
            List<Action> actions = triggerRule.getActions();
            Set<String> actionSet = new HashSet<>(actions.size());
            for (Action action:actions) {
                actionSet.add(action.getAction());
            }
            markReadCheckBox.setChecked(actionSet.contains(Contract.MARK_READ));
            markStarCheckBox.setChecked(actionSet.contains(Contract.MARK_STAR));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 后者为短期内按下的次数
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void onRecoveryInstanceState(@NonNull Bundle outState) {
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.menu_url_rewrite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //监听左上角的返回箭头
            case android.R.id.home:
                exit();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exit() {
        this.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // @Override
    // protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
    //     mColorfulBuilder
    //             .backgroundColor(R.id.action_root, R.attr.root_view_bg)
    //             .backgroundColor(R.id.action_toolbar, R.attr.topbar_bg);
    //     return mColorfulBuilder;
    // }
}
