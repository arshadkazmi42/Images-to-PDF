package swati4star.createpdf.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import swati4star.createpdf.R;
import swati4star.createpdf.adapter.ImageFiltersAdapter;
import swati4star.createpdf.interfaces.OnFilterItemClickedListener;
import swati4star.createpdf.model.FilterItem;

import static swati4star.createpdf.util.Constants.IMAGE_EDITOR_KEY;
import static swati4star.createpdf.util.Constants.RESULT;
import static swati4star.createpdf.util.ImageFilterUtils.getFiltersList;

public class ImageEditor extends AppCompatActivity implements OnFilterItemClickedListener {

    private ArrayList<String> mFilterUris = new ArrayList<>();
    private final ArrayList<String> mImagepaths = new ArrayList<>();
    private ArrayList<FilterItem> mFilterItems;

    private int mImagesCount;
    private int mDisplaySize;
    private int mCurrentImage = 0;
    private String mFilterName;

    @BindView(R.id.nextimageButton)
    ImageButton mNextButton;
    @BindView(R.id.imagecount)
    TextView mImgcount;
    @BindView(R.id.savecurrent)
    Button saveCurrent;
    @BindView(R.id.previousImageButton)
    ImageButton mPreviousButton;
    @BindView(R.id.resetCurrent)
    Button resetCurrent;
    SeekBar doodleSeekBar;

    private Bitmap mBitmap;
    private PhotoEditorView mPhotoEditorView;
    private boolean mClicked = false;
    private boolean mClickedFilter = false;
    private boolean mIsLast = false;

    private PhotoEditor mPhotoEditor;
    Button mDoodleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_editor);
        ButterKnife.bind(this);

        mPhotoEditorView = findViewById(R.id.photoEditorView);

        // Extract images
        mFilterUris = getIntent().getExtras().getStringArrayList(IMAGE_EDITOR_KEY);
        mDisplaySize = mFilterUris.size();
        mImagesCount = mFilterUris.size() - 1;
        mBitmap = BitmapFactory.decodeFile(mFilterUris.get(0));
        mPhotoEditorView.getSource().setImageBitmap(mBitmap);
        String showingText = "Showing " + String.valueOf(1) + " of " + mDisplaySize;
        mImgcount.setText(showingText);
        mPreviousButton.setVisibility(View.INVISIBLE);
        mFilterItems = getFiltersList(this);
        mImagepaths.addAll(mFilterUris);
        initRecyclerView();
        mDoodleButton = findViewById(R.id.doodleButton);
        doodleSeekBar = findViewById(R.id.doodleSeekBar);
        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true)
                .build();
        mPhotoEditor.setBrushSize(30);
        doodleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPhotoEditor.setBrushSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mPhotoEditor.setBrushDrawingMode(false);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.doodle_color_white)
    void setColorWhite() {
        doodleSeekBar.setBackgroundColor(getResources().getColor(R.color.mb_white));
        mPhotoEditor.setBrushColor(getResources().getColor(R.color.mb_white));
    }

    @OnClick(R.id.doodle_color_black)
    void setColorBlack() {
        doodleSeekBar.setBackgroundColor(getResources().getColor(R.color.black_87));
        mPhotoEditor.setBrushColor(getResources().getColor(R.color.black_87));
    }

    @OnClick(R.id.doodle_color_red)
    void setColorRed() {
        doodleSeekBar.setBackgroundColor(getResources().getColor(R.color.red));
        mPhotoEditor.setBrushColor(getResources().getColor(R.color.red));
    }

    @OnClick(R.id.doodle_color_blue)
    void setColorBlue() {
        doodleSeekBar.setBackgroundColor(getResources().getColor(R.color.mb_blue));
        mPhotoEditor.setBrushColor(getResources().getColor(R.color.mb_blue));
    }

    @OnClick(R.id.doodle_color_green)
    void setColorGreen() {
        doodleSeekBar.setBackgroundColor(getResources().getColor(R.color.mb_green));
        mPhotoEditor.setBrushColor(getResources().getColor(R.color.mb_green));
    }

    @OnClick(R.id.nextimageButton)
    void nextImg() {
        //Proceed to next if Save Current has been clicked
        if (mClicked) {
            next();
            incrementImageCount();
        } else
            Toast.makeText(getApplicationContext(), R.string.save_first, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.previousImageButton)
    void previousImg() {
        //move to previous if Save Current has been clicked
        if (mClicked) {
            previous();
            decrementImageCount();
        } else
            Toast.makeText(getApplicationContext(), R.string.save_first, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.savecurrent)
    void saveC() {
        mClicked = true;
        if (mClickedFilter) {
            saveCurrentImage();
        } else {
            applyFilter(PhotoFilter.NONE);
            saveCurrentImage();
        }
    }

    @OnClick(R.id.resetCurrent)
    void resetCurrent() {
        String originalPath = mFilterUris.get(mCurrentImage);
        if (!mImagepaths.contains(originalPath)) {
            mImagepaths.remove(mCurrentImage);
            mImagepaths.add(mCurrentImage, originalPath);
            mBitmap = BitmapFactory.decodeFile(originalPath);
            mPhotoEditorView.getSource().setImageBitmap(mBitmap);
            mPhotoEditor.clearAllViews();
            mPhotoEditor.undo();
        }
    }

    /**
     * Increment image count to display in textView
     */
    private void incrementImageCount() {
        if (mCurrentImage < mImagesCount) {
            setImageCount();
            mPreviousButton.setVisibility(View.VISIBLE);
        } else if (mCurrentImage == mImagesCount) {
            setImageCount();
            mNextButton.setVisibility(View.INVISIBLE);
            mPreviousButton.setVisibility(View.VISIBLE);
            mIsLast = true;
        } else {
            mNextButton.setEnabled(false);
        }
    }

    /**
     * Decrement image count to display in textView
     */
    private void decrementImageCount() {
        if (mCurrentImage > 0) {
            setImageCount();
            mNextButton.setVisibility(View.VISIBLE);
        } else if (mCurrentImage == 0) {
            setImageCount();
            mPreviousButton.setVisibility(View.INVISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
        } else {
            mPreviousButton.setEnabled(false);
        }
    }

    private void setImageCount() {
        String sText = "Showing " + String.valueOf(mCurrentImage + 1) + " of " + mDisplaySize;
        mImgcount.setText(sText);
    }

    /**
     * Saves Current Image with applied filter
     */
    private void saveCurrentImage() {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/PDFfilter");
            dir.mkdirs();
            String fileName = String.format(getString(R.string.filter_file_name),
                    String.valueOf(System.currentTimeMillis()), mFilterName);
            File outFile = new File(dir, fileName);
            String imagePath = outFile.getAbsolutePath();

            mPhotoEditor.saveAsFile(imagePath, new PhotoEditor.OnSaveListener() {
                @Override
                public void onSuccess(@NonNull String imagePath) {
                    mImagepaths.remove(mCurrentImage);
                    mImagepaths.add(mCurrentImage, imagePath);
                    Toast.makeText(getApplicationContext(), R.string.saving_dialog, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("imgFilter", "Failed to save");
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Store imagepaths for creation of PDF when Done
     */
    private void done() {
        if (!mClicked) {
            passUris(mFilterUris);
        } else {
            passUris(mImagepaths);
        }
    }

    /**
     * Intent to Send Back final edited URIs
     *
     * @param mImagepaths - the images array to be send pack
     */
    private void passUris(ArrayList<String> mImagepaths) {
        Intent returnIntent = new Intent();
        returnIntent.putStringArrayListExtra(RESULT, mImagepaths);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    /**
     * Display next image on nextImage button click
     */
    private void next() {
        try {
            if (mCurrentImage + 1 <= mImagesCount) {
                mBitmap = BitmapFactory.decodeFile(mImagepaths.get(mCurrentImage + 1));
                mPhotoEditorView.getSource().setImageBitmap(mBitmap);
                mCurrentImage++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Display Previous image on previousImage button click
     */
    private void previous() {
        try {
            if (mCurrentImage - 1 >= 0) {
                mBitmap = BitmapFactory.decodeFile(mImagepaths.get((mCurrentImage - 1)));
                mPhotoEditorView.getSource().setImageBitmap(mBitmap);
                mCurrentImage--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize Recycler View
     */
    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        ImageFiltersAdapter adapter = new ImageFiltersAdapter(mFilterItems, this, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Get Item Position and call Filter Function
     *
     * @param view     - view which is clicked
     * @param position - position of item clicked
     */
    @Override
    public void onItemClick(View view, int position) {
        PhotoFilter filter = mFilterItems.get(position).getFilter();
        applyFilter(filter);
    }

    /**
     * Apply Filter to Image
     */
    private void applyFilter(PhotoFilter filterType) {
        try {
            mClickedFilter = true;
            mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                    .setPinchTextScalable(true)
                    .build();
            mPhotoEditor.setFilterEffect(filterType);
            mFilterName = filterType.name();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.finish:
                done();
                return true;
            case android.R.id.home:
                cancelFilter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void cancelFilter() {
        new MaterialDialog.Builder(this)
                .onPositive((dialog, which) -> finish())
                .title(R.string.filter_cancel_question)
                .content(R.string.filter_cancel_description)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel).show();

    }

    @OnClick(R.id.doodleButton)
    public void doodleEffect() {

        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true)
                .build();
        LinearLayout colorLayout = findViewById(R.id.doodle_colors);
        if (doodleSeekBar.getVisibility() == View.GONE && colorLayout.getVisibility() == View.GONE) {
            mPhotoEditor.setBrushDrawingMode(true);
            doodleSeekBar.setVisibility(View.VISIBLE);
            mDoodleButton.setText(R.string.disable_doodle_effect);
            mDoodleButton.setBackgroundColor(getResources().getColor(R.color.mb_white));
            colorLayout.setVisibility(View.VISIBLE);
        } else if (doodleSeekBar.getVisibility() == View.VISIBLE &&
                colorLayout.getVisibility() == View.VISIBLE) {
            mPhotoEditor.setBrushDrawingMode(false);
            doodleSeekBar.setVisibility(View.GONE);
            mDoodleButton.setText(R.string.enable_doodle_effect);
            mDoodleButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
            colorLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        done();
    }
}