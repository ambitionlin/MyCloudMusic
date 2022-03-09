package com.example.mycloudmusic

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_music.*
import java.lang.Integer.min
import java.lang.Integer.parseInt
import java.sql.Connection

class MusicActivity : AppCompatActivity(), View.OnClickListener {
    //按钮
    private var mBtnPlay: Button? = null
    private var mBtnPause: Button? = null
    private var mBtnContinuePlay: Button? = null
    private var mBtnExit: Button? = null

    //动画
    private var mAnimator: Animator? = null
    private var mMusicControl: MusicService.MusicControl? = null
    private var mName: String? = null
    private var mIntent1: Intent? = null
    private var mIntent2: Intent? = null
    private var mConn: MyServiceConn? = null

    //记录服务是否被解绑，默认没有
    private var mIsUnbind: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        //获取从fragment1传来的信息
        mIntent1 = intent
        init()
    }

    private fun init() {
        //进度条的控件
        mSeekBar = findViewById(R.id.seekbar)
        //进度条上小绿点的位置，也就是当前已播放时间
        mTextViewProgress = findViewById(R.id.textview_progress)
        //进度条的总长度，就是总时间
        mTextViewTotal = findViewById(R.id.textview_total)
        //歌曲名显示的控件
        mSongName = findViewById(R.id.song_name)
        //按钮的控件
        mBtnPlay = findViewById(R.id.btn_play)
        mBtnPause = findViewById(R.id.btn_pause)
        mBtnContinuePlay = findViewById(R.id.btn_continue_play)
        mBtnExit = findViewById(R.id.btn_exit)
        //给各个按钮设置点击事件
        mBtnPlay?.setOnClickListener(this)
        mBtnPause?.setOnClickListener(this)
        mBtnContinuePlay?.setOnClickListener(this)
        mBtnExit?.setOnClickListener(this)

        mName = mIntent1?.getStringExtra("name")
        mSongName?.text = mName

        //创建一个意图对象，是从当前的Activity跳转到Service， Service中需要有companion object
        mIntent2 = Intent(this, MusicService::class.java)
        mConn = MyServiceConn()    //创建服务连接对象
        bindService(mIntent2, mConn!!, BIND_AUTO_CREATE)    //绑定服务

        //为滑动条添加事件监听，每个控件不同 点击事件方法名都不同
        mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            //这一行注解是保证API在KITKAT以上的模拟器才能顺利运行，也就是19以上
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            override fun onProgressChanged(seekBar: SeekBar, process: Int, fromUser: Boolean) {
                //当滑动到滑动条末端时，动画结束
                if (process == seekBar.max) {
                    mAnimator?.pause()
                }
            }

            //滑动条开始滑动时调用
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            //滑动条停止滑动时调用
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //根据拖动的进度改变音乐播放进度
                val progress: Int? = seekBar?.progress    //获取seekBar的进度
                progress?.let { mMusicControl?.seekTo(it) }    //改变播放进度
            }
        })
        //声明并绑定音乐播放器的iv_music控件
        val imageViewMusic: ImageView = findViewById(R.id.iv_music)
        val position: String? = mIntent1?.getStringExtra("position")
        //praseInt()就是将字符串变成整数类型
        val i: Int = position?.let { parseInt(it) } ?: 0
        imageViewMusic.setImageResource(FirstFragment.icons[i])
        //rotation和0f,360.0f就设置了动画是从0°旋转到360°
        mAnimator = ObjectAnimator.ofFloat(imageViewMusic, "rotation", 0f, 360.0f)
        mAnimator?.duration = 10000   //动画旋转一周的时间为10秒
        mAnimator?.interpolator = LinearInterpolator()
    }

    //handler机制，可以理解为线程间的通信，我获取到一个信息，然后把这个信息告诉你，就这么简单
    companion object {
        //进度条
        private var mSeekBar: SeekBar? = null
        private var mTextViewTotal: TextView? = null
        private var mTextViewProgress: TextView? = null
        private var mSongName: TextView? = null

        public var mHandler: Handler = object : Handler() {   //创建消息处理器对象
            //在主线程中处理从子线程发送过来的消息，在本项目中主要处理时间信息并展示
            override fun handleMessage(msg: Message) {
                //msg就是子线程发送过来的消息
                val bundle: Bundle = msg.data  //获取从子线程发送过来的音乐播放进度
                //获取当前进度currentPosition和总时长duration
                val duration: Int = bundle.getInt("duration")
                val currentPosition: Int = bundle.getInt("currentPosition")
                //对进度条进行设置
                mSeekBar?.max = duration  //总时长
                mSeekBar?.progress = currentPosition  //播放进度
                //歌曲总时长多少分钟多少秒
                val minute: Int = duration / 1000 / 60
                val second: Int = duration / 1000 % 60
                var strMinute: String? = null
                var strSecond: String? = null
                strMinute = if (minute < 10) {      //如果歌曲的时间中的分钟小于10
                    "0$minute"      //在分钟的前面加一个0
                } else {
                    minute.toString()
                }
                strSecond = if (second < 10) {      //如果歌曲的时间中的秒小于10
                    "0$second"
                } else {
                    second.toString()
                }
                //这里就显示了歌曲总时长
                mTextViewTotal?.text = "$strMinute:$strSecond"

                //歌曲当前播放进度多少分钟多少秒
                val curMinute: Int = currentPosition / 1000 / 60
                val curSecond: Int = currentPosition / 1000 % 60
                var strCurMinute: String? = null
                var strCurSecond: String? = null
                strCurMinute = if (curMinute < 10) {
                    "0$curMinute"
                } else {
                    curMinute.toString()
                }
                strCurSecond = if (curSecond < 10) {
                    "0$curSecond"
                } else {
                    curSecond.toString()
                }
                //这里显示歌曲当前播放时长
                mTextViewProgress?.text = "$strCurMinute:$strCurSecond"
            }
        }
    }

    //用于实现连接服务，比较模板化，不需要详细知道内容
    inner class MyServiceConn : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mMusicControl = service as? MusicService.MusicControl
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    //判断服务是否被解绑
    private fun unbind(isUnbind: Boolean) {
        //如果解绑了
        if (!isUnbind) {
            mMusicControl?.pausePlay()   //音乐暂停播放
            mConn?.let { unbindService(it) }   ////解绑服务
        }
    }

    //这一行注解是保证API在KITKAT以上的模拟器才能顺利运行，也就是19以上
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_play -> {   //播放按钮点击事件
                val position: String = mIntent1?.getStringExtra("position") ?: ""
                val i: Int = position.toInt()
                mMusicControl?.play(i)
                mAnimator?.start()
            }
            R.id.btn_pause -> {   //暂停按钮点击事件
                mMusicControl?.pausePlay()
                mAnimator?.pause()
            }
            R.id.btn_continue_play -> {      //继续播放按钮点击事件
                mMusicControl?.continuePlay()
                mAnimator?.start()
            }
            R.id.btn_exit -> {   //退出按钮点击事件
                unbind(mIsUnbind)  //解绑服务
                mIsUnbind = true
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbind(mIsUnbind)  //解绑服务
    }
}