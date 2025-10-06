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

    private GraphicsContext gc;
    private double canvas_width = Main.WINDOW_WIDTH;
    private double canvas_height = Main.WINDOW_HEIGHT;
    private double CELL_SIZE = 80;
    private Color boardColor = new Color(0,0,0.5,1);
    private Color redColor = new Color(0.5,0,0,1);
    private Color blueColor = new Color(0,0,0.5,1);
    private Color yellowColor = new Color(0.7,0.6,0.3,1);
    private Board board;
    private Artist artist = new Artist();

    private ArrayList<Chip> redChips = new ArrayList<Chip>();
    private ArrayList<Chip> yellowChips = new ArrayList<Chip>();
    
    @FXML
    private AnchorPane root;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas.getGraphicsContext2D();
        canvas_width = canvas.getWidth();
        canvas_height = canvas.getHeight();
        board = new Board(6,7);

        
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        canvas.widthProperty().addListener(evt -> draw());
        canvas.heightProperty().addListener(evt -> draw());
        draw();


    }

    // Hacer que vuelva a dibujar cuando cambia de tamaño la ventana
    public void draw() {
        gc.clearRect(0, 0, canvas_width, canvas_height);
        gc.setFill(boardColor);
        artist.draw();


    }


    class Artist implements drawable{
        private static final double draggableChips_x = 650;
        private static final double draggableChips_y = 100;
        
        @Override
        public void draw() {
            drawBoard(board);
            drawDraggableChips();
            
        }

        public void drawBoard(Board board) {
            board.artist.draw();
        }

        public void drawChip(Chip chip) {
            chip.artist.draw();
        }

        public void drawDraggableChips() {
            // Draw red chips
            gc.setFill(redColor);
            gc.fillOval(draggableChips_x, draggableChips_y, CELL_SIZE, CELL_SIZE);
            // Draw blue chips
            gc.setFill(yellowColor);
            gc.fillOval(draggableChips_x + CELL_SIZE + 10, draggableChips_y, CELL_SIZE, CELL_SIZE);

    }
    }

    class Board{
        private BoardArtist artist;
        private int[][] grid; // 0 = empty, 1 = red, 2 = yellow


        public Board(int rows, int cols) {
            grid = new int[rows][cols];
            artist = new BoardArtist(20,100, rows, cols);
            grid[5][6] = 1; // for testing
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
        }
    }

    class Chip {
        private int player; // 1 o 2
        private ChipArtist artist;

        public Chip(int player) {
            this.player = player;
            this.artist = new ChipArtist(Artist.draggableChips_x, Artist.draggableChips_y,CELL_SIZE, this.player);
        }

        class ChipArtist implements drawable {
            private double x, y;
            private double diameter;
            private Color color;
            public ChipArtist(double x, double y, double diameter, int player) {
                if (player == 1) {
                    this.color = redColor;
                } else {
                    this.color = yellowColor;
                }
                this.x = x;
                this.y = y;
                this.diameter = diameter;

            }
            public void draw() {
                gc.setFill(color);
                gc.fillOval(x, y, diameter, diameter);
            }
        }
    }

    public Chip createChip(int player) {
        return new Chip(player);
    }



    public interface drawable {
        void draw();
    }
}
