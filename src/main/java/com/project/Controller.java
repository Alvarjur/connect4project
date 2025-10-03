package com.project;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Controller implements Initializable{
    @FXML
    private Canvas canvas;

    private GraphicsContext gc;
    private double canvas_width = Main.WINDOW_WIDTH;
    private double canvas_height = Main.WINDOW_HEIGHT;
    private double CELL_SIZE = 80;
    private Color boardColor = new Color(0,0,0.5,1);
    private Board board;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas.getGraphicsContext2D();
        canvas_width = canvas.getWidth();
        canvas_height = canvas.getHeight();
        board = new Board(20,50, 6,7);
        

        draw();


    }
    // HAcer que vuelva a dibujar cuando cambia de tamaño la ventana
    public void draw() {
        gc.clearRect(0, 0, canvas_width, canvas_height);
        gc.setFill(boardColor);
        drawBoard(board);
    }
    public void drawBoard(Board board) {
        gc.fillRect(board.x, board.y, board.width + board.margin, board.height + board.space_between);

        for(int i = 0; i < board.cols; i++) {
            for(int j = 0; j < board.rows; j++) {
                double x = board.x + board.margin/2 + i * (CELL_SIZE + board.space_between);
                double y = board.y + board.margin/2 + j * (CELL_SIZE + board.space_between);

                gc.setFill(Color.WHITE);
                gc.fillOval(x, y, CELL_SIZE, CELL_SIZE);
            }
        }


    }

    class Board {
        private double height = 0;
        private double width = 0;
        private int rows;
        private int cols;
        private double x, y;
        private double margin = CELL_SIZE/10;
        private double space_between = margin/2;

        public Board(double x, double y, int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
            
            for(int i = 0; i < cols; i++) {
                this.width += CELL_SIZE;
            }
            for(int i = 0; i < rows; i++) {
                this.height += CELL_SIZE;
            }

            this.width += space_between * cols;
            this.height += space_between * rows;
            this.x = x;
            this.y = y;
        }
    }

    class Chip {
        private double x, y;
        private double diameter;
        private Color color;

        public Chip(double x, double y, double diameter, Color color) {
            this.x = x;
            this.y = y;
            this.diameter = CELL_SIZE;
            this.color = color;
        }
    }

}
