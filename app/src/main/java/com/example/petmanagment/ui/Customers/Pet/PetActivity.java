package com.example.petmanagment.ui.Customers.Pet;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petmanagment.R;
import com.example.petmanagment.ui.Customers.Customer;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.UUID;

public class PetActivity extends AppCompatActivity {
    //Customer customer;
    FirebaseUser user;
    FirebaseFirestore db;
    String id;
    FirebaseStorage storage;
    StorageReference storageRef;
    RecyclerView recyclerView;
    ArrayList<String> pets = new ArrayList<>();
    ListAdapterPet listAdapterPet;
    private EditText name;
    private EditText race;
    private EditText typology;
    private AlertDialog dialog;
    final ImageButton add_pet = findViewById(R.id.addpet);


    //TODO bisogna impostare customer al cliente corrente
    Customer customer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet);
        TextView customerName = (TextView) findViewById(R.id.customer_name);
        ImageView icon = (ImageView) findViewById(R.id.imageView3);

        recyclerView = findViewById(R.id.recyclerView);

        add_pet.setOnClickListener(view -> elaboratePet("add",-1));


        Bundle extras = getIntent().getExtras();
        String info = extras.getString("NameLastName");
        customerName.setText(info);

        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();
        assert user != null;
        db.collection(user.getEmail()).document(info).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                id = documentSnapshot.getString("uuid");
                assert id != null;
                storageRef.child(id).getDownloadUrl().addOnSuccessListener(uri -> {
                    ImageView icon1 = (ImageView) findViewById(R.id.imageView3);
                    ImageView smallCamera = (ImageView) findViewById(R.id.imcamera);
                    smallCamera.setVisibility(View.INVISIBLE);
                    Glide
                            .with(getApplicationContext())
                            .load(uri)
                            .into(icon1);
                }).addOnFailureListener(e -> System.out.println("no image"));
            }
        });

        icon.setOnClickListener(view -> {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, 3);
            ImageView camera_icon = findViewById(R.id.imcamera);
            camera_icon.setVisibility(View.INVISIBLE);
        });

        ListAdapterPet listAdapter = new ListAdapterPet(pets);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);
    }

    String deletedPet = null;
    String modifiedPet = null;


    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            deletedPet = pets.get(position);

            pets.remove(position);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            //la snackbar serve per tornare indietro in caso di errore nella cancellazione
            Snackbar snackbar = Snackbar.make(recyclerView, "Customer " + deletedPet + " deleted", Snackbar.LENGTH_LONG).addCallback(new Snackbar.Callback());
            snackbar.addCallback(new Snackbar.Callback() {

                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                deletePets(deletedPet);
                            }
                        }
                    }).setAction("Undo", view -> {
                        pets.add(position, deletedPet);
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));

                    }).setActionTextColor(getResources().getColor(R.color.orange))
                    .setTextColor(getResources().getColor(R.color.black))
                    .setBackgroundTint(getResources().getColor(R.color.white))
                    .show();
        } else {
            modifiedPet = pets.get(position);
            elaboratePet("modify", position);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            ImageView icon = (ImageView) findViewById(R.id.imageView3);
            icon.setImageURI(selectedImage);
            StorageReference imageRef = storageRef.child(id);
            UploadTask imageLoader = (UploadTask) imageRef.putFile(selectedImage).addOnSuccessListener(taskSnapshot -> Toast.makeText(PetActivity.this, "Upload ok", Toast.LENGTH_LONG))
                    .addOnFailureListener(e -> Toast.makeText(PetActivity.this, "No upload", Toast.LENGTH_LONG));
        }
    }

    public void getPets(ArrayList<String> c) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        db.collection(user.getEmail())
                .document(customer.getName() + '\t' + customer.getLastName())
                .collection(customer.getName() + customer.getLastName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (!c.contains(document.getId()))
                            c.add(document.getId());
                        listAdapterPet.notifyDataSetChanged();
                    }
                });
    }

    public void addNewPet(Pet pet) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        ArrayList<Pet> current_pet = new ArrayList<>();
        current_pet.add(pet);
        db.collection(user.getEmail())
                .document(customer.getName() + '\t' + customer.getLastName())
                .collection(customer.getName() + customer.getLastName())
                .document(pet.getName())
                .set(pet, SetOptions.merge()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Pet added successfully", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void deletePets(String name) {
        db.collection(user.getEmail())
                .document(customer.getName() + '\t' + customer.getLastName())
                .collection(customer.getName() + customer.getLastName())
                .document(name)
                .delete();
    }


    public void elaboratePet(String operation, int eventualPosition) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View popup = getLayoutInflater().inflate(R.layout.popwindow, null);
        name = (EditText) popup.findViewById(R.id.firstname);
        race = (EditText) popup.findViewById(R.id.lastname);
        typology = (EditText) popup.findViewById(R.id.phone);

        Button confirm = (Button) popup.findViewById(R.id.cbutton);

        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();
        dialog.show();

        confirm.setOnClickListener(view -> {
            switch (operation) {
                case "add":
                    Pet c = new Pet(name.getText().toString(), race.getText().toString(), typology.getText().toString(), UUID.randomUUID().toString());
                    addNewPet(c);
                    // getCustomers(customers);
                    break;
                case "modify":
                    updatePets(pets.get(eventualPosition), name.getText().toString(), race.getText().toString(), typology.getText().toString());
                    if (!name.getText().toString().isEmpty() || !race.getText().toString().isEmpty())
                        pets.set(eventualPosition, String.format("%s\t%s", name.getText(), race.getText()));
                    break;
            }
            getPets(pets);
            dialog.dismiss();

        });
    }

    public void updatePets(String petname, String name, String race, String typology) {
        if (!name.isEmpty())
            db.collection(user.getEmail())
                    .document(customer.getName() + '\t' + customer.getLastName())
                    .collection(customer.getName() + customer.getLastName())
                    .document(name)
                    .update("name", name);
        if (!race.isEmpty())
            db.collection(user.getEmail())
                    .document(customer.getName() + '\t' + customer.getLastName())
                    .collection(customer.getName() + customer.getLastName())
                    .document(name)
                    .update("race", race);
        if (!typology.isEmpty())
            db.collection(user.getEmail())
                    .document(customer.getName() + '\t' + customer.getLastName())
                    .collection(customer.getName() + customer.getLastName())
                    .document(name)
                    .update("typology", typology);
        db.collection(user.getEmail())
                .document(customer.getName() + '\t' + customer.getLastName())
                .collection(customer.getName() + customer.getLastName())
                .document(name).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String fileName;
                        if (name.isEmpty() && race.isEmpty())
                            fileName = petname;
                        else
                            fileName = name + race + typology;
                        db.collection(user.getEmail()).document(fileName).set(documentSnapshot.getData(), SetOptions.merge()).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(PetActivity.this, "Customer edited successfully", Toast.LENGTH_LONG).show();
                                if (!fileName.equals(petname))
                                    db.collection(user.getEmail())
                                            .document(customer.getName() + '\t' + customer.getLastName())
                                            .collection(customer.getName() + customer.getLastName())
                                            .document(name).delete();
                            }
                        });
                    }
                });
    }
}