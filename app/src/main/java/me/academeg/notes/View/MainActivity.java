package me.academeg.notes.View;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import me.academeg.notes.Control.NotesAdapter;
import me.academeg.notes.Model.NotesDatabase;
import me.academeg.notes.Model.NotesDatabaseHelper;
import me.academeg.notes.R;


public class MainActivity extends ActionBarActivity {

    private static final String PATCH_PHOTOS = Environment.getExternalStorageDirectory().getPath() + "/.notes/";
    private NotesAdapter notesAdapter;
    private NotesDatabase notesDatabase;
    private ListView notesList;

    private static final int REQUEST_CODE_EDIT_NOTE = 1;
    private static final int REQUEST_CODE_CREATE_NOTE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesDatabase = new NotesDatabase(this);
        notesDatabase.open();
        notesList = (ListView) findViewById(R.id.notesListView);
        fillListNotes();
//        notesList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

//        notesList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
//            private boolean selectedItems[];
//            private int countSelectedItems;
//
//            @Override
//            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
//                selectedItems[position]=checked;
//                countSelectedItems += checked ? 1 : -1;
//                mode.setTitle(Integer.toString(countSelectedItems));
//            }
//
//            @Override
//            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                selectedItems=new boolean[noteArrayList.size()];
//                for(int i=0; i<selectedItems.length; i++) selectedItems[i]=false;
//                countSelectedItems=0;
//                mode.getMenuInflater().inflate(R.menu.context_main, menu);
//                return true;
//            }
//
//            @Override
//            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                if (item.getItemId() == R.id.deleteNotes) {
//                    SQLiteDatabase sdb = notesDatabase.getWritableDatabase();
//                    for(int index=selectedItems.length-1; index>=0; index--) {
//                        if (selectedItems[index]) {
//                            removePhotos(noteArrayList.get(index).getId(), sdb);
//                            deleteNote(noteArrayList.get(index).getId(), sdb);
//                            noteArrayList.remove(index);
////                            Log.d("Notes", "Note with index " + Integer.toString(index) + "was deleted.");
//                        }
//                    }
//                    if (countSelectedItems > 0)
//                        notesAdapter.notifyDataSetChanged();
//
//                    sdb.close();
//                    mode.finish();
//                }
//                return true;
//            }
//
//            @Override
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//
//            @Override
//            public void onDestroyActionMode(ActionMode mode) {
//
//            }
//        });


        notesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ViewNoteActivity.class);
                intent.putExtra("id",(int) id);
                startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE);
            }
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.createNoteButton);
        floatingActionButton.attachToListView(notesList);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewNoteActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CREATE_NOTE);
            }
        });
    }

    private void fillListNotes() {
        Cursor cursor = notesDatabase.getListNotes();
        startManagingCursor(cursor);
        notesAdapter = new NotesAdapter(this, R.layout.item_note_list, cursor, 0);
        notesList.setAdapter(notesAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if ((requestCode == REQUEST_CODE_EDIT_NOTE
                    || requestCode == REQUEST_CODE_CREATE_NOTE)
                    && data.getBooleanExtra("changes", true)) {
                fillListNotes();
                notesAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notesDatabase.close();
    }

    public void removePhotos(int noteID, SQLiteDatabase sdb) {
        ArrayList<String> photoName = new ArrayList<>();
        Cursor cursor = sdb.query(
                NotesDatabaseHelper.TABLE_PHOTO,
                null,
                "note" + NotesDatabaseHelper.UID + " = " + Integer.toString(noteID),
                null,
                null,
                null,
                null
        );

        int idPhotoName = cursor.getColumnIndex(NotesDatabaseHelper.PHOTO_NAME);
        while (cursor.moveToNext()) {
            photoName.add(cursor.getString(idPhotoName));
        }
        cursor.close();

        for (String name : photoName) {
            File deletePhotoFile = new File(PATCH_PHOTOS + name);
            deletePhotoFile.delete();
        }
    }

    public void deleteNote(int noteID, SQLiteDatabase sdb) {
        // delete note
        sdb.delete(
                NotesDatabaseHelper.TABLE_PHOTO,
                "note" + NotesDatabaseHelper.UID + " = ?",
                new String[]{ Integer.toString(noteID) }
        );

        // delete photos
        sdb.delete(
                NotesDatabaseHelper.TABLE_NOTE,
                NotesDatabaseHelper.UID + " = "
                        + Integer.toString(noteID),
                null
        );
    }

}