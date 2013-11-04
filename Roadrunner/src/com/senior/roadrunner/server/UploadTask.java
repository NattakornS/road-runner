package com.senior.roadrunner.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.senior.roadrunner.R;

public class UploadTask extends AsyncTask<String, Integer, String> {

	private Context context;
    int serverResponseCode = 0;
    DialogUpload dialog;
    String upLoadServerUri = "http://192.168.1.105/UploadToServer.php";
    final String uploadFilePath = "/mnt/sdcard/";
    final String uploadFileName = "tracker.xml";
    
    HttpURLConnection conn = null;
    DataOutputStream dos = null;  
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024 * 1024; 

    
	public UploadTask(Context context) {
		this.context=context;
	    dialog = new DialogUpload(this.context, this);
	    dialog.setTitle(this.context.getString(R.string.app_name));
	    dialog.setMessage("Upload result to server");
	}
@Override
protected void onPreExecute() {
	// TODO Auto-generated method stub
	super.onPreExecute();
	dialog.show();
}
@Override
protected void onPostExecute(String result) {
	// TODO Auto-generated method stub
	super.onPostExecute(result);
	dialog.dismiss();
}
	@Override
	protected String doInBackground(String... params) {
		// take CPU lock to prevent CPU from going off if the user 
        // presses the power button during download
		String fileUri = params[0];
	    File sourceFile = new File(fileUri);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
             getClass().getName());
        wl.acquire();
        System.out.println("Connection ");
        try {
//        	//Upload task
        	StringBuilder str = new StringBuilder();
    		HttpClient client = new DefaultHttpClient();
    		HttpPost httpPost = new HttpPost(upLoadServerUri);
    		List<NameValuePair> setPostEntity = new ArrayList<NameValuePair>();
    		
    		setPostEntity.add(new BasicNameValuePair("uploaded_path", "uploads/mahidol/"));
    		
    		try {
    			httpPost.setEntity(new UrlEncodedFormEntity(setPostEntity));
    			HttpResponse response = client.execute(httpPost);
    			StatusLine statusLine = response.getStatusLine();
    			int statusCode = statusLine.getStatusCode();
    			if (statusCode == 200) { // Status OK
    				HttpEntity entity = response.getEntity();
    				InputStream content = entity.getContent();
    				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
    				String line;
    				while ((line = reader.readLine()) != null) {
    					str.append(line);
    				}
    			} else {
    				Log.e("Log", "Failed to download result..");
    			}
    		} catch (ClientProtocolException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        	  // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
             
            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection(); 
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileUri); 
            conn.setRequestProperty("uploaded_path", "uploads/mahidol/");

            dos = new DataOutputStream(conn.getOutputStream());
   
            dos.writeBytes(twoHyphens + boundary + lineEnd); 
//            dos.writeBytes("Content-Disposition: form-data; name="uploaded_file";filename=""
//                                      + fileName + """ + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileUri +"\"" + lineEnd);
            dos.writeBytes(lineEnd);
   
            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available(); 
   
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
   
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
               
            while (bytesRead > 0) {
                 
              dos.write(buffer, 0, bufferSize);
              bytesAvailable = fileInputStream.available();
              bufferSize = Math.min(bytesAvailable, maxBufferSize);
              bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
               
             }
   
            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
   
            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();
              System.out.println(serverResponseMessage);
            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);
             
            if(serverResponseCode == 200){         
            	
//                         String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
//                                       +" http://www.androidexample.com/media/uploads/"
//                                       +uploadFileName;       
            }    
             
            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();
        }catch (MalformedURLException ex) {
            
           dialog.dismiss();  
           ex.printStackTrace();

       } catch (Exception e) {
            
           dialog.dismiss();  
           e.printStackTrace();
            

       }
        finally {
            wl.release();
        }
        dialog.dismiss(); 
		return null;
	}

}
