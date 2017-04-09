package chap5;

import java.util.ArrayList;
import java.util.Arrays;

import chap5.Optimization.ASObject;

public class NQueens {
	public static int POP_SIZE=30000;
	private int boardSize;

	public static void main(String[] args) {
		NQueens nq = new NQueens(10);
		nq.go();
	}
	
	public NQueens(int i) {
		this.boardSize = i;
	}
	
	public void go() {
		int[] sol = geneticOptimize();
		//int cost = this.cost(sol);
		printSolution(sol);
	}
	
	public void printSolution(int[] sol) {
		int[][] board = new int[this.boardSize][this.boardSize];
		for (int i=0; i<this.boardSize; i++)
			board[sol[2*i]][sol[2*i+1]] = 1;
		for (int i=0; i<this.boardSize; i++) {
			for (int j=0; j<this.boardSize; j++) {
				if (board[i][j] == 1) System.out.print("Q ");
				else System.out.print("- ");
			}
			System.out.println();
		}
	}
	
	public int cost(int[] sol) {
		int cleanQueen=0;
		for (int i=0; i<sol.length; i+=2) {
			int[] tmp=sol.clone();
			tmp[i] = -1;
			tmp[i+1] = -1;
			if (isCleanQueen(sol[i], sol[i+1], tmp))
				cleanQueen++;
			
			// aggressive version
			//if (!isCleanQueen(sol[i], sol[i+1], tmp)) return -1;
		}
		return cleanQueen;
		//return this.boardSize;
	}
	
	private boolean isCleanQueen(int row, int col, int[] sol) {
		for (int i=0; i<sol.length; i+=2) {
			// same row or column
			if ((sol[i]==row) || (sol[i+1]==col)) return false;
			// diagnal collision
			if ((sol[i]-row) == (sol[i+1]-col)) return false;
			if ((row-sol[i]) == (sol[i+1]-col)) return false;
		}
		return true;
	}
	
    public int[] mutate(int[] r) {
        int step = 1;
        int[] vec=r.clone();
       	// decide which queen to move
       	int which = (int)(Math.random()*this.boardSize);
       	if (Math.random()<0.5 && vec[2*which]>=step) // move left
       		vec[2*which] -= step;
       	else if (vec[2*which] < boardSize-step)
       		vec[2*which] += step;
        	
       	if (Math.random()<0.5 && vec[2*which+1]>=step) // move up
       		vec[2*which+1] -= step;
       	else if (vec[2*which+1] < boardSize-step)
       		vec[2*which+1] += step;
        	
        return vec;
    }

    public int[] crossover(int[] r1, int[] r2) {
        int[] vec=r1.clone();
       	int pos = (int)(Math.random()*this.boardSize);
       	for (int i=pos*2; i<vec.length; i++) vec[i] = r2[i];
        return vec;
    }
    
    public boolean isSolution(int[] sol) {
    	for (int i=0; i<this.boardSize; i++) {
    		for (int j=i+1; j<this.boardSize; j++) {
    			if (sol[2*i]==sol[2*j] && sol[2*i+1]==sol[2*j+1])
    				return false;
    		}
    	}
    	return true;
    }

    public int[] geneticOptimize() {
        int popsize = POP_SIZE;
        double elite = 0.2;
        double mutprob = 0.2;
        int[] bestsol=null;

        ArrayList<int[]> pop = new ArrayList<int[]>();
        while (pop.size() < popsize) {
            int[] sol = new int[this.boardSize*2];
            for (int j=0; j<sol.length; j++) {
                sol[j] = (int)(Math.random()*boardSize);
            }
            pop.add(sol);
        }

        int topelite=(int)(elite*popsize);

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
        	if (count%1000 == 0) System.out.println("Optimization iteration "+count+", best cost="+cost);
            if (cost == this.boardSize) {
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
