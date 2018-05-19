package app.bughunt.medhealthcare.base

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity




open class BaseActivity : AppCompatActivity() {

    private  var alertDialog: AlertDialog.Builder? = null
    private var mProgressDialog: ProgressDialog?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    public fun showDialog(txt: String) {

        if (alertDialog == null) {

       alertDialog = AlertDialog.Builder(this@BaseActivity)
               .setMessage(txt).setTitle("Warning").setNegativeButton("Close") { dialog, _ ->

                   dialog.dismiss()
               }

        }


    }



    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.setMessage("Loading...")
            mProgressDialog!!.isIndeterminate = true
        }

        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }
}