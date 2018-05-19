package app.bughunt.medhealthcare

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import app.bughunt.medhealthcare.adapter.ChatAdapter
import app.bughunt.medhealthcare.base.BaseActivity
import app.bughunt.medhealthcare.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import siclo.com.ezphotopicker.api.EZPhotoPick
import siclo.com.ezphotopicker.api.EZPhotoPickStorage
import siclo.com.ezphotopicker.api.models.EZPhotoPickConfig
import siclo.com.ezphotopicker.api.models.PhotoSource
import java.io.File
import java.io.IOException


class MainActivity : BaseActivity() {

    private var rvChat: RecyclerView? = null
    private var etMessage: EditText? = null
    private var btnAddPhoto: ImageButton? = null
    private var btnSendMessage: Button? = null
    private var messageList: MutableList<Message>? = null
    private var chatAdapter: ChatAdapter? = null

    private var user: FirebaseUser? = null
    private var messageReference: DatabaseReference? = null
    private var storageReference: StorageReference? = null
    private var mUserMessageDatabase: DatabaseReference? = null


    private lateinit var bottomSheetView: View
    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        bindView()
        setupView()
        setupBottomSheet()
        setupGoogleApiClient()
        setupFirebaseAuth()
        setupFirebaseDatabase()
        setupFirebaseStorage()
        loadChat()

    }

    private fun bindView() {

        rvChat = findViewById(R.id.rv_chat)
        etMessage = findViewById(R.id.et_message)
        btnAddPhoto = findViewById(R.id.btn_add_photo)
        btnSendMessage = findViewById(R.id.btn_send_message)


    }

    private fun setupView() {
        rvChat?.layoutManager = LinearLayoutManager(this);

        btnSendMessage?.setOnClickListener({

            sendMessageToFirebase(etMessage?.text.toString(), Message.TYPE_TEXT)
            etMessage?.setText("")

        })
        btnAddPhoto?.setOnClickListener({

            showBottomSheet()

        })

    }

    private fun setupFirebaseStorage() {

        val storage = FirebaseStorage.getInstance()
        storageReference = storage.reference


    }

    private fun setupFirebaseDatabase() {
        val database = FirebaseDatabase.getInstance()
        messageReference = database.getReference("messages")
        mUserMessageDatabase = database?.getReference("user-post")
    }

    private fun setupFirebaseAuth() {
        user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user?.uid
            setupChat(uid)
        } else {
            goToLoginScreen()
        }

    }

    private fun goToLoginScreen() = startActivity(Intent(this@MainActivity, LoginActivity::class.java))

    private fun setupChat(uid: String?) {
        chatAdapter = ChatAdapter(uid!!)
        rvChat?.adapter = chatAdapter
        messageList = ArrayList()
        chatAdapter!!.setMessage(messageList)

    }

    private fun setupGoogleApiClient() {


    }

    private fun loadChat() {
        showProgressDialog()
        messageReference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                hideProgressDialog()
                dataSnapshot.ref.removeEventListener(this)
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgressDialog()
                //showAlert(R.string.chat_initialize_message_failure)
            }
        })
        messageReference?.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, s: String?) {
                val messages = dataSnapshot?.getValue(Message::class.java)
                //messageList?.add(dataSnapshot?.getValue(Message::class.java)!!)
                messageList?.add(messages!!)

                chatAdapter?.setMessage(messageList)
                chatAdapter?.notifyDataSetChanged()
                rvChat?.smoothScrollToPosition(chatAdapter?.itemCount!!)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun sendMessageToFirebase(text: String, type: String) {

        if (user != null) {
            val databaseReference = messageReference?.push()
            val mUserMessage = mUserMessageDatabase?.child(user?.uid)
            val message = Message(avatar = user?.photoUrl.toString(), message = text, type = type, senderId = user?.uid, userName = user?.displayName)
            mUserMessage?.push()?.setValue(message)
            databaseReference?.setValue(message)
        } else {

            goToLoginScreen()

        }
    }

    private fun setupBottomSheet() {
        bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog = BottomSheetDialog(this@MainActivity)

        bottomSheetDialog.apply {

            bottomSheetDialog.setContentView(bottomSheetView)
        }

    }

    private fun showBottomSheet() {


        bottomSheetView.findViewById<TextView>(R.id.menu_bottom_sheet_camera).setOnClickListener({
            EZPhotoPick.startPhotoPickActivity(this, chooseCamera())
            bottomSheetDialog.cancel()

        })

        bottomSheetView.findViewById<TextView>(R.id.menu_bottom_sheet_gallery).setOnClickListener({
            EZPhotoPick.startPhotoPickActivity(this, chooseImage())
            bottomSheetDialog.cancel()
        })

        bottomSheetDialog.show()
    }

    private fun chooseImage() = EZPhotoPickConfig().apply {
        photoSource = PhotoSource.GALLERY
        exportingSize = 900
        exportedPhotoName = "IMG_" + System.currentTimeMillis().toString()

    }


    private fun chooseCamera() = EZPhotoPickConfig().apply {

        photoSource = PhotoSource.CAMERA
        exportingSize = 900
        exportedPhotoName = "IMG_" + System.currentTimeMillis().toString()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EZPhotoPick.PHOTO_PICK_GALLERY_REQUEST_CODE ||
                requestCode == EZPhotoPick.PHOTO_PICK_CAMERA_REQUEST_CODE &&
                resultCode == Activity.RESULT_OK) {
            try {

                //   val pickedPhoto: Bitmap = EZPhotoPickStorage(this).loadLatestStoredPhotoBitmap()


                var photoName = data?.getStringExtra(EZPhotoPick.PICKED_PHOTO_NAME_KEY)!!
                var photoPath = EZPhotoPickStorage(this).getAbsolutePathOfStoredPhoto("", photoName)

                showProgressDialog()
                uploadFromFile(photoPath)

            } catch (e: IOException) {
                e.printStackTrace()
                // onChoosePhotoFailure(e.toString())
            }
        }
    }


    private fun uploadFromFile(path: String) {
        val file = Uri.fromFile(File(path))
        val imageRef = storageReference?.child(file.lastPathSegment)
        val mUploadTask = imageRef?.putFile(file)

        mUploadTask?.addOnFailureListener({
            // mView?.upLoadImageUnSuccess(it.message!!)
            hideProgressDialog()
            Log.d("FirebaseLog", it.message)

        })?.addOnSuccessListener({
            //mView?.onHideProgressDialog()
            //this.postMessage?.link = it.downloadUrl.toString()
            hideProgressDialog()
            //   mView?.upLoadImageSuccess(it.uploadSessionUri.toString())
            Log.d("FirebaseLog", it.metadata!!.path)
            Log.d("FirebaseLog", it.downloadUrl.toString())

            sendMessageToFirebase(it.downloadUrl.toString(), Message.TYPE_IMAGE)

        })

    }

}







