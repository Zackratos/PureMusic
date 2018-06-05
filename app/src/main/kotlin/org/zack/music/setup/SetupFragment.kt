package org.zack.music.setup

//import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import kotlinx.android.synthetic.main.fragment_setup.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.zack.music.BaseFragment
import org.zack.music.LauncherActivity
import org.zack.music.R
import org.zack.music.config.Config
import org.zack.music.config.ConfigHelper
import org.zack.music.event.BgChange
import org.zack.music.event.SeekBarChange
import org.zack.music.tools.AppUtils
import org.zack.music.tools.RxBus
import android.graphics.Color.parseColor
import android.support.v4.content.ContextCompat
import android.text.method.LinkMovementMethod
import android.text.Spanned
import android.text.style.URLSpan
import android.text.SpannableString
import org.zack.music.Constants
import org.zack.music.update.UpdateUtils


/**
 * @author : Zhangwenchao
 * @e-mail : zhangwch@yidianling.com
 * @time   : 2018/5/21
 */
class SetupFragment: BaseFragment() {


    companion object {

        private const val OPEN_ALBUM = 1

        fun newInstance(): SetupFragment {
            return SetupFragment()
        }
    }

    private lateinit var parentActivity: LauncherActivity

    override fun layoutId(): Int {
        return R.layout.fragment_setup
    }

    override fun initEventAndData() {
        parentActivity = activity as LauncherActivity
        setHasOptionsMenu(true)
        rootView?.setOnTouchListener { _, _ -> true }
        setRootPadding()
        toolbar.setTitle(R.string.setup_label)
//        val rootActivity = activity as LauncherActivity
        parentActivity.setSupportActionBar(toolbar)
        val actionBar = parentActivity.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        tv_version.text = String.format(getString(R.string.setup_about_version), AppUtils.getAppVersionName())

        setGithub()

        toolbar.setNavigationOnClickListener {
            parentActivity.removeSetup()
        }

        rg_bg.check(when(ConfigHelper.getInstance().getBgType()) {
            Config.SONGS -> R.id.rb_song
            Config.GIRL -> R.id.rb_girl
            else -> R.id.rb_tran
        })

        sc_seek_tran.isChecked = ConfigHelper.getInstance().isSeekTran()
        sc_notification_control.isChecked = ConfigHelper.getInstance().isNotificationControl()

        rg_bg.setOnCheckedChangeListener { _, checkedId ->
            val type = when(checkedId) {
                R.id.rb_girl -> Config.GIRL
                R.id.rb_song -> Config.SONGS
//                R.id.rb_custom -> Config.CUSTOM
                else -> Config.TRANS
            }
            RxBus.getInstance().post(BgChange(type))
            ConfigHelper.getInstance().putBgType(type)
        }

        sc_seek_tran.setOnCheckedChangeListener { _, isChecked ->
            RxBus.getInstance().post(SeekBarChange(isChecked))
            ConfigHelper.getInstance().putSeekTran(isChecked)
        }

        sc_notification_control.setOnCheckedChangeListener { _, isChecked ->
            parentActivity.playBinder?.setNotificationControl(isChecked)
            ConfigHelper.getInstance().putNotificationControl(isChecked)
        }

        tv_update.setOnClickListener {
            val updateDisposable = UpdateUtils.checkUpdate(fragmentManager, true, true)
            addDisposable(updateDisposable)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_ALBUM && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
        }
    }




    private fun setRootPadding() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return
        val parent = activity as LauncherActivity
        rootView?.setPadding(0, parent.getStatusBarHeight(), 0, parent.getNavigationBarHeight())
    }


    private fun setGithub() {
        val ss = SpannableString(tv_github.text)
        val urlSpan = URLSpan(Constants.GITHUB_ADDRESS)
        ss.setSpan(urlSpan, 6, ss.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        tv_github.movementMethod = LinkMovementMethod.getInstance()
        tv_github.highlightColor = ContextCompat.getColor(activity, R.color.tran_default)
        tv_github.text = ss
    }

    private fun openAlbum() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, OPEN_ALBUM)
    }

    private fun getImagePath(uri: Uri): String? {
        var path: String? = null
        val cursor = activity.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }
}