# ExpandableView

动画列表展开收起
 当展开时如果超过屏幕的高度时父控件自动上移
 
 
 
 
 
 
   final ExpandableView expandableView = holder.getView(R.id.item_expanble);
   expandableView.setAdpater(queryCarInfo.uniques, new ExpandableView.OnBindDatas<UniquesInfo>() {
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
       public void onBindChildView(int contentPos, int contentCount, UniquesInfo carUniquesInfo, ExpandableView.ViewHolder holder) {
         //列表布局 数据绑定
       }

       @Override
       public boolean expandableUpdataView() { //列表展开收起 是否重新 更新数据 
           return true;
       }
   });
}
