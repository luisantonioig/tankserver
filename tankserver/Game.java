package tankserver;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStreamReader;
import java.io.IOException;

public class Game {

    /**
     * A board has nine squares.  Each square is either unowned or
     * it is owned by a player.  So we use a simple array of player
     * references.  If null, the corresponding square is unowned,
     * otherwise the array cell stores a reference to the player that
     * owns it.
     */
    private Player[] board = {
        null, null, null,
        null, null, null,
        null, null, null};

    static int shots;
    /**
     * The current player.
     */
    Player currentPlayer;

    /**
     * Returns whether the current state of the board is such that one
     * of the players is a winner.
     */
    public boolean hasWinner() {
        return
            (board[0] != null && board[0] == board[1] && board[0] == board[2])
          ||(board[3] != null && board[3] == board[4] && board[3] == board[5])
          ||(board[6] != null && board[6] == board[7] && board[6] == board[8])
          ||(board[0] != null && board[0] == board[3] && board[0] == board[6])
          ||(board[1] != null && board[1] == board[4] && board[1] == board[7])
          ||(board[2] != null && board[2] == board[5] && board[2] == board[8])
          ||(board[0] != null && board[0] == board[4] && board[0] == board[8])
          ||(board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }

    /**
     * Returns whether there are no more empty squares.
     */
    public boolean boardFilledUp() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called by the player threads when a player tries to make a
     * move.  This method checks to see if the move is legal: that
     * is, the player requesting the move must be the current player
     * and the square in which she is trying to move must not already
     * be occupied.  If the move is legal the game state is updated
     * (the square is set and the next player becomes current) and
     * the other player is notified of the move so it can update its
     * client.
     */
    public synchronized boolean legalMove(int location, Player player) {
        if (player == currentPlayer && board[location] == null) {
            board[location] = currentPlayer;
            currentPlayer = currentPlayer.opponent;
            currentPlayer.otherPlayerMoved(location);
            return true;
        }
        return false;
    }

    /**
     * The class for the helper threads in this multithreaded server
     * application.  A Player is identified by a character mark
     * which is either 'X' or 'O'.  For communication with the
     * client the player has a socket with its input and output
     * streams.  Since only text is being communicated we use a
     * reader and a writer.
     */
    public class Player extends Thread {
        String positions;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
	int x;
	int y;

	Thread shot;
	List<Shot> shots = new CopyOnWriteArrayList<>();
	

        /**
         * Constructs a handler thread for a given socket and mark
         * initializes the stream fields, displays the first two
         * welcoming messages.
         */
        public Player(Socket socket, String positions, int shots) {
            this.socket = socket;
            this.positions = positions;
	    if (positions == "1"){
		this.x = 1;
		this.y = 1;
	    }else{
		this.x = 6;
		this.y = 1;
	    }
            try {
                input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME");
                output.println(positions);
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            }
        }

        /**
         * Accepts notification of who the opponent is.
         */
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        /**
         * Handles the otherPlayerMoved message.
         */
        public void otherPlayerMoved(int location) {
            output.println("OPPONENT_MOVED " + location);
            output.println(
                hasWinner() ? "DEFEAT" : boardFilledUp() ? "TIE" : "");
        }

        /**
         * The run method of this thread.
         */
        public void run() {
	    output.println("MESSAGE All players connected");
	    String cadena = "";
	    try{
		Shot s = null;
		while (true){
		    cadena = input.readLine();
		    if (cadena.contains("shot")){
			Game.shots++;
			String positions[] = cadena.split(" ");
			s = new Shot(Integer.parseInt(positions[3]),Integer.parseInt(positions[1]),Integer.parseInt(positions[2]), output, opponent.output, Game.shots, opponent);
			shot = new Thread(s);
			shot.start();
			output.println(cadena + " " + Game.shots);
			opponent.output.println(cadena + " " + Game.shots);
		    }
		    else{
			System.out.println("llegó " + cadena);
			opponent.output.println("OPPONENT_MOVE " + cadena);
			String pos[] = cadena.split(" ");
			this.x = Integer.parseInt(pos[1]);
			this.y = Integer.parseInt(pos[2]);
			//output.println(cadena);
		    }
		}
	    } catch (IOException e) {
		System.out.println("Player died: " + e);
	    }


        }
    }
}
