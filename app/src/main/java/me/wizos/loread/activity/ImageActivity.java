package me.wizos.loread.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import java.io.File;

import me.wizos.loread.R;
import me.wizos.loread.adapter.MaterialSimpleListAdapter;
import me.wizos.loread.adapter.MaterialSimpleListItem;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.view.DragPhotoView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

/**
 * @author Wizos on 2018/06/06
 */
public class ImageActivity extends AppCompatActivity {
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Intent imageIntent = getIntent();
        imageUri = imageIntent.getData();

//        KLog.e("图片路径" + imageUri.getPath() + "  " + imageUri.getHost() );
        DragPhotoView originalImage = findViewById(R.id.image_photo_view);


        //必须添加一个onExitListener,在拖拽到底部时触发
        originalImage.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        originalImage.setOnExitListener(new DragPhotoView.OnExitListener() {
            @Override
            public void onExit(DragPhotoView dragPhotoView, float v, float v1, float v2, float v3) {
                exit();
            }
        });

        originalImage.setOnTapListener(new DragPhotoView.OnTapListener() {
            @Override
            public void onTap(DragPhotoView dragPhotoView) {
                exit();
            }

            @Override
            public void onLongTap(me.wizos.loread.view.DragPhotoView view) {
                MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(ImageActivity.this);
                adapter.add(new MaterialSimpleListItem.Builder(ImageActivity.this)
                        .content(R.string.image_dialog_save_img)
                        .backgroundColor(Color.TRANSPARENT)
                        .build());
                adapter.add(new MaterialSimpleListItem.Builder(ImageActivity.this)
                        .content(R.string.image_dialog_share_img)
                        .backgroundColor(Color.TRANSPARENT)
                        .build());
                new MaterialDialog.Builder(ImageActivity.this)
                        .adapter(adapter, new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                dialog.dismiss();
                                switch (which) {
                                    case 0:
//                                        KLog.e("正在复制图片", imagePath + "  " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/"+ new File(imagePath).getName()  );
                                        try {
                                            FileUtil.copyFileToPictures(new File(imageUri.getPath()));
//                                            FileUtil.copyFileToPictures(new File("/storage/emulated/0/Android/data/me.wizos.loread/files/cache/1d3403c4bd6519744ffc7efd2dbf2e60/1d3403c4bd6519744ffc7efd2dbf2e60_files/0-0f6ebf6b5ba6e4c6eac8946be9ca8153874e469b"));
                                            ToastUtil.showLong("图片已经保存到相册");
                                        } catch (Exception e) {
                                            return;
                                        }
                                        break;
                                    case 1:
//                                        Uri imageUri = Uri.fromFile( new File(imagePath ) );
                                        Intent shareIntent = new Intent();
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                        shareIntent.setType("image/*");
                                        startActivity(Intent.createChooser(shareIntent, "分享图片"));
                                        break;
                                    default:
                                        break;
                                }
                            }
                        })
                        .show();
            }
        });
        originalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exit();
            }
        });

        Glide.with(this)
                .load(imageUri)
                .into(originalImage);
    }

    private void exit() {
        ImageActivity.this.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
