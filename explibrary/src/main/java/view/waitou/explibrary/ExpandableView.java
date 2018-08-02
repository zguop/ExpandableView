package view.waitou.explibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
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

public class ExpandableView<T> extends LinearLayout {

    private static final float DEGREES = 180;

    public static final int MOBILE_TOP = 0;
    public static final int MOBILE_BUM = 1;

    private int closePositionSize = 0; ////当前显示的view大小

    private LayoutInflater    mInflater;
    private List<ViewHolder>  titleHolderList;
    private List<ViewHolder>  childHolderList;
    private List<Integer>     mChildSizeList;           //记录每个子孩子的高度--叠加的
    private RelativeLayout    titleLayout;              //点击的view
    private LinearLayout      childLayout;              //内容隐藏的view
    private ImageView         rightIcon;                //右边点击箭头 ，需进行set设置
    private ValueAnimator     expandAnimator;           //列表展开的动画
    private ValueAnimator     collapseAnimator;         //列表收起的动画
    private RotateAnimation   expandRotateAnimation;    //箭头展开的旋转动画
    private RotateAnimation   collapseRotateAnimation;  //箭头收起的旋转动画
    private List<T>           dataList;                 //数据集
    private OnBindListener<T> mOnBindListener;          //所要实现的接口
    private int               duration;                 //动画时间
    private int               mFinalHeight;             //子列表的最大高度
    private int               mHeightPixels;            //屏幕的高度
    private int               expMobile;                //列表模式
    private boolean           isAnimating;              //动画是否在执行
    private boolean           isExpanded;               //列表是否展开
    private boolean           isArranged;               //测量后的初始化 ，只进行一次
    private boolean           isCalculatedSize;         //是否进入测量的控制

    public ExpandableView(Context context) {
        this(context, null);
    }

    public ExpandableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        mHeightPixels = context.getResources().getDisplayMetrics().heightPixels;
        titleHolderList = new ArrayList<>();
        childHolderList = new ArrayList<>();
        mChildSizeList = new ArrayList<>();
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        setOrientation(VERTICAL);
        childLayout = new LinearLayout(context);
        childLayout.setOrientation(VERTICAL);
        titleLayout = new RelativeLayout(context);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.expandableView, defStyle, 0);
        expMobile = a.getInt(R.styleable.expandableView_exp_mobile, 0);
        duration = a.getInteger(R.styleable.expandableView_exp_duration, 300);
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
            mChildSizeList.clear();
            final int childCount = childLayout.getChildCount();
            if (childCount > 0) {
                int sumSize = 0;
                for (int i = 0; i < childCount; i++) {
                    final View view = childLayout.getChildAt(i);
                    //测量每个子孩子的高度
                    measureChild(view, widthMeasureSpec, heightMeasureSpec);
                    if (i > 0) {
                        sumSize = mChildSizeList.get(i - 1);
                    }
                    //高度添加到集合中保存
                    mChildSizeList.add(view.getMeasuredHeight() + sumSize);
                }
                //获取当前最大的高度
                mFinalHeight = mChildSizeList.get(childCount - 1);
            } else {
                //子view没有数据 最大高度为0
                mFinalHeight = 0;
            }
            //进入测量 必定是数据集发生变化，重新初始化动画
            initValueAnimator();
            //默认不展开的 因此第一次测量必定不会执行 展开情况下，如有数据变化，如新增数据，则立即设置最新的高度值
            if (isExpandable()) {
                setLayoutSize(mFinalHeight);
            }
            //测量开关
            isCalculatedSize = true;
            //这里只进行一次调用的初始化 目前关闭状态下 设置子view高度为0 之后不在进行调用
            if (!isArranged) {
                if (!isExpandable()) {
                    setLayoutSize(closePositionSize);
                }
                isArranged = true;
            }
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

    public void setAdapter(List<T> data, OnBindListener<T> bindListener) {
        this.dataList = data;
        this.mOnBindListener = bindListener;
        initClickView();
        initChildView();
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
    public void setClickableVisibility(int visibility) {
        if (titleLayout.getVisibility() == visibility) {
            return;
        }
        titleLayout.setVisibility(visibility);
    }

    /**
     * 设置箭头的view
     */
    public void setArrorAnimationView(ImageView view) {
        if (rightIcon != null) {
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
        if (this.duration == duration) {
            return;
        }
        this.duration = duration;
        initValueAnimator();
        expandRotateAnimation = null;
        collapseRotateAnimation = null;
    }

    /**
     * 设置保留view的个数 滑动的起始点
     */
    public void setKeepChild(final int childNumber) {

        childLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                childLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (!isArranged) {
                    return;
                }
                if (childNumber < 0 || childNumber > mChildSizeList.size()) {
                    throw new IllegalArgumentException("There aren't the view having this index. size " + mChildSizeList.size());
                }
                int childIndexSize = mChildSizeList.get(childNumber - 1);
                if (closePositionSize == childIndexSize) {
                    return;
                }
                closePositionSize = childIndexSize;
                initValueAnimator();
                if (!isExpandable()) {
                    setLayoutSize(closePositionSize);
                    childLayout.requestLayout();
                }
            }
        });
    }

    private void initChildView() {
        if (dataList != null && dataList.size() > 0) {
            int childCount = childLayout.getChildCount();
            int dataSize = dataList.size();
            if (dataSize < childCount) {//数据源小于现有子View，删除后面多的
                childLayout.removeViews(dataSize, childCount - dataSize); //删除View也清缓存
                while (childHolderList.size() > dataSize) {
                    childHolderList.remove(childHolderList.size() - 1);
                }
            }
            for (int i = 0; i < dataSize; i++) {
                ViewHolder holder;
                if (childHolderList.size() - 1 >= i) {//说明有缓存，不用inflate，否则inflate
                    holder = childHolderList.get(i);
                } else {
                    holder = new ViewHolder(mInflater.inflate(mOnBindListener.addChildView(), this, false));
                    childHolderList.add(holder);//inflate 出来后 add进来缓存
                }
                mOnBindListener.onBindChildView(i, dataSize, dataList.get(i), holder);
                //如果View没有父控件 添加
                if (holder.getConvertView().getParent() == null) {
                    childLayout.addView(holder.getConvertView());
                }
            }
            //数据数量发生变化时 重新测量
            if (mChildSizeList.size() != dataSize) {
                initLayout();
            }
        } else {
            childLayout.removeAllViews();
        }
    }

    private void initClickView() {
        ViewHolder holder;
        if (titleHolderList.size() == 0) {
            holder = new ViewHolder(mInflater.inflate(mOnBindListener.addClickView(), titleLayout));
            titleHolderList.add(holder);
        } else {
            holder = titleHolderList.get(0);
        }
        mOnBindListener.onBindClickView(holder);
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
        if (rightIcon == null) {
            return;
        }
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
        if (collapseAnimator == null) {
            initValueAnimator();
        }
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
                        expandableUpdataView();
                    }
                } else {
                    adjustItemPosIfHidden();
                }
            }

            @Override
            public void onAnimationStart(Animator animator) {
                isAnimating = true;
                if (status && mOnBindListener.expandableUpDataView()) {
                    expandableUpdataView();
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
    private void expandableUpdataView() {
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
     * 初始化测量
     */
    void initLayout() {
        isCalculatedSize = false;
    }

    /**
     * 需要实现的接口
     */
    public interface OnBindListener<T> {
        //添加头部点击的view
        int addClickView();

        // 绑定头部view数据
        void onBindClickView(ViewHolder clickHolder);

        //添加子view
        int addChildView();

        // 绑定子view数据
        void onBindChildView(int childPos, int childCount, T t, ViewHolder childHolder);

        //是否打开关闭 更新数据 改动UI的显示
        boolean expandableUpDataView();
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
