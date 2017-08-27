package com.example.krishna.onmapfriend;

import com.example.krishna.onmapfriend.OnMap;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.R.attr.path;
import static android.R.attr.start;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {

    Button writeContact,getmap;
    long queueid;
    DownloadManager dm;
    String path  = "/storage/emulated/0/documents/contacts.txt";
    ArrayList data = new ArrayList();
    ArrayList names = new ArrayList();

    public static final String origin="com.example.krishna.onmapfriend.MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        writeContact = (Button) findViewById(R.id.button3);
      //  writeContact.setEnabled(false);
        getmap = (Button)findViewById(R.id.button4);
        getmap.setEnabled(false);
        writeContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readData(path);
                setContacts();
                getmap.setEnabled(true);
            }
        });
        getmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                names.add(((ArrayList) data.get(0)).get(0).toString());
                names.add(((ArrayList) data.get(1)).get(0).toString());
                names.add(((ArrayList) data.get(2)).get(0).toString());
                names.add(((ArrayList) data.get(3)).get(0).toString());
                names.add(((ArrayList) data.get(4)).get(0).toString());

                Toast.makeText(MainActivity.this, "i done adding to maps array", Toast.LENGTH_SHORT).show();
                startMap();
            }
        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)){
                    DownloadManager.Query req_query = new DownloadManager.Query();
                    req_query.setFilterById(queueid);


                    Cursor c = dm.query(req_query);

                    if(c.moveToFirst()){

                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);

                        if (DownloadManager.STATUS_SUCCESSFUL==c.getInt(columnIndex)){

                            Toast.makeText(context, "Download Successfull!", Toast.LENGTH_SHORT).show();
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        }
                    }
                }

            }
        };

        registerReceiver(receiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    public void startMap(){
        Intent intent = new Intent(this,OnMap.class);
        intent.putExtra(origin,names);
        startActivity(intent);
        Toast.makeText(this, "i done start map activity", Toast.LENGTH_SHORT).show();

    }


    public void readData( String address){
        int temp = 0, count = 0;
        String tempString = "";
        File file = new File(address);
        if (!file.exists()){
            Toast.makeText(this, "File not Exist!", Toast.LENGTH_SHORT).show();
          //  statusUpdate.setText("File does not exists yet, try Again");
            return;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            temp = fileInputStream.read();
            while(temp!= -1){
                data.add(new ArrayList());
                tempString = "";
                while(temp!= (int)' '){
                    tempString+=(char)temp;
                    temp = fileInputStream.read();
                }
                ((ArrayList) data.get(count)).add(tempString);
                tempString = "";
                temp = fileInputStream.read();
                while(temp!= (int)' '){
                    tempString+=(char)temp;
                    temp = fileInputStream.read();
                }
                ((ArrayList) data.get(count)).add(tempString);
                tempString = "";
                temp = fileInputStream.read();
                while(temp!= (int)' '){
                    tempString+=(char)temp;
                    temp = fileInputStream.read();
                }
                ((ArrayList) data.get(count)).add(tempString);
                tempString = "";
                temp = fileInputStream.read();
                while(temp!= (int)'\n'){
                    tempString+=(char)temp;
                    temp = fileInputStream.read();

                }
             ((ArrayList) data.get(count)).add(tempString);
             if(temp == -1) break;
             temp = fileInputStream.read();
                count++;
                if(count>=5)break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setContacts(){
        //Initialize the data that are relevant to contact:
       Toast.makeText(this, "Writing to Contacts STARTED!!", Toast.LENGTH_SHORT).show();
       // statusUpdate.setText("Writing Contacts to your Contacts List:");
       // statusUpdate.append("\nPease note that if the contacts already exists, they may or may " +
       //         "or may not be added again depending upon the behviour of ContactsContract.RawContact" +
       //         "table. The Best suggestion isto delete the earlier contacts");
        int tSize = data.size(), i = 0;
        String name = null,numberHome = null,numberMobile = null,email = null;
        for(i=0;i<tSize;i++) {
            name = ((ArrayList) data.get(i)).get(0).toString();
            email = ((ArrayList) data.get(i)).get(1).toString();
            numberMobile = ((ArrayList) data.get(i)).get(2).toString();
            numberHome = ((ArrayList) data.get(i)).get(3).toString();

//            Toast.makeText(this, "I am here", Toast.LENGTH_SHORT).show();

            try {
                ArrayList<ContentProviderOperation> ops =
                        new ArrayList<ContentProviderOperation>();
                int rawContactInsertIndex = ops.size();
                ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build());

                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build());

                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, numberMobile)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());

                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, numberHome)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                        .build());

                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                        .build());


                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(this, "Contact " +name+ " Added successfully", Toast.LENGTH_SHORT).show();
           //     statusUpdate.append("\nAdded " + name+ "'s Data" );
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
    }




    public void Download_click(View v){

        dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://www.cs.columbia.edu/~coms6998-8/assignments/homework2/contacts/contacts.txt"));
        // request.setDestinationInExternalPublicDir("/storage/emulated/0/documents/","contacts.txt");
       // request.setDestinationInExternalPublicDir("/sdcard/documents","contacts.txt");
        queueid = dm.enqueue(request);


    }

    public void View_Click(View v){
        Intent i = new Intent();
        i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        startActivity(i);

    }

}
