package com.kas.clientservice.haiyansmartenforce.Module.CaseCommit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.kas.clientservice.haiyansmartenforce.API.CaseCommit;
import com.kas.clientservice.haiyansmartenforce.API.ZhuanXiangZhengZhiAPI;
import com.kas.clientservice.haiyansmartenforce.Base.BaseActivity;
import com.kas.clientservice.haiyansmartenforce.Entity.Img;
import com.kas.clientservice.haiyansmartenforce.Http.ExceptionHandle;
import com.kas.clientservice.haiyansmartenforce.Http.MySubscriber;
import com.kas.clientservice.haiyansmartenforce.Http.RequestUrl;
import com.kas.clientservice.haiyansmartenforce.Http.RetrofitClient;
import com.kas.clientservice.haiyansmartenforce.Module.IllegalParking.IllegalParkingCommitImgRvAdapter;
import com.kas.clientservice.haiyansmartenforce.Module.IllegalParking.ImageActivity;
import com.kas.clientservice.haiyansmartenforce.Module.TianDiTu.TiandiMapActivity;
import com.kas.clientservice.haiyansmartenforce.R;
import com.kas.clientservice.haiyansmartenforce.User.UserSingleton;
import com.kas.clientservice.haiyansmartenforce.Utils.BitmapToBase64;
import com.kas.clientservice.haiyansmartenforce.Utils.Constants;
import com.kas.clientservice.haiyansmartenforce.Utils.Dp2pxUtil;
import com.kas.clientservice.haiyansmartenforce.Utils.StringUtils;
import com.kas.clientservice.haiyansmartenforce.Utils.ToastUtils;
import com.kas.clientservice.haiyansmartenforce.Utils.Utils;
import com.kas.clientservice.haiyansmartenforce.Utils.WaterMaskImageUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.kas.clientservice.haiyansmartenforce.Utils.Utils.saveImageToLocal;

public class CaseCommitActivity extends BaseActivity implements View.OnClickListener, IllegalParkingCommitImgRvAdapter.OnImageAddClickListener, IllegalParkingCommitImgRvAdapter.OnImagelickListener, TakePhoto.TakeResultListener, IllegalParkingCommitImgRvAdapter.OnImageLongClickListener, IllegalParkingCommitImgRvAdapter.OnImgDeleteClickListener {
    @BindView(R.id.tv_header_title)
    TextView tv_title;
    @BindView(R.id.iv_heaer_back)
    ImageView iv_back;
    @BindView(R.id.rl_caseType)
    RelativeLayout rl_caseType;
    @BindView(R.id.tv_typeCommit_type)
    TextView tv_type;
    @BindView(R.id.rv_case_commit)
    RecyclerView recyclerView;
    @BindView(R.id.rl_caseCommit_location)
    RelativeLayout rl_location;
    @BindView(R.id.tv_caseCommit_location)
    TextView tv_location;
    @BindView(R.id.et_caseCommit_address)
    EditText et_address;
    @BindView(R.id.tv_case_commit_btn)
    TextView tv_submit;
    @BindView(R.id.et_caseCommit_describe)
    EditText et_decribe;



    List<Bitmap> arr_image;
    IllegalParkingCommitImgRvAdapter adapter;
    TakePhoto takePhoto;
    Uri uri;
    String smallClass = "";
    String langitude = "";
    String latitude = "";
    String bigClass = "";
    String smallCode = "";
    private String time_chose;
    private CropOptions cropOptions;  //裁剪参数
    private CompressConfig compressConfig; //压缩参数
    List<Img> imgList = new ArrayList<>();
    String gridcode = "33042400100101";
    List<String> arr_uri = new ArrayList<>();
    @Override
    protected int getLayoutId() {
        return R.layout.activity_case_commit;
    }

    @Override
    protected String getTAG() {
        return this.toString();
    }

    public TakePhoto getTakePhoto() {
        if (takePhoto == null) {
            takePhoto = new TakePhotoImpl(this, this);
        }
        return takePhoto;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
//        Log.i(TAG, "onActivityResult: "+data.toString());
        Log.i(TAG, "onActivityResult: " + requestCode + "  " + resultCode);
        if (requestCode == Constants.RESULTCODE_TIANDITU) {
            if (data!=null) {

                langitude = data.getStringExtra("Longitude");
                latitude = data.getStringExtra("Latitude");
                Log.i(TAG, "onActivityResult: " + langitude + "  " + latitude);
                tv_location.setText(langitude + "," + latitude);
            }
        }
        if (requestCode == Constants.RESULTCODE_CASE_TYPE) {
            if (data != null) {

                if (data.getStringExtra("TypeName") != null) {
                    Log.i(TAG, "onActivityResult: " + data.getStringExtra("TypeName") + "  " + data.getStringExtra("BigClass"));
                    tv_type.setText(data.getStringExtra("TypeName"));
                    smallClass = data.getStringExtra("SmallClass");
                    bigClass = data.getStringExtra("BigClass");
                    smallCode = data.getStringExtra("SmallCode");
                }
            }
        }
    }


    @Override
    protected void initResAndListener() {
        super.initResAndListener();

        tv_title.setText("问题上报");
        iv_back.setOnClickListener(this);
        rl_caseType.setOnClickListener(this);
        rl_location.setOnClickListener(this);
        tv_submit.setOnClickListener(this);

        arr_image = new ArrayList<>();
        adapter = new IllegalParkingCommitImgRvAdapter(arr_image, mContext);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 2, LinearLayout.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setOnImageAddClickListener(this);
        adapter.setOnImagelickListener(this);
        adapter.setOnImageLongClickListener(this);
        adapter.setOnImgDeleteClickListener(this);
        setRecyclerViewHeight(arr_image.size());
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_heaer_back:
                finish();
                break;
            case R.id.rl_caseType:
                startActivityForResult(new Intent(mContext, CaseTypeActivity.class), Constants.RESULTCODE_CASE_TYPE);
                break;
            case R.id.rl_caseCommit_location:
                startActivityForResult(new Intent(mContext, TiandiMapActivity.class), Constants.RESULTCODE_TIANDITU);
                break;
            case R.id.tv_case_commit_btn:
                if (bigClass.equals("")) {
                    ToastUtils.showToast(this, "请选择类型");
                    return;
                }
                if (TextUtils.isEmpty(tv_location.getText().toString())) {
                    ToastUtils.showToast(this, "请选择位置");
                    return;
                }
                if (TextUtils.isEmpty(et_address.getText().toString())) {
                    ToastUtils.showToast(this, "请输入地址");
                    return;
                }
                if (TextUtils.isEmpty(et_decribe.getText().toString())) {
                    ToastUtils.showToast(this, "请输入问题描述");
                    return;
                }
                if (arr_image.size() == 0) {
                    ToastUtils.showToast(this, "照片不能为空");
                    return;
                }
//                getImgUrl();
                uploadImg(BitmapToBase64.bitmapListToBase64(arr_image));
//                showLoadingDialog();
        }
    }

    public void setRecyclerViewHeight(int size) {
        int height = ((size / 2) + 1) * 140 + 30;
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Dp2pxUtil.dip2px(mContext, height));
        layoutParams.setMargins(0, Dp2pxUtil.dip2px(mContext, 5), 0, Dp2pxUtil.dip2px(mContext, 50));
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(layoutParams));
    }

    @Override
    public void onImageClick(int p) {
//        Bitmap bmp = arr_image.get(p);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] bytes = baos.toByteArray();
//
////        Bundle b = new Bundle();
////        b.putByteArray("bitmap", bytes);
//        Intent intent = new Intent(mContext, ImageActivity.class);
//        intent.putExtra("image", bytes);
//        startActivity(intent);
        Intent intent = new Intent(mContext, ImageActivity.class);
        Log.i(TAG, "onImageClick: "+arr_uri.get(p));
        intent.putExtra("uri", String.valueOf(arr_uri.get(p)));
        startActivity(intent);
    }

    @Override
    public void takeSuccess(String imagePath) {
        Log.i(TAG, "takeSuccess: " + imagePath);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);//filePath

        Bitmap water_bitmap = WaterMaskImageUtil.drawTextToRightBottom(mContext, bmp, getTime(), 6, getResources().getColor(R.color.orange), 5, 5);
        Log.i(TAG, "takeSuccess: length=" + water_bitmap.getByteCount() / 1024);
        arr_image.add(water_bitmap);
        arr_uri.add(saveImageToLocal(water_bitmap, mContext));
        setRecyclerViewHeight(arr_image.size());
        adapter.notifyDataSetChanged();
    }

    String strImgUrl = "";
    String imgurl;

    private void uploadImg(String img) {
//        RequestBody requestFile =
//                RequestBody.create(MediaType.parse("multipart/form-data"), img);
        RetrofitClient.createService(ZhuanXiangZhengZhiAPI.class, RequestUrl.baseUrl_leader)
                .httpZXZZimg(img,"-1","citizen")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new MySubscriber<ZhuanXiangZhengZhiAPI.UploadImgEntity>(mContext) {
                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable responeThrowable) {
                        Log.i(TAG, "onError: " + responeThrowable.toString());
                        showNetErrorToast();
                    }

                    @Override
                    public void onNext(ZhuanXiangZhengZhiAPI.UploadImgEntity s) {
                        Log.i(TAG, "onNext: "+gson.toJson(s));
                        String[] split = StringUtils.split(s.getKS().get(0), "|");
                        for (int i = 0; i < split.length; i++) {
                            String string = split[i];
                            String Icon = RequestUrl.baseUrl_img + string;
                            Img img2 = new Img(Icon);
                            imgList.add(img2);
                        }
                        for (int i = 0; i < imgList.size(); i++) {
                            String img = imgList.get(i).getImg();
                            imgurl = img + "|";
                            strImgUrl += imgurl;
                        }
                        upload(strImgUrl.substring(0, strImgUrl.length() - 1));
//                        Collections.addAll(list_img,s.getKS().get(0).split("\\|"));
//                        if (list_img.size()>0) {
//                            commit();
//                        }
                    }
                });

    }

    private void getImgUrl() {
        Log.i(TAG, "getImgUrl: " + BitmapToBase64.bitmapListToBase64(arr_image));
        showLoadingDialog();
        OkHttpUtils.post().url(RequestUrl.URL)
                .addParams("optionName", RequestUrl.getImgUrl)
                .addParams("img", BitmapToBase64.bitmapListToBase64(arr_image))
                .build().execute(new StringCallback() {

                                     @Override
                                     public void onError(Call call, Exception e, int id) {
                                         Log.i(TAG, "onError: " + e.toString());
                                         dismissLoadingDialog();
                                     }

                                     @Override
                                     public void onResponse(final String response, int id) {
                                         Log.i(TAG, "onResponse: " + response);
                                         try {
                                             JSONObject object = new JSONObject(response);
                                             boolean State = object.getBoolean("State");
                                             if (State) {
                                                 String Rtn = object.getString("Rtn");
                                                 String[] split = StringUtils.split(Rtn, "|");
                                                 for (int i = 0; i < split.length; i++) {
                                                     String string = split[i];
                                                     String Icon = RequestUrl.IMGURL + string;
                                                     Img img2 = new Img(Icon);
                                                     imgList.add(img2);
                                                 }
                                                 for (int i = 0; i < imgList.size(); i++) {
                                                     String img = imgList.get(i).getImg();
                                                     imgurl = img + "|";
                                                     strImgUrl += imgurl;
                                                 }
                                                 upload(strImgUrl.substring(0, strImgUrl.length() - 1));
                                             }
                                         } catch (Exception e) {
                                             e.printStackTrace();
                                             Log.i(TAG, "onResponse: " + e.toString());
                                         }
                                         dismissLoadingDialog();
                                     }
                                 }


        );
    }

    private void upload(String substring) {
//        this.address = address;
//        this.bigClass = bigClass;
//        this.descript = descript;
//        this.fid = fid;
//        this.gridcode = gridcode;
//        this.picurls = picurls;
//        this.smallClass = smallClass;
//        this.typecode = typecode;
//        this.userid = userid;
//            String address = new String(et_address.getText().toString().getBytes("utf-8"),"gb2312");
//            String des = new String(et_decribe.getText().toString().getBytes("utf-8"),"gb2312");
        Log.i(TAG, "upload: "+substring);
        RetrofitClient.createService(CaseCommit.class,"http://117.149.146.131:86/")
                .httpCaseCommit("zmninsertproject",
                        "1",
                        UserSingleton.USERINFO.getPublicUsersID(),
                        bigClass,
                        smallCode,
                        gridcode+"",
                        et_address.getText().toString(),
                        et_decribe.getText().toString(),
                        tv_location.getText().toString(),
                        substring,
                        "application/x-www-form-urlencoded; charset=UTF-8")
//                .httpCaseCommit(parseBodyToJson(caseCommitEntity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new MySubscriber<String>(mContext) {
                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable responeThrowable) {

                    }

                    @Override
                    public void onNext(String s) {
                        Log.i(TAG, "onResponse: " + s);
                        if (s.contains("成功")) {
                            ToastUtils.showToast(mContext,"提交成功");
                            finish();
                        }else {
                            ToastUtils.showToast(mContext,"提交失败");
                        }
                    }
                });
//        OkHttpUtils.post().url(RequestUrl.baseUrl)
//                .addParams("optionName", "zmninsertproject")
//                .addParams("typecode", "1")
//                .addParams("userid", UserSingleton.USERINFO.getZFRYID())
//                .addParams("bigClass", bigClass)
//                .addParams("smallClass", smallCode)
//                .addParams("gridcode", gridcode + "")
//                .addParams("address", et_address.getText().toString())
//                .addParams("descript", et_decribe.getText().toString())
//                .addParams("fid", tv_location.getText().toString())
//                .addParams("picurls", substring)
////                .addParams("ePicArry", "")
////                .addParams("addType", "02")
//                .build().execute(new StringCallback() {
//            @Override
//            public void onError(Call call, Exception e, int id) {
//                Log.i(TAG, "onError: " + e.toString());
//                dismissLoadingDialog();
//            }
//
//            @Override
//            public void onResponse(final String response, int id) {
//                Log.i(TAG, "onResponse: " + response);
//                dismissLoadingDialog();
//                if (response.contains("成功")) {
//                    ToastUtils.showToast(mContext,"提交成功");
//                    finish();
//                }else {
//                    ToastUtils.showToast(mContext,"提交失败");
//                }
//
//            }
//
//
//
//        });
    }


    @Override
    public void takeFail(String msg) {
        ToastUtils.showToast(mContext, "拍摄失败");
    }

    @Override
    public void takeCancel() {
        ToastUtils.showToast(mContext, "取消拍摄");
    }

    @Override
    public void onImageAddClick() {
        Log.i(TAG, "onImageAddClick: ");
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    100);
        } else {
            //查看图片权限
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //申请权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        101);
            } else {
                uri = Utils.getImageCropUri();
//                cropOptions = new CropOptions.Builder().setAspectX(1).setAspectY(1).setWithOwnCrop(false).create();
                //设置压缩参数
                compressConfig = new CompressConfig.Builder().setMaxSize(Constants.PIC_MAXSIZE * 1024).setMaxPixel(Constants.COMPRESSRATE).create();
                takePhoto.onEnableCompress(compressConfig, true); //设置为需要压缩
                takePhoto.onPickFromCapture(uri);
            }
        }
    }


    @Override
    public void onImgLongClick() {

    }

    @Override
    public void onImgDeleteClick(int position) {
        arr_image.remove(position);
        arr_uri.remove(position);
        adapter.notifyDataSetChanged();
    }
}
