package   com.cvte.encode;


public class BufferData {
    byte[] buffer;
    long presentationTimeUs;

    public BufferData(byte[] buffer, int length, long presentationTimeUs) {
        this.buffer = new byte[length];
        System.arraycopy(buffer, 0, this.buffer, 0, buffer.length);
        this.presentationTimeUs = presentationTimeUs;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public long getPresentationTimeUs() {
        return presentationTimeUs;
    }

}
