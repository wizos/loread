package me.wizos.loread.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.viewmodel.ActionManagerViewModel;
import me.wizos.loread.adapter.TriggerRulesGroupedAdapter;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.rule.TriggerRule;
import me.wizos.loread.utils.Classifier;
import pokercc.android.expandablerecyclerview.ExpandableRecyclerView;


public class TriggerRuleManagerActivity extends AppCompatActivity {
    @BindView(R.id.action_manager_recycler_view)
    ExpandableRecyclerView rulesView;

    @BindView(R.id.action_manager_toolbar)
    Toolbar toolbar;

    // @BindView(R.id.action_manager_sticky_layout)
    // StickyHeaderLayout stickyLayout;
    // RulesAdapter rulesAdapter;

    // TriggerRulesAdapter rulesAdapter;

    TriggerRulesGroupedAdapter rulesAdapter;

    String type;
    String targetId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_trigger_rule_manager);
        ButterKnife.bind(this);
        setSupportActionBar(findViewById(R.id.action_manager_toolbar));
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

        type = intent.getStringExtra(Contract.TYPE);
        targetId = intent.getStringExtra(Contract.TARGET_ID);

        initListView();
    }

    public void initListView() {
        if(Contract.TYPE_GLOBAL.equals(type)){
            toolbar.setTitle(getString(R.string.rules_target, getString(R.string.global)));
        }else if(Contract.TYPE_CATEGORY.equals(type)){
            Category category = CoreDB.i().categoryDao().getById(App.i().getUser().getId(), targetId);
            if(category!=null){
                toolbar.setTitle(getString(R.string.rules_target, category.getTitle()));
            }
        }else if(Contract.TYPE_FEED.equals(type)){
            Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), targetId);
            if(feed!=null){
                toolbar.setTitle(getString(R.string.rules_target, feed.getTitle()));
            }
        }

        rulesView.setLayoutManager(new LinearLayoutManager(this));
        // 还有另外一种方案，通过设置动画执行时间为0来解决问题：
        rulesView.getItemAnimator().setChangeDuration(0);

        // rulesAdapter = new TriggerRulesAdapter(this);

        // rulesAdapter = new RulesAdapter(this);
        // rulesAdapter.setOnHeaderClickListener(new GroupedRecyclerViewAdapter.OnHeaderClickListener() {
        //     @Override
        //     public void onHeaderClick(GroupedRecyclerViewAdapter adapter, BaseViewHolder holder, int groupPosition) {
        //         Intent intent = new Intent(ActionManagerActivity.this, ActionActivity.class);
        //         intent.putExtra(Contract.RULE_ID, ((RulesAdapter)adapter).getGroup(groupPosition).getId());
        //         startActivity(intent);
        //         overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
        //     }
        // });

        rulesAdapter = new TriggerRulesGroupedAdapter(this);
        ActionManagerViewModel actionManagerViewModel = new ViewModelProvider(this).get(ActionManagerViewModel.class);
        if(!TextUtils.isEmpty(type) && !TextUtils.isEmpty(targetId)){
            if(Contract.TYPE_CATEGORY.equals(type)){
                actionManagerViewModel.loadAboveCategoryGroupedTriggerRules(App.i().getUser().getId(), targetId).observe(this, new Observer<List<TriggerRule>>() {
                    @Override
                    public void onChanged(List<TriggerRule> triggerRules) {
                        rulesAdapter.setGroupedTriggerRules(Classifier.group(triggerRules));
                        rulesAdapter.expandAllGroup();
                        rulesAdapter.notifyDataSetChanged();
                    }
                });
            }else {
                actionManagerViewModel.loadAboveFeedGroupedTriggerRules(App.i().getUser().getId(), targetId).observe(this, new Observer<List<TriggerRule>>() {
                    @Override
                    public void onChanged(List<TriggerRule> triggerRules) {
                        rulesAdapter.setGroupedTriggerRules(Classifier.group(triggerRules));
                        rulesAdapter.expandAllGroup();
                        rulesAdapter.notifyDataSetChanged();
                    }
                });
            }
        }else {
            actionManagerViewModel.loadGroupedTriggerRules(App.i().getUser().getId()).observe(this, new Observer<List<TriggerRule>>() {
                @Override
                public void onChanged(List<TriggerRule> triggerRules) {
                    rulesAdapter.setGroupedTriggerRules(Classifier.group(triggerRules));
                    rulesAdapter.expandAllGroup();
                    rulesAdapter.notifyDataSetChanged();
                }
            });
        }


        // Drawable divider = ContextCompat.getDrawable(this, R.drawable.divider);
        // GroupedLinearItemDecoration itemDecoration = new GroupedLinearItemDecoration(rulesAdapter,0, divider,0,null,1,divider);
        // rulesView.addItemDecoration(itemDecoration);

        rulesView.setAdapter(rulesAdapter);

        // rulesAdapter.notifyDataChanged();
        // rulesAdapter.expandAll();
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
        getMenuInflater().inflate(R.menu.menu_trigger_rule_manager, menu);
        MenuItem createRule = menu.findItem(R.id.action_manager_menu_create_rule);
        if(!TextUtils.isEmpty(type) && !TextUtils.isEmpty(targetId)){
            createRule.setVisible(true);
        }else {
            createRule.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //监听左上角的返回箭头
        if (item.getItemId() == android.R.id.home) {
            exit();
        }else if(item.getItemId() == R.id.action_manager_menu_create_rule_global){
            Intent intent = new Intent(this, TriggerRuleEditActivity.class);
            intent.putExtra(Contract.TYPE, Contract.TYPE_GLOBAL);
            intent.putExtra(Contract.TARGET_ID, Contract.TYPE_GLOBAL);
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
        }else if(item.getItemId() == R.id.action_manager_menu_create_rule){
            Intent intent = new Intent(this, TriggerRuleEditActivity.class);
            intent.putExtra(Contract.TYPE, type);
            intent.putExtra(Contract.TARGET_ID, targetId);
            startActivity(intent);
            overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
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
