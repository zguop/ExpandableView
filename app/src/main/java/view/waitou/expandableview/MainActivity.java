package view.waitou.expandableview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String[] modelName = {"ALFA", "V8", "CM", "DPPPP"};
    private String[] uniques = {"444", "AAA", "BBBB", "444", "444", "444"};
    private List<QueryInfo> mInfos = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ExpandableViewAdapter expandableViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ModelActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<UniquesInfo> uniques = mInfos.get(0).uniques;
                UniquesInfo uniquesInfo = new UniquesInfo();
                uniquesInfo.snap = "添加的";
                uniques.add(uniquesInfo);
                expandableViewAdapter.addData(mInfos);
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<UniquesInfo> uniques = mInfos.get(0).uniques;
                uniques.remove(uniques.size() - 1);
                expandableViewAdapter.addData(mInfos);
            }
        });


        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        init();
        bindView();
    }

    private void bindView() {
        expandableViewAdapter = new ExpandableViewAdapter(this, mInfos);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(expandableViewAdapter);
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
