package org.teamkaji.doyukoto2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements RecognitionListener {
    private static final int VOICE_REQUEST_CODE = 123;
    
    SpeechRecognizer speechRecognizer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 保存ボタン
        Button btn = (Button) this.findViewById(R.id.button1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceSearch();
            }
        });
        
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

//        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
//        speechRecognizer.setRecognitionListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void voiceSearch() {
        try {
            // インテント作成
            Intent intent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "VoiceRecognitionTest");
            
            // インテント発行
            startActivityForResult(intent, VOICE_REQUEST_CODE);
//            speechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(getApplicationContext()));
            
        } catch (ActivityNotFoundException e) {
            // このインテントに応答できるアクティビティがインストールされていない場合
            Toast.makeText(this,
                "ActivityNotFoundException", Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 自分が投げたインテントであれば応答する
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
            
            // 結果文字列リスト
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            
            // トーストを使って結果を表示
            Toast.makeText(this, results.get(0), Toast.LENGTH_LONG).show();
            EditText accountText = (EditText) this.findViewById(R.id.editText1);
            accountText.getText();
            
            postData(accountText.getText().toString(), results.get(0));
            voiceSearch();
        } else if (requestCode == VOICE_REQUEST_CODE && resultCode == RecognizerIntent.RESULT_NO_MATCH) {
            Toast.makeText(this, resultCode, Toast.LENGTH_LONG).show();
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void postData(String account, String text) {
//        HttpPost request = new HttpPost("http://cd4tdik-aj2-app000.c4sa.net/register.php");
        HttpPost request = new HttpPost("http://210.129.194.74/pushTalk.php");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("account", account));
        params.add(new BasicNameValuePair("text", text));
        try {
            request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        // HTTPリクエスト発行
        new HttpPostTask().execute(request);
        
    }
    
    class HttpPostTask extends AsyncTask<HttpUriRequest, Void, HttpResponse> {
        // doInBackground() に、バックグラウンド処理の内容を記述する。
        // ここではAndroidHttpClientによるHTTP GET実行
        protected HttpResponse doInBackground(HttpUriRequest... request) {
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Demo AndroidHttpClient");
            HttpResponse response = null;
            try {
                response = httpClient.execute(request[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        
        // onPostExecute() に、バックグラウンド処理完了時の処理を記述する。
        // ここでは、HTTPレスポンスボディとして取得した文字列のTextViewへの貼り付け
        protected void onPostExecute(HttpResponse response) {
            try {
                Log.d("================", EntityUtils.toString(response.getEntity()));
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void onError(int error) {
        switch (error) {
        case SpeechRecognizer.ERROR_AUDIO:
            // 音声データ保存失敗
            break;
        case SpeechRecognizer.ERROR_CLIENT:
            // Android端末内のエラー(その他)
            break;
        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
            // 権限無し
            break;
        case SpeechRecognizer.ERROR_NETWORK:
            // ネットワークエラー(その他)
            Log.e("voice", "network error");
            break;
        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
            // ネットワークタイムアウトエラー
            Log.e("voice", "network timeout");
            break;
        case SpeechRecognizer.ERROR_NO_MATCH:
            // 音声認識結果無し
            Toast.makeText(this, "no match Text data", Toast.LENGTH_LONG).show();
            break;
        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
            // RecognitionServiceへ要求出せず
            break;
        case SpeechRecognizer.ERROR_SERVER:
            // Server側からエラー通知
            break;
        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            // 音声入力無し
            Toast.makeText(this, "no input?", Toast.LENGTH_LONG).show();
            break;
        default:
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBufferReceived(byte[] arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onEndOfSpeech() {
        
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Toast.makeText(this, "onReadyForSpeech", Toast.LENGTH_LONG).show();
        
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> recData = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Toast.makeText(this, "aaaaaaaaaaaaa" + recData.get(0), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRmsChanged(float arg0) {
        // TODO Auto-generated method stub
        
    }
}
