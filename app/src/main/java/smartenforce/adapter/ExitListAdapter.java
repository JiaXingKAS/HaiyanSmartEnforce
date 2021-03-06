package smartenforce.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kas.clientservice.haiyansmartenforce.R;

import smartenforce.bean.tcsf.TcListBeanResult;

import java.util.List;


public class ExitListAdapter extends RecyclerView.Adapter<ExitListAdapter.ViewHolder> {
    List<TcListBeanResult> list;
    Context mContext;
    LayoutInflater inflater;
    private OnItemClickListener onItemClickListener;

    public ExitListAdapter(List<TcListBeanResult> list, Context mContext) {
        this.list = list;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_exit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tev_cphm.setText(list.get(position).carnum);
        holder.tev_pwbh.setText(list.get(position).Berthname);
        holder.tev_trsj.setText(list.get(position).starttime);
        holder.llt_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tev_pwbh, tev_cphm, tev_trsj;
        LinearLayout llt_item;

        public ViewHolder(View itemView) {
            super(itemView);
            tev_pwbh = (TextView) itemView.findViewById(R.id.tev_pwbh);
            tev_cphm = (TextView) itemView.findViewById(R.id.tev_cphm);
            tev_trsj = (TextView) itemView.findViewById(R.id.tev_trsj);
            llt_item = (LinearLayout) itemView.findViewById(R.id.llt_item);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int p);

    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

}

