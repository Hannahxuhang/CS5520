package hangxu.finalproject.cs5520.hikerplus.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import hangxu.finalproject.cs5520.hikerplus.R;
import hangxu.finalproject.cs5520.hikerplus.ReviewRecordActivity;
import hangxu.finalproject.cs5520.hikerplus.model.Record;

/**
 * Adapter class for record list.
 */

public class RecordRecyclerAdapter extends RecyclerView.Adapter<RecordRecyclerAdapter.RecordViewHolder> {

    private static final String AT = "At ";

    private Context context;
    private List<Record> recordList;

    public RecordRecyclerAdapter(Context context, List<Record> recordList) {
        this.context = context;
        this.recordList = recordList;
    }

    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_row, parent, false);

        return new RecordViewHolder(view, context);

    }

    @Override
    public void onBindViewHolder(RecordViewHolder holder, int position) {

        Record record = recordList.get(position);

        holder.recordDate.setText(record.getDate());
        holder.destinationAddr.setText(AT + record.getDestinationAddress());
        holder.recordId = record.getRecordId();
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    // Create subclass of ViewHolder
    public class RecordViewHolder extends RecyclerView.ViewHolder {

        public TextView recordDate;
        public TextView destinationAddr;
        public String recordId;

        public RecordViewHolder(View view, Context ctx) {
            super(view);

            context = ctx;

            recordDate = (TextView) view.findViewById(R.id.recordDateList);
            destinationAddr = (TextView) view.findViewById(R.id.recordDestinationList);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = ReviewRecordActivity.newIntent(context, recordId);
                    context.startActivity(intent);
                }
            });
        }
    }
}
