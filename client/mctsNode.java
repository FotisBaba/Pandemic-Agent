package PLH512.client;

import java.util.ArrayList;
import java.util.Random;
import PLH512.server.Board;

public class mctsNode{
	public Board b;
	private mctsNode parent;
	private ArrayList<mctsNode> unvisitedChildren;
	private ArrayList<mctsNode> visitedChildren;
	public Client c;
	public String move;
	public int playerID;
	public boolean fullyExpanded;
	boolean expanded;
	public int visitCount;
	int victoryCount;
	public int visited;
	public int level;
	
	public mctsNode(Board b,mctsNode parent,int playerID) {
		this.b=b; //keep track of the board that this node exist
		this.parent=parent; //keep track of the parent of this node
		this.playerID=playerID; //keep track of the playerID that owns this node
		this.unvisitedChildren=new ArrayList<mctsNode>(); //list with unvisited children
		this.visitedChildren=new ArrayList<mctsNode>(); //list with visited children
		this.fullyExpanded=false; //true if at all the children of this node,we have started at least one simulation 
		this.expanded=false; //true if node has been expanded
		this.visitCount=0; //how many time we visited this node
		this.visited=0; //how many times we started a simulation from this node
		this.victoryCount=0; // how many times we reach a win through this node
		if(this.isRoot()) 
			this.level=0;
		else 
			this.level = parent.level+1;	
		
		
	}
	
	public mctsNode getParent() {
		return this.parent;
	}
	
	public int getLevel(mctsNode n) {
		return this.level;
	}
	
	public void setLevel(int lvl) {
		this.level=lvl;
	}
	
	public boolean isRoot() {
		return this.parent == null;
	}
	
	public Board getBoard() {
		return this.b;
	}
	
	public ArrayList<mctsNode> getUnvisitedChildren(){
		return this.unvisitedChildren;
	}
	
	public ArrayList<mctsNode> getVisitedChildren(){
		return this.visitedChildren;
	}
	public boolean removeUnvisitedChild(mctsNode n) {
	    return this.unvisitedChildren.remove(n);
	}
	
	public void addUnVisitedChild(mctsNode n) {
	    this.unvisitedChildren.add(n);
	}

	public void removeVisitedChild(mctsNode n) {
	    this.visitedChildren.remove(n);
	}

	public void addVisitedChild(mctsNode n) {
	    this.visitedChildren.add(n);
	}
	
	public boolean isFullyExpanded() {
		return this.fullyExpanded;
	}
	
	public boolean isTerminal() {
		return b.getGameEnded();
	}
	
	public boolean is4lvlTerminal() {
		return this.level == 5;
	}
	
	
	public boolean isExpanded() {
		return this.expanded;
	}
	
	public double getVictoryCount() {
		return this.victoryCount;
	}
	
	public double getVisitCount() {
		return this.visitCount;
	}
	
	public void printNodeStatistics(mctsNode n) {
		System.out.println("fullyExpanded-->"+n.fullyExpanded);
		System.out.println("expanded-->"+n.expanded);
		System.out.println("level-->"+n.level);
		System.out.println("move-->"+n.move);
		System.out.println("playerID-->"+n.playerID);
		System.out.println("victoryCount-->"+n.victoryCount);
		System.out.println("visitCount-->"+n.visitCount);
	}
	
	
	
	
	
	public boolean expand(int playerID) {
		
		System.out.println("Expanding in mctsnode");
		if(isFullyExpanded()) { //cannot expand more
			fullyExpanded=true;
			return true;
		}
		if(isExpanded()) { //father already expanded,do not expand again
			//System.out.println("isexpandededededeededededed");
			return true;
		}
		//Here I am ready to expand this node
		this.expanded=true;
		ArrayList<String> moves = Client.getLegalMoves(playerID,b);
		
		for(int i=0;i<moves.size();i++) {
			Board cloneBoard = Client.copyBoard(this.b);
			mctsNode node = new mctsNode(Board.performMove(moves.get(i), cloneBoard),this,playerID);
			node.move = moves.get(i);
			//node.level=this.level+1;
			this.addUnVisitedChild(node);
			//printNodeStatistics(node);
		}
		
		return false;
		
		
	}
	public mctsNode expandLast(int playerID) {
		ArrayList<String> moves = Client.getLegalMoves(playerID, this.b);
		
		for(int i=0;i<moves.size();i++) {
			
			mctsNode lastPlayed = new mctsNode(Board.performMove(moves.get(i), this.b),this,playerID);
			lastPlayed.move = moves.get(i);
			
			updateStats(lastPlayed.b);
			
			this.addUnVisitedChild(lastPlayed);
		}
		return bestLastChild(this);
		
	}
	
	
	public mctsNode bestLastChild(mctsNode mc) {
		int max = 0;
		mctsNode theBest=null;
		for(mctsNode child : this.getUnvisitedChildren()) {
			if(child.victoryCount>=max) {
				max = child.victoryCount;
				theBest = child;
			}
		}
		
		return theBest;
		
		
	}
	
	
	
	
	//UCT function to be maximized
	//the more higher the value "2" the more UCT will explore 
	//unpromising nodes
	public double getUCT() {
	    return (this.victoryCount / this.visitCount
	        + 2 * Math.sqrt(Math.log(this.getParent().visitCount) / this.visitCount));
	  }
	
	public void updateStats(Board terminalStatus) {
		int nice = 0;
		int bad = 0;
		int rootCured = 0;
		int leafCured = 0;
		int rootEradicated = 0;
		int leafEradicated = 0;
		ArrayList<citiesWithDistancesObj> distanceMap = new ArrayList<citiesWithDistancesObj>();		
		distanceMap = Client.buildDistanceMap(this.getParent().b, this.getParent().b.getPawnsLocations(this.playerID), distanceMap);
		//update visit count of father
		this.getParent().visitCount++;
		
		for(int i=0;i<4;i++) {
			if(this.getParent().b.getCured(i))
				rootCured++;
			if(terminalStatus.getCured(i))
				leafCured++;
			if(this.getParent().b.getErradicated(i))
				rootEradicated++;
			if(terminalStatus.getErradicated(i))
				leafEradicated++;
		}
		//check if we cure a disease in the world producted by the simulation
		//if so we have a small victory
		if(rootCured<leafCured)
			this.victoryCount=this.victoryCount+20;
		//also a small victory if we eradicated a disease
		if(rootEradicated<leafEradicated)
			this.victoryCount=this.victoryCount+30;
		
		if(this.getParent().b.getResearchStationsBuild()<terminalStatus.getResearchStationsBuild()) {
			if(terminalStatus.getResearchStationsBuild()>4)
				this.victoryCount++;
			else if(terminalStatus.getResearchStationsBuild()<=4)
				this.victoryCount = this.victoryCount+5;
		}
		/*
		if(terminalStatus.getPawnsLocations(this.getParent().playerID).equals(Client.getMostInfectedInRadius(4, distanceMap, this.getParent().b)))
			this.victoryCount=this.victoryCount+4;
		else if(terminalStatus.getPawnsLocations(this.getParent().playerID).equals(Client.getMostInfectedInRadius(3, distanceMap, this.getParent().b)))
			this.victoryCount=this.victoryCount+3;
		else if(terminalStatus.getPawnsLocations(this.getParent().playerID).equals(Client.getMostInfectedInRadius(2, distanceMap, this.getParent().b)))
			this.victoryCount=this.victoryCount+2;
		else if(terminalStatus.getPawnsLocations(this.getParent().playerID).equals(Client.getMostInfectedInRadius(1, distanceMap, this.getParent().b)))
			this.victoryCount++;
			
			*/
		
		
			if(terminalStatus.getHandOf(this.getParent().playerID).size()==this.getParent().b.getHandOf(this.getParent().playerID).size()) {
				if(terminalStatus.getRoleOf(this.getParent().playerID).equals("Scientist")) {
					if(terminalStatus.getHandOf(this.getParent().playerID).size()<3)
						this.victoryCount=this.victoryCount+5;
				}else if(terminalStatus.getRoleOf(this.getParent().playerID).equals("Medic")) {
					if(terminalStatus.getHandOf(this.getParent().playerID).size()<3)
						this.victoryCount=this.victoryCount+5;
				}
			}else {
				if(terminalStatus.getRoleOf(this.getParent().playerID).equals("Scientist"))
					this.victoryCount--;
				else if(terminalStatus.getRoleOf(this.getParent().playerID).equals("Medic"))
					this.victoryCount--;
			}
			
			//check if infection rate remains still
		//and count it as a small victory
		//if(this.getBoard().getInfectionRate()==terminalStatus.getInfectionRate())
		//	this.victoryCount++;
		
		
		//cubesLeft return the remaining number of cubes left
		//so we want the produced world to have more cubes left than the one
		//we started
		for(int i=0;i<4;i++) {
			if(terminalStatus.getCubesLeft(i)>this.getParent().b.getCubesLeft(i))
				this.victoryCount = this.victoryCount+5;
		}
		
		
	    if (terminalStatus.checkIfWon()) {
	      this.victoryCount+=99999999;
	    }
	    
	    //update the visitCount of the root
	   // if (!isRoot()) {
	    //  this.getParent().visitCount++;
	      //this.getParent().visitChild(this);
	   // }
	  }
	
	public void visitChild(mctsNode n) {
	    if (!this.fullyExpanded && this.removeUnvisitedChild(n)) {
	      this.addVisitedChild(n);
	    }
	    if (this.getUnvisitedChildren().size() == 0) {
	      this.fullyExpanded = true;
	    }
	  }

}
