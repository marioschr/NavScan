package com.unipi.marioschr.navscan.MainActivity;

import android.Manifest;
import 	android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import es.dmoral.toasty.Toasty;

public class ScannerFragment extends Fragment {

	private static ScannerFragment scannerFragmentContext;
	public AtomicBoolean processingBarcode = new AtomicBoolean(false);
	private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
	private Executor analysisExecutor;
	private Camera camera;
	boolean toggleFlash = false;
	private FragmentScanBinding binding;
	public static ScannerFragment getScannerFragmentContext() {
		return scannerFragmentContext;
	}
	BarcodeAnalyzer barcodeAnalyzer;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentScanBinding.inflate(inflater, container, false);
		ActivityResultLauncher<String[]> requestPermissionLauncher =
				registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
					if (!isGranted.get(Manifest.permission.CAMERA)) {
						Toasty.error(getContext(), getString(R.string.cant_continue_without_camera_permission), Toasty.LENGTH_LONG).show();
					}
					if (!isGranted.get(Manifest.permission.ACCESS_FINE_LOCATION)) {
						Toasty.error(getContext(), getString(R.string.cant_continue_without_location_permission), Toasty.LENGTH_LONG).show();
					}
					barcodeAnalyzer = new BarcodeAnalyzer();
					analysisExecutor = Executors.newSingleThreadExecutor();
					cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
					cameraProviderFuture.addListener(() -> {
						try {
							ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
							bindPreview(cameraProvider);
						} catch (ExecutionException | InterruptedException ignored) { }
					}, ContextCompat.getMainExecutor(requireContext()));
				});
		scannerFragmentContext = this;
		requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION});
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