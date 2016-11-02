package view.waitou.expandableview;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import view.waitou.explibrary.ExpandableView;
import view.waitou.explibrary.ExpandingList;
import view.waitou.explibrary.NestFullFlexboxLayout;

public class MainActivity extends AppCompatActivity {

    private String[]        modelName  = {"ALFA", "V8", "CM", "DPPPP"};
    private String[]        uniques = {"444", "AAA", "BBBB", "444", "444", "444"};
    private List<QueryInfo> mInfos     = new ArrayList<>();
    private NestFullFlexboxLayout mNestFullFlexboxLayout;
    private ExpandingList mExpandingList;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNestFullFlexboxLayout = (NestFullFlexboxLayout) findViewById(R.id.flex_label);
        mExpandingList = (ExpandingList) findViewById(R.id.expanding_list_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mNestFullFlexboxLayout.setVisibility(View.GONE);
        mExpandingList.setVisibility(View.GONE);

        init();
        bindView();
    }

    private void bindView() {

        mNestFullFlexboxLayout.setAdapter(R.layout.item_expandble_view, mInfos, new NestFullFlexboxLayout.OnBindDatas<QueryInfo>() {

            @Override
            public void onBind(final int pos, final int itemCount, final QueryInfo queryInfo, NestFullFlexboxLayout.NestFullViewHolder holder) {
                final ExpandableView expandableView = holder.getView(R.id.item_expanble);
                expandableView.setAdpater(queryInfo.uniques, new ExpandableView.OnBindDatas<UniquesInfo>() {
                    @Override
                    public int addClickView() {
                        return R.layout.item_expanble_querycar;
                    }

                    @Override
                    public void onBindClickView(ExpandableView.ViewHolder clickHolder) {
                        ImageView view = clickHolder.getView(R.id.iv_arrow);
                        TextView textView = clickHolder.getView(R.id.tv);
                        View lineView = clickHolder.getView(R.id.line_view);

                        textView.setText(queryInfo.name);

                        expandableView.setArrorAnimationView(view);

                        if (pos == itemCount - 1 && !expandableView.isExpandable()) {
                            lineView.setVisibility(View.GONE);
                        } else {
                            lineView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public int addChildView() {
                        return R.layout.item_expanble_caruniques;
                    }

                    @Override
                    public void onBindChildView(int contentPos, int contentCount, UniquesInfo uniquesInfo, ExpandableView.ViewHolder holder) {
                        TextView tvRight = holder.getView(R.id.tv_right);
                        TextView tvLeft = holder.getView(R.id.tv_left);
                        View lineView = holder.getView(R.id.line_view);
                        if (pos != itemCount - 1 && contentPos == contentCount - 1) {
                            lineView.setVisibility(View.VISIBLE);
                        } else {
                            lineView.setVisibility(View.GONE);
                        }
                        boolean delivered = uniquesInfo.delivered;
                        if (!delivered) {
                            tvRight.setText("呵呵哒");
                            tvRight.setTextColor(ActivityCompat.getColor(MainActivity.this, R.color.orange_FF8903));
                            tvLeft.setTextColor(ActivityCompat.getColor(MainActivity.this, R.color.orange_FF8903));
                        }
                        tvLeft.setText(uniquesInfo.snap);
                    }

                    @Override
                    public boolean expandableUpdataView() {
                        return true;
                    }
                });
            }

            @Override
            public void setLayoutParams(int pos, NestFullFlexboxLayout.NestFullViewHolder holder) {

            }
        });


        mExpandingList.getBoxLayout().setAdapter(R.layout.item_expandble_view, mInfos, new NestFullFlexboxLayout.OnBindDatas<QueryInfo>() {

            @Override
            public void onBind(int pos, int itemCount, QueryInfo queryInfo, NestFullFlexboxLayout.NestFullViewHolder holder) {
                final ExpandableView expandableView = holder.getView(R.id.item_expanble);
                expandableView.setAdpater(queryInfo.uniques, new ExpandableView.OnBindDatas() {
                    @Override
                    public int addClickView() {
                        return R.layout.item_expanble_querycar;
                    }

                    @Override
                    public void onBindClickView(ExpandableView.ViewHolder clickHolder) {
                        ImageView view = clickHolder.getView(R.id.iv_arrow);
                        expandableView.setArrorAnimationView(view);
                    }

                    @Override
                    public int addChildView() {
                        return R.layout.item_expanble_caruniques;
                    }

                    @Override
                    public void onBindChildView(int pos, int itemCount, Object o, ExpandableView.ViewHolder holder) {

                    }

                    @Override
                    public boolean expandableUpdataView() {
                        return false;
                    }
                });
            }

            @Override
            public void setLayoutParams(int pos, NestFullFlexboxLayout.NestFullViewHolder holder) {

            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new ExpandableViewAdapter(this,mInfos));
    }

    private void init() {
        for (int i = 0; i < modelName.length; i++) {
            QueryInfo queryInfo = new QueryInfo();
            queryInfo.finished = false;
            queryInfo.name = modelName[i];
            queryInfo.uniques = new ArrayList<>();
            for (int j = 0; j < uniques.length; j++) {
                UniquesInfo uniquesInfo = new UniquesInfo();
                uniquesInfo.snap = uniques[j];
                uniquesInfo.delivered = false;
                queryInfo.uniques.add(uniquesInfo);
            }
            mInfos.add(queryInfo);
        }
    }
}
