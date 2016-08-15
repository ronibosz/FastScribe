package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.*;
import javafx.scene.*;


public class Main extends Application{
	
	double width = Screen.getPrimary().getVisualBounds().getWidth();
	double height = Screen.getPrimary().getVisualBounds().getHeight();
	
	@Override
	public void start(Stage primaryStage){
		
		try{
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/Main.fxml"));
			Parent root = (Parent)loader.load();
			MainController controller = (MainController)loader.getController();
			Scene scene = new Scene(root, width*2/3,height*4/5);
			primaryStage.setScene(scene);
			primaryStage.setTitle("FastScribe");
			primaryStage.show();
			
			controller.setupListeners(primaryStage);
			
		} catch(Exception e){
			
				e.printStackTrace();
			}
	}
	
	public static void main(String[] args){
		
		launch(args);
	}
}
