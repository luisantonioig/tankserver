package tankserver;

import java.io.PrintWriter;
import tankserver.Game;

public class Shot implements Runnable {


    public int x;
    public int y;
    Game.Player opponente;

    public int direction;
    PrintWriter output;
    PrintWriter opponent_output;
    int shots;

    int opponent_x;
    int opponent_y;

    public Shot(int direction,int x,int y, PrintWriter output, PrintWriter opp_out, int shots,  Game.Player opp){
	this.shots = shots;
	this.x = x;
	this.y = y;
	this.direction = direction;
	this.output = output;
	this.opponent_output = opp_out;
	this.opponente = opp; 
    }

    public void run() {
	while(x>-1 && x<28 && y>-1 && y<28){
	    try{
	        switch(direction){
		case 1: y--; break;
		case 2: x++; break;
		case 3: y++; break;
		case 4: x--;
		}
		Thread.sleep(150);
	    }
	    catch(InterruptedException IE){
	    }
	    System.out.println("Disparo (" + x + ", " + y + ")" );
	    System.out.println("Oponente (" + opponente.x + ", " + opponente.y + ")" );
	    if ((x >= opponente.x - 1 && x <= opponente.x +1)&&
		(y >= opponente.y - 1 && y <= opponente.y +1)){
		System.out.println("Disparo acertado!!");
		x = -1;
		y = -1;
		output.println("OPPONENT_DIED");
		opponent_output.println("YOU_DIED");
	    }
	    else{
		output.println("disparo " + x +  " " + y + " " + direction + " " + shots);
		opponent_output.println("disparo " + x +  " " + y + " " + direction + " " + shots);
	    }
	}
	output.println("remove_shot " + shots);
	opponent_output.println("remove_shot " + shots);
    }

}
