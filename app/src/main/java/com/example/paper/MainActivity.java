package com.example.paper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private EditText nameText, sizeText, priceText;
    private Button addButton, updateButton, deleteButton, selectImageButton;
    private ImageView imageView;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private String selectedClothngItem;
    private String imagePath = null;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Paper.init(this);

        nameText = findViewById(R.id.titleText);
        sizeText = findViewById(R.id.AuthorText);
        priceText = findViewById(R.id.PriceText);
        addButton = findViewById(R.id.addButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        selectImageButton = findViewById(R.id.selectImageButton);
        imageView = findViewById(R.id.imageView);
        listView = findViewById(R.id.listView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getClothngItemNames());
        listView.setAdapter(adapter);

        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedClothngItem = adapter.getItem(i);
            ClothngItem item = Paper.book().read(selectedClothngItem, null);

            if (item != null) {
                nameText.setText(item.getName());
                sizeText.setText(item.getSize());
                priceText.setText(String.valueOf(item.getPrice()));
                imagePath = item.getImagePath();
                imageView.setImageURI(Uri.parse(imagePath));
            }
        });

        addButton.setOnClickListener(view -> {
            String name = nameText.getText().toString();
            String size = sizeText.getText().toString();
            String priceString = priceText.getText().toString();

            if (!name.isEmpty() && !size.isEmpty() && !priceString.isEmpty() && imagePath != null) {
                double price = Double.parseDouble(priceString);
                ClothngItem item = new ClothngItem(name, name, size, price, imagePath);
                Paper.book().write(name, item);
                updateClothingList();
                clearInputs();
            } else {
                Toast.makeText(MainActivity.this, "Заполните все поля и выберите изображение!", Toast.LENGTH_SHORT).show();
            }
        });

        updateButton.setOnClickListener(view -> {
            if (selectedClothngItem == null) {
                Toast.makeText(MainActivity.this, "Выберите товар!", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = nameText.getText().toString();
            String size = sizeText.getText().toString();
            String priceString = priceText.getText().toString();

            if (!name.isEmpty() && !size.isEmpty() && !priceString.isEmpty() && imagePath != null) {
                double price = Double.parseDouble(priceString);
                ClothngItem item = new ClothngItem(selectedClothngItem, name, size, price, imagePath);
                Paper.book().write(selectedClothngItem, item);
                updateClothingList();
                clearInputs();
            } else {
                Toast.makeText(MainActivity.this, "Заполните все поля и выберите изображение!", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(view -> {
            if (selectedClothngItem == null) {
                Toast.makeText(MainActivity.this, "Выберите товар!", Toast.LENGTH_SHORT).show();
                return;
            }

            Paper.book().delete(selectedClothngItem);
            updateClothingList();
            clearInputs();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            imagePath = selectedImageUri.toString(); 
            imageView.setImageURI(selectedImageUri); 
        }
    }

    private void clearInputs() {
        nameText.setText("");
        sizeText.setText("");
        priceText.setText("");
        imageView.setImageResource(0); 
        imagePath = null;
        selectedClothngItem = null;
    }

    private void updateClothingList() {
        adapter.clear();
        adapter.addAll(getClothngItemNames());
        adapter.notifyDataSetChanged();
    }

    private List<String> getClothngItemNames() {
        return new ArrayList<>(Paper.book().getAllKeys());
    }
}
