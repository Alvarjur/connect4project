package com.project;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class Controller implements Initializable{
    @FXML
    private Canvas canvas;

    private static double mouse_x, mouse_y;

    private GraphicsContext gc;
    private static double canvas_width = Main.WINDOW_WIDTH;
    private static double canvas_height = Main.WINDOW_HEIGHT;
    private double CELL_SIZE = 80;
    private Color boardColor = new Color(0,0,0.5,1);
    private Color redColor = new Color(0.5,0,0,1);
    private Color blueColor = new Color(0,0,0.5,1);
    private Color yellowColor = new Color(0.7,0.6,0.3,1);
    private Artist artist = new Artist();
    private Game game;

    private ArrayList<Chip> redChips = new ArrayList<Chip>();
    private ArrayList<Chip> yellowChips = new ArrayList<Chip>();
    
    @FXML
    private AnchorPane root;
    
    public static void setMousePos(double x, double y) {
        mouse_x = x;
        mouse_y = y;
    }


    public static void setNewWindowSize(int width, int height) {
        Main.WINDOW_WIDTH = width;
        Main.WINDOW_HEIGHT = height;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas.getGraphicsContext2D();
        canvas_width = canvas.getWidth();
        canvas_height = canvas.getHeight();
        game = new Game();


        
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        canvas.widthProperty().addListener(evt -> updateWindowSize());
        canvas.heightProperty().addListener(evt -> updateWindowSize());

        canvas.setOnMouseMoved(event -> {
            Controller.setMousePos(event.getSceneX(), event.getSceneY());

            update();
        });

        canvas.setOnMouseDragged(event -> {
            Controller.setMousePos(event.getSceneX(), event.getSceneY());
            game.setPlayersDragging(true);
            
            update();
        });

        canvas.setOnMouseReleased(event -> {
            game.setPlayersDragging(false);
            System.out.println("released");
            update();
        });

        update();


    }
    public void updateWindowSize() {
        canvas_width = canvas.getWidth();
        canvas_height = canvas.getHeight();
        draw();
    }

    public void draw() {
        gc.clearRect(0, 0, canvas_width, canvas_height);
        gc.setFill(boardColor);
        artist.draw();


    }

    public void update() {
        game.updatePlayerPositions();
        game.updateLogic();
        draw();
    }

    class Artist implements drawable{
        
        @Override
        public void draw() {
            game.artist.drawDraggableChips();
            game.artist.draw();
            game.artist.drawChipsDragging();
            
            
        }

        
    }

    class Game {
        private int currentPlayer;
        private Board board;
        private Player player1, player2;
        private ArrayList<Player> players = new ArrayList<Player>();
        private GameArtist artist;
        private double draggableChips_red_x = 650;
        private double draggableChips_red_y = 100;
        private double draggableChips_yellow_x = 800;
        private double draggableChips_yellow_y = 100;
        private DraggableChip redDraggableChip;
        private DraggableChip yellowDraggableChip;
        private ArrayList<DraggableChip> draggableChips = new ArrayList<DraggableChip>();
        private Chip currentChip;


        public Game() {
            currentPlayer = 1; // Red starts
            
            board = new Board(6,7);
            player1 = new Player(1, 50, 50);
            player2 = new Player(2, 150, 50);
            players.add(player1);
            players.add(player2);

            redDraggableChip = new DraggableChip(draggableChips_red_x, draggableChips_red_y, redColor, player1);
            yellowDraggableChip = new DraggableChip(draggableChips_yellow_x, draggableChips_yellow_y, yellowColor, player2);
            draggableChips.add(redDraggableChip);
            draggableChips.add(yellowDraggableChip);
            artist = new GameArtist();

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

        public boolean isPlayerDraggingChip(Player player, DraggableChip draggableChip) {
            return draggableChip.isPlayerDraggingThisChip();
            
        }

        public void updateLogic() {
            if (isPlayerDraggingChip(player1, redDraggableChip)) {
                currentChip = player1.takeChip();
                redDraggableChip.setIsBeingDragged(true);
                System.out.println("red dragging");
            } else {
                redDraggableChip.setIsBeingDragged(false);
                currentChip = null;
            }
            if (isPlayerDraggingChip(player2, yellowDraggableChip)) {
                currentChip = player2.takeChip();
                yellowDraggableChip.setIsBeingDragged(true);
                
                System.out.println("yellow dragging");
            } else {
                yellowDraggableChip.setIsBeingDragged(false);
                currentChip = null;
            }
        }

        class GameArtist implements drawable {
            @Override
            public void draw() {
                board.artist.draw();
                player1.artist.draw();
                player2.artist.draw();
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
        private PlayerArtist artist;
        private double x, y;
        private boolean isDragging = false;
        
        public Player(int playerNumber, double x, double y) {
            this.playerNumber = playerNumber;
            this.artist = new PlayerArtist(x, y);
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

            public PlayerArtist(double x, double y) {
        
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


        public Board(int rows, int cols) {
            grid = new int[rows][cols];
            artist = new BoardArtist(20,100, rows, cols);
            grid[5][6] = 1; // for testing
            grid[4][6] = 2; // for testing
            grid[3][6] = 1; // for testing
            grid[2][6] = 2; // for testing
            grid[1][6] = 1; // for testing
            grid[0][6] = 1; // for testing
            grid[5][5] = 2; // for testing
            
        }

        class BoardArtist implements drawable {
            private double x, y;
            private double width = 0, height = 0;
            private double margin = CELL_SIZE/10;
            private double space_between = margin/2;

            public BoardArtist(double x, double y, int rows, int cols) {
                this.x = x;
                this.y = y;

                for(int i = 0; i < cols; i++) {
                    this.width += CELL_SIZE;
                }
                for(int i = 0; i < rows; i++) {
                    this.height += CELL_SIZE;
                }

                this.width += space_between * cols;
                this.height += space_between * rows;
            }

            public boolean isChipIn(int row, int col, int player) {
                if (grid[row][col] == player) {
                    return true;
                }
                return false;
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

            @Override
            public void draw() {
                gc.setFill(boardColor);
                gc.fillRect(x, y, width + margin, height + space_between);
                                    //rows
                for(int i = 0; i < grid.length; i++) {
                                      // cols
                    for(int j = 0; j < grid[0].length; j++) {
                        double x = this.x + margin/2 + j * (CELL_SIZE + space_between);
                        double y = this.y + margin/2 + i * (CELL_SIZE + space_between);
                        gc.setFill(Color.WHITE);
                        gc.fillOval(x, y, CELL_SIZE, CELL_SIZE);

                        // Drawing chips in case there are
                        if(isChipIn(i, j, 1)) {
                            gc.setFill(redColor);
                            gc.fillOval(x, y, CELL_SIZE, CELL_SIZE);
                        } else if (isChipIn(i, j, 2)) {
                            gc.setFill(yellowColor);
                            gc.fillOval(x, y, CELL_SIZE, CELL_SIZE);
                        }
                    }
                }
            }

            public void drawPossibleMoves() {
                ArrayList<Integer> notFullCols = getNotFullColumns();
                gc.setFill(new Color(0,1,0,0.3));
                for (int col : notFullCols) {
                    double x = this.x + margin/2 + col * (CELL_SIZE + space_between);
                    double y = (this.y + margin/2) - CELL_SIZE - space_between;
                    gc.fillOval(x, y, CELL_SIZE, CELL_SIZE);
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
                gc.fillOval(player.x - diameter/2, player.y - diameter/2, diameter, diameter);
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
