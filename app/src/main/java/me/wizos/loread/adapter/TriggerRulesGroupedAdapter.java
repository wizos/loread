package me.wizos.loread.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.hjq.toast.ToastUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.TriggerRuleEditActivity;
import me.wizos.loread.bean.GroupedTriggerRules;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.rule.Action;
import me.wizos.loread.db.rule.Scope;
import me.wizos.loread.db.rule.TriggerRule;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.TriggerRuleUtils;
import pokercc.android.expandablerecyclerview.ExpandableAdapter;

public class TriggerRulesGroupedAdapter extends ExpandableAdapter<ExpandableAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater mInflater;
    private List<GroupedTriggerRules> groupedTriggerRules;

    public TriggerRulesGroupedAdapter(Context context) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setGroupedTriggerRules(List<GroupedTriggerRules> groupedTriggerRules) {
        this.groupedTriggerRules = groupedTriggerRules;
    }

    @Override
    public int getChildCount(int parentPosition) {
        List<TriggerRule> children = groupedTriggerRules.get(parentPosition).getTriggerRules();
        return children == null ? 0 : children.size();
    }

    @Override
    public int getGroupCount() {
        return groupedTriggerRules == null ? 0 : groupedTriggerRules.size();
    }

    @NotNull
    @Override
    protected ViewHolder onCreateGroupViewHolder(@NotNull ViewGroup viewGroup, int viewType) {
        return new GroupHolder(mInflater.inflate(R.layout.item_trigger_rule_scope, viewGroup, false));
    }

    @NotNull
    @Override
    protected ViewHolder onCreateChildViewHolder(@NotNull ViewGroup viewGroup, int viewType) {
        return new ChildHolder(mInflater.inflate(R.layout.item_trigger_rule_view, viewGroup, false));
    }

    @Override
    protected void onBindGroupViewHolder(@NotNull ViewHolder holder, int groupPosition, boolean expand, @NotNull List<?> payloads) {
        ((GroupHolder) holder).bind(groupedTriggerRules.get(groupPosition));
        // payloads 为空，说明是更新整个 ViewHolder
        // payloads 不为空，这只更新需要更新的 View 即可。
    }

    @Override
    protected void onBindChildViewHolder(@NotNull ViewHolder holder, int groupPosition, int childPosition, @NotNull List<?> payloads) {
        ((ChildHolder) holder).bind(groupedTriggerRules.get(groupPosition).getTriggerRules().get(childPosition));
    }

    @Override
    protected void onGroupViewHolderExpandChange(@NotNull ViewHolder viewHolder, int groupPosition, long animDuration, boolean expand) {

    }

    class GroupHolder extends ViewHolder {
        TextView targetView;
        GroupHolder(@NonNull View itemView) {
            super(itemView);
            targetView = itemView.findViewById(R.id.item_rule_target_textview);
        }
        public void bind(@NonNull Scope target) {
            if(Contract.TYPE_GLOBAL.equals(target.getType())){
                targetView.setText(R.string.global);
            }else if(Contract.TYPE_CATEGORY.equals(target.getType())){
                Category category = CoreDB.i().categoryDao().getById(App.i().getUser().getId(), target.getTarget());
                targetView.setText(context.getString(R.string.category_with_name,category.getTitle()));
            }else {
                Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), target.getTarget());
                targetView.setText(context.getString(R.string.feed_with_name,feed.getTitle()));
            }
        }

        public void bind(@NonNull GroupedTriggerRules groupedTriggerRules) {
            if(Contract.TYPE_GLOBAL.equals(groupedTriggerRules.getType())){
                targetView.setText(R.string.global);
            }else if(Contract.TYPE_CATEGORY.equals(groupedTriggerRules.getType())){
                Category category = CoreDB.i().categoryDao().getById(App.i().getUser().getId(), groupedTriggerRules.getTarget());
                targetView.setText(context.getString(R.string.category_with_name,category.getTitle()));
            }else {
                Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), groupedTriggerRules.getTarget());
                targetView.setText(context.getString(R.string.feed_with_name,feed.getTitle()));
            }
        }
    }

    class ChildHolder extends ViewHolder {
        RecyclerView conditionsView;
        TextView actionsView;
        ConditionViewAdapter adapter;

        ChildHolder(@NonNull View itemView) {
            super(itemView);
            conditionsView = itemView.findViewById(R.id.item_action_view_conditions_recyclerview);
            conditionsView.setLayoutManager(new LinearLayoutManager(context));
            adapter = new ConditionViewAdapter(context);
            actionsView = itemView.findViewById(R.id.item_action_textview);
        }

        public void bind(TriggerRule triggerRule) {
            adapter.setData(triggerRule.getConditions());
            conditionsView.setAdapter(adapter);

            List<String> actions = new ArrayList<>();
            for (Action action:triggerRule.getActions()) {
                if(Contract.MARK_READ.equals(action.getAction())){
                    actions.add(context.getString(R.string.mark_read));
                }else if(Contract.MARK_STAR.equals(action.getAction())){
                    actions.add(context.getString(R.string.mark_star));
                }
            }
            actionsView.setText( StringUtils.join(" + ", actions) );

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, TriggerRuleEditActivity.class);
                intent.putExtra(Contract.RULE_ID, triggerRule.getScope().getId());
                context.startActivity(intent);
            });
            adapter.setOnClickListener(view -> {
                Intent intent = new Intent(context, TriggerRuleEditActivity.class);
                intent.putExtra(Contract.RULE_ID, triggerRule.getScope().getId());
                context.startActivity(intent);
            });

            itemView.setOnLongClickListener(v -> {
                exeRule(triggerRule);
                // MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
                //     @Override
                //     public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
                //         if (index == 0) {
                //             CoreDB.i().triggerRuleDao().delete(triggerRule.getScope());
                //         }else if(index == 1){
                //             int count = TriggerRuleUtils.exeRule(App.i().getUser().getId(),0, triggerRule);
                //             // ToastUtils.show(R.string.executed);
                //             ToastUtils.show(context.getResources().getQuantityString(R.plurals.executed_involving_n_articles,count,count));
                //         }
                //         dialog.dismiss();
                //     }
                // });
                // adapter.add(new MaterialSimpleListItem.Builder(context)
                //         .content(R.string.delete)
                //         .icon(R.drawable.ic_delete)
                //         .backgroundColor(Color.TRANSPARENT)
                //         .build());
                //
                // adapter.add(new MaterialSimpleListItem.Builder(context)
                //         .content(R.string.execute_for_existing_articles)
                //         .icon(R.drawable.ic_rule)
                //         .backgroundColor(Color.TRANSPARENT)
                //         .build());
                //
                // new MaterialDialog.Builder(context)
                //         .adapter(adapter, new LinearLayoutManager(context))
                //         .show();
                return true;
            });

            adapter.setOnLongClickListener(v -> {
                exeRule(triggerRule);
                return true;
            });
        }

        private void exeRule(TriggerRule triggerRule){
            MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
                @Override
                public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
                    if (index == 0) {
                        CoreDB.i().triggerRuleDao().delete(triggerRule.getScope());
                    }else if(index == 1){
                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                TriggerRuleUtils.exeRule(App.i().getUser().getId(),0, triggerRule);
                                ToastUtils.show(R.string.executed);
                            }
                        });
                    }
                    dialog.dismiss();
                }
            });
            adapter.add(new MaterialSimpleListItem.Builder(context)
                    .content(R.string.delete)
                    .icon(R.drawable.ic_delete)
                    .backgroundColor(Color.TRANSPARENT)
                    .build());

            adapter.add(new MaterialSimpleListItem.Builder(context)
                    .content(R.string.execute_for_existing_articles)
                    .icon(R.drawable.ic_rule)
                    .backgroundColor(Color.TRANSPARENT)
                    .build());

            new MaterialDialog.Builder(context)
                    .adapter(adapter, new LinearLayoutManager(context))
                    .show();
        }
    }
}
