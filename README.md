# ExpandableView

动画列表展开收起 ,列表右边的指示箭头可以再任何位置
 当展开时如果超过屏幕的高度时父控件自动上移，滚动的父布局可以是RecyclerView,ScrollView
 
[![](https://jitpack.io/v/zguop/ExpandableView.svg)](https://jitpack.io/#zguop/ExpandableView)


Then, add the library to your module `build.gradle`
```gradle
dependencies {
	 compile 'com.to.aboomy:explibrary:2.0.2'
}
```
### 使用方式

```groovy
    
    final ExpandableView expandableView = holder.getView(R.id.item_expanble);
    expandableView.setAdapter(queryInfo.uniques.size(), new ExpandableView.OnBindListener() {

    @Override
    public View bindTitleView(View titleView) {
        if (titleView == null) {
            titleView = View.inflate(mContext, R.layout.item_expanble_querycar, null);
        }
        //设置箭头id
        holder1.mExpandingList.setArrowAnimationView(R.id.iv_arrow);
        
        //头部数据绑定...

        ...
        return titleView;
    }

    @Override
    public View bindChildView(int childPos, View child) {
        if(child == null){
            child = View.inflate(mContext,R.layout.item_expanble_caruniques,null);
        }
        //列表布局 数据绑定
        ...
        return child;
    }

    //返回true 列表展开收起 都将重新调用 bindTitleView bindChildView方法 适用于数据变化
    @Override
    public boolean expandableUpDataView() {
        return true;
    }
});
```
### 效果图：

![这里写图片描述](Untitled20.gif)    ![这里写图片描述](Untitled21.gif)


