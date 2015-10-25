package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.scanner.BarcodeScannerActivity;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;

public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Loader to fetch book details from database.
    private static final int LOADER_ID = 1;

    // Request code for starting scan activity.
    private static final int START_SCAN_ACTIVITY = 1;

    private static final int ISBN10_LENGTH = 10;
    private static final int ISBN13_LENGTH = 13;

    private View mRootView;
    private EditText mEanEditText;

    public AddBook() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEanEditText != null) {
            outState.putString(getString(R.string.ean_add_book_key),
                    mEanEditText.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        mEanEditText = (EditText) mRootView.findViewById(R.id.ean);

        mEanEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == ISBN10_LENGTH &&
                        !ean.startsWith(getString(R.string.prefix_isbn_13))) {
                    ean = getString(R.string.prefix_isbn_13) + ean;
                }
                if (ean.length() < ISBN13_LENGTH) {
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoader();
            }
        });

        mRootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the package manager.
                PackageManager pm = getActivity().getPackageManager();

                // Check if there is a rear camera on the device.
                if(pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    // Start the barcode scanner activity.
                    Intent scanIntent = new Intent(getActivity(), BarcodeScannerActivity.class);

                    startActivityForResult(scanIntent, START_SCAN_ACTIVITY);
                } else {
                    // Display toast to user that the device does not have a rear camera.
                    Context context = getActivity();
                    Toast.makeText(context, context.getString(R.string.no_rear_camera),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mRootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEanEditText.setText("");
            }
        });

        mRootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mEanEditText.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                mEanEditText.setText("");
            }
        });

        if (savedInstanceState != null) {
            mEanEditText.setText(savedInstanceState.getString(getString(R.string.ean_add_book_key)));
            mEanEditText.setHint("");
        }

        return mRootView;
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mEanEditText.getText().length() == 0) {
            return null;
        }
        String eanStr = mEanEditText.getText().toString();
        if (eanStr.length() == ISBN10_LENGTH &&
                !eanStr.startsWith(getString(R.string.prefix_isbn_13))) {
            eanStr = getString(R.string.prefix_isbn_13) + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        ((TextView) mRootView.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) mRootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage((ImageView) mRootView.findViewById(R.id.bookCover)).execute(imgUrl);
            mRootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) mRootView.findViewById(R.id.categories)).setText(categories);

        mRootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.authors)).setText("");
        ((TextView) mRootView.findViewById(R.id.categories)).setText("");
        mRootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Confirm the result is from scan activity and a success.
        if (requestCode == START_SCAN_ACTIVITY && resultCode == Activity.RESULT_OK) {
            // Extract scan result from intent.
            String scanFormat = data.getStringExtra(getString(R.string.scan_format));
            String scanContents = data.getStringExtra(getString(R.string.scan_contents));

            // Set the scan contents onto the edit text.
            mEanEditText.setText(scanContents);

            Toast.makeText(getActivity(), getString(R.string.scan_format) + scanFormat + "\n"
                    + getString(R.string.scan_contents) + scanContents, Toast.LENGTH_SHORT).show();
        }
    }
}
