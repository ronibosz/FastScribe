package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class MainController implements Initializable
{
	@FXML private Button playButton;
	@FXML private Button stopButton;
	@FXML private Button rewButton;
	@FXML private Button forwButton;
	@FXML private Slider slider;
	@FXML private Slider rateSlider;
	@FXML private Slider volumeSlider;
	@FXML private Label timeLbl;
	@FXML private Label durationLbl;
	@FXML private Label fileName;
	@FXML private Label txtNameLbl;
	@FXML private TextArea text;
	@FXML private MenuItem saveMI;
	@FXML private MenuItem saveAsMI;
	@FXML private MenuItem NewMI;
	@FXML private MenuItem openSoundMI;
	@FXML private MenuItem openTxtMI;
	
	private Stage controllerStage;
	private Media media;
	private MediaPlayer mp;
	private File mediaFile;
	private Duration mediaDuration;
	private boolean firstOpen;
	private KeyCombination rewKeyComb;
	private KeyCombination forwKeyComb;
	private KeyCombination playKeyComb;
	private KeyCombination stopKeyComb;
	private KeyCombination ctrlZKeyComb;
	private File txtFile;
	private File backupFile;
	File tempTxtFile;

	@Override
	public void initialize(URL location, ResourceBundle resources) 
	{
		timeLbl.setText("hh:mm:ss");
		durationLbl.setText("hh:mm:ss");
		txtNameLbl.setText("New file (unsaved)");
		forwButton.setGraphic(new ImageView("/media/forwicon.png"));
		rewButton.setGraphic(new ImageView("/media/rewicon.png"));
		stopButton.setGraphic(new ImageView("/media/stopicon.png"));
		playButton.setGraphic(new ImageView("/media/playicon.png"));
		slider.setDisable(true);
		volumeSlider.setValue(0.8);
		rateSlider.setValue(1);
		firstOpen = false;
		saveMI.setDisable(true);
		setAccelerators();
		
		backupFile = new File(System.getProperty("user.dir") + "/BackupTextFile000.txt");
		backup();
	}
	
	public void setAccelerators()
	{
		saveMI.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
		saveAsMI.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		openSoundMI.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
		openTxtMI.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN));
		NewMI.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
	}
	
	public void setupListeners(Stage stage)
	{
		controllerStage = stage;
		
		playerShortcuts();
		stageOnClose();
		disableCtrlZ();
		rateSliderDoubleClick();
	}
	
	public void playerShortcuts()
	{
		rewKeyComb = new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN);
		forwKeyComb = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
		playKeyComb = new KeyCodeCombination(KeyCode.SPACE, KeyCombination.SHORTCUT_DOWN);
		stopKeyComb = new KeyCodeCombination(KeyCode.SPACE, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);
		
		controllerStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() 
		{
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
	
	public void stageOnClose()
	{
		controllerStage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent we)
			{
				if (saveAlert())
				{
					controllerStage.close();
				}
				else
				{
					we.consume();
				}
			}
		});
	}

	public void disableCtrlZ()
	{
		ctrlZKeyComb = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
		
		text.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent ke)
			{
				if (ctrlZKeyComb.match(ke))
					ke.consume();
			}
		});
	}
	
	public void rateSliderDoubleClick()
	{
		rateSlider.setOnMouseClicked(new EventHandler<MouseEvent>() 
		{

			@Override
			public void handle(MouseEvent event) 
			{
				if (event.getClickCount() == 2)
					rateSlider.setValue(1);
			}
		});
	}
	public void playpause (ActionEvent event)
	{
		if (media != null)
		{
				
			if (mp.getStatus() != Status.PLAYING)
			{
				mp.play();
				playButton.setGraphic(new ImageView("/media/pauseicon.png"));
			} 
			
			else
			{
				mp.pause();
				playButton.setGraphic(new ImageView("/media/playicon.png"));
			}
		}
	}
	
	public void stop (ActionEvent event)
	{
		if (media != null)
		{
			mp.stop();
			playButton.setGraphic(new ImageView("/media/playicon.png"));
		}
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
//		slider.valueChangingProperty().addListener(new InvalidationListener() 
//		{
//			@Override
//			public void invalidated(Observable observable) 
//			{
//				if (!slider.isValueChanging() && (mp.getStatus() == Status.PLAYING || mp.getStatus() == Status.PAUSED))
//				{
//					mp.seek(Duration.seconds(slider.getValue()));
//				}
//				
//				else
//				{
//					slider.setValue(0);
//				}
//			}
//		});
		
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
		fileName.setText(media.getSource().replaceAll("%20", " ").substring(6));
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
	
	public boolean saveAlert()
	{
		ButtonType yesBT = new ButtonType("Yes");
		ButtonType noBT = new ButtonType("No");
		ButtonType cancelBT = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		
		Alert saveAlert = new Alert(AlertType.CONFIRMATION);
		saveAlert.setContentText("Save changes to document \"" + txtNameLbl.getText() +"\"?");
		saveAlert.getButtonTypes().setAll(yesBT, noBT, cancelBT);
		saveAlert.setHeaderText("");
		saveAlert.setTitle("Save");
		
		Optional<ButtonType> result = saveAlert.showAndWait();
		
		if (result.get() == yesBT)
		{
			if (!saveMI.isDisable())
			{
				save();
				return true;
			}
			else
			{
				if (saveTxt(null))
					return true;
				else 
					return false;
			}
		}
		else if (result.get() == noBT)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void newFile()
	{
		if (saveAlert())
		{
			saveMI.setDisable(true);
			text.setText("");
			txtNameLbl.setText("New file (unsaved)");
//			saveTxt(null);
			
		}
		else
		{
			return;
		}
		
	}
	
	public void openTxt(ActionEvent event)
	{	
		if (saveAlert())
		{
			
		FileChooser txtChooser = new FileChooser();
		txtChooser.setTitle("Open .txt");
//		txtChooser.setInitialDirectory(new File("/home/rosz/Downloads"));
		txtChooser.getExtensionFilters().addAll(new ExtensionFilter("*.txt", "*.txt"));
		
		if ((tempTxtFile = txtChooser.showOpenDialog(null)) != null)
		{
			
			
			txtFile = tempTxtFile;
//			txtFile = txtChooser.showOpenDialog(null);
			text.setText(fileToString(txtFile));
				
			if (txtFile.getPath().equals(backupFile.getAbsolutePath()))
				saveTxt(null);
		
			txtNameLbl.setText(txtFile.getAbsolutePath());
			saveMI.setDisable(false);	
		
		}
		}
	}
	
	public String fileToString(File file)
	{
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try 
		{
			bufferedReader = new BufferedReader(new FileReader(file));
			String line;	
			while ((line = bufferedReader.readLine()) != null)
			{
				stringBuilder.append(line + "\n");
			}
		} catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally 
		{
			try 
			{
				bufferedReader.close();
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return stringBuilder.toString();
	}
	
	public boolean saveTxt (ActionEvent event)
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
				fileWriter.write(text.getText().replaceAll("\n", System.getProperty("line.separator")));
				fileWriter.close();
				txtFile = saveFile;
				txtNameLbl.setText(txtFile.getAbsolutePath());
				saveMI.setDisable(false);
				
			}
			
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		else 
			return false;
	}
	
	public void save()
	{
		if (!saveMI.isDisable())
		{
			if (txtFile != null)
			{
				FileWriter fileWriter;
				try 
				{
					fileWriter = new FileWriter(txtFile);
					fileWriter.write(text.getText().replaceAll("\n", System.getProperty("line.separator")));
					fileWriter.close();
				}
			
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else
			saveTxt(null);
	}
	
	public void backup()
	{
		Timeline backupTimeline = new Timeline(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {

		    @Override
		    public void handle(ActionEvent event) 
		    {
		        FileWriter fileWriter;
				try 
				{
					fileWriter = new FileWriter(backupFile);
					fileWriter.write(text.getText());
					fileWriter.close();
				}
				
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}));
		
		backupTimeline.setCycleCount(Timeline.INDEFINITE);
		backupTimeline.play();
	}
	
	public void openAction (ActionEvent event)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open");
//		fileChooser.setInitialDirectory(new File("/home/rosz/Downloads"));
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
					playButton.setGraphic(new ImageView("/media/playicon.png"));
				}
			});
		}
	}

	public void shortcutsDialog(ActionEvent event)
	{
		Alert shortcutsAlert = new Alert(AlertType.INFORMATION);
		shortcutsAlert.setTitle("Shortcuts");
		shortcutsAlert.setHeaderText("Shortcuts");
		shortcutsAlert.setContentText("Ctrl+R --> Rewind\nCtrl+F --> Forward\nCtrl+Space --> Play/Pause\nCtrl+Alt+Space --> Stop");
		shortcutsAlert.showAndWait();
	}
	
	public void aboutDialog(ActionEvent event)
	{
		Alert shortcutsAlert = new Alert(AlertType.INFORMATION);
		shortcutsAlert.setTitle("About");
		shortcutsAlert.setHeaderText(null);
		shortcutsAlert.setContentText("FastScribe 1.1.1");
		shortcutsAlert.showAndWait();
	}
}
