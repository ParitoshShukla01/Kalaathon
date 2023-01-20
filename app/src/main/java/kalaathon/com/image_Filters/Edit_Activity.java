package kalaathon.com.image_Filters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kalaathon.com.Interface.EditImageFragmentListener;
import kalaathon.com.Interface.FiltersListFragmentListener;
import kalaathon.com.R;
import kalaathon.com.upload.shareActivity;
import kalaathon.com.utils.SquareImageView;

public class Edit_Activity extends AppCompatActivity implements FiltersListFragmentListener, EditImageFragmentListener {

    ImageView back;
    SquareImageView imagePreview;
    TabLayout tabLayout;
    ViewPager viewPager;
    TextView mTextView,save;
    RelativeLayout mRelativeLayout;
    Bitmap originalImage,filteredImage,finalImage;
    FiltersListFragment filtersListFragment;
    EditImageFragment editImageFragment;
    int brightnessFinal = 0;
    float saturationFinal = 1.0f;
    float contrastFinal = 1.0f;
    private String contest;
    private String savepath;
    private String desc;
    private int pos;

    // load native image filters library
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit__acitivity);

        mRelativeLayout=findViewById(R.id.rel_edit_image);
        mTextView=findViewById(R.id.text_edit_image);
        save=findViewById(R.id.save_edit_image);
        back=findViewById(R.id.back_edit_image);
        viewPager=findViewById(R.id.viewpager);
        imagePreview=findViewById(R.id.image_preview);
        tabLayout=findViewById(R.id.tabs);
        tabLayout.setVisibility(View.GONE);
        mTextView.setVisibility(View.VISIBLE);

        String path=getIntent().getStringExtra("path");
        contest=getIntent().getStringExtra("contest");
        desc=getIntent().getStringExtra("desc");
        pos=getIntent().getIntExtra("pos",-1);

        loadImage(path);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);


        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextView.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                filtersListFragment.prepareThumbnail(originalImage);
                mRelativeLayout.setOnClickListener(null);
                mTextView.setOnClickListener(null);
            }
        });

        mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextView.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                filtersListFragment.prepareThumbnail(originalImage);
                mRelativeLayout.setOnClickListener(null);
                mTextView.setOnClickListener(null);
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImageToGallery();
                Intent intent=new Intent(Edit_Activity.this, shareActivity.class);
                intent.putExtra("contest",contest);
                intent.putExtra("path",savepath);
                intent.putExtra("desc",desc);
                intent.putExtra("pos",pos);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // adding filter list fragment
        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);

        // adding edit image fragment
        editImageFragment = new EditImageFragment();
        editImageFragment.setListener(this);

        adapter.addFragment(filtersListFragment, "Filters");
        adapter.addFragment(editImageFragment, "Edit");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFilterSelected(Filter filter) {
        resetControls();
        // applying the selected filter
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        // preview filtered image
        imagePreview.setImageBitmap(filter.processFilter(filteredImage));
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    @Override
    public void onBrightnessChanged(final int brightness) {
        brightnessFinal = brightness;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightness));
        imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onSaturationChanged(final float saturation) {
        saturationFinal = saturation;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new SaturationSubfilter(saturation));
        imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onContrastChanged(final float contrast) {
        contrastFinal = contrast;
        Filter myFilter = new Filter();
        myFilter.addSubFilter(new ContrastSubFilter(contrast));
        imagePreview.setImageBitmap(myFilter.processFilter(finalImage.copy(Bitmap.Config.ARGB_8888, true)));
    }

    @Override
    public void onEditStarted() {

    }

    @Override
    public void onEditCompleted() {
        // once the editing is done i.e seekbar is drag is completed,
        // apply the values on to filtered image
        final Bitmap bitmap = filteredImage.copy(Bitmap.Config.ARGB_8888, true);

        Filter myFilter = new Filter();
        myFilter.addSubFilter(new BrightnessSubFilter(brightnessFinal));
        myFilter.addSubFilter(new ContrastSubFilter(contrastFinal));
        myFilter.addSubFilter(new SaturationSubfilter(saturationFinal));
        finalImage = myFilter.processFilter(bitmap);
    }



    /**
     * Resets image edit controls to normal when new filter
     * is selected
     */
    private void resetControls() {
        if (editImageFragment != null) {
            editImageFragment.resetControls();
        }
        brightnessFinal = 0;
        saturationFinal = 1.0f;
        contrastFinal = 1.0f;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    private void saveImageToGallery() {
        savepath = BitmapUtils.insertImage(getContentResolver(), finalImage, "profile.jpg", null);
    }

    private void loadImage(String path) {
        Bitmap bitmap = BitmapUtils.getBitmapFromGallery(path, 800, 800);
        Glide.with(this).load(Uri.fromFile(new File(path))).into(imagePreview);
        try {
            Bitmap right=BitmapUtils.modifyOrientation(bitmap,path);
            originalImage = right.copy(Bitmap.Config.ARGB_8888, true);
            filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            imagePreview.setImageBitmap(finalImage);
            bitmap.recycle();
            right.recycle();
        } catch (IOException e) {
            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            imagePreview.setImageBitmap(finalImage);
            bitmap.recycle();
            e.printStackTrace();
        }
    }

}
