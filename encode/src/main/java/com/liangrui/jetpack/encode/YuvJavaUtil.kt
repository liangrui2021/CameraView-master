package com.liangrui.jetpack.encode

import java.nio.ByteBuffer
import java.util.*

/**
 * @author chenhong
 */
class YuvJavaUtil {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            testConvert()
            println("  ------------------------------------  ")
            testRotate()
        }

        private fun testRotate() {
        }

        private fun testConvert() {
            val nv21Data = byteArrayOf(
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                3,
                2,
                3,
                2,
                3,
                2,
                3,
                2,
                3,
                2
            )

            val width = 5
            val height = 4

            val i420 = nv21ToI420(nv21Data, width, height)
            println("  nv21ToI420    " + Arrays.toString(i420))

            val nv21 = i420ToNv21(i420, width, height)
            println("  i420ToNv21    " + Arrays.toString(nv21))

            val rotateYUVDegree90 = rotateYUVDegree90(nv21, width, height)
            println("  rotateYUVDegree90    " + Arrays.toString(rotateYUVDegree90))
        }

        /**
         * yyyyy vuvuvu  ->  yyyyyyy uvuvuv
         */
        fun nv21ToNv12(data: ByteArray, width: Int, height: Int): ByteArray {
            val destArray = ByteArray(data.size)
            var base = width * height
            System.arraycopy(data, 0, destArray, 0, base)
            for (a in base until 3 * base / 2 step 2) {
                destArray[a] = data[a + 1]
                destArray[a + 1] = data[a]
            }
            return destArray
        }

        fun nv21ToI420(nv21Data: ByteArray, width: Int, height: Int): ByteArray {

            val i420Data = ByteArray(nv21Data.size)
            val total = width * height

            val yByteBuffer = ByteBuffer.wrap(i420Data, 0, total)
            val uByteBuffer = ByteBuffer.wrap(i420Data, total, total / 4)
            val vByteBuffer = ByteBuffer.wrap(i420Data, total + total / 4, total / 4)

            yByteBuffer.put(nv21Data, 0, total)
            var i = total
            while (i < nv21Data.size) {
                vByteBuffer.put(nv21Data[i])
                uByteBuffer.put(nv21Data[i + 1])
                i += 2
            }
            return i420Data
        }

        fun i420ToNv21(i420Data: ByteArray, width: Int, height: Int): ByteArray {
            val nv21Data = ByteArray(i420Data.size)
            val total = width * height
            val yByteBuffer = ByteBuffer.wrap(nv21Data, 0, total)
            val vuByteBuffer = ByteBuffer.wrap(nv21Data, total, total / 2)

            //y
            yByteBuffer.put(i420Data, 0, total)
            for (i in 0 until total / 4) {
                //v
                vuByteBuffer.put(i420Data[total + i + total / 4])
                //u
                vuByteBuffer.put(i420Data[total + i])
            }
            return nv21Data
        }

        fun rotateYUVDegree270(data: ByteArray, kWidth: Int, kHeight: Int): ByteArray {

            val destData = ByteArray(data.size)
            var index = 0
            for (i in kWidth - 1 downTo 0) {
                for (j in 0 until kHeight) {
                    destData[index++] = data[j * kWidth + i]
                }
            }

            var u_index = kWidth * kHeight
            var v_index = kWidth * kHeight * 5 / 4

            val u_src = kWidth * kHeight
            val v_src = kWidth * kHeight * 5 / 4

            for (i in kWidth / 2 - 1 downTo 0) {
                for (j in 0 until kHeight / 2) {
                    destData[u_index++] = data[u_src + kWidth / 2 * j + i]
                    destData[v_index++] = data[v_src + kWidth / 2 * j + i]
                }
            }
            return destData
        }

        fun rotateYUVDegree90(data: ByteArray, kWidth: Int, kHeight: Int): ByteArray {

            val destData = ByteArray(data.size)
            var index = 0
            for (i in 0 until kWidth) {
                for (j in kHeight - 1 downTo 0) {
                    destData[index++] = data[j * kWidth + i]
                }
            }

            var u_index = kWidth * kHeight
            var v_index = kWidth * kHeight * 5 / 4

            val u_src = kWidth * kHeight
            val v_src = kWidth * kHeight * 5 / 4

            for (i in 0 until kWidth / 2) {
                for (j in kHeight / 2 - 1 downTo 0) {
                    destData[u_index++] = data[u_src + kWidth / 2 * j + i]
                    destData[v_index++] = data[v_src + kWidth / 2 * j + i]
                }
            }
            return destData
        }

        /**
         * yyyy uuuu vvvv-> yyyyy vvvvvv uuuuuuu
         */
        fun yu12ToYv12(data: ByteArray, width: Int, height: Int): ByteArray {
            val byteArray = ByteArray(data.size)
            var base = width * height
            System.arraycopy(data, 0, byteArray, 0, base)
            System.arraycopy(data, base, byteArray, base * 5 / 4, base / 4)
            System.arraycopy(data, base * 5 / 4, byteArray, base, base / 4)
            return byteArray
        }

        /**
         * yyyy uuuu vvvv-> yyyyy uvuvuv
         */
        fun yuv420Toyuv420sp(data: ByteArray, width: Int, height: Int): ByteArray {
            val byteArray = ByteArray(data.size)
            var base = width * height
            System.arraycopy(data, 0, byteArray, 0, base)
            var index = base
            for (a in base until 5 * base / 4) {
                byteArray[index] = data[a]
                byteArray[index + 1] = data[a + base / 4]
                index += 2
            }
            return byteArray
        }

        /**
         *
         * yyyyy vuvuvu  ->yyyyyy uvuvuv
         * nv21 ->yuv420sp
         */
        fun nv21Toyu12(data: ByteArray, width: Int, height: Int): ByteArray {
            val byteArray = ByteArray(data.size)
            var base = width * height
            System.arraycopy(data, 0, byteArray, 0, base)
            var index = base
            var i = base
            while (i < base * 3 / 2) {
                byteArray[index] = data[i + 1]
                byteArray[index + 1] = data[i]
                index += 2
                i += 2
            }
            return byteArray
        }

        /**
         * 镜像操作，数组位置互换
         *
         * @param data
         * @param width
         * @param height
         * @return
         */
        fun mirror(data: ByteArray, width: Int, height: Int): ByteArray {
            var tempData: Byte
            for (i in 0 until height * 3 / 2) {
                for (j in 0 until width / 2) {
                    tempData = data[i * width + j]
                    data[i * width + j] = data[(i + 1) * width - 1 - j]
                    data[(i + 1) * width - 1 - j] = tempData
                }

            }
            return data
        }
    }
}
