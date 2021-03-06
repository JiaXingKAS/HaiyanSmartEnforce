package smartenforce.aty.parking;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kas.clientservice.haiyansmartenforce.R;

import smartenforce.base.NetResultBean;
import smartenforce.bean.tcsf.TcListBeanResult;
import smartenforce.dialog.PayInfoDialog;

import smartenforce.projectutil.PrintUtil;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.ArrayList;

import smartenforce.base.HttpApi;
import smartenforce.impl.BeanCallBack;
import smartenforce.impl.NoFastClickLisener;
import smartenforce.util.DateUtil;
import smartenforce.zxing.ScanActivity;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

/**
 * 离开详情页面
 */
public class ExitActivity extends PrintActivity {
    private TextView tev_cphm, tev_trsj, tev_pwbh, tev_lksj, tev_tcfy;
    private TextView tev_print, tev_submit;

    private String endTime;
    private long cost;
    private String lengthTime;

    private TcListBeanResult bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exitinging);
    }

    @Override
    protected void findViews() {
        tev_cphm = (TextView) findViewById(R.id.tev_cphm);
        tev_trsj = (TextView) findViewById(R.id.tev_trsj);
        tev_pwbh = (TextView) findViewById(R.id.tev_pwbh);
        tev_lksj = (TextView) findViewById(R.id.tev_lksj);
        tev_tcfy = (TextView) findViewById(R.id.tev_tcfy);
        tev_print = (TextView) findViewById(R.id.tev_print);
        tev_submit = (TextView) findViewById(R.id.tev_submit);

    }

    @Override
    protected void initDataAndAction() {
        tev_title.setText("离开收费");
        bean = (TcListBeanResult) getIntent().getSerializableExtra("TcListBeanResult");
        tev_print.setOnClickListener(FastClickLister);
        tev_submit.setOnClickListener(FastClickLister);
        endTime = DateUtil.currentTime();
        cost = DateUtil.calMoney(endTime, bean.starttime);
        lengthTime = DateUtil.getTimeLenth(endTime, bean.starttime);
        tev_cphm.setText(bean.carnum);
        tev_pwbh.setText(bean.Berthname);
        tev_trsj.setText(bean.starttime);
        tev_lksj.setText(endTime);
        tev_tcfy.setText(cost + "元");
        changeState(true, tev_print, tev_submit);

    }

    @Override
    public void onPrintSuccess() {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanResult != null) {
                    String authCode = scanResult.getContents();
                    doSuccessInfo(authCode);
                } else {
                    showPayCash("扫描失败，重新选择支付方式");
                }
            }
        }

    }

    NoFastClickLisener FastClickLister = new NoFastClickLisener() {
        @Override
        public void onNofastClickListener(View v) {
            switch (v.getId()) {
                case R.id.tev_print:
                    String[] body = new String[]{"车牌号码：" + bean.carnum, "停入时间：" + bean.starttime,
                            "泊位编号：" + bean.Berthname, "离开时间：" + endTime, "停车费用：" + cost};
                    ArrayList<byte[]> list = (new PrintUtil("停车收费小票", null, body, getFooterString())).getData();
                    doCheckConnection(list);
                    break;
                case R.id.tev_submit:

                    OkHttpUtils.post().url(HttpApi.URL_PARK_EXIT)
                            .addParams("Opername", getOpername())
                            .addParams("type", "1")
                            .addParams("stoptime", endTime)
                            .addParams("money", cost + "")
                            .addParams("btid", bean.btid + "")
                            .addParams("LengthTime", lengthTime)
                            .build().execute(new BeanCallBack(aty, "数据提交中") {

                        @Override
                        public void handleBeanResult(NetResultBean bean) {
                            if (bean.State) {
                                changeState(false, tev_print, tev_submit);
//                                showPayCash("本次停车将收取"+cost+"元");
                                showcustomer("本次停车将收取" + cost + "元");
                            } else {
                                warningShow(bean.ErrorMsg);
                            }

                        }

                    });
                    break;
            }

        }
    };


    private void showcustomer(final String body) {
        if (cost > 0) {
            SpannableStringBuilder spannable = new SpannableStringBuilder(body);
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 7, body.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new AbsoluteSizeSpan(25, true), 7, body.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            PayInfoDialog dialog = new PayInfoDialog(this, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.tev_money:
                            doSuccessInfo("-1");
                            break;
                        case R.id.tev_weixin:
                            startActivityForResult(new Intent(aty, ScanActivity.class), REQUEST_CODE);
                            break;
                        case R.id.tev_none:
                            doSuccessInfo("-2");
                            break;
                    }
                }


            });

            dialog.showSpinnerString(spannable);

        } else {
            doSuccessInfo("-1");
        }


    }

    private void showPayCash(final String body) {
        if (cost == 0) {
            doSuccessInfo("-1");
        } else {
            final String[] arr = new String[]{"现金", "微信", "不交"};
            new AlertView("收费", body, null, null, arr, aty, null, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if (position == 0) {
                        doSuccessInfo("-1");
                    } else if (position == 1) {
                        startActivityForResult(new Intent(aty, ScanActivity.class), REQUEST_CODE);
                    } else if (position == 2) {
                        doSuccessInfo("-2");
                    }
                }
            }).show();
        }

    }


    private void doSuccessInfo(String authCode) {
        OkHttpUtils.post().url(HttpApi.URL_WXPAY)
                .addParams("auth_code", authCode)
                .addParams("body", bean.carnum + "停车收费")
                .addParams("fee", cost * 100 + "")
                .addParams("btid", bean.btid + "")
                .build().execute(new BeanCallBack(aty, cost > 0 ? "收款中" : null) {

            @Override
            public void handleBeanResult(NetResultBean netResultBean) {
                if (cost > 0) {
                    if (netResultBean.State) {
                        show("收款成功");
                    } else {
                        showPayCash(netResultBean.ErrorMsg);
                    }
                } else {
                    //付款金额为0,只调用一次，如果失败不再重复调用

                }

            }
        });
    }


}