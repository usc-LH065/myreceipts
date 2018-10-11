package myreceipts.lee.com.myreceipt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ReceiptListFragment extends Fragment{
    private RecyclerView mReceiptRecyclerView;
    private ReceiptAdapter mAdapter;
    private RelativeLayout mEmptyView;
    private Button mEmptyViewAddButton;


    private Callbacks mCallbacks;

    public interface Callbacks {
        void onReceiptSelected(Receipt receipt);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receipt_list, container, false);

        mEmptyView = (RelativeLayout) view.findViewById(R.id.empty_view);
        mEmptyViewAddButton = (Button) mEmptyView.findViewById(R.id.empty_view_add_button);

        mEmptyViewAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addReceipt();
            }
        });

        mReceiptRecyclerView = (RecyclerView) view.findViewById(R.id.receipt_recycler_view);
        mReceiptRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));



        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.RIGHT) {
                    mAdapter.swipeToDelete(viewHolder.getAdapterPosition());
                    updateUI();
                }
            }
        });

        itemTouchHelper.attachToRecyclerView(mReceiptRecyclerView);

        return view;
    }

    public void updateUI() {
        ReceiptLab receiptLab = ReceiptLab.get(getActivity());
        List<Receipt> Receipts = receiptLab.getReceipts();

        if (mAdapter == null) {
            mAdapter = new ReceiptAdapter(Receipts);
            mReceiptRecyclerView.setAdapter(mAdapter);
        } else {
            // Otherwise update existing adapter data
            mAdapter.setReceipts(Receipts);
            mAdapter.notifyDataSetChanged();
        }
        if (mAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }

    }


    // Private class for viewholder
    private class ReceiptHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private Receipt mReceipt;

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private TextView mShopTextView;
        public ReceiptHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mTitleTextView =
                    itemView.findViewById(R.id.list_item_receipt_title_text_view);
            mDateTextView =
                    itemView.findViewById(R.id.list_item_receipt_date_text_view);
            mShopTextView =
                    itemView.findViewById(R.id.list_item_receipt_shop_text_view);


        }

        public void bindReceipt(Receipt receipt) {
            mReceipt = receipt;
            mTitleTextView.setText(mReceipt.getTitle());
            Date date = mReceipt.getDate();
            String formattedDate = DateFormatter.formatDateAsString(DateFormat.LONG, date);
            mDateTextView.setText(formattedDate);
            mShopTextView.setText(mReceipt.getShopName());
        }

        @Override
        public void onClick(View view) {
            mCallbacks.onReceiptSelected(mReceipt);
        }
    }


    private class ReceiptAdapter extends RecyclerView.Adapter<ReceiptHolder> {

        private List<Receipt> mReceipts;

        public ReceiptAdapter(List<Receipt> Receipts) {
            mReceipts = Receipts;
        }

        @Override
        public ReceiptHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            View view = layoutInflater.inflate(R.layout.list_item_receipt, parent, false);
            return new ReceiptHolder(view);
        }

        @Override
        public void onBindViewHolder(ReceiptHolder holder, int position) {
            Receipt Receipt = mReceipts.get(position);
            holder.bindReceipt(Receipt);
        }

        @Override
        public int getItemCount() {
            return mReceipts.size();
        }

        public void setReceipts(List<Receipt> Receipts) {
            mReceipts = Receipts;
        }

        public void swipeToDelete(int position) {
            ReceiptLab receiptLab = ReceiptLab.get(getActivity());
            Receipt receipt = mReceipts.get(position);
            receiptLab.deleteReceipt(receipt);
            mAdapter.notifyItemRemoved(position);
            mAdapter.notifyItemRangeChanged(position, receiptLab.getReceipts().size());
            Toast.makeText(getContext(), R.string.toast_delete_receipt, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_receipt_list, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_receipt:
                addReceipt();
                return true;
            case R.id.menu_item_help:
                gotoHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private void addReceipt() {
        Receipt receipt = new Receipt();
        ReceiptLab.get(getActivity()).addReceipt(receipt);
        updateUI();
        mCallbacks.onReceiptSelected(receipt);
    }
    private void gotoHelp() {
        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        startActivity(intent);
    }

}
