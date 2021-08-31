package com.unipi.marioschr.navscan;

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
                if (barcodes != null && barcodes.size() > 0) {
                    CollectionReference colRef = db.collection("locations");
                    for (Barcode barcode: barcodes) {
                        DocumentReference docRef = colRef.document(barcode.getRawValue());
                        docRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("code", barcode.getRawValue());
                                    navController.navigate(R.id.action_navigation_scanner_to_locationInfoFragment, bundle);
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        });
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
