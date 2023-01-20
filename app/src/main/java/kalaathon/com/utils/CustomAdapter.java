package kalaathon.com.utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import kalaathon.com.R;

public class CustomAdapter extends BaseAdapter {
    Context context;
    int icons[];
    String[] items;
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, int[] icons, String[] items) {
        this.context = applicationContext;
        this.icons = icons;
        this.items = items;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return icons.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.custom_spinner, null);
        ImageView icon = (ImageView) view.findViewById(R.id.imageView);
        TextView names = (TextView) view.findViewById(R.id.textView);
        icon.setImageResource(icons[i]);
        names.setText(items[i]);
        if(i==0) {
            icon.setVisibility(View.GONE);
            names.setText("  "+items[i]+"  ");
            names.setTextColor(ContextCompat.getColor(context,R.color.grey));
        }
        return view;
    }
}