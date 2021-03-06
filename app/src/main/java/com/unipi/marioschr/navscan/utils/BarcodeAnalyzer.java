package com.unipi.marioschr.navscan.utils;

import android.annotation.SuppressLint;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.unipi.marioschr.navscan.MainActivity.ScannerFragment;
import com.unipi.marioschr.navscan.R;

import es.dmoral.toasty.Toasty;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
    BarcodeScannerOptions options =
            new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build();
    BarcodeScanner barcodeScanner = BarcodeScanning.getClient(options);
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    NavController navController = Navigation.findNavController(ScannerFragment.getScannerFragmentContext().getActivity() , R.id.nav_host_fragment_main);

    @Override
    public void analyze(ImageProxy imageProxy) {
        @SuppressLint({"UnsafeOptInUsageError"})
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            String TAG = "Document";
            barcodeScanner.process(image).addOnSuccessListener(barcodes -> {
                if (!barcodes.isEmpty()) {
                    CollectionReference colRef = db.collection("locations");
                    for (Barcode barcode: barcodes) {
                        if (!barcode.getRawValue().contains("/")) {
                            DocumentReference docRef = colRef.document(barcode.getRawValue());
                            docRef.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString("code", barcode.getRawValue());
                                        if (navController.getCurrentDestination().getId() == R.id.navigation_scanner) {
                                            navController.navigate(R.id.action_navigation_scanner_to_locationInfoFragment, bundle);
                                        }
                                    } else {
                                        Log.d(TAG, "No such document");
                                        Toasty.warning(ScannerFragment.getScannerFragmentContext().requireActivity(), "This QR-code doesn't match with any of the available locations.",Toasty.LENGTH_LONG).show();
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            });
                        } else {
                            Toasty.warning(ScannerFragment.getScannerFragmentContext().requireActivity(), "This QR-code doesn't match with any of the available locations.",Toasty.LENGTH_LONG).show();
                        }
                    }
                } else {
                    ScannerFragment.getScannerFragmentContext().processingBarcode.set(false);
                }
            })
            .addOnFailureListener(e -> {
                // Process failed
            })
            .addOnCompleteListener(task -> imageProxy.close());
        }
    }
}
