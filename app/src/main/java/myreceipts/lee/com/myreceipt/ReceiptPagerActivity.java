package myreceipts.lee.com.myreceipt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.UUID;

public class ReceiptPagerActivity extends AppCompatActivity implements ReceiptFragment.Callbacks {

    private static final String EXTRA_RECEIPT_ID =
            "myreceipts.lee.com.myreceipt.receipt_id";

    private ViewPager mViewPager;
    private List<Receipt> mReceipts;

    public static Intent newIntent(Context packageContext, UUID receiptId) {
        Intent intent = new Intent(packageContext, ReceiptPagerActivity.class);
        intent.putExtra(EXTRA_RECEIPT_ID, receiptId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_pager);

        UUID receiptId = (UUID) getIntent().getSerializableExtra(EXTRA_RECEIPT_ID);

        mViewPager =  findViewById(R.id.activity_receipt_pager_view_pager);

        mReceipts = ReceiptLab.get(this).getReceipts();
        FragmentManager fragmentManager = getSupportFragmentManager();

        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Receipt receipt = mReceipts.get(position);
                return ReceiptFragment.newInstance(receipt.getId());
            }

            @Override
            public int getCount() {
                return mReceipts.size();
            }
        });

        for (int i = 0; i < mReceipts.size(); i++) {
            if (mReceipts.get(i).getId().equals(receiptId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }

    }

    @Override
    public void onReceiptUpdated(Receipt receipt) {

    }
}
