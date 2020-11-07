package safety.com.br.shake_sample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;

import org.w3c.dom.Document;

import safety.com.br.android_shake_detector.core.ShakeCallback;
import safety.com.br.android_shake_detector.core.ShakeDetector;
import safety.com.br.android_shake_detector.core.ShakeOptions;

/**
 * @author netodevel
 */
public class MainActivity extends AppCompatActivity {

    private ShakeDetector shakeDetector;
    boolean isMsgSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildView();

        ShakeOptions options = new ShakeOptions()
                .background(false)
                .interval(1000)
                .shakeCount(2)
                .sensibility(2.0f);

        this.shakeDetector = new ShakeDetector(options).start(this, new ShakeCallback() {
            @Override
            public void onShake() {

                TextView tvName = (TextView)findViewById(R.id.status);
                tvName.setText("I am shaken");

                if (!isMsgSent) {
                    SendMsgAsyncTask task = new SendMsgAsyncTask();
                    task.execute();
                    isMsgSent = true;
                }

                Log.d("event", "onShake");
            }
        });

        //IF YOU WANT JUST IN BACKGROUND
        //this.shakeDetector = new ShakeDetector(options).start(this);
    }

    /**
     * Async Task to create a new memo into the DynamoDB table
     */
    private class SendMsgAsyncTask extends AsyncTask<Document, Void, Void> {
        @Override
        protected Void doInBackground(Document... documents) {

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getBaseContext(),
                    "us-west-2:7e3517e9-e03c-4a79-aba8-58bd3505a47a", // Identity pool ID
                    Regions.US_WEST_2 // Region
            );

            AmazonSNSClient snsClient = new AmazonSNSClient(credentialsProvider);
            snsClient.setRegion(Region.getRegion(Regions.US_WEST_2));

            PublishRequest publishRequest = new PublishRequest
                    ("arn:aws:sns:us-west-2:111371901067:CustomerEmail",
                            "Dear Customer, \n\n We are extremely sorry to inform you that your product got damaged in transit. Not to worry we are shipping replacement for you with replacement order ID: 101-1231231-2323. \n\n This will arrive your destination by Friday.",
                            "Change in your Order Status");

            snsClient.publish(publishRequest);

            return null;
        }
    }

    private void buildViewShake() {
        Button btnStopService = (Button) findViewById(R.id.btnStopService);
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("destroy", "destroy service shake");
                shakeDetector.stopShakeDetector(getBaseContext());
            }
        });
    }

    private void buildView() {
        Button btnStopService = (Button) findViewById(R.id.btnStopService);
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("destroy", "destroy service shake");
                shakeDetector.stopShakeDetector(getBaseContext());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        shakeDetector.destroy(getBaseContext());
        super.onDestroy();
    }

}
