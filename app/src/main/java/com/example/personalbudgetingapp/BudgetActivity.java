package com.example.personalbudgetingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.personalbudgetingapp.model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class BudgetActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView totalBudgetAmountTextView;
    private RecyclerView recyclerView;

    private FloatingActionButton fab;

    private DatabaseReference budgetRef, personalRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    private String post_key = "";
    private String item = "";
    private int amount = 0;
    private String note = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Budget");

        fab = findViewById(R.id.addBudgetFAB);

        mAuth = FirebaseAuth.getInstance();
        budgetRef = FirebaseDatabase.getInstance("https://budgeting-app-7fa87-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("budget").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance("https://budgeting-app-7fa87-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);

        totalBudgetAmountTextView = findViewById(R.id.totalBudgetAmountTextView);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;

                for (DataSnapshot snap: snapshot.getChildren()) {
                    Data data = snap.getValue(Data.class);
                    totalAmount += data.getAmount();
                    String sTotal = String.valueOf("Month budget: RM" + totalAmount);
                    totalBudgetAmountTextView.setText(sTotal);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                additem();
            }
        });

        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    int totalamount = 0;

                    for(DataSnapshot snap : snapshot.getChildren()) {

                        Data data = snap.getValue(Data.class);

                        totalamount += data.getAmount();

                        String sttotal = String.valueOf("Monthly Budget: RM" + totalamount);

                        totalBudgetAmountTextView.setText(sttotal);

                    }
                    int weeklyBudget = totalamount/4;
                    int dailyBudget = totalamount/30;
                    personalRef.child("budget").setValue(totalamount);
                    personalRef.child("weeklyBudget").setValue(weeklyBudget);
                    personalRef.child("dailyBudget").setValue(dailyBudget);
                } else {
                    personalRef.child("budget").setValue(0);
                    personalRef.child("weeklyBudget").setValue(0);
                    personalRef.child("dailyBudget").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getMonthItemBudgetRatios("Transport", "Trans");
        getMonthItemBudgetRatios("Food", "Food");
        getMonthItemBudgetRatios("House", "House");
        getMonthItemBudgetRatios("Entertainment", "Ent");
        getMonthItemBudgetRatios("Education", "Edu");
        getMonthItemBudgetRatios("Charity", "Charity");
        getMonthItemBudgetRatios("Apparel", "Apparel");
        getMonthItemBudgetRatios("Health", "Health");
        getMonthItemBudgetRatios("Personal", "Personal");
        getMonthItemBudgetRatios("Other", "Other");
        /*getMonthTransportBudgetRatios();
        getMonthFoodBudgetRatios();
        getMonthHouseBudgetRatios();
        getMonthEntBudgetRatios();
        getMonthEduBudgetRatios();
        getMonthCharityBudgetRatios();
        getMonthApparelBudgetRatios();
        getMonthHealthBudgetRatios();
        getMonthPersonalBudgetRatios();
        getMonthOtherBudgetRatios();*/

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void additem() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_budget_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = myView.findViewById(R.id.itemsspinner);
        final EditText amount = myView.findViewById(R.id.amount);
        final EditText notes = myView.findViewById(R.id.note);
        final Button cancel = myView.findViewById(R.id.cancel);
        final Button save = myView.findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String budgetAmount = amount.getText().toString();
                String budgetItem = itemSpinner.getSelectedItem().toString();
                String budgetNote = notes.getText().toString();

                if(TextUtils.isEmpty(budgetAmount)) {
                    amount.setError("Amount is required!");
                    return;
                }

                if(budgetItem.equals("Select item")) {
                    Toast.makeText(BudgetActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                }

                else {
                    loader.setMessage("adding a budget item");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String id = budgetRef.push().getKey();
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Calendar cal = Calendar.getInstance();
                    String date = dateFormat.format(cal.getTime());

                    MutableDateTime epoch = new MutableDateTime();
                    epoch.setDate(0);
                    DateTime now = new DateTime();
                    Weeks weeks = Weeks.weeksBetween(epoch, now);
                    Months months = Months.monthsBetween(epoch, now);

                    String itemNday = budgetItem + date;
                    String itemNweek = budgetItem + weeks.getWeeks();
                    String itemNmonth = budgetItem + months.getMonths();

                    Data data = new Data(budgetItem, date, id, itemNday, itemNweek, itemNmonth, Integer.parseInt(budgetAmount), weeks.getWeeks(), months.getMonths(), budgetNote);
                    budgetRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(BudgetActivity.this, "Budget item added successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }

                            loader.dismiss();
                        }
                    });
                }
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(budgetRef, Data.class)
                .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, final int position, @NonNull final Data model) {

                holder.setItemAmount("RM" + model.getAmount());
                holder.setDate(model.getDate());
                holder.setItemName(model.getItem());
                holder.setNotes(model.getNotes());


                /*holder.notes.setVisibility(View.GONE);*/

                switch (model.getItem()) {
                    case "Transport":
                        holder.imageView.setImageResource(R.drawable.transport);
                        break;
                    case "Food":
                        holder.imageView.setImageResource(R.drawable.food);
                        break;
                    case "House":
                        holder.imageView.setImageResource(R.drawable.house);
                        break;
                    case "Entertainment":
                        holder.imageView.setImageResource(R.drawable.entertainment);
                        break;
                    case "Education":
                        holder.imageView.setImageResource(R.drawable.education);
                        break;
                    case "Charity":
                        holder.imageView.setImageResource(R.drawable.charity);
                        break;
                    case "Apparel":
                        holder.imageView.setImageResource(R.drawable.apparel);
                        break;
                    case "Health":
                        holder.imageView.setImageResource(R.drawable.health);
                        break;
                    case "Personal":
                        holder.imageView.setImageResource(R.drawable.personal);
                        break;
                    case "Other":
                        holder.imageView.setImageResource(R.drawable.others);
                        break;
                }

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(holder.getBindingAdapterPosition()).getKey();
                        item = model.getItem();
                        amount = model.getAmount();
                        note = model.getNotes();
                        updateData();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_budget_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public ImageView imageView;
        public TextView notes, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            notes = itemView.findViewById(R.id.note);
            date = itemView.findViewById(R.id.date);

        }

        public void setItemName(String itemName) {
            TextView item = mView.findViewById(R.id.item);
            item.setText(itemName);
        }

        public void setItemAmount(String itemAmount) {
            TextView amount = mView.findViewById(R.id.amount);
            amount.setText(itemAmount);
        }

        public void setDate(String itemDate) {
            TextView date = mView.findViewById(R.id.date);
            date.setText(itemDate);
        }

        public void setNotes(String itemNote) {
            TextView note = mView.findViewById(R.id.note);
            note.setText(itemNote);
        }
    }

    private void updateData() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.update_layout, null);

        myDialog.setView(mView);
        final AlertDialog dialog = myDialog.create();

        final TextView mItem = mView.findViewById(R.id.itemName);
        final EditText mAmount = mView.findViewById(R.id.amount);
        final EditText mNotes = mView.findViewById(R.id.note);

        /*mNotes.setVisibility(View.GONE);*/

        mItem.setText(item);
        mNotes.setText(note);
        mAmount.setText(String.valueOf(amount));
        mAmount.setSelection(String.valueOf(amount).length());

        Button delBut = mView.findViewById(R.id.btnDelete);
        Button btnUpdate = mView.findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = Integer.parseInt(mAmount.getText().toString());
                note = mNotes.getText().toString();

                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar cal = Calendar.getInstance();
                String date = dateFormat.format(cal.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Weeks weeks = Weeks.weeksBetween(epoch, now);
                Months months = Months.monthsBetween(epoch, now);

                String itemNday = item + date;
                String itemNweek = item + weeks.getWeeks();
                String itemNmonth = item + months.getMonths();

                Data data = new Data(item, date, post_key,itemNday, itemNweek, itemNmonth, amount, weeks.getWeeks(), months.getMonths(), note);
                budgetRef.child(post_key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(BudgetActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();

            }
        });

        delBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                budgetRef.child(post_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(BudgetActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void getMonthItemBudgetRatios(String budgetItem, String itemRatio) {
        Query query = budgetRef.orderByChild("item").equalTo(budgetItem);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayItemRatio = pTotal/30;
                    int weekItemRatio = pTotal/4;
                    int monthItemRatio = pTotal;

                    personalRef.child("day" + itemRatio + "Ratio").setValue(dayItemRatio);
                    personalRef.child("week" + itemRatio + "Ratio").setValue(weekItemRatio);
                    personalRef.child("month" + itemRatio + "Ratio").setValue(monthItemRatio);

                } else {
                    personalRef.child("day" + itemRatio + "Ratio").setValue(0);
                    personalRef.child("week" + itemRatio + "Ratio").setValue(0);
                    personalRef.child("month" + itemRatio + "Ratio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /*private void getMonthTransportBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Transport");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayTransRatio = pTotal/30;
                    int weekTransRatio = pTotal/4;
                    int monthTransRatio = pTotal;

                    personalRef.child("dayTransRatio").setValue(dayTransRatio);
                    personalRef.child("weekTransRatio").setValue(weekTransRatio);
                    personalRef.child("monthTransRatio").setValue(monthTransRatio);

                } else {
                    personalRef.child("dayTransRatio").setValue(0);
                    personalRef.child("weekTransRatio").setValue(0);
                    personalRef.child("monthTransRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthFoodBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Food");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayFoodRatio = pTotal/30;
                    int weekFoodRatio = pTotal/4;
                    int monthFoodRatio = pTotal;

                    personalRef.child("dayFoodRatio").setValue(dayFoodRatio);
                    personalRef.child("weekFoodRatio").setValue(weekFoodRatio);
                    personalRef.child("monthFoodRatio").setValue(monthFoodRatio);

                } else {
                    personalRef.child("dayFoodRatio").setValue(0);
                    personalRef.child("weekFoodRatio").setValue(0);
                    personalRef.child("monthFoodRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthHouseBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("House");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayHouseRatio = pTotal/30;
                    int weekHouseRatio = pTotal/4;
                    int monthHouseRatio = pTotal;

                    personalRef.child("dayHouseRatio").setValue(dayHouseRatio);
                    personalRef.child("weekHouseRatio").setValue(weekHouseRatio);
                    personalRef.child("monthHouseRatio").setValue(monthHouseRatio);

                } else {
                    personalRef.child("dayHouseRatio").setValue(0);
                    personalRef.child("weekHouseRatio").setValue(0);
                    personalRef.child("monthHouseRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthEntBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Entertainment");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayEntRatio = pTotal/30;
                    int weekEntRatio = pTotal/4;
                    int monthEntRatio = pTotal;

                    personalRef.child("dayEntRatio").setValue(dayEntRatio);
                    personalRef.child("weekEntRatio").setValue(weekEntRatio);
                    personalRef.child("monthEntRatio").setValue(monthEntRatio);

                } else {
                    personalRef.child("dayEntRatio").setValue(0);
                    personalRef.child("weekEntRatio").setValue(0);
                    personalRef.child("monthEntRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthEduBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Education");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayEduRatio = pTotal/30;
                    int weekEduRatio = pTotal/4;
                    int monthEduRatio = pTotal;

                    personalRef.child("dayEduRatio").setValue(dayEduRatio);
                    personalRef.child("weekEduRatio").setValue(weekEduRatio);
                    personalRef.child("monthEduRatio").setValue(monthEduRatio);

                } else {
                    personalRef.child("dayEduRatio").setValue(0);
                    personalRef.child("weekEduRatio").setValue(0);
                    personalRef.child("monthEduRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthCharityBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Charity");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayCharityRatio = pTotal/30;
                    int weekCharityRatio = pTotal/4;
                    int monthCharityRatio = pTotal;

                    personalRef.child("dayCharityRatio").setValue(dayCharityRatio);
                    personalRef.child("weekCharityRatio").setValue(weekCharityRatio);
                    personalRef.child("monthCharityRatio").setValue(monthCharityRatio);

                } else {
                    personalRef.child("dayCharityRatio").setValue(0);
                    personalRef.child("weekCharityRatio").setValue(0);
                    personalRef.child("monthCharityRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthApparelBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Apparel");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayApparelRatio = pTotal/30;
                    int weekApparelRatio = pTotal/4;
                    int monthApparelRatio = pTotal;

                    personalRef.child("dayApparelRatio").setValue(dayApparelRatio);
                    personalRef.child("weekApparelRatio").setValue(weekApparelRatio);
                    personalRef.child("monthApparelRatio").setValue(monthApparelRatio);

                } else {
                    personalRef.child("dayApparelRatio").setValue(0);
                    personalRef.child("weekApparelRatio").setValue(0);
                    personalRef.child("monthApparelRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthHealthBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Health");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayHealthRatio = pTotal/30;
                    int weekHealthRatio = pTotal/4;
                    int monthHealthRatio = pTotal;

                    personalRef.child("dayHealthRatio").setValue(dayHealthRatio);
                    personalRef.child("weekHealthRatio").setValue(weekHealthRatio);
                    personalRef.child("monthHealthRatio").setValue(monthHealthRatio);

                } else {
                    personalRef.child("dayHealthRatio").setValue(0);
                    personalRef.child("weekHealthRatio").setValue(0);
                    personalRef.child("monthHealthRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthPersonalBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Personal");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayPersonalRatio = pTotal/30;
                    int weekPersonalRatio = pTotal/4;
                    int monthPersonalRatio = pTotal;

                    personalRef.child("dayPersonalRatio").setValue(dayPersonalRatio);
                    personalRef.child("weekPersonalRatio").setValue(weekPersonalRatio);
                    personalRef.child("monthPersonalRatio").setValue(monthPersonalRatio);

                } else {
                    personalRef.child("dayPersonalRatio").setValue(0);
                    personalRef.child("weekPersonalRatio").setValue(0);
                    personalRef.child("monthPersonalRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthOtherBudgetRatios() {
        Query query = budgetRef.orderByChild("item").equalTo("Other");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayOtherRatio = pTotal/30;
                    int weekOtherRatio = pTotal/4;
                    int monthOtherRatio = pTotal;

                    personalRef.child("dayOtherRatio").setValue(dayOtherRatio);
                    personalRef.child("weekOtherRatio").setValue(weekOtherRatio);
                    personalRef.child("monthOtherRatio").setValue(monthOtherRatio);

                } else {
                    personalRef.child("dayOtherRatio").setValue(0);
                    personalRef.child("weekOtherRatio").setValue(0);
                    personalRef.child("monthOtherRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

}