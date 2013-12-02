package com.senior.roadrunner.finish;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.senior.roadrunner.R;
import com.senior.roadrunner.racetrack.TrackMemberList;

public class FinishAdaptor extends BaseAdapter implements OnClickListener{

	private Activity activity;
	private ArrayList<TrackMemberList> data;
	private LayoutInflater inflater;
	private TrackMemberList tempValues;

	public FinishAdaptor(FinishActivity finishActivity, ArrayList<TrackMemberList> trackMemberList) {
        activity = finishActivity;
        data=trackMemberList;
        
        /***********  Layout inflator to call external xml layout () **********************/
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	  /******** What is the size of Passed Arraylist Size ************/
    public int getCount() {
    	
    	if(data.size()<=0)
    		return 1;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    public static class ViewHolder{
    	
        public TextView nameTxt;
        public TextView durationTxt;
        public TextView placeTxt;
        public ImageView image;

    }
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi=convertView;
        ViewHolder holder;
        
        if(convertView==null){
        	
        	/********** Inflate tabitem.xml file for each row ( Defined below ) ************/
            vi = inflater.inflate(R.layout.finish_list_item, null); 
            vi.setBackgroundColor(Color.alpha(R.color.blue_gradientE));
//            vi.setBackgroundResource(R.drawable.rounded_corners);
            /******** View Holder Object to contain tabitem.xml file elements ************/
            holder=new ViewHolder();
            holder.nameTxt=(TextView)vi.findViewById(R.id.finish_name_list_txt);
            holder.durationTxt=(TextView)vi.findViewById(R.id.finish_duration_list_txt);
            holder.placeTxt=(TextView)vi.findViewById(R.id.finish_place_list_txt);
            holder.image=(ImageView)vi.findViewById(R.id.finish_pic_list_img);
             
           /************  Set holder with LayoutInflater ************/
            vi.setTag(holder);
        }
        else  
            holder=(ViewHolder)vi.getTag();
        
        if(data.size()<=0)
        {
        	holder.nameTxt.setText("No Data");
            
        }
        else
        {
        	/***** Get each Model object from Arraylist ********/
	        tempValues=null;
	        tempValues = data.get(position);
	        
	        /************  Set Model values in Holder elements ***********/
	         holder.nameTxt.setText(tempValues.getfId());
	         holder.placeTxt.setText(tempValues.getRank()+"");
//	         holder.durationTxt.setText(tempValues.getRank());3
//	         holder.image.setImageResource(res.getIdentifier("com.androidexample.customlistview:drawable/"+tempValues.getImage(),null,null));
	         
	         /******** Set Item Click Listner for LayoutInflater for each row ***********/
	         vi.setOnClickListener(new OnItemClickListener(position));
        }
        return vi;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	/********* Called when Item click in ListView ************/
    private class OnItemClickListener  implements OnClickListener{           
        private int mPosition;
        
        OnItemClickListener(int position){
        	 mPosition = position;
        	 
        }
        
        @Override
        public void onClick(View arg0) {
        	FinishActivity sct = (FinishActivity)activity;
        	sct.onItemClick(mPosition);
        	Toast.makeText(activity, "click : "+mPosition, 1000).show();
        }               
    }   

}
