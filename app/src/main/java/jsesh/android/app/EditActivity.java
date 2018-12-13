package jsesh.android.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import jsesh.android.AndroidUtils;
import jsesh.editor.JMDCEditor;
import jsesh.editor.JMDCEditorWorkflow;
import jsesh.graphics.export.ExportData;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;
import jsesh.resources.ResourcesManager;


public class EditActivity extends AppCompatActivity {

    private boolean inEditMode = true;

    MDCDocument mdcDocument;

    public static final int READ_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ResourcesManager.setContext(this.getApplicationContext());

        setContentView(R.layout.activity_edit);

        Toolbar toolbar = findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }

        toolbar.setVisibility(View.GONE);

        findViewById(R.id.main_jmdceditor).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) setEditMode(true);
            }
        });

        mdcDocument = new MDCDocument();

        //Handle intent

        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                try {

                    InputStream is = getContentResolver().openInputStream(uri);
                    MDCDocumentReader reader = new MDCDocumentReader();
                    mdcDocument = reader.readStream(is, new File(".gly"));
                    mdcDocument.setFile(null);

                    JMDCEditor editor = findViewById(R.id.main_jmdceditor);
                    editor.setHieroglyphiTextModel(mdcDocument.getHieroglyphicTextModel());
                    editor.getDrawingSpecifications().applyDocumentPreferences(mdcDocument.getDocumentPreferences());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MDCSyntaxError mdcSyntaxError) {
                    mdcSyntaxError.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (inEditMode) setEditMode(false);
        else super.onBackPressed();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event != null){
            int keyCode = event.getKeyCode();
            if(keyCode == KeyEvent.KEYCODE_BACK){
                if (inEditMode) setEditMode(false);
                return false;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setEditMode(boolean editMode) {
        if (!editMode && inEditMode) {
            inEditMode = false;
            Toolbar toolbar = findViewById(R.id.edit_toolbar);
            toolbar.setVisibility(View.VISIBLE);
            AndroidUtils.hideKeyboard(this);

            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                View editor = findViewById(R.id.main_jmdceditor);
                FrameLayout.LayoutParams layoutParams = ((FrameLayout.LayoutParams) editor.getLayoutParams());
                layoutParams.topMargin = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
                editor.setLayoutParams(layoutParams);
            }
            toolbar.requestFocus();
        }
        else if (editMode && !inEditMode) {
            inEditMode = true;
            Toolbar toolbar = findViewById(R.id.edit_toolbar);
            toolbar.setVisibility(View.GONE);
            AndroidUtils.hideKeyboard(this);

            View editor = findViewById(R.id.main_jmdceditor);
            FrameLayout.LayoutParams layoutParams = ((FrameLayout.LayoutParams) editor.getLayoutParams());
            layoutParams.topMargin = 0;
            editor.setLayoutParams(layoutParams);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        JMDCEditor editor = findViewById(R.id.main_jmdceditor);
        JMDCEditorWorkflow workflow = editor.getWorkflow();
        switch (item.getItemId()) {
            //Edit
            case R.id.undo:
                workflow.undo();
                return true;
            case R.id.redo:
                workflow.redo();
                return true;
            case R.id.cut:
                //TODO
                return true;
            case R.id.copy:
                //TODO
                return true;
            case R.id.paste:
                //TODO
                return true;
            case R.id.duplicate:
                //TODO
                return true;
            case R.id.delete:
                workflow.doBackspace();
                return true;
            case R.id.select_all:
                workflow.selectAll();
                return true;
            case R.id.deselect_all:
                workflow.clearMark();
                return true;

            //Group manipulation
            case R.id.group_horizontally:
                workflow.groupHorizontally();
                return true;
            case R.id.group_vertically:
                workflow.groupVertically();
                return true;
            case R.id.ligature_elements:
                workflow.ligatureElements();
                return true;
            case R.id.ligature_group_with_hieroglyph:
                workflow.ligatureGroupWithHieroglyph();
                return true;
            case R.id.ligature_hieroglyph_with_group:
                workflow.ligatureHieroglyphWithGroup();
                return true;
            case R.id.explode_group:
                workflow.explodeGroup();
                return true;
            case R.id.edit_group:
                //TODO
                return true;
            case R.id.insert_space:
                workflow.insertSpace();
                return true;
            case R.id.insert_half_space:
                workflow.insertHalfSpace();
                return true;
            case R.id.new_page:
                workflow.insertPageBreak();
                return true;
            case R.id.insert_red_point:
                //TODO
                return true;
            case R.id.insert_black_point:
                //TODO
                return true;
            case R.id.insert_full_size_shading:
                //TODO
                return true;
            case R.id.insert_horizontal_shading:
                //TODO
                return true;
            case R.id.insert_vertical_shading:
                //TODO
                return true;
            case R.id.insert_quarter_shading:
                //TODO
                return true;
            case R.id.shade_zone:
                workflow.shadeZone();
                return true;
            case R.id.unshade_zone:
                workflow.unshadeZone();
                return true;
            case R.id.paint_zone_in_red:
                workflow.paintZoneInRed();
                return true;
            case R.id.paint_zone_in_black:
                workflow.paintZoneInBlack();
                return true;
            case R.id.shading:
                //TODO
                return true;
            case R.id.cartouches:
                //TODO
                return true;
            case R.id.philological_markup:
                //TODO
                return true;

            //File
            case R.id.new_file:
                //TODO
                return true;
            case R.id.open:
                StaticTransfer.obj = this;
                startActivity(new Intent(this, OpenActivity.class));
                return true;
            case R.id.open_recent:
                //TODO
                return true;
            case R.id.close:
                //TODO
                return true;
            case R.id.save:
                if (mdcDocument.getFile() != null) {
                    MDCDocument newDocument = new MDCDocument(editor.getHieroglyphicTextModel().getModel(), editor.getDrawingSpecifications());
                    newDocument.setFile(mdcDocument.getFile());
                    newDocument.setDialect(mdcDocument.getDialect());
                    newDocument.setEncoding(mdcDocument.getEncoding());
                    mdcDocument = newDocument;
                    try {
                        mdcDocument.save();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            case R.id.save_as:
                mdcDocument = new MDCDocument(editor.getHieroglyphicTextModel().getModel(), editor.getDrawingSpecifications());
                StaticTransfer.obj = mdcDocument;
                new DocumentSaverDialogFragment().show(getSupportFragmentManager(), "save");
                return true;
            case R.id.import_file:
                //TODO
                return true;
            case R.id.export:
                //TODO
                return true;
            case R.id.set_as_model:
                //TODO
                return true;
            case R.id.use_model_preferences:
                //TODO
                return true;
            case R.id.document_properties:
                //TODO
                return true;
            case R.id.format:
                //TODO
                return true;
            case R.id.text_in_lines:
                item.setChecked(true);
                editor.setTextOrientation(TextOrientation.HORIZONTAL);
                return true;
            case R.id.text_in_columns:
                item.setChecked(true);
                editor.setTextOrientation(TextOrientation.VERTICAL);
                return true;
            case R.id.left_to_right_text:
                item.setChecked(true);
                editor.setMDCTextDirection(TextDirection.LEFT_TO_RIGHT);
                return true;
            case R.id.right_to_left_text:
                item.setChecked(true);
                editor.setMDCTextDirection(TextDirection.RIGHT_TO_LEFT);
                return true;
            case R.id.center_small_signs:
                item.setChecked(!item.isChecked());
                editor.setSmallSignsCentered(item.isChecked());
                return true;
            case R.id.justify_text:
                item.setChecked(!item.isChecked());
                editor.getDrawingSpecifications().setJustified(item.isChecked());
                return true;

            //Export
            case R.id.export_bitmap:
                StaticTransfer.obj = new ExportData(editor.getDrawingSpecifications(), editor.getWorkflow().getCaret(), editor.getHieroglyphicTextModel().getModel(), 1);
                Intent intent = new Intent(this, BitmapExporterActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                MDCDocumentReader reader = new MDCDocumentReader();
                try {
                    mdcDocument = reader.loadFile(new File(uri.getPath()));
                    InputStream is = getContentResolver().openInputStream(uri);

                    JMDCEditor editor = findViewById(R.id.main_jmdceditor);
                    editor.setHieroglyphiTextModel(mdcDocument.getHieroglyphicTextModel());
                    editor.getDrawingSpecifications().applyDocumentPreferences(mdcDocument.getDocumentPreferences());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (MDCSyntaxError mdcSyntaxError) {
                    mdcSyntaxError.printStackTrace();
                }
            }

        }

    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

}
