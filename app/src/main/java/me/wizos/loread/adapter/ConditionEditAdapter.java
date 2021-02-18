package me.wizos.loread.adapter;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.elvishew.xlog.XLog;

import java.util.List;

import me.wizos.loread.R;
import me.wizos.loread.db.rule.Condition;
import me.wizos.loread.utils.Translator;
import me.wizos.loread.utils.TriggerRuleUtils;

public class ConditionEditAdapter extends RecyclerView.Adapter<ConditionEditAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater mInflater;
    private List<Condition> data;
    private ArrayAdapter<String> attrAdapter;
    private ArrayAdapter<String> judgeTitleAdapter;
    private ArrayAdapter<String> judgeContentAdapter;
    private ArrayAdapter<String> judgeEqualsAdapter;
    private ArrayAdapter<String> judgeNumberAdapter;

    private Translator translator;

    public void update(List<Condition> data){
        this.data = data;
    }
    public ConditionEditAdapter(Context context, List<Condition> data){
        this.context = context;
        this.data = data;
        this.mInflater = LayoutInflater.from(context);
        translator = new Translator(context);
        translator.mergeRuleResources();

        String[] attributesName = context.getResources().getStringArray(R.array.action_attributes);
        attrAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, attributesName);

        String[] judgeTitle = context.getResources().getStringArray(R.array.action_judge_title);
        judgeTitleAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, judgeTitle);

        String[] judgeContent = context.getResources().getStringArray(R.array.action_judge_content);
        judgeContentAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, judgeContent);

        String[] judgeEquals = context.getResources().getStringArray(R.array.action_judge_equals);
        judgeEqualsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, judgeEquals);

        String[] judgeNumber = context.getResources().getStringArray(R.array.action_judge_number);
        judgeNumberAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, judgeNumber);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_trigger_rule_edit, parent, false);
        return new ViewHolder(view);
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
        private TextView labelView;
        private Spinner attrSpinner;
        private Spinner judgeSpinner;
        private TextView valueTextView;
        private ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            labelView = itemView.findViewById(R.id.action_item_label);
            attrSpinner = itemView.findViewById(R.id.action_item_attr);
            judgeSpinner = itemView.findViewById(R.id.action_item_judge);
            valueTextView = itemView.findViewById(R.id.action_item_value);
            deleteButton = itemView.findViewById(R.id.action_item_delete);
        }

        void bindTo(int position){
            Condition condition = data.get(position);

            if(position == 0){
                labelView.setText(R.string.if_x);
                deleteButton.setVisibility(View.GONE);
            }else {
                deleteButton.setVisibility(View.VISIBLE);
            }
            attrSpinner.setAdapter(attrAdapter);
            if(!TextUtils.isEmpty(condition.getAttr())){
                int pos = attrAdapter.getPosition(translator.get(condition.getAttr()));
                attrSpinner.setSelection(pos);
                XLog.d("设置属性选项：" + pos);
            }


            attrSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position == 0){
                        judgeSpinner.setAdapter(judgeTitleAdapter);
                        valueTextView.setHint(R.string.keyword);
                    }else if(position == 1){
                        judgeSpinner.setAdapter(judgeContentAdapter);
                        valueTextView.setHint(R.string.keyword);
                    }else if(position == 2){
                        judgeSpinner.setAdapter(judgeEqualsAdapter);
                        valueTextView.setHint(R.string.text);
                    }else if(position == 3){
                        judgeSpinner.setAdapter(judgeNumberAdapter);
                        valueTextView.setHint(R.string.number);
                    }else if(position == 4){
                        judgeSpinner.setAdapter(judgeNumberAdapter);
                        valueTextView.setHint(R.string.number);
                    }else if(position == 5){
                        judgeSpinner.setAdapter(judgeNumberAdapter);
                        valueTextView.setHint(R.string.number);
                    }

                    condition.setAttr(translator.get(attrAdapter.getItem(position)));

                    // condition.setAttr(attrAdapter.getItem(position));

                    if(!TextUtils.isEmpty(condition.getJudge())){
                        judgeSpinner.setSelection( ((ArrayAdapter<String>)judgeSpinner.getAdapter()).getPosition(translator.get(condition.getJudge())) );
                    }
                    XLog.d("选择属性：" + condition.getAttr() + translator.get(condition.getAttr()) );
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


            judgeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                    condition.setJudge(translator.get((String)judgeSpinner.getAdapter().getItem(position)));
                    XLog.d("选择判断：" + condition.getJudge() + translator.get(condition.getJudge()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            valueTextView.setText(condition.getValue());
            XLog.d("value 不为空：" + condition.getValue() + " , " + position + " = " + data.size());

            valueTextView.setOnClickListener(v -> {
                // editText.setInputType(InputType.TYPE_NULL);//来禁止手机软键盘
                // editText.setInputType(InputType.TYPE_CLASS_TEXT);//来开启软键盘
                // 应用程序默认为开启状态、特别注意：这种方法也只能禁止软键盘、若手机自带硬键盘、此方案失效
                if(judgeSpinner.getAdapter() != judgeNumberAdapter){
                    XLog.d("点击value：" + judgeSpinner.getAdapter() + " , " + judgeNumberAdapter);
                    View view = mInflater.inflate(R.layout.dialog_input_keywords, null);
                    EditText keywordsEditText = (EditText) view.findViewById(R.id.input_value);
                    keywordsEditText.setText(valueTextView.getText().toString());
                    keywordsEditText.findFocus();
                    new MaterialDialog.Builder(context)
                            .title(R.string.input_keyword)
                            .customView(view, true)
                            .negativeText(android.R.string.cancel)
                            .onNegative((dialog, which) -> {
                                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                if(imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            })
                            .positiveText(R.string.agree)
                            .onPositive((dialog, which) -> {
                                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                if(imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                String value = TriggerRuleUtils.getOptimizedKeywords(keywordsEditText.getText().toString());
                                valueTextView.setText(value);
                                condition.setValue(value);
                            })
                            .show();
                }else {
                    new MaterialDialog.Builder(context)
                            .title(R.string.input_number)
                            .content(R.string.only_positive_integers_are_allowed)
                            .inputType(InputType.TYPE_CLASS_NUMBER)
                            .inputRange(1, 6)
                            .input(context.getString(R.string.number), valueTextView.getText().toString(), new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    valueTextView.setText(input);
                                    condition.setValue(input.toString());
                                }
                            })
                            .positiveText(R.string.confirm)
                            .negativeText(android.R.string.cancel)
                            .show();
                }
            });


            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    XLog.d("移除：" + data.size() + " , " + position);
                    if( position >= 0 && position < data.size()){
                        data.remove(position);
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }
}
