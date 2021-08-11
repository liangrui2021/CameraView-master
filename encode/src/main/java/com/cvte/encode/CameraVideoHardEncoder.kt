package   com.cvte.encode

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log

import java.lang.RuntimeException
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 创建日期：2020-02-28 on 10:02
 * 描述:
 * 作者: chenhong
 */
class CameraVideoHardEncoder(private var encodeListener: IEncodeListener?) {

    companion object {
        private val TAG = "CameraVideoHardEncoder"
        private const val MIME_TYPE = "video/avc"
        private var iFrameInternal = 10000
        private var maxBufferSize = 20
    }

    private var bitRate = 5000000
    private var frameRate = 30
    private var width = 1920
    private var height = 1080
    @Volatile
    private var isConfigured = false
    private var requestKeyFrameFlag = false
    private var encoder: MediaCodec? = null

    private val mQueue = ConcurrentLinkedQueue<BufferData>()
    private var timeStamp: Int = 0
    private var frameIndex = 0
    private var outData: ByteArray? = null
    private var configureData: ByteArray? = null
    private var handlerThread: HandlerThread = HandlerThread("CameraVideoHardEncoder")

    fun initEncoder(width: Int, height: Int, bitRate: Int, frameRate: Int) {


        this.width =  width
        this.height =  height
        this.bitRate = bitRate
        this.frameRate = frameRate

        outData = ByteArray(this.width * this.height * 3 / 2)

        Log.d(TAG, "get wh: ${this.width} ${this.height}")
        val codecInfo =
                HardEncoderHelper.selectCodec(MIME_TYPE)
        if (codecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for $MIME_TYPE")
            return
        }
        Log.d(TAG, "found codec: " + codecInfo.name)
        val colorFormat =
                HardEncoderHelper.selectColorFormat(
                        codecInfo,
                        MIME_TYPE
                )
        Log.d(TAG, "found colorFormat: $colorFormat")
        val format = MediaFormat.createVideoFormat(
            MIME_TYPE, this.width,
            this.height
        )
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
        format.setInteger(
            MediaFormat.KEY_BIT_RATE,
            bitRate
        )
        format.setInteger(
            MediaFormat.KEY_FRAME_RATE,
            frameRate
        )
        format.setInteger(
            MediaFormat.KEY_I_FRAME_INTERVAL,
            iFrameInternal
        )
        Log.d(TAG, "format: $format")

        encoder = MediaCodec.createByCodecName(codecInfo.name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            handlerThread.start()
            val handler = Handler(handlerThread.looper)
            encoder!!.setCallback(CodecCallback(), handler)
        } else {
            encoder!!.setCallback(CodecCallback())
        }

        encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        Log.d(TAG, ": $format")
        isConfigured = true
    }

    fun startEncoder() {
        if (!isConfigured || encoder == null) {
            throw RuntimeException("encoder not init")
        }
        encoder!!.start()
    }

    fun addFrame(data: ByteArray, length: Int, timeStamp: Long) {
        if (isConfigured) {
            if (mQueue.size > maxBufferSize) {
                Log.d(TAG, "addFrame mQueue is full")
                mQueue.poll()
            }
            mQueue.add(BufferData(data, length, timeStamp))
            Log.d(TAG, "addFrame " + data.size + " mQueue " + mQueue.size)
        } else {
            mQueue.clear()
            Log.d(TAG, "not configured")
        }
    }

    fun requestKeyFrame() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                val params = Bundle()
                params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                encoder?.setParameters(params)
                Log.d(TAG, "requestKeyFrame suc")
            } else {
                Log.d(TAG, "requestKeyFrame fail,api not support")
            }
            requestKeyFrameFlag = true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private inner class CodecCallback : MediaCodec.Callback() {

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            try {
                val inputBuffer = codec.getInputBuffer(index)
                if (inputBuffer == null) {
                    Log.d("onInputBufferAvailable", "inputBuffer == null  $index")
                    return
                }

                val data = mQueue.poll()
                if (data == null) {
                    codec.queueInputBuffer(
                        index,
                        0, 0, 0, 0
                    )
                } else {
                    val presentationTime = computePresentationTime(frameIndex.toLong())
                    frameIndex++
                    Log.d(TAG, "onInputBufferAvailable  data != null " + mQueue.size)
                    inputBuffer.clear()
                    inputBuffer.put(data.buffer)

                    codec.queueInputBuffer(
                        index,
                        0,
                        data.buffer.size,
                        presentationTime,
                        0
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onOutputBufferAvailable(
            codec: MediaCodec,
            index: Int,
            bufferInfo: MediaCodec.BufferInfo
        ) {
            try {
                //输出编码后数据
                if (index >= 0) {
                    getEncodeData(codec, bufferInfo, index)
                    encoder?.releaseOutputBuffer(index, false)
                } else {
                    Log.e(TAG, "onOutputBufferAvailable invalid index $index")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Log.e(TAG, "onError  $e")
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            Log.e(TAG, "onOutputFormatChanged  ")
        }
    }

    private fun getEncodeData(
        codec: MediaCodec,
        bufferInfo: MediaCodec.BufferInfo,
        outputBufferIndex: Int
    ) {
        var encodedData = codec.getOutputBuffer(outputBufferIndex)
        encodedData!!.get(outData, 0, bufferInfo.size)

        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
            Log.i(TAG, "getEncodeData get key frame success")
            if (configureData != null && requestKeyFrameFlag) {
                onEncodeVideoData(configureData!!, configureData!!.size)
                requestKeyFrameFlag = false
                Log.i(TAG, "getEncodeData send sps and pps data")
            }
        }
        onEncodeVideoData(outData!!, bufferInfo.size)
        //提取SPS和PPS
        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            Log.i(TAG, "getEncodeData get sps & pps success")
            configureData = ByteArray(bufferInfo.size)
            System.arraycopy(outData!!, 0, configureData, 0, bufferInfo.size)

            bufferInfo.size = 0
            encodedData = null
        }

        if (encodedData != null) {
            encodedData.position(bufferInfo.offset)
            encodedData.limit(bufferInfo.offset + bufferInfo.size)
            timeStamp += bufferInfo.size
        }
    }

    private fun onEncodeVideoData(data: ByteArray, size: Int) {
        encodeListener?.onEncodeData(data, size, timeStamp)
    }

    fun releaseEncoder() {
        mQueue.clear()
        isConfigured = false
        try {
            if (encoder != null) {
                encoder!!.stop()
                encoder!!.release()
                Log.d(TAG, "releaseEncoder ")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun computePresentationTime(frameIndex: Long): Long {
        return 132 + frameIndex * 1000000 / frameRate
    }
}
