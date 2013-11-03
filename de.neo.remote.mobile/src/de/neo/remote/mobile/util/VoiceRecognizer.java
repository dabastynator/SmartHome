package de.neo.remote.mobile.util;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

/**
 * @author paul wein
 * 
 */
public class VoiceRecognizer implements RecognitionListener {

	private SpeechRecognizer mRecognizer;
	private Context mContext;
	private IVoiceRecognition mCallback;
	private ProgressDialog mDialog;

	public VoiceRecognizer(Context context, IVoiceRecognition callback) {
		mContext = context;
		mRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
		mRecognizer.setRecognitionListener(this);
		mCallback = callback;
	}

	public void startRecognizing() {
		mDialog = ProgressDialog.show(mContext, "Record",
				"Record remote commands");
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "123");
		intent.putExtra("calling_package", "VoiceIME");
		mRecognizer.startListening(intent);
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		mCallback.onBufferReceived(buffer);
	}

	@Override
	public void onEndOfSpeech() {
		Log.e("VoiceRegonizer", "end of speech");
		mCallback.onEndOfSpeech();
	}

	@Override
	public void onError(int error) {
		Log.e("VoiceRegonizer", "error occured");
		mDialog.cancel();
		mCallback.onError();
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		onPartialOrFinalResults(partialResults);
	}

	@Override
	public void onResults(Bundle results) {
		onPartialOrFinalResults(results);
	}

	private void onPartialOrFinalResults(Bundle resultsBundle) {
		Log.e("VoiceRegonizer", "partial or final result");
		mDialog.cancel();
		ArrayList<String> results = resultsBundle
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		if (results != null) {
			String result = results.get(0);
			Log.e("TAG", result);
			mCallback.onRecognized(result);
		}
	}

	public void destroy() {
		mRecognizer.destroy();
	}

	@Override
	public void onBeginningOfSpeech() {
		Log.e("VoiceRegonizer", "beginn of speech");
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		Log.e("VoiceRegonizer", "event: " + eventType);
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		Log.e("VoiceRegonizer", "ready for speech");
	}

	@Override
	public void onRmsChanged(float rmsdB) {
	}

	public interface IVoiceRecognition {
		public void onRecognized(String result);

		public void onEndOfSpeech();

		public void onError();

		public void onBufferReceived(byte[] buffer);
	}

}
