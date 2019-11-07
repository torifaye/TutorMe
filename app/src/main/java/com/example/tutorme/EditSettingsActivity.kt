package com.example.tutorme

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tutorme.databinding.ActivityEditSettingsBinding
import com.example.tutorme.models.Student
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.tutorme.R
import com.example.tutorme.swipe_view.SwipeActivity
import kotlinx.android.synthetic.main.activity_edit_settings.*

private const val TAG = "editsettings"
class EditSettingsActivity : AppCompatActivity() {

    // Using view-binding from arch-components (requires Android Studio 3.6 Canary 11+)
    private lateinit var binding: ActivityEditSettingsBinding
    private lateinit var theSchool: String

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d("EditSettingsActivity", "Created EditSettingsActivity")
        super.onCreate(savedInstanceState)
        binding = ActivityEditSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = FirebaseFirestore.getInstance()

        theSchool = "default"

        val thisIntent = intent
        if (thisIntent.getStringExtra("school") != null) {
            theSchool = thisIntent.getStringExtra("school")!!
            binding.editSettingsSchool.text = theSchool
        }

        val student =
            db.collection("students").document(FirebaseAuth.getInstance().currentUser!!.uid)
        var oldSettings: Student?
//        var userExists = false
        student.get().addOnSuccessListener {
            oldSettings = it.toObject(Student::class.java)
            binding.editSettingsEmail.setText(FirebaseAuth.getInstance().currentUser?.email)
            binding.editSettingsFirstName.setText(oldSettings?.first_name)
            binding.editSettingsLastName.setText(oldSettings?.last_name)
            binding.editSettingsProfilePic.setText(oldSettings?.profile_picture_url)
            if(oldSettings != null){
                theSchool = oldSettings?.school.toString()
                binding.editSettingsSchool.text = oldSettings?.school
                binding.editSchoolBtn.isEnabled = false
            }
        }

        binding.selectPhotoEditSettings.setOnClickListener {
            Log.d(TAG, "try to select photo")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        binding.editSchoolBtn.setOnClickListener {
                // Redirects back to the tutor list page after saving
                val intent = Intent(this, SchoolListActivity::class.java)
                startActivity(intent)
        }

        binding.editSettingsSaveButton.setOnClickListener {

            println("Thing1: ${binding.editSettingsSchool.text}\n" +
                    "Thing2: ${theSchool}")

            // If the school hasn't been selected or info is missing, refuse the save
            if(theSchool == "default" || binding.editSettingsFirstName.length() == 0){
                Toast.makeText(this, "Please make sure to select your school and " +
                        "enter your name!", Toast.LENGTH_SHORT).show()
            } else {
                // Prepares the settings based on the fields
                val settings = hashMapOf(
                    "id" to FirebaseAuth.getInstance().currentUser!!.uid,
                    "first_name" to binding.editSettingsFirstName.text.toString(),
                    "last_name" to binding.editSettingsLastName.text.toString(),
                    "profile_picture_url" to binding.editSettingsProfilePic.text.toString(),
                    "school" to binding.editSettingsSchool.text.toString()
                )

                // Adds or updates the document to the students collection based on the login email used
                db.collection("students").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .set(settings)

                // Redirects back to the tutor list page after saving
                val intent = Intent(this, SwipeActivity::class.java)
                startActivity(intent)
            }

            //TODO: Update vs. Create (Currently works fine as is, maybe change for NFR Checkpoint)
//            if(!userExists){
//                Log.d("DEBUG", "Should have created user")
//                db.collection("students").document(FirebaseAuth.getInstance().currentUser!!.uid).set(settings).addOnSuccessListener { Log.d("Document", "Successfully created user") }
//            } else {
//                db.collection("students").document(FirebaseAuth.getInstance().currentUser!!.uid).set(settings, SetOptions.merge())
//            }


        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val bitmapDrawable = BitmapDrawable(bitmap)
            profilePicImgView.setBackgroundDrawable(bitmapDrawable)
            select_photo_edit_settings.alpha = 0f
        }
    }
    override fun onResume() {
        val thisIntent = intent
        if (thisIntent.getStringExtra("school") != null) {
            theSchool = thisIntent.getStringExtra("school")!!
            binding.editSettingsSchool.text = theSchool
        }
        super.onResume()

    }
}
