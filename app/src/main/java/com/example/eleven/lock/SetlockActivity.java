package com.example.eleven.lock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

import com.example.eleven.speechlibrary.DTWrecognize;
import com.example.eleven.speechlibrary.MFCC;
import com.example.eleven.speechlibrary.Preprocess;
import com.example.eleven.speechlibrary.Recorder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


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

    static final int frequency = 22050;
    static final int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private final int NUM_OF_MFCC = 12;
    private final int NUM_OF_FILTER = 26;

    private double[][] firstMfcc;
    private double[][] secondMfcc;

    private AlertDialog alertDialog;

    private final CountDownLatch latch = new CountDownLatch(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setlock);

        recorder = new Recorder(frequency, channelConfiguration, audioEncoding);

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

                    ExecutorService executor = Executors.newCachedThreadPool();
                    Future<double[][]> future = executor.submit(new DataProcess());//处理数据，获得第一个语音的mfcc参数

                    try {
                        firstMfcc = future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
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

                    ExecutorService executor = Executors.newCachedThreadPool();
                    Future<double[][]> future = executor.submit(new DataProcess());//处理数据，获得第二个语音的mfcc参数

                    try {
                        secondMfcc = future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    if (secondMfcc == null) {
                        String strNoSpeech = getString(R.string.no_speech);
                        alertDialog.setMessage(strNoSpeech);
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);

                    } else {
                        Future<Double> doubleFuture = executor.submit(new dtwCalcTask(firstMfcc, secondMfcc));
                        double dtwDis;
                        try {
                            dtwDis = doubleFuture.get();
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
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * 获得mfcc参数
     */
    private class DataProcess implements Callable<double[][]> {

        @Override
        public double[][] call() throws Exception {
            double[] nomalizeData = recorder.getNormalizedData();
            //预加重信号
            double[] preEmphasis = Preprocess.highpass(nomalizeData);
            //短时能量和短时过零率
            double[] stApm = Preprocess.shortTernEnergy(preEmphasis, frequency);
            double[] stCZ = Preprocess.shortTernCZ(preEmphasis, frequency);
            //端点检测
            ArrayList<Integer> endPoints = null;
            endPoints = Preprocess.divide(stApm, stCZ);

            if (endPoints.size() < 2) {
                return null;
            }

            ArrayList<double[]> speechFrames = new ArrayList<>();

            for (int i = 0; i < endPoints.size(); i = i + 2){
                for (int j = endPoints.get(i); j < endPoints.get(i + 1); ++j) {
                    double[] frame = new double[512];
                    System.arraycopy(preEmphasis, 256 * j, frame, 0, 512);
                    Preprocess.hamming(frame);
                    speechFrames.add(frame);
                }
            }

            double[][] mfcc = new double[speechFrames.size()][NUM_OF_MFCC];
            for (int i = 0; i < speechFrames.size(); ++i) {
                double[] fftData = MFCC.rFFT(speechFrames.get(i), 512);
                double[] mfccData = MFCC.mfcc(fftData, 512, NUM_OF_FILTER, NUM_OF_MFCC, frequency);
                mfcc[i] = mfccData;
            }
            double[][] dtMfcc = MFCC.diff(mfcc, 2); //mfcc一阶差分参数
            double[][] result = new double[speechFrames.size()][2 * NUM_OF_MFCC];
            for (int i = 0; i < result.length; i++){ //合并mfcc和一阶差分参数
                System.arraycopy(mfcc[i], 0, result[i], 0, NUM_OF_MFCC);
                System.arraycopy(dtMfcc[i], 0, result[i], NUM_OF_MFCC, NUM_OF_MFCC);
            }
            return result;
        }
    }

    /**
     * 计算dtw距离
     */
    private class dtwCalcTask implements Callable<Double> {

        private final double[][] first;
        private final double[][] second;

        public dtwCalcTask(double[][] first, double[][] second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public Double call() throws Exception {
            double dtwDis = DTWrecognize.dtw(first, second);
            return dtwDis;
        }
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
