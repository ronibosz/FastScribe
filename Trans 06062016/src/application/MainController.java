package application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController implements Initializable
{
	@FXML Button playButton;
	@FXML Button stopButton;
	@FXML Button rewButton;
	@FXML Button forwButton;
	@FXML Slider slider;
	@FXML Slider rateSlider;
	@FXML Slider volumeSlider;
	@FXML Label timeLbl;
	@FXML Label durationLbl;
	@FXML Label fileName;
	@FXML TextArea text;
	
	private Media media;
	private MediaPlayer mp;
	private File mediaFile;
	private Duration mediaDuration;
	private boolean firstOpen;
	private KeyCombination rewKeyComb;
	private KeyCombination forwKeyComb;
	private KeyCombination playKeyComb;
	private KeyCombination stopKeyComb;
	Stage controllerStage;

	
	@Override
	public void initialize(URL location, ResourceBundle resources) 
	{
		timeLbl.setText("hh:mm:ss");
		durationLbl.setText("hh:mm:ss");
		slider.setDisable(true);
		volumeSlider.setValue(0.8);
		rateSlider.setValue(1);
		firstOpen = false;
		
	}
	
	public void setupListeners(Stage stage)
	{
		controllerStage = stage;
		rewKeyComb = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
		forwKeyComb = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
		playKeyComb = new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN);
		stopKeyComb = new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);
		
		controllerStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent ke) 
			{
				if (rewKeyComb.match(ke))
					rewind(null);
				
				if (forwKeyComb.match(ke))
					forward(null);
				
				if (playKeyComb.match(ke))
					playpause(null);
				
				if (stopKeyComb.match(ke))
					stop(null);
			}
		});
	}

	
	public void playpause (ActionEvent event)
	{
		if (media != null)
			if (mp.getStatus() != Status.PLAYING)
			{
				mp.play();
				System.out.println(mp.getStatus());
			} 
			
			else
			{
				mp.pause();
			}
	}
	
	public void stop (ActionEvent event)
	{
		if (media != null)
			mp.stop();
	}
	
	public void forward (ActionEvent event)
	{
		if (media != null)
			mp.seek(mp.getCurrentTime().add(Duration.seconds(3)));
	}
	
	public void rewind (ActionEvent event)
	{
		if (media != null)
			mp.seek(mp.getCurrentTime().add(Duration.seconds(-3)));
	}
	
	public void volumeSliderInit()
	{
		mp.setVolume(volumeSlider.getValue());
		volumeSlider.valueProperty().addListener(new InvalidationListener() 
		{			
			@Override
			public void invalidated(Observable observable) 
			{
				mp.setVolume(volumeSlider.getValue());
			}
		});
	}
	
	public void rateSliderInit()
	{
			rateSlider.valueProperty().addListener(new InvalidationListener() 
			{			
				@Override
				public void invalidated(Observable observable) 
				{
					mp.setRate(rateSlider.getValue());	
				}
			});
	}
	
	public void progressSliderInit()
	{
		slider.setDisable(false);
		
		/*on slider move*/
		slider.valueChangingProperty().addListener(new InvalidationListener() 
		{
			@Override
			public void invalidated(Observable observable) 
			{
				if (!slider.isValueChanging() && (mp.getStatus() == Status.PLAYING || mp.getStatus() == Status.PAUSED))
				{
					mp.seek(Duration.seconds(slider.getValue()));
				}
				
				else
				{
					slider.setValue(0);
				}
			}
		});
		
		/*on slider click*/
		slider.valueProperty().addListener(new InvalidationListener() 
		{
			@Override
			public void invalidated(Observable observable) 
			{
				if (mp.getStatus() == Status.PLAYING || mp.getStatus() == Status.PAUSED)
				{
					if (Math.abs(mp.getCurrentTime().toSeconds() - slider.getValue()) > 1)
					{
						mp.seek(Duration.seconds(slider.getValue()));
					}
				}
				
				else
				{
					slider.setValue(0);
				}
			}
		});
		
		/*moving slider*/
		mp.currentTimeProperty().addListener(new InvalidationListener() 
		{
			@Override
			public void invalidated(Observable observable) 
			{
				if (! slider.isValueChanging())	
				{
					slider.setValue(mp.getCurrentTime().toSeconds());
				}
				
				timeLbl.setText(durationToString(mp.getCurrentTime()));
			}
		});
	}
	
	public void mediaPlayerInit()
	{
		mp = new MediaPlayer(media);
		volumeSliderInit();
		rateSliderInit();
		progressSliderInit();					
		fileName.setText(media.getSource().replaceAll("%20", " "));
		rateSlider.setValue(1);
	}
	
	private String durationToString (Duration dur) 
	{
		double sum =  dur.toSeconds();
		LocalTime ltime = LocalTime.ofSecondOfDay((long) sum);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String sTime = formatter.format(ltime);
		
		return sTime;
	}
	
	public void saveTxt (ActionEvent event)
	{
		FileChooser saveChooser = new FileChooser();
		saveChooser.setTitle("Save As...");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("*.txt", "*.txt");
		saveChooser.getExtensionFilters().add(extFilter);
		saveChooser.setInitialFileName(".txt");
		
		File saveFile = saveChooser.showSaveDialog(null);
		
		if (saveFile != null)
		{
			FileWriter fileWriter;
			try 
			{
				fileWriter = new FileWriter(saveFile);
				fileWriter.write(text.getText());
				fileWriter.close();
			}
			
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void openAction (ActionEvent event)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open");
		fileChooser.setInitialDirectory(new File("/home/rosz/Downloads"));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("*.mp3, *.mp4, *.m4a, *.wav, *.aiff, *.aif", "*.mp3", "*.mp4", "*.m4a", "*.wav", "*.aiff", "*.aif"));
		mediaFile = fileChooser.showOpenDialog(null);
		
		if (mediaFile !=null)		
		{				
			media = new Media(mediaFile.toURI().toString());
			
			if (firstOpen)
			{
				if (mp.getStatus() == Status.PLAYING)
					mp.stop();				
			}
			else
				firstOpen = true;
			
			mediaPlayerInit();			
		
			mp.setOnReady(new Runnable() 
			{
				public void run() 
				{
					mediaDuration = media.getDuration();
					timeLbl.setText(durationToString(mp.getCurrentTime()));
					durationLbl.setText(durationToString(mediaDuration));
					slider.setMax(mp.getTotalDuration().toSeconds());
				}
			});
		
			mp.setOnEndOfMedia(new Runnable() 
			{
				@Override
				public void run() 
				{
					mp.stop();
					mp.seek(Duration.ZERO);
				}
			});
		}
	}
	
}
