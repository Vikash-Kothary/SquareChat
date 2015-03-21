package nsl.squarechat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Slidr.attach(this);


            // Launching my app

            boolean connected = PebbleKit.isWatchConnected(getApplicationContext());

            if(connected == true)
            {
                Log.i("wu", "Pebble is " + (connected ? "connected" : "not connected"));

            }
            else
            {
                Log.i("wu", "Pebble is not Connected!");
                Log.i("wu", "Device compatibility: " + PebbleKit.areAppMessagesSupported(getApplicationContext()));
            }


        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        toolbar.setTitle("Chatting with X");

        setSupportActionBar(toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.abc_action_mode_done, R.string.abc_action_mode_done) {
            @Override
            public void onDrawerClosed(View drawerView) {

                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(toggle);
        
        final SquareView currentSquare = (SquareView) findViewById(R.id.currentSquare);

        final LinearLayout holder = (LinearLayout) findViewById(R.id.holder);
        final FloatingActionButton send = (FloatingActionButton) findViewById(R.id.send);


        final ParseQuery<Message> innerQuery = new ParseQuery<Message>("Message");
        innerQuery.whereEqualTo("from", ParseUser.getCurrentUser().getObjectId());
        innerQuery.whereEqualTo("to", Core.ChattingTo.getObjectId());

        final ParseQuery<Message> innerQuery2 = new ParseQuery<Message>("Message");
        innerQuery2.whereEqualTo("to", Core.ChattingTo.getUsername());
        innerQuery2.whereEqualTo("from", ParseUser.getCurrentUser().getObjectId());

        ArrayList ls = new ArrayList();
        ls.add(innerQuery);
        ls.add(innerQuery2);
        ParseQuery<Message> query = ParseQuery.or(ls);


        //ParseQuery query = new ParseQuery("ClassA");
        //query.whereMatchesQuery("pointer-column-to-classB", innerQuery);
        query.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> parseUsers, com.parse.ParseException e) {
                for (Message m : parseUsers) {
                    ConversationSection section = new ConversationSection(MainActivity.this);
                    byte[] imageAsBytes = Base64.decode(m.getSquare().getBytes() , Base64.DEFAULT);

                    Bitmap b = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                    section.init(b,m.getFrom().getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) ?
                        section.ME : section.OTHER);
                    holder.addView(section);

                }

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(100);
                            ((ScrollView) holder.getParent()).fullScroll(ScrollView.FOCUS_DOWN);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };

                thread.start();
            }
        });

        SeekBar thickness = (SeekBar) findViewById(R.id.thickness);
        thickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSquare.Thickness = progress / 100.0f * 6f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap b = Bitmap.createBitmap(
                        currentSquare.getLayoutParams().width,
                        currentSquare.getLayoutParams().height,
                        Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                //v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                currentSquare.draw(c);
                ConversationSection section = new ConversationSection(MainActivity.this);
                section.init(b,ConversationSection.ME);

                holder.addView(section);
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(100);
                            ((ScrollView) holder.getParent()).fullScroll(ScrollView.FOCUS_DOWN);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };

                thread.start();
                    currentSquare.lines.clear();
                    currentSquare.invalidate();

                    Message newMessage = new Message();
                    newMessage.setFrom(ParseUser.getCurrentUser());
                    newMessage.setTo(Core.ChattingTo);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream .toByteArray();

                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    newMessage.setSquare(encoded);

                    newMessage.saveInBackground();
                }
            });

        LinearLayout colors = (LinearLayout) findViewById(R.id.colorHolder);
        for(int x =0; x< colors.getChildCount();x++){
            colors.getChildAt(x).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentSquare.color =((ColorDrawable) v.getBackground()).getColor();
                }
            });
        }
        onPostCreate(null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (toggle.onOptionsItemSelected(item))
            return true;
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(toggle!=null)toggle.syncState();
    }
}
