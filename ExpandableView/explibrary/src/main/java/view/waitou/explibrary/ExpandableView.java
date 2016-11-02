package view.waitou.explibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by waitou on 16/10/28.
 * 列表展开收起view
 */

public class ExpandableView<T> extends RelativeLayout {

    private static final int   DURATION = 400;
    private              float DEGREES  = 180;
    private              int   duration = DURATION;

    private RelativeLayout   clickableLayout;  //点击的view
    private LinearLayout     contentLayout;  //内容隐藏的view
    private ImageView        rightIcon; //右边点击箭头 ，需进行set设置
    private ValueAnimator    expandAnimator;  // 列表展开的动画
    private ValueAnimator    collapseAnimator; //列表收起的动画
    private RotateAnimation  expandRotateAnimation; //箭头展开的旋转动画
    private RotateAnimation  collapseRotateAnimation; // 箭头收起的旋转动画
    private LayoutInflater   mInflater;
    private List<ViewHolder> clickViewList;
    private List<ViewHolder> contentViewList;
    private List<T>          datas;
    private OnBindDatas<T>   mOnBindDatas;
    private int              mFinalHeight;
    private int              mHeightPixels;
    private boolean          isAnimating; //动画是否在执行
    private boolean          isExpanded; //列表是否展开

    public ExpandableView(Context context) {
        this(context, null);
    }

    public ExpandableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHeightPixels = context.getResources().getDisplayMetrics().heightPixels;
        clickViewList = new ArrayList<>();
        contentViewList = new ArrayList<>();
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.expandable_view, this);
        mInflater = LayoutInflater.from(context);
        clickableLayout = (RelativeLayout) findViewById(R.id.expandable_view_clickable_content);
        contentLayout = (LinearLayout) findViewById(R.id.expandable_view_content_layout);
        contentLayout.setVisibility(GONE);
        onPreDrawListener();
        clickableLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnimating) {
                    return;
                }
                if (contentLayout.isShown()) {
                    isExpanded = false;
                    collapse();
                } else {
                    isExpanded = true;
                    expand();
                }
            }
        });
    }

    /**
     * 初始化列表展开动画
     */
    private void initValueAnimator() {
        expandAnimator = slideAnimator(0, mFinalHeight, true);
        collapseAnimator = slideAnimator(mFinalHeight, 0, false);
    }

    private void onPreDrawListener() {
        contentLayout.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        contentLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                        final int widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                        final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                        contentLayout.measure(widthSpec, heightSpec);
                        mFinalHeight = contentLayout.getMeasuredHeight();
                        initValueAnimator();
                        return true;
                    }
                });
    }


    /**
     * 展开超出频幕高度时 移动到当前列表的最底部
     */
    private void adjustItemPosIfHidden() {
        int[] itemPos = new int[2];
        contentLayout.getLocationOnScreen(itemPos);
        int itemY = itemPos[1];
        int endPosition = itemY + mFinalHeight;
        if (endPosition > mHeightPixels) {
            int delta = endPosition - mHeightPixels;
            //可根据业务自行增加
            ViewParent parent = getParent();
            if (parent != null) { //一层的嵌套 获取RecyclerView
                if (parent instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) parent;
                    recyclerView.smoothScrollBy(0, delta);
                } else {
                    ViewParent parent1 = parent.getParent();
                    if (parent1 != null) {
                        if (parent1 instanceof RecyclerView) { //二层的嵌套 RecyclerView
                            RecyclerView recyclerView = (RecyclerView) parent1;
                            recyclerView.smoothScrollBy(0, delta);
                        } else if (parent1 instanceof ScrollView) { //或者 ScrollView
                            ScrollView scrollView = (ScrollView) parent1;
                            scrollView.smoothScrollTo(0, getScrollY() + delta);
                        }
                    }
                }
            }
        }
    }

    public interface OnBindDatas<T> {
        //添加头部点击的view
        int addClickView();

        // 绑定头部view数据
        void onBindClickView(ViewHolder clickHolder);

        //添加子view
        int addChildView();

        // 绑定子view数据
        void onBindChildView(int pos, int itemCount, T t, ViewHolder holder);

        //是否打开关闭 更新数据 改动UI的显示
        boolean expandableUpdataView();
    }

    public void setAdpater(List datas, OnBindDatas onBindDatas) {
        this.datas = datas;
        this.mOnBindDatas = onBindDatas;
        initClickView();
        initChildView();
    }

    private void initChildView() {
        if (datas != null && datas.size() > 0) {
            int childCount = contentLayout.getChildCount();
            int dataSize = datas.size();
            if (dataSize < childCount) {//数据源小于现有子View，删除后面多的
                contentLayout.removeViews(dataSize, childCount - dataSize);
                //删除View也清缓存
                while (contentViewList.size() > dataSize) {
                    contentViewList.remove(contentViewList.size() - 1);
                }
            }
            for (int i = 0; i < dataSize; i++) {
                ViewHolder holder;
                if (contentViewList.size() - 1 >= i) {//说明有缓存，不用inflate，否则inflate
                    holder = contentViewList.get(i);
                } else {
                    holder = new ViewHolder(mInflater.inflate(mOnBindDatas.addChildView(), this, false));
                    contentViewList.add(holder);//inflate 出来后 add进来缓存
                }
                mOnBindDatas.onBindChildView(i, dataSize, datas.get(i), holder);
                //如果View没有父控件 添加
                if (holder.getConvertView().getParent() == null) {
                    contentLayout.addView(holder.getConvertView());
                }
            }
        } else {
            contentLayout.removeAllViews();
        }
    }

    private void initClickView() {
        ViewHolder holder;
        if (clickViewList.size() == 0) {
            holder = new ViewHolder(mInflater.inflate(mOnBindDatas.addClickView(), clickableLayout));
            clickViewList.add(holder);
        } else {
            holder = clickViewList.get(0);
        }
        mOnBindDatas.onBindClickView(holder);
    }

    /**
     * 重新刷新列表数据
     */
    private void expandableUpdataView() {
        initClickView();
        initChildView();
    }

    /**
     * 设置箭头的view
     */
    public void setArrorAnimationView(ImageView view) {
        if(rightIcon != null){
            return;
        }
        this.rightIcon = view;
    }

    /**
     * 列表是否打开关闭
     */
    public boolean isExpandable() {
        return isExpanded;
    }

    /**
     * 设置动画时间，重新初始化动画
     */
    public void setDuration(int duration) {
        this.duration = duration;
        initValueAnimator();
        expandRotateAnimation = null;
        collapseRotateAnimation = null;
    }

    /**
     * 列表展开
     */
    private void expand() {
        expandAnimator.start();
        if (rightIcon == null) {
            return;
        }
        contentLayout.setVisibility(View.VISIBLE);
        if (expandRotateAnimation == null) {
            int x = rightIcon.getMeasuredWidth() / 2;
            int y = rightIcon.getMeasuredHeight() / 2;
            expandRotateAnimation = new RotateAnimation(0f, DEGREES, x, y);
            expandRotateAnimation.setInterpolator(new LinearInterpolator());
            expandRotateAnimation.setRepeatCount(Animation.ABSOLUTE);
            expandRotateAnimation.setFillAfter(true);
            expandRotateAnimation.setDuration(duration);
        }
        rightIcon.startAnimation(expandRotateAnimation);

    }

    /**
     * 列表收起
     */
    private void collapse() {
        collapseAnimator.start();
        if (rightIcon == null) {
            return;
        }
        if (collapseRotateAnimation == null) {
            int x = rightIcon.getMeasuredWidth() / 2;
            int y = rightIcon.getMeasuredHeight() / 2;
            collapseRotateAnimation = new RotateAnimation(DEGREES, 0f, x, y);
            collapseRotateAnimation.setInterpolator(new LinearInterpolator());
            collapseRotateAnimation.setRepeatCount(Animation.ABSOLUTE);
            collapseRotateAnimation.setFillAfter(true);
            collapseRotateAnimation.setDuration(duration);
        }
        rightIcon.startAnimation(collapseRotateAnimation);
    }

    private ValueAnimator slideAnimator(int start, int end, final boolean status) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = contentLayout.getLayoutParams();
                layoutParams.height = value;
                contentLayout.setLayoutParams(layoutParams);
                contentLayout.invalidate();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimating = false;
                //如果结束的end大于了关闭的值说明是展开的状态
                if (!status) {
                    contentLayout.setVisibility(View.GONE);
                    if (mOnBindDatas.expandableUpdataView()) {
                        expandableUpdataView();
                    }
                } else {
                    adjustItemPosIfHidden();
                }
            }

            @Override
            public void onAnimationStart(Animator animator) {
                isAnimating = true;
                if (status && mOnBindDatas.expandableUpdataView()) {
                    expandableUpdataView();
                }
            }
        });
        return animator;
    }

    public static class ViewHolder {
        private View              mConvertView;
        private SparseArray<View> mViews;

        ViewHolder(View view) {
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
