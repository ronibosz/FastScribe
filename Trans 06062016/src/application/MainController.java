package application;

import java.io.*;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.animation.*;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.*;
import javafx.util.Duration;

public class MainController implements Initializable{
	
	@FXML private Button playButton;
	@FXML private Button stopButton;
	@FXML private Button rewButton;
	@FXML private Button forwButton;
	@FXML private Slider slider;
	@FXML private Slider rateSlider;
	@FXML private Slider volumeSlider;
	@FXML private Slider timeSlider;
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
	public void initialize(URL location, ResourceBundle resources){
		
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
		backupFile = new File(System.getProperty("user.home") + "/BackupTextFile000.txt");
		
		backup();
		setAccelerators();
	}
	
	public void backup(){
		
		Timeline backupTimeline = new Timeline(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>(){

		    @Override
		    public void handle(ActionEvent event){
		    	
		        FileWriter fileWriter;
				try{
					
					fileWriter = new FileWriter(backupFile);
					fileWriter.write(text.getText().replaceAll("\n", System.getProperty("line.separator")));
					fileWriter.close();
				}
				
				catch (IOException e){
					
					e.printStackTrace();
				}
		    }
		}));
		
		backupTimeline.setCycleCount(Timeline.INDEFINITE);
		backupTimeline.play();
	}
	
	public void setAccelerators(){
		
		saveMI.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
		saveAsMI.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		openSoundMI.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
		openTxtMI.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN));
		NewMI.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
	}
	
	public void setupListeners(Stage stage){
		
		controllerStage = stage;
		
		setPlayerShortcuts();
		setOnClose();
		disableCtrlZ();
		rateSliderDoubleClick();
	}
	
	public void setPlayerShortcuts(){
		
		rewKeyComb = new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN);
		forwKeyComb = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
		playKeyComb = new KeyCodeCombination(KeyCode.SPACE, KeyCombination.SHORTCUT_DOWN);
		stopKeyComb = new KeyCodeCombination(KeyCode.SPACE, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);
		
		controllerStage.getScene().setOnKeyPressed(new EventHandler<KeyEvent>(){
			
			@Override
			public void handle(KeyEvent ke){
				
				if (playKeyComb.match(ke))
					playpause();
				
				if (stopKeyComb.match(ke))
					stop();
				
				if (rewKeyComb.match(ke))
					rewind();
				
				if (forwKeyComb.match(ke))
					forward();
			}
		});
	}
	
	public void playpause (){
		
		if (media != null){
				
			if (mp.getStatus() != Status.PLAYING){
				
				mp.play();
				playButton.setGraphic(new ImageView("/media/pauseicon.png"));
			} 
			
			else{
				
				mp.pause();
				playButton.setGraphic(new ImageView("/media/playicon.png"));
			}
		}
	}
	
	public void stop (){
		
		if (media != null){
			
			mp.stop();
			playButton.setGraphic(new ImageView("/media/playicon.png"));
		}
	}
	
	public void rewind (){
		
		if (media != null)
			mp.seek(mp.getCurrentTime().add(Duration.seconds(-timeSlider.getValue())));
		
		if (mp.getStatus() != Status.PLAYING)
			mp.play();
	}
	
	public void forward (){
		
		if (media != null)
			mp.seek(mp.getCurrentTime().add(Duration.seconds(timeSlider.getValue())));
		
		if (mp.getStatus() != Status.PLAYING)
			mp.play();
	}
	
	public void setOnClose(){
		
		controllerStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
			
			@Override
			public void handle(WindowEvent we){
				
				if (saveAlert())
					controllerStage.close();		
				
				else
					we.consume();
			}
		});
	}
	
	public boolean saveAlert(){
		
		ButtonType yesBT = new ButtonType("Yes");
		ButtonType noBT = new ButtonType("No");
		ButtonType cancelBT = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);		
		Alert saveAlert = new Alert(AlertType.CONFIRMATION);
		
		saveAlert.setContentText("Save changes to document \"" + txtNameLbl.getText() +"\"?");
		saveAlert.getButtonTypes().setAll(yesBT, noBT, cancelBT);
		saveAlert.setHeaderText("");
		saveAlert.setTitle("Save");
		
		Optional<ButtonType> result = saveAlert.showAndWait();
		
		if (result.get() == yesBT){
			
			if (!saveMI.isDisable()){
				
				save();
				return true;
			}
			
			else{
				
				if (saveAs())
					return true;
				
				else 
					return false;
			}
		}
		
		else if (result.get() == noBT)
			return true;
		
		else
			return false;
	}

	public void disableCtrlZ(){
		
		ctrlZKeyComb = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);		
		text.setOnKeyPressed(new EventHandler<KeyEvent>(){
			
			@Override
			public void handle(KeyEvent ke){
				
				if (ctrlZKeyComb.match(ke))
					ke.consume();
			}
		});
	}
	
	public void rateSliderDoubleClick(){
		
		rateSlider.setOnMouseClicked(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent event){
				
				if (event.getClickCount() == 2)
					rateSlider.setValue(1);
			}
		});
	}
	
	private String durationToString (Duration dur){
		
		double sum =  dur.toSeconds();
		LocalTime ltime = LocalTime.ofSecondOfDay((long) sum);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String sTime = formatter.format(ltime);
		
		return sTime;
	}
	
	public void openSoundAction (){
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("*.mp3, *.mp4, *.m4a, *.wav, *.aiff, *.aif", "*.mp3", "*.mp4", "*.m4a", "*.wav", "*.aiff", "*.aif"));
		mediaFile = fileChooser.showOpenDialog(null);
		
		if (mediaFile !=null){	
			
			media = new Media(mediaFile.toURI().toString());
			if (firstOpen){
				
				if (mp.getStatus() == Status.PLAYING)
					mp.stop();		
					playButton.setGraphic(new ImageView("/media/playicon.png"));
			}
			else
				firstOpen = true;
			
			mediaPlayerInit();			
		
			mp.setOnReady(new Runnable(){
				
				public void run(){
					
					mediaDuration = media.getDuration();
					timeLbl.setText(durationToString(mp.getCurrentTime()));
					durationLbl.setText(durationToString(mediaDuration));
					slider.setMax(mp.getTotalDuration().toSeconds());
				}
			});
		
			mp.setOnEndOfMedia(new Runnable(){
				
				@Override
				public void run(){
					
					mp.stop();
					mp.seek(Duration.ZERO);
					playButton.setGraphic(new ImageView("/media/playicon.png"));
				}
			});
		}
	}
	
	public void mediaPlayerInit(){
		
		mp = new MediaPlayer(media);
		volumeSliderInit();
		rateSliderInit();
		progressSliderInit();					
		fileName.setText(media.getSource().replaceAll("%20", " ").substring(6));
		rateSlider.setValue(1);
	}
	
	public void volumeSliderInit(){
		
		mp.setVolume(volumeSlider.getValue());
		volumeSlider.valueProperty().addListener(new InvalidationListener(){
			
			@Override
			public void invalidated(Observable observable){
				
				mp.setVolume(volumeSlider.getValue());
			}
		});
	}
	
	public void rateSliderInit(){		
		
		rateSlider.valueProperty().addListener(new InvalidationListener(){	
			
			@Override
			public void invalidated(Observable observable){
				
				mp.setRate(rateSlider.getValue());
			}
		});
			
		
	}
	
	public void progressSliderInit(){
		
		slider.setDisable(false);
		slider.valueProperty().addListener(new InvalidationListener(){
			
			@Override
			public void invalidated(Observable observable){
				
				if (mp.getStatus() == Status.PLAYING || mp.getStatus() == Status.PAUSED){
					
					if (Math.abs(mp.getCurrentTime().toSeconds() - slider.getValue()) > 1){
						
						mp.seek(Duration.seconds(slider.getValue()));
					}
				}
				
				else{
					
					slider.setValue(0);
				}
			}
		});
		
		mp.currentTimeProperty().addListener(new InvalidationListener(){
			
			@Override
			public void invalidated(Observable observable){
				
				if (! slider.isValueChanging()){
					
					slider.setValue(mp.getCurrentTime().toSeconds());
				}
				
				timeLbl.setText(durationToString(mp.getCurrentTime()));
			}
		});
	}
	
	public void newFile(){
		
		if (saveAlert()){
			
			saveMI.setDisable(true);
			text.setText("");
			txtNameLbl.setText("New file (unsaved)");			
		}
		
		else{
			
			return;
		}
		
	}
	
	public void openTxt(){	
		
		if (saveAlert()){
			
			FileChooser txtChooser = new FileChooser();
			txtChooser.setTitle("Open .txt");
			txtChooser.getExtensionFilters().addAll(new ExtensionFilter("*.txt", "*.txt"));
		
			if ((tempTxtFile = txtChooser.showOpenDialog(null)) != null){			
			
				txtFile = tempTxtFile;
				text.setText(fileToString(txtFile));
				
				if (txtFile.getPath().equals(backupFile.getAbsolutePath()))
					saveAs();
		
				txtNameLbl.setText(txtFile.getAbsolutePath());
				saveMI.setDisable(false);	
		
			}
		}
	}
	
	public String fileToString(File file){
		
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		
		try{
			
			bufferedReader = new BufferedReader(new FileReader(file));
			String line;	
			while ((line = bufferedReader.readLine()) != null){
				
				stringBuilder.append(line + "\n");
			}
		} catch (FileNotFoundException e){
			
			e.printStackTrace();
			
		} catch (IOException e){
			
			e.printStackTrace();
			
		} finally{
			
			try{
				
				bufferedReader.close();
				
			} catch (IOException e){
				
				e.printStackTrace();
			}
		}
		
		return stringBuilder.toString();
	}
	
	public boolean saveAs (){
		
		FileChooser saveChooser = new FileChooser();
		saveChooser.setTitle("Save As...");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("*.txt", "*.txt");
		saveChooser.getExtensionFilters().add(extFilter);
		saveChooser.setInitialFileName(".txt");
		
		File saveFile = saveChooser.showSaveDialog(null);

		
		if (saveFile != null){
			
			FileWriter fileWriter;
			
			try{
				fileWriter = new FileWriter(saveFile);
				fileWriter.write(text.getText().replaceAll("\n", System.getProperty("line.separator")));
				fileWriter.close();
				txtFile = saveFile;
				txtNameLbl.setText(txtFile.getAbsolutePath());
				saveMI.setDisable(false);
			} catch (IOException e){
				
				e.printStackTrace();
			}
			
			return true;
		}
		
		else 
			return false;
	}
	
	public void save(){
		
		if (!saveMI.isDisable()){
			
			if (txtFile != null){
				
				FileWriter fileWriter;
				
				try{
					
					fileWriter = new FileWriter(txtFile);
					fileWriter.write(text.getText().replaceAll("\n", System.getProperty("line.separator")));
					fileWriter.close();
				} catch (IOException e){
					
					e.printStackTrace();
				}
			}
		}
		
		else
			saveAs();
	}
	
	public void shortcutsDialog(){
		
		Alert shortcutsAlert = new Alert(AlertType.INFORMATION);
		shortcutsAlert.setTitle("Shortcuts");
		shortcutsAlert.setHeaderText(null);
		shortcutsAlert.setContentText("Ctrl+R --> Rewind\nCtrl+F --> Forward\nCtrl+Space --> Play/Pause\nCtrl+Alt+Space --> Stop");
		shortcutsAlert.showAndWait();
	}
	
	public void aboutDialog(){
		
		Alert shortcutsAlert = new Alert(AlertType.INFORMATION);
		shortcutsAlert.setTitle("About");
		shortcutsAlert.setHeaderText(null);
		shortcutsAlert.setContentText("FastScribe 1.1.4");
		shortcutsAlert.showAndWait();
	}
}
