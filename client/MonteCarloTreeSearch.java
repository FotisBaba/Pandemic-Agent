package PLH512.client;

import java.util.ArrayList;
import java.util.Random;

import PLH512.client.mctsNode;
import PLH512.server.Board;
public class MonteCarloTreeSearch {
	
	mctsNode root;
	int limit;
	long iterationTime;
	int iterationCounter;
	int playerID;	
	
	public MonteCarloTreeSearch(Board b, int limit,int playerID,mctsNode parent) {
	    this.root = parent;
		this.limit = limit;
	    this.playerID = playerID;
	    
	  }
	
	public mctsNode run() {
	    if (root.isTerminal()) {
	      return root;
	    } else {
	      iterationCounter = 0;
	      //resourceAvailable()
	      while (resourceAvailable()) {
	        mctsNode leaf = traverse(root);
	        Board terminalStatus = rollout(leaf);
	        backpropagate(leaf, terminalStatus);
	        iterationCounter++;
	      }
	     return bestChild(root);
	    }
	  }
	
	public mctsNode traverse(mctsNode node) {		
		
	    if (node.isFullyExpanded()) {
	    	//System.out.println("Expansion started111111111111111");
	    	return bestUCTChild(node);
	    } else {
	      if (!node.is4lvlTerminal()) {
	    	  //System.out.println("Expansion started22222222222222");
	        node.expand(this.playerID);
	        return bestUnvisitedChild(node);
	      } else {
	        return null;
	      }
	    }
	  }
	
	public Board rollout(mctsNode leaf) {
	    Board game = Client.copyBoard(leaf.b);
	    //simulation 1 gyro mprosta
	    //leaf.printNodeStatistics(leaf);
	    //System.out.println("simu"+leaf.move);
	    return game.simulation(leaf);
	  }
	
	public void backpropagate(mctsNode leaf, Board terminalStatus) {
	    if (leaf.isRoot()) {
	      return;
	    }
	    leaf.updateStats(terminalStatus);
	  }

	  public mctsNode bestChild(mctsNode node) {
	    // TODO sort children list to perform a quicker max ?
	    double max = 0;
	    mctsNode result = null;
	    for (mctsNode child : node.getVisitedChildren()) {
	      if (max < child.visitCount) {
	        max = child.visitCount;
	        result  = child;
	      }
	    }
	    return result;
	  }
	  
	  public mctsNode bestVictoryChild(mctsNode node) {
		  double max = 0;
		  mctsNode result = null;
		  for(mctsNode child : node.getVisitedChildren()) {
			  if(max <= child.victoryCount) {
				  max = child.victoryCount;
				  result = child;
			  }
		  }
		  return result;
	  }
	  
	  

	  public mctsNode bestUCTChild(mctsNode node) {
	    // TODO sort children list to perform a quicker max ?
		  
	    double max = 0;
	    mctsNode result = null;
	    for (mctsNode child : node.getVisitedChildren()) {
	      if (max < child.getUCT()) {
	        max = child.getUCT();
	        result = (mctsNode) child;
	      }
	    }
	    System.out.println("UCTUCTUCT			"+result.getUCT());
	    return result;
	  }

	  public mctsNode bestUnvisitedChild(mctsNode node) {
	    // TODO pick at random only between unvisited nodes
		  mctsNode uChild;
		  int upb = node.getUnvisitedChildren().size();
		  if(upb==1) {
			  uChild = node.getUnvisitedChildren().get(0);
		  }else {
			  if(upb==0)
				  upb++;
			  Random rn = new Random();
			  int toBeRemoved = rn.nextInt(upb-1);
			  uChild = node.getUnvisitedChildren().get(toBeRemoved);
		  }
		  
		  //node.removeUnvisitedChild(uChild);
	    return (mctsNode) uChild; 
	  }

	  public boolean resourceAvailable() {
	    // return iterationTime + timeLimit > System.currentTimeMillis();
	    return iterationCounter < this.limit;
	  }

	  public void setRoot(Board b) {
	    Board clone = Client.copyBoard(b);
	    this.root = new mctsNode(clone, null,this.playerID);
	  }

	  public void setLimit(int limit) {
	    this.limit = limit;
	  }
	
	
	
	
}

