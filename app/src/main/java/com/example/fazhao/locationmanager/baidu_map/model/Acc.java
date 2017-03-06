package com.example.fazhao.locationmanager.baidu_map.model;

import android.hardware.Sensor;
import android.hardware.SensorListener;
/**
 *根据加速度判断手机是否处于静止
 * @author Administrator
 *
 */
public class Acc implements SensorListener {
	//	/**
	//	 * 手机加速度各方向状态
	//	 */
	//	private float F_Acc_x,F_Acc_y,F_Acc_z;
	//	/**
	//	 * 上次获取状态时间
	//	 */
	//	private long LastUpdateTime;   
	//	/**
	//	 * 两次获取状态时间间隔单位(秒)
	//	 */
	//	private final int UPTATE_INTERVAL_TIME = 1000*10;   
	//	
	/**
	 * 当前传感器的值
	 */
	private float gravityNew = 0;
	/**
	 * 上次传感器的值
	 */
	private float gravityOld = 0;
	/**
	 * 此次波峰的时间
	 */
	private long timeOfThisPeak = 0;
	/**
	 * 上次波峰的时间
	 */
	private long timeOfLastPeak = 0;
	/**
	 * 当前的时间
	 */
	private long timeOfNow = 0;;
	/**
	 * 波峰值
	 */
	private float peakOfWave = 0;
	/**
	 * 波谷值
	 */
	private float valleyOfWave = 0;
	/**
	 * 初始阈值
	 */
	private float ThreadValue = (float) 2.0;
	/**
	 * 动态阈值需要动态的数据，这个值用于这些动态数据的阈值
	 */
	private final float initialValue = (float) 1.3;
	/**
	 * 上一点的状态，上升还是下降
	 */
	private boolean lastStatus = false;
	/**
	 * 是否上升的标志位
	 */
	private boolean isDirectionUp = false;
	/**
	 * 持续上升次数
	 */
	private int continueUpCount = 0;
	/**
	 * 上一点的持续上升的次数，为了记录波峰的上升次数
	 */
	private int continueUpFormerCount = 0;
	public boolean is_Acc=false;
	//	private int ACC=30;//手机感应器波动范围,30以内判定手机处于静止
	private int tempCount = 0;
	private final int valueNum = 4;
	/**
	 * 用于存放计算阈值的波峰波谷差值
	 */
	private float[] tempValue = new float[valueNum];
	/**
	 * 记录波峰数量
	 */
	private int CountValue = 0;
	/**
	 * 判断传感器是否在运行
	 */
	public boolean IsRun=false; 
	
	public Acc(){
		//		LastUpdateTime=System.currentTimeMillis();
	}
	@Override
	public void onAccuracyChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}
	/**
	 * 感应器状态改变时自动调用此方法
	 */
	@Override
	public void onSensorChanged(int arg0, float[] arg1) {
		// TODO Auto-generated method stub
		IsRun=true;
		if(arg0==Sensor.TYPE_ACCELEROMETER){
			//			JIUjia(arg1);
			gravityNew = (float) Math.sqrt(arg1[0] * arg1[0]
					+ arg1[1] * arg1[1] + arg1[2] * arg1[2]);
			DetectorNewStep(gravityNew);
		}
	}

	//	 protected boolean JIUjia(float[] values) {
	//	    	if(F_Acc_x!=0){
	//	    		long currentUpdateTime = System.currentTimeMillis();   
	//	    		long timeInterval = currentUpdateTime - LastUpdateTime;    
	//	    		if(timeInterval < UPTATE_INTERVAL_TIME)
	//	    			return false;
	//	    		LastUpdateTime=currentUpdateTime;
	//	    		float tem0=values[0]-F_Acc_x;
	//	    		float tem1=values[1]-F_Acc_y;
	//	    		float tem2=values[2]-F_Acc_z;
	//	    		System.out.println(Math.abs(tem0)+","+Math.abs(tem1)+","+Math.abs(tem2));
	//	    		if(Math.abs(tem0)>ACC||Math.abs(tem1)>ACC||Math.abs(tem2)>ACC)
	//	    			is_Acc=true;
	//	    		
	//	    	}
	//	    	F_Acc_x=values[0];
	//	    	F_Acc_y=values[1];
	//	    	F_Acc_z=values[2];
	//	    	return is_Acc;
	//
	//	    }

	/*
	 * 检测步子
	 * 1.传入sersor中的数据
	 * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
	 * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
	 * */
	public void DetectorNewStep(float values) {
		if (gravityOld == 0) {
			gravityOld = values;
		} else {
			if (DetectorPeak(values, gravityOld)) {
				timeOfLastPeak = timeOfThisPeak;
				timeOfNow = System.currentTimeMillis();
				if ((timeOfNow - timeOfLastPeak) >= 250&& (peakOfWave - valleyOfWave >= ThreadValue)) {
					timeOfThisPeak = timeOfNow;
					//两步之间间隔大于4秒则不算
					if((timeOfNow-timeOfLastPeak)>40000)
						CountValue=0;
					else
						CountValue++;
					//只有手机连续摇晃4下或者以上才判定为走路
					if(CountValue>=4)
						is_Acc=true;
					//	                    mStepListeners.onStep();
				}
				if (timeOfNow - timeOfLastPeak >= 250&& (peakOfWave - valleyOfWave >= initialValue)) {
					timeOfThisPeak = timeOfNow;
					ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);
				}
			}
		}
		gravityOld = values;
	}

	/*
	 * 检测波峰
	 * 以下四个条件判断为波峰：
	 * 1.目前点为下降的趋势：isDirectionUp为false
	 * 2.之前的点为上升的趋势：lastStatus为true
	 * 3.到波峰为止，持续上升大于等于4次
	 * 4.波峰值大于20
	 * 记录波谷值
	 * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
	 * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
	 * */
	public boolean DetectorPeak(float newValue, float oldValue) {
		lastStatus = isDirectionUp;
		if (newValue >= oldValue) {
			isDirectionUp = true;
			continueUpCount++;
		} else {
			continueUpFormerCount = continueUpCount;
			continueUpCount = 0;
			isDirectionUp = false;
		}

		if (!isDirectionUp && lastStatus&& (continueUpFormerCount >= 4 || oldValue >= 20&&oldValue<=40)) {
			peakOfWave = oldValue;
			return true;
		} else if (!lastStatus && isDirectionUp) {
			valleyOfWave = oldValue;
			return false;
		} else {
			return false;
		}
	}

	/*
	 * 阈值的计算
	 * 1.通过波峰波谷的差值计算阈值
	 * 2.记录4个值，存入tempValue[]数组中
	 * 3.在将数组传入函数averageValue中计算阈值
	 * */
	public float Peak_Valley_Thread(float value) {
		float tempThread = ThreadValue;
		if (tempCount < valueNum) {
			tempValue[tempCount] = value;
			tempCount++;
		} else {
			tempThread = averageValue(tempValue, valueNum);
			for (int i = 1; i < valueNum; i++) {
				tempValue[i - 1] = tempValue[i];
			}
			tempValue[valueNum - 1] = value;
		}
		return tempThread;

	}

	/*
	 * 梯度化阈值
	 * 1.计算数组的均值
	 * 2.通过均值将阈值梯度化在一个范围里
	 * */
	public float averageValue(float value[], int n) {
		float ave = 0;
		for (int i = 0; i < n; i++) {
			ave += value[i];
		}
		ave = ave / valueNum;
		if (ave >= 8)
			ave = (float) 4.3;
		else if (ave >= 7 && ave < 8)
			ave = (float) 3.3;
		else if (ave >= 4 && ave < 7)
			ave = (float) 2.3;
		else if (ave >= 3 && ave < 4)
			ave = (float) 2.0;
		else {
			ave = (float) 1.3;
		}
		return ave;
	}
}
