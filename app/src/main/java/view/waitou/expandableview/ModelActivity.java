package view.waitou.expandableview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import view.waitou.explibrary.ExpandableView;

/**
 * Created by waitou on 17/3/6.
 */

public class ModelActivity extends AppCompatActivity {


    private String[]        modelName = {"ALFA", "V8", "CM", "DPPPP"};
    private List<QueryInfo> mInfos    = new ArrayList<>();
    private ExpandableView mExpandableView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);
        mExpandableView = (ExpandableView) findViewById(R.id.expan);

        init();


        mExpandableView.setAdapter(mInfos, new ExpandableView.OnBindListener<QueryInfo>() {
            @Override
            public int addClickView() {
                return R.layout.item_expenable;
            }

            @Override
            public void onBindClickView(ExpandableView.ViewHolder clickHolder) {
                ImageView view = clickHolder.getView(R.id.btn_fold);
                mExpandableView.setArrorAnimationView(view);
            }

            @Override
            public int addChildView() {
                return R.layout.item_expanble_caruniques;
            }

            @Override
            public void onBindChildView(int contentPos, int contentCount, QueryInfo queryInfo, ExpandableView.ViewHolder holder) {
                TextView tvRight = holder.getView(R.id.tv_right);
                TextView tvLeft = holder.getView(R.id.tv_left);
                boolean delivered = queryInfo.finished;
                if (!delivered) {
                    tvRight.setText("呵呵哒");
                    tvRight.setTextColor(ActivityCompat.getColor(ModelActivity.this, R.color.orange_FF8903));
                    tvLeft.setTextColor(ActivityCompat.getColor(ModelActivity.this, R.color.orange_FF8903));
                }
                tvLeft.setText(queryInfo.name);
            }

            @Override
            public boolean expandableUpDataView() {
                return false;
            }
        });
    }

    private void init() {
        for (int i = 0; i < modelName.length; i++) {
            QueryInfo queryInfo = new QueryInfo();
            queryInfo.finished = false;
            queryInfo.name = modelName[i];
            mInfos.add(queryInfo);
        }
    }
}
