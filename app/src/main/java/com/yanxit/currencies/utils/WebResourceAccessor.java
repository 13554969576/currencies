package com.yanxit.currencies.utils;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

/**
 * Created by dongc on 8/25/2017.
 */

public class WebResourceAccessor {
    public static <T> T getObjectFromUrl(String httpUrl,Class<T> clazz){
        HttpURLConnection conn = null;
        try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.5.157.46",80));
            URL url = new URL(httpUrl);
            conn = (HttpURLConnection)url.openConnection(proxy);
            conn.setUseCaches(false);
            conn.connect();
            InputStream in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int c;
            while((c=in.read()) != -1)out.write(c);
            String s = new String(out.toByteArray(),"UTF-8");
            if(clazz.isAssignableFrom(JsonNode.class)){
                return (T)new ObjectMapper().readTree(s) ;
            } else {
                return new ObjectMapper().readerFor(clazz).readValue(s);
            }

        } catch (IOException e) {
           throw new RuntimeException(e);
        } finally {
            if(conn!=null){
                conn.disconnect();
            }
        }


    }
}
