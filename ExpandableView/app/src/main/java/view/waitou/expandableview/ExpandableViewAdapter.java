package view.waitou.expandableview;

import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import view.waitou.explibrary.ExpandableView;

/**
 * Created by waitou on 16/11/1.
 */

public class ExpandableViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<QueryInfo> mInfos;
    private LayoutInflater  mInflater;
    private Context         mContext;


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
    public void onBindViewHolder(RecyclerView.ViewHolder holder,  int position) {
        final ViewHolder holder1 = (ViewHolder) holder;
        final QueryInfo queryInfo = mInfos.get(holder1.getAdapterPosition());

        holder1.mExpandingList.setAdpater(queryInfo.uniques, new ExpandableView.OnBindDatas<UniquesInfo>() {
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

                holder1.mExpandingList.setArrorAnimationView(view);

                if (holder1.getAdapterPosition() == getItemCount() - 1 && !holder1.mExpandingList.isExpandable()) {
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
                if (holder1.getAdapterPosition() != getItemCount() - 1 && contentPos == contentCount - 1) {
                    lineView.setVisibility(View.VISIBLE);
                } else {
                    lineView.setVisibility(View.GONE);
                }
                boolean delivered = uniquesInfo.delivered;
                if (!delivered) {
                    tvRight.setText("呵呵哒");
                    tvRight.setTextColor(ActivityCompat.getColor(mContext, R.color.orange_FF8903));
                    tvLeft.setTextColor(ActivityCompat.getColor(mContext, R.color.orange_FF8903));
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
