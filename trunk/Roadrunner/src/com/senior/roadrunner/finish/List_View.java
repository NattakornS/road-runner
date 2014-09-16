package com.senior.roadrunner.finish;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.senior.roadrunner.R;

public class List_View extends ListFragment{
	String[] list_items;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.list, container, false);
		list_items = getResources().getStringArray(R.array.list);
		setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list_items));
		
		return rootView;
	}

}
