package view.waitou.explibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by waitou on 16/10/12.
 * 动态添加view 带缓存 减少inflate
 */
public class NestFullFlexboxLayout<T> extends FlexboxLayout {

    private LayoutInflater           mInflater;
    private List<NestFullViewHolder> mCahcesList;//缓存ViewHolder,按照add的顺序缓存，
    private List<T>                  mDatas; // 数据源
    private int                      mItemLayoutId; //itemId
    private OnBindDatas<T>           mBindDatas;

    public NestFullFlexboxLayout(Context context) {
        this(context, null);
    }

    public NestFullFlexboxLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestFullFlexboxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mInflater = LayoutInflater.from(context);
        mCahcesList = new ArrayList<>();
    }

    /**
     * 外部调用  同时刷新视图
     */
    public NestFullFlexboxLayout setAdapter(int itemId, List datas, OnBindDatas onBindDatas) {
        mItemLayoutId = itemId;
        mDatas = datas;
        mBindDatas = onBindDatas;
        updateUI();
        return this;
    }

    public interface OnBindDatas<T> {
        //更新数据
        void onBind(int pos, int itemCount, T t, NestFullViewHolder holder);

        //设置view大小 在add到父控件时调用
        void setLayoutParams(int pos, NestFullViewHolder holder);
    }



    /**
     * 刷新数据
     */
    private void updateUI() {
        if (mDatas != null && mDatas.size() > 0) {
            if (mDatas.size() < getChildCount()) {
                removeViews(mDatas.size(), getChildCount() - mDatas.size());
                //删除View也清缓存
                while (mCahcesList.size() > mDatas.size()) {
                    mCahcesList.remove(mCahcesList.size() - 1);
                }
            }
            for (int i = 0; i < mDatas.size(); i++) {
                NestFullViewHolder holder;
                if (mCahcesList.size() - 1 >= i) {
                    holder = mCahcesList.get(i);
                } else {
                    holder = new NestFullViewHolder(mInflater.inflate(mItemLayoutId, this, false));
                    mCahcesList.add(holder);
                }
                if (mBindDatas != null) {
                    mBindDatas.onBind(i, mDatas.size(), mDatas.get(i), holder);
                }
                if (holder.getConvertView().getParent() == null) {
                    addView(holder.getConvertView());
                    if (mBindDatas != null) {
                        mBindDatas.setLayoutParams(i,holder);
                    }
                }
            }
        } else {
            removeAllViews();
        }
    }


    public static class NestFullViewHolder {
        private View              mConvertView;
        private SparseArray<View> mViews;

        NestFullViewHolder(View view) {
            this.mConvertView = view;
            this.mViews = new SparseArray<>();
        }

        /**
         * 通过viewId获取控件
         */
        public <T extends View> T getView(int viewId) {
            View view = mViews.get(viewId);
            if (view == null) {
                view = mConvertView.findViewById(viewId);
                mViews.put(viewId, view);
            }
            return (T) view;
        }

       public View getConvertView() {
            return mConvertView;
        }
    }
}
