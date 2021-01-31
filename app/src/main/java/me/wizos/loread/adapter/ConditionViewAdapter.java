package me.wizos.loread.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.wizos.loread.R;
import me.wizos.loread.db.rule.Condition;
import me.wizos.loread.utils.Translator;

public class ConditionViewAdapter extends RecyclerView.Adapter<ConditionViewAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater mInflater;
    private List<Condition> data;
    private Translator translator;


    public ConditionViewAdapter(Context context){
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.translator = new Translator(context);
        this.translator.mergeRuleResources();
    }

    public void setData(List<Condition> data){
        this.data = data;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_trigger_rule_condition, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(position);
    }

    @Override
    public int getItemCount() {
            return data!=null? data.size():0;
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        TextView labelView;
        TextView conditionView;

        public ViewHolder(View itemView) {
            super(itemView);
            labelView = itemView.findViewById(R.id.item_condition_labelview);
            conditionView = itemView.findViewById(R.id.item_condition_textview);
        }


        void bindTo(int position) {
            if(position == 0) {
                labelView.setText(R.string.if_x);
            }
            Condition condition = data.get(position);
            conditionView.setText(context.getString(R.string.condition_view, translator.get(condition.getAttr()), translator.get(condition.getJudge()), translator.get(condition.getValue())));

            itemView.setOnClickListener(v -> {
                if(onClickListener !=null){
                    onClickListener.onClick(v);
                }
            });
            itemView.setOnLongClickListener(v -> {
                if(onLongClickListener !=null){
                    return onLongClickListener.onLongClick(v);
                }
                return false;
            });
        }
    }


    private OnClickListener onClickListener;
    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    interface OnClickListener{
        void onClick(View view);
    }


    private OnLongClickListener onLongClickListener;
    public void setOnLongClickListener(OnLongClickListener onLongClickListener){
        this.onLongClickListener = onLongClickListener;
    }

    interface OnLongClickListener{
        boolean onLongClick(View view);
    }
}
