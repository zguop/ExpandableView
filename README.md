# ExpandableView

动画列表展开收起 ,列表右边的指示箭头可以再任何位置
 当展开时如果超过屏幕的高度时父控件自动上移，滚动的父布局可以是RecyclerView,ScrollView
 



            final ExpandableView expandableView = holder.getView(R.id.item_expanble);
             expandableView.setAdapter(queryCarInfo.uniques, new ExpandableView.OnBindDatas<UniquesInfo>() {
                
                 @Override
                 public int addClickView() {  //添加点击的布局 
                     return R.layout.item_expanble_querycar;
                 }

                 @Override
                 public void onBindClickView(ExpandableView.ViewHolder clickHolder) {
                   //点击部分的数据绑定
                 }

                 @Override
                 public int addChildView() {// 列表布局
                     return R.layout.item_expanble_caruniques;
                 }

                 @Override
                 public void onBindChildView(int contentPos, int contentCount, UniquesInfo uniquesInfo, ExpandableView.ViewHolder holder) {
                   //列表布局 数据绑定
                 }

                 @Override
                 public boolean expandableUpdataView() { //列表展开收起 是否重新 更新数据 
                     return true;
                 }
             });
          }

### 效果图：

![这里写图片描述](Untitled20.gif)

![这里写图片描述](Untitled21.gif)
