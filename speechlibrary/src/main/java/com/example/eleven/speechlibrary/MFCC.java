package com.example.eleven.speechlibrary;

public class MFCC {
	
	private static void bitReverseCopy(double[] data, int len){
		int n = (int)Math.ceil(Math.log10(len) / Math.log10(2));
		for (int l = 0; l < len/2; l++) {
			int temp = l;
			for (int i = 0, j = n - 1; i < j ; i++, j--){
				if (((temp >> i & 0x01) ^ (temp >> j & 0x01)) == 1){
					temp ^= 1 << i;
					temp ^= 1 << j;
				}
			}
			if (temp != l) {
				double tmp = data[l];
				data[l] = data[temp];
				data[temp] = tmp;
			}
		}
	}

	/**
	 * 实数快速傅里叶变换
	 * @param data 待处理数据
	 * @param len 数据长度
	 * @return 快速傅里叶变换后数据fft_data，n=0为频率为0的实部，n=1为频率点len/2的实部，n=2*i为频率点i实部，n=2*i+1为频率点i虚部
	 */
	public static double[] rFFT(double[] data, int len) {
		bitReverseCopy(data, len);
		Complex[] complex_fft = new Complex[len];
		for (int i = 0; i < len; ++i){
			complex_fft[i] = new Complex(data[i], 0.0);
		}
		int n = (int)Math.ceil(Math.log10(len) / Math.log10(2));
		for (int i = 1; i <= n; i++) {
			int m = 1 << i;
			Complex W_m = new Complex(Math.cos(2*Math.PI/m), Math.sin(2*Math.PI/m));

			for (int j = 0; j < len; j += m) {
				Complex W = new Complex(1.0, 0.0);
				for (int k = 0; k < m/2; k++){
					Complex t = Complex.multiply(W, complex_fft[j + k + m/2]);
					Complex u = complex_fft[j + k];
					complex_fft[j + k] = Complex.add(u, t);
					complex_fft[j + k + m/2] = Complex.minus(u, t);
					W = Complex.multiply(W, W_m);
				}
			}
		}
		
		double[] fft_data = new double[len];
		
		fft_data[0] = complex_fft[0].getReal();
		fft_data[1] = complex_fft[len / 2].getReal();
		for (int i = 1; i < len / 2; i++){
			fft_data[2 * i] = complex_fft[i].getReal();
			fft_data[2 * i + 1] = complex_fft[i].getImage();
		}

		return fft_data;
	}

	/**
	 *
	 * @param fft 信号快速傅里叶变换数据
	 * @param len 信号长度
	 * @param m MFCC三角滤波器数目
	 * @param l MFCC参数维度,一般小于滤波器数m
	 * @param fre 采样率
	 * @return l组MFCC参数
	 */
	public static double[] mfcc(double[] fft, int len, int m, int l, int fre) {
		double max_mel = 2595*Math.log10(1 + fre/(2*700)); //�������Ƶ�ʣ���Ӧfre/2
		double inter_mel = max_mel/(m + 1);
		
		double[] cen_fre = new double[m + 2];
		cen_fre[0] = 0;
		for (int i = 1; i <= m; i++){
			cen_fre[i] = (Math.pow(10, inter_mel*i / 2595) - 1) * 700; //1��mΪm���˲�������Ƶ��
		}
		cen_fre[m + 1] = fre / 2;
		
		int[] d_cen = new int[m + 2];//��ɢƵ�����ĵ�
		d_cen[0] = 0;
		for (int i = 1; i <= m + 1; ++i) {
			d_cen[i] = (int)Math.round(cen_fre[i] * len / fre);
		}
		
		double[] power_fre = new double[len / 2 + 1]; //������
		
		power_fre[0] = fft[0] * fft[0];
		for (int i = 1; i < len / 2; ++i) {
			power_fre[i] = fft[2 * i] * fft[2 * i] + fft[2 * i + 1] * fft[2 * i + 1];
		}
		power_fre[len / 2] = fft[1] * fft[1];

		double[] output = new double[m]; //�����˲�

		for (int i = 1; i <= m; ++i) {
			output[i - 1] = 0;
			for (int j = d_cen[i - 1]; j < d_cen[i + 1]; ++j) {
				if (j <= d_cen[i])
					output[i - 1] += power_fre[j] * (j - d_cen[i - 1] + 1) / (d_cen[i] - d_cen[i - 1] + 1);
				else
					output[i - 1] += power_fre[j] * (d_cen[i + 1] - j + 1) / (d_cen[i + 1] - d_cen[i] + 1);
			}
		}

		for (int i = 0; i < m; i++) {
			output[i] = Math.log10(output[i]);
		}
		
		double[] c_mfcc = new double[l]; //mfcc参数
		for (int i = 0; i < l; i++) {
			c_mfcc[i] = 0;
			for (int j = 0; j < m; j++) {
				c_mfcc[i] += output[j] * Math.cos(Math.PI * (i + 1) * (j + 0.5) / m);
			}
		}

		return c_mfcc;
	}

    /**
     *
     * @param mfcc 待差分数组
     * @param k 时间差
     * @return 一阶差分数组
     */
    public static double[][] diff(double[][] mfcc, int k) {
        int length = mfcc.length;
        int dimension = mfcc[0].length;

        double[][] diff = new double[length][dimension];

        for (int j = 0; j < dimension; j++) {
            for (int i = 0; i < length; i++) {
                if (i < k) {
                    diff[i][j] = mfcc[i + 1][j] - mfcc[i][j];
                } else if (i >= length - k) {
                    diff[i][j] = mfcc[i][j] - mfcc[i - 1][j];
                } else {
                    double sum = 0.0;
                    double div = 0.0;
                    for (int count = 1; count <= k; count++) {
                        sum += count * (mfcc[i + count][j] - mfcc[i - count][j]);
                        div += 2 * Math.pow(k, 2);
                    }
                    diff[i][j] = sum / Math.sqrt(div);
                }
            }
        }
        return diff;
    }
}
