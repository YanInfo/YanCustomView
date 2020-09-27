package com.yan.yancountedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * * created by zhangyan
 * 自定义可计数输入框
 * 2020-9-9
 */

public class YanCountEditText extends RelativeLayout {

    //类型1(单数类型)：TextView显示总字数，然后根据输入递减.例：100，99，98
    //类型2(百分比类型)：TextView显示总字数和当前输入的字数，例：0/100，1/100，2/100
    public static final String SINGULAR = "Singular";//类型1(单数类型)
    public static final String PERCENTAGE = "Percentage";//类型2(百分比类型)
    private EditText etContent;//文本框
    private View vLineUp;//底部横线
    private View vLineDn;//底部横线
    private String TYPES = SINGULAR;//类型，需要根据字段判断

    private String mText = "";//默认文字
    private String mHint = "请输入内容";//提示文字
    private int mMaxNum = 100;//最大字符
    private int mLineColor = Color.BLACK;//横线颜色
    private int mTextColor = Color.BLACK;//输入文字颜色
    private TextView mTvNum;//字数显示TextView
    private int mTextLeftColor;//设置/左边的默认颜色

    public YanCountEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public YanCountEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);

    }

    /**
     * 构造函数初始化
     *
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.layout_edittext, this, true);
        etContent = findViewById(R.id.etContent);
        mTvNum = findViewById(R.id.tvNum);
        vLineUp = findViewById(R.id.vLineUp);
        vLineDn = findViewById(R.id.vLineDn);

        // 自定义属性取值
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EhireCountEditText);
        if (typedArray != null) {
            //默认文字
            mText = typedArray.getString(R.styleable.EhireCountEditText_etText);
            etContent.setText(mText);
            etContent.setSelection(etContent.getText().length());
            //提示文字
            mHint = typedArray.getString(R.styleable.EhireCountEditText_etHint);
            etContent.setHint(mHint);
            //提示文字颜色
            etContent.setHintTextColor(typedArray.getColor(R.styleable.EhireCountEditText_etHintColor, Color.rgb(155, 155, 155)));
            //最小高度
            etContent.setMinHeight(px2dip(context,
                    typedArray.getDimensionPixelOffset(R.styleable.EhireCountEditText_etMinHeight, 200)));
            //最大字符
            mMaxNum = typedArray.getInt(R.styleable.EhireCountEditText_etMaxLength, 100);
            //横线颜色
            mLineColor = typedArray.getColor(R.styleable.EhireCountEditText_etLineColor, Color.BLACK);
            vLineDn.setBackgroundColor(mLineColor);
            vLineUp.setBackgroundColor(mLineColor);
            //输入文字大小
            etContent.setTextSize(px2sp(context,
                    typedArray.getDimensionPixelOffset(R.styleable.EhireCountEditText_etTextSize, 16)));
            //输入文字颜色
            mTextColor = typedArray.getColor(R.styleable.EhireCountEditText_etTextColor, Color.BLACK);
            etContent.setTextColor(mTextColor);
            //设置提示统计文字大小
            mTvNum.setTextSize(px2sp(context,
                    typedArray.getDimensionPixelSize(R.styleable.EhireCountEditText_etPromptTextSize, 12)));
            //设置提示统计文字颜色
            mTvNum.setTextColor(typedArray.getColor(R.styleable.EhireCountEditText_etPromptTextColor, Color.BLACK));
            //初始化/左边的文字颜色
            mTextLeftColor = typedArray.getColor(R.styleable.EhireCountEditText_etPromptTextLeftColor, Color.BLACK);
            //设置提示统计显示类型
            int t = typedArray.getInt(R.styleable.EhireCountEditText_etType, 0);
            if (t == 0) {
                TYPES = SINGULAR;
            } else {
                TYPES = PERCENTAGE;
            }
            if (TYPES.equals(SINGULAR)) {         //类型1
                mTvNum.setText(String.valueOf(mMaxNum));
            } else {                              //类型2
                mTvNum.setText(0 + "/" + mMaxNum);
            }
            //设置提示位置
            int promptPosition = typedArray.getInt(R.styleable.EhireCountEditText_etPromptPosition, 0);
            if (promptPosition == 0) {//上方
                vLineDn.setVisibility(View.VISIBLE);
                vLineUp.setVisibility(View.GONE);
            } else {//下方
                vLineUp.setVisibility(View.VISIBLE);
                vLineDn.setVisibility(View.GONE);
            }
            typedArray.recycle();
        }
        //监听输入
        etContent.addTextChangedListener(mTextWatcher);
    }

    private int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    private static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            int editStart = etContent.getSelectionStart();
            int editEnd = etContent.getSelectionEnd();
            // 先去掉监听器，否则会出现栈溢出
            etContent.removeTextChangedListener(mTextWatcher);
            // 去除首位空格
            if (s.toString().startsWith(" ")) {
                s.replace(0, 1, "");
            }
            // 注意这里只能每次都对整个EditText的内容求长度，不能对删除的单个字符求长度
            // 因为是中英文混合，这里计算的规则是一个汉字对应两个字节，一个数字或字母对应一个字节
            // 最后在UI上的变化是，一个汉字计数+1，一个字母或数字+1，如果继续输入一个字母或数字则计数不变
            while (getInputCount() > mMaxNum * 2) { // 当输入字符个数超过限制的大小时，进行截断操作
                s.delete(editStart - 1, editEnd);
                editStart--;
                editEnd--;
            }
            etContent.setSelection(s.length());
            // 恢复监听器
            etContent.addTextChangedListener(mTextWatcher);
            setLeftCount();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    /**
     * 刷新剩余输入字数
     */
    private void setLeftCount() {
        if (TYPES.equals(SINGULAR)) {//类型1
            mTvNum.setText(String.valueOf((mMaxNum - getInputCount())));
        } else if (TYPES.equals(PERCENTAGE)) {//类型2, 这里需要计算颜色，以"/"区分不同的颜色
            String content = (getInputCount() + 1) / 2 + "/" + mMaxNum;
            int end = content.indexOf("/");
            int start = 0;
            SpannableStringBuilder builder = new SpannableStringBuilder(content);
            ForegroundColorSpan span = new ForegroundColorSpan(mTextLeftColor);
            builder.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // 如果输入框内容重新变为空，则计数值为0
            if (getInputCount() == 0) {
                mTvNum.setText(0 + "/" + mMaxNum);
            } else {     // 如果不为空，则为其填充计数和颜色
                mTvNum.setText(builder);
            }
        }

    }

    /**
     * 获取用户输入内容字数
     */
    private long getInputCount() {
        return calculateLength(etContent.getText().toString().trim());
    }

    /**
     * 计算分享内容的字数，一个汉字=两个英文字母，一个中文标点=两个英文标点
     * 注意：该函数的不适用于对单个字符进行计算，因为单个字符四舍五入后都是1
     *
     * @param s
     * @return
     */
    private int calculateLength(String s) {
        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            char charAt = s.charAt(i);
            if (charAt < 255) {
                len++;
            } else {
                len += 2;
            }
        }
        return Math.round(len);
    }

    /**
     * 设置默认内容
     *
     * @param str --内容
     */
    public void setText(String str) {
        etContent.setText(str);
        etContent.setSelection(etContent.getText().length());
    }

    /**
     * 获取输入内容，这是一个java方法
     *
     * @return 内容
     */
    public String getText() {
        return etContent.getText().toString();
    }

}
