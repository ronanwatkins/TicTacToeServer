// Fig. 18.8: TicTacToeServer.java
// This class maintains a game of Tic-Tac-Toe for two client applets.
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;
import javax.swing.*;
import javax.xml.bind.SchemaOutputResolver;

public class TicTacToeServer extends JFrame {
    private char[] board;
    private JTextArea outputArea;
    private Player[] players;
    private ServerSocket server;
    private int currentPlayer;
    private boolean isNewGame = false, draw = false;
    private final int PLAYER_X = 0, PLAYER_O = 1;
    private final char X_MARK = 'X', O_MARK = 'O';
    private static final Logger LOGGER = Logger.getLogger( TicTacToeServer.class.getName() );

    // set up tic-tac-toe server and GUI that displays messages
    public TicTacToeServer(int port)
    {
        super( "Tic-Tac-Toe Server" );

        board = new char[ 9 ];
        players = new Player[ 2 ];
        currentPlayer = PLAYER_X;

        // set up ServerSocket
        try {
            server = new ServerSocket( port, 2 );
        }

        // process problems creating ServerSocket
        catch( IOException ioException ) {
            ioException.printStackTrace();
            System.exit( 1 );
        }

        // set up JTextArea to display messages during execution
        outputArea = new JTextArea();
        getContentPane().add( outputArea, BorderLayout.CENTER );
        outputArea.setText( "Server awaiting connections\n" );

        setSize( 300, 300 );
        setVisible( true );

    } // end TicTacToeServer constructor

    // wait for two connections so game can be played
    public void execute()
    {
        // wait for each client to connect
        for ( int i = 0; i < players.length; i++ ) {

            // wait for connection, create Player, start thread
            try {
                players[ i ] = new Player( server.accept(), i );
                players[ i ].start();
            }

            // process problems receiving connection from client
            catch( IOException ioException ) {
                ioException.printStackTrace();
                System.exit( 1 );
            }
        }

        // Player X is suspended until Player O connects.
        // Resume player X now.
        synchronized ( players[ PLAYER_X ] ) {
            players[ PLAYER_X ].setSuspended( false );
            players[ PLAYER_X ].notify();
        }

    }  // end method execute

    // utility method called from other threads to manipulate
    // outputArea in the event-dispatch thread
    private void displayMessage( final String messageToDisplay )
    {
        // display message from event-dispatch thread of execution
        SwingUtilities.invokeLater(
                new Runnable() {  // inner class to ensure GUI updates properly

                    public void run() // updates outputArea
                    {
                        outputArea.append( messageToDisplay );
                        outputArea.setCaretPosition(
                                outputArea.getText().length() );
                    }

                }  // end inner class

        ); // end call to SwingUtilities.invokeLater
    }

    // Determine if a move is valid. This method is synchronized because
    // only one move can be made at a time.
    public synchronized boolean validateAndMove( int location, int player )
    {
        // while not current player, must wait for turn
        System.out.println("waiting for player!=currentPlayer");
        while ( player != currentPlayer ) {

            // wait for turn
            try {
                wait();
            }

            // catch wait interruptions
            catch( InterruptedException interruptedException ) {
                interruptedException.printStackTrace();
            }
        }

        // if location not occupied, make move
        if ( !isOccupied( location ) ) {

            // set move in board array
            board[ location ] = currentPlayer == PLAYER_X ? X_MARK : O_MARK;

            // change current player
            currentPlayer = ( currentPlayer + 1 ) % 2;

            // let new current player know that move occurred
            players[ currentPlayer ].otherPlayerMoved( location );

            notify(); // tell waiting player to continue

            // tell player that made move that the move was valid
            return true;
        }

        // tell player that made move that the move was not valid
        else
            return false;

    } // end method validateAndMove

    // determine whether location is occupied
    public boolean isOccupied( int location )
    {
        if ( board[ location ] == X_MARK || board [ location ] == O_MARK )
            return true;
        else
            return false;
    }

    // place code in this method to determine whether game over
    public boolean isGameOver()
    {
        boolean result = false;
        if(     (board[0] == X_MARK && board[1] == X_MARK && board[2] == X_MARK) || //First across
                (board[3] == X_MARK && board[4] == X_MARK && board[5] == X_MARK) || //Middle across
                (board[6] == X_MARK && board[7] == X_MARK && board[8] == X_MARK) || //Bottom across
                (board[0] == X_MARK && board[3] == X_MARK && board[6] == X_MARK) || //Left down
                (board[1] == X_MARK && board[4] == X_MARK && board[7] == X_MARK) || //Middle down
                (board[2] == X_MARK && board[5] == X_MARK && board[8] == X_MARK) || //Right down
                (board[0] == X_MARK && board[4] == X_MARK && board[8] == X_MARK) || //Diagonal from top left
                (board[2] == X_MARK && board[4] == X_MARK && board[6] == X_MARK)) { //Diagonal from top right
            displayMessage("Game over");
            displayMessage("\nPlayer X is the winner");
            result = true;
        }
        else if((board[0] == O_MARK && board[1] == O_MARK && board[2] == O_MARK) || //First across
                (board[3] == O_MARK && board[4] == O_MARK && board[5] == O_MARK) || //Middle across
                (board[6] == O_MARK && board[7] == O_MARK && board[8] == O_MARK) || //Bottom across
                (board[0] == O_MARK && board[3] == O_MARK && board[6] == O_MARK) || //Left down
                (board[1] == O_MARK && board[4] == O_MARK && board[7] == O_MARK) || //Middle down
                (board[2] == O_MARK && board[5] == O_MARK && board[8] == O_MARK) || //Right down
                (board[0] == O_MARK && board[4] == O_MARK && board[8] == O_MARK) || //Diagonal from top left
                (board[2] == O_MARK && board[4] == O_MARK && board[6] == O_MARK)) { //Diagonal from top right
            displayMessage("Game over");
            displayMessage("\nPlayer O is the winner");
            result = true;
        }
        else if((board[0] != 0 && board[1] != 0 && board[2] != 0) && //First across
                (board[3] != 0 && board[4] != 0 && board[5] != 0) && //Middle across
                (board[6] != 0 && board[7] != 0 && board[8] != 0) && //Bottom across
                (board[0] != 0 && board[3] != 0 && board[6] != 0) && //Left down
                (board[1] != 0 && board[4] != 0 && board[7] != 0) && //Middle down
                (board[2] != 0 && board[5] != 0 && board[8] != 0) && //Right down
                (board[0] != 0 && board[4] != 0 && board[8] != 0) && //Diagonal from top left
                (board[2] != 0 && board[4] != 0 && board[6] != 0)) { //Diagonal from top right
            displayMessage("Game over");
            displayMessage("\nDraw match");
            draw = true;
            result = true;
        }
        return result;
    }

    public static void main( String args[] )
    {
        int portNumber = Integer.parseInt(args[0]);
        TicTacToeServer application = new TicTacToeServer(portNumber);
        application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        application.execute();
    }

    // private inner class Player manages each Player as a thread
    private class Player extends Thread {
        private Socket connection;
        private DataInputStream input;
        private DataOutputStream output;
        private int playerNumber;
        private char mark;
        protected boolean suspended = true;

        // set up Player thread
        public Player(Socket socket, int number) {
            playerNumber = number;

            // specify player's mark
            mark = (playerNumber == PLAYER_X ? X_MARK : O_MARK);

            connection = socket;

            // obtain streams from Socket
            try {
                input = new DataInputStream(connection.getInputStream());
                output = new DataOutputStream(connection.getOutputStream());
            }

            // process problems getting streams
            catch (IOException ioException) {
                ioException.printStackTrace();
                System.exit(1);
            }

        } // end Player constructor

        // send message that other player moved
        public void otherPlayerMoved(int location) {
            // send message indicating move
            try {
                if (!isGameOver()) {
                    output.writeUTF("Opponent moved");
                    output.writeInt(location);
                } else
                    output.writeUTF("Game over");
            }

            // process problems sending message
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        // control thread's execution
        public void run() {
            playGame();
        }

        public void playGame()
        {
            // send client message indicating its mark (X or O),
            // process messages from client
            try {
                displayMessage("Player " + (playerNumber ==
                        PLAYER_X ? X_MARK : O_MARK) + " connected\n");

                output.writeChar(mark); // send player's mark

                // send message indicating connection
                output.writeUTF( "Player " + ( playerNumber == PLAYER_X ?
                        "X connected\n" : "O connected, please wait\n" ) );

                // if player X, wait for another player to arrive
                if ( mark == X_MARK ) {
                    output.writeUTF( "Waiting for another player" );

                    // wait for player O
                    if(!isNewGame) {
                        try {
                            synchronized (this) {
                                while (suspended) {
                                    wait();
                                }
                            }
                        }

                        // process interruptions while waiting
                        catch (InterruptedException exception) {
                            exception.printStackTrace();
                        }

                        // send message that other player connected and
                        // player X can make a move
                    }
                    output.writeUTF( "Other player connected. Your move." );
                }

                // while game not over
                String message = "";
                while (!isGameOver()) {
                    // get move location from client
                    message = input.readUTF();
                    System.out.println("In !isGameOver " + message);
                    if(message.equalsIgnoreCase("new game"))
                        break;
                    else {
                        int location = Integer.parseInt(message.substring(2));

                        // check for valid move
                        if (validateAndMove(location, playerNumber)) {
                            displayMessage("\nPlayer: " + message.substring(0, 1) + " location: " + location);
                            System.out.println("Valid move.");
                            output.writeUTF("Valid move.");
                        } else
                            output.writeUTF("Invalid move, try again");
                    }
                }

                output.writeUTF("Game over");
                if (!draw)
                    output.writeUTF("Player " + (playerNumber == PLAYER_X ? X_MARK : O_MARK) + " is the winner!");
                else
                    output.writeUTF("Draw match");

                //String message = input.readUTF();
                System.out.println("Out of !isGameOver " + message);

                if (message.equalsIgnoreCase("new game")) {
                    output.writeUTF("new game");
                    Arrays.fill(board, '\u0000');
                    draw = false;
                    currentPlayer = ( currentPlayer + 1 ) % 2;
                    System.out.println("starting new game");
                    playGame();
                }
            } // end try

            // process problems communicating with client
            catch( IOException ioException ) {
                ioException.printStackTrace();
                System.exit( 1 );
            }

        } // end method run

        // set whether or not thread is suspended
        public void setSuspended( boolean status )
        {
            suspended = status;
        }

    } // end class Player

} // end class TicTacToeServer