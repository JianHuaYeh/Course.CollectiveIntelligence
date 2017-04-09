package chap5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class KnightTour {
	public static int POP_SIZE=1000000;
	public static double ELITE_RATIO=0.2;
	public static double MUTATION_RATE=0.5;
	public static int MOVE_KIND=8;
	private int boardSize;
	private int startRow;
	private int startCol;
	private int steps;
	
	public static void main(String[] args) {
		KnightTour kt = new KnightTour(8, 3, 3);
		kt.go();
	}
	
	public KnightTour(int i, int i2, int i3) {
		this.boardSize = i;
		this.startRow = i2;
		this.startCol = i3;
		// maximal steps
		this.steps = this.boardSize*this.boardSize-1;
	}
	
	public void go() {
		int[] sol = geneticOptimize();
		printSolution(sol);
		//randomRestartHillClimbing();
	}
	
	public void printSolution(int[] sol) {
		System.out.println("Knight's starting position: ("+startRow+","+startCol+")");
		System.out.println("Maximal legal move: "+this.cost(sol));
		System.out.print("Tour sequence: ");
		for (int move: sol) System.out.print(move+" ");
		System.out.println();
		System.out.println("- 7 - 0 -");
		System.out.println("6 - - - 1");
		System.out.println("- - X - -");
		System.out.println("5 - - - 2");
		System.out.println("- 4 - 3 -");
	}
	
	public int cost(int[] sol) {
		// find maximal legal move: no double entrant
		int[][] board = new int[this.boardSize][this.boardSize];
		int currRow = this.startRow;
		int currCol = this.startCol;
		board[currRow][currCol] = 1;
		int legalMove = 0;
		// move code
        // - 7 - 0 -
        // 6 - - - 1
        // - - X - -
        // 5 - - - 2
        // - 4 - 3 -
		for (int move: sol) {
			int testRow=-1, testCol=-1;
			switch (move) {
				case 0: testRow=currRow-2; testCol=currCol+1; break;
				case 1: testRow=currRow-1; testCol=currCol+2; break;
				case 2: testRow=currRow+1; testCol=currCol+2; break;
				case 3: testRow=currRow+2; testCol=currCol+1; break;
				case 4: testRow=currRow+2; testCol=currCol-1; break;
				case 5: testRow=currRow+1; testCol=currCol-2; break;
				case 6: testRow=currRow-1; testCol=currCol-2; break;
				case 7: testRow=currRow-2; testCol=currCol-1; break;
			}
			try {
				if (board[testRow][testCol] == 0) {
					currRow = testRow;
					currCol = testCol;
					board[currRow][currCol] = 1;
					legalMove++;
				}
				//else return legalMove;
			} catch (ArrayIndexOutOfBoundsException e) {
				//return legalMove;
			}
		}
		return legalMove;
	}
	
	private ArrayList<Integer> findIllegalMove(int[] sol) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		// find maximal legal move: no double entrant
		int[][] board = new int[this.boardSize][this.boardSize];
		int currRow = this.startRow;
		int currCol = this.startCol;
		board[currRow][currCol] = 1;
		int legalMove = 0;
		// move code
		// - 7 - 0 -
		// 6 - - - 1
		// - - X - -
		// 5 - - - 2
		// - 4 - 3 -
		for (int i=0; i<sol.length; i++) {
			int move=sol[i];
			int testRow=-1, testCol=-1;
			switch (move) {
				case 0: testRow=currRow-2; testCol=currCol+1; break;
				case 1: testRow=currRow-1; testCol=currCol+2; break;
				case 2: testRow=currRow+1; testCol=currCol+2; break;
				case 3: testRow=currRow+2; testCol=currCol+1; break;
				case 4: testRow=currRow+2; testCol=currCol-1; break;
				case 5: testRow=currRow+1; testCol=currCol-2; break;
				case 6: testRow=currRow-1; testCol=currCol-2; break;
				case 7: testRow=currRow-2; testCol=currCol-1; break;
			}
			try {
				if (board[testRow][testCol] == 0) {
					currRow = testRow;
					currCol = testCol;
					board[currRow][currCol] = 1;
					legalMove++;
				}
				else result.add(i);
			} catch (ArrayIndexOutOfBoundsException e) {
				result.add(i);
			}
		}
		return result;
	}
	
	public int[] mutate(int[] r) {
		if (Math.random() < 0.5)
			return mutate0(r);
		return mutate1(r);
	}
	
    public int[] mutate0(int[] r) {
        int step = 1;
        int[] vec=r.clone();
       	// decide which queen to move
       	int which = (int)(Math.random()*steps);
       	if (Math.random()<0.5 && vec[which]>=step) // move left
       		vec[which] -= step;
       	else if (vec[which] < MOVE_KIND-step)
       		vec[which] += step;
        	
        return vec;
    }
    
    public int[] mutate1(int[] r) {
    	ArrayList<Integer> im = findIllegalMove(r);
    	int which = (int)(Math.random()*im.size());
        int step = 1;
        int[] vec=r.clone();
       	// decide which queen to move
       	//int which = (int)(Math.random()*steps);
       	if (Math.random()<0.5 && vec[which]>=step) // move left
       		vec[which] -= step;
       	else if (vec[which] < MOVE_KIND-step)
       		vec[which] += step;
        	
        return vec;
    }
    
    public int[] crossover(int[] r1, int[] r2) {
		if (Math.random() < 0.5)
			return crossover0(r1, r2);
		return crossover1(r1, r2);
	}

    public int[] crossover0(int[] r1, int[] r2) {
        int[] vec=r1.clone();
       	int pos = (int)(Math.random()*steps);
       	for (int i=pos*2; i<vec.length; i++) vec[i] = r2[i];
        return vec;
    }
    
    public int[] crossover1(int[] r1, int[] r2) {
    	ArrayList<Integer> im = findIllegalMove(r1);
    	int pos = (int)(Math.random()*im.size());
        int[] vec=r1.clone();
       	for (int i=pos*2; i<vec.length; i++) vec[i] = r2[i];
        return vec;
    }
    
    public int[] geneticOptimize() {
        int popsize = POP_SIZE;
        double elite = ELITE_RATIO;
        double mutprob = MUTATION_RATE;
        int[] bestsol=null;
        
        System.out.println("Making initial population.");
        ArrayList<int[]> pop = new ArrayList<int[]>();
        while (pop.size() < popsize) {
            int[] sol = new int[steps];
            // move code
            // - 7 - 0 -
            // 6 - - - 1
            // - - X - -
            // 5 - - - 2
            // - 4 - 3 -
            for (int j=0; j<sol.length; j++) sol[j] = (int)(Math.random()*MOVE_KIND);
            pop.add(sol);
        }

        int topelite=(int)(elite*popsize);
        System.out.println("Starting evolution.");
        int count=0;
        while (true) {
        	count++;
            ASObject[] array = new ASObject[pop.size()];
            for (int i=0; i<pop.size(); i++) {
                int[] r = (int[])pop.get(i);
                int cost = this.cost(r);
                array[i] = new ASObject(i, cost);
            }
            Arrays.sort(array);
            
            int[] sol = pop.get(array[0].label);
            int cost = this.cost(sol);
        	if (count%10 == 0) System.out.println("Optimization iteration "+count+", best cost="+cost);
            if (cost == this.steps) {
            	bestsol = sol;
            	break;
            }
            
            ArrayList<int[]> pop2 = new ArrayList<int[]>();
            for (int i=0; i<topelite; i++) {
                pop2.add(pop.get(array[i].label));
            }
            pop = pop2;

            while (pop.size() < popsize) {
                if (Math.random() < mutprob) {
                    int c = (int)(Math.random()*topelite);
                    pop.add(mutate((int[])pop.get(c)));
                }
                else {
                    int c1 = (int)(Math.random()*topelite);
                    int c2 = (int)(Math.random()*topelite);
                    pop.add(crossover((int[])pop.get(c1), (int[])pop.get(c2)));
                }
            }
            //System.out.println("Current best cost = "+
            //        ((ASObject)array[0]).score+" in generation "+count);
        }
        System.out.println("Done optimization in iteration "+count);

        return bestsol;
    }

    public class ASObject implements Comparable<ASObject> {
        public int label;
        public double score;

        public ASObject(int l, double s) {
            this.label = l;
            this.score = s;
        }

        public int compareTo(ASObject other) {
            if (this.score < other.score) return 1;
            else if (this.score > other.score) return -1;
            return 0;
        }
    }
}
