package in.bucksbuddy.bucksbuddy;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by shrukul on 20/1/16.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {

    public static class PersonViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView personName;
        TextView personAge;
        ImageView personPhoto;
        TextView amount;
        ImageView type;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            personName = (TextView) itemView.findViewById(R.id.recent_item_name);
            personAge = (TextView) itemView.findViewById(R.id.recent_item_number);
            personPhoto = (ImageView) itemView.findViewById(R.id.recent_item_pic);
            type = (ImageView) itemView.findViewById(R.id.type_trans);
            amount = (TextView) itemView.findViewById(R.id.amt);
        }
    }

    List<Person> persons;

    RVAdapter(List<Person> persons) {
        this.persons = persons;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recentslist_item_layout, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        personViewHolder.personName.setText(persons.get(i).name);
        personViewHolder.personAge.setText(persons.get(i).phone);
        personViewHolder.amount.setText("₹ " + persons.get(i).amount);
//        personViewHolder.personPhoto.setImageResource(persons.get(i).photoId);
        personViewHolder.personPhoto.setImageResource(R.drawable.profile);
        if (persons.get(i).type == 0)
            personViewHolder.type.setImageResource(R.drawable.ic_log_outgoing);
        else
            personViewHolder.type.setImageResource(R.drawable.ic_log_incoming);

    }

    @Override
    public int getItemCount() {
        return persons.size();
    }
}
