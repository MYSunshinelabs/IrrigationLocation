package com.irrigation.wifilocation.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUrlConnection extends AsyncTask<String, Void, String> {
    private static final String TAG = HttpUrlConnection.class.getSimpleName();
    private JSONObject obj;
    private GetJSONListener listener;
    private Map<String, List<String>> headerMap=new HashMap<>();
    private Boolean isPostRequest=false,isSocketTimeOut=false;
    private View view;
    private Context context;

    public HttpUrlConnection(GetJSONListener listener, JSONObject obj) {
        this.listener = listener;
        this.obj = obj;
        context= (Context) listener;
        isPostRequest=true;
    }

    public HttpUrlConnection(GetJSONListener listener) {
        this.listener = listener;
        context=(Context) listener;
        isPostRequest=false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(String... params) {
        if(Utils.isDeviceConnected(context)) {
            Utils.printLog(TAG+" Request Url====>>",params[0]);
            if (isPostRequest)
                return hitPostMethod(params[0]);
            else
                return hitGetMethod(params[0]);
        }else
            return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try{
            if(result!=null)
                if(isSocketTimeOut)
                    listener.onSocketTimeOut();
                else
                    listener.onRemoteCallComplete(result);

            if(result!=null)
                Utils.printLog(TAG+" Response====>>",result);
            else {
                Utils.printLog(TAG+" Response====>>","null");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface GetJSONListener {
        void onRemoteCallComplete(String jsonFromNet);
        void onSocketTimeOut();
    }

    public String hitPostMethod(String url) {
        String result = null;
        HttpURLConnection connection = null;
        try {

            URL u = new URL(url);
            //Set up the Connection
            connection = (HttpURLConnection) u.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(true);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.connect();

            //Send Json object to the Server
            if(obj!=null){
                OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
                wr.write(obj.toString());
                wr.close();
                Utils.printLog(TAG+" Body====>>",obj.toString());
            }

            int status = connection.getResponseCode();
            Utils.printLog(TAG+" Status====>>",status+"");
            // Get Response json from the server
            InputStream stream = connection.getErrorStream();
            if (stream == null) {
                stream = connection.getInputStream();
            }

            headerMap=connection.getHeaderFields();
            BufferedReader br = new BufferedReader(new InputStreamReader(stream,"utf-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if(isCancelled())
                    return "";
                sb.append(line);
            }
            br.close();
            result= sb.toString();

        }catch (SocketTimeoutException se){
            se.printStackTrace();
            isSocketTimeOut=true;
        }
        catch (Exception e){
            Utils.printLog(TAG,e.getMessage());
        }

        finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    Utils.printLog(TAG,e.getMessage());
                }
            }
        }

        return result;
    }

    public String hitGetMethod(String u){
        URL url = null;
        String result=null;
        HttpURLConnection connection = null;
        try {
            url = new URL(u);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(true);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.connect();

            int responseCode = connection.getResponseCode();

            final StringBuilder output = new StringBuilder("Request URL " + url);
            output.append(System.getProperty("line_white.separator") + "Response Code " + responseCode);
            output.append(System.getProperty("line_white.separator") + "Type " + "GET");

            headerMap=connection.getHeaderFields();
            // Get Response json from the server
            InputStream stream = connection.getErrorStream();
            if (stream == null) {
                stream = connection.getInputStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line = "";
            StringBuilder responseOutput = new StringBuilder();

            while((line = br.readLine()) != null ) {
                if(isCancelled())
                    return "";
                responseOutput.append(line);
            }
            br.close();
            result= responseOutput.toString();

        } catch (SocketTimeoutException se) {
            isSocketTimeOut=true;
        } catch (Exception e){
            Utils.printLog(TAG,e.toString());
        }

        finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    Utils.printLog(TAG,e.getMessage());
                }
            }
        }
        return result;
    }

    public Map<String, List<String>> getHeaderMap(){
        return headerMap;
    }
}

