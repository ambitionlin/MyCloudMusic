package com.example.mycloudmusic

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var mView: View? = null

    //创建歌曲的String数组和歌手图片的int数组
    var name: Array<String?>? = arrayOf<String?>("久石让——寻与千寻", "南京——鼓楼", "晚风")

    companion object {
        var icons = intArrayOf(R.drawable.music0, R.drawable.music1, R.drawable.music2)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //绑定布局，只不过这里是用inflate()方法
        mView = inflater.inflate(R.layout.music_list, container, false)
        //创建listView列表并且绑定控件
        val mListView: ListView? = mView?.findViewById(R.id.list_view)
        //实例化一个适配器
        val adapter: MyBaseAdapter = MyBaseAdapter()
        //列表设置适配器
        mListView?.adapter = adapter
        //列表元素的点击监听器
        mListView?.onItemClickListener =
            object : AdapterView.OnItemClickListener{

                //                    parent, view, position, id ->
//                run {
//                    //创建Intent对象，参数就是从FirstFragment跳转到MusicActivity
//                    val intent: Intent = Intent(this@FirstFragment.context, MusicActivity1.javaClass)
//                    //将歌曲名和歌曲的下标存入Intent对象
//                    intent.putExtra("name", name?.get(position))
//                    intent.putExtra("position", position.toString())
//                    //开始跳转
//                    startActivity(intent)
//                }
                override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    //创建Intent对象，参数就是从FirstFragment跳转到MusicActivity
                    val intent: Intent = Intent(this@FirstFragment.context, MusicActivity::class.java)
                    //将歌曲名和歌曲的下标存入Intent对象
                    intent.putExtra("name", name?.get(position))
                    intent.putExtra("position", position.toString())
                    //开始跳转
                    startActivity(intent)
                }
            }
        return mView
    }

    //这里是创建一个自定义适配器，可以作为模板
    inner class MyBaseAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return name?.size ?: 0
        }

        override fun getItem(position: Int): Any {
            return name?.get(position) ?: ""
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            //绑定好View，然后绑定控件
            val view: View = View.inflate(this@FirstFragment.context, R.layout.item_layout, null)
            val textViewName: TextView = view.findViewById(R.id.item_name)
            val imageView: ImageView = view.findViewById(R.id.image_view)
            //设置控件显示的内容，就是获取的歌曲名和歌手图片
            textViewName.setText(name?.get(position) ?: "歌曲名")
            imageView.setImageResource(icons[position])
            return view
        }

    }
}
