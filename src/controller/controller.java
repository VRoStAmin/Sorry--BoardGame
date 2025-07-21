package controller;
import model.Cards.*;
import model.Squares.Slide.StartSlideSquare;
import model.Squares.Square;
import view.*;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller, controls the games flow and updates everything to the
 * state of the game.
 * @version 1.0
 * @author Mastorakhs Emmanouil AM csd5255
 */
public class controller {
    private Menu menuView;
    private SorryGraphics gameView;
    private Board board;
    private Player[] players;
    private Deck cards;
    private Card currentCard;
    private int currentPlayer;
    boolean canClickReceive;
    boolean receivesCardAgain;
    boolean canClickFold;
    boolean canChosePawn;
    boolean canChoseYellow;
    boolean canChoseRed;
    boolean canChosePawn1;
    boolean canChosePawn2;

    /**
     * Initialises everything so that the game can start.
     */
    public controller(SorryGraphics gameView, int numberOfPlayers) {
        this.gameView = gameView;
        cards = new Deck();
        this.gameView = gameView;
        players = new Player[numberOfPlayers];
        currentCard = null;

        canChosePawn2 = true;
        canChosePawn1 = true;

        for(int i=0; i<numberOfPlayers; i++) {
            Color playerColor;
            switch(i){
                case 0:
                    playerColor = Color.YELLOW;
                    break;
                case 1:
                    playerColor = Color.RED;
                    break;
                case 2:
                    playerColor = Color.BLUE;
                    break;
                case 3:
                    playerColor = Color.GREEN;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid number of players. Must be between 1 and 4.");
            }
            String name = playerColor.name() + " Player";
            players[i] = new Player(name, playerColor);
        }
        board = new Board(players);
        canClickFold = false;
        canChosePawn = false;
        receivesCardAgain = false;
        currentPlayer = 0;

        String currentPlayerName = players[currentPlayer].getName();
        startTurn();
        gameView.updateTextBox(String.valueOf(cards.getCardsLeft()), currentPlayerName, "Click the Receive card button.");
        gameView.updateBoard();

    }

    /**
     * Action for when the receive card button is clicked.
     * Receives a new card from the deck and updates the graphics accordingly.
     * @pre The player has not already received a card this turn.
     * @post A new card is drawn and the turn of the player continues accordingly.
     */
    public void handleReceiveCardBt() {
        if(!canClickReceive) {
            gameView.showErrorMessage("Player cannot receive a card again.");
            gameView.updateBoard();
            return;
        }
        canClickReceive = false;
        currentCard = cards.drawCard();
        if(currentCard == null){
            cards.redoDeck();
            currentCard = cards.drawCard();
        }
        String currentPlayerName = players[currentPlayer].getName();
        gameView.updateTextBox(String.valueOf(cards.getCardsLeft()), currentPlayerName, currentCard.getDescription());
        gameView.updateCurrentCardDisplay(currentCard);
        gameView.updateBoard();
    }


    /**
     * Action for when the current card button is received.
     * This method checks for the card that was drawn and executes the card based on what it does.
     * @pre A card must be drawn.
     * @post The game state gets updated based on what the card drawn is.
     */
    public void handleCurrentCardBt() {
        Player[] players_board = board.getPlayers();
        Square[][] squares = board.getBoard();
        if(currentCard == null){
            gameView.showErrorMessage("You have to draw a card first.");
            gameView.updateBoard();
            return;
        }
        if(players_board[currentPlayer].getPawn1().isAtStart() && players_board[currentPlayer].getPawn2().isAtStart()) {
            if (currentCard instanceof NumberCard) {
                int cardNumber = ((NumberCard) currentCard).getNumber();
                if (cardNumber == 1 || cardNumber == 2) {
                    gameView.showErrorMessage("You have drawn 1 or 2.\nChose one of your pawns.");
                    pawnSelection();
                } else {
                    gameView.showErrorMessage("You have not drawn 1 or 2. Press fold.");
                    canClickFold = true;

                }
            } else if (currentCard instanceof SorryCard){
                int otherPlayer = 0;
                if(currentPlayer == 0) otherPlayer = 1;
                if(players[otherPlayer].getPawn1().isAtStart() && players[otherPlayer].getPawn2().isAtStart()){
                    gameView.showErrorMessage("You cannot swap with anyone. Press fold.");
                    canClickFold = true;
                }else{
                    gameView.showErrorMessage("Select a pawn that you would like to swap.");
                    pawnSelection();
                }
            }
            gameView.updateBoard();
        }else {
            if (currentCard instanceof NumberCard) {
                int cardNumber = ((NumberCard) currentCard).getNumber();
                if (cardNumber == 1 || cardNumber == 2 || cardNumber == 4) {
                    gameView.showErrorMessage("Chose one of your pawns.");
                    canChosePawn1 = true;
                    canChosePawn2 = true;
                    pawnSelection();
                }else if(cardNumber == 3 || cardNumber == 5) {
                    int[] coordinates = currentCard.execute(players[currentPlayer], board, players[currentPlayer].getPawn1());
                    Color color = players[currentPlayer].getPlayerColor();
                    if(coordinates.length == 4){
                        int[] coordinates1 = {coordinates[0], coordinates[1]};
                        int[] coordinates2 = {coordinates[2], coordinates[3]};

                        gameView.placePawn(coordinates1, 1, color);
                        gameView.placePawn(coordinates2, 2, color);
                        slidePawn(squares[coordinates1[0]][coordinates1[1]]);
                        slidePawn(squares[coordinates2[0]][coordinates2[1]]);
                        endTurn();
                    } else if(coordinates.length == 1) {
                        gameView.showErrorMessage("Card cannot be played. Click fold.");
                        canClickFold = true;
                    } else if(coordinates.length == 3) {
                        int[] coordinates_id = {coordinates[0], coordinates[1]};
                        int id = coordinates[2];
                        gameView.placePawn(coordinates_id, id, color);
                        slidePawn(squares[coordinates[0]][coordinates[1]]);
                        endTurn();
                    } else if(coordinates.length == 5) {
                        int[] coordinates_id = {coordinates[0], coordinates[1]};
                        int id = coordinates[4];
                        gameView.placePawn(coordinates_id, id, color);
                        slidePawn(squares[coordinates_id[0]][coordinates_id[1]]);

                        int[] coordinates2 = {coordinates[2], coordinates[3]};
                        if(coordinates2[1] == 3){
                            gameView.placePawn(coordinates2, 1, Color.RED);
                        }else if(coordinates2[1] == 4) {
                            gameView.placePawn(coordinates2, 2, Color.RED);
                        }else if(coordinates2[1] == 11) {
                            gameView.placePawn(coordinates2, 1, Color.YELLOW);
                        }else {
                            gameView.placePawn(coordinates2, 2, Color.YELLOW);
                        }
                        slidePawn(squares[coordinates2[0]][coordinates2[1]]);
                        endTurn();
                    } else if(coordinates.length == 6) {
                        int[] coordinates1 = {coordinates[0], coordinates[1]};
                        int[] coordinates2 = {coordinates[2], coordinates[3]};

                        gameView.placePawn(coordinates1, 1, color);
                        gameView.placePawn(coordinates2, 2, color);

                        slidePawn(squares[coordinates1[0]][coordinates1[1]]);
                        slidePawn(squares[coordinates2[0]][coordinates2[1]]);

                        int[] coordinates3 = {coordinates[4], coordinates[5]};
                        if(coordinates3[1] == 3){
                            gameView.placePawn(coordinates3, 1, Color.RED);
                        }else if(coordinates3[1] == 4) {
                            gameView.placePawn(coordinates3, 2, Color.RED);
                        }else if(coordinates3[1] == 11) {
                            gameView.placePawn(coordinates3, 1, Color.YELLOW);
                        }else {
                            gameView.placePawn(coordinates3, 2, Color.YELLOW);
                        }
                        slidePawn(squares[coordinates3[0]][coordinates3[1]]);
                        endTurn();
                    } else{
                        int[] coordinates_id = {coordinates[0], coordinates[1]};
                        int id = coordinates[2];
                        gameView.placePawn(coordinates_id, id, color);
                        slidePawn(squares[coordinates_id[0]][coordinates_id[1]]);
                        endTurn();
                    }
                }else if(cardNumber == 12 || cardNumber == 8) {
                    Pawn pawn1 = players_board[currentPlayer].getPawn1();
                    Pawn pawn2 = players_board[currentPlayer].getPawn2();
                    boolean canMove1 = false;
                    boolean canMove2 = false;
                    receivesCardAgain = true;
                    if(cardNumber == 12){
                        if(((SimpleNumberCard)currentCard).canMove(12, pawn1, board)){
                            canMove1 = true;
                        }
                        if(((SimpleNumberCard)currentCard).canMove(12, pawn2, board)){
                            canMove2 = true;
                        }
                    }else{
                        if(((SimpleNumberCard)currentCard).canMove(8, pawn1, board)){
                            canMove1 = true;
                        }
                        if(((SimpleNumberCard)currentCard).canMove(8, pawn2, board)){
                            canMove2 = true;
                        }
                    }
                    if(canMove1 && canMove2) {
                        gameView.showErrorMessage("Pick one Pawn to move " + cardNumber + " spaces, or draw a card.");
                        canChosePawn1 = true;
                        canChosePawn2 = true;
                        pawnSelection();
                    }else if(canMove1) {
                        gameView.showErrorMessage("Click Pawn1 to move it " + cardNumber + " spaces, or draw a card.");
                        canChosePawn1 = true;
                        canChosePawn2 = false;
                        pawnSelection();
                    }else if(canMove2) {
                        gameView.showErrorMessage("Click Pawn2 to move it " + cardNumber + " spaces, or draw a card.");
                        canChosePawn1 = false;
                        canChosePawn2 = true;
                        pawnSelection();
                    }else {
                        canChosePawn1 = false;
                        canChosePawn2 = false;
                        gameView.showErrorMessage("You cannot move any pawns, draw a card.");
                        pawnSelection();
                    }
                    if(receivesCardAgain) {
                        canClickReceive = true;
                        canClickFold = false;
                        receivesCardAgain = false;
                    }
                }else if(cardNumber == 7) {
                    handleCard7();
                }else if(cardNumber == 10) {
                    handleCard10();
                }else if(cardNumber == 11) {
                    boolean one = false;
                    List<Integer> optionalMoves = new ArrayList<>();
                    List<String> options = new ArrayList<>();

                    Pawn pawn1 = players[currentPlayer].getPawn1();
                    Pawn pawn2 = players[currentPlayer].getPawn2();

                    int otherPlayer = 1;
                    if(currentPlayer == 1) otherPlayer = 0;

                    Pawn otherPawn1 = players[otherPlayer].getPawn1();
                    Pawn otherPawn2 = players[otherPlayer].getPawn2();


                    boolean canMove1 = false;
                    boolean canMove2 = false;

                    boolean canBeSwapped1 = true;
                    boolean canBeSwapped2 = true;
                    boolean swapPawn1 = true;
                    boolean swapPawn2 = true;

                    if(((NumberElevenCard)currentCard).canMove(11, pawn1, board)){
                        canMove1 = true;
                    }
                    if(((NumberElevenCard)currentCard).canMove(11, pawn2, board)){
                        canMove2 = true;
                    }

                    if(otherPawn1.isAtStart() || otherPawn1.isAtHome() || otherPawn1.isInSafeZone()) canBeSwapped1 = false;
                    if(otherPawn2.isAtHome() || otherPawn2.isInSafeZone() || otherPawn2.isAtStart()) canBeSwapped2 = false;
                    if(pawn1.isAtHome() || pawn1.isAtStart() || pawn1.isInSafeZone()) swapPawn1 = false;
                    if(pawn2.isAtHome() || pawn2.isAtStart() || pawn2.isInSafeZone()) swapPawn2 = false;

                    if(canMove1 || canMove2) {
                        options.add("Move Pawn");
                        one = true;
                    }else options.add("NULL");

                    if((canBeSwapped1 || canBeSwapped2) && (swapPawn1 || swapPawn2)) {
                        options.add("Swap pawn");
                        one = true;
                    }else options.add("NULL");

                    options.add("Fold");

                    if(!one) {
                        gameView.showErrorMessage("You cannot swap or move any pawns.\nPress fold.");
                        gameView.updateBoard();
                        canClickFold = true;
                    }else{
                        String[] opts = new String[3];
                        for(int i = 0; i<3; i++) {
                            String option = options.get(i);
                            opts[i] = option;
                        }
                        int choice = gameView.showOptions("Choose what you would like to do.", "Move pawn or Swap", opts);
                        while(choice == -1) {
                            gameView.showErrorMessage("You must select an option.");
                            choice = gameView.showOptions("Choose what you would like to do.", "Move pawn or Swap", opts);
                            gameView.updateBoard();
                        }

                        if(choice == 0 && !Objects.equals(options.get(0), "NULL")) {
                            String[] pawn = new String[2];
                            if(canMove1) {
                                optionalMoves.add(1);
                            } else optionalMoves.add(-1);

                            if(canMove2) {
                                optionalMoves.add(2);
                            } else optionalMoves.add(-1);

                            for(int i = 0; i<2; i++) {
                                String p = String.valueOf(optionalMoves.get(i));
                                pawn[i] = p;
                            }

                            int select = gameView.showOptions("Choose a pawn", "Move pawn 11 spaces", pawn);
                            while((select == -1) || (select == 0 && Objects.equals(pawn[0], "-1")) || (select == 1 && Objects.equals(pawn[1], "-1"))) {
                                select = gameView.showOptions("Choose a pawn", "Move pawn 11 spaces", pawn);
                                gameView.showErrorMessage("Choose a valid pawn");
                                gameView.updateBoard();
                            }

                            if(select == 0){
                                int[] coordinates = ((NumberElevenCard) currentCard).executeCard11(11, pawn1, board);
                                if(coordinates.length == 5) {
                                    int[] start = new int[]{coordinates[2], coordinates[3]};
                                    int[] coordinatesPawn = new int[]{coordinates[0], coordinates[1]};
                                    int otherId = coordinates[4];

                                    Color color = Color.RED;
                                    if(players[currentPlayer].getPlayerColor() == Color.RED) color = Color.YELLOW;

                                    gameView.placePawn(start, otherId, color);
                                    gameView.placePawn(coordinatesPawn, 1, players[currentPlayer].getPlayerColor());
                                    slidePawn(squares[coordinatesPawn[0]][coordinatesPawn[1]]);
                                    gameView.updateBoard();
                                }else {
                                    int[] coordinatesPawn = new int[]{coordinates[0], coordinates[1]};
                                    gameView.placePawn(coordinatesPawn, 1, players[currentPlayer].getPlayerColor());
                                    slidePawn(squares[coordinatesPawn[0]][coordinatesPawn[1]]);
                                    gameView.updateBoard();
                                }
                                endTurn();
                            }else if(select == 1) {
                                int[] coordinates = ((NumberElevenCard) currentCard).executeCard11(11, pawn2, board);
                                if(coordinates.length == 5) {
                                    int[] start = new int[]{coordinates[2], coordinates[3]};
                                    int[] coordinatesPawn = new int[]{coordinates[0], coordinates[1]};
                                    int otherId = coordinates[4];

                                    Color color = Color.RED;
                                    if(players[currentPlayer].getPlayerColor() == Color.RED) color = Color.YELLOW;

                                    gameView.placePawn(start, otherId, color);
                                    gameView.placePawn(coordinatesPawn, 2, players[currentPlayer].getPlayerColor());
                                    slidePawn(squares[coordinatesPawn[0]][coordinatesPawn[1]]);
                                    gameView.updateBoard();
                                }else {
                                    int[] coordinatesPawn = new int[]{coordinates[0], coordinates[1]};
                                    gameView.placePawn(coordinatesPawn, 2, players[currentPlayer].getPlayerColor());
                                    slidePawn(squares[coordinatesPawn[0]][coordinatesPawn[1]]);
                                    gameView.updateBoard();
                                }
                                endTurn();
                            }

                        }else if(choice == 1 && !Objects.equals(options.get(1), "NULL")) {
                            List<int[]> optionalSwaps = new ArrayList<>();

                            if(canBeSwapped1 && swapPawn1) {
                                optionalSwaps.add(new int[]{1, 1});
                            }else{
                                optionalSwaps.add(new int[]{-1, -1});
                            }

                            if(canBeSwapped2 && swapPawn1) {
                                optionalSwaps.add(new int[]{1, 2});
                            }else{
                                optionalSwaps.add(new int[]{-1, -1});
                            }

                            if(canBeSwapped1 && swapPawn2) {
                                optionalSwaps.add(new int[]{2, 1});
                            }else{
                                optionalSwaps.add(new int[]{-1, -1});
                            }

                            if(canBeSwapped2 && swapPawn2) {
                                optionalSwaps.add(new int[]{2, 2});
                            }else{
                                optionalSwaps.add(new int[]{-1, -1});
                            }

                            String[] swaps = new String[4];
                            for(int i = 0; i<4; i++) {
                                int[] optionSwap = optionalSwaps.get(i);
                                swaps[i] = optionSwap[0] + "-" + optionSwap[1];
                            }
                            int choiceSwap = gameView.showOptions("Choose how you would like to swap pawns.", "Swap pawns", swaps);

                            while(choiceSwap == -1 || optionalSwaps.get(choiceSwap)[0] == -1) {
                                gameView.showErrorMessage("You must select an option.");
                                gameView.updateBoard();
                                choiceSwap = gameView.showOptions("Choose how you would like to swap pawns.", "Swap pawns", swaps);
                            }

                            if(choiceSwap == 0 && optionalSwaps.get(0)[0] != -1) {
                                int[] coordinatesPawn1 = pawn1.getPositionXY();
                                gameView.placePawn(coordinatesPawn1, 1, otherPawn1.getColor());
                                int[] coordinates = ((NumberElevenCard)currentCard).executeSwapCard(pawn1, otherPawn1, board);
                                gameView.placePawn(coordinates, 1, pawn1.getColor());
                                slidePawn(squares[pawn1.getPositionX()][pawn1.getPositionY()]);
                                slidePawn(squares[coordinatesPawn1[0]][coordinatesPawn1[1]]);


                            }else if(choiceSwap == 1 && optionalSwaps.get(1)[0] != -1) {
                                int[] coordinatesPawn1 = pawn1.getPositionXY();
                                gameView.placePawn(coordinatesPawn1, 2, otherPawn2.getColor());
                                int[] coordinates = ((NumberElevenCard)currentCard).executeSwapCard(pawn1, otherPawn2, board);
                                gameView.placePawn(coordinates, 1, pawn1.getColor());
                                slidePawn(squares[pawn1.getPositionX()][pawn1.getPositionY()]);
                                slidePawn(squares[coordinatesPawn1[0]][coordinatesPawn1[1]]);

                            }else if(choiceSwap == 2 && optionalSwaps.get(2)[0] != -1) {
                                int[] coordinatesPawn2 = pawn2.getPositionXY();
                                gameView.placePawn(coordinatesPawn2, 1, otherPawn1.getColor());
                                int[] coordinates = ((NumberElevenCard)currentCard).executeSwapCard(pawn2, otherPawn1, board);
                                gameView.placePawn(coordinates, 2, pawn2.getColor());
                                slidePawn(squares[pawn2.getPositionX()][pawn2.getPositionY()]);
                                slidePawn(squares[coordinatesPawn2[0]][coordinatesPawn2[1]]);

                            }else if(choiceSwap == 3 && optionalSwaps.get(3)[0] != -1) {
                                int[] coordinatesPawn2 = pawn2.getPositionXY();
                                gameView.placePawn(coordinatesPawn2, 2, otherPawn2.getColor());
                                int[] coordinates = ((NumberElevenCard)currentCard).executeSwapCard(pawn2, otherPawn2, board);
                                gameView.placePawn(coordinates, 2, pawn2.getColor());
                                slidePawn(squares[pawn2.getPositionX()][pawn2.getPositionY()]);
                                slidePawn(squares[coordinatesPawn2[0]][coordinatesPawn2[1]]);
                            }
                            gameView.updateBoard();
                            endTurn();
                        }else if(choice == 2 && Objects.equals(options.get(0), "NULL")) {
                            endTurn();
                        }else if(choice == 2 && !Objects.equals(options.get(0), "NULL")) {
                            gameView.showErrorMessage("You must move a pawn.");
                            gameView.updateBoard();
                            handleCurrentCardBt();
                        } else{
                            gameView.showErrorMessage("You must select a valid option.");
                            gameView.updateBoard();
                            handleCurrentCardBt();
                        }
                    }
                }
            } else if(currentCard instanceof SorryCard) {
                if(!players[currentPlayer].getPawn1().isAtStart() && !players[currentPlayer].getPawn2().isAtStart()){
                    gameView.showErrorMessage("None of your pawns is at the start. Press fold.");
                    canClickFold = true;
                }else{
                    int otherPlayer = 0;
                    if(currentPlayer == 0) otherPlayer = 1;
                    if(players[otherPlayer].getPawn1().isAtStart() && players[otherPlayer].getPawn2().isAtStart()){
                        gameView.showErrorMessage("You cannot swap with anyone. Press fold.");
                        canClickFold = true;
                    }else{
                        gameView.showErrorMessage("Select a pawn that you would like to swap.");
                        pawnSelection();
                    }
                }
            }
            gameView.updateBoard();
        }
    }

    /**
     * Action for when the fold button is pressed.
     * Ends the player's turn if they cannot play the card that was drawn.
     * @pre a card must be drawn.
     * @post The player's turn ends and the next player's turn starts.
     */
    public void handleFoldButton() {
        if(canClickFold) {
            endTurn();
        }else {
            gameView.showErrorMessage("You must play your card.");
        }
        gameView.updateBoard();
    }
    /**
     * Draws a new card from the deck
     * @pre The deck must not be empty.
     * @pre The player has yet to receive a card.
     * @post The card gets received and the player then continues their turn.
     */
    public void newCard() {}

    /**
     * Moves a pawn somewhere in the board.
     * @pre The player must have received a card first telling him that he is able to move a pawn.
     * @pre The player must follow the received card's rules.
     * @post The pawn moves to the chosen square.
     */
    public void movePawn() {}

    /**
     * Moves more than one pawn.
     * @pre The player must have received a card letting them move more than 1 pawn.
     * @pre The player must follow the received card's rules.
     * @pre The player must not choose the same square for both pawns even if possible.
     * @pre A square must be highlighted.
     * @post The pawns move to the chosen squares.
     */
    public void movePawns() {}

    /**
     * Ends the current players turn, and the next player's turn starts.
     * @pre The player must have played their move based on the rules of the card.
     * @post The player's turn ends.
     */
    public void endTurn() {
        if (players[currentPlayer].getPawn1().isAtHome() && players[currentPlayer].getPawn2().isAtHome()) {
            gameView.updateTextBox(String.valueOf(cards.getCardsLeft()), players[currentPlayer].getName(), "Player " + players[currentPlayer].getName() + " won!!!");
            gameView.showErrorMessage("");
            gameView.updateBoard();
            canChosePawn = false;
            canClickReceive = false;
            canClickFold = false;
            currentCard = null;
        }else {
            currentPlayer++;
            if (currentPlayer == 2) {
                currentPlayer = 0;
            }
            canChosePawn = false;
            canChosePawn1 = true;
            canChosePawn2 = true;
            canClickReceive = true;
            canClickFold = false;
            currentCard = null;

            String currentPlayerName = players[currentPlayer].getName();
            startTurn();
            gameView.updateTextBox(String.valueOf(cards.getCardsLeft()), currentPlayerName, "Click the Receive card button.");
            gameView.showErrorMessage("");
            gameView.updateBoard();
        }
    }

    /**
     * Enables the player to select a pawn of their color.
     * @pre the player must have drawn a card that requires them to move a pawn.
     * @post allows pawn selection.
     */
    public void pawnSelection() {
        canChosePawn = true;
        canChoseRed = false;
        canChoseYellow = false;
        if(board.getPlayers()[currentPlayer].getPlayerColor() == Color.RED) {
            canChoseRed = true;
        }else {
            canChoseYellow = true;
        }
    }

    /**
     * Action for the press of a pawn. If a pawn is pressed the function gets called
     * and based of the parameters it has, it executes the movement based on the card that is drawn.
     * @param color the color of the pawn.
     * @param id the id of the pawn (1 for pawn1 and 2 for pawn2).
     * @pre The pawnSelection method must be called first. Letting the player pick a pawn.
     * @post Moves the pawn selected based on card that is drawn.
     */
    public void handlePawnChosen(model.Color color, int id) {
        Square[][] squares = board.getBoard();
        boolean playAgain = false;
        Pawn pawnChosen = null;
        if(canChosePawn) {
            if(players[currentPlayer].getPlayerColor() != color) {
                gameView.showErrorMessage("Choose a pawn of your color.");
                gameView.updateBoard();
            }else {
                if(!canChosePawn1 && !canChosePawn2 && !(currentCard instanceof SorryCard)){
                    gameView.showErrorMessage("None of your pawns can make this move.");
                    gameView.updateBoard();
                }else if(!canChosePawn1 && id == 1 && !(currentCard instanceof SorryCard)) {
                    gameView.showErrorMessage("This pawn cannot make this move.");
                    gameView.updateBoard();
                }else if(!canChosePawn2 && id == 2 && !(currentCard instanceof SorryCard)) {
                    gameView.showErrorMessage("This pawn cannot make this move.");
                    gameView.updateBoard();
                }else if(id == 1) {
                    pawnChosen = players[currentPlayer].getPawn1();
                }else {
                    pawnChosen = players[currentPlayer].getPawn2();
                }

                if(pawnChosen != null){
                    if(currentCard instanceof NumberCard) {
                        int[] coordinates = currentCard.execute(players[currentPlayer], board, pawnChosen);
                        if (coordinates[0] == -3) {
                            gameView.showErrorMessage("Pawn chosen is at the start or at home. \nChoose another one.");
                            playAgain = true;
                            gameView.updateBoard();
                        } else if (coordinates[0] == -1) {
                            gameView.showErrorMessage("One of your pawns is already in that square.\nChose that one.");
                            playAgain = true;
                            gameView.updateBoard();
                        } else if (coordinates[0] == -2) {
                            gameView.showErrorMessage("One enemy pawn is already in that square.\nPress fold.");
                            gameView.updateBoard();
                            canClickFold = true;
                        } else if(coordinates[0] == -4) {
                            gameView.showErrorMessage("The pawn you chose has already finished its run. \nChoose another one.");
                            playAgain = true;
                            gameView.updateBoard();
                        } else if(coordinates[0] == -5) {
                            gameView.showErrorMessage("Too many squares for that pawn. \nChoose another one.");
                            playAgain = true;
                            gameView.updateBoard();
                        } else if(coordinates[0] == -6) {
                            gameView.showErrorMessage("The pawn cannot make this move. Click fold.");
                            canClickFold = true;
                            gameView.updateBoard();
                        }else {
                            if (currentCard instanceof NumberTwoCard) {
                                receivesCardAgain = true;
                            }
                            if (currentCard instanceof SimpleNumberCard && ((SimpleNumberCard) currentCard).getNumber() == 12) {
                                receivesCardAgain = false;
                            }
                            if(coordinates.length == 4){
                                int[] coordinates1 = {coordinates[0], coordinates[1]};
                                int[] coordinates2 = {coordinates[2], coordinates[3]};
                                if(coordinates2[1] == 3){
                                    gameView.placePawn(coordinates2, 1, Color.RED);
                                }else if(coordinates2[1] == 4) {
                                    gameView.placePawn(coordinates2, 2, Color.RED);
                                }else if(coordinates2[1] == 11) {
                                    gameView.placePawn(coordinates2, 1, Color.YELLOW);
                                }else {
                                    gameView.placePawn(coordinates2, 2, Color.YELLOW);
                                }
                                slidePawn(squares[coordinates2[0]][coordinates2[1]]);
                                gameView.placePawn(coordinates1, id, color);
                                slidePawn(squares[coordinates1[0]][coordinates1[1]]);
                            }else {
                                gameView.placePawn(coordinates, id, color);
                                slidePawn(squares[coordinates[0]][coordinates[1]]);
                            }
                        }
                        if (receivesCardAgain) {
                            if(players[currentPlayer].getPawn1().isAtHome() && players[currentPlayer].getPawn2().isAtHome()) endTurn();
                            canClickReceive = true;
                            currentCard = null;
                            canClickFold = false;
                            receivesCardAgain = false;
                        } else if (!playAgain) {
                            canChosePawn2 = true;
                            canChosePawn1 = true;
                            canChosePawn = false;
                            canChoseRed = false;
                            canChoseYellow = false;
                            endTurn();
                        }
                    }else if(currentCard instanceof SorryCard) {
                        Pawn currentPawn;
                        if(id == 1) {
                            currentPawn = players[currentPlayer].getPawn1();
                        }else {
                            currentPawn = players[currentPlayer].getPawn2();
                        }
                        if(!currentPawn.isAtStart()){
                            gameView.showErrorMessage("Pawn is not at the start choose another one.");
                            playAgain = true;
                            gameView.updateBoard();
                        }else {
                            int otherPlayer = 0;
                            if(currentPlayer == 0) otherPlayer = 1;
                            handleCardSorry(currentPawn, players[currentPlayer], players[otherPlayer], id);
                        }
                        if (receivesCardAgain) {
                            canClickReceive = true;
                            currentCard = null;
                            canClickFold = false;
                            receivesCardAgain = false;
                        } else if (!playAgain) {
                            canChosePawn2 = true;
                            canChosePawn1 = true;
                            canChosePawn = false;
                            canChoseRed = false;
                            canChoseYellow = false;
                            endTurn();
                        }
                    }
                }
            }
        }else {
            gameView.showErrorMessage("Cannot choose pawn yet.");
            gameView.updateBoard();
        }
    }

    /**
     * Handles the logic of the sorry card.
     * It creates a JOption pane that lets the player choose where to move the pawn.
     * If the option is not possible then the option appears as -1--1 saying that the move is not possible.
     * If the player picks an option that is not possible, the method is called again until they pick a possible one.
     * If there are no possible moves the player's turn ends.
     * Based on the choice of the player, the player's pawn.
     * @param pawn the pawn that the player chose to swap with another player's.
     * @param currentPlayer the player currently playing.
     * @param otherPlayer the other player.
     * @param id the id of the pawn 1 for pawn1 2 for pawn2.
     * @pre the card that was drawn is a sorry card.
     * @post executes the logic behind the sorry card.
     */
    public void handleCardSorry(Pawn pawn, Player currentPlayer, Player otherPlayer, int id) {
        Square[][] squares = board.getBoard();
        List<int[]> optionalMoves = new ArrayList<>();
        Pawn pawnOther1 = otherPlayer.getPawn1();
        Pawn pawnOther2 = otherPlayer.getPawn2();

        if(!pawnOther1.isAtStart() && !pawnOther1.isAtHome() && !pawnOther1.isInSafeZone()) {
          optionalMoves.add(new int[]{id, 1});
        }else{
            optionalMoves.add(new int[]{-1, -1});
        }
        if(!pawnOther2.isAtStart() && !pawnOther2.isAtHome() && !pawnOther2.isInSafeZone()) {
            optionalMoves.add(new int[]{id, 2});
        }else{
            optionalMoves.add(new int[]{-1, -1});
        }

        String[] opts = new String[2];
        for(int i = 0; i<2; i++) {
            int[] option = optionalMoves.get(i);
            opts[i] = option[0] + "-" + option[1];
        }
        int choice = gameView.showOptions("Choose how you would like to swap pawns.", "Swap pawns", opts);
        if(choice == -1) {
            gameView.showErrorMessage("You must select an option.");
            handleCardSorry(pawn, currentPlayer, otherPlayer, id);
        }else {
            if(choice == 0 && optionalMoves.get(0)[0] != -1) {
                int[] coordinates = ((SorryCard)currentCard).executeSorryCard(pawn, pawnOther1, board);
                gameView.placePawn(coordinates, id, pawn.getColor());
                slidePawn(squares[coordinates[0]][coordinates[1]]);
                int[] start = pawnOther1.getStart();
                gameView.placePawn(start, 1, pawnOther1.getColor());
            }else if(choice == 1 && optionalMoves.get(1)[0] != -1) {
                int[] coordinates = ((SorryCard)currentCard).executeSorryCard(pawn, pawnOther2, board);
                gameView.placePawn(coordinates, id, pawn.getColor());
                slidePawn(squares[coordinates[0]][coordinates[1]]);
                int[] start = pawnOther2.getStart();
                gameView.placePawn(start, 2, pawnOther2.getColor());
            }else{
                gameView.showErrorMessage("You must select a valid option.");
                handleCardSorry(pawn, currentPlayer, otherPlayer, id);
            }
        }
    }

    /**
     * Handles the logic for card 10.
     * It creates a JOption pane that lets the player currently playing to
     * choose whether they want to move a pawn backwards 1 space or forward 10 spaces.
     * If a move is not possible, then the option appears as -1--1 meaning the player cannot make the move.
     * If they choose an option that is not possible, the handleCard10 gets called again until they pick a possible one.
     * If there are no possible moves, the player can click fold, and their turn ends.
     * @pre the drawn card must be 10
     * @post executes the logic of the card 10.
     */
    public void handleCard10() {
        List<int[]> optionalMoves = new ArrayList<>();
        Square[][] squares = board.getBoard();
        boolean one = false;
        Pawn pawn1 = players[currentPlayer].getPawn1();
        Pawn pawn2 = players[currentPlayer].getPawn2();
        if(pawn1.isAtStart() && pawn2.isAtStart()) {
            canClickFold = true;
            gameView.showErrorMessage("No possible move. Click fold.");
            gameView.updateBoard();
        }else{
            if(((NumberTenCard)currentCard).canMove(10, pawn1, board)) {
                optionalMoves.add(new int[]{1, 10});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(((NumberTenCard)currentCard).canMove(10, pawn2, board)) {
                optionalMoves.add(new int[]{2, 10});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(((NumberTenCard) currentCard).canMoveBackwards(1, pawn1, board)) {
                optionalMoves.add(new int[]{1, -1});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(((NumberTenCard) currentCard).canMoveBackwards(1, pawn2, board)) {
                optionalMoves.add(new int[]{2, -1});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(!one) {
                gameView.showErrorMessage("No possible move, click fold.");
                gameView.updateBoard();
                canClickFold = true;
            }else{
                String[] opts = new String[4];
                for(int i = 0; i<4; i++) {
                    int[] option = optionalMoves.get(i);
                    opts[i] = option[0] + "-" + option[1];
                }
                int choice = gameView.showOptions("Choose what you would like to do.", "Move pawns", opts);
                if(choice == -1) {
                    gameView.showErrorMessage("You must select an option.");
                    handleCard10();
                }else{
                    int[] coordinates;
                    if(choice == 0 && optionalMoves.get(0)[0] != -1) {
                        coordinates = ((NumberTenCard) currentCard).executeCard10(10, pawn1, board);
                        if(coordinates.length == 4){
                            int[] coordinates1 = {coordinates[0], coordinates[1]};
                            int[] coordinates2 = {coordinates[2], coordinates[3]};
                            if(coordinates2[1] == 3){
                                gameView.placePawn(coordinates2, 1, Color.RED);
                            }else if(coordinates2[1] == 4) {
                                gameView.placePawn(coordinates2, 2, Color.RED);
                            }else if(coordinates2[1] == 11) {
                                gameView.placePawn(coordinates2, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinates2, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinates2[0]][coordinates2[1]]);
                            gameView.placePawn(coordinates1, 1, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinates1[0]][coordinates1[1]]);
                        }else {
                            gameView.placePawn(coordinates, 1, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinates[0]][coordinates[1]]);
                        }
                        endTurn();
                    }else if(choice == 1 && optionalMoves.get(1)[0] != -1) {
                        coordinates = ((NumberTenCard) currentCard).executeCard10(10, pawn2, board);
                        if(coordinates.length == 4){
                            int[] coordinates1 = {coordinates[0], coordinates[1]};
                            int[] coordinates2 = {coordinates[2], coordinates[3]};
                            if(coordinates2[1] == 3){
                                gameView.placePawn(coordinates2, 1, Color.RED);
                            }else if(coordinates2[1] == 4) {
                                gameView.placePawn(coordinates2, 2, Color.RED);
                            }else if(coordinates2[1] == 11) {
                                gameView.placePawn(coordinates2, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinates2, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinates2[0]][coordinates2[1]]);
                            gameView.placePawn(coordinates1, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinates1[0]][coordinates1[1]]);
                        }else {
                            gameView.placePawn(coordinates, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinates[0]][coordinates[1]]);
                        }
                        endTurn();
                    }else if(choice == 2 && optionalMoves.get(2)[0] != -1) {
                        coordinates = ((NumberTenCard) currentCard).executeCard10(-1, pawn1, board);
                        if(coordinates.length == 4){
                            int[] coordinates1 = {coordinates[0], coordinates[1]};
                            int[] coordinates2 = {coordinates[2], coordinates[3]};
                            if(coordinates2[1] == 3){
                                gameView.placePawn(coordinates2, 1, Color.RED);
                            }else if(coordinates2[1] == 4) {
                                gameView.placePawn(coordinates2, 2, Color.RED);
                            }else if(coordinates2[1] == 11) {
                                gameView.placePawn(coordinates2, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinates2, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinates2[0]][coordinates2[1]]);
                            gameView.placePawn(coordinates1, 1, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinates1[0]][coordinates1[1]]);
                        }else {
                            gameView.placePawn(coordinates, 1, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinates[0]][coordinates[1]]);
                        }
                        endTurn();
                    }else if(choice == 3 && optionalMoves.get(3)[0] != -1){
                        coordinates = ((NumberTenCard) currentCard).executeCard10(-1, pawn2, board);
                        if(coordinates.length == 4){
                            int[] coordinates1 = {coordinates[0], coordinates[1]};
                            int[] coordinates2 = {coordinates[2], coordinates[3]};
                            if(coordinates2[1] == 3){
                                gameView.placePawn(coordinates2, 1, Color.RED);
                            }else if(coordinates2[1] == 4) {
                                gameView.placePawn(coordinates2, 2, Color.RED);
                            }else if(coordinates2[1] == 11) {
                                gameView.placePawn(coordinates2, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinates2, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinates2[0]][coordinates2[1]]);
                            gameView.placePawn(coordinates1, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinates1[0]][coordinates1[1]]);
                        }else {
                            gameView.placePawn(coordinates, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinates[0]][coordinates[1]]);
                        }
                        endTurn();
                    }else{
                        gameView.showErrorMessage("You must select a valid option.");
                        handleCard10();
                    }
                }
            }
        }
    }

    /**
     * Handles the logic for card 7.
     * It creates a JOption pane that lets the player currently playing to
     * choose how to distribute 7 moves between their 2 pawns.
     * If only 1 pawn can be moved then the player selects that pawn and it moves 7 spaces.
     * If a move is not possible, then the option appears as -1--1 meaning the player cannot make the move.
     * If they choose an option that is not possible, the handleCard7 gets called again until they pick a possible one.
     * If there are no possible moves, the player can click fold, and their turn ends.
     * @pre the drawn card must be 7.
     * @post executes the logic of the card 7.
     */
    public void handleCard7() {
        List<int[]> optionalMoves = new ArrayList<>();
        boolean one = false;
        Square[][] squares = board.getBoard();
        Pawn pawn1 = players[currentPlayer].getPawn1();
        Pawn pawn2 = players[currentPlayer].getPawn2();
        if(pawn1.isAtStart() && pawn2.isAtStart()) {
            canClickFold = true;
            gameView.showErrorMessage("No possible move. Click fold.");
            gameView.updateBoard();
        }else if(players[currentPlayer].getPawn1().isAtStart() || players[currentPlayer].getPawn1().isAtHome() && !players[currentPlayer].getPawn2().isAtStart() && !players[currentPlayer].getPawn2().isAtHome()) {
            gameView.showErrorMessage("Chose pawn2 to move 7 spaces.");
            canChosePawn2 = true;
            gameView.updateBoard();
            pawnSelection();
        }else if(players[currentPlayer].getPawn2().isAtStart() || players[currentPlayer].getPawn2().isAtHome() && !players[currentPlayer].getPawn1().isAtStart() && !players[currentPlayer].getPawn1().isAtHome()) {
            gameView.showErrorMessage("Chose pawn1 to move 7 spaces.");
            canChosePawn1 = true;
            gameView.updateBoard();
            pawnSelection();
        }else{
            if(((NumberSevenCard)currentCard).canMove(7, 0, players[currentPlayer], board)) {
                optionalMoves.add(new int[]{7, 0});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(((NumberSevenCard)currentCard).canMove(6, 1, players[currentPlayer], board)) {
                optionalMoves.add(new int[]{6, 1});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(((NumberSevenCard)currentCard).canMove(5, 2, players[currentPlayer], board)) {
                optionalMoves.add(new int[]{5, 2});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(((NumberSevenCard)currentCard).canMove(4, 3, players[currentPlayer], board)) {
                optionalMoves.add(new int[]{4, 3});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(((NumberSevenCard)currentCard).canMove(0, 7, players[currentPlayer], board)) {
                optionalMoves.add(new int[]{0, 7});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(((NumberSevenCard)currentCard).canMove(1, 6, players[currentPlayer], board)) {
                optionalMoves.add(new int[]{1, 6});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(((NumberSevenCard)currentCard).canMove(2, 5, players[currentPlayer], board)) {
                optionalMoves.add(new int[]{2, 5});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(((NumberSevenCard)currentCard).canMove(3, 4, players[currentPlayer], board)) {
                optionalMoves.add(new int[]{3, 4});
                one = true;
            }else{
                optionalMoves.add(new int[]{-1, -1});
            }
            if(!one) {
                gameView.showErrorMessage("No possible move, click fold.");
                gameView.updateBoard();
                canClickFold = true;
            }else{
                String[] opts = new String[8];
                for(int i = 0; i<8; i++) {
                    int[] option = optionalMoves.get(i);
                    opts[i] = option[0] + "-" + option[1];
                }
                int choice = gameView.showOptions("Choose how to split 7 moves.", "Move pawns", opts);
                if(choice == -1) {
                    gameView.showErrorMessage("You must select an option.");
                    handleCard7();
                }else {
                    int coordinates[];
                    int coordinatesPawn1[];
                    int coordinatesPawn2[];
                    int coordinatesPawn3[];
                    int coordinatesPawn4[];

                    if(choice == 0 && optionalMoves.get(0)[0] != -1) {
                        coordinates = ((NumberSevenCard) currentCard).executeCard7(7, 0, players[currentPlayer], board);
                        coordinatesPawn1 = new int[]{coordinates[0], coordinates[1]};
                        coordinatesPawn2 = new int[]{coordinates[2], coordinates[3]};
                        if(coordinates.length == 4){
                            gameView.placePawn(coordinatesPawn1, 1, players[currentPlayer].getPlayerColor());
                            gameView.placePawn(coordinatesPawn2, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinatesPawn1[0]][coordinatesPawn1[1]]);
                            slidePawn(squares[coordinatesPawn2[0]][coordinatesPawn2[1]]);
                        }else if(coordinates.length == 6) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                        }else if(coordinates.length == 8) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                            coordinatesPawn4 = new int[]{coordinates[6], coordinates[7]};
                            if(coordinatesPawn4[1] == 3){
                                gameView.placePawn(coordinatesPawn4, 1, Color.RED);
                            }else if(coordinatesPawn4[1] == 4) {
                                gameView.placePawn(coordinatesPawn4, 2, Color.RED);
                            }else if(coordinatesPawn4[1] == 11) {
                                gameView.placePawn(coordinatesPawn4, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn4, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn4[0]][coordinatesPawn4[1]]);

                        }
                    }else if(choice == 1 && optionalMoves.get(1)[0] != -1) {
                        coordinates = ((NumberSevenCard) currentCard).executeCard7(6, 1, players[currentPlayer], board);
                        coordinatesPawn1 = new int[]{coordinates[0], coordinates[1]};
                        coordinatesPawn2 = new int[]{coordinates[2], coordinates[3]};
                        if(coordinates.length == 4){
                            gameView.placePawn(coordinatesPawn1, 1, players[currentPlayer].getPlayerColor());
                            gameView.placePawn(coordinatesPawn2, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinatesPawn1[0]][coordinatesPawn1[1]]);
                            slidePawn(squares[coordinatesPawn2[0]][coordinatesPawn2[1]]);
                        }else if(coordinates.length == 6) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                        }else if(coordinates.length == 8) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                            coordinatesPawn4 = new int[]{coordinates[6], coordinates[7]};
                            if(coordinatesPawn4[1] == 3){
                                gameView.placePawn(coordinatesPawn4, 1, Color.RED);
                            }else if(coordinatesPawn4[1] == 4) {
                                gameView.placePawn(coordinatesPawn4, 2, Color.RED);
                            }else if(coordinatesPawn4[1] == 11) {
                                gameView.placePawn(coordinatesPawn4, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn4, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn4[0]][coordinatesPawn4[1]]);
                        }
                    }else if(choice == 2 && optionalMoves.get(2)[0] != -1) {
                        coordinates = ((NumberSevenCard) currentCard).executeCard7(5, 2, players[currentPlayer], board);
                        coordinatesPawn1 = new int[]{coordinates[0], coordinates[1]};
                        coordinatesPawn2 = new int[]{coordinates[2], coordinates[3]};
                        if(coordinates.length == 4){
                            gameView.placePawn(coordinatesPawn1, 1, players[currentPlayer].getPlayerColor());
                            gameView.placePawn(coordinatesPawn2, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinatesPawn1[0]][coordinatesPawn1[1]]);
                            slidePawn(squares[coordinatesPawn2[0]][coordinatesPawn2[1]]);
                        }else if(coordinates.length == 6) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                        }else if(coordinates.length == 8) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                            coordinatesPawn4 = new int[]{coordinates[6], coordinates[7]};
                            if(coordinatesPawn4[1] == 3){
                                gameView.placePawn(coordinatesPawn4, 1, Color.RED);
                            }else if(coordinatesPawn4[1] == 4) {
                                gameView.placePawn(coordinatesPawn4, 2, Color.RED);
                            }else if(coordinatesPawn4[1] == 11) {
                                gameView.placePawn(coordinatesPawn4, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn4, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn4[0]][coordinatesPawn4[1]]);
                        }
                    }else if(choice == 3 && optionalMoves.get(3)[0] != -1) {
                        coordinates = ((NumberSevenCard) currentCard).executeCard7(4, 3, players[currentPlayer], board);
                        coordinatesPawn1 = new int[]{coordinates[0], coordinates[1]};
                        coordinatesPawn2 = new int[]{coordinates[2], coordinates[3]};
                        if(coordinates.length == 4){
                            gameView.placePawn(coordinatesPawn1, 1, players[currentPlayer].getPlayerColor());
                            gameView.placePawn(coordinatesPawn2, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinatesPawn1[0]][coordinatesPawn1[1]]);
                            slidePawn(squares[coordinatesPawn2[0]][coordinatesPawn2[1]]);
                        }else if(coordinates.length == 6) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                        }else if(coordinates.length == 8) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                            coordinatesPawn4 = new int[]{coordinates[6], coordinates[7]};
                            if(coordinatesPawn4[1] == 3){
                                gameView.placePawn(coordinatesPawn4, 1, Color.RED);
                            }else if(coordinatesPawn4[1] == 4) {
                                gameView.placePawn(coordinatesPawn4, 2, Color.RED);
                            }else if(coordinatesPawn4[1] == 11) {
                                gameView.placePawn(coordinatesPawn4, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn4, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn4[0]][coordinatesPawn4[1]]);
                        }
                    }else if(choice == 4 && optionalMoves.get(4)[0] != -1) {
                        coordinates = ((NumberSevenCard) currentCard).executeCard7(0, 7, players[currentPlayer], board);
                        coordinatesPawn1 = new int[]{coordinates[0], coordinates[1]};
                        coordinatesPawn2 = new int[]{coordinates[2], coordinates[3]};
                        if(coordinates.length == 4){
                            gameView.placePawn(coordinatesPawn1, 1, players[currentPlayer].getPlayerColor());
                            gameView.placePawn(coordinatesPawn2, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinatesPawn1[0]][coordinatesPawn1[1]]);
                            slidePawn(squares[coordinatesPawn2[0]][coordinatesPawn2[1]]);
                        }else if(coordinates.length == 6) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                        }else if(coordinates.length == 8) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                            coordinatesPawn4 = new int[]{coordinates[6], coordinates[7]};
                            if(coordinatesPawn4[1] == 3){
                                gameView.placePawn(coordinatesPawn4, 1, Color.RED);
                            }else if(coordinatesPawn4[1] == 4) {
                                gameView.placePawn(coordinatesPawn4, 2, Color.RED);
                            }else if(coordinatesPawn4[1] == 11) {
                                gameView.placePawn(coordinatesPawn4, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn4, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn4[0]][coordinatesPawn4[1]]);
                        }
                    }else if(choice == 5 && optionalMoves.get(5)[0] != -1) {
                        coordinates = ((NumberSevenCard) currentCard).executeCard7(1, 6, players[currentPlayer], board);
                        coordinatesPawn1 = new int[]{coordinates[0], coordinates[1]};
                        coordinatesPawn2 = new int[]{coordinates[2], coordinates[3]};
                        if(coordinates.length == 4){
                            gameView.placePawn(coordinatesPawn1, 1, players[currentPlayer].getPlayerColor());
                            gameView.placePawn(coordinatesPawn2, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinatesPawn1[0]][coordinatesPawn1[1]]);
                            slidePawn(squares[coordinatesPawn2[0]][coordinatesPawn2[1]]);
                        }else if(coordinates.length == 6) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                        }else if(coordinates.length == 8) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                            coordinatesPawn4 = new int[]{coordinates[6], coordinates[7]};
                            if(coordinatesPawn4[1] == 3){
                                gameView.placePawn(coordinatesPawn4, 1, Color.RED);
                            }else if(coordinatesPawn4[1] == 4) {
                                gameView.placePawn(coordinatesPawn4, 2, Color.RED);
                            }else if(coordinatesPawn4[1] == 11) {
                                gameView.placePawn(coordinatesPawn4, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn4, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn4[0]][coordinatesPawn4[1]]);
                        }
                    }else if(choice == 6 && optionalMoves.get(6)[0] != -1) {
                        coordinates = ((NumberSevenCard) currentCard).executeCard7(2, 5, players[currentPlayer], board);
                        coordinatesPawn1 = new int[]{coordinates[0], coordinates[1]};
                        coordinatesPawn2 = new int[]{coordinates[2], coordinates[3]};
                        if(coordinates.length == 4){
                            gameView.placePawn(coordinatesPawn1, 1, players[currentPlayer].getPlayerColor());
                            gameView.placePawn(coordinatesPawn2, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinatesPawn1[0]][coordinatesPawn1[1]]);
                            slidePawn(squares[coordinatesPawn2[0]][coordinatesPawn2[1]]);
                        }else if(coordinates.length == 6) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                        }else if(coordinates.length == 8) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                            coordinatesPawn4 = new int[]{coordinates[6], coordinates[7]};
                            if(coordinatesPawn4[1] == 3){
                                gameView.placePawn(coordinatesPawn4, 1, Color.RED);
                            }else if(coordinatesPawn4[1] == 4) {
                                gameView.placePawn(coordinatesPawn4, 2, Color.RED);
                            }else if(coordinatesPawn4[1] == 11) {
                                gameView.placePawn(coordinatesPawn4, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn4, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn4[0]][coordinatesPawn4[1]]);
                        }
                    }else if(choice == 7 && optionalMoves.get(7)[0] != -1){
                        coordinates = ((NumberSevenCard) currentCard).executeCard7(3, 4, players[currentPlayer], board);
                        coordinatesPawn1 = new int[]{coordinates[0], coordinates[1]};
                        coordinatesPawn2 = new int[]{coordinates[2], coordinates[3]};
                        if(coordinates.length == 4){
                            gameView.placePawn(coordinatesPawn1, 1, players[currentPlayer].getPlayerColor());
                            gameView.placePawn(coordinatesPawn2, 2, players[currentPlayer].getPlayerColor());
                            slidePawn(squares[coordinatesPawn1[0]][coordinatesPawn1[1]]);
                            slidePawn(squares[coordinatesPawn2[0]][coordinatesPawn2[1]]);
                        }else if(coordinates.length == 6) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                        }else if(coordinates.length == 8) {
                            coordinatesPawn3 = new int[]{coordinates[4], coordinates[5]};
                            if(coordinatesPawn3[1] == 3){
                                gameView.placePawn(coordinatesPawn3, 1, Color.RED);
                            }else if(coordinatesPawn3[1] == 4) {
                                gameView.placePawn(coordinatesPawn3, 2, Color.RED);
                            }else if(coordinatesPawn3[1] == 11) {
                                gameView.placePawn(coordinatesPawn3, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn3, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn3[0]][coordinatesPawn3[1]]);
                            coordinatesPawn4 = new int[]{coordinates[6], coordinates[7]};
                            if(coordinatesPawn4[1] == 3){
                                gameView.placePawn(coordinatesPawn4, 1, Color.RED);
                            }else if(coordinatesPawn4[1] == 4) {
                                gameView.placePawn(coordinatesPawn4, 2, Color.RED);
                            }else if(coordinatesPawn4[1] == 11) {
                                gameView.placePawn(coordinatesPawn4, 1, Color.YELLOW);
                            }else {
                                gameView.placePawn(coordinatesPawn4, 2, Color.YELLOW);
                            }
                            slidePawn(squares[coordinatesPawn4[0]][coordinatesPawn4[1]]);
                        }
                    }else {
                        gameView.showErrorMessage("You must select a valid option.");
                        handleCard7();
                    }
                }
                endTurn();
            }
        }
    }

    /**
     * Checks to see if a player has won.
     * @post Checks if a player has won.
     * @return the player who has won, null otherwise.
     */
    public Player checkForWinner() {return null;}

    /**
     * Updates the info box
     * @pre There must be a new turn.
     * @post displays the info of the player currently playing.
     * @post displays the amount of cards left in the deck.
     * @post displays the description and the note of the card received.
     */
    public void updateInfoBox() {}

    /**
     * Executes if a player clicks the fold button.
     * @pre The player must have recieved a card.
     * @post ends the players turn so that the other player can play.
     */
    public void foldPressed() {}

    /**
     * Resolves the cases where 2 pawns are on the same square.
     * @pre The card received must allow a player to collide with another pawn either by the slide or a sorry card, etc.
     * @post handles the cases where some pawns collide following the rules of the game.
     */
    public void pawnCollision() {}

    /**
     * Saves the current game.
     * @pre The game must not have finished.
     * @pre There must be at least a move played by a player.
     * @post Saves the game so that it can be accessed in the continueButton from the menu.
     */
    public void saveGame() {}

    /**
     * Continues a game.
     * @pre there must be at least a game that has been saved.
     * @post Loads the saved games state so that it can continue from where it was left from.
     */
    public void continueGame() {}

    /**
     * Exits a game.
     * @post closes a game without saving it.
     */
    public void exitGame() {}

    /**
     * Starts a new game.
     * @post starts a new game without having the previous one saved.
     */
    public void startNewGame() {}

    /**
     * Handles the players changes after their turn.
     * @pre The player must have played their turn.
     * @post Everything that the player did in their turn gets saved in their stats.
     * @param player The player whose stats will be updated.
     */
    public void updatePlayer(Player player) {}

    /**
     * Shuffles the cards.
     * @pre It's the start of the game, or all the cards have been received.
     * @post The cards get shuffled and players can continue receiving them in a different order.
     * @param deck The deck of cards that is in the game.
     */
    public void shuffleCards(Deck deck) {}

    /**
     * Shows possible moves of a player's pawn.
     * @pre The player must have received a card.
     * @pre The player must have moves available.
     * @post The squares that the player can go get highlighted based on the card that the player received.
     * @param received the card that the player received..
     */
    public void showPossibleMoves(Card received) {}

    /**
     * Slides the pawn down a slide.
     * @pre the square given must exist in the board
     * @post The pawn slides all the squares of the slide and ends up at the end of it.
     * @post All the pawns in the slide get back at their home squares.
     * @param square the square that will be checked for the slidePawn.
     */
    public void slidePawn(Square square) {
        if(square instanceof StartSlideSquare && square.getOccupied().getColor() != square.getColor()) {
            Pawn[] pawns = ((StartSlideSquare) square).Slide(board);
            for(int i = 0; i<pawns.length; i++) {
                int id = pawns[i].getId();
                int[] coordinates = pawns[i].getPositionXY();
                Color color = pawns[i].getColor();

                gameView.placePawn(coordinates, id, color);
                gameView.updateBoard();
            }
        }
    }

    /**
     * The player currently playing can receive a card. Meaning their turn starts.
     * @pre the player previously playing must have ended their turn.
     * @post The player currently playing can receive a card.
     */
    public void startTurn() {
        canClickReceive = true;
    }
}
