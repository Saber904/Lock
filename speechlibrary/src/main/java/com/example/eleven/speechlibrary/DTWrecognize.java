package com.example.eleven.speechlibrary;

import java.util.Arrays;

public class DTWrecognize {
	/**
	 * 计算mfcc参数之间的dtw距离
	 * @param input
	 * @param pattern
	 * @return
	 */
	public static double dtw(final double[][] input, final double[][] pattern) {
		int n = input.length;
		int m = pattern.length;
		double[][] distance_matrix = new double[n][m];//距离矩阵
		for (int i = 0; i < n; ++i) {
			Arrays.fill(distance_matrix[i], -1);
		}
		//计算每帧距离
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < m; ++j) {
				//if ((j + 1 <= 2 * (i + 1) - 1) && (j + 1 >= 2 * (i + 1) + (m - 2 * n))) {
					distance_matrix[i][j] = distance(input[i], pattern[j]);
				//}
			}
		}
		
		double[][] cost_matrix = null;
		cost_matrix = calc_cost(distance_matrix, n, m);
		
		return cost_matrix[cost_matrix.length - 1][cost_matrix[0].length - 1];
		
	}
	
	private static double[][] calc_cost(double[][] dis, int n, int m) {
		int start_i = 0,
			start_j = 0,
			stop_i = n,
			stop_j = m;
		double minDis = Double.MAX_VALUE;

		int di = n / 20;
		int dj = m / 20;

		for (int i = 0; i < di; i++){
			for (int j = 0; j < dj; j++){
				if (dis[i][j] < minDis) {
					minDis = dis[i][j];
					start_i = i;
					start_j = j;
				}
			}
		}
		minDis = Double.MAX_VALUE;
		for (int i = n - 1; i >= n - di; i--){
			for (int j = m - 1; j >= m - dj; j--){
				if (dis[i][j] < minDis){
					minDis = dis[i][j];
					stop_i = i;
					stop_j = j;
				}
			}
		}
		double[][] cost = new double[stop_i - start_i + 1][stop_j - start_j + 1];
		for (int i = 0; i < cost.length; ++i) {
			Arrays.fill(cost[i], -1);
		}
		cost[0][0] = dis[start_i][start_j];
		//计算第一行
		for (int i = start_i + 1; i <= stop_i; ++i) {
			//if (dis[i][0] < 0) {
				//break;
			//}
			//else {
				cost[i - start_i][0] = dis[i][0] + cost[i - start_i - 1][0];
			//}
		}
		//计算第一列
		for (int j = start_j + 1; j <= stop_j; ++j) {
			//if (dis[0][j] < 0) {
				//break;
			//}
			//else {
				cost[0][j - start_j] = dis[0][j] + cost[0][j - start_j - 1];
			//}
		}
		//计算其他
		for (int j = start_j + 1; j <= stop_j; ++j) {
			for (int i = start_i + 1; i <= stop_i; ++i) {
					cost[i - start_i][j - start_j] = dis[i][j] +
							Math.min(cost[i - start_i - 1][j - start_j - 1],
									Math.min(cost[i - start_i- 1][j - start_j], cost[i - start_i][j - start_j - 1]));
			}
		}
		return cost;
	}

	/**
	 * 计算矢量间的欧氏距离
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	private static double distance(final double[] lhs, final double[] rhs) {
		double result = 0;
		for (int i = 0; i < lhs.length; ++i) {
			result += Math.pow((lhs[i] - rhs[i]), 2);
		}
		return result;
	}
}
