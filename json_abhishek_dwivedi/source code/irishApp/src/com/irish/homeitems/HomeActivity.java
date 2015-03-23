/* ************************************************
 * 
 * AUTHER: Abhishek Kumar Dwivedi
 * LOCATION: Delhi NCR, INDIA
 * 
 * ***********************************************/

package com.irish.homeitems;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.test.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/* *************************************************************
 * DESIGN LOGIC
 * ------------
 * 
 * * Main activity is created with only Linear layout and scrollView.
 * * Upon stating App, an AsyncTask gets created, which does download whole
 *   JSON file. And creates an object "jobjRoot" to hold this JSON object.
 * * If network is not available, nothing is done at this part but still jobjRoot
 *   is populated with previously saved JSON file, irishapps.json, in private data.
 * * Then image is downloaded, with information available in JSON object.
 * 
 * * At onPostExecution, home screen is drawn by creating dynamic views and placing it
 *   on LinearLayout.
 * 
 * *************************************************************/

public class HomeActivity extends Activity {
	
	private static final String tag = "IRIS";
	private static final String HomeUrl = "http://80.93.28.24/json/irishappstest/irishapps.json";
	private static final String HomeFile = "irishapps.json";
	private static JSONObject jobjRoot = null; //Will contain whole JSON received from HomeUrl
	
	private String imageHome = null;
	private boolean includeImageInLayout = false;
	private boolean includeTitleInLayout = false;
	private boolean includeTextInLayout = false;
	private String imagePosition = null;
	private String titlePosition = null;
	private String textPosition = null;
	private Map<String, View> position = new HashMap<String, View>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		new AsyncHomePageItems().execute(HomeUrl);
	}

	/*
	 * AsyncTask does all jobs from receiving content from server/private data and placing
	 * it on GUI.
	 * 
	 * */
	private class AsyncHomePageItems extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... url) {
			Log.d(tag, "doInBackground()"+url[0]);

			GetHomeJson(HomeUrl, HomeFile);

			DownloadImage();
			return true;
		}

		/*
		 * All data are received at doInBackgroud(). Now just use logic and place content
		 * into views in right order.
		 * */
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			UpdateHome();
		}
	}


	private void DownloadImage(){
    	try {

			/*
			 * If JSON is not found from past storage or recent download, nothing to do here.
			 * */

    		if (jobjRoot ==null){return;}
        	JSONArray aHomeItems = (JSONArray) jobjRoot.get("homeItems");
			JSONObject jHomeItems = aHomeItems.getJSONObject(0);
			
			Log.d(tag,tag+"jobj:"+jHomeItems.toString());
			JSONObject jo = (JSONObject) jHomeItems.get("image");
			String imagePath = jo.get("baseURL").toString()+jo.get("name").toString();
			imageHome = jo.getString("name").toString();
			
			Log.d(tag,tag+"ImageURL:"+imagePath);
			
			URL imageUrl = new URL(imagePath);
			URLConnection ucon = imageUrl.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
           
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			File file = new File(getApplicationContext().getFilesDir() ,imageHome);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();			
			
		} catch (FileNotFoundException e) {

			e.printStackTrace();
			
		} catch (JSONException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
		
	/*
	 * Get home JSON from cloud, or otherwise take it from saved private file.
	 * We can further optimize performance of app, by keeping these data in database.
	 * 
	 * */
	private void GetHomeJson(String Fileurl, String fileName){
		
		try {
			/*
			 * Is accessible to cloud?
			 * */
			ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
			
			Log.d(tag, "Network connection:"+isConnected);
			
			if(isConnected==true){ 
    	   
				URL url = new URL(Fileurl);
				URLConnection ucon = url.openConnection();

				/*
				 * Define InputStreams to read from the URLConnection.
				 * */
				InputStream is = ucon.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);

				/*
				 * Read bytes to the Buffer until there is nothing more to read(-1).
				 * */
				ByteArrayBuffer baf = new ByteArrayBuffer(50);
               
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}

				FileOutputStream fos = openFileOutput(HomeFile, Context.MODE_PRIVATE);
				fos.write(baf.toByteArray());
				fos.close();
				
				
				FileInputStream in;
				in = new FileInputStream(new File(getApplicationContext().getFilesDir(), HomeFile));
				@SuppressWarnings("resource")
				BufferedReader b = new BufferedReader(new InputStreamReader(in));
				jobjRoot = new JSONObject(b.readLine());
			}

       } catch (IOException e) {
               Log.d(tag, "Error: " + e);
       
       } catch (JSONException e) {
		e.printStackTrace();
       }
	}
	

	private boolean UpdateHome(){
		
        try {
			/*
			 * Found JSON object from cloud or available in private data? Nothing to do if didn't.
			 * */
			if(jobjRoot==null){return false;}

        	JSONArray aHomeItems = (JSONArray) jobjRoot.get("homeItems");
			JSONObject jHomeItems = aHomeItems.getJSONObject(0);
			includeImageInLayout = (boolean) jHomeItems.get("includeImageInLayout");
			includeTextInLayout = (boolean) jHomeItems.get("includeTextInLayout");
			includeTitleInLayout = (boolean) jHomeItems.get("includeTitleInLayout");
			
			
			/*
			 * Create views here dynamically and put in position map (position vs view mapping).
			 * These views could be provided with better appearance and for optimized timing, 
			 * if there are multiple such view sets. 
			 * For our case, only one set of views, this would be fine.
			 * */
			if(includeImageInLayout){
				imagePosition = (String) jHomeItems.get("imagePosition");
				ImageView iv = new ImageView(this);

				File image = new File(getApplicationContext().getFilesDir(), imageHome);
				if(image.exists()){
					iv.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
				}
				position.put(imagePosition, iv);
			}
			if(includeTextInLayout){
				textPosition = (String) jHomeItems.get("textPosition");
				String text = jHomeItems.getString("text");
				TextView textview = new TextView(this);
				textview.setText(text);
				position.put(textPosition, textview);
			}
			if(includeTitleInLayout){
				titlePosition = (String) jHomeItems.get("titlePosition");
				String title = jHomeItems.getString("title");
				TextView titleview = new TextView(this);
				titleview.setText(title);
				position.put(titlePosition, titleview);
			}
			
			LinearLayout ll = (LinearLayout) findViewById(R.id.home);
			
			/*
			 * Get top->middle->bottom from position map entry and place views in LinearLayout.
			 * */
			if(position.get("top")!=null){ll.addView(position.get("top"));}
			if(position.get("middle")!=null){ll.addView(position.get("middle"));}
			if(position.get("bottom")!=null){ll.addView(position.get("bottom"));}
			
		} catch (JSONException e) {

			e.printStackTrace();
		}
        return true;
	}
}