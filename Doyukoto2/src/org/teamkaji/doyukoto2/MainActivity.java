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
import android.widget.TextView;
import android.widget.Toast;

/**
 * メインアクティビティ
 * @author yotama
 *
 */
public class MainActivity extends Activity implements RecognitionListener {
    
    SpeechRecognizer speechRecognizer;
    boolean starting = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 開始ボタン
        Button btn = (Button) this.findViewById(R.id.button1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starting = true;
                setStatusText("認識中");
                voiceSearch();
            }
        });
        
        // 停止ボタン
        Button btn2 = (Button) this.findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVoiceSearch();
            }
        });
        
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 音声認識インテント発行
     */
    private void voiceSearch() {
        if (!starting) {
            return;
        }
        
        try {
            // インテント作成
            Intent intent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
            
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                            getPackageName());
            
            // インテント発行
            speechRecognizer.startListening(intent);
            Log.d("voice", "認識中");
            setStatusText("認識中");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this,
                "ActivityNotFoundException", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * テキストをサーバにポストする
     * 
     * @param account
     * @param text
     */
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
        
        protected void onPostExecute(HttpResponse response) {
            try {
                Log.d("================", EntityUtils.toString(response.getEntity()));
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * エラーコールバック
     */
    @Override
    public void onError(int error) {
        String errorMsg = null;
        switch (error) {
        case SpeechRecognizer.ERROR_AUDIO:
            // 音声データ保存失敗
            errorMsg = "error audio";
            break;
        case SpeechRecognizer.ERROR_CLIENT:
            // Android端末内のエラー(その他)
            errorMsg = "error client";
            break;
        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
            // 権限無し
            errorMsg = "no permissions";
            break;
        case SpeechRecognizer.ERROR_NETWORK:
            // ネットワークエラー(その他)
            errorMsg = "network error";
            break;
        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
            // ネットワークタイムアウトエラー
            Log.e("voice", "network timeout");
            errorMsg = "no permissions";
            break;
        case SpeechRecognizer.ERROR_NO_MATCH:
            // 音声認識結果無し
            errorMsg = "no match";
            break;
        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
            // RecognitionServiceへ要求出せず
            errorMsg = "recognizer busy";
            break;
        case SpeechRecognizer.ERROR_SERVER:
            // Server側からエラー通知
            errorMsg = "server error";
            break;
        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            // 音声入力無し
            errorMsg = "speech timeout";
            break;
        default:
        }
        
        Log.e("voice", errorMsg);
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        setStatusText(errorMsg);
        voiceSearch();
    }

    @Override
    public void onBeginningOfSpeech() {
        
    }

    @Override
    public void onBufferReceived(byte[] arg0) {
        
    }

    @Override
    public void onEndOfSpeech() {
        
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.d("voice", "準備できたよん");
    }

    /**
     * 認識後処理
     */
    @Override
    public void onResults(Bundle results) {
        setStatusText("送信中");
        ArrayList<String> recData = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d("voice", recData.get(0));
        Toast.makeText(this, recData.get(0), Toast.LENGTH_LONG).show();

        setTermText(recData.get(0));

        EditText accountText = (EditText) this.findViewById(R.id.editText1);
        postData(accountText.getText().toString(), recData.get(0));
        voiceSearch();
    }

    @Override
    public void onRmsChanged(float arg0) {
        
    }
    
    /**
     * ステータス表示変更
     * @param status
     */
    private void setStatusText(String status) {
        TextView statusText = (TextView) findViewById(R.id.textView_status);
        statusText.setText("ステータス：" + status);
    }
    
    /**
     * 認識文を表示する
     * @param text
     */
    private void setTermText(String text) {
        TextView termText = (TextView) this.findViewById(R.id.textView_term);
        termText.setText(text);
    }
    
    /**
     * 認識を停止する
     */
    private void stopVoiceSearch() {
        starting = false;
        setStatusText("停止中");
    }
}
