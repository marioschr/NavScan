package com.unipi.marioschr.navscan.MainActivity;

import 	android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.unipi.marioschr.navscan.BarcodeAnalyzer;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.databinding.FragmentScanBinding;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScannerFragment extends Fragment {

	private static ScannerFragment scannerFragmentContext;
	public AtomicBoolean processingBarcode = new AtomicBoolean(false);
	private BarcodeAnalyzer barcodeAnalyzer = new BarcodeAnalyzer();
	private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
	private Executor analysisExecutor;
	private Camera camera;
	boolean toggleFlash = false;
	private FragmentScanBinding binding;
	public static ScannerFragment getScannerFragmentContext() {
		return scannerFragmentContext;
	}

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentScanBinding.inflate(inflater, container, false);
		scannerFragmentContext = this;
		analysisExecutor = Executors.newSingleThreadExecutor();
		cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
		cameraProviderFuture.addListener(() -> {
			try {
				ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
				bindPreview(cameraProvider);
			} catch (ExecutionException | InterruptedException ignored) { }
		}, ContextCompat.getMainExecutor(requireContext()));
		binding.flashlightButton.setOnClickListener(view -> Flash());
		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		binding.flashlightButton.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.ic_baseline_flash_on_24, requireContext().getTheme()));
		processingBarcode.set(false);
	}

	void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
		Preview preview = new Preview.Builder()
				.build();

		CameraSelector cameraSelector = new CameraSelector.Builder()
				.requireLensFacing(CameraSelector.LENS_FACING_BACK)
				.build();

		preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

		ImageAnalysis imageAnalysis =
				new ImageAnalysis.Builder()
						.setTargetResolution(new Size(1280, 720))
						.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
						.build();

		imageAnalysis.setAnalyzer(analysisExecutor, imageProxy -> {
			if (processingBarcode.compareAndSet(false,true)){
				barcodeAnalyzer.analyze(imageProxy);
			}
		});
		cameraProvider.unbindAll();
		camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
	}

	public void Flash() {
		camera.getCameraControl().enableTorch(toggleFlash ^= true );
		if (toggleFlash) {
			binding.flashlightButton.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.ic_baseline_flash_off_24, requireContext().getTheme()));
		} else {
			binding.flashlightButton.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.ic_baseline_flash_on_24, requireContext().getTheme()));
		}
	}
}