package com.example.mycloudmusic

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import java.util.*

//这是一个Service服务类
class MusicService : Service() {
    //声明一个MediaPlayer引用
    private var mMediaPlayer: MediaPlayer? = null

    //声明一个计时器引用
    private var mTimer: Timer? = null

    //构造函数
    fun MusicService() {
    }

    override fun onBind(intent: Intent): IBinder {
        //Return the communication channel to the service.
        return MusicControl()
    }

    override fun onCreate() {
        super.onCreate()
        //创建音乐播放器对象
        mMediaPlayer = MediaPlayer()
    }


    //添加计时器用于设置音乐播放器中的播放进度条
    public fun addTimer() {
        //如果timer不存在，也就是没有引用实例
        if (mTimer == null) {
            mTimer = Timer()
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    if (mMediaPlayer == null) return
                    val duration: Int? = mMediaPlayer!!.duration   //获取歌曲总时长
                    val currentPosition: Int? = mMediaPlayer!!.currentPosition   //获取歌曲总进度
                    val msg: Message = MusicActivity.mHandler.obtainMessage()      //创建消息对象

                    //将音乐的总时长和播放进度封装至bundle中
                    val bundle: Bundle = Bundle()
                    duration?.let { bundle.putInt("duration", it) }
                    currentPosition?.let { bundle.putInt("currentPosition", it) }
                    //再将bundle封装到msg消息对象中
                    msg.data = bundle
                    //最后将消息发送到主线程的消息队列
                    MusicActivity.mHandler.sendMessage(msg)
                }
            }
            //开始计时任务后的5毫秒，第一次执行task任务，以后每500毫秒（0.5s）执行一次
            mTimer?.schedule(task, 5, 500);
        }
    }

    //Binder是一种跨进程的通信方式
    inner class MusicControl : Binder() {
        public fun play(i: Int) {
            val uri: Uri =
                Uri.parse("android.resource://" + getPackageName() + "/raw/" + "music" + i)
            try {
                if (mMediaPlayer == null) return
                //重置音乐播放器
                mMediaPlayer!!.reset()
                //加载多媒体文件
                mMediaPlayer = MediaPlayer.create(applicationContext, uri)
                mMediaPlayer!!.start()  //播放音乐
                addTimer()   //添加计时器
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //下面的暂停继续和退出方法全部调用的是MediaPlayer自带的方法
        public fun pausePlay() {
            mMediaPlayer?.pause()   //暂停播放音乐
        }

        public fun continuePlay() {
            mMediaPlayer?.start()    //继续播放音乐
        }

        public fun seekTo(progress: Int) {
            mMediaPlayer?.seekTo(progress)    //设置音乐的播放位置
        }
    }
    //销毁多媒体播放器
    override fun onDestroy() {
        super.onDestroy()
        if (mMediaPlayer == null) return
        if (mMediaPlayer!!.isPlaying) mMediaPlayer!!.stop()   //停止播放音乐
        mMediaPlayer!!.release()        //释放占用的资源
        mMediaPlayer == null    //将mMediaPlayer置为空
    }
}