/*
 * Copyright (C) 2015 Ravi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.jaschke.alexandria.scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import it.jaschke.alexandria.R;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class BarcodeScannerActivity extends Activity implements ZBarScannerView.ResultHandler {

    // Reference to the scanner view.
    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Create the scanner view.
        mScannerView = new ZBarScannerView(this);

        // Set the scanner view properties.
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(true);

        // Set the barcode format to EAN13.
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.EAN13);
        mScannerView.setFormats(formats);

        // Set the scanner view as the content view.
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register to receive scan result from scanner view.
        mScannerView.setResultHandler(this);

        // Start the camera.
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop the camera.
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result scanResult) {
        // Create intent to send scan result.
        Intent scanResultIntent = new Intent();

        // Put scan result data into intent.
        scanResultIntent.putExtra(getString(R.string.scan_format), scanResult.getBarcodeFormat().getName());
        scanResultIntent.putExtra(getString(R.string.scan_content), scanResult.getContents());

        // Set the result and finish.
        setResult(RESULT_OK, scanResultIntent);
        finish();
    }
}