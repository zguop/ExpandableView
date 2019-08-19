package view.waitou.explibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by waitou on 16/10/28.
 * 列表展开收起view
 */

public class ExpandableView extends LinearLayout {

    private static final float DEGREES = 180;

    public static final int MOBILE_TOP = 0;
    public static final int MOBILE_BUM = 1;

    private View titleView;
    private List<View> childHolderList;
    private List<Integer> childSizeList;             //记录每个子孩子的高度--叠加的
    private RelativeLayout titleLayout;              //点击的view
    private LinearLayout childLayout;                //内容隐藏的view
    private ValueAnimator expandAnimator;            //列表展开的动画
    private ValueAnimator collapseAnimator;          //列表收起的动画
    private OnBindListener mOnBindListener;          //所要实现的接口
    private int rightIconViewId;                     //右边点击箭头 ，需进行set设置
    private int dataCount;                           //数据集
    private int duration;                            //动画时间
    private int closePositionSize;                   //当前显示的view高度
    private int mFinalHeight;                        //子列表的最大高度
    private int mHeightPixels;                       //屏幕的高度
    private int expMobile;                           //列表模式
    private int keepChild;                           //固定多少个子View个数
    private boolean isAnimating;                     //动画是否在执行
    private boolean isExpanded;                      //列表是否展开
    private boolean isCalculatedSize;                //是否进入测量的控制

    public ExpandableView(Context context) {
        this(context, null);
    }

    public ExpandableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHeightPixels = context.getResources().getDisplayMetrics().heightPixels;
        childHolderList = new ArrayList<>();
        childSizeList = new ArrayList<>();
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        setOrientation(VERTICAL);
        childLayout = new LinearLayout(context);
        childLayout.setOrientation(VERTICAL);
        titleLayout = new RelativeLayout(context);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.ExpandableView, defStyle, 0);
        expMobile = a.getInt(R.styleable.ExpandableView_exp_mobile, 0);
        duration = a.getInteger(R.styleable.ExpandableView_exp_duration, 300);
        a.recycle();

        addViewContent();

        titleLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnimating) {
                    return;
                }
                if (isExpandable()) {
                    isExpanded = false;
                    collapse();
                } else {
                    isExpanded = true;
                    expand();
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isCalculatedSize) {
            childSizeList.clear();
            final int childCount = childLayout.getChildCount();
            if (childCount > 0) {
                int sumSize = 0;
                for (int i = 0; i < childCount; i++) {
                    final View view = childLayout.getChildAt(i);
                    //测量每个子孩子的高度
                    measureChild(view, widthMeasureSpec, heightMeasureSpec);
                    if (i > 0) {
                        sumSize = childSizeList.get(i - 1);
                    }
                    //高度添加到集合中保存
                    childSizeList.add(view.getMeasuredHeight() + sumSize);
                }
                //获取当前最大的高度
                mFinalHeight = childSizeList.get(childCount - 1);
            } else {
                //子view没有数据 最大高度为0
                mFinalHeight = 0;
            }
            //进入测量 必定是数据集发生变化，重新初始化动画

            int layouHeight = mFinalHeight;

            if (keepChild > 0 && keepChild <= childSizeList.size()) {
                closePositionSize = childSizeList.get(keepChild - 1);
                layouHeight = closePositionSize;
            }

            initValueAnimator();

            //默认不展开的 因此第一次测量必定不会执行 展开情况下，如有数据变化，如新增数据，则立即设置最新的高度值
            if (isExpandable()) {
                setLayoutSize(layouHeight);
            } else {
                setLayoutSize(closePositionSize);
            }
            //测量开关
            isCalculatedSize = true;

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        //计算布局的高度
//        int titleLayoutHeight = 0;
//        int measuredHeight;
//
//        if (titleLayout.getVisibility() == VISIBLE) {
//            titleLayoutHeight = titleLayout.getMeasuredHeight();
//        }
//        if (isExpandable()) {
//            measuredHeight = mFinalHeight + titleLayoutHeight;
//        } else {
//            measuredHeight = closePositionSize + titleLayoutHeight;
//        }
//
//        setMeasuredDimension(widthMeasureSpec, );
    }

    public void setAdapter(int dataCount, OnBindListener bindListener) {
        this.dataCount = dataCount;
        this.mOnBindListener = bindListener;
        expandableUpdateView();
    }

    /**
     * 更改模式 设置点击列表 展开头部在下 还是在上 两种值可使用
     * MOBILE_TOP
     * MOBILE_BUM
     *
     * @param expMobile 模式值
     */
    public void setExpMobile(int expMobile) {
        if (this.expMobile == expMobile) {
            return;
        }
        removeAllViews();
        addViewContent();
    }

    /**
     * 设置点击部分的 隐藏 显示Visibility
     */
    public void setTitleViewVisibility(int visibility) {
        titleLayout.setVisibility(visibility);
    }

    /**
     * 设置箭头的view
     */
    public void setArrowAnimationView(int rightIconViewId) {
        this.rightIconViewId = rightIconViewId;
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
        if (this.duration != duration) {
            this.duration = duration;
            initValueAnimator();
        }
    }

    /**
     * 设置保留view的个数 滑动的起始点,例1 child的第一个位置始终显示
     */
    public void setKeepChild(final int childNumber) {
        this.keepChild = childNumber;
    }

    private void initChildView() {
        if (dataCount > 0) {
            int childCount = childLayout.getChildCount();
            //数据发生变化，首先删除删除后面多的View
            if (dataCount < childCount) {
                childLayout.removeViews(dataCount, childCount - dataCount);
                while (childHolderList.size() > dataCount) {
                    childHolderList.remove(childHolderList.size() - 1);
                }
            }
            for (int i = 0; i < dataCount; i++) {
                //把现有的View传递出去
                View holder = null;
                if (childHolderList.size() - 1 >= i) {
                    holder = childHolderList.get(i);
                }
                View child = mOnBindListener.bindChildView(i, holder);
                //回来的View不是之前的View，那么替换掉之前的View
                if (holder != child) {
                    childHolderList.remove(holder);
                    holder = child;
                    childHolderList.add(i, holder);
                }
                //如果View没有父控件 添加
                if (holder != null && holder.getParent() == null) {
                    childLayout.addView(holder);
                }
            }
            //数据数量发生变化时 重新测量
            if (childSizeList.size() != dataCount) {
                isCalculatedSize = false;
            }
        } else {
            childLayout.removeAllViews();
        }
    }

    private void initClickView() {
        View childAt = mOnBindListener.bindTitleView(titleView);
        if (titleView != childAt) {
            titleLayout.removeView(titleView);
            titleView = childAt;
            titleLayout.addView(titleView);
        }
    }

    /**
     * 初始化列表展开动画
     */
    private void initValueAnimator() {
        expandAnimator = slideAnimator(closePositionSize, mFinalHeight, true);
        collapseAnimator = slideAnimator(mFinalHeight, closePositionSize, false);
    }

    /**
     * 根据模式顺序添加view
     */
    private void addViewContent() {
        if (expMobile == MOBILE_TOP) {
            addView(titleLayout);
            addView(childLayout);
        } else if (expMobile == MOBILE_BUM) {
            addView(childLayout);
            addView(titleLayout);
        }
    }

    /**
     * 列表展开
     */
    private void expand() {
        if (expandAnimator == null) {
            initValueAnimator();
        }
        expandAnimator.start();
        View rightIcon = findViewById(rightIconViewId);
        if (rightIcon == null) {
            return;
        }
        int x = rightIcon.getWidth() / 2;
        int y = rightIcon.getHeight() / 2;
        RotateAnimation expandRotateAnimation = new RotateAnimation(0f, DEGREES, x, y);
        expandRotateAnimation.setInterpolator(new LinearInterpolator());
        expandRotateAnimation.setRepeatCount(Animation.ABSOLUTE);
        expandRotateAnimation.setFillAfter(true);
        expandRotateAnimation.setDuration(duration);
        rightIcon.startAnimation(expandRotateAnimation);
    }

    /**
     * 列表收起
     */
    private void collapse() {
        if (collapseAnimator == null) {
            initValueAnimator();
        }
        collapseAnimator.start();
        View rightIcon = findViewById(rightIconViewId);
        if (rightIcon == null) {
            return;
        }
        int x = rightIcon.getWidth() / 2;
        int y = rightIcon.getHeight() / 2;
        RotateAnimation collapseRotateAnimation = new RotateAnimation(DEGREES, 0f, x, y);
        collapseRotateAnimation.setInterpolator(new LinearInterpolator());
        collapseRotateAnimation.setRepeatCount(Animation.ABSOLUTE);
        collapseRotateAnimation.setFillAfter(true);
        collapseRotateAnimation.setDuration(duration);
        rightIcon.startAnimation(collapseRotateAnimation);
    }

    private ValueAnimator slideAnimator(int start, int end, final boolean status) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = childLayout.getLayoutParams();
                layoutParams.height = value;
                childLayout.setLayoutParams(layoutParams);
                ViewCompat.postInvalidateOnAnimation(childLayout);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimating = false;
                if (!status) {
                    if (mOnBindListener.expandableUpDataView()) {
                        expandableUpdateView();
                    }
                } else {
                    adjustItemPosIfHidden();
                }
            }

            @Override
            public void onAnimationStart(Animator animator) {
                isAnimating = true;
                if (status && mOnBindListener.expandableUpDataView()) {
                    expandableUpdateView();
                }
            }
        });
        return animator;
    }

    /**
     * 展开超出频幕高度时 移动到当前列表的最底部
     */
    private void adjustItemPosIfHidden() {
        int[] itemPos = new int[2];
        childLayout.getLocationOnScreen(itemPos);
        int itemY = itemPos[1];
        int endPosition;
        if (expMobile != MOBILE_TOP) {
            int measuredHeight = titleLayout.getMeasuredHeight();
            endPosition = itemY + mFinalHeight + measuredHeight;
        } else {
            endPosition = itemY + mFinalHeight;
        }
        if (endPosition > mHeightPixels) {
            int delta = endPosition - mHeightPixels;
            scrollBy(getParent(), delta);
        }
    }

    /**
     * 寻找可滑动的父view
     */
    private void scrollBy(ViewParent parent, int delta) {
        if (parent == null) {
            return;
        }
        if (parent instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) parent;
            recyclerView.smoothScrollBy(0, delta);
        } else if (parent instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) parent;
            scrollView.smoothScrollBy(0, delta);
        } else {
            scrollBy(parent.getParent(), delta);
        }
    }

    /**
     * 重新刷新列表数据
     */
    private void expandableUpdateView() {
        initClickView();
        initChildView();
    }

    /**
     * 设置view的高度
     */
    private void setLayoutSize(int size) {
        childLayout.getLayoutParams().height = size;
    }

    /**
     * 需要实现的接口
     */
    public interface OnBindListener<T> {

        /**
         * 绑定头部view
         */
        View bindTitleView(View titleView);

        /**
         * 绑定子View
         */
        View bindChildView(int childPos, View child);

        /**
         * return true 每次点击都会重新调用 bindTitleView bindChildView
         */
        boolean expandableUpDataView();

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        expandAnimator.cancel();
        collapseAnimator.cancel();
    }
}
