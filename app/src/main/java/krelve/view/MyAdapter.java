package krelve.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wwjun.wang on 2015/9/18.
 */
public class MyAdapter extends BaseAdapter {
    private Context context;
    private List<String> mDatas;

    public MyAdapter(Context context, List<String> datas) {
        this.context = context;
        this.mDatas = datas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.tv);
        tv.setText(mDatas.get(position));
        return convertView;
    }

    public void remove(int position) {
        mDatas.remove(position);
        mDatas.add("我是新出现的");
        mDatas.add("我是新出现的");
        mDatas.add("我是新出现的");
        notifyDataSetChanged();
    }
}
