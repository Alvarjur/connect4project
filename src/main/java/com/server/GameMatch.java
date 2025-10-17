package com.server;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.project.CanvasTimer;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

public class GameMatch implements Initializable{
    @FXML
    private Canvas canvas;
    public static int WINDOW_WIDTH = 900;
    public static int WINDOW_HEIGHT = 600;
    private static double mouse_x, mouse_y;

    private GraphicsContext gc;
    public static double canvas_width = WINDOW_WIDTH;
    public static double canvas_height = WINDOW_HEIGHT;
    private double CELL_SIZE = 80;
    public static Color boardColor = new Color(0,0,0.5,1);
    private Color redColor = new Color(0.5,0,0,1);
    private Color blueColor = new Color(0,0,0.5,1);
    private Color yellowColor = new Color(0.7,0.6,0.3,1);
    private Artist artist = new Artist();
    public static Game game;
    private CanvasTimer timer;


    // Animación de caida
    private Chip animChip;
    private boolean animating = false;
    private int animCol = -1, animRow = -1;
    private double animY;
    private double targetY;
    private double fallSpeed = 1300;
    private long lastRunNanos = 0;

    // Winner line
    private double winner_start_x, winner_start_y, winner_end_x, winner_end_y;



    

    private ArrayList<Chip> redChips = new ArrayList<Chip>();
    private ArrayList<Chip> yellowChips = new ArrayList<Chip>();
    
    @FXML
    private AnchorPane root;
    
    public static void setMousePos(double x, double y) {
        mouse_x = x;
        mouse_y = y;
    }

    // Este es de servidor, hecho durante cambios
    public static void updatePlayerMousePos(String player, double pos_x, double pos_y) {
        game.setPlayerPos(player, pos_x, pos_y);
    }


    public static void setNewWindowSize(int width, int height) {
        WINDOW_WIDTH = width;
        WINDOW_HEIGHT = height;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas.getGraphicsContext2D();
        canvas_width = canvas.getWidth();
        canvas_height = canvas.getHeight();
        // game = new Game();

        timer = new CanvasTimer(
            fps -> update(), 
            this::draw, 
            120);
        timer.start();

        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        canvas.widthProperty().addListener(evt -> updateWindowSize());
        canvas.heightProperty().addListener(evt -> updateWindowSize());

        canvas.setOnMouseMoved(event -> {
            GameMatch.setMousePos(event.getSceneX(), event.getSceneY());

            update();
        });

        canvas.setOnMouseDragged(event -> {
            GameMatch.setMousePos(event.getSceneX(), event.getSceneY());
            game.setPlayersDragging(true);
            
            // update();
        });

        canvas.setOnMouseReleased(event -> {
            game.setPlayersDragging(false);
            // System.out.println("released");
            game.checkReleases();
            // update();
        });

        // update();


    }

    public GameMatch(String player1, String player2) {
        game = new Game(player1, player2);
    }

    public void updateWindowSize() {
        canvas_width = canvas.getWidth();
        canvas_height = canvas.getHeight();
        // draw();
    }

    

    public void update() {
        System.out.println("player1 pos_x: " + game.player1.x + " player2 pos_x: " + game.player2.x);
        game.updatePlayerPositions();
        game.updateLogic();
        game.updateVisualLogics();

        

        
    }

    public void draw() {
        artist.draw();
        
        
    }

    class Artist implements drawable{
        
        @Override
        public void draw() {
            gc.clearRect(0, 0, canvas_width, canvas_height);
            gc.setFill(boardColor);
            game.artist.drawDraggableChips();
            game.artist.draw();
            game.artist.drawChipsDragging();
            game.drawAnimChip();
            if(game.winner != null) {
                game.artist.drawWinnerLine();
            }
            
        }

        
    }

    class Game {
        private int currentPlayer;
        private Board board;
        public Player player1, player2;
        private Player winner;
        private ArrayList<Player> players = new ArrayList<Player>();
        public GameArtist artist;
        private double draggableChips_red_x = 650;
        private double draggableChips_red_y = 100;
        private double draggableChips_yellow_x = 800;
        private double draggableChips_yellow_y = 100;
        private DraggableChip redDraggableChip;
        private DraggableChip yellowDraggableChip;
        private ArrayList<DraggableChip> draggableChips = new ArrayList<DraggableChip>();
        private Chip currentChip;


        public Game(String player1, String player2) {
            currentPlayer = 1; // Red starts
            
            board = new Board(20, 100, 6,7);
            this.player1 = new Player(player1,1, 50, 50);
            this.player2 = new Player(player2, 2, 150, 50);
            players.add(this.player1);
            players.add(this.player2);

            redDraggableChip = new DraggableChip(draggableChips_red_x, draggableChips_red_y, redColor, this.player1);
            yellowDraggableChip = new DraggableChip(draggableChips_yellow_x, draggableChips_yellow_y, yellowColor, this.player2);
            draggableChips.add(redDraggableChip);
            draggableChips.add(yellowDraggableChip);
            artist = new GameArtist();

        }

        public void setPlayerPos(String playerSending, double pos_x, double pos_y) {
            for (Player player : players) {
                if(player.name.equals(playerSending)) {
                    player.x = pos_x;
                    player.y = pos_y;

                    return;
                }
            }
        }

        public void switchPlayer() {
            if (currentPlayer == 1) {
                currentPlayer = 2;
            } else {
                currentPlayer = 1;
            }
        }

        public void setPlayersDragging(boolean isDragging) {
            player1.isDragging = isDragging;
            player2.isDragging = isDragging;
        }

        public void updatePlayerPositions() {
            player1.setPosition(mouse_x, mouse_y);
            player2.setPosition(mouse_x + 100, mouse_y);
        }

        public void updateVisualLogics()
        {
            long now = System.nanoTime();
            double dt;
            if (lastRunNanos == 0) {
            dt = 0; // primer frame
            } else {
            dt = (now - lastRunNanos) / 1_000_000_000.0;
            }
            lastRunNanos = now;

            if (animating && dt > 0) {
            animY += fallSpeed * dt;
            if (animY >= targetY) {
                animY = targetY;
                board.addChip(animChip, animCol); // Se añade la ficha al terminar de caer
                animating = false;
                
            }

        }
        }

        public void drawAnimChip() {
            if (!animating) return;
            double r = CELL_SIZE;
            double cx = game.board.x + game.board.artist.margin/2 + animCol * (CELL_SIZE + game.board.artist.space_between);
            double cy = animY;
            
            Color color = animChip.player == 1 ? redColor : yellowColor;
            gc.setFill(color);
            gc.fillOval(cx, cy, r, r);

        }

        public boolean isPlayerDraggingChip(Player player, DraggableChip draggableChip) {
            return draggableChip.isPlayerDraggingThisChip();
            
        }

        public void checkReleases() {
            if (game.currentChip != null) {
                ArrayList<Integer> possibleMoves = board.getNotFullColumns();
                int move = game.board.whatColIsChipDroppedIn(currentChip);
                for (Integer possibleMove : possibleMoves) {
                    if(possibleMove == move) {
                        // Preparando animación
                        board.artist.doAddChipAnimation(currentChip, move); // La ficha se añade cuando se cambia animating = false en updateVisualLogics()
                    }
                }
            }
        }

        public int checkWinner() {
            for(int row = 0; row < board.grid.length; row++) {
                for(int col = 0; col < board.grid[0].length; col++) {
                    // Check for the player assigned to the chip, then check for coincidences in a direction,
                    // if there are 4 straight, then that player wins.
                    int player = board.grid[row][col];
                    if(player != 0){
                        if(checkIsThereFourStraight(player, row, col)) {
                            System.out.println(player + " wins");
                            winner = players.get(player - 1);
                            return player;
                        }
                    }

                }
            }

            return -1;
        }


        public boolean checkIsThereFourStraight(int player, int row, int col) {
            if (player == 0) return false;
            
            int coincidenceAmount = 0;
            // Checking to the right
            for (int i = 0; i < 4; i++) {
                
                //Checking if it's out of bounds
                if (col + i > game.board.grid[0].length - 1) break;
                if (game.board.grid[row][col + i] == player) coincidenceAmount++;
                if (coincidenceAmount >= 4) {
                    winner_start_x = board.artist.getRowColPosition(row, col)[0]; 
                    winner_start_y = board.artist.getRowColPosition(row, col)[1]; 
                    winner_end_x = board.artist.getRowColPosition(row, col + i)[0];
                    winner_end_y = board.artist.getRowColPosition(row, col)[1]; 
                    return true;
                }
            }

            coincidenceAmount = 0;
            // Checking down
            for (int i = 0; i < 4; i++) {
                if (i == 0) {
                    winner_start_x = board.artist.getRowColPosition(row, col)[0]; 
                }
                //Checking if it's out of bounds
                if (row + i > game.board.grid.length - 1) break;
                if (game.board.grid[row + i][col] == player) coincidenceAmount++;
                if (coincidenceAmount >= 4) {
                    winner_start_x = board.artist.getRowColPosition(row, col)[0]; 
                    winner_start_y = board.artist.getRowColPosition(row, col)[1]; 
                    winner_end_x = board.artist.getRowColPosition(row, col)[0];
                    winner_end_y = board.artist.getRowColPosition(row + i, col)[1];
                    return true;
                }
            }

            // No need to check up or left

            coincidenceAmount = 0;
            // Checking down-right
            for (int i = 0; i < 4; i++) {
                if (i == 0) {
                    winner_start_x = board.artist.getRowColPosition(row, col)[0]; 
                }
                //Checking if it's out of bounds
                if (row + i > game.board.grid.length - 1 || col + i > game.board.grid[0].length - 1) break;
                if (game.board.grid[row + i][col + i] == player) coincidenceAmount++;
                if (coincidenceAmount >= 4) {
                    winner_start_x = board.artist.getRowColPosition(row, col)[0]; 
                    winner_start_y = board.artist.getRowColPosition(row, col)[1]; 
                    winner_end_x = board.artist.getRowColPosition(row + i, col + i)[0];
                    winner_end_y = board.artist.getRowColPosition(row + i, col + i)[1];
                    return true;
                }
            }

            coincidenceAmount = 0;
            // Checking down-left
            for (int i = 0; i < 4; i++) {
                if (i == 0) {
                    winner_start_x = board.artist.getRowColPosition(row, col)[0]; 
                }
                //Checking if it's out of bounds
                if (row + i > game.board.grid.length - 1 || col - i < 0) break;
                if (game.board.grid[row + i][col - i] == player) coincidenceAmount++;
                if (coincidenceAmount >= 4) {
                    winner_start_x = board.artist.getRowColPosition(row, col)[0]; 
                    winner_start_y = board.artist.getRowColPosition(row, col)[1]; 
                    winner_end_x = board.artist.getRowColPosition(row + i, col - i)[0];
                    winner_end_y = board.artist.getRowColPosition(row + i, col - i)[1]; 
                    return true;
                }
            }


            return false;
        }




        public void updateLogic() {
            if (isPlayerDraggingChip(player1, redDraggableChip)) {
                currentChip = player1.takeChip();
                redDraggableChip.setIsBeingDragged(true);
                // System.out.println("red dragging");
            } else {
                redDraggableChip.setIsBeingDragged(false);
                currentChip = null;
            }
            if (isPlayerDraggingChip(player2, yellowDraggableChip)) {
                currentChip = player2.takeChip();
                yellowDraggableChip.setIsBeingDragged(true);
                
                // System.out.println("yellow dragging");
            } else {
                yellowDraggableChip.setIsBeingDragged(false);
                currentChip = null;
            }
            checkWinner();
        }

        class GameArtist implements drawable {
            @Override
            public void draw() {
                board.artist.draw();
                player1.artist.draw();
                player2.artist.draw();
            }

            public void drawWinnerLine() {
                gc.setFill(yellowColor);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(20);
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.strokeLine(winner_start_x, winner_start_y, winner_end_x, winner_end_y);
            }

            public void drawBoard(Board board) {
                board.artist.draw();
            }

            public void drawChip(Chip chip) {
                chip.artist.draw();
            }

            public void drawDraggableChips() {
                // Draw red chips
                redDraggableChip.artist.draw();
                yellowDraggableChip.artist.draw();

            }

            public void drawChipsDragging() {
                if (isPlayerDraggingChip(player1, redDraggableChip)) {
                    currentChip = createChip(1);
                    currentChip.artist.drawOnPlayer(player1);
                    // Drawing the player again so it appears on top of the chip
                    player1.artist.draw();
                    board.artist.drawPossibleMoves();
                }
                if (isPlayerDraggingChip(player2, yellowDraggableChip)) {
                    currentChip = createChip(2);
                    currentChip.artist.drawOnPlayer(player2);
                    // Drawing the player again so it appears on top of the chip
                    player2.artist.draw();
                    board.artist.drawPossibleMoves();
                }
            }
        }

        class DraggableChip {
            private double x, y;
            private double diameter = CELL_SIZE;
            private Color color;
            private boolean beingDragged = false;
            private Player assignedPlayer;
            private DraggableChipArtist artist;

            public DraggableChip(double x, double y, Color color, Player player) {
                this.x = x;
                this.y = y;
                this.color = color;
                this.assignedPlayer = player;
                this.artist = new DraggableChipArtist();
            }

            public void setIsBeingDragged(boolean beingDragged) {
                this.beingDragged = beingDragged;
            }

            public boolean isPlayerDraggingThisChip() {
                if(!isPlayerDragging()) return false;

                if(beingDragged) return true;

                if (isPlayerOverChip() && isPlayerDragging()) {
                    setIsBeingDragged(true);
                    return true;
                }
                setIsBeingDragged(false);
                return false;
            }

            public boolean isPlayerDragging() {
                return assignedPlayer.isDragging;
            }

            public boolean isPlayerOverChip() {
                if (assignedPlayer.x >= x && assignedPlayer.x <= x + diameter &&
                    assignedPlayer.y >= y && assignedPlayer.y <= y + diameter) {
                        return true;
                    }
                return false;
            }

            class DraggableChipArtist implements drawable {
                @Override
                public void draw() {
                    gc.setFill(color);
                    gc.fillOval(x, y, diameter, diameter);
                }
            }
        }



    }

    class Player {
        private int playerNumber;
        private String name;
        private PlayerArtist artist;
        private double x, y;
        private boolean isDragging = false;
        
        public Player(String playerName, int playerNumber, double x, double y) {
            this.name = playerName;
            this.playerNumber = playerNumber;
            this.artist = new PlayerArtist();
            this.x = x;
            this.y = y;
        }

        public void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Chip takeChip() {
            return createChip(playerNumber);
        }

        class PlayerArtist implements drawable {
            private double radius = CELL_SIZE/3;
            private Color color1 = new Color(1,0,0,0.5);
            private Color color2 = new Color(0,1,1,0.5);

            public PlayerArtist() {
        
            }

            @Override
            public void draw() {
                if (playerNumber == 1) {
                    gc.setFill(color1);
                } else {
                    gc.setFill(color2);
                }
                gc.fillOval(x - radius/2, y - radius/2, radius, radius);
            }
        }
    }

    class Board{
        private BoardArtist artist;
        private int[][] grid; // 0 = empty, 1 = red, 2 = yellow
        private double x, y;


        public Board(double x, double y, int rows, int cols) {
            grid = new int[rows][cols];
            artist = new BoardArtist(rows, cols);
            this.x = x;
            this.y = y;
            // grid[5][6] = 1; // for testing
            // grid[4][6] = 2; // for testing
            // grid[3][6] = 1; // for testing
            // grid[2][6] = 2; // for testing
            // grid[1][6] = 1; // for testing
            // grid[0][6] = 1; // for testing
            // grid[5][5] = 2; // for testing
            
        }

        public ArrayList<Integer> getNotFullColumns() {
                ArrayList<Integer> notFullCols = new ArrayList<Integer>();
                for (int col = 0; col < grid[0].length; col++) {
                    if (grid[0][col] == 0) {
                        notFullCols.add(col);
                    }
                }

                return notFullCols;
            }

        public int whatColIsChipDroppedIn(Chip chip) {
                double colXStart = 5000, colXEnd = 5000, colYStart = 5000, colYEnd = 5000;
                colYStart = game.board.y - CELL_SIZE - game.board.artist.space_between;
                colYEnd = game.board.y;

                for(int col = 0; col < grid[0].length; col++) {
                    colXStart = game.board.x + game.board.artist.margin/2 + col * (CELL_SIZE + game.board.artist.space_between);
                    colXEnd = game.board.x + CELL_SIZE + game.board.artist.margin/2 + col * (CELL_SIZE + game.board.artist.space_between);
                    if (game.players.get(chip.player - 1).x > colXStart && game.players.get(chip.player - 1).x < colXEnd 
                    && game.players.get(chip.player - 1).y > colYStart && game.players.get(chip.player - 1).y < colYEnd) {
                        return col;
                    }
                }
                return -1;
            }

        public void addChip(Chip chip, int col) {
            int row_to_add_in = 0;

            for(int i = grid.length - 1; i > 0; i--) {
                if(grid[i][col] == 0) {
                    row_to_add_in = i;
                    break;
                }
            }

            grid[row_to_add_in][col] = chip.player;
        }



        class BoardArtist implements drawable {
            private double width = 0, height = 0;
            private double margin = CELL_SIZE/10;
            private double space_between = margin/2;

            public BoardArtist(int rows, int cols) {
                

                for(int i = 0; i < cols; i++) {
                    this.width += CELL_SIZE;
                }
                for(int i = 0; i < rows; i++) {
                    this.height += CELL_SIZE;
                }

                this.width += space_between * cols;
                this.height += space_between * rows;
            }

            public double[] getRowColPosition(int row, int col) {
                double[] position = new double[2];
                double pos_x = 5000, pos_y = 5000;

                for(int i = 0; i < grid.length; i++) {
                                      // cols
                    for(int j = 0; j < grid[0].length; j++) {
                        if (i == row && j == col) {
                            pos_x = CELL_SIZE/2 + x + margin/2 + j * (CELL_SIZE + space_between);
                            pos_y = CELL_SIZE/2 + y + margin/2 + i * (CELL_SIZE + space_between);
                        }
                        
                    }
                }
                position[0] = pos_x;
                position[1] = pos_y;
                return position;
            }

            public void doAddChipAnimation(Chip chip, int col) {
                int row = findLowestEmptyRow(col);
                if (row < 0) return; // columna plena

                double cellCenterYTop = game.board.y + CELL_SIZE * 0.5;
                double startY = game.board.y - CELL_SIZE * 0.5; // lleugerament per sobre
                double endY = cellCenterYTop + row * CELL_SIZE;

                // Se asigna currentChip a animChip ya que currentChip se vuelve null casi instantáneamente.
                animChip = game.currentChip;
                animCol = col;
                animRow = row;
                animY = startY;
                targetY = endY;
                animating = true;
                lastRunNanos = 0; // perquè el primer dt es calculi bé
            }

            private int findLowestEmptyRow(int col) {
            for (int r = game.board.grid.length - 1; r >= 0; r--) {
                if (game.board.grid[r][col] == 0) return r;
            }
            return -1;
        }

            public boolean isChipIn(int row, int col, int player) {
                if (grid[row][col] == player) {
                    return true;
                }
                return false;
            }


            

            

            @Override
            public void draw() {
                gc.setFill(boardColor);
                gc.fillRect(x, y, width + margin, height + space_between);
                                    //rows
                for(int i = 0; i < grid.length; i++) {
                                      // cols
                    for(int j = 0; j < grid[0].length; j++) {
                        double pos_x = x + margin/2 + j * (CELL_SIZE + space_between);
                        double pos_y = y + margin/2 + i * (CELL_SIZE + space_between);
                        gc.setFill(Color.WHITE);
                        gc.fillOval(pos_x, pos_y, CELL_SIZE, CELL_SIZE);

                        // Drawing chips in case there are
                        if(isChipIn(i, j, 1)) {
                            gc.setFill(redColor);
                            gc.fillOval(pos_x, pos_y, CELL_SIZE, CELL_SIZE);
                        } else if (isChipIn(i, j, 2)) {
                            gc.setFill(yellowColor);
                            gc.fillOval(pos_x, pos_y, CELL_SIZE, CELL_SIZE);
                        }
                    }
                }
            }

            public void drawPossibleMoves() {
                ArrayList<Integer> notFullCols = getNotFullColumns();
                gc.setFill(new Color(0,1,0,0.3));
                for (int col : notFullCols) {
                    double pos_x = x + margin/2 + col * (CELL_SIZE + space_between);
                    double pos_y = (y + margin/2) - CELL_SIZE - space_between;
                    gc.fillOval(pos_x, pos_y, CELL_SIZE, CELL_SIZE);
                }
            }


        }
    }

    class Chip {
        private int player; // 1 o 2
        private ChipArtist artist;
        private double x, y;

        public Chip(int player, double x, double y) {
            this.x = x;
            this.y = y;
            this.player = player;
            this.artist = new ChipArtist(CELL_SIZE, this.player);
        }

        class ChipArtist implements drawable {
            private double diameter;
            private Color color;
            public ChipArtist(double diameter, int player) {
                if (player == 1) {
                    this.color = redColor;
                } else {
                    this.color = yellowColor;
                }

                this.diameter = diameter;

            }
            public void draw() {
                gc.setFill(color);
                gc.fillOval(x, y, diameter, diameter);
            }

            public void drawOnPlayer(Player player) {
                gc.setFill(color);
                x = player.x - diameter/2;
                y = player.y - diameter/2;
                draw();
            }
        }
    }

    public Chip createChip(int player) {
        return new Chip(player, game.players.get(player - 1).x, game.players.get(player - 1).y);
    }



    public interface drawable {
        void draw();
    }
}
