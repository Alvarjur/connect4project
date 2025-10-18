package com.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.json.JSONArray;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

import com.project.CanvasTimer;
import com.server.GameMatch;

public class ControllerGame implements Initializable {
    
    private static double mouse_x, mouse_y;
    private static boolean dragging;
    private static String name = Main.clientName;
    private static int playerNum;
    public static double CELL_SIZE = 80;
    @FXML
    private Canvas canvas;
    private static GraphicsContext gc;

    public double mouse_x_p2, mouse_y_p2;
    public static int[][] grid;

    public static double board_x, board_y;
    public static double red_chip_dragg_x, red_chip_dragg_y, yellow_chip_dragg_x, yellow_chip_dragg_y;

    public static Color boardColor = new Color(0,0,0.5,1);
    private static Color redColor = new Color(0.5,0,0,1);
    private static Color blueColor = new Color(0,0,0.5,1);
    private static Color yellowColor = new Color(0.7,0.6,0.3,1);

    public static double currentChip_x, currentChip_y;
    public static int currentChip_player;

    public static ArrayList<Integer> possibleMovesList = new ArrayList<>();

    // Animación de caida
    private boolean animating = false;
    private int animCol = -1, animRow = -1;
    private double animY;
    private double targetY;
    private double fallSpeed = 1300;
    private long lastRunNanos = 0;

    // redDraggableChip = new DraggableChip(draggableChips_red_x, draggableChips_red_y, redColor, this.player1);
    // yellowDraggableChip = new DraggableChip(draggableChips_yellow_x, draggableChips_yellow_y, yellowColor, this.player2);

    // Winner line
    private double winner_start_x, winner_start_y, winner_end_x, winner_end_y;


    // Artists
    private static BoardArtist boardArtist;
    private ChipArtist chipArtist;
    private PlayerArtist playerArtist;
    private GameArtist gameArtist;
    
    public static void updateBoardPos(double x, double y) {
        board_x = x;
        board_y = y;
    }
    public static void updateCurrentChip(String chip) {
        if (chip.equals("none")) {
            currentChip_player = 0;
            return;
        }

        String parts[] = chip.split(" ");
        currentChip_x = Double.parseDouble(parts[0]);
        currentChip_y = Double.parseDouble(parts[1]);
        currentChip_player = Integer.parseInt(parts[2]);

    }
    public static void updateDragChipsPos(double red_x, double red_y, double yellow_x, double yellow_y) {
        red_chip_dragg_x = red_x;
        red_chip_dragg_y = red_y;
        yellow_chip_dragg_x = yellow_x;
        yellow_chip_dragg_y = yellow_y;
    }
    public static void setPlayerInfo(String nombre, int numero, boolean draggea) {
        name = Main.clientName;
        playerNum = numero;
        dragging = draggea;
    }

    public static void updatePossibleMoves(String possibleMoves) {
        if(possibleMoves.equals("none")) {
            possibleMovesList.clear();
            return;
        }

        for(int i = 0; i < possibleMoves.length(); i++) {
            possibleMovesList.add(Integer.valueOf(possibleMoves.charAt(i)));
        }
    }

    public static void updateGrid(JSONArray arr) {
        for (int i = 0; i < arr.length(); i++) {
            Main.log(arr.get(i).toString());
            String[] parts = arr.get(i).toString().split(" ");

            grid[Integer.parseInt(parts[0])][Integer.parseInt(parts[1])] = Integer.parseInt(parts[2]);
        }
    }

    public static void draw(double pos_x_1, double pos_y_1, double pos_x_2, double pos_y_2) {
        gc.clearRect(0, 0, 10000, 10000);
        // Drawing the board
        boardArtist.draw();
        Color color1 = new Color(1,0,0,0.5);
        Color color2 = new Color(0,1,1,0.5);

        // Drawing draggable chips
        gc.setFill(redColor);
        gc.fillOval(red_chip_dragg_x, red_chip_dragg_y, CELL_SIZE, CELL_SIZE);
        gc.setFill(yellowColor);
        gc.fillOval(yellow_chip_dragg_x, yellow_chip_dragg_y, CELL_SIZE, CELL_SIZE);

        // Drawing possibleMoves
        if (currentChip_player != 0) {
            boardArtist.drawPossibleMoves();
        }

        // Drawing currentChip
        if(currentChip_player != 0) {
            gc.setFill(currentChip_player == 1 ? redColor : yellowColor);
            gc.fillOval(currentChip_x - CELL_SIZE/2, currentChip_y - CELL_SIZE/2, CELL_SIZE, CELL_SIZE);
        }

        // Drawing the players
        gc.setFill(color1);
        gc.fillOval(pos_x_1 - 25, pos_y_1 - 25, 50, 50);
        gc.setFill(color2);
        gc.fillOval(pos_x_2 - 25, pos_y_2 - 25, 50, 50);

        
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas.getGraphicsContext2D();
        boardArtist = new BoardArtist(6, 7);
        grid = new int[6][7];

        canvas.setOnMouseMoved(event -> {
            Main.sendPlayerMousePosInfo(Main.clientName, event.getSceneX(), event.getSceneY(), dragging);

            //TODO Aquí hacer que mande un json con la info necesaria
        });

        canvas.setOnMouseDragged(event -> {
            dragging = true;
            Main.sendPlayerMousePosInfo(Main.clientName, event.getSceneX(), event.getSceneY(), dragging);
            

            //TODO Aquí hacer que mande un json con la info necesaria
        });

        canvas.setOnMouseReleased(event -> {
            dragging = false;
            Main.sendPlayerMousePosInfo(Main.clientName, event.getSceneX(), event.getSceneY(), dragging);
            
            // game.checkReleases();

            //TODO Aquí hacer que mande un json con la info necesaria

        });
}

    public interface drawable {
            void draw();
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
                                pos_x = CELL_SIZE/2 + board_x + margin/2 + j * (CELL_SIZE + space_between);
                                pos_y = CELL_SIZE/2 + board_y + margin/2 + i * (CELL_SIZE + space_between);
                            }
                            
                        }
                    }
                    position[0] = pos_x;
                    position[1] = pos_y;
                    return position;
                }

                public void doAddChipAnimation(int col) {
                    int row = findLowestEmptyRow(col);
                    if (row < 0) return; // columna plena

                    double cellCenterYTop = board_y + CELL_SIZE * 0.5;
                    double startY = board_y - CELL_SIZE * 0.5; // lleugerament per sobre
                    double endY = cellCenterYTop + row * CELL_SIZE;

                    // Se asigna currentChip a animChip ya que currentChip se vuelve null casi instantáneamente.
                    animCol = col;
                    animRow = row;
                    animY = startY;
                    targetY = endY;
                    animating = true;
                    lastRunNanos = 0; // perquè el primer dt es calculi bé
                }

                private int findLowestEmptyRow(int col) {
                for (int r = grid.length - 1; r >= 0; r--) {
                    if (grid[r][col] == 0) return r;
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
                    gc.fillRect(board_x, board_y, width + margin, height + space_between);
                                        //rows
                    for(int i = 0; i < grid.length; i++) {
                    // for(int i = 0; i < 7; i++) {
                                        // cols
                        for(int j = 0; j < grid[0].length; j++) {
                        // for(int j = 0; j < 6; j++) {
                            double pos_x = board_x + margin/2 + j * (CELL_SIZE + space_between);
                            double pos_y = board_y + margin/2 + i * (CELL_SIZE + space_between);
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
                        double pos_x = board_x + margin/2 + col * (CELL_SIZE + space_between);
                        double pos_y = (board_y + margin/2) - CELL_SIZE - space_between;
                        gc.fillOval(pos_x, pos_y, CELL_SIZE, CELL_SIZE);
                    }
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


            }
        
        class ChipArtist implements drawable {
            private double diameter;
            private Color color;
            private double x,y;

            public ChipArtist(double diameter, int player) {
                if (player == 1) {
                    this.color = redColor;
                } else {
                    this.color = yellowColor;
                }

                this.diameter = diameter;

            }

            public void setPosition(double x, double y) {
                this.x = x;
                this.y = y;
            }
            public void draw() {
                gc.setFill(color);
                gc.fillOval(this.x, this.y, diameter, diameter);
            }

            public void drawOnPlayer(double player_x, double player_y) {
                gc.setFill(color);
                x = player_x - diameter/2;
                y = player_y - diameter/2;
                draw();
            }
        }

        class PlayerArtist implements drawable {
            private double radius = CELL_SIZE/3;
            private Color color1 = new Color(1,0,0,0.5);
            private Color color2 = new Color(0,1,1,0.5);

            public PlayerArtist() {
        
            }

            @Override
            public void draw() {
                if (playerNum == 1) {
                    gc.setFill(color1);
                } else {
                    gc.setFill(color2);
                }
                gc.fillOval(mouse_x - radius/2, mouse_y - radius/2, radius, radius);
            }
        }
        
        class GameArtist implements drawable {
            @Override
            public void draw() {
                boardArtist.draw();
                playerArtist.draw();
                // player2.artist.draw();
            }

            public void drawWinnerLine() {
                gc.setFill(yellowColor);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(20);
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.strokeLine(winner_start_x, winner_start_y, winner_end_x, winner_end_y);
            }

            public void drawBoard() {
                boardArtist.draw();
            }

            public void drawChip() {
                chipArtist.draw();
            }

            // public void drawDraggableChips() {
            //     // Draw red chips
            //     redDraggableChip.artist.draw();
            //     yellowDraggableChip.artist.draw();

            // }

            // public void drawChipsDragging() {
            //     if (isPlayerDraggingChip(player1, redDraggableChip)) {
            //         currentChip = createChip(1);
            //         currentChip.artist.drawOnPlayer(player1);
            //         // Drawing the player again so it appears on top of the chip
            //         player1.artist.draw();
            //         board.artist.drawPossibleMoves();
            //     }
            //     if (isPlayerDraggingChip(player2, yellowDraggableChip)) {
            //         currentChip = createChip(2);
            //         currentChip.artist.drawOnPlayer(player2);
            //         // Drawing the player again so it appears on top of the chip
            //         player2.artist.draw();
            //         board.artist.drawPossibleMoves();
            //     }
            // }
        }
    }


    

