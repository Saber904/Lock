package com.example.eleven.lock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

import com.example.eleven.speechlibrary.DTWrecognize;
import com.example.eleven.speechlibrary.Recorder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lock.util.DataProcess;


/**
 * Created by Eleven on 2015/7/23.
 */

public class SetlockActivity extends Activity{
    private ViewFlipper vf;

    private View firstView; //设置声音锁的顺序
    private View secondView;
    private View thirdView;
    private View forthView;

    private Recorder recorder;

    private double[][] firstMfcc;
    private double[][] secondMfcc;

    ExecutorService executor = Executors.newCachedThreadPool();

    private AlertDialog alertDialog;

    private final CountDownLatch latch = new CountDownLatch(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setlock);

        recorder = Recorder.getInstance();

        vf = (ViewFlipper) findViewById(R.id.viewFlipper);

        firstView = vf.findViewById(R.id.first);
        secondView = vf.findViewById(R.id.second);
        thirdView = vf.findViewById(R.id.third);
        forthView = vf.findViewById(R.id.forth);

        Button btn_speak1 = (Button) firstView.findViewById(R.id.btn_speak);
        Button btn_speak2 = (Button) thirdView.findViewById(R.id.btn_speak);

        Button btn_cancel1 = (Button) firstView.findViewById(R.id.btn_cancel);
        Button btn_cancel2 = (Button) secondView.findViewById(R.id.btn_cancel2);
        Button btn_cancel3 = (Button) thirdView.findViewById(R.id.btn_cancel);
        Button btn_confirm = (Button) forthView.findViewById(R.id.btn_confirm);

        Button btn_next = (Button) secondView.findViewById(R.id.btn_next);

        Button btn_unlock = (Button) forthView.findViewById(R.id.btn_unlock);

        btn_cancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_cancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_cancel3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vf.showNext();
            }
        });

        btn_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetlockActivity.this, UnlockActivity.class);
                startActivity(intent);
            }
        });

        btn_speak1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    AlertDialog.Builder adBuilder = new AlertDialog.Builder(SetlockActivity.this);
                    adBuilder.setMessage(R.string.recording)
                            .setCancelable(false)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    alertDialog = adBuilder.create();
                    alertDialog.show();//显示对话框
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

                    recorder.start();//开始录音
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    recorder.stop();
                    String strProcessing = getString(R.string.processing);
                    alertDialog.setMessage(strProcessing);

                    try {
                        firstMfcc = DataProcess.getMfcc(recorder.getNormalizedData());
                    } catch (Exception e) {
                        Log.e("getMfcc", "error");
                    }

                    if (firstMfcc == null) {
                        String strNoSpeech = getString(R.string.no_speech);
                        alertDialog.setMessage(strNoSpeech);
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        alertDialog.cancel();
                        vf.showNext();
                    }
                }
                return false;
            }
        });

        btn_speak2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    AlertDialog.Builder adBuilder = new AlertDialog.Builder(SetlockActivity.this);
                    adBuilder.setMessage(R.string.recording)
                            .setCancelable(false)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    alertDialog = adBuilder.create();
                    alertDialog.show();//显示对话框
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

                    recorder.start();//开始录音
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    recorder.stop();
                    String strProcessing = getString(R.string.processing);
                    alertDialog.setMessage(strProcessing);



                    try {
                        secondMfcc = DataProcess.getMfcc(recorder.getNormalizedData());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (secondMfcc == null) {
                        String strNoSpeech = getString(R.string.no_speech);
                        alertDialog.setMessage(strNoSpeech);
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);

                    } else {
                        double dtwDis;
                        try {
                            dtwDis = DTWrecognize.dtw(firstMfcc, secondMfcc);
                            double meanErr = dtwDis * 2 / (secondMfcc.length + firstMfcc.length);
//                            alertDialog.setMessage("dtw误差：" + dtwDis + '\n'
//                                        + "帧数1：" + firstMfcc.length + '\n'
//                                        + "帧数2：" + secondMfcc.length + '\n'
//                                        + "平均误差：" + mean);
//                            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);

                            if (meanErr > 40) {//阀值设的40
                                String strReRecord = getString(R.string.not_the_same);
                                alertDialog.setMessage(strReRecord);
                                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                            } else {
                                executor.execute(new writeDataTask("firstMfcc.dat", firstMfcc));//保存声音锁的mfcc数据
                                executor.execute(new writeDataTask("secondMfcc.dat", secondMfcc));
                                latch.await();
                                alertDialog.cancel();
                                vf.showNext();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });
    }


    /**
     * 写入数据
     */
    private class writeDataTask implements Runnable {
        String fileName;
        double[][] data;

        public writeDataTask(String fileName, double[][] data) {
            this.fileName = fileName;
            this.data = data;
        }

        @Override
        public void run() {
            FileOutputStream fos = null;
            try {
                fos = openFileOutput(fileName, MODE_PRIVATE);
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos));
                for (int i = 0; i < data.length; i++){
                    for (int j = 0; j < data[0].length; j++){
                        dos.writeDouble(data[i][j]);
                    }
                }
                dos.flush();
                latch.countDown();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
