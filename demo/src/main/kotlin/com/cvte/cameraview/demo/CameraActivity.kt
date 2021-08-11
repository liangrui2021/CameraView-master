package com.cvte.cameraview.demo

import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.*
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
 
import com.luoye.bzyuvlib.BZYUVUtil
 
import com.cvte.cameraview.*
import com.cvte.cameraview.filter.Filters
import com.cvte.cameraview.frame.Frame
import com.cvte.cameraview.frame.FrameProcessor
import com.cvte.easyfloat.EasyFloat
import com.cvte.easyfloat.enums.ShowPattern
import com.cvte.easyfloat.enums.SidePattern
import com.cvte.easyfloat.interfaces.OnTouchRangeListener
import com.cvte.easyfloat.utils.DragUtils
import com.cvte.easyfloat.widget.BaseSwitchView
import com.cvte.encode.CameraVideoHardEncoder
import com.cvte.encode.IEncodeListener
import java.io.*
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class CameraActivity : AppCompatActivity()  ,View.OnClickListener, IEncodeListener {

    companion object {
        public val YUVQueue: ArrayBlockingQueue<ByteArray> = ArrayBlockingQueue<ByteArray>(10)
        private val LOG = CameraLogger.create("DemoApp")
        private const val USE_FRAME_PROCESSOR = true
        private const val DECODE_BITMAP = false
    }

//    private val camera: CameraView by lazy { findViewById(R.id.camera) }
    private val controlPanel: ViewGroup by lazy { findViewById(R.id.controls) }
    private var captureTime: Long = 0
    private lateinit var yuvUtil: BZYUVUtil
    var fileOutputStream: FileOutputStream? = null
    var h264FileOutputStream: FileOutputStream? = null
    var encoder: CameraVideoHardEncoder? = null
    private var currentFilter = 0
    private val allFilters = Filters.values()
    private val SIZE_1920 = 1920
    private val SIZE_1080 = 1080
    private val FPS_30 = 30


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
//        camera.setLifecycleOwner(this)
//        camera.addCameraListener(Listener())
        yuvUtil = BZYUVUtil()
        encoder = CameraVideoHardEncoder(this)
        encoder!!.initEncoder(SIZE_1920, SIZE_1080, 5000000, FPS_30)
//        Handler().postDelayed({
            encoder!!.startEncoder()
//            encoder!!.requestKeyFrame()
//        }, 2000)
        //------------使用硬编替换---------------------
//        avcCodec = AvcEncoder(this, SIZE_1920, SIZE_1080, FPS_30, 8500 * 1000)
//        avcCodec!!.StartEncoderThread()



        val s: String = getFilesDir().getAbsolutePath() + "/test89.yuv"
        try {
            fileOutputStream = FileOutputStream(s)
            val file: File = File(getFilesDir().getAbsolutePath() + "/test123.h264")
            h264FileOutputStream = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        //---------------------------===============
//        if (USE_FRAME_PROCESSOR) {
//            camera.addFrameProcessor(object : FrameProcessor {
//                private var lastTime = System.currentTimeMillis()
//
//                @RequiresApi(Build.VERSION_CODES.KITKAT)
//                override fun process(frame: Frame) {
//                    val newTime = frame.time
//                    val delay = newTime - lastTime
//                    lastTime = newTime
//                    LOG.v("Frame delayMillis:", delay, "FPS:", 1000 / delay)
//                    val image = frame.getData<Image>()
//                    val bytesYUV420 = yuvUtil.preHandleYUV420(image, false, 0)
////                    val byteNv21 = ByteArray(bytesYUV420.size)
////                    BZYUVUtil.yuvI420ToNV21(bytesYUV420, byteNv21, image.width, image.height)
//
//                    Log.i(LOG.toString(), "onImageAvailable: " + image.height + " width: " + image.width);
////                    try {
////                        fileOutputStream?.write(bytesYUV420)
////                    } catch (e: IOException) {
////                        e.printStackTrace();
////                    }
//                    //            Frame frame = getFrameManager().getFrame(image,
////                    System.currentTimeMillis());
//                    addDataToEncoder(bytesYUV420, 1920, 1080)
////                    putYUVData(bytesYUV420,bytesYUV420.size)
//                    image.close()
//
//                }
//            })
//        }
//        findViewById<View>(R.id.edit).setOnClickListener(this)
//        findViewById<View>(R.id.capturePicture).setOnClickListener(this)
//        findViewById<View>(R.id.capturePictureSnapshot).setOnClickListener(this)
//        findViewById<View>(R.id.captureVideo).setOnClickListener(this)
//        findViewById<View>(R.id.captureVideoSnapshot).setOnClickListener(this)
//        findViewById<View>(R.id.toggleCamera).setOnClickListener(this)
        findViewById<View>(R.id.changeFilter).setOnClickListener(this)
//        val group = controlPanel.getChildAt(0) as ViewGroup
//        val watermark = findViewById<View>(R.id.watermark)
//        val options: List<Option<*>> = listOf(
//                // Layout
//                Option.Width(), Option.Height(),
//                // Engine and preview
//                Option.Mode(), Option.Engine(), Option.Preview(),
//                // Some controls
//                Option.Flash(), Option.WhiteBalance(), Option.Hdr(),
//                Option.PictureMetering(), Option.PictureSnapshotMetering(),
//                Option.PictureFormat(),
//                // Video recording
//                Option.PreviewFrameRate(), Option.VideoCodec(), Option.Audio(), Option.AudioCodec(),
//                // Gestures
//                Option.Pinch(), Option.HorizontalScroll(), Option.VerticalScroll(),
//                Option.Tap(), Option.LongTap(),
//                // Watermarks
//                Option.OverlayInPreview(watermark),
//                Option.OverlayInPictureSnapshot(watermark),
//                Option.OverlayInVideoSnapshot(watermark),
//                // Frame Processing
//                Option.FrameProcessingFormat(),
//                // Other
//                Option.Grid(), Option.GridColor(), Option.UseDeviceOrientation()
//        )
//        val dividers = listOf(
//                // Layout
//                false, true,
//                // Engine and preview
//                false, false, true,
//                // Some controls
//                false, false, false, false, false, true,
//                // Video recording
//                false, false, false, true,
//                // Gestures
//                false, false, false, false, true,
//                // Watermarks
//                false, false, true,
//                // Frame Processing
//                true,
//                // Other
//                false, false, true
//        )
//        for (i in options.indices) {
//            val view = OptionView<Any>(this)
//            view.setOption(options[i] as Option<Any>, this)
//            view.setHasDivider(dividers[i])
//            group.addView(view, MATCH_PARENT, WRAP_CONTENT)
//        }
//        controlPanel.viewTreeObserver.addOnGlobalLayoutListener {
//            BottomSheetBehavior.from(controlPanel).state = BottomSheetBehavior.STATE_HIDDEN
//        }
//
//        // Animate the watermark just to show we record the animation in video snapshots
//        val animator = ValueAnimator.ofFloat(1f, 0.8f)
//        animator.duration = 300
//        animator.repeatCount = ValueAnimator.INFINITE
//        animator.repeatMode = ValueAnimator.REVERSE
//        animator.addUpdateListener { animation ->
//            val scale = animation.animatedValue as Float
//            watermark.scaleX = scale
//            watermark.scaleY = scale
//            watermark.rotation = watermark.rotation + 2
//        }
//        animator.start()
    }
    fun putYUVData(buffer: ByteArray?, length: Int) {
        if ( YUVQueue.size >= 10) {
            YUVQueue.poll()
        }
        YUVQueue.add(buffer)
    }
    open fun addDataToEncoder(srcData: ByteArray, width: Int, height: Int) {

//        byte[] destData = YuvJavaUtil.Companion.rotateYUVDegree90(srcData, width, height);

//        if (HardEncoderHelper.isNv12Colo
    //        rFormat(colorFormat)) {
//        val yuv420Toyuv420sp: ByteArray = YuvJavaUtil.Companion.yuv420Toyuv420sp(srcData, width, height)
//        try {
//            fileOutputStream!!.write(yuv420Toyuv420sp, 0, yuv420Toyuv420sp.size)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
        encoder!!.addFrame(srcData, srcData.size, System.currentTimeMillis())
//        } else {
//            encoder?.addFrame(destData, destData.size)
//        }
    }


//    private fun message(content: String, important: Boolean) {
//        if (important) {
//            LOG.w(content)
//            Toast.makeText(this, content, Toast.LENGTH_LONG).show()
//        } else {
//            LOG.i(content)
//            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private inner class Listener : CameraListener() {
//        override fun onCameraOpened(options: CameraOptions) {
//            val group = controlPanel.getChildAt(0) as ViewGroup
//            for (i in 0 until group.childCount) {
//                val view = group.getChildAt(i) as OptionView<*>
//                view.onCameraOpened(camera, options)
//            }
//        }
//
//        override fun onCameraError(exception: CameraException) {
//            super.onCameraError(exception)
//            message("Got CameraException #" + exception.reason, true)
//        }
//
//        override fun onPictureTaken(result: PictureResult) {
//            super.onPictureTaken(result)
//            if (camera.isTakingVideo) {
//                message("Captured while taking video. Size=" + result.size, false)
//                return
//            }
//
//            // This can happen if picture was taken with a gesture.
//            val callbackTime = System.currentTimeMillis()
//            if (captureTime == 0L) captureTime = callbackTime - 300
//            LOG.w("onPictureTaken called! Launching activity. Delay:", callbackTime - captureTime)
//            PicturePreviewActivity.pictureResult = result
//            val intent = Intent(this@CameraActivity, PicturePreviewActivity::class.java)
//            intent.putExtra("delay", callbackTime - captureTime)
//            startActivity(intent)
//            captureTime = 0
//            LOG.w("onPictureTaken called! Launched activity.")
//        }
//
//        override fun onVideoTaken(result: VideoResult) {
//            super.onVideoTaken(result)
//            LOG.w("onVideoTaken called! Launching activity.")
//            VideoPreviewActivity.videoResult = result
//            val intent = Intent(this@CameraActivity, VideoPreviewActivity::class.java)
//            startActivity(intent)
//            LOG.w("onVideoTaken called! Launched activity.")
//        }
//
//        override fun onVideoRecordingStart() {
//            super.onVideoRecordingStart()
//            LOG.w("onVideoRecordingStart!")
//        }
//
//        override fun onVideoRecordingEnd() {
//            super.onVideoRecordingEnd()
//            message("Video taken. Processing...", false)
//            LOG.w("onVideoRecordingEnd!")
//        }
//
//        override fun onExposureCorrectionChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {
//            super.onExposureCorrectionChanged(newValue, bounds, fingers)
//            message("Exposure correction:$newValue", false)
//        }
//
//        override fun onZoomChanged(newValue: Float, bounds: FloatArray, fingers: Array<PointF>?) {
//            super.onZoomChanged(newValue, bounds, fingers)
//            message("Zoom:$newValue", false)
//        }
//    }
//
    override fun onClick(view: View) {
        when (view.id) {
//            R.id.edit -> edit()
//            R.id.capturePicture -> capturePicture()
//            R.id.capturePictureSnapshot -> capturePictureSnapshot()
//            R.id.captureVideo -> captureVideo()
//            R.id.captureVideoSnapshot -> captureVideoSnapshot()
//            R.id.toggleCamera -> toggleCamera()
            R.id.changeFilter -> showAppFloat()
        }
    }
//
//    override fun onBackPressed() {
//        val b = BottomSheetBehavior.from(controlPanel)
//        if (b.state != BottomSheetBehavior.STATE_HIDDEN) {
//            b.state = BottomSheetBehavior.STATE_HIDDEN
//            return
//        }
//        super.onBackPressed()
//    }
//
//    private fun edit() {
//        BottomSheetBehavior.from(controlPanel).state = BottomSheetBehavior.STATE_COLLAPSED
//    }
//
//    private fun capturePicture() {
//        if (camera.mode == Mode.VIDEO) return run {
//            message("Can't take HQ pictures while in VIDEO mode.", false)
//        }
//        if (camera.isTakingPicture) return
//        captureTime = System.currentTimeMillis()
//        message("Capturing picture...", false)
//        camera.takePicture()
//    }
//
//    private fun capturePictureSnapshot() {
//        if (camera.isTakingPicture) return
//        if (camera.preview != Preview.GL_SURFACE) return run {
//            message("Picture snapshots are only allowed with the GL_SURFACE preview.", true)
//        }
//        captureTime = System.currentTimeMillis()
//        message("Capturing picture snapshot...", false)
//        camera.takePictureSnapshot()
//    }
//
//    private fun captureVideo() {
//        if (camera.mode == Mode.PICTURE) return run {
//            message("Can't record HQ videos while in PICTURE mode.", false)
//        }
//        if (camera.isTakingPicture || camera.isTakingVideo) return
//        message("Recording for 5 seconds...", true)
//        camera.takeVideo(File(filesDir, "video.mp4"), 5000)
//    }
//
//    private fun captureVideoSnapshot() {
//        if (camera.isTakingVideo) return run {
//            message("Already taking video.", false)
//        }
//        if (camera.preview != Preview.GL_SURFACE) return run {
//            message("Video snapshots are only allowed with the GL_SURFACE preview.", true)
//        }
//        message("Recording snapshot for 5 seconds...", true)
//        camera.takeVideoSnapshot(File(filesDir, "video.mp4"), 5000)
//    }
//
//    private fun toggleCamera() {
//        if (camera.isTakingPicture || camera.isTakingVideo) return
//        when (camera.toggleFacing()) {
//            Facing.BACK -> message("Switched to back camera!", false)
//            Facing.FRONT -> message("Switched to front camera!", false)
//        }
//    }
//
//    private fun changeCurrentFilter() {
//        if (camera.preview != Preview.GL_SURFACE) return run {
//            message("Filters are supported only when preview is Preview.GL_SURFACE.", true)
//        }
//        if (currentFilter < allFilters.size - 1) {
//            currentFilter++
//        } else {
//            currentFilter = 0
//        }
//        val filter = allFilters[currentFilter]
//        message(filter.toString(), false)
//
//        // Normal behavior:
//        camera.filter = filter.newInstance()
//
//        // To test MultiFilter:
//        // DuotoneFilter duotone = new DuotoneFilter();
//        // duotone.setFirstColor(Color.RED);
//        // duotone.setSecondColor(Color.GREEN);
//        // camera.setFilter(new MultiFilter(duotone, filter.newInstance()));
//    }
//
//    override fun <T : Any> onValueChanged(option: Option<T>, value: T, name: String): Boolean {
//        if (option is Option.Width || option is Option.Height) {
//            val preview = camera.preview
//            val wrapContent = value as Int == WRAP_CONTENT
//            if (preview == Preview.SURFACE && !wrapContent) {
//                message("The SurfaceView preview does not support width or height changes. " +
//                        "The view will act as WRAP_CONTENT by default.", true)
//                return false
//            }
//        }
//        option.set(camera, value)
//        BottomSheetBehavior.from(controlPanel).state = BottomSheetBehavior.STATE_HIDDEN
//        message("Changed " + option.name + " to " + name, false)
//        return true
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        val valid = grantResults.all { it == PERMISSION_GRANTED }
//        if (valid && !camera.isOpened) {
//            camera.open()
//        }
//    }

    override fun onEncodeData(data: ByteArray, size: Int, timeStamp: Int) {
        try {
            h264FileOutputStream?.write(data, 0, size)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun showAppFloat() {
        EasyFloat.with(this.applicationContext)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.RESULT_SIDE)
                .setImmersionStatusBar(true)
                .setGravity(Gravity.END, -20, 10)
                .setLayout(R.layout.float_app) {
                    it.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
                        EasyFloat.dismiss()
                    }
                    val camera = it.findViewById<CameraView>(R.id.camera)
                            camera.open()
                    // ------------
                    if (USE_FRAME_PROCESSOR) {
                        camera.addFrameProcessor(object : FrameProcessor {
                            private var lastTime = System.currentTimeMillis()

                            @RequiresApi(Build.VERSION_CODES.KITKAT)
                            override fun process(frame: Frame) {
                                val newTime = frame.time
                                val delay = newTime - lastTime
                                lastTime = newTime
                                LOG.v("Frame delayMillis:", delay, "FPS:", 1000 / delay)
                                val image = frame.getData<Image>()
                                val bytesYUV420 = yuvUtil.preHandleYUV420(image, false, 0)
//                    val byteNv21 = ByteArray(bytesYUV420.size)
//                    BZYUVUtil.yuvI420ToNV21(bytesYUV420, byteNv21, image.width, image.height)

                                Log.i(LOG.toString(), "onImageAvailable: " + image.height + " width: " + image.width);
//                    try {
//                        fileOutputStream?.write(bytesYUV420)
//                    } catch (e: IOException) {
//                        e.printStackTrace();
//                    }
                                //            Frame frame = getFrameManager().getFrame(image,
//                    System.currentTimeMillis());
                                addDataToEncoder(bytesYUV420, 1920, 1080)
//                    putYUVData(bytesYUV420,bytesYUV420.size)
                                image.close()

                            }
                        })
                    }

                    it.findViewById<CheckBox>(R.id.checkbox)
                            .setOnCheckedChangeListener { _, isChecked -> EasyFloat.dragEnable(isChecked) }

                }
                .registerCallback {
                    drag { _, motionEvent ->
                        DragUtils.registerDragClose(motionEvent, object : OnTouchRangeListener {
                            override fun touchInRange(inRange: Boolean, view: BaseSwitchView) {
                                view.findViewById<TextView>(R.id.tv_delete).text =
                                        if (inRange) "松手删除" else "删除浮窗"

                                view.findViewById<ImageView>(R.id.iv_delete)
                                        .setImageResource(
                                                if (inRange) R.drawable.icon_delete_selected
                                                else R.drawable.icon_delete_normal
                                        )
                            }

                            override fun touchUpInRange() {
                                EasyFloat.dismiss()
                            }
                        }, showPattern = ShowPattern.ALL_TIME)
                    }
                }
                .show()
    }
}
