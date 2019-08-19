package view.waitou.expandableview;

import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import view.waitou.explibrary.ExpandableView;

/**
 * Created by waitou on 16/11/1.
 */

public class ExpandableViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<QueryInfo> mInfos;
    private LayoutInflater mInflater;
    private Context mContext;


    public ExpandableViewAdapter(Context context, List<QueryInfo> infos) {
        this.mInfos = infos;
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_expanble_recycle, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder holder1 = (ViewHolder) holder;
        final QueryInfo queryInfo = mInfos.get(holder1.getAdapterPosition());

        if(position == 2){
            ((ViewHolder) holder).mExpandingList.setKeepChild(1);
        }

        holder1.mExpandingList.setAdapter(queryInfo.uniques.size(), new ExpandableView.OnBindListener() {

            @Override
            public View bindTitleView(View titleView) {
                Log.e("aa" , " titleView " + titleView);
                if (titleView == null) {
                    titleView = View.inflate(mContext, R.layout.item_expanble_querycar, null);
                }
                holder1.mExpandingList.setArrowAnimationView(R.id.iv_arrow);
                TextView textView = titleView.findViewById(R.id.tv);
                textView.setText(queryInfo.name);
                View lineView = titleView.findViewById(R.id.line_view);
                if (holder1.getAdapterPosition() == getItemCount() - 1 && !holder1.mExpandingList.isExpandable()) {
                    lineView.setVisibility(View.GONE);
                } else {
                    lineView.setVisibility(View.VISIBLE);
                }
                return titleView;
            }

            @Override
            public View bindChildView(int childPos, View child) {
                Log.e("aa" , " child " + child);
                if(child == null){
                    child = View.inflate(mContext,R.layout.item_expanble_caruniques,null);
                }
                TextView tvRight = child.findViewById(R.id.tv_right);
                TextView tvLeft = child.findViewById(R.id.tv_left);
                View lineView = child.findViewById(R.id.line_view);

                if (holder1.getAdapterPosition() != getItemCount() - 1 && childPos == queryInfo.uniques.size() - 1) {
                    lineView.setVisibility(View.VISIBLE);
                } else {
                    lineView.setVisibility(View.GONE);
                }
                UniquesInfo uniquesInfo = queryInfo.uniques.get(childPos);
                boolean delivered = uniquesInfo.delivered;
                if (!delivered) {
                    tvRight.setText("呵呵哒");
                    tvRight.setTextColor(ActivityCompat.getColor(mContext, R.color.orange_FF8903));
                    tvLeft.setTextColor(ActivityCompat.getColor(mContext, R.color.orange_FF8903));
                }
                tvLeft.setText(uniquesInfo.snap);
                return child;
            }

            @Override
            public boolean expandableUpDataView() {
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mInfos.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        ExpandableView mExpandingList;

        public ViewHolder(View itemView) {
            super(itemView);
            mExpandingList = (ExpandableView) itemView.findViewById(R.id.flex_label);
        }
    }
}
