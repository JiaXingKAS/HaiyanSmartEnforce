package com.kas.clientservice.haiyansmartenforce.Module.Ledger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kas.clientservice.haiyansmartenforce.Entity.LedgerListEntity;
import com.kas.clientservice.haiyansmartenforce.R;

import java.util.List;

/**
 * 描述：
 * 时间：2018-08-02
 * 公司：COMS
 */

public class LedgerListAdapter extends BaseAdapter {
    Context mContext;
    List<LedgerListEntity.RowsBean> list;

    public LedgerListAdapter(Context mContext, List<LedgerListEntity.RowsBean> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder vh = null;
        if (view == null) {
            vh = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_city_search,null);
            vh.tv_describe = (TextView) view.findViewById(R.id.tv_item_citySearch_describe);
            vh.tv_name = (TextView) view.findViewById(R.id.tv_item_citySearch_name);
            vh.tv_time = (TextView) view.findViewById(R.id.tv_item_citySearch_time);

            view.setTag(vh);
        }else vh = (ViewHolder) view.getTag();

        vh.tv_name.setText(list.get(i).getNameAre());
        vh.tv_time.setText(list.get(i).getLedgerTypeName());
        vh.tv_describe.setText(list.get(i).getText());
        return view;
    }

    class ViewHolder{
        TextView tv_name,tv_time,tv_describe;
    }
}
