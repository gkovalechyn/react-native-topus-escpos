package com.reactnativetopusescpos

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit


/**
 * This class acts as a wrapper to write data to the USB Interface in Android
 * behaving like an `OutputStream` class.
 */
class AndroidUSBOutputStream(private val sendEndPoint: UsbEndpoint, // Variables.
														 private val usbConnection: UsbDeviceConnection) : OutputStream() {
	private val writeQueue: LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue(512)
	private var streamOpen = true

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	override fun write(oneByte: Int) {
		write(byteArrayOf(oneByte.toByte()))
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	override fun write(buffer: ByteArray) {
		write(buffer, 0, buffer.size)
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	override fun write(buffer: ByteArray, offset: Int, count: Int) {
		val finalData = ByteArray(count)
		System.arraycopy(buffer, offset, finalData, 0, count)

		if (!streamOpen) {
			throw IOException("Not connected to USB device");
		}

		try {
			writeQueue.add(finalData)
		} catch (e: IllegalStateException) {
			Log.e(TopusEscposModule.TAG, "Could not add data, write queue is full: " + e.message, e)
		}
	}

	/**
	 * Internal class used to write data coming from a queue.
	 */
	internal inner class DataWriter : Thread() {
		override fun run() {
			while (streamOpen) {
				try {
					val dataToWrite = writeQueue.poll(100, TimeUnit.MILLISECONDS) ?: continue

					val bytesTransferred = usbConnection.bulkTransfer(sendEndPoint, dataToWrite, dataToWrite.size, WRITE_TIMEOUT)
					Log.d(TopusEscposModule.TAG, "Wrote $bytesTransferred bytes to the output stream");

					// Failed to write to the stream, we are probably disconnected
					if (bytesTransferred < 0) {
						streamOpen = false;
					}

				} catch (e: InterruptedException) {
					Log.e(TopusEscposModule.TAG, "Interrupted while getting data from the write queue: " + e.message, e)
				}
			}
		}
	}

	@Throws(IOException::class)
	override fun close() {
		// Stop the data writer.
		streamOpen = false
		super.close()
	}

	companion object {
		// Constants.
		private const val WRITE_TIMEOUT = 2000
	}

	/**
	 * Class constructor. Instantiates a new `AndroidUSBOutputStream`
	 * object with the given parameters.
	 *
	 * @param writeEndpoint The USB end point to use to write data to.
	 * @param connection The USB connection to use to write data to.
	 *
	 * @see UsbDeviceConnection
	 *
	 * @see UsbEndpoint
	 */
	init {
		val dataWriter: DataWriter = DataWriter()
		dataWriter.start()
	}
}
