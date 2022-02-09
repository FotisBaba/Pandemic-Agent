package PLH512.client;

import java.io.*; 
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import PLH512.server.Board;
import PLH512.server.City;

public class Client  
{
    final static int ServerPort = 64240;
    final static String username = "myName";
    final static int SC_MAX_CARDS_TO_CURE = 3;
    final static int MAX_CARDS_TO_CURE = 4;
    final static int history=50;

    
    public static ArrayList<String> dtMoves = new ArrayList<String>();
    public static ArrayList<String> dfMoves = new ArrayList<String>();
    public static ArrayList<String> cfMoves = new ArrayList<String>();
    public static ArrayList<String> sfMoves = new ArrayList<String>();
    public static ArrayList<String> brsMoves = new ArrayList<String>();
    public static ArrayList<String> rrsMoves = new ArrayList<String>();
    public static ArrayList<String> tdMoves = new ArrayList<String>();
    public static ArrayList<String> cd2Moves = new ArrayList<String>();
    
    public int actionZcounter=0;
    
    //here we make a list of list of moves to keep track 
    //the history of moves
    public static ArrayList<String> movesList = new ArrayList<String>();
    public static ArrayList<ArrayList<String>> commonMoves = new ArrayList<ArrayList<String>>();
    


  
    public static void main(String args[]) throws UnknownHostException, IOException, ClassNotFoundException  
    { 
    	int numberOfPlayers;
    	int myPlayerID;
    	String myUsername;
    	String myRole;
    	ArrayList<Integer> totalVictoryCounts = new ArrayList<Integer>(4);
        // Getting localhost ip 
        InetAddress ip = InetAddress.getByName("localhost"); 
     
        // Establish the connection 
        Socket s = new Socket(ip, ServerPort); 
        System.out.println("\nConnected to server!");
        
        // Obtaining input and out streams 
        ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream dis = new ObjectInputStream(s.getInputStream());  
        
        // Receiving the playerID from the Server
        myPlayerID = (int)dis.readObject();
        myUsername = "User_" + myPlayerID;
        System.out.println("\nHey! My username is " + myUsername);
        
        // Receiving number of players to initialize the board
        numberOfPlayers = (int)dis.readObject();
        
        // Receiving my role for this game
        myRole = (String)dis.readObject();
        System.out.println("\nHey! My role is " + myRole);
        
        // Sending the username to the Server
        dos.reset();
        dos.writeObject(myUsername);
        // Setting up the board
        Board[] currentBoard = {new Board(numberOfPlayers)};
        // Creating sendMessage thread 
        Thread sendMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() {
            	
            	boolean timeToTalk = false;
            	
            	//MPOREI NA GINEI WHILE  TRUE ME BREAK GIA SINTHIKI??
                while (currentBoard[0].getGameEnded() == false) 
                { 	
                	timeToTalk = ((currentBoard[0].getWhoIsTalking() == myPlayerID)  && !currentBoard[0].getTalkedForThisTurn(myPlayerID));
                	
                	try {
						TimeUnit.MILLISECONDS.sleep(15);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                	
                    try { 
                        // Executing this part of the code once per round
                        if (timeToTalk)
                        {
                        	
                        	Board myBoard = currentBoard[0];
                        	//initialize the board with respect
                        	//to who is talking and who is playing
                        	//we want currentBoard[0] to be the common board
                        	//currentBoard[1] will be the simulating board for player0
                        	//currentBoard[2]-->player1
                        	//currentBoard[3]-->player2
                        	//currentBoard[4]-->player3
                        	//for the players
                        	
                        	
                        	// Initializing variables for current round
           
                        	String myCurrentCity = myBoard.getPawnsLocations(myPlayerID);
                        	City myCurrentCityObj = myBoard.searchForCity(myCurrentCity);
                        	
                        	ArrayList<String> myHand = myBoard.getHandOf(myPlayerID);
                        	
                        	int[] myColorCount = {0, 0, 0, 0};
                        	
                        	for (int i = 0 ; i < 4 ; i++)
                        		myColorCount[i] =  cardsCounterOfColor(myBoard, myPlayerID, myBoard.getColors(i));
                        	
                        	ArrayList<citiesWithDistancesObj> distanceMap = new ArrayList<citiesWithDistancesObj>();
                        	distanceMap = buildDistanceMap(myBoard, myCurrentCity, distanceMap);
                        	
                        	
                        	String myAction = "";
                        	String mySuggestion = "";
                        	ArrayList<String> myMoves = new ArrayList<String>(4);
                        	int myActionCounter = 0;
                        	
                        	// Printing out my current hand
                        	//System.out.println("\nMy current hand...");
                        	//printHand(myHand);
                        	
                        	// Printing out current color count
                        	System.out.println("\nMy hand's color count...");
                        	for (int i = 0 ; i < 4 ; i++)
                        		System.out.println(myBoard.getColors(i) + " cards count: " + myColorCount[i]);
                        	
                        	// Printing out distance map from current city
                        	//System.out.println("\nDistance map from " + myCurrentCity);
                        	//printDistanceMap(distanceMap);
                        	
                        	
                        	
                        	
                        	// ADD YOUR CODE FROM HERE AND ON!!
                        	
                        		//LEVEL 1
                        		mctsNode mcNode = new mctsNode(myBoard,null,myBoard.getWhoIsPlaying());
                            	MonteCarloTreeSearch mcts = new MonteCarloTreeSearch(myBoard,200,myBoard.getWhoIsPlaying(),mcNode);
                            	
                            	mcNode = mcts.run();
                            	
                            	System.out.println("BEST MOVE_LVL1----->"+mcNode.move);
                            	System.out.println("VISITED COUNT----->"+mcNode.visitCount);
                            	System.out.println("VICTORY COUNT----->"+mcNode.victoryCount);
                            	System.out.println("LEVEL----->"+mcNode.level);
                            	System.out.println("SIMULATIONS----->"+mcNode.getParent().visitCount);
                            	
                            	
                            	myBoard = Board.performMove(mcNode.move, myBoard);
                            	
                            	//currentBoard[0] = Board.performMove(mcNode.move, myBoard);
                            	//myBoard = currentBoard[0];
                            	myMoves.add(0, mcNode.move);
                            	
                            	//Now we set as father to the next tree
                            	//the best node from the previous tree
                            	
                            	//reset
                            	resetNode(mcNode);
                            	
                            	
                            	
                            	//LEVEL 2
                            	MonteCarloTreeSearch mctsLvl2 = new MonteCarloTreeSearch(myBoard,200,myBoard.getWhoIsPlaying(),mcNode);
                            	mctsNode mcNodeLvl2 = new mctsNode(myBoard,mcNode,myBoard.getWhoIsPlaying());
                            	
                            	mcNodeLvl2 = mctsLvl2.run();
                            	
                            	System.out.println("BEST MOVE_LVL2----->"+mcNodeLvl2.move);
                            	System.out.println("VISITED COUNT----->"+mcNodeLvl2.visitCount);
                            	System.out.println("VICTORY COUNT----->"+mcNodeLvl2.victoryCount);
                            	System.out.println("LEVEL----->"+mcNodeLvl2.level);
                            	System.out.println("SIMULATIONS----->"+mcNodeLvl2.getParent().visitCount);

                            	myBoard = Board.performMove(mcNodeLvl2.move, myBoard);
                            	
                            	
                            	//currentBoard[0] = Board.performMove(mcNodeLvl2.move, myBoard);
                            	//myBoard = currentBoard[0];
                            	myMoves.add(1, mcNodeLvl2.move);
                            	
                            	//reset
                            	resetNode(mcNodeLvl2);
                            	
                            	//LEVEL 3
                            	MonteCarloTreeSearch mctsLvl3 = new MonteCarloTreeSearch(myBoard,200,myBoard.getWhoIsPlaying(),mcNodeLvl2);
                            	mctsNode mcNodeLvl3 = new mctsNode(myBoard,mcNodeLvl2,myBoard.getWhoIsPlaying());
                            	
                            	mcNodeLvl3 = mctsLvl3.run();
                            	
                            	System.out.println("BEST MOVE_LVL3----->"+mcNodeLvl3.move);
                            	System.out.println("VISITED COUNT----->"+mcNodeLvl3.visitCount);
                            	System.out.println("VICTORY COUNT----->"+mcNodeLvl3.victoryCount);
                            	System.out.println("LEVEL----->"+mcNodeLvl3.level);
                            	System.out.println("SIMULATIONS----->"+mcNodeLvl3.getParent().visitCount);

                            	myBoard = Board.performMove(mcNodeLvl3.move, myBoard);
                            	
                            	//currentBoard[0] = Board.performMove(mcNodeLvl3.move, myBoard);
                            	//myBoard = currentBoard[0];
                            	myMoves.add(2, mcNodeLvl3.move);
                            	
                            	//reset
                            	resetNode(mcNodeLvl3);
                            	
                            	//LEVEL 4
                            	MonteCarloTreeSearch mctsLvl4 = new MonteCarloTreeSearch(myBoard,200,myBoard.getWhoIsPlaying(),mcNodeLvl3);
                            	mctsNode mcNodeLvl4 = new mctsNode(myBoard,mcNodeLvl3,myBoard.getWhoIsPlaying());
                            	
                            	mcNodeLvl4 = mctsLvl4.run();
                            	
                            	System.out.println("BEST MOVE_LVL4----->"+mcNodeLvl4.move);
                            	System.out.println("VICITED COUNT----->"+mcNodeLvl4.visitCount);
                            	System.out.println("VICTORY COUNT----->"+mcNodeLvl4.victoryCount);
                            	System.out.println("LEVEL----->"+mcNodeLvl4.level);
                            	System.out.println("SIMULATIONS----->"+mcNodeLvl4.getParent().visitCount);

                            	myBoard = Board.performMove(mcNodeLvl4.move, myBoard);
                            	
                            	//currentBoard[0] = Board.performMove(mcNodeLvl4.move, myBoard);
                            	//myBoard = currentBoard[0];
                            	myMoves.add(3, mcNodeLvl4.move);
                            	
                            	//reset
                            	resetNode(mcNodeLvl4);
                            	
                            	
                        	
                        	// UP TO HERE!! DON'T FORGET TO EDIT THE "msgToSend"
                        	
                        	// Message type 
                        	// toTextShuttleFlight(0,Atlanta)+"#"+etc
                        	String msgToSend;
                        	if (currentBoard[0].getWhoIsPlaying() == myPlayerID) {
                        		//ArrayList<String> finalMoves = new ArrayList<String>();
                        		//finalMoves = getBetterRec(currentBoard);
                        		
                        		msgToSend = myMoves.get(0)
                    					+myMoves.get(1)
                    					+myMoves.get(2)
                    					+myMoves.get(3);
                        	}
                        		
                        		
                        		//msgToSend = "AP,"+myPlayerID+"#AP,"+myPlayerID+"#AP,"+myPlayerID+"#C,"+myPlayerID+",This was my action#AP,"+myPlayerID+"#C,"+myPlayerID+",This should not be printed..";//"Action";
                            else 
                        		msgToSend = "#C,"+myPlayerID+",This was my recommendation,"+
                        				myMoves.get(0)+"#"
                    					+myMoves.get(1)+"#"
                    					+myMoves.get(2)+"#"
                    					+myMoves.get(3)+"#"; //"Recommendation"
                        	
                        	// NO EDIT FROM HERE AND ON (EXEPT FUNCTIONS OUTSIDE OF MAIN() OF COURSE)
                        	
                        	// Writing to Server
                        	dos.flush();
                        	dos.reset();
                        	if (msgToSend != "")
                        		msgToSend = msgToSend.substring(1); // Removing the initial delimeter
                        	dos.writeObject(msgToSend);
                        	System.out.println(myUsername + " : I've just sent my " + msgToSend);
                        	currentBoard[0].setTalkedForThisTurn(true, myPlayerID);
                        }
                    } catch (IOException e) { 
                        e.printStackTrace(); 
					}
                } 
            } 
        }); 
          
        // Creating readMessage thread 
        Thread readMessage = new Thread(new Runnable()  
        { 
            @Override
            public void run() { 
            	
            	
                while (currentBoard[0].getGameEnded() == false) { 
                    try { 
                        
                    	// Reading the current board
                    	//System.out.println("READING!!!");
                    	currentBoard[0] = (Board)dis.readObject();
                    	//System.out.println("READ!!!");
                    	
                    	// Read and print Message to all clients
                    	String prtToScreen = currentBoard[0].getMessageToAllClients();
                    	if (!prtToScreen.equalsIgnoreCase(""))
                    		System.out.println(prtToScreen);
                    	
                    	// Read and print Message this client
                    	prtToScreen = currentBoard[0].getMessageToClient(myPlayerID);
                    	if (!prtToScreen.equalsIgnoreCase(""))
                    		System.out.println(prtToScreen);
                    	
                    } catch (IOException e) { 
                        e.printStackTrace(); 
                    } catch (ClassNotFoundException e) {
						e.printStackTrace();
					} 
                } 
            } 
        }); 
        
        // Starting the threads
        readMessage.start();
        sendMessage.start(); 
        
        // Checking if the game has ended
        while (true) 
        {
        	if (currentBoard[0].getGameEnded() == true) {
        		System.out.println("\nGame has finished. Closing resources.. \n");
        		//scn.close();
            	s.close();
            	System.out.println("Recources closed succesfully. Goodbye!");
            	System.exit(0);
            	break;
        }
        
        }
    } 
    
   //new methods
    


    public ArrayList<String> suggestionsWithSignalling(ArrayList<String> m1,ArrayList<String> m2,Board originalBoard){
    	
    	//the board that the game is playing
    	Board currBoard1 = originalBoard;
    	Board currBoard2 = originalBoard;
    	int firstBoard=0;
    	int secondBoard=0;
    	for(String move:m1) {
    		currBoard1 = Board.performMove(move, currBoard1);
    	}
    	
    	for(String move:m2) {
    		currBoard2 = Board.performMove(move, currBoard2);
    	}
    	
    
    	for(int i=0;i<4;i++) {
        	//check cubes left between 2 worlds

    		if(currBoard1.getCubesLeft(i)>=currBoard2.getCubesLeft(i))
        		firstBoard++;
        	else
        		secondBoard++;
    		
        	//check if a disease has been cured
    		if(currBoard1.getCured(i))
        		firstBoard++;
        	if(currBoard2.getCured(i))
        		secondBoard++;
        	
        	if(currBoard1.checkIfWon())
        		firstBoard++;
        	if(currBoard2.checkIfWon())
        		secondBoard++;
    		
    	}
    	
    	if(firstBoard<=secondBoard)
    		return m1;
    	else
    		return m2;
    	
    	
    }
    
    
    
    
    public static void resetNode(mctsNode n) {
    	n.getUnvisitedChildren().clear();
    	n.getVisitedChildren().clear();
    	n.fullyExpanded=false;
    	n.expanded=false;
    	n.visitCount=0;
    	n.visited=0;
    	n.victoryCount=0;
    }
    
    
  //return the list of legal moves that the player can play
  	public static ArrayList<String> getLegalMoves(int playerID,Board b){
  		
  		
  		ArrayList<String> tempList = new ArrayList<String>();
  		movesList.clear();
  		
  		tempList = getDTMoves(playerID,b);
  		if(!tempList.isEmpty()) {
	  		for(int i=0;i<tempList.size();i++) {
	  			movesList.add(tempList.get(i));
	  		}
  		}
  		tempList.clear();
  		
  		tempList = getDFMoves(playerID,b);
  		if(!tempList.isEmpty())	{
  			for(int i=0;i<tempList.size();i++) {
	  			movesList.add(tempList.get(i));
	  		}
  		}
  		tempList.clear();
  		
  		tempList = getCFMoves(playerID,b);
  		if(!tempList.isEmpty())	{
  			for(int i=0;i<tempList.size();i++) {
	  			movesList.add(tempList.get(i));
	  		}
  		}
  		tempList.clear();
  		
  		tempList = getSFMoves(playerID,b);
  		if(!tempList.isEmpty())	{
  			for(int i=0;i<tempList.size();i++) {
	  			movesList.add(tempList.get(i));
	  		}
  		}
  		tempList.clear();
  		
  		tempList = getBRSMoves(playerID,b);
  		if(!tempList.isEmpty())	{
  			for(int i=0;i<tempList.size();i++) {
	  			movesList.add(tempList.get(i));
	  		}
  		}
  		tempList.clear();
  		
  		tempList = getRRSMoves(playerID,b);
  		if(!tempList.isEmpty())	{
  			for(int i=0;i<tempList.size();i++) {
	  			movesList.add(tempList.get(i));
	  		}
  		}
  		tempList.clear();
  		
  		tempList = getTDMoves(playerID,b);
  		if(!tempList.isEmpty())	{
  			for(int i=0;i<tempList.size();i++) {
	  			movesList.add(tempList.get(i));
	  		}
  		}
  		tempList.clear();
  		
  		tempList = getCD2Moves(playerID,b);
  		if(!tempList.isEmpty())	{
  			for(int i=0;i<tempList.size();i++) {
	  			movesList.add(tempList.get(i));
	  		}
  		}
  		tempList.clear();

  		return movesList;
  		
  		
  		
  		
  	}
  	
  	public static void printLegalMoves(ArrayList<String> s) {
  		
  		//printDT moves
  		
  		printDTMoves(s);
  		printDFMoves(s);
  		printCFMoves(s);
  		printSFMoves(s);
  		printBRSMoves(s);
  		printRRSMoves(s);
		printTDMoves(s);
  		printCD2Moves(s);

  		
  		
  	}
    
    
    
    
    
    
    
    //Get all the legal drive/ferry actions from the city you are in
    //and keep in track the world/board you are currently in
	
  	public static ArrayList<String> getDTMoves(int playerID,Board b){
  		
  		dtMoves.clear();
  		City currentCity = b.searchForCity(b.getPawnsLocations(playerID));
  		
  		for(int i=0;i<currentCity.getNeighboursNumber();i++) {
  			dtMoves.add(toTextDriveTo(playerID,currentCity.getNeighbour(i)));
  		}
  		return dtMoves;
  		
  	}
  	
  	//Legal fly to moves
  	
  	public static ArrayList<String> getDFMoves(int playerID,Board b){
  		
  		dfMoves.clear();
  		
  		for(String card:b.getHandOf(playerID)) {
  			dfMoves.add(toTextDirectFlight(playerID,card));
  		}
  		
  		return dfMoves;
  		
  	}
  	
  	//Legal Charter fly
  	//also includes the 2nd special ability of operation expert
  	
  	public static ArrayList<String> getCFMoves(int playerID,Board b){
  		
  		cfMoves.clear();
  		String currLocation = b.getPawnsLocations(playerID);
  		ArrayList<String> allCities = b.getCityList();
  		
  		
  		//2nd special ability of operation expert
  		if(b.getRSLocations().contains(currLocation) && b.getRoleOf(playerID).equals("Operations Expert")) {
  			for(String card : b.getHandOf(playerID)) {
  				for(String city : allCities) {
  					cfMoves.add(toTextOpExpTravel(playerID,city,card));
  				}
  			}
  		}
  		
  		
  		//universal action of Charter fly
  		if(b.getHandOf(playerID).contains(currLocation)) {
  			//possible flights are everywhere
  			for(String city : allCities) {
  				//here we exclude the move to fly to the same location
  				if(!city.equals(currLocation)) {
  					cfMoves.add(toTextCharterFlight(playerID,city));
  				}
  			}
  		}
  		
  		return cfMoves;
  		
  	}
  	
  	//Legal shuttle fly
  	
  	public static ArrayList<String> getSFMoves(int playerID,Board b){
  		
  		sfMoves.clear();
  		String currLocation = b.getPawnsLocations(playerID);
  		
  		if(b.getRSLocations().contains(currLocation)) {
  			//possible moves are to move to any RS
  			for(String RS : b.getRSLocations()) {
  				if(!RS.equals(currLocation))
  					sfMoves.add(toTextShuttleFlight(playerID,RS));
  			}
  		}
  		
  		return sfMoves;
  		
  		
  	}
  	
  	//Legal build a reaserch station
  	//also the 1st special ability of OpExp
  	
  	public static ArrayList<String> getBRSMoves(int playerID,Board b){
  		
  		brsMoves.clear();
  		
  		String currLocation = b.getPawnsLocations(playerID);
  		
  		//if current location already has a RS
  		//there is no need to build there
  		if(b.getRSLocations().contains(currLocation))
  			return brsMoves;
  		
  		if(b.getHandOf(playerID).contains(currLocation)) {
  			brsMoves.add(toTextBuildRS(playerID,currLocation));
  		}
  		
  		//1st special ability of OpExp
  		if(b.getRoleOf(playerID).equals("Operations Expert")) {
  			brsMoves.add(toTextBuildRS(playerID,currLocation));
  		}
  		
  		
  		
  		return brsMoves;

  	}

  	public static ArrayList<String> getRRSMoves(int playerID,Board b){
  		
  		rrsMoves.clear();
  		String currLocation = b.getPawnsLocations(playerID);

  		
  		if(b.getResearchStationsBuild()==6) {
  			if(b.getHandOf(playerID).contains(currLocation) && !b.getRoleOf(playerID).equals("Operations Expert")) {
				//we need potential remove RS moves
				for(String rs : b.getRSLocations()) {
					rrsMoves.add(toTextRemoveRS(playerID,rs));
				}
  			}else if(b.getRoleOf(playerID).equals("Operations Expert")) {
  				for(String rs : b.getRSLocations()) {
					rrsMoves.add(toTextRemoveRS(playerID,rs));
				}
  			}
  		}
  		
  		return rrsMoves;
  		
  	}
  	//Legal treat disease
  	
  	public static ArrayList<String> getTDMoves(int playerID,Board b){
  		
  		tdMoves.clear();
  		String currLocation = b.getPawnsLocations(playerID);
  		City c = b.searchForCity(currLocation);
  		//here we try to treat a disease of any colour in 
  		//the city we are in
  		//so for potential moves we want to check what happens in the game
  		//if we try to remove a cube 
  		if(c.getColour().equals("Black") && c.getBlackCubes()>0) {
  			tdMoves.add(toTextTreatDisease(playerID,currLocation,"Black"));
  		}
  		
  		if(c.getColour().equals("Red") && c.getRedCubes()>0) {
  			tdMoves.add(toTextTreatDisease(playerID,currLocation,"Red"));
  		}
  		
  		if(c.getColour().equals("Yellow") && c.getYellowCubes()>0) {
  			tdMoves.add(toTextTreatDisease(playerID,currLocation,"Yellow"));
  		}
  		
  		if(c.getColour().equals("Blue") && c.getBlueCubes()>0) {
  			tdMoves.add(toTextTreatDisease(playerID,currLocation,"Blue"));
  		}

  		return tdMoves;
  		
  	}
  	
  	public static ArrayList<String> getCD2Moves(int playerID,Board b){
  		
  		cd2Moves.clear();
  		int redCount=0;
  		int yellowCount=0;
  		int blueCount=0;
  		int blackCount=0;
  		
  		ArrayList<String> yellowCards = new ArrayList<String>();
  		ArrayList<String> blueCards = new ArrayList<String>();
  		ArrayList<String> redCards = new ArrayList<String>();
  		ArrayList<String> blackCards = new ArrayList<String>();

  		
  		ArrayList<ArrayList<String>> possibleDiscards = new ArrayList<ArrayList<String>>();
  		
  		String currLocation = b.getPawnsLocations(playerID);  		
  		
  		//check if already eradicated
  		
  		
  		
  		
  		if(b.getRSLocations().contains(currLocation)) {
  			
  			for(String card : b.getHandOf(playerID)) {
					City c = b.searchForCity(card);
					if(c.getColour().equals("Yellow")) {
						yellowCount++;
						yellowCards.add(card);
					}	
					else if(c.getColour().equals("Black")) {
						blackCount++;
						blackCards.add(card);
					}	
					else if(c.getColour().equals("Red")) {
						redCount++;
						redCards.add(card);
					}
					else {
						blueCount++;
						blueCards.add(card);
					}
						
				}
  			
  			if(b.getRoleOf(playerID).equals("Scientist")) {
  				if(blueCount==3) {
  					cd2Moves.add(toTextCureDisease(playerID,"Blue"));
  				}else if(blueCount>3) { 
  					possibleDiscards = getAllCombinations(b.getRoleOf(playerID),blueCards);
  					for(int i=0;i<possibleDiscards.size();i++) {
  						cd2Moves.add(toTextCureDisease(playerID,"Blue",possibleDiscards.get(i).get(0),possibleDiscards.get(i).get(1),possibleDiscards.get(i).get(2)));
  					}
  					
  				}
  				possibleDiscards.clear();
  				if(redCount==3) {
  					cd2Moves.add(toTextCureDisease(playerID,"Red"));
  				}else if(redCount>3) { 
  					possibleDiscards = getAllCombinations(b.getRoleOf(playerID),redCards);
  					for(int i=0;i<possibleDiscards.size();i++) {
  						cd2Moves.add(toTextCureDisease(playerID,"Red",possibleDiscards.get(i).get(0),possibleDiscards.get(i).get(1),possibleDiscards.get(i).get(2)));
  					}
  					
  				}
  				possibleDiscards.clear();//in case the scientist joins two ifs
  				if(yellowCount==3) {
  					cd2Moves.add(toTextCureDisease(playerID,"Yellow"));
  				}else if(yellowCount>3) { 
  					possibleDiscards = getAllCombinations(b.getRoleOf(playerID),yellowCards);
  					for(int i=0;i<possibleDiscards.size();i++) {
  						cd2Moves.add(toTextCureDisease(playerID,"Yellow",possibleDiscards.get(i).get(0),possibleDiscards.get(i).get(1),possibleDiscards.get(i).get(2)));
  					}
  					
  				}
  				possibleDiscards.clear();

  				if(blackCount==3) {
  					cd2Moves.add(toTextCureDisease(playerID,"Black"));
  				}else if(blackCount>3) { 
  					possibleDiscards = getAllCombinations(b.getRoleOf(playerID),blackCards);
  					for(int i=0;i<possibleDiscards.size();i++) {
  						cd2Moves.add(toTextCureDisease(playerID,"Black",possibleDiscards.get(i).get(0),possibleDiscards.get(i).get(1),possibleDiscards.get(i).get(2)));
  					}
  					
  				}
  				possibleDiscards.clear();

  				
  				
  			}else {// not scientist
  				if(blueCount==4) {
  					cd2Moves.add(toTextCureDisease(playerID,"Blue",blueCards.get(0),blueCards.get(1),blueCards.get(2),blueCards.get(3)));
  				}else if(blueCount>4) { 
  					possibleDiscards = getAllCombinations(b.getRoleOf(playerID),blueCards);
  					for(int i=0;i<possibleDiscards.size();i++) {
  						cd2Moves.add(toTextCureDisease(playerID,"Blue",possibleDiscards.get(i).get(0),possibleDiscards.get(i).get(1),possibleDiscards.get(i).get(2),possibleDiscards.get(i).get(3)));
  					}
  					
  				}
  				possibleDiscards.clear();
  				if(redCount==4) {
  					cd2Moves.add(toTextCureDisease(playerID,"Red",redCards.get(0),redCards.get(1),redCards.get(2),redCards.get(3)));
  				}else if(blueCount>4) { 
  					possibleDiscards = getAllCombinations(b.getRoleOf(playerID),redCards);
  					for(int i=0;i<possibleDiscards.size();i++) {
  						cd2Moves.add(toTextCureDisease(playerID,"Red",possibleDiscards.get(i).get(0),possibleDiscards.get(i).get(1),possibleDiscards.get(i).get(2),possibleDiscards.get(i).get(3)));
  					}
  					
  				}
  				possibleDiscards.clear();
  				if(yellowCount==4) {
  					cd2Moves.add(toTextCureDisease(playerID,"Yellow",yellowCards.get(0),yellowCards.get(1),yellowCards.get(2),yellowCards.get(3)));
  				}else if(yellowCount>4) { 
  					possibleDiscards = getAllCombinations(b.getRoleOf(playerID),yellowCards);
  					for(int i=0;i<possibleDiscards.size();i++) {
  						cd2Moves.add(toTextCureDisease(playerID,"Yellow",possibleDiscards.get(i).get(0),possibleDiscards.get(i).get(1),possibleDiscards.get(i).get(2),possibleDiscards.get(i).get(3)));
  					}
  					
  				}
  				possibleDiscards.clear();
  				if(blackCount==4) {
  					cd2Moves.add(toTextCureDisease(playerID,"Black",blackCards.get(0),blackCards.get(1),blackCards.get(2),blackCards.get(3)));
  				}else if(blackCount>4) { 
  					possibleDiscards = getAllCombinations(b.getRoleOf(playerID),blackCards);
  					for(int i=0;i<possibleDiscards.size();i++) {
  						cd2Moves.add(toTextCureDisease(playerID,"Black",possibleDiscards.get(i).get(0),possibleDiscards.get(i).get(1),possibleDiscards.get(i).get(2),possibleDiscards.get(i).get(3)));
  					}
  					
  				}
  				possibleDiscards.clear();	
  			}	
  		}
  		
	
		return cd2Moves;
  	}
  	
  	
  	public static ArrayList<ArrayList<String>> getAllCombinations(String playerRole, ArrayList<String> cardsToDiscard){
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<ArrayList<String>> combinations = new ArrayList<ArrayList<String>>();

        int i = 0;
        int j = 1;
        int k = 2;
        int l = 3;
        
        int cardstodiscard = 4;
        if(playerRole.equals("Scientist")) {
			cardstodiscard = 3 ;
        }
        	
        
        while(i<(cardsToDiscard.size() - 3)){
            while(j<(cardsToDiscard.size() - 2)){
                while(k<(cardsToDiscard.size() - 1)){                	
                	while(l<(cardsToDiscard.size())){
                		
                		result.add(cardsToDiscard.get(i));
                        result.add(cardsToDiscard.get(j));
                        result.add(cardsToDiscard.get(k));
                		if(cardstodiscard == 3) {
                			break;
                        }                        
                        result.add(cardsToDiscard.get(l));
                        combinations.add(new ArrayList<String>(result));
                        result.clear();
                        l++;
                    }
                	k++;
                	l = k + 1;
                	if(l > cardsToDiscard.size()) break;
                }
            j++;
            k = j + 1;
            l = k + 1;
            if(k > cardsToDiscard.size() || l > cardsToDiscard.size()) break;
        }
        i++;
        j = i + 1;
        k = j + 1;
        l = k + 1;
        if(k > cardsToDiscard.size() || k > cardsToDiscard.size() || l > cardsToDiscard.size()) break;
        }
		return combinations;
	}

 
  	public static void printDTMoves(ArrayList<String> s) {
  		
  		for(int i=0;i<s.size();i++) {
  			if(s.get(i).contains("#DT"))
  				System.out.println("DTMOVE-->"+s.get(i));
  		}
  			
  	}
	public static void printDFMoves(ArrayList<String> s) {
	  		
		for(int i=0;i<s.size();i++) {
  			if(s.get(i).contains("#DF"))
  				System.out.println("DFMOVE-->"+s.get(i));
  		}
		
	  	}
	public static void printCFMoves(ArrayList<String> s) {
			
		for(int i=0;i<s.size();i++) {
  			if(s.get(i).contains("#CF"))
  				System.out.println("CFMOVE-->"+s.get(i));
  		}
		
		}
	public static void printSFMoves(ArrayList<String> s) {
			
		for(int i=0;i<s.size();i++) {
  			if(s.get(i).contains("#SF"))
  				System.out.println("SFMOVE-->"+s.get(i));
  		}
		
		}
	public static void printBRSMoves(ArrayList<String> s) {
			
		for(int i=0;i<s.size();i++) {
  			if(s.get(i).contains("#BRS"))
  				System.out.println("BRSMOVE-->"+s.get(i));
  		}
		
		}
	public static void printRRSMoves(ArrayList<String> s) {
		
		for(int i=0;i<s.size();i++) {
  			if(s.get(i).contains("#RRS"))
  				System.out.println("RRSMOVE-->"+s.get(i));
  		}
		
	}
	public static void printTDMoves(ArrayList<String> s) {
			
		for(int i=0;i<s.size();i++) {
  			if(s.get(i).contains("#TD"))
  				System.out.println("TDMOVE-->"+s.get(i));
  		}
		
		}
	public static void printCD2Moves(ArrayList<String> s) {
			
		for(int i=0;i<s.size();i++) {
  			if(s.get(i).contains("#CD2"))
  				System.out.println("CD2MOVE-->"+s.get(i));
  		}
		
		}	
  	
  	
  	

    
    
    // --> Useful functions <--
    
    public static Board copyBoard (Board boardToCopy)
    {
    	Board copyOfBoard;
    	
    	try {
    	     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	     ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
    	     outputStrm.writeObject(boardToCopy);
    	     ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    	     ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
    	     copyOfBoard = (Board)objInputStream.readObject();
    	     return copyOfBoard;
    	   }
    	   catch (Exception e) {
    	     e.printStackTrace();
    	     return null;
    	   }
    }
    
    public static String getDirectionToMove (String startingCity, String goalCity, ArrayList<citiesWithDistancesObj> distanceMap, Board myBoard)
    {
    	City startingCityObj = myBoard.searchForCity(startingCity);
    	
    	int minDistance = distanceFrom(goalCity, distanceMap);
    	int testDistance = 999;
    	
    	String directionToDrive = null;
    	String testCity = null;
    	
    	for (int i = 0 ; i < startingCityObj.getNeighboursNumber() ; i++)
    	{
    		ArrayList<citiesWithDistancesObj> testDistanceMap = new ArrayList<citiesWithDistancesObj>();
    		testDistanceMap.clear();
    		
    		testCity = startingCityObj.getNeighbour(i);
    		testDistanceMap = buildDistanceMap(myBoard, testCity, testDistanceMap);
    		testDistance = distanceFrom(goalCity, testDistanceMap);
    		
    		if (testDistance < minDistance)
    		{
    			minDistance = testDistance;
    			directionToDrive = testCity;
    		}
    	}
    	return directionToDrive;
    }
    
    
    public static String getMostInfectedInRadius(int radius, ArrayList<citiesWithDistancesObj> distanceMap, Board myBoard)
    {
    	int maxCubes = -1;
    	String mostInfected = null;
    	
    	for (int i = 0 ; i < distanceMap.size() ; i++)
    	{
    		if (distanceMap.get(i).getDistance() <= radius)
    		{
    			City cityToCheck = myBoard.searchForCity(distanceMap.get(i).getName());
    			
    			if (cityToCheck.getMaxCube() > maxCubes)
    			{
    				mostInfected = cityToCheck.getName();
    				maxCubes = cityToCheck.getMaxCube();
    			}
    		}
    	}
    	
    	return mostInfected;
    }
    
    // Count how many card of the color X player X has
    public static int cardsCounterOfColor(Board board, int  playerID, String color)
    {
    	int cardsCounter = 0;
    	
    	for (int i = 0 ; i < board.getHandOf(playerID).size() ; i++)
    		if (board.searchForCity(board.getHandOf(playerID).get(i)).getColour().equals(color))
    			cardsCounter++;
    	
    	return cardsCounter;
    }
    
    public static void printHand(ArrayList<String> handToPrint)
    {
    	for (int i = 0 ; i < handToPrint.size() ; i++)
    		System.out.println(handToPrint.get(i));
    }
    
    public static boolean alredyInDistanceMap(ArrayList<citiesWithDistancesObj> currentMap, String cityName)
    {
    	for (int i = 0 ; i < currentMap.size() ; i++)
    		if (currentMap.get(i).getName().equals(cityName))
    			return true;
    	
    	return false;
    }
    
    public static boolean isInDistanceMap (ArrayList<citiesWithDistancesObj> currentMap, String cityName)
    {
    	for (int i = 0 ; i < currentMap.size() ; i++)
    	{
    		if (currentMap.get(i).getName().equals(cityName))
    			return true;
    	}
    	return false;
    }
    
    public static void printDistanceMap(ArrayList<citiesWithDistancesObj> currentMap)
    {
    	for (int i = 0 ; i < currentMap.size() ; i++)
    		System.out.println("Distance from " + currentMap.get(i).getName() + ": " + currentMap.get(i).getDistance());
    }
    
    public static int distanceFrom(String cityToFind, ArrayList<citiesWithDistancesObj> currentDistanceMap)
    {
    	int result = -1;
    	
    	for (int i = 0 ; i < currentDistanceMap.size() ; i++)
    		if (currentDistanceMap.get(i).getName().equals(cityToFind))
    			result = currentDistanceMap.get(i).getDistance();
    	
    	return result;
    }
    
    public static int numberOfCitiesWithDistance(int distance, ArrayList<citiesWithDistancesObj> currentDistanceMap)
    {
    	int count = 0;
    	
    	for (int i = 0 ; i < currentDistanceMap.size() ; i++)
    		if (currentDistanceMap.get(i).getDistance() == distance)
    			count++;
    	
    	return count;
    }
    
    public static ArrayList<citiesWithDistancesObj> buildDistanceMap(Board myBoard, String currentCityName, ArrayList<citiesWithDistancesObj> currentMap)
    {
    	currentMap.clear();
    	currentMap.add(new citiesWithDistancesObj(currentCityName, myBoard.searchForCity(currentCityName), 0));

    	for (int n = 0 ; n < 15 ; n++)
    	{
        	for (int i = 0 ; i < currentMap.size() ; i++)
        	{
        		if (currentMap.get(i).getDistance() == (n-1))
        		{
        			for (int j = 0 ; j < currentMap.get(i).getCityObj().getNeighboursNumber() ; j++)
        			{
        				String nameOfNeighbor = currentMap.get(i).getCityObj().getNeighbour(j);
        				
        				if (!(alredyInDistanceMap(currentMap, nameOfNeighbor)))
        					currentMap.add(new citiesWithDistancesObj(nameOfNeighbor, myBoard.searchForCity(nameOfNeighbor), n));
        			}
        		}
        	}
    	}
    	
    	return currentMap;
    }
    
    
    // --> Actions <--
    
    
    // --> Coding functions <--
    
    public static String toTextDriveTo(int playerID, String destination)
    {
    	return "#DT,"+playerID+","+destination;
    }
    	
    public static String toTextDirectFlight(int playerID, String destination)
    {
    	return "#DF,"+playerID+","+destination;
    }
    
    public static String toTextCharterFlight(int playerID, String destination)
    {
    	return "#CF,"+playerID+","+destination;
    }
    
    public static String toTextShuttleFlight(int playerID, String destination)
    {
    	return "#SF,"+playerID+","+destination;
    }
    
    public static String toTextBuildRS(int playerID, String destination)
    {
    	return "#BRS,"+playerID+","+destination;
    }
    
    public static String toTextRemoveRS(int playerID, String destination)
    {
    	return "#RRS,"+playerID+","+destination;
    }
    
    public static String toTextTreatDisease(int playerID, String destination, String color)
    {
    	return "#TD,"+playerID+","+destination+","+color;
    }
    
    public static String toTextCureDisease(int playerID, String color)
    {
    	return "#CD1,"+playerID+","+color;
    }
    
    public static String toTextCureDisease(int playerID, String color, String card1, String card2, String card3, String card4)
    {
    	return "#CD2,"+playerID+","+color+","+card1+","+card2+","+card3+","+card4;
    }
    
    public static String toTextCureDisease(int playerID, String color, String card1, String card2, String card3)
    {
    	return "#CD2,"+playerID+","+color+","+card1+","+card2+","+card3;
    }

    public static String toTextActionPass(int playerID)
    {
    	return "#AP,"+playerID;
    }
    
    public static String toTextChatMessage(int playerID, String messageToSend)
    {
    	return "#C,"+playerID+","+messageToSend;
    }
    
    public static String toTextPlayGG(int playerID, String cityToBuild)
    {
    	return "#PGG,"+playerID+","+cityToBuild;
    }
    
    public static String toTextPlayQN(int playerID)
    {
    	return "#PQN,"+playerID;
    }
    public static String toTextPlayA(int playerID, int playerToMove, String cityToMoveTo)
    {
    	return "#PA,"+playerID+","+playerToMove+","+cityToMoveTo;
    }
    public static String toTextPlayF(int playerID)
    {
    	return "#PF,"+playerID;
    }
    public static String toTextPlayRP(int playerID, String cityCardToRemove)
    {
    	return "#PRP,"+playerID+","+cityCardToRemove;
    }
    public static String toTextOpExpTravel(int playerID, String destination, String colorToThrow)
    {
    	return "#OET,"+playerID+","+destination+","+colorToThrow;
    }

} 