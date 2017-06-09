package com.test.BTClient;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import com.BluetoothScope.R;
import com.test.BTClient.DeviceListActivity;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
//import android.view.Menu;            //如使用菜单加入此三包
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BTClient extends Activity {
	
	private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
	
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
	
	private Scope mScope = null;
	private InputStream is;    //输入流，用来接收蓝牙数据
    private EditText edit0;    //发送数据输入句柄
    public String filename=""; //用来保存存储的文件名
    BluetoothDevice _device = null;     //蓝牙设备
    BluetoothSocket _socket = null;      //蓝牙通信socket
    boolean _discoveryFinished = false;    
    boolean bRun = true;
    boolean bThread = false;
	private TextView peakText = null; 
	private TextView averageText = null ;
    
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备
    final Handler drawScopeHandel = new Handler(){
    	public void handleMessage(Message msg){
    		super.handleMessage(msg);
    		mScope.inputData((Integer) msg.obj);
    		peakText.setText(mScope.getPeakValueString());
    		averageText.setText(mScope.getAverageValueString());
    		mScope.invalidate();
    	}
    };
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);   //设置画面为主画面 main.xml
        edit0 = (EditText)findViewById(R.id.Edit0);   //得到输入框句柄
        mScope = (Scope) findViewById(R.id.ScopeView_View);
        peakText = (TextView) findViewById(R.id.PeakValue_Text);
        averageText = (TextView) findViewById(R.id.AverageValue_Text);
        findViewById(R.id.Button_Connect_View).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onConnectButtonClicked(v);
			}
		});
        findViewById(R.id.Button_Save_View).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "存储波形的功能还没弄出来- 。-", Toast.LENGTH_LONG).show();
			}
		});
        findViewById(R.id.Button_Clear_View).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mScope.EmptyQueue();
			}
		});
        findViewById(R.id.Button_Exit_View).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();		
			}
		});
        findViewById(R.id.Button_Send_View).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onSendButtonClicked(v);// TODO Auto-generated method stub
				
			}
		});

       //如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (_bluetooth == null){
        	Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // 设置设备可以被搜索  
       new Thread(){
    	   public void run(){
    		   if(_bluetooth.isEnabled()==false){
        		_bluetooth.enable();
    		   }
    	   }   	   
       }.start();      
    }

    //发送按键响应
    public void onSendButtonClicked(View v){
    	int i=0;
    	int n=0;
    	try{
    		OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流
    		byte[] bos = edit0.getText().toString().getBytes();
    		for(i=0;i<bos.length;i++){
    			if(bos[i]==0x0a)n++;
    		}
    		byte[] bos_new = new byte[bos.length+n];
    		n=0;
    		for(i=0;i<bos.length;i++){ //手机中换行为0a,将其改为0d 0a后再发送
    			if(bos[i]==0x0a){
    				bos_new[n]=0x0d;
    				n++;
    				bos_new[n]=0x0a;
    			}else{
    				bos_new[n]=bos[i];
    			}
    			n++;
    		}
    		
    		os.write(bos_new);	
    	}catch(IOException e){  		
    	}  	
    }
    
    //接收活动结果，响应startActivityForResult()
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode){
    	case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
    		// 响应返回结果
            if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                // MAC地址，由DeviceListActivity设置返回
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // 得到蓝牙设备句柄      
                _device = _bluetooth.getRemoteDevice(address);
 
                // 用服务号得到socket
                try{
                	_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                }catch(IOException e){
                	Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                }
                //连接socket
            	Button btn = (Button) findViewById(R.id.Button_Connect_View);
                try{
                	_socket.connect();
                	Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
                	findViewById(R.id.Button_Save_View).setEnabled(true);
                	findViewById(R.id.Button_Clear_View).setEnabled(true);
                	findViewById(R.id.Button_Send_View).setEnabled(true);
                	findViewById(R.id.Edit0).setEnabled(true);
                	btn.setText("断开");
                }catch(IOException e){
                	try{
                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                		_socket.close();
                		_socket = null;
                	}catch(IOException ee){
                		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                	}
                	
                	return;
                }
                
                //打开接收线程
                try{
            		is = _socket.getInputStream();   //得到蓝牙数据输入流
            		}catch(IOException e){
            			Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
            			return;
            		}
            		if(bThread==false){
            			ReadThread.start();
            			bThread=true;
            		}else{
            			bRun = true;
            		}
            }
    		break;
    	default:break;
    	}
    }
    
    //接收数据线程
    Thread ReadThread=new Thread(){
    	
    	public void run(){
    		int num = 0;
    		byte[] buffer = new byte[1024];
    		bRun = true;
    		//接收线程
    		while(true){
    			try{
    				while(is.available()==0){
    					while(bRun == false){}
    				}
    				while(true){
    					num = is.read(buffer);         //读入数据
    					for(int i=0;i<num;i++)
    					{
    						if(buffer[i]!=-18)
    							continue;
    						else if(i>=num-2 ||buffer[i+1]==-18 )
    							continue;
	    					Message mes = Message.obtain();
	    					short x =(byte) (buffer[++i] & 0xff);
	    					short y =buffer[++i];
	    		     		mes.obj=(y>0?(x *256 + y):(x *256 +(256+y)));
	    		     		drawScopeHandel.sendMessage(mes);
    					}
    				}
    				//发送显示消息，进行显示刷新  	    		
    	    		}catch(IOException e){
    	    		}
    		}
    	}
    };
    
    
    //关闭程序掉用处理部分
    public void onDestroy(){
    	super.onDestroy();
    	if(_socket!=null)  //关闭连接socket
    	try{
    		_socket.close();
    	}catch(IOException e){}
    //	_bluetooth.disable();  //关闭蓝牙服务
    }
    //连接按键响应函数
    public void onConnectButtonClicked(View v){ 
    	if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
    		Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
    		return;
    	}
        //如未连接设备则打开DeviceListActivity进行设备搜索
    	Button btn = (Button) findViewById(R.id.Button_Connect_View);
    	if(_socket==null){
    		Intent serverIntent = new Intent(this, DeviceListActivity.class); //跳转程序设置
    		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
    	}
    	else{
    		 //关闭连接socket
    	    try{
    	    	
    	    	is.close();
    	    	_socket.close();
    	    	_socket = null;
    	    	bRun = false;
    	    	btn.setText("连接");
    	    	findViewById(R.id.Button_Save_View).setEnabled(false);
            	findViewById(R.id.Button_Clear_View).setEnabled(false);
            	findViewById(R.id.Button_Send_View).setEnabled(false);
            	findViewById(R.id.Edit0).setEnabled(false);
    	    }catch(IOException e){}   
    	}
    	return;
    }
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.option_menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.about_item) {
			Toast.makeText(this, "made by BW", Toast.LENGTH_LONG).show();
		}
		return super.onOptionsItemSelected(item);
	}
}