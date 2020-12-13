package com.shym.petfind.Cards;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shym.petfind.R;

import java.util.List;

public class arrayAdapter extends ArrayAdapter<cards>{

    Context context;

    public arrayAdapter(Context context, int resourceId, List<cards> items){
        super(context, resourceId, items);
    }
    public View getView(int position, View convertView, ViewGroup parent){
        cards card_item = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        name.setText(card_item.getName());
        switch(card_item.getProfileImageUrl()){
            case "default":
                Glide.with(convertView.getContext()).load(R.mipmap.ic_launcher).into(image);
                break;
            default:
                Glide.clear(image);
                Glide.with(convertView.getContext()).load(card_item.getProfileImageUrl()).into(image);
                break;
        }

        if (card_item.getPet() != null && card_item.isClicked()) {
            convertView.findViewById(R.id.pet_layout).setVisibility(View.VISIBLE);
            TextView pet = (TextView) convertView.findViewById(R.id.textView12);
            pet.setText(card_item.getPet());
        }

        if (card_item.getColor() != null && card_item.isClicked()) {
            convertView.findViewById(R.id.color_layout).setVisibility(View.VISIBLE);
            TextView color = (TextView) convertView.findViewById(R.id.textView14);
            color.setText(card_item.getColor());
        }

        if (card_item.getCity() != null && card_item.isClicked()) {
            convertView.findViewById(R.id.city_layout).setVisibility(View.VISIBLE);
            TextView city = (TextView) convertView.findViewById(R.id.textView20);
            city.setText(card_item.getCity());
        }

        if (card_item.getDescription() != null && card_item.isClicked()) {
            convertView.findViewById(R.id.description_layout).setVisibility(View.VISIBLE);
            TextView desc = (TextView) convertView.findViewById(R.id.textView21);
            desc.setText(card_item.getDescription());
        }

        return convertView;

    }
}
