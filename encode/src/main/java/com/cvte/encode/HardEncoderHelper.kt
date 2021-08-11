package   com.cvte.encode

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.util.Log
import java.lang.RuntimeException


/**
 * 创建日期：2020-03-25 on 15:48
 * 描述:
 * 作者: chenhong
 */
class HardEncoderHelper {

    companion object {

        fun isNv12ColorFormat(colorFormat: Int): Boolean {
            //COLOR_FormatYUV420SemiPlanar
            return colorFormat == 21
        }

        fun isNv21ColorFormat(colorFormat: Int): Boolean {
            //COLOR_FormatYUV420PackedSemiPlanar
            return colorFormat == 39
        }


        fun isYv12ColorFormat(colorFormat: Int): Boolean {
            //COLOR_FormatYUV420PackedPlanar
            return colorFormat == 20
        }

        fun isYv21ColorFormat(colorFormat: Int): Boolean {
            //COLOR_FormatYUV420Planar
            return colorFormat == 19
        }

        private var TAG = HardEncoderHelper.javaClass.simpleName
        private const val MIME_TYPE = "video/avc"

        fun getSupportColorFormat(): Int {
            val codecInfo = selectCodec(MIME_TYPE)
            if (codecInfo == null) {
                Log.e(TAG, "Unable to find an appropriate codec for $MIME_TYPE")
                throw RuntimeException("Unable to find an appropriate codec for $MIME_TYPE")
            }
            Log.d(TAG, "found codec: " + codecInfo.name)
            val colorFormat = selectColorFormat(codecInfo, MIME_TYPE)
            selectColorFormatTest(codecInfo, MIME_TYPE)
            Log.d(TAG, "found colorFormat: $colorFormat")
            return colorFormat
        }

        fun selectCodec(mimeType: String): MediaCodecInfo? {
            val numCodecs = MediaCodecList.getCodecCount()
            for (i in 0 until numCodecs) {
                val codecInfo = MediaCodecList.getCodecInfoAt(i)
                if (!codecInfo.isEncoder) {
                    continue
                }
                val types = codecInfo.supportedTypes
                for (j in types.indices) {
                    if (types[j].equals(mimeType, ignoreCase = true)) {
                        return codecInfo
                    }
                }
            }
            return null
        }

        fun selectColorFormat(codecInfo: MediaCodecInfo, mimeType: String): Int {
            val capabilities = codecInfo.getCapabilitiesForType(mimeType)
            for (i in capabilities.colorFormats.indices) {
                val colorFormat = capabilities.colorFormats[i]
                if (isRecognizedFormat(
                        colorFormat
                    )
                ) {
                    return colorFormat
                }
            }
            Log.d(TAG, "couldn't find a good color format for " + codecInfo.name + " / " + mimeType)
            return 0
        }

        private fun selectColorFormatTest(codecInfo: MediaCodecInfo, mimeType: String): Int {
            val capabilities = codecInfo.getCapabilitiesForType(mimeType)
            for (i in capabilities.colorFormats.indices) {
                val colorFormat = capabilities.colorFormats[i]
                if (isRecognizedFormat(
                        colorFormat
                    )
                ) {
                    Log.d(TAG, "selectColorFormatTest $colorFormat")
                }
            }
            Log.d(TAG, "couldn't find a good color format for " + codecInfo.name + " / " + mimeType)
            return 0
        }

        private fun isRecognizedFormat(colorFormat: Int): Boolean {
            return when (colorFormat) {
                // these are the formats we know how to handle for this test
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar,
                MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar
                -> true
                else -> false
            }
        }
    }
}