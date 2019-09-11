package com.example.sullivanplus.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sullivanplus.R;
import com.example.sullivanplus.camera.AutoFitTextureView;
import com.example.sullivanplus.db.SullivanResourceData;
import com.example.sullivanplus.etc.ColorUtils;
import com.example.sullivanplus.ui.Adapter.AdditionAdapter;
import com.example.sullivanplus.ui.Adapter.MenuAdapter;
import com.example.sullivanplus.ui.Adapter_Item.MenuItem;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends BaseActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
	/**
	 * Conversion from screen rotation to JPEG orientation.
	 */
	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

	static {
		ORIENTATIONS.append(Surface.ROTATION_0, 0);
		ORIENTATIONS.append(Surface.ROTATION_90, 90);
		ORIENTATIONS.append(Surface.ROTATION_180, 180);
		ORIENTATIONS.append(Surface.ROTATION_270, 270);
	}


	// Request code for camera permissions.
	private static final int REQUEST_CAMERA_PERMISSIONS = 1;
	// Timeout for the pre-capture sequence.
	private static final long PRECAPTURE_TIMEOUT_MS = 1000;
	// Tolerance when comparing aspect ratios.
	private static final double ASPECT_RATIO_TOLERANCE = 0.005;
	// * Max preview width that is guaranteed by Camera2 API
	private static final int MAX_PREVIEW_WIDTH = 1080;
	// * Max preview height that is guaranteed by Camera2 API
	private static final int MAX_PREVIEW_HEIGHT = 1920;
	//* Tag for the {@link Log}.
	private static final String TAG = "MainActivity";
	//Camera state: Device is closed.
	private static final int STATE_CLOSED = 0;
	//Camera state: Device is opened, but is not capturing.
	private static final int STATE_OPENED = 1;
	//* Camera state: Showing camera preview.
	private static final int STATE_PREVIEW = 2;
	// * Camera state: Waiting for 3A convergence before capturing a photo.
	private static final int STATE_WAITING_FOR_3A_CONVERGENCE = 3;
	/**
	 * An {@link OrientationEventListener} used to determine when device rotation has occurred.
	 * This is mainly necessary for when the device is rotated by 180 degrees, in which case
	 * onCreate or onConfigurationChanged is not called as the view dimensions remain the same,
	 * but the orientation of the has changed, and thus the preview rotation must be updated.
	 */
	private OrientationEventListener mOrientationListener;


	/**
	 * {@link TextureView.SurfaceTextureListener} handles several lifecycle events of a
	 * {@link TextureView}.
	 */
	private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
			configureTransform(width, height);
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
			configureTransform(width, height);
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
			synchronized (mCameraStateLock) {
				mPreviewSize = null;
			}
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture texture) {

			//업데이트 함수
			//Log.i("Test", "onSurfaceTextureUpdated");

		}
	};
	//* An {@link AutoFitTextureView} for camera preview.
	private AutoFitTextureView mTextureView;
	// * An additional thread for running tasks that shouldn't block the UI.  This is used for all
	//* callbacks from the {@link CameraDevice} and {@link CameraCaptureSession}s.
	private HandlerThread mBackgroundThread;
	// * A counter for tracking corresponding {@link CaptureRequest}s and {@link CaptureResult}s
	// * across the {@link CameraCaptureSession} capture callbacks.
	private final AtomicInteger mRequestCounter = new AtomicInteger();
	// * A {@link Semaphore} to prevent the app from exiting before closing the camera.
	private final Semaphore mCameraOpenCloseLock = new Semaphore(1);
	// * A lock protecting camera state.
	private final Object mCameraStateLock = new Object();
	// *********************************************************************************************
	// State protected by mCameraStateLock.
	//
	// The following state is used across both the UI and background threads.  Methods with "Locked"
	// in the name expect mCameraStateLock to be held while calling.

	//	 * ID of the current {@link CameraDevice}.
	private String mCameraId;
	//* A {@link CameraCaptureSession } for camera preview.
	private CameraCaptureSession mCaptureSession;
	//* A reference to the open {@link CameraDevice}.
	private CameraDevice mCameraDevice;
	// * The {@link Size} of camera preview.
	private Size mPreviewSize;
	// * The {@link CameraCharacteristics} for the currently configured camera device.
	private CameraCharacteristics mCharacteristics;

	// * A {@link Handler} for running tasks in the background.
	private Handler mBackgroundHandler;

	/**
	 * A reference counted holder wrapping the {@link ImageReader} that handles JPEG image
	 * captures. This is used to allow us to clean up the {@link ImageReader} when all background
	 * tasks using its {@link Image}s have completed.
	 */
	private RefCountedAutoCloseable<ImageReader> mJpegImageReader;

	/**
	 * Whether or not the currently configured camera device is fixed-focus.
	 */
	private boolean mNoAFRun = false;
	// * Number of pending user requests to capture a photo.
	private int mPendingUserCaptures = 0;
	// * Request ID to {@link ImageSaver.ImageSaverBuilder} mapping for in-progress JPEG captures.
	private final TreeMap<Integer, ImageSaver.ImageSaverBuilder> mJpegResultQueue = new TreeMap<>();
	// * {@link CaptureRequest.Builder} for the camera preview
	private CaptureRequest.Builder mPreviewRequestBuilder;
	// * The state of the camera device.
	// * @see #mPreCaptureCallback
	private int mState = STATE_CLOSED;
	// * Timer to use with pre-capture sequence to ensure a timely capture if 3A convergence is
	// * taking too long.
	private long mCaptureTimer;

	private boolean isPrecessing = false;

	/**
	 * {@link CameraDevice.StateCallback} is called when the currently active {@link CameraDevice}
	 * changes its state.
	 */
	private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

		@Override
		public void onOpened(CameraDevice cameraDevice) {
			// This method is called when the camera is opened.  We start camera preview here if
			// the TextureView displaying this has been set up.
			synchronized (mCameraStateLock) {
				mState = STATE_OPENED;
				mCameraOpenCloseLock.release();
				mCameraDevice = cameraDevice;

				// Start the preview session if the TextureView has been set up already.
				if (mPreviewSize != null && mTextureView.isAvailable()) {
					createCameraPreviewSessionLocked();
				}
			}
		}

		@Override
		public void onDisconnected(CameraDevice cameraDevice) {
			synchronized (mCameraStateLock) {
				mState = STATE_CLOSED;
				mCameraOpenCloseLock.release();
				cameraDevice.close();
				mCameraDevice = null;
			}
		}

		@Override
		public void onError(CameraDevice cameraDevice, int error) {
			Log.e(TAG, "Received camera device error: " + error);
			synchronized (mCameraStateLock) {
				mState = STATE_CLOSED;
				mCameraOpenCloseLock.release();
				cameraDevice.close();
				mCameraDevice = null;
			}
			// if (null != activity) {                            needcheck
			finish();
			//}
		}

	};

	/**
	 * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
	 * JPEG image is ready to be saved.
	 */

	//사진을 찍을 때 들어오는 리스너
	private final ImageReader.OnImageAvailableListener mOnJpegImageAvailableListener = new ImageReader.OnImageAvailableListener() {
		@Override
		public void onImageAvailable(ImageReader reader) {
			dequeueAndSaveImage(mJpegResultQueue, mJpegImageReader);
		}
	};


	//int frameCount=0;
	/**
	 * A {@link CameraCaptureSession.CaptureCallback} that handles events for the preview and
	 * pre-capture sequence.
	 */
	private CameraCaptureSession.CaptureCallback mPreCaptureCallback = new CameraCaptureSession.CaptureCallback() {
		private void process(CaptureResult result) {
			synchronized (mCameraStateLock) {
				switch (mState) {
					case STATE_PREVIEW: {

						// We have nothing to do when the camera preview is running normally.
						break;
					}
					case STATE_WAITING_FOR_3A_CONVERGENCE: {
						Log.i("CameraSize", "STATE_WAITING_FOR_3A_CONVERGENCE");

						boolean readyToCapture = true;
						if (!mNoAFRun) {
							Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
							if (afState == null) {
								break;
							}
							// If auto-focus has reached locked state, we are ready to capture
							readyToCapture =
									(afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
											afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED);
						}
						// If we are running on an non-legacy device, we should also wait until
						// auto-exposure and auto-white-balance have converged as well before
						// taking a picture.
						if (!isLegacyLocked()) {
							Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
							Integer awbState = result.get(CaptureResult.CONTROL_AWB_STATE);

							if (aeState == null || awbState == null) {
								break;
							}

							readyToCapture = readyToCapture &&	aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED &&	awbState == CaptureResult.CONTROL_AWB_STATE_CONVERGED;
						}
						// If we haven't finished the pre-capture sequence but have hit our maximum
						// wait timeout, too bad! Begin capture anyway.
						if (!readyToCapture && hitTimeoutLocked()) {
							Log.w(TAG, "Timed out waiting for pre-capture sequence to complete.");
							readyToCapture = true;
						}
						if (readyToCapture && mPendingUserCaptures > 0) {
							// Capture once for each user tap of the "Picture" button.
							while (mPendingUserCaptures > 0) {
								captureStillPictureLocked();
								mPendingUserCaptures--;
							}
							// After this, the camera will go back to the normal state of preview.
							mState = STATE_PREVIEW;
						}

						Toast.makeText(getApplication(), "결과값 로딩 중", Toast.LENGTH_LONG).show();
						isPrecessing = true;
						faceDetector.release();
						textRecognizer.release();
						vibrator.cancel();
					}
				}
			}
		}

		@Override
		public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
			process(partialResult);
		}

		@Override
		public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
			process(result);
			//업데이트 함수
			//Log.i("Test", "onCaptureCompleted");

			if (isPrecessing || mTextureView.getVisibility() != View.VISIBLE) {
				return;
			}

			if (mTextureView != null && mTextureView.isAvailable() && !lockdetect) {
				isPrecessing = true;
				Bitmap textureBitmap = mTextureView.getBitmap();

				Frame frame = new Frame.Builder().setBitmap(textureBitmap).build();

				if (SullivanResourceData.Curr_Mode.equals(getString(R.string.mode_face))) {
					SparseArray<Face> faces = faceDetector.detect(frame);

					if (faces.size() > 0) {
						if (isFaceVoiceNotice) {
							if (!isfaceDetect) {
								isfaceDetect = true;
								soundPool.play(SoundID, 1, 1, 0, 0, 2f);
							}
						} else {
							vibrator.vibrate(VIBRATOR_TIME);
						}
					} else {
						isfaceDetect = false;
						if (!isFaceVoiceNotice)
							vibrator.cancel();
					}
				} else if (SullivanResourceData.Curr_Mode.equals(getString(R.string.mode_text))) {
					if (textRecognizer.isOperational()) {
						SparseArray<TextBlock> text = textRecognizer.detect(frame);

						Log.i("CameraSize","textRecognizer");
						if (text.size() > 0) {
							if (isTextVoiceNotice) {
								if (!istextDetect) {
									istextDetect = true;
									soundPool.play(SoundID, 1, 1, 0, 0, 2f);
								}
							} else {
								vibrator.vibrate(VIBRATOR_TIME);
								Log.i("Test", String.format("vibrator: %s", vibrator.hasVibrator()));
							}
						} else {
							istextDetect = false;
							if (!isTextVoiceNotice)
								vibrator.cancel();
						}
					}
				}
				isPrecessing = false;
			}
		}

	};

	/**
	 * A {@link CameraCaptureSession.CaptureCallback} that handles the still JPEG and RAW capture
	 * request.
	 */
	private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
		@Override
		public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
			String currentDateTime = generateTimestamp();

			Log.i("CameraSize","onCaptureStarted");

			File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Sullivan");
			if (!dir.exists()) {
				dir.mkdir();
			}

			File jpegFile = new File(dir, "Sullivan" + currentDateTime + ".jpg");

			// Look up the ImageSaverBuilder for this request and update it with the file name
			// based on the capture start time.
			ImageSaver.ImageSaverBuilder jpegBuilder;
			int requestId = (int) request.getTag();
			synchronized (mCameraStateLock) {

				Log.i("CameraSize","mCameraStateLock");
				jpegBuilder = mJpegResultQueue.get(requestId);
			}

			if (jpegBuilder != null) jpegBuilder.setFile(jpegFile);
		}

		///////////////////////////////////////////캡처 완성!!//////////////////////////////////////
		@Override
		public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
			int requestId = (int) request.getTag();
			ImageSaver.ImageSaverBuilder jpegBuilder;

			// Look up the ImageSaverBuilder for this request and update it with the CaptureResult
			synchronized (mCameraStateLock) {
				jpegBuilder = mJpegResultQueue.get(requestId);
				if (jpegBuilder != null) {
					jpegBuilder.setResult(result);
				}
				// If we have all the results necessary, save the image to a file in the background.
				handleCompletionLocked(requestId, jpegBuilder, mJpegResultQueue);

				finishedCaptureLocked();
			}
		}

		@Override
		public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
			int requestId = (int) request.getTag();
			synchronized (mCameraStateLock) {
				mJpegResultQueue.remove(requestId);
				finishedCaptureLocked();
			}
		}
	};

	/**
	 * A {@link Handler} for showing {@link Toast}s on the UI thread.
	 */
	private final Handler mMessageHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			if (this != null) {
				Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
			}
		}
	};

	private boolean setUpCameraOutputs() {
		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

		/*if (manager == null) {
			ErrorDialog.buildErrorDialog("This device doesn't support Camera2 API.").show(getFragmentManager(), "dialog");
			return false;
		}*/
		try {
			// Find a CameraDevice that supports RAW captures, and configure state.
			for (String cameraId : manager.getCameraIdList()) {
				CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);


				StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

				// For still image captures, we use the largest available size.
				//Size largestJpeg = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
				/*for(int i=0; i<map.getOutputSizes(ImageFormat.JPEG).length;i++)
				{
					Log.i("CameraSize",Integer.toString(i) + " : " + Integer.toString(map.getOutputSizes(ImageFormat.JPEG)[i].getHeight())+ " / " +Integer.toString(map.getOutputSizes(ImageFormat.JPEG)[i].getWidth()));
				}*/
				Size largestJpeg = map.getOutputSizes(ImageFormat.JPEG)[17];

				synchronized (mCameraStateLock) {
					// Set up ImageReaders for JPEG and RAW outputs.  Place these in a reference
					// counted wrapper to ensure they are only closed when all background tasks
					// using them are finished.
					if (mJpegImageReader == null || mJpegImageReader.getAndRetain() == null) {
						mJpegImageReader = new RefCountedAutoCloseable<>(ImageReader.newInstance(largestJpeg.getWidth(), largestJpeg.getHeight(), ImageFormat.JPEG, /*maxImages*/5));
					}
					mJpegImageReader.get().setOnImageAvailableListener(mOnJpegImageAvailableListener, mBackgroundHandler);
					mCharacteristics = characteristics;
					mCameraId = cameraId;
				}
				return true;
			}
		} catch (CameraAccessException e) {
			Log.i("CameraSize", "setUpCameraOutputs");
			e.printStackTrace();
		}

		// If we found no suitable cameras for capturing RAW, warn the user.
		//ErrorDialog.buildErrorDialog("This device doesn't support capturing RAW photos").	show(getFragmentManager(), "dialog");
		return false;
	}

	/**
	 * Opens the camera specified by {@link #mCameraId}.
	 */
	@SuppressWarnings("MissingPermission")
	public void openCamera() {
		if (!setUpCameraOutputs()) {
			return;
		}
		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		try {
			// Wait for any previously running session to finish.
			if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
				throw new RuntimeException("Time out waiting to lock camera opening.");
			}

			String cameraId;
			Handler backgroundHandler;
			synchronized (mCameraStateLock) {
				cameraId = mCameraId;
				backgroundHandler = mBackgroundHandler;
			}

			// Attempt to open the camera. mStateCallback will be called on the background handler's
			// thread when this succeeds or fails.
			manager.openCamera(cameraId, mStateCallback, backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
		}
	}

	public void closeCamera() {
		Log.i("Test", "closeCamera");
		try {
			mCameraOpenCloseLock.acquire();
			synchronized (mCameraStateLock) {

				// Reset state and clean up resources used by the camera.
				// Note: After calling this, the ImageReaders will be closed after any background
				// tasks saving Images from these readers have been completed.
				mPendingUserCaptures = 0;
				mState = STATE_CLOSED;
				if (null != mCaptureSession) {
					mCaptureSession.close();
					mCaptureSession = null;
				}
				if (null != mCameraDevice) {
					mCameraDevice.close();
					mCameraDevice = null;
				}
				if (null != mJpegImageReader) {
					mJpegImageReader.close();
					mJpegImageReader = null;
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
		} finally {
			mCameraOpenCloseLock.release();
		}
	}

	/**
	 * Starts a background thread and its {@link Handler}.
	 */
	public void startBackgroundThread() {
		mBackgroundThread = new HandlerThread("CameraBackground");
		mBackgroundThread.start();
		synchronized (mCameraStateLock) {
			mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
		}
	}

	/**
	 * Stops the background thread and its {@link Handler}.
	 */
	public void stopBackgroundThread() {
		Log.i("Test", "stopBackgroundThread");
		mBackgroundThread.quitSafely();
		try {
			mBackgroundThread.join();
			mBackgroundThread = null;
			synchronized (mCameraStateLock) {
				mBackgroundHandler = null;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new {@link CameraCaptureSession} for camera preview.
	 * <p/>
	 * Call this only with {@link #mCameraStateLock} held.
	 */
	private void createCameraPreviewSessionLocked() {
		try {
			Log.i("CameraSize", "createCameraPreviewSessionLocked");
			SurfaceTexture texture = mTextureView.getSurfaceTexture();
			// We configure the size of default buffer to be the size of camera preview we want.
			texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

			// This is the output Surface we need to start preview.
			Surface surface = new Surface(texture);

			// We set up a CaptureRequest.Builder with the output Surface.
			mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			mPreviewRequestBuilder.addTarget(surface);

			// Here, we create a CameraCaptureSession for camera preview.
			mCameraDevice.createCaptureSession(Arrays.asList(surface, mJpegImageReader.get().getSurface()), new CameraCaptureSession.StateCallback() {
						@Override
						public void onConfigured(CameraCaptureSession cameraCaptureSession) {
							synchronized (mCameraStateLock) {
								// The camera is already closed
								if (null == mCameraDevice) {
									return;
								}

								try {
									setup3AControlsLocked(mPreviewRequestBuilder);
									// Finally, we start displaying the camera preview.
									Log.i("CameraSize", "onConfigured");
									cameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mPreCaptureCallback, mBackgroundHandler);
									mState = STATE_PREVIEW;
								} catch (CameraAccessException | IllegalStateException e) {
									e.printStackTrace();
									return;
								}
								// When the session is ready, we start displaying the preview.
								mCaptureSession = cameraCaptureSession;
								flashlight();
							}
						}

						@Override
						public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
						}
					}, mBackgroundHandler
			);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the given {@link CaptureRequest.Builder} to use auto-focus, auto-exposure, and
	 * auto-white-balance controls if available.
	 * <p/>
	 * Call this only with {@link #mCameraStateLock} held.
	 *
	 * @param builder the builder to configure.
	 */
	private void setup3AControlsLocked(CaptureRequest.Builder builder) {
		// Enable auto-magical 3A run by camera device
		builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

		Float minFocusDist = mCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);

		// If MINIMUM_FOCUS_DISTANCE is 0, lens is fixed-focus and we need to skip the AF run.
		mNoAFRun = (minFocusDist == null || minFocusDist == 0);

		if (!mNoAFRun) {
			// If there is a "continuous picture" mode available, use it, otherwise default to AUTO.
			if (contains(mCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
				builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
			} else {
				builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
			}
		}

		if (contains(mCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES), CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED))
			builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
		else
			builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);

		// If there is an auto-magical flash control mode available, use it, otherwise default to
		// the "on" mode, which is guaranteed to always be available.
		/*if (contains(mCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES), CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)) {
			builder.set(CaptureRequest.CONTROL_AE_MODE,	CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
		} else {
			builder.set(CaptureRequest.CONTROL_AE_MODE,
					CaptureRequest.CONTROL_AE_MODE_ON);
		}*/

		// If there is an auto-magical white balance control mode available, use it.
		if (contains(mCharacteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES), CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
			// Allow AWB to run auto-magically if this device supports this
			builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
		}
	}

	/**
	 * Configure the necessary {@link android.graphics.Matrix} transformation to `mTextureView`,
	 * and start/restart the preview capture session if necessary.
	 * <p/>
	 * This method should be called after the camera state has been initialized in
	 * setUpCameraOutputs.
	 *
	 * @param viewWidth  The width of `mTextureView`
	 * @param viewHeight The height of `mTextureView`
	 */
	private void configureTransform(int viewWidth, int viewHeight) {
		synchronized (mCameraStateLock) {
			if (null == mTextureView) {
				return;
			}

			StreamConfigurationMap map = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

			// For still image captures, we always use the largest available size.
			//Size largestJpeg = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
			Size largestJpeg = map.getOutputSizes(ImageFormat.JPEG)[17];
			//Log.i("Test", Integer.toString(largestJpeg.getWidth()) + Integer.toString(largestJpeg.getHeight()));
			// Find the rotation of the device relative to the native device orientation.
			int deviceRotation = getWindowManager().getDefaultDisplay().getRotation();
			Point displaySize = new Point();
			getWindowManager().getDefaultDisplay().getSize(displaySize);

			// Find the rotation of the device relative to the camera sensor's orientation.
			int totalRotation = sensorToDeviceRotation(mCharacteristics, deviceRotation);

			// Swap the view dimensions for calculation as needed if they are rotated relative to
			// the sensor.
			boolean swappedDimensions = totalRotation == 90 || totalRotation == 270;
			int rotatedViewWidth = viewWidth;
			int rotatedViewHeight = viewHeight;
			int maxPreviewWidth = displaySize.y;
			int maxPreviewHeight = displaySize.x;

			if (swappedDimensions) {
				rotatedViewWidth = viewHeight;
				rotatedViewHeight = viewWidth;
				maxPreviewWidth = displaySize.x;
				maxPreviewHeight = displaySize.y;
			}

			// Preview should not be larger than display size and 1080p.
			if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
				maxPreviewWidth = MAX_PREVIEW_WIDTH;
			}

			if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
				maxPreviewHeight = MAX_PREVIEW_HEIGHT;
			}

			// Find the best preview size for these view dimensions and configured JPEG size.
			Size previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedViewWidth, rotatedViewHeight, maxPreviewWidth, maxPreviewHeight, largestJpeg);


			if (swappedDimensions) {
				mTextureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
			} else {
				mTextureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
			}

			// Find rotation of device in degrees (reverse device orientation for front-facing
			// cameras).
			int rotation = (mCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
					CameraCharacteristics.LENS_FACING_FRONT) ?
					(360 + ORIENTATIONS.get(deviceRotation)) % 360 :
					(360 - ORIENTATIONS.get(deviceRotation)) % 360;

			Matrix matrix = new Matrix();
			RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
			RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
			float centerX = viewRect.centerX();
			float centerY = viewRect.centerY();

			// Initially, output stream images from the Camera2 API will be rotated to the native
			// device orientation from the sensor's orientation, and the TextureView will default to
			// scaling these buffers to fill it's view bounds.  If the aspect ratios and relative
			// orientations are correct, this is fine.
			//
			// However, if the device orientation has been rotated relative to its native
			// orientation so that the TextureView's dimensions are swapped relative to the
			// native device orientation, we must do the following to ensure the output stream
			// images are not incorrectly scaled by the TextureView:
			//   - Undo the scale-to-fill from the output buffer's dimensions (i.e. its dimensions
			//     in the native device orientation) to the TextureView's dimension.
			//   - Apply a scale-to-fill from the output buffer's rotated dimensions
			//     (i.e. its dimensions in the current device orientation) to the TextureView's
			//     dimensions.
			//   - Apply the rotation from the native device orientation to the current device
			//     rotation.
			if (Surface.ROTATION_90 == deviceRotation || Surface.ROTATION_270 == deviceRotation) {
				bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
				matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
				float scale = Math.max(
						(float) viewHeight / previewSize.getHeight(),
						(float) viewWidth / previewSize.getWidth());
				matrix.postScale(scale, scale, centerX, centerY);

			}
			matrix.postRotate(rotation, centerX, centerY);

			mTextureView.setTransform(matrix);

			// Start or restart the active capture session if the preview was initialized or
			// if its aspect ratio changed significantly.
			if (mPreviewSize == null || !checkAspectsEqual(previewSize, mPreviewSize)) {
				mPreviewSize = previewSize;
				if (mState != STATE_CLOSED) {
					createCameraPreviewSessionLocked();
					Log.i("CameraSize", "configureTransform");
				}
			}
		}
	}

	/**
	 * Initiate a still image capture.
	 * <p/>
	 * This function sends a capture request that initiates a pre-capture sequence in our state
	 * machine that waits for auto-focus to finish, ending in a "locked" state where the lens is no
	 * longer moving, waits for auto-exposure to choose a good exposure value, and waits for
	 * auto-white-balance to converge.
	 */
	private void takePicture() {

		Log.i("CameraSize", "pass1");
		synchronized (mCameraStateLock) {
			mPendingUserCaptures++;
			Log.i("CameraSize", "pass2");
			//Log.i("CameraSize", Integer.toString(mPreviewSize.getHeight()));
			//Toast.makeText(getApplication(), "사진 촬영", Toast.LENGTH_LONG).show();
			// If we already triggered a pre-capture sequence, or are in a state where we cannot
			// do this, return immediately.
			if (mState != STATE_PREVIEW) {
				return;
			}

			try {
				// Trigger an auto-focus run if camera is capable. If the camera is already focused,
				// this should do nothing.
				if (!mNoAFRun) {
					mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
				} else
					Log.i("CameraSize", "error1");

				// If this is not a legacy device, we can also trigger an auto-exposure metering
				// run.
				if (!isLegacyLocked()) {
					// Tell the camera to lock focus.
					mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
				} else
					Log.i("CameraSize", "error2");

				// Update state machine to wait for auto-focus, auto-exposure, and
				// auto-white-balance (aka. "3A") to converge.
				mState = STATE_WAITING_FOR_3A_CONVERGENCE;

				// Start a timer for the pre-capture sequence.
				startTimerLocked();

				// Replace the existing repeating request with one with updated 3A triggers.
				mCaptureSession.capture(mPreviewRequestBuilder.build(), mPreCaptureCallback, mBackgroundHandler);
			} catch (CameraAccessException e) {
				e.printStackTrace();

				Log.i("CameraSize", "error3");
			}

			result_text.setVisibility(View.VISIBLE);

			if (SullivanResourceData.Curr_Mode.equals(getString(R.string.mode_color))) {
				Palette palette;
				ArrayList<Palette.Swatch> swatchList = new ArrayList<>();

				if (SullivanResourceData.isColorOne) {
					Bitmap captureBitmap = Bitmap.createBitmap(mTextureView.getBitmap(), mTextureView.getWidth() / 3, mTextureView.getHeight() / 5 * 2, mTextureView.getBitmap().getWidth() / 3, mTextureView.getHeight() / 5);
					palette = CreatePaletteSync(captureBitmap);
				} else {
					palette = CreatePaletteSync(mTextureView.getBitmap());
				}
				swatchList.add(palette.getLightVibrantSwatch());
				swatchList.add(palette.getVibrantSwatch());
				swatchList.add(palette.getDarkVibrantSwatch());
				swatchList.add(palette.getLightMutedSwatch());
				swatchList.add(palette.getMutedSwatch());

				ColorUtils colorUtils = new ColorUtils(getApplicationContext());

				if (SullivanResourceData.isColorOne) {
					int i = 0;
					while (i < swatchList.size()) {
						if (swatchList.get(i) != null) {
							Palette.Swatch colorSwatch = swatchList.get(i);
							int red = Color.red(colorSwatch.getRgb());
							int green = Color.green(colorSwatch.getRgb());
							int blue = Color.blue(colorSwatch.getRgb());

							showResult(colorUtils.getColorNameFromRgb(red, green, blue));
//							result_text.setText(colorUtils.getColorNameFromRgb(red, green, blue));

							break;
						}
						if (i == swatchList.size() - 1)
							showResult(R.string.color_fail);
//							result_text.setText(getString(R.string.color_fail));
						i++;
					}
				} else {
					StringBuilder stringBuilder = new StringBuilder();

					for (int i = 0; i < swatchList.size(); i++) {
						if (swatchList.get(i) != null) {
							Palette.Swatch colorSwatch = swatchList.get(i);
							int red = Color.red(colorSwatch.getRgb());
							int green = Color.green(colorSwatch.getRgb());
							int blue = Color.blue(colorSwatch.getRgb());

							stringBuilder.append(colorUtils.getColorNameFromRgb(red, green, blue));
							if (i != 4)
								stringBuilder.append(", ");
						}
					}

					if (stringBuilder.equals(""))
						showResult(R.string.color_fail);
//						result_text.setText(R.string.color_fail);
					else
						showResult(stringBuilder.toString());
//						result_text.setText(stringBuilder);
				}
			}
//			else
//				result_text.setText("Hello");
		}
	}

	/**
	 * Send a capture request to the camera device that initiates a capture targeting the JPEG and
	 * RAW outputs.
	 * <p/>
	 * Call this only with {@link #mCameraStateLock} held.
	 */
	private void captureStillPictureLocked() {
		try {
			if (null == mCameraDevice) {
				return;
			}
			// This is the CaptureRequest.Builder that we use to take a picture.
			final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

			captureBuilder.addTarget(mJpegImageReader.get().getSurface());

			// Use the same AE and AF modes as the preview.
			setup3AControlsLocked(captureBuilder);

			// Set orientation.
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, sensorToDeviceRotation(mCharacteristics, rotation));

			// Set request tag to easily track results in callbacks.
			captureBuilder.setTag(mRequestCounter.getAndIncrement());

			CaptureRequest request = captureBuilder.build();

			// Create an ImageSaverBuilder in which to collect results, and add it to the queue
			// of active requests.
			ImageSaver.ImageSaverBuilder jpegBuilder = new ImageSaver.ImageSaverBuilder(this).setCharacteristics(mCharacteristics);

			mJpegResultQueue.put((int) request.getTag(), jpegBuilder);

			Log.i("CameraSize", "captureStillPictureLocked");

			isPrecessing = true;

			mCaptureSession.capture(request, mCaptureCallback, mBackgroundHandler);

		} catch (CameraAccessException e) {
			Log.i("CameraSize", "mCaptureCallback_Exception");

			e.printStackTrace();
		}
	}

	/**
	 * Called after a RAW/JPEG capture has completed; resets the AF trigger state for the
	 * pre-capture sequence.
	 * <p/>
	 * Call this only with {@link #mCameraStateLock} held.
	 */
	private void finishedCaptureLocked() {
		try {
			// Reset the auto-focus trigger in case AF didn't run quickly enough.
			if (!mNoAFRun) {
				mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

				mCaptureSession.capture(mPreviewRequestBuilder.build(), mPreCaptureCallback, mBackgroundHandler);

				mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);

				Toast.makeText(getApplication(), "사진 저장 완료", Toast.LENGTH_LONG).show();
				isPrecessing = false;
				faceDetector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(true).build();
				textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve the next {@link Image} from a reference counted {@link ImageReader}, retaining
	 * that {@link ImageReader} until that {@link Image} is no longer in use, and set this
	 * {@link Image} as the result for the next request in the queue of pending requests.  If
	 * all necessary information is available, begin saving the image to a file in a background
	 * thread.
	 *
	 * @param pendingQueue the currently active requests.
	 * @param reader       a reference counted wrapper containing an {@link ImageReader} from which
	 *                     to acquire an image.
	 */
	private void dequeueAndSaveImage(TreeMap<Integer, ImageSaver.ImageSaverBuilder> pendingQueue, RefCountedAutoCloseable<ImageReader> reader) {
		synchronized (mCameraStateLock) {
			Log.i("Test", "dequeue And save Image");

			Map.Entry<Integer, ImageSaver.ImageSaverBuilder> entry = pendingQueue.firstEntry();
			ImageSaver.ImageSaverBuilder builder = entry.getValue();

			// Increment reference count to prevent ImageReader from being closed while we
			// are saving its Images in a background thread (otherwise their resources may
			// be freed while we are writing to a file).
			if (reader == null || reader.getAndRetain() == null) {
				pendingQueue.remove(entry.getKey());
				return;
			}

			Image image;
			try {
				image = reader.get().acquireNextImage();
			} catch (IllegalStateException e) {
				Log.e(TAG, "Too many images queued for saving, dropping image for request: " + entry.getKey());
				pendingQueue.remove(entry.getKey());
				return;
			}

			builder.setRefCountedReader(reader).setImage(image);

			handleCompletionLocked(entry.getKey(), builder, pendingQueue);
		}
	}


	/**
	 * Runnable that saves an {@link Image} into the specified {@link File}, and updates
	 * {@link android.provider.MediaStore} to include the resulting file.
	 * <p/>
	 * This can be constructed through an {@link ImageSaver.ImageSaverBuilder} as the necessary image and
	 * result information becomes available.
	 */
	private static class ImageSaver implements Runnable {
		/**
		 * The image to save.
		 */
		private final Image mImage;
		/**
		 * The file we save the image into.
		 */
		private final File mFile;

		/**
		 * The CaptureResult for this image capture.
		 */
		private final CaptureResult mCaptureResult;

		/**
		 * The CameraCharacteristics for this camera device.
		 */
		private final CameraCharacteristics mCharacteristics;

		/**
		 * The Context to use when updating MediaStore with the saved images.
		 */
		private final Context mContext;

		/**
		 * A reference counted wrapper for the ImageReader that owns the given image.
		 */
		private final RefCountedAutoCloseable<ImageReader> mReader;

		private ImageSaver(Image image, File file, CaptureResult result, CameraCharacteristics characteristics, Context context, RefCountedAutoCloseable<ImageReader> reader) {
			mImage = image;
			mFile = file;
			mCaptureResult = result;
			mCharacteristics = characteristics;
			mContext = context;
			mReader = reader;
		}


		@Override
		public void run() {
			boolean success = false;
			int format = mImage.getFormat();
			switch (format) {
				case ImageFormat.JPEG: {
					ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
					byte[] bytes = new byte[buffer.remaining()];
					buffer.get(bytes);
					FileOutputStream output = null;
					try {
						output = new FileOutputStream(mFile);
						output.write(bytes);
						success = true;
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						mImage.close();
						closeOutput(output);
					}
					break;
				}
				default: {
					Log.e(TAG, "Cannot save image, unexpected image format:" + format);
					break;
				}
			}

			// Decrement reference count to allow ImageReader to be closed to free up resources.
			mReader.close();

			// If saving the file succeeded, update MediaStore.
			if (success) {
				MediaScannerConnection.scanFile(mContext, new String[]{mFile.getPath()},
						/*mimeTypes*/null, new MediaScannerConnection.MediaScannerConnectionClient() {
							@Override
							public void onMediaScannerConnected() {
								// Do nothing
							}

							@Override
							public void onScanCompleted(String path, Uri uri) {
								Log.i(TAG, "Scanned " + path + ":");
								Log.i(TAG, "-> uri=" + uri);
							}
						});
			}
		}

		/**
		 * Builder class for constructing {@link ImageSaver}s.
		 * <p/>
		 * This class is thread safe.
		 */
		public static class ImageSaverBuilder {
			private Image mImage;
			private File mFile;
			private CaptureResult mCaptureResult;
			private CameraCharacteristics mCharacteristics;
			private Context mContext;
			private RefCountedAutoCloseable<ImageReader> mReader;

			/**
			 * Construct a new ImageSaverBuilder using the given {@link Context}.
			 *
			 * @param context a {@link Context} to for accessing the
			 *                {@link android.provider.MediaStore}.
			 */
			public ImageSaverBuilder(final Context context) {
				mContext = context;
			}

			public synchronized ImageSaver.ImageSaverBuilder setRefCountedReader(
					RefCountedAutoCloseable<ImageReader> reader) {
				if (reader == null) throw new NullPointerException();

				mReader = reader;
				return this;
			}

			public synchronized ImageSaver.ImageSaverBuilder setImage(final Image image) {
				if (image == null) throw new NullPointerException();
				mImage = image;
				return this;
			}

			public synchronized ImageSaver.ImageSaverBuilder setFile(final File file) {
				if (file == null) throw new NullPointerException();
				mFile = file;
				return this;
			}

			public synchronized ImageSaver.ImageSaverBuilder setResult(final CaptureResult result) {
				if (result == null) throw new NullPointerException();
				mCaptureResult = result;
				return this;
			}

			public synchronized ImageSaver.ImageSaverBuilder setCharacteristics(
					final CameraCharacteristics characteristics) {
				if (characteristics == null) throw new NullPointerException();
				mCharacteristics = characteristics;
				return this;
			}

			public synchronized ImageSaver buildIfComplete() {
				if (!isComplete()) {
					return null;
				}
				return new ImageSaver(mImage, mFile, mCaptureResult, mCharacteristics, mContext,
						mReader);
			}

			public synchronized String getSaveLocation() {
				return (mFile == null) ? "Unknown" : mFile.toString();
			}

			private boolean isComplete() {
				return mImage != null && mFile != null && mCaptureResult != null
						&& mCharacteristics != null;
			}
		}

	}

	static class CompareSizesByArea implements Comparator<Size> {

		@Override
		public int compare(Size lhs, Size rhs) {
			// We cast here to ensure the multiplications won't overflow
			return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
					(long) rhs.getWidth() * rhs.getHeight());
		}

	}

	public static class RefCountedAutoCloseable<T extends AutoCloseable> implements AutoCloseable {
		private T mObject;
		private long mRefCount = 0;

		/**
		 * Wrap the given object.
		 *
		 * @param object an object to wrap.
		 */
		public RefCountedAutoCloseable(T object) {
			if (object == null) throw new NullPointerException();
			mObject = object;
		}

		/**
		 * Increment the reference count and return the wrapped object.
		 *
		 * @return the wrapped object, or null if the object has been released.
		 */
		public synchronized T getAndRetain() {
			if (mRefCount < 0) {
				return null;
			}
			mRefCount++;
			return mObject;
		}

		/**
		 * Return the wrapped object.
		 *
		 * @return the wrapped object, or null if the object has been released.
		 */
		public synchronized T get() {
			return mObject;
		}

		/**
		 * Decrement the reference count and release the wrapped object if there are no other
		 * users retaining this object.
		 */
		@Override
		public synchronized void close() {
			if (mRefCount >= 0) {
				mRefCount--;
				if (mRefCount < 0) {
					try {
						mObject.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						mObject = null;
					}
				}
			}
		}
	}

	/**
	 * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
	 * is at least as large as the respective texture view size, and that is at most as large as the
	 * respective max size, and whose aspect ratio matches with the specified value. If such size
	 * doesn't exist, choose the largest one that is at most as large as the respective max size,
	 * and whose aspect ratio matches with the specified value.
	 *
	 * @param choices           The list of sizes that the camera supports for the intended output
	 *                          class
	 * @param textureViewWidth  The width of the texture view relative to sensor coordinate
	 * @param textureViewHeight The height of the texture view relative to sensor coordinate
	 * @param maxWidth          The maximum width that can be chosen
	 * @param maxHeight         The maximum height that can be chosen
	 * @param aspectRatio       The aspect ratio
	 * @return The optimal {@code Size}, or an arbitrary one if none were big enough
	 */
	private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {
		// Collect the supported resolutions that are at least as big as the preview Surface
		List<Size> bigEnough = new ArrayList<>();
		// Collect the supported resolutions that are smaller than the preview Surface
		List<Size> notBigEnough = new ArrayList<>();
		int w = aspectRatio.getWidth();
		int h = aspectRatio.getHeight();
		for (Size option : choices) {
			if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight && option.getHeight() == option.getWidth() * h / w) {
				if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
					bigEnough.add(option);
				} else {
					notBigEnough.add(option);
				}
				//Log.d("CameraSize", "optionmal size : "+option.toString());
			}
		}

		// Pick the smallest of those big enough. If there is no one big enough, pick the
		// largest of those not big enough.
		if (bigEnough.size() > 0) {
			return Collections.min(bigEnough, new CompareSizesByArea());
		} else if (notBigEnough.size() > 0) {
			return notBigEnough.get(1);
			//return Collections.max(notBigEnough, new CompareSizesByArea());
		} else {
			Log.e(TAG, "Couldn't find any suitable preview size");
			return choices[0];
		}
	}

	/**
	 * Generate a string containing a formatted timestamp with the current date and time.
	 *
	 * @return a {@link String} representing a time.
	 */
	private static String generateTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);
		return sdf.format(new Date());
	}

	/**
	 * Cleanup the given {@link OutputStream}.
	 *
	 * @param outputStream the stream to close.
	 */
	private static void closeOutput(OutputStream outputStream) {
		if (null != outputStream) {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Return true if the given array contains the given integer.
	 *
	 * @param modes array to check.
	 * @param mode  integer to get for.
	 * @return true if the array contains the given integer, otherwise false.
	 */
	private static boolean contains(int[] modes, int mode) {
		if (modes == null) {
			return false;
		}
		for (int i : modes) {
			if (i == mode) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if the two given {@link Size}s have the same aspect ratio.
	 *
	 * @param a first {@link Size} to compare.
	 * @param b second {@link Size} to compare.
	 * @return true if the sizes have the same aspect ratio, otherwise false.
	 */
	private static boolean checkAspectsEqual(Size a, Size b) {
		double aAspect = a.getWidth() / (double) a.getHeight();
		double bAspect = b.getWidth() / (double) b.getHeight();
		return Math.abs(aAspect - bAspect) <= ASPECT_RATIO_TOLERANCE;
	}

	/**
	 * Rotation need to transform from the camera sensor orientation to the device's current
	 * orientation.
	 *
	 * @param c                 the {@link CameraCharacteristics} to query for the camera sensor
	 *                          orientation.
	 * @param deviceOrientation the current device orientation relative to the native device
	 *                          orientation.
	 * @return the total rotation from the sensor orientation to the current device orientation.
	 */
	private static int sensorToDeviceRotation(CameraCharacteristics c, int deviceOrientation) {


		int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

		// Get device orientation in degrees
		deviceOrientation = ORIENTATIONS.get(deviceOrientation);

		// Reverse device orientation for front-facing cameras
		if (c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
			deviceOrientation = -deviceOrientation;
		}

		// Calculate desired JPEG orientation relative to camera orientation to make
		// the image upright relative to the device orientation
		return (sensorOrientation - deviceOrientation + 360) % 360;
	}

	/**
	 * Shows a {@link Toast} on the UI thread.
	 *
	 * @param text The message to show.
	 */

	/**
	 * If the given request has been completed, remove it from the queue of active requests and
	 * send an {@link ImageSaver} with the results from this request to a background thread to
	 * save a file.
	 * <p/>
	 * Call this only with {@link #mCameraStateLock} held.
	 *
	 * @param requestId the ID of the {@link CaptureRequest} to handle.
	 * @param builder   the {@link ImageSaver.ImageSaverBuilder} for this request.
	 * @param queue     the queue to remove this request from, if completed.
	 */
	private void handleCompletionLocked(int requestId, ImageSaver.ImageSaverBuilder builder,
	                                    TreeMap<Integer, ImageSaver.ImageSaverBuilder> queue) {
		if (builder == null) return;
		ImageSaver saver = builder.buildIfComplete();
		if (saver != null) {
			queue.remove(requestId);
			AsyncTask.THREAD_POOL_EXECUTOR.execute(saver);
		}
	}

	/**
	 * Check if we are using a device that only supports the LEGACY hardware level.
	 * <p/>
	 * Call this only with {@link #mCameraStateLock} held.
	 *
	 * @return true if this is a legacy device.
	 */
	private boolean isLegacyLocked() {
		return mCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ==
				CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
	}

	/**
	 * Start the timer for the pre-capture sequence.
	 * <p/>
	 * Call this only with {@link #mCameraStateLock} held.
	 */
	private void startTimerLocked() {
		mCaptureTimer = SystemClock.elapsedRealtime();
	}

	/**
	 * Check if the timer for the pre-capture sequence has been hit.
	 * <p/>
	 * Call this only with {@link #mCameraStateLock} held.
	 *
	 * @return true if the timeout occurred.
	 */
	private boolean hitTimeoutLocked() {
		return (SystemClock.elapsedRealtime() - mCaptureTimer) > PRECAPTURE_TIMEOUT_MS;
	}


	///////////////////////////////////////////////////////////////////////////////////////////////////
	public TextView curr_mode_txt;
	ImageButton menu_btn;
	ImageButton shoot_btn;
	ImageButton addition_btn;
	public TextView result_text;
	public RelativeLayout relativeLayout;

	//////////////////menu///////////////////////
	//GridView gridView;
	Set<String> unexceptMenu = new HashSet<String>();

	RecyclerView recyclerView;
	ArrayList<MenuItem> menuItemArrayList;
	//ArrayList<MenuItem> menuItemArrayList;

	//////////////////addition///////////////////////

	final SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	int SoundID;
	FaceDetector faceDetector;
	boolean isfaceDetect;
	TextRecognizer textRecognizer;
	boolean istextDetect;

	SharedPreferences sharedPreferences;

	Boolean isTextVoiceNotice;
	Boolean isFaceVoiceNotice;

	private Vibrator vibrator;
	private static final long VIBRATOR_TIME = 50;
	Palette palette;
	Bitmap colorBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SoundID = soundPool.load(this, R.raw.warning1, 1);

		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);

		SullivanResourceData.mainActivity = this;
		Intent intent = getIntent();
		String curr_mode_data = intent.getStringExtra(getString(R.string.curr_mode));
		SullivanResourceData.Curr_Mode = curr_mode_data;

		curr_mode_txt = findViewById(R.id.curr_mode_txt);
		menu_btn = findViewById(R.id.menu_btn);
		shoot_btn = findViewById(R.id.shoot_btn);
		addition_btn = findViewById(R.id.addition_btn);
		result_text = findViewById(R.id.result_text);
		mTextureView = (AutoFitTextureView) findViewById(R.id.texture);

		menuItemArrayList = new ArrayList<>();
		recyclerView = findViewById(R.id.menu_recyclerView);

		RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
		recyclerView.setLayoutManager(layoutManager);

		relativeLayout = findViewById(R.id.main_relativelayout);

		if (curr_mode_data.equals(getString(R.string.mode_color))) {
			curr_mode_txt.setText(getString(R.string.color_one));
		} else {
			curr_mode_txt.setText(curr_mode_data);
		}
		menu_btn.setOnClickListener(this);
		shoot_btn.setOnClickListener(this);
		addition_btn.setOnClickListener(this);

		mOrientationListener = new OrientationEventListener(this,
				SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				if (mTextureView != null && mTextureView.isAvailable()) {
					configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
				}
			}
		};

		//gridView = findViewById(R.id.menu_gridview);

		MenuDataSetting();

		faceDetector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(true).build();
		textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	boolean lockdetect=false;
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.shoot_btn: {
				takePicture();

				break;
			}
			case R.id.menu_btn: {
				relativeLayout.setVisibility(View.GONE);
				//gridView.setVisibility(View.VISIBLE);
				recyclerView.setVisibility(View.VISIBLE);
				MenuDataSetting();
				//closeCamera();
				lockdetect = true;
				break;
			}
			case R.id.addition_btn: {
				relativeLayout.setVisibility(View.GONE);
				recyclerView.setVisibility(View.VISIBLE);
				AdditionDataSetting(SelectAddition(SullivanResourceData.Curr_Mode));
				//closeCamera();
				lockdetect=true;
				break;
			}
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		Log.i("Test","Restart");
		if(mState == STATE_OPENED) {
			Intent intent = new Intent(this, IntroActivity.class);
			startActivity(intent);
			finishAffinity();
		}

		SullivanResourceData.curr_activity = this;
		MenuDataSetting();
	}

	@Override
	protected void onStart() {
		super.onStart();
		SullivanResourceData.curr_activity = this;
	}

	@Override
	protected void onResume() {
		super.onResume();
		startBackgroundThread();
		Init();

		mTextureView.setVisibility(View.VISIBLE);
		if (mTextureView.isAvailable()) {
			configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
		} else {
			mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
		}
		if (mOrientationListener != null && mOrientationListener.canDetectOrientation()) {
			mOrientationListener.enable();
		}

		sharedPreferences = getSharedPreferences(SullivanResourceData.setting_data, MODE_PRIVATE);
		isTextVoiceNotice = sharedPreferences.getBoolean(getString(R.string.text_recog), true);
		isFaceVoiceNotice = sharedPreferences.getBoolean(getString(R.string.face_recog), true);
	}

	public void Init() {
		openCamera();

		lockdetect = false;
		istextDetect = false;
		isfaceDetect = false;
		result_text.setVisibility(View.INVISIBLE);

		//ActiveCameraView(SullivanResourceData.Curr_Mode);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("Test", "onPause");

		mTextureView.setVisibility(View.INVISIBLE);

		if (mOrientationListener != null) {
			mOrientationListener.disable();
		}

		vibrator.cancel();
		closeCamera();
		stopBackgroundThread();
	}

	@Override
	public void onBackPressed() {
		if (recyclerView.getVisibility() == View.VISIBLE) {
			ActiveCameraView(curr_mode_txt.getText().toString());

			lockdetect = false;
			//Init();
		} else {
			new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
					.setMessage("어플리케이션을 종료하시겠습니까?")
					.setCancelable(false)
					.setPositiveButton("확인", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finishAffinity();
						}
					})
					.setNegativeButton("취소", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).show();
		}
	}

	public void flashlight() {
		synchronized (mCameraStateLock) {
			if (mPreviewRequestBuilder != null) {
				if (SullivanResourceData.mFlash) {
					mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
					mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
				} else {
					mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
					mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
				}
			}

			try {
				if (mCaptureSession != null) {
					mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mPreCaptureCallback, mBackgroundHandler);
				}
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private void MenuDataSetting() {

		if(menuItemArrayList != null)
			menuItemArrayList.clear();

		/*MenuAdapter menuAdapter = new MenuAdapter();

		menuAdapter.mainActivity = this;
		menuAdapter.layout = R.layout.activity_menu_item;
		menuAdapter.inf = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
*/

		ArrayList<String> exceptMenuList = new ArrayList<>(SullivanResourceData.choiceMode);

		SharedPreferences sharedPreferences = getSharedPreferences(SullivanResourceData.setting_data, MODE_PRIVATE);
		unexceptMenu = sharedPreferences.getStringSet(SullivanResourceData.menu_set, SullivanResourceData.choiceMode);

		for (int i = 0; i < exceptMenuList.size(); i++) {
			ArrayList<String> arrayList = new ArrayList<>(unexceptMenu);

			for (int j = 0; j < arrayList.size(); j++) {
				if (arrayList.get(j).equals(exceptMenuList.get(i))) {
					exceptMenuList.remove(i);
				}
			}
		}
		for (int i = 0; i < getResources().getStringArray(R.array.array_mode).length; i++) {
			int count = 0;

			String[] modeArr_name = getResources().getStringArray(R.array.array_mode);
			TypedArray drawableTypedArray = getApplicationContext().getResources().obtainTypedArray(R.array.array_mode_draw);// modeArr_img = getResources().getarr(R.array.array_mode_draw);
			if (exceptMenuList.size() > 0) {
				for (int j = 0; j < exceptMenuList.size(); j++) {
					if (exceptMenuList.get(j).equals(modeArr_name[i]))
						count++;
				}
				if (count == 0) {
					MenuItem menuItem = new MenuItem(modeArr_name[i], ContextCompat.getDrawable(getApplicationContext(), drawableTypedArray.getResourceId(i, 0)));
					menuItemArrayList.add(menuItem);
				} else
					count = 0;
			} else {
				MenuItem menuItem = new MenuItem(modeArr_name[i], ContextCompat.getDrawable(getApplicationContext(), drawableTypedArray.getResourceId(i, 0)));
				menuItemArrayList.add(menuItem);
			}
		}
		//gridView.setAdapter(menuAdapter);
		recyclerView.setAdapter(new MenuAdapter(menuItemArrayList, this));
	}

	public void ActiveCameraView(String modeName) {
		relativeLayout.setVisibility(View.VISIBLE);
		recyclerView.setVisibility(View.GONE);

		curr_mode_txt.setText(SullivanResourceData.Curr_Mode);
		if (SullivanResourceData.Curr_Mode.equals(getString(R.string.mode_color))) {
			if (SullivanResourceData.isColorOne)
				curr_mode_txt.setText(getString(R.string.color_one));
			else
				curr_mode_txt.setText(getString(R.string.color_multi));
		}
	}

	public void AdditionDataSetting(int num[]) {

		if(menuItemArrayList != null)
			menuItemArrayList.clear();

		String[] modeArr_name = getResources().getStringArray(R.array.array_addition);
		TypedArray drawableTypedArray = getApplicationContext().getResources().obtainTypedArray(R.array.array_addition_draw);

		MenuItem additionItem;
		for (int i = 0; i < num.length; i++) {
			if (modeArr_name[num[i]].equals(getString(R.string.addition_flash_on))) {
				if (SullivanResourceData.mFlash == true)
					additionItem = new MenuItem(getString(R.string.addition_flash_off), getDrawable(R.drawable.addition_flashoff));
				else
					additionItem = new MenuItem(modeArr_name[num[i]], ContextCompat.getDrawable(getApplicationContext(), drawableTypedArray.getResourceId(num[i], 0)));
			} else if (modeArr_name[num[i]].equals(getString(R.string.addition_color_one)) || modeArr_name[i].equals(getString(R.string.addition_color_multi))) {
				if (SullivanResourceData.isColorOne == true)
					additionItem = new MenuItem(getString(R.string.addition_color_multi), getDrawable(R.drawable.addition_color_multi));
				else
					additionItem = new MenuItem(modeArr_name[num[i]], ContextCompat.getDrawable(getApplicationContext(), drawableTypedArray.getResourceId(num[i], 0)));
			} else if (modeArr_name[num[i]].equals(getString(R.string.addition_reverse_on))) {
				if (SullivanResourceData.isReverse == true)
					additionItem = new MenuItem(modeArr_name[num[i]], getDrawable(R.drawable.addition_color_multi));
				else
					additionItem = new MenuItem(modeArr_name[num[i]], ContextCompat.getDrawable(getApplicationContext(), drawableTypedArray.getResourceId(num[i], 0)));
			} else
				additionItem = new MenuItem(modeArr_name[num[i]], ContextCompat.getDrawable(getApplicationContext(), drawableTypedArray.getResourceId(num[i], 0)));

			menuItemArrayList.add(additionItem);
		}
		recyclerView.setAdapter(new AdditionAdapter(menuItemArrayList,this));
	}

	public int[] SelectAddition(String curr_mode) {
		int num[] = {};
		if(curr_mode.equals(getString(R.string.mode_ai)))
			num = SullivanResourceData.num_ai;
		else if(curr_mode.equals(getString(R.string.mode_text)))
			num = SullivanResourceData.num_text;
		else if(curr_mode.equals(getString(R.string.mode_face)))
			num = SullivanResourceData.num_face;
		else if(curr_mode.equals(getString(R.string.mode_image)))
			num = SullivanResourceData.num_image;
		else if(curr_mode.equals(getString(R.string.mode_color)))
			num = SullivanResourceData.num_color;
		else if(curr_mode.equals(getString(R.string.mode_light)))
			num = SullivanResourceData.num_light;
		else if(curr_mode.equals(getString(R.string.mode_magnify)))
			num = SullivanResourceData.num_glasses;
		/*switch (curr_mode) {
			case "AI 모드":
				num = SullivanResourceData.num_ai;
				break;
			case "문자 인식":
				num = SullivanResourceData.num_text;
				break;
			case "얼굴 인식":
				num = SullivanResourceData.num_face;
				break;
			case "이미지 묘사":
				num = SullivanResourceData.num_image;
				break;
			case "색상 인식":
				num = SullivanResourceData.num_color;
				break;
			case "빛 밝기":
				num = SullivanResourceData.num_light;
				break;
			case "돋보기":
				num = SullivanResourceData.num_glasses;
				break;
			default:
				num = new int[0];
		}*/
		return num;
	}

	public Palette CreatePaletteSync(Bitmap bitmap) {
		Palette palette = Palette.from(bitmap).generate();

		return palette;
	}

	private void showResult(int resourceId) {
		showResult(getString(resourceId));
	}

	private void showResult(String result) {
		runOnUiThread(() -> {
			if(result_text.getVisibility() != View.VISIBLE)
				result_text.setVisibility(View.VISIBLE);

			result_text.setText(result);
		});
	}
}
