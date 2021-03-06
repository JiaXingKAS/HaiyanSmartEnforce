package com.kas.clientservice.haiyansmartenforce.Module.IllegalParking;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.alertview.AlertView;
import com.google.gson.Gson;
import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.kas.clientservice.haiyansmartenforce.API.PaperNumAPI;
import com.kas.clientservice.haiyansmartenforce.Base.BaseActivity;
import com.kas.clientservice.haiyansmartenforce.Base.BaseEntity;
import com.kas.clientservice.haiyansmartenforce.Entity.CarNumRecgnizeEntity;
import com.kas.clientservice.haiyansmartenforce.Http.ExceptionHandle;
import com.kas.clientservice.haiyansmartenforce.Http.MySubscriber;
import com.kas.clientservice.haiyansmartenforce.Http.RetrofitClient;
import com.kas.clientservice.haiyansmartenforce.Module.RoadSearch.RoadSearchActivity;
import com.kas.clientservice.haiyansmartenforce.Module.TianDiTu.GeoBean;
import com.kas.clientservice.haiyansmartenforce.Module.TianDiTu.GeoUtils;
import com.kas.clientservice.haiyansmartenforce.Module.TianDiTu.TiandiMapActivity;
import com.kas.clientservice.haiyansmartenforce.R;
import com.kas.clientservice.haiyansmartenforce.Utils.BitmapToBase64;
import com.kas.clientservice.haiyansmartenforce.Utils.Constants;
import com.kas.clientservice.haiyansmartenforce.Utils.Dp2pxUtil;
import com.kas.clientservice.haiyansmartenforce.Utils.NetUtils;
import com.kas.clientservice.haiyansmartenforce.Utils.SPUtils;
import com.kas.clientservice.haiyansmartenforce.Utils.TimePickerDialog;
import com.kas.clientservice.haiyansmartenforce.Utils.TimePickerDialog.TimePickerDialogInterface;
import com.kas.clientservice.haiyansmartenforce.Utils.TimeUtils;
import com.kas.clientservice.haiyansmartenforce.Utils.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import net.posprinter.utils.DataForSendToPrinterPos58;
import net.xprinter.service.XprinterService;
import net.xprinter.xpinterface.IMyBinder;
import net.xprinter.xpinterface.ProcessData;
import net.xprinter.xpinterface.UiExecute;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import okhttp3.Call;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import smartenforce.base.HttpApi;
import smartenforce.projectutil.OcrUtil;
import smartenforce.util.ToastUtil;
import smartenforce.widget.ProgressDialogUtil;

public class IllegalParkingCommitActivity extends BaseActivity implements IllegalParkingCommitImgRvAdapter.OnImageAddClickListener, IllegalParkingCommitImgRvAdapter.OnImagelickListener, TakePhoto.TakeResultListener, View.OnClickListener,TimePickerDialogInterface {
    @BindView(R.id.iv_heaer_back)
    ImageView iv_back;
    @BindView(R.id.sp_province)
    Spinner sp_province;
    @BindView(R.id.sp_ABC)
    Spinner sp_A2Z;
    @BindView(R.id.et_illegalparkingcommit_num)
    EditText et_num;
    @BindView(R.id.rv_illegalParkingCommit)
    RecyclerView recyclerView;
    @BindView(R.id.tv_header_title)
    TextView tv_title;
    @BindView(R.id.tv_illegalParking_time)
    TextView tv_time;
    @BindView(R.id.iv_illefalParking_location)
    ImageView iv_location;
    @BindView(R.id.et_illegalparkingcommit_position)
    TextView et_positon;
    @BindView(R.id.tv_illegalParking_code)
    TextView tv_code;
    @BindView(R.id.tv_commitIllegalParking_print)
    TextView tv_commitIllegalParking_print;
    @BindView(R.id.tv_commitIllegalParking_next)
    TextView tv_next;
    @BindView(R.id.iv_illegalParking_choseTime)
    ImageView iv_choseTime;
    @BindView(R.id.iv_commitParking_takePhoto)
    ImageView iv_takePhoto;
    @BindView(R.id.tv_bottom)
    TextView tv_bottom;


    @BindView(R.id.tev_road)
    TextView tev_road;

    @BindView(R.id.tev_cplb_c)
    TextView tev_cplb_c;
    @BindView(R.id.tev_color)
    TextView tev_color;
    @BindView(R.id.tev_cllx_c)
    TextView tev_cllx_c;

    String[] arr_province;
    String[] arr_abc;


    boolean isServiceConnected =false;
    List<Bitmap> arr_image;
    IllegalParkingCommitImgRvAdapter adapter;
    TakePhoto takePhoto;

    private CompressConfig compressConfig; //压缩参数
    String[] list_province;
    String[] list_A2Z;
    String province = "浙";
    String A2Z = "F";
    Uri uri;
    boolean isNumGot = false;
    private String roadId;

    IMyBinder binder;
    ServiceConnection conn;
    BluetoothDevice device;
    TimePickerDialog timePickerDialog;
    String roadName = "";

    private String[] array_cplx,array_color,array_car;
    private AlertView alertView_cplx,alertView_color,alertView_car;
    List<CPLXBean.RtnBean> list=new ArrayList<>();
    List<CarColorBean.RtnBean> colorList=new ArrayList<>();
    List<CarTypeBean.RtnBean> carList=new ArrayList<>();
    private GeoBean geoBean = new GeoBean(null);
    @Override
    protected int getLayoutId() {
        return R.layout.activity_illegal_parking_commit;
    }

    @Override
    protected String getTAG() {
        return "IllegalParkingCommit";
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
        Log.i(TAG, "onActivityResult: " + requestCode + "  " + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.RESULTCODE_TIANDITU) {
                if (data != null) {

                    geoBean = (GeoBean) data.getSerializableExtra("GeoBean");
                    et_positon.setText(geoBean.address);
                }

            }
            if (requestCode == Constants.RESULTCODE_ROAD) {
                if (data != null) {

                    roadId = data.getStringExtra("RoadId");
                    roadName = data.getStringExtra("Road");
                    SPUtils.putCommit(mContext, "Road", data.getStringExtra("Road"));
                    SPUtils.putCommit(mContext, "RoadId", data.getStringExtra("RoadId"));
                    tev_road.setText(roadName);
                }
            }
        }
    }

    @Override
    protected void initResAndListener() {
        super.initResAndListener();

        tv_title.setText("违章停车");
        tv_time.setText(TimeUtils.getFormedTime("yyyy-MM-dd HH:mm"));
        takePhoto = new TakePhotoImpl(this, this);
        arr_province = getResources().getStringArray(R.array.provinceName);
        arr_abc = getResources().getStringArray(R.array.A2Z);


        roadName = (String) SPUtils.get(mContext,"Road","");
        roadId = (String) SPUtils.get(mContext,"RoadId","");
        tev_road.setText(roadName);

        list_province = getResources().getStringArray(R.array.provinceName);
        list_A2Z = getResources().getStringArray(R.array.A2Z);

        sp_province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                province = list_province[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sp_A2Z.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "onItemSelected: " + list_A2Z[i]);
                Log.i(TAG, "onItemSelected: position=" + i);
                A2Z = list_A2Z[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //设置自动大小写转换
        et_num.setTransformationMethod(new UpperCaseTransform());
        iv_back.setOnClickListener(this);
        iv_choseTime.setOnClickListener(this);
        iv_location.setOnClickListener(this);
        tv_commitIllegalParking_print.setOnClickListener(this);
        tv_next.setOnClickListener(this);
        iv_takePhoto.setOnClickListener(this);
        et_positon.setOnClickListener(this);
        tv_code.setOnClickListener(this);
        tv_time.setOnClickListener(this);
        tev_road.setOnClickListener(this);

        tev_cplb_c.setOnClickListener(this);
        tev_color.setOnClickListener(this);
        tev_cllx_c.setOnClickListener(this);
        searchCPLX();
        searchColor();
        searchCarType();
        arr_image = new ArrayList<>();
        adapter = new IllegalParkingCommitImgRvAdapter(arr_image, mContext);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 2, LinearLayout.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setOnImageAddClickListener(this);
        adapter.setOnImagelickListener(this);
        setRecyclerViewHeight(arr_image.size());
        recyclerView.setAdapter(adapter);
        timePickerDialog = new TimePickerDialog(mContext);
        loadPaperNum();


        GeoUtils.getInstance().startLocation(this, new GeoUtils.onLocationSuccessCallback() {
            @Override
            public void onSuccess(GeoBean geo) {
                geoBean = geo;
                et_positon.setText(geoBean.address);
            }
        });
    }


    private void loadPaperNum() {
        RetrofitClient.createService(PaperNumAPI.class)
                .httpGetPaperNum()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MySubscriber<BaseEntity<PaperNumAPI.EntityBean>>(mContext) {
                    @Override
                    public void onError(ExceptionHandle.ResponeThrowable responeThrowable) {
                        Log.i(TAG, "onError: " + responeThrowable);
                        ToastUtil.show(mContext,"抄告单获取失败，请点击抄告单号重新获取");
                        isNumGot = false;
                    }

                    @Override
                    public void onNext(BaseEntity<PaperNumAPI.EntityBean> entityBeanBaseEntity) {
                        if (entityBeanBaseEntity.isState()) {
                            tv_code.setText(entityBeanBaseEntity.getRtn().getJdsnum());
                            isNumGot = true;
                        } else {
                            ToastUtil.show(mContext,"抄告单获取失败，请点击抄告单号重新获取");
                            isNumGot = false;
                        }
                    }
                });
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
                uri = getImageCropUri();
//                cropOptions = new CropOptions.Builder().setAspectX(1).setAspectY(1).setWithOwnCrop(false).create();
                //设置压缩参数
//                compressConfig = new CompressConfig.Builder().setMaxSize(50 * 1024).setMaxPixel(400).create();
//                takePhoto.onEnableCompress(compressConfig, true); //设置为需要压缩
                takePhoto.onPickFromCapture(uri);
            }
        }

    }

    private Uri getImageCropUri() {
        File file = new File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        return Uri.fromFile(file);
    }

    @Override
    public void onImageClick(int p) {
        Bitmap bmp = arr_image.get(p);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);//TODO PNG JPEG
        byte[] bytes = baos.toByteArray();

        Intent intent = new Intent(mContext, ImageActivity.class);
        intent.putExtra("image", bytes);
        startActivity(intent);


    }

    @Override
    public void takeSuccess(String imagePath) {
        Log.i(TAG, "takeSuccess: " + imagePath);
        Bitmap bmp = BitmapFactory.decodeFile(imagePath);//filePath

        carNumRecognize(BitmapToBase64.bitmapToBase64(bmp));
    }

    @Override
    public void takeFail(String msg) {
        ToastUtils.showToast(mContext, "拍摄失败");
    }

    @Override
    public void takeCancel() {
        ToastUtils.showToast(mContext, "取消拍摄");
    }

    public void setRecyclerViewHeight(int size) {
        int height = ((size / 2) + 1) * 140 + 30;
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Dp2pxUtil.dip2px(mContext, height));
        layoutParams.setMargins(0, Dp2pxUtil.dip2px(mContext, 5), 0, Dp2pxUtil.dip2px(mContext, 50));
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(layoutParams));
    }

    public void carNumRecognize(String img) {
        OcrUtil.getInstance().getCarNumberInfo(this, img, new OcrUtil.onCarNumberCallBack() {
            @Override
            public void onSuccess(String carNumber) {
                et_num.setText(carNumber.substring(2));
                String prov = String.valueOf(carNumber.charAt(0));
                String a2z = String.valueOf(carNumber.charAt(1));

                for (int i = 0; i < list_province.length; i++) {
                    if (prov.equals(list_province[i])) {
                        sp_province.setSelection(i);
                        province = prov;
                        break;
                    }
                }
                for (int i = 0; i < list_A2Z.length; i++) {
                    if (a2z.equals(list_A2Z[i])) {
                        sp_A2Z.setSelection(i);
                        A2Z = a2z;
                        break;
                    }
                }
            }

            @Override
            public void onErroMsg(String msg) {
                ToastUtils.showToast(mContext,msg);
            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_commitIllegalParking_print:
                String carNumber=et_num.getText().toString().trim();
                if (TextUtils.isEmpty(carNumber)){
                    ToastUtils.showToast(mContext, "请输入车牌号");
                }else{
                   if (carNumber.length()==5||carNumber.length()==6){

                       String roadNum=tev_road.getText().toString().trim();
                       if (!TextUtils.isEmpty(roadNum)) {
                           if (isNumGot) {
                               print();
                           }else {
                               ToastUtil.show(mContext,"抄告单获取失败，请点击抄告单号重新获取");
                           }
                       } else {
                           ToastUtils.showToast(mContext, "请输入地点信息");
                       }

                   }
                    else{
                       ToastUtils.showToast(mContext, "车牌号格式错误");
                   }

                }
                break;
            case R.id.iv_heaer_back:
                finish();
                break;
            case R.id.tv_illegalParking_time:
            case R.id.iv_illegalParking_choseTime:
                choseTime();
                break;
            case R.id.iv_illefalParking_location:
                Intent mapIntent = new Intent(this, TiandiMapActivity.class);
                mapIntent.putExtra("GeoBean", geoBean);
                startActivityForResult(mapIntent,   Constants.RESULTCODE_TIANDITU);
                break;
            case R.id.tv_commitIllegalParking_next:

                String carNumber_=et_num.getText().toString().trim();
                if (TextUtils.isEmpty(carNumber_)){
                    ToastUtils.showToast(mContext, "请输入车牌号");
                }else{
                    if (carNumber_.length()==5||carNumber_.length()==6){
                        String roadNum=tev_road.getText().toString().trim();
                        if (!TextUtils.isEmpty(roadNum)) {
                            if (isNumGot) {

                                String JDSnum=getTextValue(tv_code);
                                Intent intent = new Intent(mContext, IllegalParkingTakePhotoActivity.class);
                                Log.i(TAG, "onNext: " + tv_time.getText().toString());
                                intent.putExtra("Time", tv_time.getText().toString());
                                intent.putExtra("Position", tev_road.getText().toString());
                                intent.putExtra("CarNum", province + A2Z + et_num.getText().toString());
                                intent.putExtra("Code", tv_code.getText().toString());
                                intent.putExtra("RoadId",roadId);
                                intent.putExtra("JDSnum",JDSnum);
                                intent.putExtra("CarPlateTypeCPT",getTextValue(tev_cplb_c));
                                intent.putExtra("CarBodyColorCBC",getTextValue(tev_color));
                                intent.putExtra("CarTypeCT",getTextValue(tev_cllx_c));
                                startActivity(intent);
                            }else {
                                ToastUtil.show(mContext,"抄告单获取失败，请点击抄告单号重新获取");
                            }
                        } else {
                            ToastUtils.showToast(mContext, "请输入地点信息");
                        }

                    }
                    else{
                        ToastUtils.showToast(mContext, "车牌号格式错误");
                    }

                }
                break;
            case R.id.iv_commitParking_takePhoto:
                takePhoto();
                break;
            case R.id.tev_road:

                startActivityForResult(new Intent(mContext, RoadSearchActivity.class), Constants.RESULTCODE_ROAD);
                break;
            case R.id.tv_illegalParking_code:
                if (!isNumGot) {

                    loadPaperNum();
                }
                break;
            case R.id.tev_cplb_c:
                if (alertView_cplx == null) {
                    alertView_cplx = getShowAlert("选择车牌类别", array_cplx, tev_cplb_c);
                }
                alertView_cplx.show();

                break;
            case R.id.tev_color:
                if (alertView_color == null) {
                    alertView_color = getShowAlert("选择车身颜色", array_color, tev_color);
                }
                alertView_color.show();
                break;
            case R.id.tev_cllx_c:
                if (alertView_car == null) {
                    alertView_car = getShowAlert("选择车身颜色", array_car, tev_cllx_c);
                }
                alertView_car.show();
                break;
            default:
                break;
        }
    }

    private void searchCPLX() {
        OkHttpUtils.post().url(HttpApi.URL_CPLX).addParams("carprop","1").build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,e.toString());
                showToast("网络发生错误");
            }

            @Override
            public void onResponse(String response, int id) {
                list.clear();
                CPLXBean bean=new Gson().fromJson(response,CPLXBean.class);
                if (bean.State){
                    list.addAll(bean.Rtn);
                    array_cplx = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        array_cplx[i] = list.get(i).CarPlateTypeCPT;
                        
                    }
                    tev_cplb_c.setText(list.get(id).CarPlateTypeCPT);
                }
            }
        });
    }
    private void searchColor() {
        OkHttpUtils.post().url(HttpApi.URL_CPLX).addParams("carprop","2").build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,e.toString());
                showToast("网络发生错误");
            }

            @Override
            public void onResponse(String response, int id) {
                colorList.clear();
                CarColorBean bean=new Gson().fromJson(response,CarColorBean.class);
                if (bean.State){
                    colorList.addAll(bean.Rtn);
                    array_color = new String[colorList.size()];
                    for (int i = 0; i < colorList.size(); i++) {
                        array_color[i] = colorList.get(i).CarBodyColorCBC;
                    }
                    tev_color.setText(colorList.get(id).CarBodyColorCBC);
                }
            }
        });
    }



    private void searchCarType() {
        OkHttpUtils.post().url(HttpApi.URL_CPLX).addParams("carprop","3").build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(TAG,e.toString());
                showToast("网络发生错误");
            }

            @Override
            public void onResponse(String response, int id) {
                carList.clear();
                CarTypeBean bean=new Gson().fromJson(response,CarTypeBean.class);
                if (bean.State){
                    carList.addAll(bean.Rtn);
                    array_car = new String[carList.size()];
                    for (int i = 0; i < carList.size(); i++) {
                        array_car[i] = carList.get(i).CarTypeCT;

                    }
                    tev_cllx_c.setText(carList.get(id).CarTypeCT);
                }
            }
        });
    }
    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    100);
        } else {
//            url("http://jisucpsb.market.alicloudapi.com/licenseplaterecognition/recognize")
//                    .addHeader("X-Ca-Key", "24553193")
//                    .addHeader("Authorization", "APPCODE 2e476d97d6994a489afb3491b44a2578")
            uri = getImageCropUri();
//                cropOptions = new CropOptions.Builder().setAspectX(1).setAspectY(1).setWithOwnCrop(false).create();
            //设置压缩参数
            compressConfig = new CompressConfig.Builder().setMaxSize(Constants.PIC_MAXSIZE * 1024).setMaxPixel(Constants.COMPRESSRATE).create();
            takePhoto.onEnableCompress(compressConfig, true); //设置为需要压缩
            takePhoto.onPickFromCapture(uri);

        }
    }

    private void print() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //申请权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    101);
        } else {
            if (!isServiceConnected) {

                Log.i(TAG, "print: ");
                conn = new ServiceConnection() {

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        // TODO Auto-generated method stub
                        Log.i(TAG, "onServiceDisconnected: ");
                        isServiceConnected = false;
                    }

                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        // TODO Auto-generated method stub
                        //绑定成功
                        isServiceConnected = true;
                        Log.i(TAG, "onServiceConnected: ");
                        binder = (IMyBinder) service;
                        Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
                        if (devices.size() > 0) {
                            for (Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext(); ) {
                                BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                                Log.i(TAG, "设备：" + bluetoothDevice.getName() + " " + bluetoothDevice.getAddress());
                                if (bluetoothDevice.getName().trim().equals("printer001")) {
                                    device = bluetoothDevice;
                                }
                            }
                        }
                        if (device != null) {
                            binder.connectBtPort(device.getAddress().trim(), new UiExecute() {
                                @Override
                                public void onsucess() {
                                    Log.i(TAG, "打印机连接成功: ");
                                    isServiceConnected = true;
                                    tv_commitIllegalParking_print.setClickable(false);
                                    binder.acceptdatafromprinter(new UiExecute() {

                                        @Override
                                        public void onsucess() {
                                            Log.i(TAG, "acceptdatafromprinter onsucess: ");

                                        }

                                        @Override
                                        public void onfailed() {
                                            isServiceConnected = false;
                                            tv_commitIllegalParking_print.setClickable(true);
                                            Toast.makeText(getApplicationContext(), "连接已断开", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    doPrintWork();
                                }

                                @Override
                                public void onfailed() {
                                    Log.i(TAG, "打印机连接失败: ");
                                    isServiceConnected = false;
                                    ToastUtils.showToast(mContext, "打印机连接失败");
                                }
                            });
                        } else {
                            isServiceConnected=false;
                            ToastUtils.showToast(mContext, "请先连接名为“Printer001”匹配码为“0000”的打印机，再进行操作！");
                        }
                    }
                };
                Intent intent = new Intent(mContext, XprinterService.class);
                bindService(intent, conn, BIND_AUTO_CREATE);

            } else {
                doPrintWork();
            }

            //蓝牙
//            startBlueScan();
        }
    }

    public void doPrintWork() {

        binder.writeDataByYouself(new UiExecute() {

            @Override
            public void onsucess() {
                ProgressDialogUtil.show(IllegalParkingCommitActivity.this,"正在打印中...");
                printHander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        printHander.obtainMessage(2,"打印成功").sendToTarget();
                    }
                },3000);

            }

            @Override
            public void onfailed() {
                isServiceConnected = false;
                printHander.obtainMessage(2,"打印失败，请重新打印").sendToTarget();



            }
        }, new ProcessData() {//第二个参数是ProcessData接口的实现
            //这个接口的重写processDataBeforeSend这个处理你要发送的指令
            @Override
            public List<byte[]> processDataBeforeSend() {
                //初始化一个list

                return formAndPrint();
            }
        });


    }

    private Handler printHander=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String infoMsg= (String) msg.obj;
            ProgressDialogUtil.hide();
            tv_commitIllegalParking_print.setClickable(true);
            showToast(infoMsg);
        }
    };

    public static byte[] strTobytes(String str) {
        byte[] b = null, data = null;
        try {
            b = str.getBytes("utf-8");
            data = new String(b, "utf-8").getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;
    }

    public List<byte[]> formAndPrint() {
        ArrayList<byte[]> list = new ArrayList<byte[]>();
        list.add(DataForSendToPrinterPos58.initializePrinter());
        list.add(DataForSendToPrinterPos58.printAndFeedForward(3));
        //标题
        byte[] title = strTobytes("道路交通安全违法行为处理通知书");
        //居中
        list.add(DataForSendToPrinterPos58.selectAlignment(1));
        list.add(DataForSendToPrinterPos58.selectCharacterSize(1));
        list.add(DataForSendToPrinterPos58.selectChineseCharModel());
        list.add(title);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        //空一行
        list.add(DataForSendToPrinterPos58.printAndFeedForward(1));

        //编号
        list.add(DataForSendToPrinterPos58.initializePrinter());
        byte[] num = strTobytes("编号：" + tv_code.getText().toString());
        list.add(DataForSendToPrinterPos58.selectAlignment(1));
        list.add(num);
//                list.add(DataForSendToPrinterPos58.selectChineseCharModel());
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        //空一行
        list.add(DataForSendToPrinterPos58.printAndFeedForward(2));

        list.add(DataForSendToPrinterPos58.selectAlignment(0));
        //省
        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(1));
        list.add(DataForSendToPrinterPos58.selectChineseCharModel());
        byte[] data_province = strTobytes(province);
        list.add(data_province);
        //车牌
        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(1));
        Log.i(TAG, "formAndPrint: " + A2Z);
        byte[] data_carNum = strTobytes(A2Z + "" + et_num.getText().toString());
        list.add(data_carNum);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(0));
        byte[] data_1 = strTobytes("号牌号码的机动车驾驶人：");
        list.add(data_1);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        list.add(DataForSendToPrinterPos58.printAndFeedForward(1));
        byte[] data_content = strTobytes(String.format("    该车辆于%s，在", tv_time.getText().toString()));
        list.add(data_content);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(1));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(1));
        list.add(DataForSendToPrinterPos58.selectChineseCharModel());
        byte[] data_Position = strTobytes(tev_road.getText().toString());
        list.add(data_Position);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(0));
        byte[] data_content2 = strTobytes("实施的停车行为违反了《道路交通安全法》第56条之规定。请您持本通知书，驾驶证，行驶证在15日内，到");
        list.add(data_content2);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(1));
        byte[] data_handlePosition = strTobytes("交通警察服务中心");
        list.add(data_handlePosition);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(0));
        byte[] data_2 = strTobytes("接受处理。");
        list.add(data_2);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());

        list.add(DataForSendToPrinterPos58.printAndFeedForward(2));

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        byte[] data_dizhi = strTobytes("地址：");
        list.add(data_dizhi);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(1));
        byte[] data_p = strTobytes("海盐县武原街道出海路与公园路交界口");
        list.add(data_p);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        list.add(DataForSendToPrinterPos58.printAndFeedForward(1));

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        byte[] data_dianhua = strTobytes("咨询电话：");
        list.add(data_dianhua);

//        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(1));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(1));
        byte[] data_call = strTobytes("0573-86198731");
        list.add(data_call);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        list.add(DataForSendToPrinterPos58.printAndFeedForward(1));


        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(0));
        byte[] beizhu = strTobytes("备注：机动车所有人登记的住所地址或联系电话发生变化的，请及时向登记地车管所申请变更备案。");
        list.add(beizhu);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());

        list.add(DataForSendToPrinterPos58.printAndFeedForward(5));

        list.add(DataForSendToPrinterPos58.selectAlignment(1));
        byte[] type = strTobytes("第一联：车主联");
        list.add(type);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(0));
        list.add(DataForSendToPrinterPos58.selectChineseCharModel());
        byte[] line = strTobytes("--------------------------------");
        list.add(line);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());

        printAgain(list);

        return list;
    }

    private void printAgain(ArrayList<byte[]> list) {
        list.add(DataForSendToPrinterPos58.initializePrinter());
        list.add(DataForSendToPrinterPos58.printAndFeedForward(3));
        //标题
        byte[] title = strTobytes("道路交通安全违法行为处理通知书");
        //居中
        list.add(DataForSendToPrinterPos58.selectAlignment(1));
        list.add(DataForSendToPrinterPos58.selectCharacterSize(1));
        list.add(DataForSendToPrinterPos58.selectChineseCharModel());
        list.add(title);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        //空一行
        list.add(DataForSendToPrinterPos58.printAndFeedForward(1));

        //编号
        list.add(DataForSendToPrinterPos58.initializePrinter());
        byte[] num = strTobytes("编号：" + tv_code.getText().toString());
        list.add(DataForSendToPrinterPos58.selectAlignment(1));
        list.add(num);
//                list.add(DataForSendToPrinterPos58.selectChineseCharModel());
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        //空一行
        list.add(DataForSendToPrinterPos58.printAndFeedForward(2));

        list.add(DataForSendToPrinterPos58.selectAlignment(0));
        //省
        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(1));
        list.add(DataForSendToPrinterPos58.selectChineseCharModel());
        byte[] data_province = strTobytes(province);
        list.add(data_province);
        //车牌
        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(1));
        Log.i(TAG, "formAndPrint: " + A2Z);
        byte[] data_carNum = strTobytes(A2Z + "" + et_num.getText().toString());
        list.add(data_carNum);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(0));
        byte[] data_1 = strTobytes("号牌号码的机动车驾驶人：");
        list.add(data_1);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        list.add(DataForSendToPrinterPos58.printAndFeedForward(1));
        byte[] data_content = strTobytes(String.format("    该车辆于%s，在", tv_time.getText().toString()));
        list.add(data_content);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(1));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(1));
        list.add(DataForSendToPrinterPos58.selectChineseCharModel());
        byte[] data_Position = strTobytes(tev_road.getText().toString());
        list.add(data_Position);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(0));
        byte[] data_content2 = strTobytes("实施的停车行为违反了《道路交通安全法》第56条之规定。请您持本通知书，驾驶证，行驶证在15日内，到");
        list.add(data_content2);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(1));
        byte[] data_handlePosition = strTobytes("交通警察服务中心");
        list.add(data_handlePosition);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(0));
        byte[] data_2 = strTobytes("接受处理。");
        list.add(data_2);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());

        list.add(DataForSendToPrinterPos58.printAndFeedForward(2));

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        byte[] data_dizhi = strTobytes("地址：");
        list.add(data_dizhi);

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(1));
        byte[] data_p = strTobytes("海盐县武原街道出海路与公园路交界口");
        list.add(data_p);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        list.add(DataForSendToPrinterPos58.printAndFeedForward(1));

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        byte[] data_dianhua = strTobytes("咨询电话：");
        list.add(data_dianhua);

//        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(1));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(1));
        byte[] data_call = strTobytes("0573-86198731");
        list.add(data_call);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        list.add(DataForSendToPrinterPos58.printAndFeedForward(1));


        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(0));
        byte[] beizhu = strTobytes("备注：机动车所有人登记的住所地址或联系电话发生变化的，请及时向登记地车管所申请变更备案。");
        list.add(beizhu);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());
        list.add(DataForSendToPrinterPos58.printAndFeedForward(5));

        list.add(DataForSendToPrinterPos58.selectAlignment(1));
        byte[] type = strTobytes("第二联：存根联");
        list.add(type);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());

        list.add(DataForSendToPrinterPos58.selectOrCancelChineseCharUnderLineModel(0));
        list.add(DataForSendToPrinterPos58.selectOrCancelUnderlineModel(0));
        list.add(DataForSendToPrinterPos58.selectChineseCharModel());
        byte[] line = strTobytes("--------------------------------");
        list.add(line);
        list.add(DataForSendToPrinterPos58.printAndFeedLine());

        list.add(DataForSendToPrinterPos58.printAndFeedForward(2));
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (conn != null) {
            if (isServiceConnected) {
                unbindService(conn);
            }

        }
    }

    private void choseTime() {
        timePickerDialog.showPPw(tv_bottom);
    }

    @Override
    public void positiveListener() {
        String year = timePickerDialog.getYear()+"";
        String months = timePickerDialog.getMonth()+"";
        String day = timePickerDialog.getDay()+"";
        String hour = timePickerDialog.getHour()+"";
        String minutes = timePickerDialog.getMinute()+"";
        tv_time.setText(year+"-"+months+"-"+day+" "+hour+":"+minutes);
    }

    @Override
    public void negativeListener() {

    }


    public class UpperCaseTransform extends ReplacementTransformationMethod {
        @Override
        protected char[] getOriginal() {
            char[] aa = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            return aa;
        }

        @Override
        protected char[] getReplacement() {
            char[] cc = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            return cc;
        }
    }
}
