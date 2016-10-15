//package me.wizos.loread.activity;
//
///**
// * Created by Wizos on 2016/10/12.
// */
//public class ShowWebImageActivity {
//    //获取图片的地址
//    private String imageUrl = null;
//
//    //用户放大,缩小,旋转,
//    private PhotoView imageView = null;
//
//    private ImageButton btnBack;
//    private Button btnDownload;
//
//    private Handler mHandler;
//
//    {
//        super.onCreate(savedInstanceState);
//
//        btnBack = (ImageButton) findViewById(R.id.btn_back);
//        btnBack.setOnClickListener(this);
//
//        btnDownload = (Button) findViewById(R.id.btn_download);
//        btnDownload.setOnClickListener(this);
//
//        imageUrl = getIntent().getStringExtra("image");
//        //photoview
//        imageView = (PhotoView) findViewById(R.id.show_webimage_imageview);
//        // 启用图片缩放功能
//        imageView.enable();
//        //显示图片
//        ImageLoaderUtils.displayWhole(this, imageView, imageUrl);
//
//        mHandler = new Handler();
//
//    }
//
//
//    @Override
//    protected int getLayoutResId() {
//        return R.layout.activity_show_webimage;
//    }
//
//
//    @Override
//    public void onClick(View view) {
//
//        if (view == btnBack) {
//
//            finish();
//        } else if (view == btnDownload) {
//
//            Toast.makeText(getApplicationContext(), "开始下载图片", Toast.LENGTH_SHORT).show();
//
//            downloadImage();
//        }
//    }
//
//    /**
//     * 开始下载图片
//     */
//    private void downloadImage() {
//        downloadAsyn(imageUrl, Environment.getExternalStorageDirectory().getAbsolutePath() + "/ImagesFromWebView");
//    }
//
//
//    /**
//     * 异步下载文件
//     *
//     * @param url
//     * @param destFileDir 本地文件存储的文件夹
//     */
//    private void downloadAsyn(final String url, final String destFileDir) {
//
//        OkHttpUtil mOkHttpUtil = OkHttpUtil.getInstance();
//
//        OkHttpClient mOkHttpClient = mOkHttpUtil.getOkHttpClient();
//
//        final Request request = new Request.Builder()
//                .url(url)
//                .build();
//
//        final Call call = mOkHttpClient.newCall(request);
//
//        call.enqueue(new Callback() {
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//                Toast.makeText(getApplicationContext(), "下载失败,请检查网络设置", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//                InputStream is = null;
//                byte[] buf = new byte[2048];
//                int len = 0;
//                FileOutputStream fos = null;
//                try {
//                    is = response.body().byteStream();
//                    File file = new File(destFileDir);
//                    //如果file不存在,就创建这个file
//                    if (!file.exists()) {
//                        file.mkdir();
//                    }
//
//                    File imageFile = new File(destFileDir, getFileName(url) + ".jpg");
//                    fos = new FileOutputStream(imageFile);
//                    while ((len = is.read(buf)) != -1) {
//                        fos.write(buf, 0, len);
//                    }
//                    fos.flush();
//                    //如果下载文件成功，第一个参数为文件的绝对路径
//                    //sendSuccessResultCallback(file.getAbsolutePath(), callback);
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "下载成功", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                } catch (IOException e) {
//
//                    e.printStackTrace();
//
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            Toast.makeText(getApplicationContext(), "下载失败,请检查网络设置", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                } finally {
//                    try {
//                        if (is != null) is.close();
//                    } catch (IOException e) {
//                    }
//                    try {
//                        if (fos != null) fos.close();
//                    } catch (IOException e) {
//                    }
//                }
//
//            }
//        });
//    }
//
//    private String getFileName(String path) {
//        int separatorIndex = path.lastIndexOf("/");
//        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
//    }
//}
