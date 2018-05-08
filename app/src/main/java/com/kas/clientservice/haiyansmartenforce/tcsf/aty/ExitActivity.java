package com.kas.clientservice.haiyansmartenforce.tcsf.aty;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kas.clientservice.haiyansmartenforce.MyApplication;
import com.kas.clientservice.haiyansmartenforce.R;
import com.kas.clientservice.haiyansmartenforce.tcsf.base.BaseActivity;
import com.kas.clientservice.haiyansmartenforce.tcsf.base.HTTP_HOST;
import com.kas.clientservice.haiyansmartenforce.tcsf.base.NetResultBean;
import com.kas.clientservice.haiyansmartenforce.tcsf.bean.TcListBeanResult;
import com.kas.clientservice.haiyansmartenforce.tcsf.intf.BeanCallBack;
import com.kas.clientservice.haiyansmartenforce.tcsf.util.DateUtil;
import com.kas.clientservice.haiyansmartenforce.tcsf.util.PrintUtil;
import com.kas.clientservice.haiyansmartenforce.tcsf.util.ToastUtil;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;

/**
 * 离开详情页面
 */
public class ExitActivity extends PrintActivity implements View.OnClickListener{
   private TextView tev_cphm,tev_trsj,tev_pwbh,tev_lksj,tev_tcfy;
    private TextView tev_print,tev_submit;
    private ImageView iv_heaer_back;
    private TextView  tv_header_title;

    private String endTime;
    private long cost;
    private String lengthTime;

    private TcListBeanResult bean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exitinging);
        bean= (TcListBeanResult) getIntent().getSerializableExtra("TcListBeanResult");

        tev_cphm= (TextView) findViewById(R.id.tev_cphm);
        tev_trsj= (TextView) findViewById(R.id.tev_trsj);
        tev_pwbh= (TextView) findViewById(R.id.tev_pwbh);
        tev_lksj= (TextView) findViewById(R.id.tev_lksj);
        tev_tcfy= (TextView) findViewById(R.id.tev_tcfy);
        tev_print= (TextView) findViewById(R.id.tev_print);
        tev_submit= (TextView) findViewById(R.id.tev_submit);
        iv_heaer_back = (ImageView) findViewById(R.id.iv_heaer_back);
        tv_header_title = (TextView) findViewById(R.id.tv_header_title);
        tv_header_title.setText("离开收费");
        iv_heaer_back.setOnClickListener(this);
        tev_print.setOnClickListener(this);
        tev_submit.setOnClickListener(this);
        endTime=DateUtil.currentTime();
        cost=DateUtil.calMoney(endTime,bean.starttime);
        lengthTime=DateUtil.getTimeLenth(endTime,bean.starttime);
        tev_cphm.setText(bean.carnum);
        tev_pwbh.setText(bean.Berthname);
        tev_trsj.setText(bean.starttime);
        tev_lksj.setText(endTime);
        tev_tcfy.setText(cost+"元");

    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.iv_heaer_back:
                finish();
                break;
            case R.id.tev_print:
                String[] body = new String[]{"车牌号码：" + bean.carnum, "停入时间：" + bean.starttime,
                        "泊位编号：" + bean.Berthname,"离开时间："+endTime,"停车费用："+cost};
                ArrayList<byte[]> list = (new PrintUtil("停车收费小票", null, body, getFooterString(null))).getData();
              doCheckConnection(list);
                break;
            case R.id.tev_submit:

                OkHttpUtils.post().url(HTTP_HOST.URL_PARK_EXIT)
                        .addParams("Opername", getOpername())
                        .addParams("type", "1")
                        .addParams("stoptime", endTime)
                        .addParams("money", cost+"")
                        .addParams("btid", bean.btid+"")
                        .addParams("LengthTime", lengthTime)
                       .build().execute(new BeanCallBack(aty, "数据提交中") {

                    @Override
                    public void handleBeanResult(NetResultBean bean) {
                        if (bean.State) {
                            ToastUtil.show(aty, "提交数据到服务器成功");
                            tev_submit.setEnabled(false);
                            tev_submit.setBackgroundColor(getResources().getColor(R.color.grey_100));
                        } else {

                            ToastUtil.show(aty, "提交数据到服务器失败，请检查数据后重试");
                        }

                    }

                });


                break;

        }
    }
}
