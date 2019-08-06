package com.hzandriod.callcenter.helper;

    import android.os.Environment;
    import android.util.Log;

    import com.google.gson.Gson;

    import java.io.File;
    import java.io.FileNotFoundException;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.Reader;
    import java.lang.reflect.ParameterizedType;
    import java.lang.reflect.Type;
    import java.net.ConnectException;
    import java.net.FileNameMap;
    import java.net.SocketException;
    import java.net.SocketTimeoutException;
    import java.net.URLConnection;
    import java.net.UnknownHostException;
    import java.sql.ParameterMetaData;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;
    import java.util.concurrent.TimeUnit;

    import okhttp3.Cache;
    import okhttp3.Call;
    import okhttp3.Callback;
    import okhttp3.Cookie;
    import okhttp3.CookieJar;
    import okhttp3.FormBody;
    import okhttp3.Headers;
    import okhttp3.HttpUrl;
    import okhttp3.MediaType;
    import okhttp3.MultipartBody;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.RequestBody;
    import okhttp3.Response;
    import okhttp3.ResponseBody;

/**
 * @author ytf
 */
public class OkHttpHelper {
    private static OkHttpClient okHttpClient;
    private volatile static OkHttpHelper instance;//防止多个线程同时访问
    //提交json数据
    private static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    //提交字符串数据
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown;charset=utf-8");
    private static String responseStrGETAsyn;

    // Log打印的通用Tag
    private final String TAG = "OkHttpHelper";

    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    // 使用getCacheDir()来作为缓存文件的存放路径（/data/data/包名/cache） ，
// 如果你想看到缓存文件可以临时使用 getExternalCacheDir()（/sdcard/Android/data/包名/cache）。
    private static File cacheFile;
    private static Cache cache;

    public OkHttpHelper() {
//        if (APP.getInstance().getApplicationContext().getCacheDir()!=null){
//            cacheFile = new File(APP.getInstance().getCacheDir(), "Test");
//            cache = new Cache(cacheFile, 1024 * 1024 * 10);
//        }

        okHttpClient = new OkHttpClient();
        okHttpClient.newBuilder()
//                .addInterceptor(new HeaderInterceptor())
//                .addNetworkInterceptor(new CacheInterceptor())
//                .cache(cache)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
//自动管理Cookie发送Request都不用管Cookie这个参数也不用去response获取新Cookie什么的了。还能通过cookieStore获取当前保存的Cookie。
                    }
                });
    }


    /**
     * 懒汉式加锁单例模式
     * @return
     */
    public static OkHttpHelper getInstance() {
        if (instance == null) {
            synchronized (OkHttpHelper.class) {
                if (instance == null) {
                    instance = new OkHttpHelper();
                }
            }
        }
        return instance;
    }


    /**
     * get同步请求不需要传参数
     * 通过response.body().string()获取返回的字符串
     *
     * @param url
     * @return
     */
    public String getSyncBackString(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            // 将response转化成String
            String responseStr = response.body().string();
            return responseStr;
        } catch (IOException e) {
            Log.d(TAG,"GET同步请求解析为String异常" + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get同步请求
     * 通过response.body().bytes()获取返回的二进制字节数组
     *
     * @param url
     * @return
     */
    public byte[] getSyncBackByteArray(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            // 将response转化成String
            byte[] responseStr = response.body().bytes();
            return responseStr;
        } catch (IOException e) {
            Log.d(TAG,"GET同步请求解析为byte数组异常" + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get同步请求
     * 通过response.body().byteStream()获取返回的二进制字节流
     *
     * @param url
     * @return
     */
    public InputStream getSyncBackByteStream(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            // 将response转化成String
            InputStream responseStr = response.body().byteStream();
            return responseStr;
        } catch (IOException e) {
            Log.d(TAG,"GET同步请求解析为String异常" + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get同步请求
     * 通过response.body().byteStream()获取返回的二进制字节流
     *
     * @param url
     * @return
     */
    public Reader getSyncBackCharReader(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            // 将response转化成String
            Reader responseStr = response.body().charStream();
            return responseStr;
        } catch (IOException e) {
            Log.d(TAG,"GET同步请求解析为Reader异常" + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get异步请求不传参数
     * 通过response.body().string()获取返回的字符串
     * 异步返回值不能更新UI，要开启新线程
     *
     * @param url
     * @return
     */
    public String getAsynBackStringWithoutParms(String url, final MyDataCallBack myDataCallBack) {

        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            myDataCallBack.onBefore(request);
            // 请求加入调度
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
//                 Logger.e("GET异步请求为String失败"+e.toString());
                    myDataCallBack.requestFailure(request, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    responseStrGETAsyn = response.body().string();
                    try {
                        myDataCallBack.requestSuccess(responseStrGETAsyn);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG,"GET异步请求为String解析异常失败" + e.toString());
                    }

                }
            });

            myDataCallBack.onAfter();
        } catch (Exception e) {
            Log.d(TAG,"GET异步请求解析为String异常" + e.toString());
            e.printStackTrace();
        }
        return responseStrGETAsyn;
    }

    /**
     * get异步请求传参数(可以传null)
     * 通过response.body().string()获取返回的字符串
     * 异步返回值不能更新UI，要开启新线程
     *
     * @param url
     * @return
     */
    public String getAsynBackStringWithParms(String url, Map<String, String> params, final MyDataCallBack myDataCallBack) {
        if (params == null) {
            params = new HashMap<>();
        }
        // 请求url（baseUrl+参数）
        String doUrl = urlJoint(url, params);
        final Request request = new Request.Builder()
                .url(doUrl)
//                .header("Cookie", "自动管理更新需要携带的Cookie")
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            myDataCallBack.onBefore(request);
            // 请求加入调度
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
//                 Logger.e("GET异步请求为String失败"+e.toString());
                    myDataCallBack.requestFailure(request, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    responseStrGETAsyn = response.body().string();
                    try {
                        myDataCallBack.requestSuccess(responseStrGETAsyn);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG,"GET异步请求为String解析异常失败" + e.toString());
                    }

                }
            });

            myDataCallBack.onAfter();
        } catch (Exception e) {
            Log.d(TAG,"GET异步请求解析为String异常" + e.toString());
            e.printStackTrace();
        }
        return responseStrGETAsyn;
    }

    /**
     * post异步请求map传参
     * 通过response.body().string()获取返回的字符串
     * 异步返回值不能更新UI，要开启新线程
     *
     * @param url
     * @return
     */
    public String postAsynBackString(String url, Map<String, String> params, final MyDataCallBack myDataCallBack) {
        RequestBody requestBody;
        if (params == null) {
            params = new HashMap<>();
        }

        FormBody.Builder builder = new FormBody.Builder();
        /**
         * 在这对添加的参数进行遍历
         */
        addMapParmsToFromBody(params, builder);

        requestBody = builder.build();
        String realURL = urlJoint(url, null);
        //结果返回
        final Request request = new Request.Builder()
                .url(realURL)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            myDataCallBack.onBefore(request);
            // 请求加入调度
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    myDataCallBack.requestFailure(request, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    responseStrGETAsyn = response.body().string();//此处也可以解析为byte[],Reader,InputStream
                    try {
                        myDataCallBack.requestSuccess(responseStrGETAsyn);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG,"POST异步请求为String解析异常失败" + e.toString());
                    }

                }
            });

            myDataCallBack.onAfter();
        } catch (Exception e) {
            Log.e(TAG,"POST异步请求解析为String异常" + e.toString());
            e.printStackTrace();
        }
        return responseStrGETAsyn;
    }

    private void addMapParmsToFromBody(Map<String, String> params, FormBody.Builder builder) {
        for (Map.Entry<String, String> map : params.entrySet()) {
            String key = map.getKey();
            String value;
            /**
             * 判断值是否是空的
             */
            if (map.getValue() == null) {
                value = "";
            } else {
                value = map.getValue();
            }
            /**
             * 把key和value添加到formbody中
             */
            builder.add(key, value);
        }
    }

    /**
     * post异步请求json传参
     * 通过response.body().string()获取返回的字符串
     * 异步返回值不能更新UI，要开启新线程
     *
     * @param url
     * @return
     */
    public String postAsynRequireJson(String url, Map<String, String> params, final MyDataCallBack myDataCallBack) {

        if (params == null) {
            params = new HashMap<>();
        }
        // 将map转换成json,需要引入Gson包
        String mapToJson = new Gson().toJson(params);
        final String realURL = urlJoint(url, null);
        final Request request = buildJsonPostRequest(realURL, mapToJson);
        Call call = okHttpClient.newCall(request);
        try {
            myDataCallBack.onBefore(request);
            // 请求加入调度
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    myDataCallBack.requestFailure(request, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    responseStrGETAsyn = response.body().string();//此处也可以解析为byte[],Reader,InputStream
                    try {
                        myDataCallBack.requestSuccess(responseStrGETAsyn);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,"POST异步请求为String解析异常失败" + e.toString());
                    }

                }
            });

            myDataCallBack.onAfter();
        } catch (Exception e) {
            Log.e(TAG,"POST异步请求解析为String异常" + e.toString());
            e.printStackTrace();
        }
        return responseStrGETAsyn;
    }

    /**
     * Json_POST请求参数
     *
     * @param url  url
     * @param json json
     * @return requestBody
     */
    private Request buildJsonPostRequest(String url, String json) {
//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        RequestBody requestBody = RequestBody.create(JSON, json);
        return new Request.Builder().url(url).post(requestBody).build();
    }

    /**
     * String_POST请求参数
     *
     * @param url  url
     * @param json json
     * @return requestBody
     */
    private Request buildStringPostRequest(String url, String json) {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_MARKDOWN, json);
        return new Request.Builder().url(url).post(requestBody).build();
    }

    /**
     * @param url    实际URL的path
     * @param params
     * @return
     */
    private static String urlJoint(String url, Map<String, String> params) {
        StringBuilder realURL = new StringBuilder();
        realURL = realURL.append(url);
        boolean isFirst = true;
        if (params == null) {
            params = new HashMap<>();
        } else {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                if (isFirst && !url.contains("?")) {
                    isFirst = false;
                    realURL.append("?");
                } else {
                    realURL.append("&");
                }
                realURL.append(entry.getKey());
                realURL.append("=");
                if (entry.getValue() == null) {
                    realURL.append(" ");
                } else {
                    realURL.append(entry.getValue());
                }

            }
        }

        return realURL.toString();
    }

    /**
     * 基于http的文件上传（传入文件名和key）
     * 通过addFormDataPart
     *
     * @param url            URL的Path部分
     * @param myDataCallBack 自定义回调接口
     *                       将file作为请求体传入到服务端.
     * @param fileKey        文件传入服务器的键"image"
     * @fileName: "pic.png"
     */
    private void upLoadMultiFileSimple(String url, String fileName, String fileKey, final MyDataCallBack myDataCallBack) {
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fileKey, fileName, fileBody)
                .build();
        final String realURL = urlJoint(url, null);
        final Request request = new Request.Builder()
                .url(realURL)
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        try {
            myDataCallBack.onBefore(request);
            // 请求加入调度
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    myDataCallBack.requestFailure(request, e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        myDataCallBack.requestSuccess(responseStrGETAsyn);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,"POST异步文件上传失败" + e.toString());
                    }

                }
            });

            myDataCallBack.onAfter();
        } catch (Exception e) {
            Log.e(TAG,"POST异步文件上传异常" + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 基于http的文件上传（传入文件数组和key）混合参数和文件请求
     * 通过addFormDataPart可以添加多个上传的文件
     *
     * @param url            URL的Path部分
     * @param myDataCallBack 自定义回调接口
     *                       将file作为请求体传入到服务端.
     * @param files          上传的文件
     * @param fileKeys       上传的文件key集合
     */
    private void upLoadMultiFile(String url, File[] files, String[] fileKeys, Map<String, String> params, final MyDataCallBack myDataCallBack) {
        if (params == null) {
            params = new HashMap<>();
        }
        final String realURL = urlJoint(url, null);
        FormBody.Builder builder = new FormBody.Builder();
        addMapParmsToFromBody(params, builder);
        RequestBody requestBody = builder.build();
        MultipartBody.Builder multipartBody = new MultipartBody.Builder();
        multipartBody.setType(MultipartBody.ALTERNATIVE)
                .addPart(requestBody);

        if (files != null) {
            RequestBody fileBody = null;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                //TODO 根据文件名设置contentType
                multipartBody.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""),
                        fileBody);
            }

        }

        final Request request = new Request.Builder()
                .url(realURL)
                .post(multipartBody.build())
                .build();

        Call call = okHttpClient.newCall(request);
        try {
            myDataCallBack.onBefore(request);
            // 请求加入调度
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    myDataCallBack.requestFailure(call.request(), e);
                }

                @Override
                public void onResponse(Call call, Response response)  {
                    try {
                        myDataCallBack.requestSuccess(response.body().string());

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG,"POST异步文件上传失败" + e.toString());
                    }

                }
            });

            myDataCallBack.onAfter();
        } catch (Exception e) {
            Log.e(TAG,"POST异步文件上传失败" + e.toString());
            e.printStackTrace();
        }
    }

    private String guessMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(fileName);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;

    }

    /**
     * 文件下载
     * @param url path路径
     * @param destFileDir 本地存储的文件夹路径
     * @param myDataCallBack 自定义回调接口
     */
    private void downLoadFileAsyn(final String url, final String destFileDir, final MyDataCallBack myDataCallBack){
        String realURL=urlJoint(url,null);
        Request request=new Request.Builder()
                .url(realURL)
                .build();
        Call call=okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                myDataCallBack.requestFailure(call.request(),e);

            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                is = response.body().byteStream();
                File file = new File(destFileDir, getFileName(url));
                try {
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1)
                    {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                } catch (IOException e) {
                    Log.e(TAG,"文件下载异常" + e.toString());
                    e.printStackTrace();
                }finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            Log.e(TAG,"文件流关闭异常" + e.toString());
                            e.printStackTrace();
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            Log.e(TAG,"文件流关闭异常" + e.toString());
                            e.printStackTrace();
                        }
                    }

                }

                //如果下载文件成功，第一个参数为文件的绝对路径
//                sendSuccessResultCallback(file.getAbsolutePath(), myDataCallBack);

                myDataCallBack.requestSuccess(response.body().toString());

            }
        });
    }

    private String getFileName(String url) {
        int separatorIndex = url.lastIndexOf("/");
        return (separatorIndex < 0) ? url : url.substring(separatorIndex + 1, url.length());

    }

//    private void sendSuccessResultCallback(final String absolutePath, final MyDataCallBack myDataCallBack) {
//        ThreadPoolUtil.execute(new Runnable() {
//            @Override
//            public void run() {
//                if (myDataCallBack!=null){
//                    myDataCallBack.requestSuccess(absolutePath);
//                }
//            }
//        });
//    }

}


