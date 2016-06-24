package application;

import java.io.File;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;


//
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
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
	
	@Override
	public void initialize(URL location, ResourceBundle resources) 
	{
		timeLbl.setText("hh:mm:ss");
		durationLbl.setText("hh:mm:ss");
		slider.setDisable(true);
		volumeSlider.setValue(0.8);
		
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
	
	public void openAction (ActionEvent event)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("/home/rosz/Downloads"));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("*.mp3, *.mp4, *.m4a, *.wav, *.aiff, *.aif", "*.mp3", "*.mp4", "*.m4a", "*.wav", "*.aiff", "*.aif"));
		
		mediaFile = fileChooser.showOpenDialog(null);
		if (mediaFile !=null)
		{	
			
		media = new Media(mediaFile.toURI().toString());
		fileName.setText(media.getSource().replaceAll("%20", " "));
		
		mp = new MediaPlayer(media);
		
		if (mp.getStatus() == Status.PLAYING)
		{
			mp.stop();
		}
		
		mp.setOnReady(new Runnable() {
			public void run() {
				mediaDuration = media.getDuration();
				timeLbl.setText(durationToString(mp.getCurrentTime()));
				durationLbl.setText(durationToString(mediaDuration));
			
				System.out.println(String.valueOf((long)mediaDuration.toSeconds()));
				
				/*volume slider*/
				
				mp.setVolume(volumeSlider.getValue());
				volumeSlider.valueProperty().addListener(new InvalidationListener() {
					
					@Override
					public void invalidated(Observable observable) {
						mp.setVolume(volumeSlider.getValue());
						
					}
				});
				
				/*rate slider*/
				rateSlider.setValue(1);
				rateSlider.valueProperty().addListener(new InvalidationListener() {
					
					@Override
					public void invalidated(Observable observable) {
						mp.setRate(rateSlider.getValue());
						
					}
				});
				
				/*progress slider*/
				slider.setDisable(false);
				slider.setMax(mp.getTotalDuration().toSeconds());
				
				////////////////////////przesuwanie slidera
				slider.valueChangingProperty().addListener(new InvalidationListener() {
					
					@Override
					public void invalidated(Observable observable) {
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
				
				////////////////////////kliknięcie w slider
				slider.valueProperty().addListener(new InvalidationListener() {
					
					@Override
					public void invalidated(Observable observable) {
//						if (!slider.isValueChanging() && (mp.getStatus() == Status.PLAYING || mp.getStatus() == Status.PAUSED))
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
				
				//////////////////////
				mp.currentTimeProperty().addListener(new InvalidationListener() 
				{
					
					@Override
					public void invalidated(Observable observable) 
					{
						if (! slider.isValueChanging())						//jesli uzytkownik nie porusza sliderem
						{
							slider.setValue(mp.getCurrentTime().toSeconds());
						}
						
						timeLbl.setText(durationToString(mp.getCurrentTime()));
					}
				});
				
			}
		});
		
		mp.setOnEndOfMedia(new Runnable() {
			
			@Override
			public void run() {
				mp.stop();
				mp.seek(Duration.ZERO);
				
			}
		});
		}
		
	}
	
	
	private String durationToString (Duration dur) 
	{
		double sum =  dur.toSeconds();
		LocalTime ltime = LocalTime.ofSecondOfDay((long) sum);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String sTime = formatter.format(ltime);
		
		return sTime;
	}

}
