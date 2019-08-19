package view.waitou.expandableview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
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
        mExpandableView = findViewById(R.id.expan);

        init();

        mExpandableView.setAdapter(mInfos.size(), new ExpandableView.OnBindListener<QueryInfo>() {

            @Override
            public View bindTitleView(View titleView) {
                if(titleView == null){
                    titleView = View.inflate(ModelActivity.this,R.layout.item_expenable,null);
                    titleView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                mExpandableView.setArrowAnimationView(R.id.btn_fold);
                return titleView;
            }

            @Override
            public View bindChildView(int childPos, View child) {
                if(child == null){
                    child = View.inflate(ModelActivity.this,R.layout.item_expanble_caruniques,null);
                }
                TextView tvRight = child.findViewById(R.id.tv_right);
                TextView tvLeft = child.findViewById(R.id.tv_left);
                boolean delivered = mInfos.get(childPos).finished;
                if (!delivered) {
                    tvRight.setText("呵呵哒");
                    tvRight.setTextColor(ActivityCompat.getColor(ModelActivity.this, R.color.orange_FF8903));
                    tvLeft.setTextColor(ActivityCompat.getColor(ModelActivity.this, R.color.orange_FF8903));
                }
                tvLeft.setText(mInfos.get(childPos).name);
                return child;
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
