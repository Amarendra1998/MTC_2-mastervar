package com.sulitous.mtc

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.TextInputLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CompoundButton
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.support.v7.widget.Toolbar

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.myhexaville.smartimagepicker.ImagePicker
import com.myhexaville.smartimagepicker.OnImagePickedListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.HashMap
import java.util.Locale
import java.util.Random

import io.grpc.Compressor

import io.opencensus.tags.TagValue.MAX_LENGTH

 class AddChildActivity:AppCompatActivity(), CompoundButton.OnCheckedChangeListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

private var mChildDobView:TextView? = null
private var year:Int = 0
private var month:Int = 0
private var day:Int = 0
private var mChildNameLayout:TextInputLayout? = null
private var mChildFatherNameLayout:TextInputLayout? = null
private var mChildMotherNameLayout:TextInputLayout? = null
private var mChildAgeLayout:TextInputLayout? = null
private var mChildPhoneLayout:TextInputLayout? = null
private var mParentSummaryLayout:TextInputLayout? = null
private var mChildWeightLayout:TextInputLayout? = null
private var mChildHeightLayout:TextInputLayout? = null
private var mChildAddressLayout:TextInputLayout? = null
private var mChildGramPanchayatLayout:TextInputLayout? = null
private var mChildDistrictLayout:TextInputLayout? = null
private var mChildBlockLayout:TextInputLayout? = null
private var mParentVisitLayout:TextInputLayout? = null
private var mChildGenderView:Spinner? = null
private var mChildBplView:Switch? = null
private var mParentSupport:Switch? = null
private var mRootRef:FirebaseFirestore? = null
private var isEdit = false
private var isImageTaken = false
private var key:String? = null
private var timeStamp:String? = null
private var imagePicker:ImagePicker? = null
private var mImage:OnImagePickedListener? = null
private var mImageView:ImageView? = null
private var mStorageRootRef:StorageReference? = null

private var mGoogleApiClient:GoogleApiClient? = null
private var mFusedLocationClient:FusedLocationProviderClient? = null
private var mLocationCallback:LocationCallback? = null
private var latitude = 0.0
private var longitude = 0.0
private var toolbar:Toolbar? = null
private var mprogressdialogue:ProgressDialog? = null
internal var thumb_byte:ByteArray? = null

private val myDateListener = object:DatePickerDialog.OnDateSetListener {
public override fun onDateSet(arg0:DatePicker, year:Int, month:Int, day:Int) {
showDate(year, month + 1, day)
}
}
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
protected override fun onCreate(savedInstanceState:Bundle?) {
super.onCreate(savedInstanceState)
setContentView(R.layout.activity_add_child)
toolbar = findViewById<View>(R.id.myappbar) as Toolbar
setSupportActionBar(toolbar)
getSupportActionBar()!!.setTitle("Add Child Information")
getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
mChildDobView = findViewById<TextView>(R.id.child_dob)
mprogressdialogue = ProgressDialog(this)
mChildGenderView = findViewById<Spinner>(R.id.child_gender)
mChildBplView = findViewById<Switch>(R.id.child_bpl)
mChildNameLayout = findViewById<TextInputLayout>(R.id.child_name_layout)
mChildFatherNameLayout = findViewById<TextInputLayout>(R.id.father_layout)
mChildMotherNameLayout = findViewById<TextInputLayout>(R.id.mother_layout)
mChildAgeLayout = findViewById<TextInputLayout>(R.id.age_layout)
mChildPhoneLayout = findViewById<TextInputLayout>(R.id.phone_layout)
mChildWeightLayout = findViewById<TextInputLayout>(R.id.weight_layout)
mChildHeightLayout = findViewById<TextInputLayout>(R.id.height_layout)
mChildAddressLayout = findViewById<TextInputLayout>(R.id.address_layout)
mChildGramPanchayatLayout = findViewById<TextInputLayout>(R.id.gram_panchayat_layout)
mChildBlockLayout = findViewById<TextInputLayout>(R.id.block_layout)
mChildDistrictLayout = findViewById<TextInputLayout>(R.id.district_layout)
mParentSummaryLayout = findViewById<TextInputLayout>(R.id.summary_layout)
mParentVisitLayout = findViewById<TextInputLayout>(R.id.visited_time_layout)
mParentSupport = findViewById<Switch>(R.id.parentSupport)
mImageView = findViewById<ImageView>(R.id.add_child_imageView)
val mChildSaveView = findViewById<Button>(R.id.child_save)

val calendar = Calendar.getInstance()
year = calendar.get(Calendar.YEAR)

month = calendar.get(Calendar.MONTH)
day = calendar.get(Calendar.DAY_OF_MONTH)

mChildDobView!!.setOnClickListener(object:View.OnClickListener {
public override fun onClick(v:View) {
onCreateDialog(999)!!.show()
}
})
mChildSaveView.setOnClickListener(object:View.OnClickListener {
public override fun onClick(v:View) {
saveChildDetails()
}
})
showGenderSpinner()
if (getSupportActionBar() != null)
{
getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
}
mRootRef = FirebaseFirestore.getInstance()

mImage = object:OnImagePickedListener {
public override fun onImagePicked(imageUri:Uri) {
previewCapturedImage(imageUri)
}
}
mGoogleApiClient = GoogleApiClient.Builder(this@AddChildActivity)
.addApi(LocationServices.API)
.addConnectionCallbacks(this@AddChildActivity)
.addOnConnectionFailedListener(this@AddChildActivity).build()
mGoogleApiClient!!.connect()

if (getIntent().hasExtra("CHILD"))
{
if (getSupportActionBar() != null)
{
getSupportActionBar()!!.setTitle("Edit Child Details")
}
val SChild = getIntent().getStringExtra("CHILD")
val gson = Gson()
val child = gson.fromJson<Child>(SChild, Child::class.java!!)
key = child.key
mChildBplView!!.setChecked(child.isBpl)
mChildGenderView!!.setSelection(child.gender + 1)
mChildDobView!!.setText(child.dob)
mChildNameLayout!!.getEditText()!!.setText(child.name)
mChildFatherNameLayout!!.getEditText()!!.setText(child.father)
mChildMotherNameLayout!!.getEditText()!!.setText(child.mother)
mChildAgeLayout!!.getEditText()!!.setText((child.age).toString())
mChildPhoneLayout!!.getEditText()!!.setText(child.phone)
mChildWeightLayout!!.getEditText()!!.setText((child.weight).toString())
mChildHeightLayout!!.getEditText()!!.setText((child.height).toString())
mChildDistrictLayout!!.getEditText()!!.setText(child.district)
mChildBlockLayout!!.getEditText()!!.setText(child.block)
mChildGramPanchayatLayout!!.getEditText()!!.setText(child.gramPanchayat)
mChildAddressLayout!!.getEditText()!!.setText(child.address)

mChildSaveView.setText(R.string.update)
isEdit = true

mParentSupport!!.setVisibility(View.VISIBLE)
mParentSupport!!.setOnCheckedChangeListener(this)
mParentSupport!!.setChecked(!child.isParentSupport)
if (!child.isParentSupport)
{
mParentVisitLayout!!.getEditText()!!.setText((child.visitCount).toString())
mParentSummaryLayout!!.getEditText()!!.setText(child.parentSummary)
}
}
else
{
mImageView!!.setOnClickListener(object:View.OnClickListener {
public override fun onClick(view:View) {
imagePicker = ImagePicker(this@AddChildActivity, null, mImage)
imagePicker!!.setWithImageCrop(1, 1)
imagePicker!!.choosePicture(true)
                }
})
mStorageRootRef = FirebaseStorage.getInstance().getReference()
if (checkPlayServices())
{
if (ActivityCompat.checkSelfPermission(this@AddChildActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@AddChildActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
{
askForPermission(LOCATION_FINE, LOCATION_FINE_CODE)
}
else
{
createLocationRequest()
}
}
}

}

private fun askForPermission(location:String, locationCode:Int) {
if (ContextCompat.checkSelfPermission(this@AddChildActivity, location) != PackageManager.PERMISSION_GRANTED)
{

 // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@AddChildActivity, location))
{

 //This is called if mUser has denied the location before
                //In this case I am just asking the location again
                ActivityCompat.requestPermissions(this@AddChildActivity, arrayOf<String>(location), locationCode)

}
else
{

ActivityCompat.requestPermissions(this@AddChildActivity, arrayOf<String>(location), locationCode)
}
}
else
{
Toast.makeText(this@AddChildActivity, "" + location + " is already granted.", Toast.LENGTH_SHORT).show()
}
}

public override fun onRequestPermissionsResult(requestCode:Int, permissions:Array<String>, grantResults:IntArray) {
super.onRequestPermissionsResult(requestCode, permissions, grantResults)
if (ActivityCompat.checkSelfPermission(this@AddChildActivity, permissions[0]) == PackageManager.PERMISSION_GRANTED)
{
when (requestCode) {

1 -> createLocationRequest()
}

Toast.makeText(this@AddChildActivity, "Permission granted", Toast.LENGTH_SHORT).show()
}
else
{
Toast.makeText(this@AddChildActivity, "Give Location Permission", Toast.LENGTH_SHORT).show()
}
}

protected fun createLocationRequest() {
@SuppressLint("RestrictedApi") val mLocationRequest = LocationRequest()
mLocationRequest.setInterval(10000)
mLocationRequest.setFastestInterval(5000)
mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

val builder = LocationSettingsRequest.Builder()
.addLocationRequest(mLocationRequest)

 // **************************
        builder.setAlwaysShow(true)

val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
result.addOnCompleteListener(object:OnCompleteListener<LocationSettingsResponse> {
public override fun onComplete(task:Task<LocationSettingsResponse>) {
try
{
val response = task.getResult<ApiException>(ApiException::class.java!!)
 // All location settings are satisfied. The client can initialize location
                    // requests here.
                }
catch (exception:ApiException) {
when (exception.getStatusCode()) {
LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
 // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try
{
 // Cast to a resolvable exception.
                                val resolvable = exception as ResolvableApiException
 // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
this@AddChildActivity, 7334)
}
catch (e:IntentSender.SendIntentException) {
 // Ignore the error.
                            }
catch (e:ClassCastException) {
 // Ignore, should be an impossible error.
                            }

LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
}// Location settings are not satisfied. However, we have no way to fix the
 // settings so we won't show the dialog.
}

}
})

}

private fun checkPlayServices():Boolean {
val googleAPI = GoogleApiAvailability.getInstance()
val resultCode = googleAPI.isGooglePlayServicesAvailable(this@AddChildActivity)
if (resultCode != ConnectionResult.SUCCESS)
{
if (googleAPI.isUserResolvableError(resultCode))
{
googleAPI.getErrorDialog(this@AddChildActivity, resultCode,
PLAY_SERVICES_RESOLUTION_REQUEST).show()
}
else
{
Toast.makeText(this@AddChildActivity.getApplicationContext(),
"This device is not supported.", Toast.LENGTH_LONG)
.show()
}
return false
}
return true
}

private fun previewCapturedImage(imageUri:Uri) {
isImageTaken = true
mImageView!!.setImageURI(imageUri)
timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}

private fun saveChildDetails() {
var addChild = true
val childName = mChildNameLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })
var childFather = mChildFatherNameLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })
var childMother = mChildMotherNameLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })
val childDob = mChildDobView!!.getText().toString().trim({ it <= ' ' })
val childAge:Int
if (TextUtils.isEmpty(mChildAgeLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })))
{
childAge = 0
}
else
{
 childAge = try {
  Integer.valueOf(mChildAgeLayout!!.getEditText()!!.getText().toString().trim{ it <= ' ' })
 } catch (e:NumberFormatException) {
  -1
 }

}
val childGender = mChildGenderView!!.getSelectedItemPosition() - 1
val childPhone = mChildPhoneLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })
val childBpl = mChildBplView!!.isChecked()
val childWeight:Double
if (TextUtils.isEmpty(mChildWeightLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })))
{
childWeight = 0.0
}
else
{
 childWeight = try {
  java.lang.Double.valueOf(mChildWeightLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' }))!!
 } catch (e:NumberFormatException) {
  -1.0
 }

}
val childHeight:Double
if (TextUtils.isEmpty(mChildHeightLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })))
{
childHeight = 0.0
}
else
{
 childHeight = try {
  java.lang.Double.valueOf(mChildHeightLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' }))!!
 } catch (e:NumberFormatException) {
  -1.0
 }

}
val childAddress = mChildAddressLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })
val childGramPanchayat = mChildGramPanchayatLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })
val childBlock = mChildBlockLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })
val childDistrict = mChildDistrictLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })
val sharedPreferences = getSharedPreferences(getString(R.string.shared_file_user), Context.MODE_PRIVATE)
val childMTC = sharedPreferences.getString(getString(R.string.centre_key_shared), "")
val childTreatment = 0

if (TextUtils.isEmpty(childName))
{
mChildNameLayout!!.setError(getString(R.string.field_required))
}
else
{
mChildNameLayout!!.setError(null)
}

if (isNameValid(childName))
{
mChildNameLayout!!.setError(getString(R.string.invalid_name))
}
else
{
mChildNameLayout!!.setError(null)
}

if (TextUtils.isEmpty(childFather) && TextUtils.isEmpty(childMother))
{
mChildFatherNameLayout!!.setError(getString(R.string.field_required))
}
else
{
mChildFatherNameLayout!!.setError(null)
}

if (isNameValid(childFather) && isNameValid(childMother))
{
mChildFatherNameLayout!!.setError(getString(R.string.invalid_parent))
}
else
{
mChildFatherNameLayout!!.setError(null)
}

if (childAge == 0)
{
mChildAgeLayout!!.setError(getString(R.string.field_required))
}
else
{
mChildAgeLayout!!.setError(null)
}

if (childAge == -1 || !isAgeValid(childAge))
{
mChildAgeLayout!!.setError(getString(R.string.invalid_age))
}
else
{
mChildAgeLayout!!.setError(null)
}

if (childGender == -1)
{
addChild = false
Toast.makeText(this@AddChildActivity, "Select Gender", Toast.LENGTH_SHORT).show()
}

if (TextUtils.isEmpty(childPhone))
{
mChildPhoneLayout!!.setError(getString(R.string.field_required))
}
else
{
mChildPhoneLayout!!.setError(null)
}

if (!isPhoneNumberValid(childPhone))
{
mChildPhoneLayout!!.setError(getString(R.string.invalid_phone))
}
else
{
mChildPhoneLayout!!.setError(null)
}

if (childWeight == 0.0)
{
mChildWeightLayout!!.setError(getString(R.string.field_required))
}
else
{
mChildWeightLayout!!.setError(null)
}

if (childWeight == -1.0)
{
mChildWeightLayout!!.setError(getString(R.string.invalid_weight))
}
else
{
mChildWeightLayout!!.setError(null)
}

if (childHeight == 0.0)
{
mChildHeightLayout!!.setError(getString(R.string.field_required))
}
else
{
mChildHeightLayout!!.setError(null)
}

if (childHeight == -1.0)
{
mChildHeightLayout!!.setError(getString(R.string.invalid_height))
}
else
{
mChildHeightLayout!!.setError(null)
}

if (TextUtils.isEmpty(childAddress))
{
mChildAddressLayout!!.setError(getString(R.string.field_required))
}
else
{
mChildAddressLayout!!.setError(null)
}

if (TextUtils.isEmpty(childGramPanchayat))
{
mChildGramPanchayatLayout!!.setError(getString(R.string.field_required))
}
else
{
mChildGramPanchayatLayout!!.setError(null)
}

if (TextUtils.isEmpty(childBlock))
{
mChildBlockLayout!!.setError(getString(R.string.field_required))
}
else
{
mChildBlockLayout!!.setError(null)
}

if (TextUtils.isEmpty(childDistrict))
{
mChildDistrictLayout!!.setError(getString(R.string.field_required))
}
else
{
mChildDistrictLayout!!.setError(null)
}

val visitedTime:String
val parentSummary:String

if (mParentSupport!!.isChecked())
{
visitedTime = mParentVisitLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })
parentSummary = mParentSummaryLayout!!.getEditText()!!.getText().toString().trim({ it <= ' ' })
}
else
{
visitedTime = "0"
parentSummary = "nah"
}

if (mParentSupport!!.isChecked())
{
if (TextUtils.isEmpty(visitedTime))
{
mParentVisitLayout!!.setError(getString(R.string.field_required))
}
else if (!TextUtils.isDigitsOnly(visitedTime))
{
mParentVisitLayout!!.setError(getString(R.string.invalid_number))
}
else
{
mParentVisitLayout!!.setError(null)
}

if (TextUtils.isEmpty(parentSummary))
{
mParentSummaryLayout!!.setError(getString(R.string.field_required))
}
else if (parentSummary.length <= 10)
{
mParentSummaryLayout!!.setError(getString(R.string.more_details))
}
else
{
mParentSummaryLayout!!.setError(null)
}
}
else
{
mParentVisitLayout!!.setError(null)
mParentSummaryLayout!!.setError(null)
}

if (addChild)
{
if (childFather.isEmpty())
{
childFather = "null"
}
if (childMother.isEmpty())
{
childMother = "null"
}
if ((mChildNameLayout!!.getError() == null && mChildFatherNameLayout!!.getError() == null && mChildMotherNameLayout!!.getError() == null && mChildAgeLayout!!.getError() == null && mChildPhoneLayout!!.getError() == null && mParentSummaryLayout!!.getError() == null && mParentVisitLayout!!.getError() == null &&
mChildWeightLayout!!.getError() == null && mChildHeightLayout!!.getError() == null && mChildAddressLayout!!.getError() == null && mChildGramPanchayatLayout!!.getError() == null && mChildDistrictLayout!!.getError() == null && mChildBlockLayout!!.getError() == null))
{
if (isEdit)
{
childAdd(childName, childFather, childMother, childDob, childAge, childGender, childPhone, childBpl, childWeight, childHeight, childAddress, childGramPanchayat, childBlock, childDistrict, childMTC, childTreatment, parentSummary, visitedTime)
}
else
{
if (isImageTaken)
{
if (latitude == 0.0 || longitude == 0.0)
{
Toast.makeText(this, "Give location permission and turn on GPS", Toast.LENGTH_SHORT).show()
}
else
{
childAdd(childName, childFather, childMother, childDob, childAge, childGender, childPhone, childBpl, childWeight, childHeight, childAddress, childGramPanchayat, childBlock, childDistrict, childMTC, childTreatment, parentSummary, visitedTime)
}
}
else
{
Toast.makeText(this, "Add Child Image also", Toast.LENGTH_SHORT).show()
}
}

}
}
else
{
Toast.makeText(this, "Check the errors", Toast.LENGTH_SHORT).show()
}
}

private fun childAdd(childName:String, childFather:String, childMother:String, childDob:String, childAge:Int, childGender:Int,
childPhone:String, childBpl:Boolean, childWeight:Double, childHeight:Double, childAddress:String, childGramPanchayat:String,
childBlock:String, childDistrict:String, childMTC:String?, childTreatment:Int, parentSummary:String, visitCount:String) {
val showDialog = ShowDialog(this@AddChildActivity)
val child = Child()
if (isEdit)
{
showDialog.setTitle("Updating Child Details")
}
else
{
showDialog.setTitle("Adding Child Details")
key = mRootRef!!.collection("Child Details").document().getId()
mImageView!!.setDrawingCacheEnabled(true)
mImageView!!.buildDrawingCache()
val bitmap = mImageView!!.getDrawingCache()
val byteArrayOutputStream = ByteArrayOutputStream()
bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
val data = byteArrayOutputStream.toByteArray()
val mStorage = mStorageRootRef!!.child(key!!).child("IMG_" + timeStamp + ".jpg")
val imageUrl = mStorage.getPath()
mStorage.putBytes(data).addOnSuccessListener(object:OnSuccessListener<UploadTask.TaskSnapshot> {
public override fun onSuccess(taskSnapshot:UploadTask.TaskSnapshot) {
Toast.makeText(this@AddChildActivity, "Image Uploaded", Toast.LENGTH_SHORT).show()
}
})
child.imageUrl=imageUrl
child.latitude=latitude
child.longitude=longitude
}
showDialog.setMessage("Uploading Data...")
showDialog.show()
child.name=childName
child.father=childFather
child.mother=childMother
child.dob=childDob
child.age=childAge
child.gender=childGender
child.phone=childPhone
child.isBpl=childBpl
child.weight=childWeight
child.height=childHeight
child.address=childAddress
child.gramPanchayat=childGramPanchayat
child.block=childBlock
child.district=childDistrict
child.mtc=childMTC
child.treatment=childTreatment
child.visitCount=Integer.valueOf(visitCount)
child.parentSummary=parentSummary
if (isEdit)
{
child.isParentSupport=!mParentSupport!!.isChecked()
}
else
{
child.isParentSupport=true
}

mRootRef!!.collection("ChildDetails").document(key!!).set(child).addOnSuccessListener(object:OnSuccessListener<Void> {
public override fun onSuccess(aVoid:Void) {
val user = FirebaseAuth.getInstance().getCurrentUser()
if (isEdit)
{
val childEditHashMap = HashMap<String, Any>()
childEditHashMap.put("edited", FieldValue.serverTimestamp())
mRootRef!!.collection("ChildDetails").document(key!!).set(childEditHashMap, SetOptions.merge())
}
else
{
val childAddHashMap = HashMap<String, Any>()
childAddHashMap.put("added", FieldValue.serverTimestamp())
val pushHashMap = HashMap<String, Any>()
pushHashMap.put(key!!, FieldValue.serverTimestamp())
assert(user != null)
mRootRef!!.collection("WaitingList").document(user!!.getUid()).set(pushHashMap, SetOptions.merge())
mRootRef!!.collection("ChildDetails").document(key!!).set(childAddHashMap, SetOptions.merge())
mRootRef!!.collection("Treatment").document(user!!.getUid()).set(pushHashMap, SetOptions.merge())
}
showDialog.cancel()
Toast.makeText(this@AddChildActivity, "Successfully added", Toast.LENGTH_SHORT).show()
switchToMainActivity()
}
}).addOnFailureListener(object:OnFailureListener {
public override fun onFailure(e:Exception) {
showDialog.cancel()
Toast.makeText(this@AddChildActivity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show()
}
})
}

private fun switchToMainActivity() {
finish()
}

private fun isNameValid(childName:String):Boolean {
return childName.length < 3
}

private fun isAgeValid(childAge:Int):Boolean {
return childAge < 60
}

private fun isPhoneNumberValid(childPhone:String):Boolean {
return childPhone.length == 10 && (childPhone.startsWith("6") || childPhone.startsWith("7") || childPhone.startsWith("8") || childPhone.startsWith("9"))
}
private fun showGenderSpinner() {
val problem:Array<String>
problem = arrayOf<String>("Select Gender", "Male", "Female")

val problems = ArrayAdapter<String>(this@AddChildActivity, android.R.layout.simple_spinner_dropdown_item, problem)

mChildGenderView!!.setAdapter(problems)
problems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
}

protected override fun onCreateDialog(id:Int):Dialog? {
if (id == 999)
{
return DatePickerDialog(this@AddChildActivity,
myDateListener, year, month, day)
}
return null
}

private fun showDate(year:Int, month:Int, day:Int) {
mChildDobView!!.setText(StringBuilder().append(day).append("/")
.append(month).append("/").append(year))

val startCalendar = GregorianCalendar()
val endCalendar = GregorianCalendar()

val c = Calendar.getInstance()

val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
val formattedDate = df.format(c.getTime())
try
{
val today = df.parse(formattedDate)
endCalendar.setTime(today)
val born = df.parse(mChildDobView!!.getText().toString())
startCalendar.setTime(born)
}
catch (e:ParseException) {
e.printStackTrace()
}

val diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
val diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)
mChildAgeLayout!!.getEditText()!!.setText((diffMonth).toString())
}

public override fun onCheckedChanged(compoundButton:CompoundButton, isChecked:Boolean) {
if (isChecked)
{
mParentSummaryLayout!!.setVisibility(View.VISIBLE)
mParentVisitLayout!!.setVisibility(View.VISIBLE)
}
else
{
mParentSummaryLayout!!.setVisibility(View.GONE)
mParentVisitLayout!!.setVisibility(View.GONE)
}
}

protected override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent) {
super.onActivityResult(requestCode, resultCode, data)
if (requestCode == 7334)
{
if (resultCode == Activity.RESULT_OK)
{
Toast.makeText(this, "on", Toast.LENGTH_SHORT).show()
}
if (resultCode == Activity.RESULT_CANCELED)
{
finish()
}
}
else
{
imagePicker!!.handleActivityResult(resultCode, requestCode, data)
}
}
public override fun onConnected(bundle:Bundle?) {
updateLocation()
}

private fun updateLocation() {
mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
val mLocationRequest = LocationRequest.create()
.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
.setInterval(5000)
if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
{

return
}

mLocationCallback = object:LocationCallback() {
public override fun onLocationResult(locationResult:LocationResult?) {
for (location in locationResult!!.getLocations())
{
latitude = location.getLatitude()
longitude = location.getLongitude()
}
}
}
LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback!!, null)
}

public override fun onConnectionSuspended(i:Int) {

}

public override fun onConnectionFailed(connectionResult:ConnectionResult) {

}

protected override fun onPause() {
super.onPause()
if (mGoogleApiClient!!.isConnected() || mGoogleApiClient!!.isConnecting())
{
mGoogleApiClient!!.disconnect()
}
if (mFusedLocationClient != null && mLocationCallback != null)
{
mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
}
}

protected override fun onDestroy() {
super.onDestroy()
if (mGoogleApiClient!!.isConnected() || mGoogleApiClient!!.isConnecting())
{
mGoogleApiClient!!.disconnect()
}
if (mFusedLocationClient != null && mLocationCallback != null)
{
mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
}
}

companion object {

private val PLAY_SERVICES_RESOLUTION_REQUEST = 1000
private val LOCATION_FINE = android.Manifest.permission.ACCESS_FINE_LOCATION
private val LOCATION_FINE_CODE = 0x1
private val Gallery_pick = 1
 fun random():String {
val generator = Random()
val randomStringBuilder = StringBuilder()
val randomLength = generator.nextInt(MAX_LENGTH)
var tempChar:Char
for (i in 0 until randomLength)
{
tempChar = (generator.nextInt(96) + 32).toChar()
randomStringBuilder.append(tempChar)
}
return randomStringBuilder.toString()
}
}
}