package   com.cvte.encode

/**
 * 创建日期：2020-03-25 on 14:48
 * 描述:
 * 作者: chenhong
 */

interface IEncodeListener {
    fun onEncodeData(data: ByteArray, size: Int, timeStamp: Int)
}