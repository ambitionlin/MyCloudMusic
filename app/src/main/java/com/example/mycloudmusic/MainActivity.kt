package com.example.mycloudmusic

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    //1.创建需要用到的控件
    private var mTextView1: TextView? = null
    private var mTextView2: TextView? = null
    private var mFragmentManager: FragmentManager? = null
    private var mFragmentTransaction: FragmentTransaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //2.绑定控件
        mTextView1 = findViewById(R.id.menu1)
        mTextView2 = findViewById(R.id.menu2)
        //3.创建监听器
        mTextView1?.setOnClickListener(this)
        mTextView2?.setOnClickListener(this)

        //若是继承FragmentActivity，  mFragmentManager = getFragmentManager();
        mFragmentManager = supportFragmentManager

        //mFragmentManager可以理解为Fragment显示的管理者，mFragmentTransaction就是它的改变者
        mFragmentTransaction = mFragmentManager?.beginTransaction()

        //默认情况下展示Fragment1
        mFragmentTransaction?.replace(R.id.content, FirstFragment())
        //提交改变的内容
        mFragmentTransaction?.commit()
    }

    //控件的点击事件
    override fun onClick(v: View?) {
        mFragmentTransaction = mFragmentManager?.beginTransaction()
        //切换选项卡
        when (v?.id) {
            R.id.menu1 -> {
                mFragmentTransaction?.replace(R.id.content, FirstFragment())
            }
            R.id.menu2 -> {
                mFragmentTransaction?.replace(R.id.content, SecondFragment())
            }
        }
        mFragmentTransaction?.commit()
    }
}
