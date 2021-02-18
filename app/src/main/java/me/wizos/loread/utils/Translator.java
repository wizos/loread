package me.wizos.loread.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.annotation.ArrayRes;

import me.wizos.loread.R;

public class Translator {
    private ArrayMap<String, String> dict;
    private Context context;
    public Translator(Context context){
        this.dict = new ArrayMap<>();
        this.context = context;
    }

    public void mergeRuleResources(){
        merge(context, R.array.action_attributes_key, R.array.action_attributes);
        merge(context, R.array.action_judge_title_key, R.array.action_judge_title);
        merge(context, R.array.action_judge_content, R.array.action_judge_content_key);
        merge(context, R.array.action_judge_equals, R.array.action_judge_equals_key);
        // merge(context, R.array.action_judge_number, R.array.action_judge_number_key);
    }

    public void merge(Context context, @ArrayRes int keysId, @ArrayRes int valuesId){
        merge(context.getResources().getStringArray(keysId), context.getResources().getStringArray(valuesId));
    }

    public void merge(String[] keys, String[] values){
        if(keys==null || values==null || keys.length != values.length){
            return;
        }
        for (int i = 0, size = keys.length; i < size; i++) {
            dict.put(keys[i], values[i]);
            dict.put(values[i], keys[i]);
        }
    }

    public String get(String key){
        String value = dict.get(key);
        if(TextUtils.isEmpty(value)){
            return key;
        }
        return value;
    }
}
